package aion.dashboard.service;

import aion.dashboard.db.DbConnectionPool;
import aion.dashboard.domainobject.*;
import org.aion.base.type.Address;
import org.aion.base.util.ByteUtil;
import org.aion.crypto.HashUtil;
import org.apache.commons.lang3.RandomStringUtils;
import org.json.JSONArray;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DBServicesTest {


    void tokenBalanceTest() throws SQLException {

        try (Connection con = DbConnectionPool.getConnection()) {
            con.createStatement().execute("Truncate table token_balance");

            TokenBalanceService service = TokenBalanceServiceImpl.getInstance();
            TokenBalance.TokenBalanceBuilder builder = new TokenBalance.TokenBalanceBuilder();
            TokenBalance balance = builder
                    .setBalance(BigDecimal.ONE)
                    .setBlockNumber(1)
                    .setContractAddress("0000001")
                    .setHolderAddress(Address.ZERO_ADDRESS().toString())
                    .build();

            assertTrue(service.save(balance));

            // con.createStatement().execute("Truncate table balance");

            List<TokenBalance> tokenBalances = Collections.nCopies(10, balance);
            service.save(tokenBalances);


            List<TokenBalance> res = service.getTokensByBlockNumber(1);

            assertFalse(res.isEmpty());
            assertTrue(res.stream().allMatch(tokenBalance -> tokenBalance.equals(balance)));

        }
    }


    void transferTest() throws SQLException {
        try (Connection con = DbConnectionPool.getConnection()) {
            con.createStatement().execute("truncate table transfer");
            Transfer.TransferBuilder builder = new Transfer.TransferBuilder();
            TransferService service = TransferServiceImpl.getInstance();
            builder.setBlockNumber(1L)
                    .setContractAddress(Address.ZERO_ADDRESS().toString())
                    .setFromAddress(String.valueOf(1000001))
                    .setOperator(String.valueOf(1000001))
                    .setToAddress(String.valueOf(20000002))
                    .setTokenValue(BigDecimal.ONE)
                    .setTransactionTimestamp(System.currentTimeMillis())
                    .setTransactionId(0);

            Transfer transfer = builder.build();


            assertTrue(service.save(transfer));


            List<Transfer> transfers = Collections.nCopies(10, transfer);



            assertTrue(service.save(transfers));


        }
    }

    void eventServiceImpl() throws SQLException {


        try(Connection con = DbConnectionPool.getConnection()){
            con.createStatement().execute("truncate table event");
            Event.EventBuilder builder = new Event.EventBuilder();

            EventService service = EventServiceImpl.getInstance();


            JSONArray paramList = new JSONArray();
            paramList.put("string name");
            paramList.put("uint128 age");

            JSONArray inputList = new JSONArray();
            inputList.put("Bob");
            inputList.put(5);


            builder.setBlockNumber(1)
                    .setContractAddr("00000010000")
                    .setName("Test_Event")
                    .setTimestamp(System.currentTimeMillis())
                    .setParameterList(paramList.toString())
                    .setInputList(inputList.toString())
                    .setTransactionID(1L);

            Event event = builder.build();


            assertTrue(service.save(event));


            List<Event> events = Collections.nCopies(10, event);

            assertTrue(service.save(events));

        }
    }


    void contractTest() throws SQLException {
        try(Connection con = DbConnectionPool.getConnection()){
            con.createStatement().execute("truncate table contract");
            ContractService service = ContractServiceImpl.getInstance();
            Contract.ContractBuilder builder = new Contract.ContractBuilder();
            builder.setContractName("Avocato")
                    .setContractTxHash(ByteUtil.toHexString(HashUtil.h256(RandomStringUtils.random(48).getBytes())))
                    .setBlockNumber(1)
                    .setTimestamp(System.currentTimeMillis())
                    .setContractAddr(Address.ZERO_ADDRESS().toString())
                    .setContractCreatorAddr("00000001234");

            Contract contract = builder.build();


            assertTrue(service.save(contract));

            List<Contract> contracts = Collections.nCopies(10, contract);

            assertTrue(service.save(contracts));
            contracts = new ArrayList<>();

            for (int i =0 ; i< 10; i++ ){

                builder.setContractName("Avocato")
                        .setContractTxHash(ByteUtil.toHexString(HashUtil.EMPTY_DATA_HASH))
                        .setBlockNumber(1)
                        .setTimestamp(System.currentTimeMillis())
                        .setContractAddr(ByteUtil.toHexString(HashUtil.h256(RandomStringUtils.random(45).getBytes())))
                        .setContractCreatorAddr("00000001234");

                contracts.add(builder.build());
            }

            assertTrue(service.save(contracts));
            Contract res = service.selectContractsByContractAddr(Address.ZERO_ADDRESS().toString());
            assertNotNull(res);
            assertEquals(res, contract);

            Contract res0 = service.selectContractsByContractAddr("5");


            assertNull(res0);






        }
    }



    void graphingIntegrityTest() throws SQLException {

        try(Connection con = DbConnectionPool.getConnection();
            Statement stmt = con.createStatement()){
            stmt.execute("truncate table graphing");
            ParserStateServiceImpl.getInstance().updateGraphingState(BigInteger.ZERO);

            List<Graphing> blocksMined = new ArrayList<>();
            Graphing.GraphingBuilder builder = new Graphing.GraphingBuilder()
                    .setDate(5)
                    .setMonth(5)
                    .setYear(2018)
                    .setTimestamp(System.currentTimeMillis())
                    .setGraphType(Graphing.GraphType.BLOCKS_MINED)
                    .setDetail("");


            int inconsistentRecord =30;
            int blockCount;
            int total = 0;
            for (int i =0;i<100;i++){
                if (i == 30) {
                    blockCount = 2;


                }
                else {
                    blockCount = 300;
                }

                total += blockCount;
                builder.setValue(BigDecimal.valueOf(blockCount)).setBlockNumber((long) total);

                System.out.println("Iteration: "+ i +" Block Number: "+ total + " Blocks mined: " + blockCount);


                blocksMined.add(builder.build());
            }


            GraphingServiceImpl.getInstance().save(blocksMined);



            long inconsistentHeight = GraphingServiceImpl.getInstance().checkIntegrity(0);

            assertEquals(9002, inconsistentHeight);
        }
    }




}
