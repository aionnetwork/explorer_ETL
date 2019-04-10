package com.aion.dashboard.etl.util;

import com.aion.dashboard.etl.domainobjects.v4.*;
import com.aion.dashboard.etl.domainobjects.v4.V4Transfer;
import com.aion.dashboard.etl.domainobjects.v5.*;

import java.math.BigInteger;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZoneId;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class Utils {
    private static ZoneId UTCZoneID = ZoneId.of("UTC");

    public static ZonedDateTime getZDT(long timestamp){
        if (timestamp < 0 ){
            throw new IllegalArgumentException("Timestamp is negative");
        }
        else {
            return Instant.ofEpochSecond(timestamp).atZone(UTCZoneID);
        }
    }

    public static List<V4Block> parseV4Blocks(ResultSet resultSet) throws SQLException {
        List<V4Block> V4Blocks = new ArrayList<>();
        while(resultSet.next()) {
            V4Blocks.add(new V4Block.BlockBuilder()
                    .blockNumber(resultSet.getLong("block_number"))
                    .blockHash(resultSet.getString("block_hash"))
                    .minerAddress(resultSet.getString("miner_address"))
                    .parentHash(resultSet.getString("parent_hash"))
                    .receiptTxRoot(resultSet.getString("receipt_tx_root"))
                    .stateRoot(resultSet.getString("state_root"))
                    .txTrieRoot(resultSet.getString("tx_trie_root"))
                    .extraData(resultSet.getString("extra_data"))
                    .nonce(resultSet.getString("nonce"))
                    .bloom(resultSet.getString("bloom"))
                    .solution(resultSet.getString("solution"))
                    .difficulty(resultSet.getString("difficulty"))
                    .totalDifficulty(resultSet.getString("total_difficulty"))
                    .nrgConsumed(resultSet.getLong("nrg_consumed"))
                    .nrgLimit(resultSet.getLong("nrg_limit"))
                    .size(resultSet.getLong("size"))
                    .blockTimestamp(resultSet.getLong("block_timestamp"))
                    .numTransactions(resultSet.getLong("num_transactions"))
                    .blockTime(resultSet.getLong("block_time"))
                    .nrgReward(new BigInteger(resultSet.getString("nrg_reward"), 16))
                    .transactionId(new BigInteger(resultSet.getString("transaction_id")))
                    .transactionList(resultSet.getString("transaction_list"))
                    .build());
        }

        return V4Blocks;
    }
    public static List<V4Event> parseV4Events(ResultSet resultSet) throws SQLException {
        List<V4Event> V4Events = new ArrayList<>();
        while(resultSet.next()) {
            V4Events.add(new V4Event.EventBuilder()
                    .setName(resultSet.getString("name"))
                    .setBlockNumber(resultSet.getLong("block_number"))
                    .setTimestamp(resultSet.getLong("event_timestamp"))
                    .setContractAddr(resultSet.getString("contract_addr"))
                    .setInputList(resultSet.getString("input_list"))
                    .setParameterList(resultSet.getString("parameter_list"))
                    .setTransactionID(resultSet.getLong("transaction_id"))
                    .build());
        }

        return V4Events;
    }
    public static List<V4Token> parseV4Tokens(ResultSet resultSet) throws SQLException {
        List<V4Token> V4Tokens = new ArrayList<>();
        while(resultSet.next()) {
            V4Tokens.add(new V4Token.TokenBuilder()
                    .contractAddress(resultSet.getString("contract_addr"))
                    .transactionHash(resultSet.getString("transaction_hash"))
                    .name(resultSet.getString("name"))
                    .symbol(resultSet.getString("symbol"))
                    .timestamp(resultSet.getLong("creation_timestamp"))
                    .creatorAddress(resultSet.getString("creator_address"))
                    .totalSupply(resultSet.getBigDecimal("total_supply").toBigInteger())
                    .granularity(resultSet.getBigDecimal("granularity"))
                    .totalLiquidSupply(resultSet.getBigDecimal("liquid_supply").toBigInteger())
                    .build()
            );
        }

        return V4Tokens;
    }
    public static List<V4Balance> parseV4Balances(ResultSet resultSet) throws SQLException {
        List<V4Balance> v4Balance = new ArrayList<>();
        while(resultSet.next()) {
            v4Balance.add(new V4Balance.BalanceBuilder()
                    .balance(resultSet.getBigDecimal("balance"))
                    .address(resultSet.getString("address"))
                    .contract(resultSet.getInt("contract"))
                    .nonce(BigInteger.valueOf(resultSet.getLong("nonce")))
                    .lastBlockNumber(resultSet.getLong("last_block_number"))
                    .transactionId(resultSet.getLong("transaction_id"))
                    .build()
            );
        }

        return v4Balance;
    }
    public static List<V4Transfer> parseV4Transfers(ResultSet resultSet) throws SQLException {
        List<V4Transfer> V4Transfers = new ArrayList<>();
        while(resultSet.next()) {
            V4Transfers.add(new V4Transfer.TransferBuilder()
                    .setOperator(resultSet.getString("operator_addr"))
                    .setToAddress(resultSet.getString("to_addr"))
                    .setFromAddress(resultSet.getString("from_addr"))
                    .setTokenValue(resultSet.getBigDecimal("tkn_value"))
                    .setContractAddress(resultSet.getString("contract_addr"))
                    .setTransactionId(resultSet.getLong("transaction_id"))
                    .setBlockNumber(resultSet.getLong("block_number"))
                    .setTransferTimestamp(resultSet.getLong("transfer_timestamp"))
                    .build()
            );
        }

        return V4Transfers;
    }
    public static List<V4Contract> parseV4Contracts(ResultSet resultSet) throws SQLException {
        List<V4Contract> V4Contracts = new ArrayList<>();
        while(resultSet.next()) {
            V4Contracts.add(new V4Contract.ContractBuilder()
                    .setBlockNumber(resultSet.getLong("block_number"))
                    .setTimestamp(resultSet.getLong("deploy_timestamp"))
                    .setContractAddr(resultSet.getString("contract_addr"))
                    .setContractCreatorAddr(resultSet.getString("contract_creator_addr"))
                    .setContractTxHash(resultSet.getString("contract_tx_hash"))
                    .setContractName(resultSet.getString("contract_name"))
                    .build());
        }

        return V4Contracts;
    }
    public static List<V4Transaction> parseV4Transactions(ResultSet resultSet) throws SQLException {
        List<V4Transaction> V4Transactions = new ArrayList<>();
        while(resultSet.next()) {
            V4Transactions.add(new V4Transaction.TransactionBuilder()
                    .setTransactionHash(resultSet.getString("transaction_hash"))
                    .setBlockHash(resultSet.getString("block_hash"))
                    .setBlockNumber(resultSet.getLong("block_number"))
                    .setBlockTimestamp(resultSet.getLong("block_timestamp"))
                    .setTransactionIndex(resultSet.getLong("transaction_index"))
                    .setFromAddr(resultSet.getString("from_addr"))
                    .setToAddr(resultSet.getString("to_addr"))
                    .setNrgConsumed(resultSet.getLong("nrg_consumed"))
                    .setNrgPrice(resultSet.getLong("nrg_price"))
                    .setTransactionTimestamp(resultSet.getBigDecimal("transaction_timestamp").longValue())
                    .setValue(resultSet.getString("value"))
                    .setTransactionLog(resultSet.getString("transaction_log"))
                    .setData(resultSet.getString("data"))
                    .setNonce(resultSet.getString("nonce"))
                    .setTxError(resultSet.getString("tx_error"))
                    .setContractAddr(resultSet.getString("contract_addr"))
                    .build()
            );
        }

        return V4Transactions;
    }
    public static List<V4TokenBalance> parseV4TokenBalances(ResultSet resultSet) throws SQLException {
        List<V4TokenBalance> v4TokenBalance = new ArrayList<>();
        while(resultSet.next()) {
            v4TokenBalance.add(new V4TokenBalance.TokenBalanceBuilder()
                    .setBalance(resultSet.getBigDecimal("tkn_balance"))
                    .setBlockNumber(resultSet.getLong("block_number"))
                    .setContractAddress(resultSet.getString("contract_addr"))
                    .setHolderAddress(resultSet.getString("holder_addr"))
                    .build()
            );
        }

        return v4TokenBalance;
    }

    public static List<V5Block> parseV5Blocks(ResultSet resultSet) throws SQLException {
        List<V5Block> V5Blocks = new ArrayList<>();
        while(resultSet.next()) {
            V5Blocks.add(new V5Block.BlockBuilder()
                    .blockNumber(resultSet.getLong("block_number"))
                    .blockHash(resultSet.getString("block_hash"))
                    .minerAddress(resultSet.getString("miner_address"))
                    .parentHash(resultSet.getString("parent_hash"))
                    .receiptTxRoot(resultSet.getString("receipt_tx_root"))
                    .stateRoot(resultSet.getString("state_root"))
                    .txTrieRoot(resultSet.getString("tx_trie_root"))
                    .extraData(resultSet.getString("extra_data"))
                    .nonce(resultSet.getString("nonce"))
                    .bloom(resultSet.getString("bloom"))
                    .solution(resultSet.getString("solution"))
                    .difficulty(resultSet.getLong("difficulty"))
                    .totalDifficulty(resultSet.getLong("total_difficulty"))
                    .nrgConsumed(resultSet.getLong("nrg_consumed"))
                    .nrgLimit(resultSet.getLong("nrg_limit"))
                    .blockSize(resultSet.getLong("block_size"))
                    .blockTimestamp(resultSet.getLong("block_timestamp"))
                    .numTransactions(resultSet.getLong("num_transactions"))
                    .blockTime(resultSet.getLong("block_time"))
                    .nrgReward(resultSet.getBigDecimal("nrg_reward"))
                    .transactionHash(resultSet.getString("transaction_hash"))
                    .transactionHashes(resultSet.getString("transaction_hashes"))
                    .build()
            );
        }

        return V5Blocks;
    }
    public static List<V5Event> parseV5Events(ResultSet resultSet) throws SQLException {
        List<V5Event> V5Events = new ArrayList<>();
        while(resultSet.next()) {
            V5Events.add(new V5Event.EventBuilder()
                    .setName(resultSet.getString("name"))
                    .setBlockNumber(resultSet.getLong("block_number"))
                    .setTimestamp(resultSet.getLong("event_timestamp"))
                    .setContractAddr(resultSet.getString("contract_addr"))
                    .setInputList(resultSet.getString("input_list"))
                    .setParameterList(resultSet.getString("parameter_list"))
                    .setTransactionHash(resultSet.getString("transaction_hash"))
                    .build());
        }

        return V5Events;
    }
    public static List<V5Token> parseV5Tokens(ResultSet resultSet) throws SQLException {
        List<V5Token> V5Tokens = new ArrayList<>();
        while(resultSet.next()) {
            V5Tokens.add(new V5Token.TokenBuilder()
                    .contractAddress(resultSet.getString("contract_addr"))
                    .transactionHash(resultSet.getString("transaction_hash"))
                    .name(resultSet.getString("name"))
                    .symbol(resultSet.getString("symbol"))
                    .timestamp(resultSet.getLong("creation_timestamp"))
                    .creatorAddress(resultSet.getString("creator_address"))
                    .totalSupply(resultSet.getBigDecimal("total_supply").toBigInteger())
                    .granularity(resultSet.getBigDecimal("granularity").toBigInteger())
                    .tokenDecimal(resultSet.getInt("token_decimal"))
                    .liquidSupply(resultSet.getBigDecimal("liquid_supply").toBigInteger())
                    .build()
            );
        }

        return V5Tokens;
    }
    public static List<V5Account> parseV5Accounts(ResultSet resultSet) throws SQLException {
        List<V5Account> v5Account = new ArrayList<>();
        while(resultSet.next()) {
            v5Account.add(new V5Account.AccountBuilder()
                    .balance(resultSet.getBigDecimal("balance"))
                    .address(resultSet.getString("address"))
                    .contract(resultSet.getInt("contract"))
                    .nonce(resultSet.getString("nonce"))
                    .lastBlockNumber(resultSet.getLong("last_block_number"))
                    .transactionHash(resultSet.getString("transaction_hash"))
                    .build()
            );
        }

        return v5Account;
    }
    public static List<V5Contract> parseV5Contracts(ResultSet resultSet) throws SQLException {
        List<V5Contract> V5Contracts = new ArrayList<>();
        while(resultSet.next()) {
            V5Contracts.add(new V5Contract.ContractBuilder()
                    .setBlockNumber(resultSet.getLong("block_number"))
                    .setTimestamp(resultSet.getLong("deploy_timestamp"))
                    .setContractAddr(resultSet.getString("contract_addr"))
                    .setContractCreatorAddr(resultSet.getString("contract_creator_addr"))
                    .setContractTxHash(resultSet.getString("contract_tx_hash"))
                    .setContractName(resultSet.getString("contract_name"))
                    .build());
        }

        return V5Contracts;
    }
    public static List<com.aion.dashboard.etl.domainobjects.v5.V5Transaction> parseV5Transactions(ResultSet resultSet) throws SQLException {
        List<com.aion.dashboard.etl.domainobjects.v5.V5Transaction> V5Transactions = new ArrayList<>();
        while(resultSet.next()) {
            V5Transactions.add(new com.aion.dashboard.etl.domainobjects.v5.V5Transaction.TransactionBuilder()
                    .setTransactionHash(resultSet.getString("transaction_hash"))
                    .setBlockHash(resultSet.getString("block_hash"))
                    .setBlockNumber(resultSet.getLong("block_number"))
                    .setBlockTimestamp(resultSet.getLong("block_timestamp"))
                    .setTransactionIndex(resultSet.getLong("transaction_index"))
                    .setFromAddr(resultSet.getString("from_addr"))
                    .setToAddr(resultSet.getString("to_addr"))
                    .setNrgConsumed(resultSet.getLong("nrg_consumed"))
                    .setNrgPrice(resultSet.getLong("nrg_price"))
                    .setTransactionTimestamp(resultSet.getBigDecimal("transaction_timestamp").longValue())
                    .setValue(resultSet.getBigDecimal("value"))
                    .setTransactionLog(resultSet.getString("transaction_log"))
                    .setData(resultSet.getString("data"))
                    .setNonce(resultSet.getString("nonce"))
                    .setTxError(resultSet.getString("tx_error"))
                    .setContractAddr(resultSet.getString("contract_addr"))
                    .build());
        }

        return V5Transactions;
    }
    public static List<V5TokenHolder> parseV5TokenHolders(ResultSet resultSet) throws SQLException {
        List<V5TokenHolder> V5TokenHolders = new ArrayList<>();
        while(resultSet.next()) {
            V5TokenHolders.add(new V5TokenHolder.TokenHolderBuilder()
                    .setScaledBalance(resultSet.getBigDecimal("scaled_balance"))
                    .setBlockNumber(resultSet.getLong("block_number"))
                    .setContractAddress(resultSet.getString("contract_addr"))
                    .setHolderAddress(resultSet.getString("holder_addr"))
                    .setRawBalance(resultSet.getString("raw_balance"))
                    .setTokenGranularity(resultSet.getBigDecimal("granularity").toBigInteger())
                    .setTokenDecimal(resultSet.getInt("token_decimal"))
                    .build()
            );
        }

        return V5TokenHolders;
    }
    public static List<V5TokenTransfer> parseV5TokenTransfers(ResultSet resultSet) throws SQLException {
        List<V5TokenTransfer> V5TokenTransfers = new ArrayList<>();
        while(resultSet.next()) {
            V5TokenTransfers.add(new V5TokenTransfer.TokenTransferBuilder()
                    .setToAddress(resultSet.getString("to_addr"))
                    .setFromAddress(resultSet.getString("from_addr"))
                    .setOperator(resultSet.getString("operator_addr"))
                    .setScaledTokenValue(resultSet.getBigDecimal("scaled_value"))
                    .setRawValue(resultSet.getString("raw_value"))
                    .setGranularity(resultSet.getBigDecimal("granularity"))
                    .setTokenDecimal(resultSet.getInt("token_decimal"))
                    .setContractAddress(resultSet.getString("contract_addr"))
                    .setTransactionHash(resultSet.getString("transaction_hash"))
                    .setBlockNumber(resultSet.getLong("block_number"))
                    .setTransactionTimestamp(resultSet.getLong("transfer_timestamp"))
                    .build()
            );
        }

        return V5TokenTransfers;
    }
    public static List<V5InternalTransfer> parseV5InternalTransfers(ResultSet resultSet) throws SQLException {
        List<V5InternalTransfer> V5InternalTransfers = new ArrayList<>();
        while(resultSet.next()) {
            V5InternalTransfers.add(new V5InternalTransfer.InternalTransferBuilder()
                    .setTransactionHash(resultSet.getString("transaction_hash"))
                    .setToAddr(resultSet.getString("to_addr"))
                    .setFromAddr(resultSet.getString("from_addr"))
                    .setValueTransferred(resultSet.getBigDecimal("value_transferred"))
                    .setBlockNumber(resultSet.getLong("block_number"))
                    .setTimestamp(resultSet.getLong("timestamp"))
                    .setTransferCount(resultSet.getInt("transfer_count"))
                    .build()
            );
        }

        return V5InternalTransfers;
    }
}
