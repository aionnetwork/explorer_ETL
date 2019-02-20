package com.aion.dashboard.etl.database;

public class Query {
    public static final String BlocksByRange = "SELECT * FROM block WHERE block_number >= ? AND block_number <= ?";
    public static final String EventsByRange = "SELECT * FROM event WHERE block_number >= ? AND block_number <= ?";
    public static final String TokensByRange = "SELECT tkn.* FROM token AS tkn, transaction AS txn WHERE txn.block_number >= ? AND txn.block_number <= ? AND txn.contract_addr = tkn.contract_addr";
    public static final String BalancesByPagination = "SELECT * FROM balance LIMIT ?,?";
    public static final String AccountsByPagination = "SELECT * FROM account LIMIT ?,?";
    public static final String ContractsByRange = "SELECT * FROM contract WHERE block_number >= ? AND block_number <= ?";
    public static final String TransactionsByRange = "SELECT * FROM transaction WHERE block_number >= ? AND block_number <= ? ORDER BY transaction_hash DESC";
    public static final String TokenHoldersByRange = "SELECT * FROM token_holders WHERE block_number >= ? AND block_number <= ?";
    public static final String TokenBalancesByRange = "SELECT * FROM token_balance WHERE block_number >= ? AND block_number <= ?";
    public static final String TransfersByRange = "SELECT * FROM transfer WHERE block_number >= ? AND block_number <= ?";
    public static final String TokenTransfersByRange = "SELECT * FROM token_transfers WHERE block_number >= ? AND block_number <= ?";
}
