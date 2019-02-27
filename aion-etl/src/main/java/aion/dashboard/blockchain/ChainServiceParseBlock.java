package aion.dashboard.blockchain;

import aion.dashboard.domainobject.*;
import aion.dashboard.exception.AionApiException;
import aion.dashboard.exception.DecodeException;
import aion.dashboard.service.ContractServiceImpl;
import aion.dashboard.service.ParserStateServiceImpl;
import aion.dashboard.service.TokenServiceImpl;
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
import java.math.MathContext;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static aion.dashboard.util.ContractEvents.decodeEventLog;
import static aion.dashboard.util.Utils.fromWei;
import static aion.dashboard.util.Utils.truncate;

/**
 * This service serves to transform the blocks extracted from the blockchain
 */
public class ChainServiceParseBlock {

    private static final Logger GENERAL = LoggerFactory.getLogger("logger_general");
    private static final TimeLogger TIME_LOGGER_BLK = new TimeLogger(ChainServiceParseBlock.class.getName());


    private AionService borrowedService;


    public ChainServiceParseBlock(AionService service) {

        this.borrowedService = service;
    }



    /**
     * Extracts the blocks, transaction and tokens from a list of block details
     *
     * @param blockDetailsList the list of blockdetails obtained from the api
     * @return a batch object containing all the parsed data obtained from this trip to the API
     */

    public BatchObject parseBlockDetails(List<BlockDetails> blockDetailsList, long requestPtr, long txIndex) throws SQLException, AionApiException, DecodeException {
        BatchObject batchObject = new BatchObject();
        txIndex ++;
        GENERAL.debug("Parsing block range({},{})", blockDetailsList.get(0).getNumber(), blockDetailsList.get(blockDetailsList.size()-1).getNumber());
        TIME_LOGGER_BLK.start();
        for (var block: blockDetailsList){
            Block temp = new Block();


            temp.setTransactionId(BigInteger.valueOf(txIndex));

            boolean canReadEvent = containsReadableEvent(block.getBloom().toBytes());
            if (isValidAddress(block.getMinerAddress().toString())) {
                batchObject.putBalance(new Balance.BalanceBuilder()
                        .address(block.getMinerAddress().toString())
                        .contract(0)
                        .lastBlockNumber(block.getNumber())
                        .transactionId(txIndex)
                        .build());
            }

            if (block.getTxDetails() != null && !block.getTxDetails().isEmpty()) {//if the txdetails list is empty just skip it
                parseTxDetails(block, temp, canReadEvent, batchObject, txIndex);
                txIndex += block.getTxDetails().size();
            }
            else{
                //The only changes to block within the parse txDetails that we care about
                temp.setNrgReward(BigInteger.ZERO);
                temp.setTransactionList(new JSONArray().toString());
            }



            temp.setBlockHash(block.getHash().toString());
            temp.setBlockNumber(block.getNumber());
            temp.setBlockTime(block.getBlockTime());
            temp.setBlockTimestamp(block.getTimestamp());
            temp.setBloom(block.getBloom().toString());
            temp.setDifficulty(block.getDifficulty().toString(16));
            temp.setExtraData(block.getExtraData().toString());
            temp.setMinerAddress(block.getMinerAddress().toString());
            temp.setNonce(block.getNonce().toString(16));
            temp.setNrgConsumed(block.getNrgConsumed());
            temp.setNrgLimit(block.getNrgLimit());
            temp.setNumTransactions((long)block.getTxDetails().size());
            temp.setParentHash(block.getParentHash().toString());
            temp.setReceiptTxRoot(block.getReceiptTxRoot().toString());
            temp.setSize((long) block.getSize());
            temp.setSolution(block.getSolution().toString());
            temp.setStateRoot(block.getStateRoot().toString());
            temp.setTotalDifficulty(block.getTotalDifficulty().toString(16));
            temp.setTxTrieRoot(block.getTxTrieRoot().toString());



            batchObject.addBlock(temp);
        }
        ParserState parserState = new ParserState.ParserStateBuilder()
                .id(ParserStateServiceImpl.DB_ID)
                .blockNumber(BigInteger.valueOf(requestPtr))
                .transactionID(
                        batchObject.getTransactions().isEmpty() ?
                                BigInteger.valueOf(txIndex - 1) :
                                batchObject.getTransactions().get(batchObject.getTransactions().size() -1).getId()
                ).build();

        batchObject.setParser_state(parserState);
        getBalanceDetails(batchObject);

        TIME_LOGGER_BLK.logTime("Parsed " + blockDetailsList.size() +" Blocks in {}");

        return batchObject;
    }//parse block details

    /**
     * Reads the txlog using the utils method
     *
     * @param log          the transaction log that needs to be read
     * @param blockDetails the block which this log belongs to
     * @param txIndex      the index of the transaction in the database that contains this event
     * @return the matching event or null
     */
    private static ContractEvent readEvent(TxLog log, BlockDetails blockDetails, long txIndex, BatchObject batchObject) throws DecodeException {
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
                        .setTransactionID(txIndex);

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
    public void parseTxDetails(BlockDetails blockDetails, Block parsedBlock, boolean canReadEvent, BatchObject batchObject, long txIndex) throws SQLException, AionApiException, DecodeException {

        JSONArray blockTxList = new JSONArray();
        List<TxDetails> details = blockDetails.getTxDetails();
        BigInteger nrgReward = BigInteger.ZERO;


        for (var tx: details){
            Transaction parsedTransaction = new Transaction();
            String toAddr = tx.getTo().toString();
            String fromAddr = tx.getFrom().toString();
            String contractAddr = tx.getContract().toString();


            //noinspection ConstantConditions
            if (toAddr != null && !toAddr.equals("")) {
                batchObject.putBalance(
                        new Balance.BalanceBuilder()
                                .address(toAddr)
                                .contract(0)
                                .lastBlockNumber(blockDetails.getNumber())
                                .transactionId(txIndex).build());
            }

            batchObject.putBalance(
                    new Balance.BalanceBuilder()
                            .address(fromAddr)
                            .contract(0)
                            .lastBlockNumber(blockDetails.getNumber())
                            .transactionId(txIndex).build());

            //noinspection ConstantConditions
            if (contractAddr != null && !contractAddr.equals("")) {
                batchObject.putBalance(
                        new Balance.BalanceBuilder()
                                .address(contractAddr)
                                .contract(1)
                                .lastBlockNumber(blockDetails.getNumber())
                                .transactionId(txIndex).build());

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
            blockTxList.put(storeTxInJSONArr(blockDetails, tx));

            if (!tx.getLogs().isEmpty()) {
                parseTxLog(parsedTransaction, tx, batchObject, canReadEvent, blockDetails, txIndex);
            } else {
                parsedTransaction.setTransactionLog("[]");
            }
            parsedTransaction.setId(BigInteger.valueOf(txIndex));
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
            parsedTransaction.setValue(tx.getValue().toString(16));


            if(!blockDetails.getMinerAddress().equals(tx.getFrom()))
                nrgReward = nrgReward.add(
                        BigInteger.valueOf(parsedTransaction.getNrgPrice())
                                .multiply(BigInteger.valueOf(parsedTransaction.getNrgConsumed())));


            txIndex++;
            batchObject.addTransaction(parsedTransaction);

        }
        parsedBlock.setNrgReward(nrgReward);
        parsedBlock.setTransactionList(blockTxList.toString());


    }//parseTxDetails

    private JSONArray storeTxInJSONArr(BlockDetails blockDetails, TxDetails tx) {
        /*
         * store the tx information in the block for easy access
         * This was removed in subsequent releases to reduce the size of a block
         */
        JSONArray txList = new JSONArray();
        txList.put(tx.getTxHash() == null ? JSONObject.NULL : tx.getTxHash().toString());
        txList.put(tx.getFrom() == null ? JSONObject.NULL : tx.getFrom().toString());
        txList.put(tx.getTo() == null ? JSONObject.NULL : tx.getTo().toString());
        txList.put(tx.getValue() == null ? JSONObject.NULL : tx.getValue().toString(16));
        txList.put(blockDetails.getTimestamp());
        txList.put(blockDetails.getNumber());
        return txList;
    }

    private void getBalanceDetails(BatchObject batchObject) throws AionApiException {
        Collection<Balance> balances = batchObject.getBalances();

        for (var balance : balances) {
            balance.setBalance(fromWei(borrowedService.getBalance(balance.getAddress())));
            balance.setLastBlockNumber(borrowedService.getBlockNumber());
            balance.setNonce(borrowedService.getNonce(balance.getAddress()));
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
        return ABIDefinitions.getInstance().atstokenabientries
                .stream()
                .anyMatch(contractAbiEntry ->
                        contractAbiEntry.getHashed().replace("0x", "")
                                .equals(topic.replace("0x", "")));
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

    public void parseTxLog(Transaction transaction, TxDetails txDetail, BatchObject batchObject, boolean canReadEvent, BlockDetails blockDetails, long txIndex) throws SQLException, AionApiException, DecodeException {

        List<TxLog> logs = txDetail.getLogs();

        JSONArray txLogs = new JSONArray();
        for (var txLog : logs) {

            JSONObject txLogObject = new JSONObject();
            JSONArray topicsArr = new JSONArray();
            if (txLog.getTopics() !=  null && !txLog.getTopics().isEmpty()) {
                parseEventInformation(batchObject, canReadEvent, blockDetails, txIndex, txLog);

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

    private void parseEventInformation(BatchObject batchObject, boolean canReadEvent, BlockDetails blockDetails, long txIndex, TxLog txLog) throws DecodeException, SQLException, AionApiException {
        if (canReadEvent) {
            ContractEvent event1 = readEvent(txLog, blockDetails, txIndex, batchObject);


            if (containsTokenEvent(txLog.getTopics().get(0)) && event1!=null) {

                Contract contract = ContractServiceImpl
                        .getInstance()
                        .selectContractsByContractAddr(event1.getAddress());
                if (contract == null) {
                    contract = batchObject.getContracts().stream().filter(e-> e.getContractAddr().equals(event1.getAddress())).findFirst().orElseThrow();
                }

                ABIDefinitions abiDefinitions = ABIDefinitions.getInstance();
                IContract contractFromAbi = borrowedService
                        .getContract(
                                Address.wrap(contract.getContractCreatorAddr()),
                                Address.wrap(contract.getContractAddr()),
                                abiDefinitions.atstokenabi);

                getToken(event1.getAddress(), batchObject, contract, contractFromAbi);

                parseTokenTransfer(batchObject, blockDetails, txIndex, event1, contract, contractFromAbi);

            }
            else if (event1 !=null && isBridgeTransfer(event1) ){

                event1.getInput(1, String.class).ifPresent(
                        address -> batchObject.putBalance(new Balance.BalanceBuilder()
                                .address(address)
                                .contract(0)
                                .lastBlockNumber(blockDetails.getNumber())
                                .transactionId(txIndex).build())
                );
            }

        }
    }

    private void parseTokenTransfer(BatchObject batchObject, BlockDetails blockDetails, long txIndex, ContractEvent event1, Contract contract, IContract contractFromAbi) throws AionApiException {
        if (event1.getEventName().equals("Sent")) {

            String fromAddr = event1.getInput("from", String.class).orElseThrow();
            String toAddr = event1.getInput("to", String.class).orElseThrow();


            if (isValidAddress(fromAddr)) {
                batchObject.putBalance(new Balance.BalanceBuilder()
                        .address(fromAddr)
                        .contract(0)
                        .lastBlockNumber(blockDetails.getNumber())
                        .transactionId(txIndex).build());
            }
            if (isValidAddress(toAddr)) {
                batchObject.putBalance(new Balance.BalanceBuilder()
                        .address(toAddr)
                        .contract(0)
                        .lastBlockNumber(blockDetails.getNumber())
                        .transactionId(txIndex).build());
            }

            parseTransfer(event1, batchObject, blockDetails, txIndex);
            updateTokenBalances(batchObject,
                    contractFromAbi,
                    toAddr,
                    fromAddr,
                    contract,
                    blockDetails.getNumber());
        }
    }

    private boolean isBridgeTransfer(ContractEvent event1) {
        ABIDefinitions definitions = ABIDefinitions.getInstance();
        List<ContractAbiEntry> entries = definitions.getABI("BridgeEvents");
        return entries.stream().anyMatch(e -> e.getHashed().equals(event1.getSignatureHash())) &&event1.getEventName().equals("Distributed");
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
        IContract contract = borrowedService.getContract(txDetails.getFrom(),contractAddr, abiDefinitions.atstokenabi);

        var token = new Token.TokenBuilder();



        token.contractAddress(contractAddr.toString());
        token.creatorAddress(txDetails.getFrom().toString());
        token.name((String) borrowedService
                .callContractFunction(contract,"name")
                .get(0));
        token.symbol((String) borrowedService
                .callContractFunction(contract,"symbol")
                .get(0));
        token.granularity(BigDecimal.valueOf((long) borrowedService
                .callContractFunction(contract, "granularity")
                .get(0)));
        token.transactionHash(txDetails.getTxHash().toString());
        token.totalSupply(BigInteger.valueOf((long) borrowedService
                .callContractFunction(contract, "totalSupply")
                .get(0)));
        token.totalLiquidSupply(BigInteger.valueOf((long) borrowedService
                .callContractFunction(contract, "liquidSupply")
                .get(0)));
        token.timestamp(txDetails.getTimestamp());

        return token.build();
    }//getToken


    /**
     * @param contractAddress
     * @param batchObject
     * @param contract
     * @param contractFromAbi
     * @throws SQLException
     * @throws AionApiException
     */
    private void getToken(String contractAddress, BatchObject batchObject, Contract contract, IContract contractFromAbi) throws SQLException, AionApiException {


        try {
            if (TokenServiceImpl.getInstance().getByContractAddr(contractAddress) != null ||
                    batchObject.getTokenSet()
                            .stream()
                            .anyMatch(token -> token.getContractAddress().equals(contractAddress))) {
                return;//if this token already exists in the DB or was just found skip it
            }


            Token.TokenBuilder tokenBuilder = new Token.TokenBuilder();


            tokenBuilder.contractAddress(contractAddress)
                    .creatorAddress(contract.getContractCreatorAddr())
                    .transactionHash(contract.getContractTxHash())
                    .name(truncate((String) borrowedService
                            .callContractFunction(contractFromAbi, "name")
                            .get(0)))
                    .symbol(truncate((String) borrowedService
                            .callContractFunction(contractFromAbi, "symbol")
                            .get(0)))
                    .granularity(BigDecimal.valueOf((long) borrowedService
                            .callContractFunction(contractFromAbi, "granularity")
                            .get(0)))
                    .totalSupply(BigInteger.valueOf((long) borrowedService
                            .callContractFunction(contractFromAbi, "totalSupply")
                            .get(0)))
                    .totalLiquidSupply(BigInteger.valueOf((long) borrowedService
                            .callContractFunction(contractFromAbi, "liquidSupply")
                            .get(0)))
                    .timestamp(contract.getTimestamp());
            batchObject.addToken(tokenBuilder.build());

        } catch (AionApiException | SQLException e) {
            GENERAL.debug("Threw an exception in get token: ", e);
            throw e;
        }
        catch (RuntimeException ignored){

        }

    }

    /**
     * Converts a transfer event to a transfer DO that can be stored in the DB
     * @param event
     * @param batchObject
     * @param blockDetails
     * @param txIndex
     */
    private void parseTransfer(ContractEvent event, BatchObject batchObject, BlockDetails blockDetails, long txIndex) {


        Transfer.TransferBuilder builder = new Transfer.TransferBuilder();
        builder.setTransactionTimestamp(blockDetails.getTimestamp())
                .setContractAddress(event.getAddress())
                .setTransactionId(txIndex)
                .setBlockNumber(blockDetails.getNumber())
                .setOperator(event.getInput("operator", String.class).orElseThrow())
                .setToAddress(event.getInput("to", String.class).orElseThrow())
                .setFromAddress(event.getInput("from", String.class).orElseThrow())
                .setTokenValue(BigDecimal.valueOf(event.getInput("amount", BigInteger.class).orElseThrow().longValue()));

        batchObject.addTransfer(builder.build());

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

        BigDecimal granularity = BigDecimal.valueOf((long) borrowedService
                .callContractFunction(contractFromAbi, "granularity")
                .get(0));

        System.out.println("Granularity: " + granularity);
        TokenBalance.TokenBalanceBuilder builder = new TokenBalance.TokenBalanceBuilder();
        builder.setHolderAddress(to)
                .setContractAddress(contract.getContractAddr())
                .setBlockNumber(blockNumber);

        BigDecimal balance = BigDecimal.valueOf((long) borrowedService.callContractFunction(contractFromAbi, "balanceOf", IAddress.copyFrom(to)).get(0));
        System.out.println("balance: "+ balance);


        builder.setBalance(balance.divide(granularity,18 ,MathContext.DECIMAL64.getRoundingMode()));
        batchObject.addTokenBalance(builder.build());

        balance = BigDecimal.valueOf((long) borrowedService.callContractFunction(
                contractFromAbi,
                "balanceOf",
                IAddress.copyFrom(from)).get(0));

        System.out.println("balance: "+ balance);        builder.setHolderAddress(from)
                .setBalance(balance.divide(granularity,18 ,MathContext.DECIMAL64.getRoundingMode()));
        batchObject.addTokenBalance(builder.build());


    }

}
