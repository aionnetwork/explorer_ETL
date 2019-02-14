package aion.dashboard.parser;

import aion.dashboard.blockchain.AionService;
import aion.dashboard.domainobject.*;
import aion.dashboard.exception.AionApiException;
import aion.dashboard.exception.DecodeException;
import aion.dashboard.service.*;
import aion.dashboard.util.ABIDefinitions;
import aion.dashboard.util.ContractEvent;
import aion.dashboard.util.ContractEvents;
import aion.dashboard.util.TimeLogger;
import org.aion.api.IContract;
import org.aion.api.sol.IAddress;
import org.aion.api.type.BlockDetails;
import org.aion.api.type.ContractAbiEntry;
import org.aion.api.type.TxDetails;
import org.aion.api.type.TxLog;
import org.aion.base.type.Address;
import org.aion.base.util.ByteUtil;
import org.aion.base.util.Utils;
import org.aion.mcf.vm.types.Bloom;
import org.aion.zero.impl.core.BloomFilter;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import static aion.dashboard.util.ContractEvents.decodeEventLog;
import static aion.dashboard.util.Utils.*;

/**
 * This service serves to transform the blocks extracted from the blockchain
 */
public class BlockParser {

    private static final Logger GENERAL = LoggerFactory.getLogger("logger_general");
    private static final TimeLogger TIME_LOGGER_BLK = new TimeLogger(BlockParser.class.getName());


    private AionService borrowedService;
    private RollingBlockMean rollingMean;
    private ExecutorService executorService;

    private final Map<String, Token> tokenMap = Collections.synchronizedMap(new HashMap<>());

    public BlockParser(AionService service, RollingBlockMean rollingMean) {

        this.borrowedService = service;

        this.rollingMean = rollingMean;
        executorService = SharedExecutorService.getInstance().getExecutorService();
    }



    /**
     * Extracts the blocks, transaction and tokens from a list of block details
     *
     * @param blockDetailsList the list of blockdetails obtained from the api
     * @return a batch object containing all the parsed data obtained from this trip to the API
     */

    public BatchObject parseBlockDetails(List<BlockDetails> blockDetailsList, long requestPtr) throws SQLException, AionApiException, DecodeException, InterruptedException, ExecutionException {
        BatchObject batchObject = new BatchObject();
        GENERAL.debug("Parsing block range({},{})", blockDetailsList.get(0).getNumber(), blockDetailsList.get(blockDetailsList.size()-1).getNumber());
        TIME_LOGGER_BLK.start();

        for (var block: blockDetailsList){
            Block temp = new Block();
            rollingMean.add(block);

            String txHash = "";
            String lastTxHash = "";
            if(!block.getTxDetails().isEmpty()) {
                txHash = block.getTxDetails().get(0).getTxHash().toString();
                lastTxHash = block.getTxDetails().get(block.getTxDetails().size() - 1).getTxHash().toString();
            }
            boolean canReadEvent = containsReadableEvent(block.getBloom().toBytes());
            if (isValidAddress(block.getMinerAddress().toString())) {
                batchObject.putAccount(new Account.AccountBuilder()
                        .address(block.getMinerAddress().toString())
                        .contract(0)
                        .lastBlockNumber(block.getNumber())
                        .transactionHash(txHash)
                        .build());
            }

            if (block.getTxDetails() != null && !block.getTxDetails().isEmpty()) //if the txdetails list is empty just skip it
                parseTxDetails(block, temp, canReadEvent, txHash, batchObject);
            else{
                //The only changes to block within the parse txDetails that we care about
                temp.setNrgReward(BigDecimal.ZERO);
                temp.setTransactionHashes(new JSONArray().toString());
            }



            temp.setBlockHash(block.getHash().toString());
            temp.setBlockNumber(block.getNumber());
            temp.setBlockTime(block.getBlockTime());
            temp.setBlockTimestamp(block.getTimestamp());
            temp.setBloom(block.getBloom().toString());
            temp.setDifficulty(block.getDifficulty().longValueExact());
            temp.setExtraData(block.getExtraData().toString());
            temp.setMinerAddress(block.getMinerAddress().toString());
            temp.setNonce(block.getNonce().toString(16));
            temp.setNrgConsumed(block.getNrgConsumed());
            temp.setNrgLimit(block.getNrgLimit());
            temp.setNumTransactions((long)block.getTxDetails().size());
            temp.setParentHash(block.getParentHash().toString());
            temp.setReceiptTxRoot(block.getReceiptTxRoot().toString());
            temp.setBlockSize((long) block.getSize());
            temp.setSolution(block.getSolution().toString());
            temp.setStateRoot(block.getStateRoot().toString());
            temp.setTotalDifficulty(block.getTotalDifficulty().longValueExact());
            temp.setTxTrieRoot(block.getTxTrieRoot().toString());
            temp.setLastTransactionHash(lastTxHash);



            batchObject.addBlock(temp);


        }


        var future0 = executorService.submit(
                ()-> rollingMean.computeStableMetricsFrom(blockDetailsList.get(blockDetailsList.size() - 1).getNumber())
        );

        var future1 = executorService.submit(
                ()-> rollingMean.computeRTMetricsFrom(blockDetailsList.get(blockDetailsList.size() -1).getNumber())
        );

        getAccountDetails(batchObject);




        ParserState dbState = new ParserState.parserStateBuilder()
                .id(ParserStateServiceImpl.DB_ID)
                .blockNumber(BigInteger.valueOf(requestPtr))
                .build();

        batchObject.addParserState(dbState);


        TIME_LOGGER_BLK.logTime("Parsed " + blockDetailsList.size() +" Blocks in {}");

        while (!future0.isDone() || !future1.isDone()) Thread.sleep(10);
        future0.get().ifPresent(batchObject::addMetric);
        future1.get().ifPresent(batchObject::addMetric);
        rollingMean.getStates().forEach(batchObject::addParserState);


        return batchObject;
    }//parse block details

    /**
     * Reads the txlog using the utils method
     *
     * @param log          the transaction log that needs to be read
     * @param blockDetails the block which this log belongs to
     * @param txHash      the index of the transaction in the database that contains this event
     * @return the matching event or null
     */
    private static ContractEvent readEvent(TxLog log, BlockDetails blockDetails, String txHash, BatchObject batchObject) throws DecodeException {
        List<ContractAbiEntry> entries = ABIDefinitions.getInstance().getAllEvents();
        for (var entry : entries) {
            Optional<ContractEvent> optionalContractEvent = decodeEventLog(log, entry);

            if (optionalContractEvent.isPresent()) {
                ContractEvent contractEvent = optionalContractEvent.get();
                JSONArray inputList = new JSONArray();
                JSONArray paramList = new JSONArray();
                Event.EventBuilder builder = new Event.EventBuilder();
                builder.setName(contractEvent.getEventName())
                        .setContractAddr(log.getAddress().toString())
                        .setTimestamp(blockDetails.getTimestamp())
                        .setBlockNumber(blockDetails.getNumber())
                        .setTransactionHash(txHash);

                List<String> names = contractEvent.getNames();
                List<String> types = contractEvent.getTypes();
                List<Object> inputs = contractEvent.getInputs();

                for (int i = 0; i < names.size(); i++) {
                    paramList.put(types.get(i) + " " + names.get(i));
                    inputList.put(inputs.get(i));
                }
                builder.setInputList(inputList.toString())
                        .setParameterList(paramList.toString());

                batchObject.addEvent(builder.build());
                return contractEvent;


            }
        }

        return null;

    }//ReadEvent

    /**
     * Checks for the existence of any readable events within a block
     *
     * @param bloomBytes
     * @return
     */
    public static boolean containsReadableEvent(byte[] bloomBytes) {
        bloomBytes = Arrays.copyOf(bloomBytes, bloomBytes.length);// Copying byte array for sanity reasons
        Bloom bloom = new Bloom(bloomBytes);


        List<ContractAbiEntry> entries = ABIDefinitions.getInstance().getAllEvents();

        return entries.parallelStream()
                .anyMatch(entry -> BloomFilter.containsEvent(bloom, ByteUtil.hexStringToBytes(entry.getHashed())));
    }

    /**
     *  Parses the transactions and extracts the logs and contracts
     * @param parsedBlock
     * @param canReadEvent
     * @return
     */
    public void parseTxDetails(BlockDetails blockDetails, Block parsedBlock, boolean canReadEvent, String txHash, BatchObject batchObject) throws SQLException, AionApiException, DecodeException {

        JSONArray blockTxList = new JSONArray();
        List<TxDetails> details = blockDetails.getTxDetails();
        BigDecimal nrgReward = BigDecimal.ZERO;


        for (var tx: details){
            Transaction parsedTransaction = new Transaction();
            String toAddr = tx.getTo().toString();
            String fromAddr = tx.getFrom().toString();
            String contractAddr = tx.getContract().toString();


            //noinspection ConstantConditions
            if (toAddr != null && !toAddr.equals("")) {
                batchObject.putAccount(new Account.AccountBuilder().address(toAddr).contract(0).lastBlockNumber(blockDetails.getNumber()).transactionHash(txHash).build());
            }
            batchObject.putAccount(new Account.AccountBuilder().address(fromAddr).contract(0).lastBlockNumber(blockDetails.getNumber()).transactionHash(txHash).build());
            readContract(blockDetails, txHash, batchObject, tx, toAddr, fromAddr, contractAddr);

            /*
             *
             */
            blockTxList.put( readTxForBlock(blockDetails, tx));

            if (!tx.getLogs().isEmpty())
                parseTxLog(parsedTransaction, tx, batchObject, canReadEvent, txHash, blockDetails);
            else
                parsedTransaction.setTransactionLog("[]");

            parsedTransaction.setBlockHash(blockDetails.getHash().toString());
            parsedTransaction.setBlockNumber(blockDetails.getNumber());
            parsedTransaction.setBlockTimestamp(blockDetails.getTimestamp());
            parsedTransaction.setContractAddr(tx.getContract().toString());
            parsedTransaction.setData(tx.getData().toString());
            parsedTransaction.setFromAddr(tx.getFrom().toString());
            parsedTransaction.setToAddr(tx.getTo().toString());
            parsedTransaction.setNonce(tx.getNonce().toString(16));
            parsedTransaction.setNrgConsumed(tx.getNrgConsumed());
            parsedTransaction.setNrgPrice(tx.getNrgPrice());
            parsedTransaction.setTransactionHash(tx.getTxHash().toString());
            parsedTransaction.setTransactionIndex((long) tx.getTxIndex());
            parsedTransaction.setTransactionTimestamp(tx.getTimestamp());
            parsedTransaction.setTxError(tx.getError());
            parsedTransaction.setValue(new BigDecimal(tx.getValue()));
            parsedTransaction.setApproxValue(approximate(tx.getValue(),18));


            if(!blockDetails.getMinerAddress().equals(tx.getFrom()))
                nrgReward = nrgReward.add(BigDecimal.valueOf(parsedTransaction.getNrgPrice()).multiply(BigDecimal.valueOf(parsedTransaction.getNrgConsumed())));
            batchObject.addTransaction(parsedTransaction);

        }
        parsedBlock.setNrgReward(nrgReward);
        parsedBlock.setApproxNrgReward(approximate(nrgReward, 18));

        StringBuilder hashes = new StringBuilder();//TODO check semantics
        JSONArray txList = new JSONArray(blockTxList.toList());
        hashes.append("[");
        for(int i=0; i< txList.length(); i++) {
            JSONArray jsonArray = txList.getJSONArray(i);
            hashes.append(jsonArray.getString(0));
            if(i != txList.length() - 1) {
                hashes.append(",");
            }
        }
        hashes.append("]");
        parsedBlock.setTransactionHashes(hashes.toString());


    }//parseTxDetails

    private void readContract(BlockDetails blockDetails, String txHash, BatchObject batchObject, TxDetails tx, String toAddr, String fromAddr, String contractAddr) {
        //noinspection ConstantConditions
        if (contractAddr != null && !contractAddr.equals("")) {
            batchObject.putAccount(new Account.AccountBuilder().address(contractAddr).contract(1).lastBlockNumber(blockDetails.getNumber()).transactionHash(txHash).build());
            //noinspection ConstantConditions
            if (toAddr == null || "".equals(toAddr.replaceAll("\\s+", ""))) {
                Contract contract = new Contract.ContractBuilder()
                        .setContractAddr(contractAddr)
                        .setContractCreatorAddr(fromAddr)
                        .setContractTxHash(tx.getTxHash().toString())
                        .setTimestamp(blockDetails.getTimestamp())
                        .setContractName("")
                        .setBlockNumber(blockDetails.getNumber())
                        .build();
                batchObject.addContract(contract);
            }


        }
    }

    private JSONArray readTxForBlock(BlockDetails blockDetails, TxDetails tx) {
        JSONArray txList = new JSONArray();
        txList.put(tx.getTxHash() == null ? JSONObject.NULL : tx.getTxHash().toString());
        txList.put(tx.getFrom() == null ? JSONObject.NULL : tx.getFrom().toString());
        txList.put(tx.getTo() == null ? JSONObject.NULL : tx.getTo().toString());
        txList.put(tx.getValue() == null ? JSONObject.NULL : tx.getValue().toString(16));
        txList.put(blockDetails.getTimestamp());
        txList.put(blockDetails.getNumber());
        return txList;
    }

    private void getAccountDetails(BatchObject batchObject) throws AionApiException {
        Collection<Account> accounts = batchObject.getAccounts();

        final long blockNumber = borrowedService.getBlockNumber();
        for (var account : accounts) {
            account.setBalance(new BigDecimal(borrowedService.getBalance(account.getAddress())));
            account.setLastBlockNumber(blockNumber);
            account.setNonce(borrowedService.getNonce(account.getAddress()).toString(16));
        }


    }



    /*
    Compare topics within the tx log to known topics of a contract
     */

    public static boolean isTokenCreationEvent(String topic) {
        String topicToTest = topic.replace("0x", "");
        return topicToTest.equals(wrapBytes(ContractEvents.CreatedBloomHash));
    }

    public static boolean isTokenMintEvent(String topic) {
        String topicToTest = topic.replace("0x", "");
        return topicToTest.equals(wrapBytes(ContractEvents.MintedBloomHash));
    }

    public static boolean isTokenBurnEvent(String topic) {
        String topicToTest = topic.replace("0x", "");
        return topicToTest.equals(wrapBytes(ContractEvents.BurnedBloomHash));
    }


    private static boolean isValidAddress(String addr) {
        return Utils.isValidAddress(addr);
    }


    private static boolean containsTokenEvent(String topic) {
        return checkForEvent(topic, ABIDefinitions.ATS_CONTRACT);
    }


    private static boolean containsTrsEvent(String topic){
        return checkForEvent(topic, ABIDefinitions.TRS_CONTRACT);
    }

    private static boolean containsBridgeEvent(String topic){
        return checkForEvent(topic, ABIDefinitions.BRIDGE_EVENTS);
    }


    private static boolean checkForEvent(String topic, String contract){
        return ABIDefinitions.getInstance().getABI(contract)
                .parallelStream()
                .anyMatch(e -> e.getHashed().replace("0x", "").equals(topic.replace("0x", "")));
    }

    private static String wrapBytes(byte[] b) {
        return ByteUtil.toHexString(b);}

    /**
     * Parses out the transaction logs and stores as json objects in json arrays.
     * If these logs contain any tokens add them to the batch object to be written to the DB.
     * @param transaction object in which the logs are to be stored
     * @param txDetail the object that owns the txlogs
     * @param batchObject the object that contains all the data that is to be writen in the database
     * @param canReadEvent a boolean indicating whether the logs contain a token event
     */

    public void parseTxLog(Transaction transaction, TxDetails txDetail, BatchObject batchObject, boolean canReadEvent, String txHash, BlockDetails blockDetails) throws SQLException, AionApiException, DecodeException {

        List<TxLog> logs = txDetail.getLogs();
        Account.AccountBuilder builder = new Account.AccountBuilder().contract(0).lastBlockNumber(blockDetails.getNumber()).transactionHash(txHash);
        int transferCount = 0;
        InternalTransfer.InternalTransferBuilder internalTransferBuilder = new InternalTransfer.InternalTransferBuilder();
        JSONArray txLogs = new JSONArray();
        for (var txLog : logs) {

            JSONObject txLogObject = new JSONObject();
            JSONArray topicsArr = new JSONArray();
            if (txLog.getTopics() !=  null && !txLog.getTopics().isEmpty()) {

                if (canReadEvent) {

                    transferCount = parseEvents(txDetail, batchObject, blockDetails, builder, transferCount, internalTransferBuilder, txLog);
                }
                else {
                    findAddressInTopics(txLog.getTopics()).stream().map(address -> builder.address(address).build()).forEach(batchObject::putAccount);
                }

                for (var topic : txLog.getTopics()){
                    topicsArr.put(topic);
                }
            }

            txLogObject.put("address", txLog.getAddress().toString());
            txLogObject.put("data", txLog.getData());


            txLogObject.put("topics", topicsArr);
            txLogs.put(txLogObject);


        }




        transaction.setTransactionLog(txLogs.toString());
    }//parseTxLog

    private int parseEvents(TxDetails txDetail, BatchObject batchObject, BlockDetails blockDetails, Account.AccountBuilder builder, int transferCount, InternalTransfer.InternalTransferBuilder internalTransferBuilder, TxLog txLog) throws DecodeException, SQLException, AionApiException {
        String txHash = txDetail.getTxHash().toString();
        ContractEvent event1 = readEvent(txLog, blockDetails, txHash, batchObject);


        if (event1 != null && containsTokenEvent(txLog.getTopics().get(0))) {

            Contract contract = ContractServiceImpl
                    .getInstance()
                    .selectContractsByContractAddr(event1.getAddress());
            if (contract == null) {

                //If this contract is not found parsing should not continue.
                contract = batchObject.getContracts().stream().filter(e-> e.getContractAddr().equals(event1.getAddress())).findFirst().orElseThrow();
            }

            ABIDefinitions abiDefinitions = ABIDefinitions.getInstance();
            IContract contractFromAbi = borrowedService
                    .getContract(
                            Address.wrap(contract.getContractCreatorAddr()),
                            Address.wrap(contract.getContractAddr()),
                            abiDefinitions.getJSONString(ABIDefinitions.ATS_CONTRACT));
            boolean isSupplyUpdate = event1.getEventName().equals("Froze") ||
                    event1.getEventName().equals("Thawed") ||
                    event1.getEventName().equals("Minted") ||
                    event1.getEventName().equals("Burned");

            getToken(event1.getAddress(), batchObject, contract, contractFromAbi, isSupplyUpdate);


            if (event1.getEventName().equals("Sent")) {

                var fromOptional = event1.getInput("from", String.class);
                var toOptional = event1.getInput("to", String.class);
                if (fromOptional.isPresent() && toOptional.isPresent()) {
                    String fromAddr = fromOptional.get();
                    String toAddr = toOptional.get();


                    parseTransfer(event1, batchObject, txHash, blockDetails);
                    updateTokenBalances(batchObject,
                            contractFromAbi,
                            toAddr,
                            fromAddr,
                            contract,
                            blockDetails.getNumber());
                }
            }

        } else if (event1 != null && containsTrsEvent(txLog.getTopics().get(0)) && event1.getEventName().equals("Withdraws")){
            var transfer = internalTransferBuilder.setBlockNumber(blockDetails.getNumber())
                    .setTransactionHash(txHash)
                    .setFromAddr(event1.getAddress())
                    .setTimestamp(blockDetails.getTimestamp())
                    .setTransferCount(transferCount)
                    .setToAddr(event1.getInput("who", String.class).orElse(""))
                    .setValueTransferred(new BigDecimal(event1.getInput("amount", BigInteger.class).orElse(BigInteger.ZERO)))
                    .build();
            batchObject.addInternalTransfer(transfer);
            transferCount ++;

        } else if (event1 != null && event1.getEventName().equalsIgnoreCase("Distributed") &&containsBridgeEvent(txLog.getTopics().get(0)) ){
                        var transfer = internalTransferBuilder.setBlockNumber(blockDetails.getNumber())
                                .setTransactionHash(txDetail.getTxHash().toString())
                                .setFromAddr(event1.getAddress())
                                .setTimestamp(blockDetails.getTimestamp())
                                .setToAddr(event1.getInput("param1", String.class).orElseThrow())
                                .setValueTransferred(new BigDecimal(event1.getInput("param2", BigInteger.class).orElseThrow()))
                                .setTransferCount(transferCount)
                                .build();

                        batchObject.addInternalTransfer(transfer);
                        transferCount++;
        }

        if (event1 !=null) {
            findAddressInEvent(event1).stream()
                    .map(address -> builder.address(address).build())
                    .forEach(batchObject::putAccount);
        }
        return transferCount;
    }

    /**
     * This method gets the contract from the blockchain and gets information needed for the database
     *
     * @param contractAddr
     * @return
     * @throws AionApiException
     */
    public Token getToken(Address contractAddr, TxDetails txDetails) throws AionApiException {


        ABIDefinitions abiDefinitions = ABIDefinitions.getInstance();
        IContract contract = borrowedService.getContract(txDetails.getFrom(),
                contractAddr,
                abiDefinitions.getJSONString(ABIDefinitions.ATS_CONTRACT));

        var token = new Token.TokenBuilder();


        return token.contractAddress(contractAddr.toString())
                .creatorAddress(txDetails.getFrom().toString())
                .name((String) borrowedService.callContractFunction(contract, "name").get(0))
                .symbol((String) borrowedService.callContractFunction(contract, "symbol").get(0))
                .granularity((BigInteger) borrowedService.callContractFunction(contract, "granularity").get(0))
                .transactionHash(txDetails.getTxHash().toString())
                .totalSupply((BigInteger) borrowedService.callContractFunction(contract, "totalSupply").get(0))
                .totalLiquidSupply((BigInteger) borrowedService.callContractFunction(contract, "liquidSupply").get(0))
                .timestamp(txDetails.getTimestamp())
                .build();

    }//getToken


    /**
     * @param contractAddress
     * @param batchObject
     * @param contract
     * @param contractFromAbi
     * @param isSupplyUpdate
     * @throws SQLException
     * @throws AionApiException
     */
    private void getToken(String contractAddress, BatchObject batchObject, Contract contract, IContract contractFromAbi, boolean isSupplyUpdate) throws SQLException, AionApiException {


        try {
            Token tokenDB = tokenMap.getOrDefault(contractAddress, TokenServiceImpl.getInstance().getByContractAddr(contractAddress));

            if (tokenDB !=null){
                tokenMap.putIfAbsent(tokenDB.getContractAddress(),tokenDB);
            }
            if ((tokenDB != null && !isSupplyUpdate) || batchObject.getTokenSet()
                    .stream()
                    .anyMatch(token -> token.getContractAddress().equals(contractAddress))) {
                return;//if this token already exists in the DB or was just found skip it
            }


            Token.TokenBuilder tokenBuilder = new Token.TokenBuilder();

            var granularity=tokenDB == null ? (BigInteger)  borrowedService
                    .callContractFunction(contractFromAbi, "granularity")
                    .get(0) : tokenDB.getGranularity();
            tokenBuilder.contractAddress(contractAddress)
                    .creatorAddress(contract.getContractCreatorAddr())
                    .transactionHash(contract.getContractTxHash())
                    .name(tokenDB == null ? truncate((String) borrowedService
                            .callContractFunction(contractFromAbi, "name")
                            .get(0)) : tokenDB.getTokenName() )
                    .symbol(tokenDB == null ? truncate((String) borrowedService
                            .callContractFunction(contractFromAbi, "symbol")
                            .get(0)) : tokenDB.getSymbol())
                    .granularity(granularity)
                    .totalSupply((BigInteger) borrowedService
                            .callContractFunction(contractFromAbi, "totalSupply")
                            .get(0))
                    .totalLiquidSupply((BigInteger) borrowedService
                            .callContractFunction(contractFromAbi, "liquidSupply")
                            .get(0))
                    .timestamp(contract.getTimestamp())
                    .setTokenDecimal(granularityToTknDec(granularity));
            batchObject.addToken(tokenBuilder.build());
            tokenMap.putIfAbsent(contractAddress, tokenBuilder.build());

        } catch (AionApiException | SQLException e) {
            GENERAL.debug("Threw an exception in get token: ", e);
            throw e;
        }
        catch (IllegalStateException ignored){
            //
        }

    }

    /**
     * Converts a transfer event to a transfer DO that can be stored in the DB
     * @param event
     * @param batchObject
     * @param blockDetails
     * @param txHash
     */
    private void parseTransfer(ContractEvent event, BatchObject batchObject, String txHash, BlockDetails blockDetails) {
        Token token = tokenMap.get(event.getAddress());


        var optionalAmount = event.getInput("amount", BigInteger.class);
        var optionalOperator= event.getInput("operator", String.class);
        var optionalTo = event.getInput("to", String.class);
        var optionalFrom = event.getInput("from", String.class);
        if (token !=null && optionalAmount.isPresent() && optionalOperator.isPresent() && optionalTo.isPresent() && optionalFrom.isPresent()) {
            var rawValue = optionalAmount.get();
            var scaledValue = scaleTokenValue(rawValue, token.getTokenDecimal());
            TokenTransfers.TransferBuilder builder = new TokenTransfers.TransferBuilder();
            builder.setTransactionTimestamp(blockDetails.getTimestamp())
                    .setContractAddress(event.getAddress())
                    .setTransactionHash(txHash)
                    .setBlockNumber(blockDetails.getNumber())
                    .setOperator(optionalOperator.get())
                    .setToAddress(optionalTo.get())
                    .setFromAddress(optionalFrom.get())
                    .setScaledTokenValue(scaledValue)
                    .setRawValue(rawValue.toString())
                    .setTokendecimal(token.getTokenDecimal())
                    .setGranularity(new BigDecimal(token.getGranularity()));

            batchObject.addTransfer(builder.build());
        }
        // add the token to the db if it does not already exist
    }

    /**
     * Adds any addresses that were incuded in a sent event to the token balance table
     * @param batchObject
     * @param contractFromAbi
     * @param to
     * @param from
     * @param contract
     * @param blockNumber
     * @throws AionApiException
     */
    private void updateTokenBalances(BatchObject batchObject, IContract contractFromAbi, String to, String from, Contract contract, long blockNumber) throws AionApiException {
        Token token = tokenMap.get(contract.getContractAddr());

        if(token != null) {
            BigInteger granularity = token.getGranularity();


            int tokenDecimal = token.getTokenDecimal();
            //Getting balance of recipient
            BigInteger rawToBalance = (BigInteger) borrowedService.callContractFunction(contractFromAbi, "balanceOf", IAddress.copyFrom(to)).get(0);
            BigDecimal scaledToBalance = scaleTokenValue(rawToBalance, tokenDecimal);
            //Getting balance of sender
            BigInteger rawFromBalance = (BigInteger) borrowedService.callContractFunction(contractFromAbi, "balanceOf", IAddress.copyFrom(from)).get(0);
            BigDecimal scaledFromBalance = scaleTokenValue(rawToBalance, tokenDecimal);

            TokenHolders.TokenBalanceBuilder builder = new TokenHolders.TokenBalanceBuilder();
            builder.setHolderAddress(to)
                    .setContractAddress(contract.getContractAddr())
                    .setBlockNumber(blockNumber)
                    .setScaledBalance(scaledToBalance)
                    .setRawBalance(rawToBalance.toString())
                    .setTokenDecimal(tokenDecimal)
                    .setTokenGranularity(granularity);
            batchObject.addTokenBalance(builder.build());


            builder.setHolderAddress(from)
                    .setScaledBalance(scaledFromBalance)
                    .setRawBalance(rawFromBalance.toString());
            batchObject.addTokenBalance(builder.build());

        }
    }

    private List<String> findAddressInEvent(ContractEvent event){

        var zipped = zip(event.getTypes(), event.getInputs());
        return zipped ==null ? Collections.emptyList():zipped.parallelStream()
                .filter(t -> t._1().equalsIgnoreCase("address"))
                .map(t -> (String)t._2())
                .map(aion.dashboard.util.Utils::getValidAddress)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private List<String> findAddressInTopics(List<String> topics){
        return topics.parallelStream()
                .map(aion.dashboard.util.Utils::getValidAddress)
                .filter(Optional::isPresent)
                .map(Optional::get).collect(Collectors.toList());
    }
}