package com.aion.dashboard.etl.tests;

import com.aion.dashboard.etl.database.Query;
import com.aion.dashboard.etl.domainobjects.v4.*;
import com.aion.dashboard.etl.domainobjects.v5.*;
import com.aion.dashboard.etl.util.Utils;

import java.sql.*;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class FromV4toV5 {
    private static ResultSet resultSet;
    private static TestConfig testConfig;
    private static PreparedStatement preparedStatement;

    private static void blocksTest() {
        try{
            int value = testConfig.getRangeMin();
            while(value < testConfig.getRangeMax()) {
                preparedStatement = testConfig.getConnection(4).prepareStatement(Query.BlocksByRange);
                preparedStatement.setInt(1, value);
                preparedStatement.setInt(2, value + 100);
                resultSet = preparedStatement.executeQuery();
                List<V4Block> v4List = Utils.parseV4Blocks(resultSet);

                preparedStatement = testConfig.getConnection(5).prepareStatement(Query.BlocksByRange);
                preparedStatement.setInt(1, value);
                preparedStatement.setInt(2, value + 100);
                resultSet = preparedStatement.executeQuery();
                List<V5Block> v5List = Utils.parseV5Blocks(resultSet);

                for (int i = 0; i < v4List.size(); i++) {
                    System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
                    System.out.println("Is Block #" + v4List.get(i).getBlockNumber() + " v4 Equals to v5: " + v4List.get(i).compare(v5List.get(i)));
                }

                value += 100;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private static void eventsTest() {
        try{
            int value = testConfig.getRangeMin();
            while(value < testConfig.getRangeMax()) {
                preparedStatement = testConfig.getConnection(4).prepareStatement(Query.EventsByRange);
                preparedStatement.setInt(1, value);
                preparedStatement.setInt(2, value + 100);
                resultSet = preparedStatement.executeQuery();
                List<V4Event> v4List = Utils.parseV4Events(resultSet);

                preparedStatement = testConfig.getConnection(5).prepareStatement(Query.EventsByRange);
                preparedStatement.setInt(1, value);
                preparedStatement.setInt(2, value + 100);
                resultSet = preparedStatement.executeQuery();
                List<V5Event> v5List = Utils.parseV5Events(resultSet);

                for (int i = 0; i < v4List.size(); i++) {
                    System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
                    System.out.println("Is Event \"" + v4List.get(i).getName() + "\" in Block #" + v4List.get(i).getBlockNumber() + " v4 Equals to v5: " + v4List.get(i).compare(v5List.get(i)));
                }

                value += 100;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private static void tokensTest() {
        try{
            int value = testConfig.getRangeMin();
            while(value < testConfig.getRangeMax()) {
                preparedStatement = testConfig.getConnection(4).prepareStatement(Query.TokensByRange);
                preparedStatement.setInt(1, value);
                preparedStatement.setInt(2, value + 100);
                resultSet = preparedStatement.executeQuery();
                List<V4Token> v4List = Utils.parseV4Tokens(resultSet);

                preparedStatement = testConfig.getConnection(5).prepareStatement(Query.TokensByRange);
                preparedStatement.setInt(1, value);
                preparedStatement.setInt(2, value + 100);
                resultSet = preparedStatement.executeQuery();
                List<V5Token> v5List = Utils.parseV5Tokens(resultSet);

                for (int i = 0; i < v4List.size(); i++) {
                    System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
                    System.out.println("Is Token " + v4List.get(i).getTokenName() + " (" + v4List.get(i).getSymbol() + ")  v4 Equals to v5: " + v4List.get(i).compare(v5List.get(i)));
                }

                value += 100;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private static void accountsTest() {
        try{
            int value = 0;
            while(value < testConfig.getRangeMax()) {
                preparedStatement = testConfig.getConnection(4).prepareStatement(Query.BalancesByPagination);
                preparedStatement.setInt(1, value);
                preparedStatement.setInt(2, 100);
                resultSet = preparedStatement.executeQuery();
                List<V4Balance> v4List = Utils.parseV4Balances(resultSet);

                preparedStatement = testConfig.getConnection(5).prepareStatement(Query.AccountsByPagination);
                preparedStatement.setInt(1, value);
                preparedStatement.setInt(2, 100);
                resultSet = preparedStatement.executeQuery();
                List<V5Account> v5List = Utils.parseV5Accounts(resultSet);

                for (int i = 0; i < v4List.size(); i++) {
                    System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
                    System.out.println("Is Account " + v4List.get(i).getAddress() + ", last updated in Block " + v4List.get(i).getLastBlockNumber() + " v4 Equals to v5: " + v4List.get(i).compare(v5List.get(i)));
                }

                value += 100;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private static void contractsTest() {
        try{
            int value = testConfig.getRangeMin();
            while(value < testConfig.getRangeMax()) {
                preparedStatement = testConfig.getConnection(4).prepareStatement(Query.ContractsByRange);
                preparedStatement.setInt(1, value);
                preparedStatement.setInt(2, value + 100);
                resultSet = preparedStatement.executeQuery();
                List<V4Contract> v4List = Utils.parseV4Contracts(resultSet);

                preparedStatement = testConfig.getConnection(5).prepareStatement(Query.ContractsByRange);
                preparedStatement.setInt(1, value);
                preparedStatement.setInt(2, value + 100);
                resultSet = preparedStatement.executeQuery();
                List<V5Contract> v5List = Utils.parseV5Contracts(resultSet);

                for (int i = 0;i < v4List.size(); i++) {
                    System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
                    System.out.println("Is Contract " + v4List.get(i).getContractAddr() + ", deployed in Block " + v4List.get(i).getBlockNumber() + " v4 Equals to v5: " + v4List.get(i).compare(v5List.get(i)));
                }

                value += 100;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private static void transactionsTest() {
        try{
            int value = testConfig.getRangeMin();
            while(value < testConfig.getRangeMax()) {
                preparedStatement = testConfig.getConnection(4).prepareStatement(Query.TransactionsByRange);
                preparedStatement.setInt(1, value);
                preparedStatement.setInt(2, value + 100);
                resultSet = preparedStatement.executeQuery();
                List<V4Transaction> v4List = Utils.parseV4Transactions(resultSet);

                preparedStatement = testConfig.getConnection(5).prepareStatement(Query.TransactionsByRange);
                preparedStatement.setInt(1, value);
                preparedStatement.setInt(2, value + 100);
                resultSet = preparedStatement.executeQuery();
                List<V5Transaction> v5List = Utils.parseV5Transactions(resultSet);

                for (int i = 0; i < v4List.size(); i++) {
                    System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
                    System.out.println("Is Transaction " + v4List.get(i).getTransactionHash() + " in Block " + v4List.get(i).getBlockNumber() + " v4 Equals to v5: " + v4List.get(i).compare(v5List.get(i)));
                }

                value += 100;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private static void tokenHoldersTest() {
        try{
            int value = testConfig.getRangeMin();
            while(value < testConfig.getRangeMax()) {

                preparedStatement = testConfig.getConnection(4).prepareStatement(Query.TokenBalancesByRange);
                preparedStatement.setInt(1, value);
                preparedStatement.setInt(2, value + 100);
                resultSet = preparedStatement.executeQuery();
                List<V4TokenBalance> v4List = Utils.parseV4TokenBalances(resultSet);

                preparedStatement = testConfig.getConnection(5).prepareStatement(Query.TokenHoldersByRange);
                preparedStatement.setInt(1, value);
                preparedStatement.setInt(2, value + 100);
                resultSet = preparedStatement.executeQuery();
                List<V5TokenHolder> v5List = Utils.parseV5TokenHolders(resultSet);

                for (int i = 0; i < v4List.size(); i++) {
                    System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
                    System.out.println("Is Token Holder/Contract Address " + v4List.get(i).getHolderAddress() + "/" + v5List.get(i).getContractAddress() + " in Block " + v4List.get(i).getBlockNumber() + " v4 Equals to v5: " + v4List.get(i).compare(v5List.get(i)));
                }

                value += 100;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private static void tokenTransfersTest() {
        try{
            int value = testConfig.getRangeMin();
            while(value < testConfig.getRangeMax()) {
                preparedStatement = testConfig.getConnection(4).prepareStatement(Query.TransfersByRange);
                preparedStatement.setInt(1, value);
                preparedStatement.setInt(2, value + 100);
                resultSet = preparedStatement.executeQuery();
                List<V4Transfer> v4List = Utils.parseV4Transfers(resultSet);

                preparedStatement = testConfig.getConnection(5).prepareStatement(Query.TokenTransfersByRange);
                preparedStatement.setInt(1, value);
                preparedStatement.setInt(2, value + 100);
                resultSet = preparedStatement.executeQuery();
                List<V5TokenTransfer> v5List = Utils.parseV5TokenTransfers(resultSet);

                for (int i = 0; i < v4List.size(); i++) {
                    System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
                    System.out.println("Is Token Transfer (under Contract Address " + v4List.get(i).getContractAddress() + ") in Block " + v4List.get(i).getBlockNumber() + " v4 Equals to v5: " + v4List.get(i).compare(v5List.get(i)));
                }

                value += 100;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            testConfig = new TestConfig(Arrays.asList(4, 5));
            
            blocksTest();
            eventsTest();
            tokensTest();
            accountsTest();
            contractsTest();
            transactionsTest();
            tokenHoldersTest();
            tokenTransfersTest();

            resultSet.close();
            preparedStatement.close();

            testConfig.closeConnections();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
