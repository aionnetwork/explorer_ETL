package aion.dashboard.db;

public class DbQuery {
    private DbQuery() {}

    public static final String GetHashFromBlockNumber = "select block_hash from block where block_number=?";

    public static final String GetParserState = "select id, block_number from parser_state";

    public static final String UpdateParserState = "update parser_state set block_number = ? where id = ?";


    // WARNING: this hits ALL the partitions => slows down with size of database, BUT SQL seems to be smart
    // BUT mySql seems to be limiting the rows based on index and seems to be running in reasonable time
    // https://dev.mysql.com/doc/refman/5.7/en/explain-output.html#explain-join-types
    public static final String ReorganizeBlocks = "delete from block where block_number > ?";
    public static final String ReorganizeTransactions = "delete from transaction where block_number > ?";
    public static final String MaxTransactionIdForBlockNumber = "select transactionHash from block where block_number = ?";


    ///Token

    public static final String TokenInsert = "REPLACE INTO token (`contract_addr`," +
            "`transaction_hash`," +
            "`name`," +
            "`symbol`," +
            "`creator_address`," +
            "`total_supply`," +
            "`creation_timestamp`," +
            "`granularity`," +
            "`liquid_supply`," +
            "`token_decimal`," +
            "`year`," +
            "`month`," +
            "`day`)" +
            "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)";

    public static final String TokenSelect = "select contract_addr,transaction_hash,name,symbol,creator_address,"  +
            "total_supply,creation_timestamp,granularity,liquid_supply,token_decimal from token where contract_addr = ? limit 1";
    public static final String TokenSelectRange = "select contract_addr,transaction_hash,name,symbol,creator_address,"+
            "total_supply,creation_timestamp,granularity,liquid_supply  from token  limit ?,?";
    public static final String TokenDelete = "delete  from token where contract_addr = ? limit 1";
    public static final String TokenDeleteByBlock= "delete from token where contract_addr in (select contract_addr from transaction where block_number >=?)";

    //Account
    public static final String AccountInsert = "REPLACE INTO account (" +
            "address," +
            "balance," +
            "last_block_number," +
            "contract," +
            "nonce," +
            "transaction_hash," +
            "approx_balance," +
            "first_block_number) " +
            "VALUES(?,?,?,?,?,?,?,?)";
    public static final String AccountSelectByAddress = "select * from account where address = ? limit 1";
    public static final String AccountSelectByRange = "select * from account  limit ?,?";
    public static final String AccountDeleteFromBlock = "delete from account where last_block_number >= ?";
    public static final String AccountSelectByContractAddrAndTxID = "select * from account where transaction_hash = ? and address = ? limit 1";
    public static final String AccountCount = "select max(last_block_number) from account ";
    public static final String AccountSelectGreaterThanBlockNumber = "select * from account where last_block_number >=?";
    public static final String AccountSelectRandom = "select * from account order by rand() limit ?";

    //Block
    public static final String BlockGetMaxBlockNumber ="select max(block_number) from block ";
    public static final String BlockGetByBlockNumber = "select * from block where block_number=?";
    public static final String BlocksDeleteFrom = "delete from block where block_number >= ?";
    public static final String InsertBlock = "replace into block(" +
            "block_number," +
            "block_hash," +
            "miner_address," +
            "parent_hash," +
            "receipt_tx_root," +
            "state_root," +
            "tx_trie_root," +
            "extra_data," +
            "nonce," +
            "bloom," +
            "solution," +
            "difficulty," +
            "total_difficulty," +
            "nrg_consumed," +
            "nrg_limit," +
            "block_size," +
            "block_timestamp," +
            "num_transactions," +
            "block_time," +
            "nrg_reward," +
            "approx_nrg_reward,"+
            "transaction_hash," +
            "transaction_hashes, " +
            "year, " +
            "month, " +
            "day," +
            "block_reward," +
            "seed," +
            "public_key," +
            "signature," +
            "seal_type) " +
            "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

    public static final String SelectFromBlockWhereTimestampBetween = "Select * from block where block_timestamp between ? and ?";
    public static final String SelectMiningInfoFromBlock = "select block_number, seal_type, miner_address from block where block_number>=? and block_number<=?";

    public static final String SelectFromBlockInRange = "Select * from block where block_number between ? and ?";


    //Transaction
    public static final String TransactionDeleteByBlock = "delete from transaction where block_number >= ?";
    public static final String TransactionInsert = "replace into transaction(" +
            "transaction_hash," +
            "block_hash," +
            "block_number," +
            "block_timestamp," +
            "transaction_index," +
            "from_addr," +
            "to_addr," +
            "nrg_consumed," +
            "nrg_price," +
            "transaction_timestamp," +
            "value," +
            "approx_value," +
            "transaction_log," +
            "data," +
            "nonce," +
            "tx_error," +
            "contract_addr," +
            "year, " +
            "month, " +
            "day, " +
            "type) " +
            "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
    public static final String TransactionUpdateInternalTransactions = "Update transaction set " +
            "internal_transaction_count = ?" +
            " where transaction_hash = ?";

    public static final String TransactionSelectByBlockNumCountBlockNum = "select count(block_number) , block_number from transaction  where block_number > ? group by block_number";


    public static final String TransactionSelectIDByBlockNum = "select transaction_hash from transaction  where block_number > ?";
    public static final String TransactionByBlockNum = "select * from transaction  where block_number = ?";

    public static final String TransactionByContractAddress = "select * from transaction  where contract_addr = ?";

    //----------------------------token_transfer-------------------------------

    public static final String TokenTransfersInsert = "replace into token_transfers( " +
            " to_addr," +
            " from_addr," +
            " operator_addr," +
            " scaled_value, " +
            " raw_value," +
            " granularity, " +
            " token_decimal," +
            " contract_addr," +
            " transaction_hash," +
            " block_number," +
            " transfer_timestamp," +
            " year, " +
            " month, " +
            " day," +
            " approx_value) " +
            " values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

    public static final String TokenTransfersDelete = "delete from token_transfers where block_number >= ?";

    //----------------------------token_holders----------------------------

    public static final String TokenHoldersInsert = "replace into token_holders( " +
            " scaled_balance," +
            " holder_addr," +
            " contract_addr," +
            " block_number," +
            " raw_balance, " +
            " token_decimal, " +
            " granularity )" +
            " values (?, ?, ?, ?, ? , ?, ?)";

    public static final String TokenHoldersSelect = "select * from token_holders where block_number >= ? ";


    public static final String TokenHoldersDeleteByBlockNumber = "delete from token_holders where block_number >= ?";

    //----------------------------event---------------------------------------

    public static final String EventInsert = "insert into event ( name," +
            " parameter_list," +
            " input_list," +
            " transaction_hash," +
            " block_number," +
            " contract_addr," +
            " event_timestamp)" +
            " values(?, ?, ?, ?, ?, ?, ?)";

    public static final String EventDelete = "delete from event where block_number >= ?";


    //----------------------------contract------------------------------------

    public static final String ContractInsert = "replace into contract (contract_addr, " +
            "contract_name, " +
            "contract_creator_addr, " +
            "contract_tx_hash, " +
            "block_number, " +
            "deploy_timestamp, " +
            "year, " +
            "month, " +
            "day, " +
            "type, " +
            "internal) " +
            "values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    public static final String ContractDelete = "delete from contract where block_number >= ?";


    public static final String ContractSelect = "select * from contract where contract_addr = ?";

    //----------------------------Graphing------------------------------------
    public static final String GraphingInsert = "insert into graphing ( value, " +
            "graph_type, " +
            "timestamp, " +
            "block_number, " +
            "detail," +
            "year, " +
            "month, " +
            "day) " +
            "values(?, ?, ?, ?, ?, ?, ?, ?)";

    public static final String GraphingDelete = " delete from graphing where block_number >= ?";

    public static final String GraphingSelectLastRecord = "select * from graphing where block_number = " +
            "(select max(block_number)  from graphing where block_number < ?) limit 1";

    public static final String CountActiveAddresses = "select count(*) as total from account where first_block_number <=? ";


    public static final String FindMinInconsistentRecord = "select block_number from graphing where id = (select min(id) from graphing where value < 100 and graph_type = 'Blocks Mined' and block_number >= ?)";

    //--------------------------------Metrics-------------------------------------------

    public static final String MetricsInsert = "insert into metrics (" +
            "id," +
            "total_transaction," +
            "transactions_per_second," +
            "peak_transactions_per_block," +
            "start_block," +
            "end_block," +
            "average_nrg_consumed," +
            "average_nrg_limit," +
            "averaged_block_time," +
            "average_difficulty," +
            " end_timestamp," +
            "start_timestamp," +
            "averaged_hash_power," +
            "last_block_reward," +
            "pow_avg_difficulty," +
            "pos_avg_difficulty," +
            "pow_avg_block_time," +
            "pos_avg_block_time," +
            "avg_pos_issuance," +
            "percentage_of_network_staking," +
            "total_stake) " +
            "values (?, ?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";


    public static final String MetricsDelete = "update metrics set " +
            " total_transaction = NULL," +
            " transactions_per_second = NULL," +
            " peak_transactions_per_block = NULL," +
            " start_block = NULL," +
            " end_block = NULL," +
            " average_nrg_consumed = NULL," +
            " average_nrg_limit = NULL," +
            " averaged_block_time = NULL," +
            " average_difficulty = NULL," +
            " end_timestamp = NULL," +
            " start_timestamp = NULL," +
            " averaged_hash_power = NULL," +
            " last_block_reward = NULL";

    //-----------------------------InternalTransfer-------------------


    public static String InternalTransferInsert = "replace into internal_transfer (" +
            "transaction_hash, " +
            "to_addr, " +
            "from_addr," +
            "value_transferred, " +
            "block_timestamp, " +
            "block_number," +
            "transfer_index," +
            "approx_value) values (?,?,?,?,?,?,?,?)";

    public static String InternalTransferDelete = "delete from graphing where block_number > ?";

    //---------------------------InternalTransactions---------------------

    public static String InsertInternalTransaction = "replace into internal_transaction(" +
            "transaction_hash, " +
            "internal_transaction_index, " +
            "nrg_price, " +
            "nrg_limit, " +
            "data, " +
            "rejected, " +
            "kind, " +
            "from_addr, " +
            "to_addr, " +
            "nonce, " +
            "block_number, " +
            "value, " +
            "timestamp," +
            "contract_address) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

    public static final String DeleteFromInternalTransaction = "Delete from internal transaction where block_number>=?";

    public static final String DeleteOneFromInternalTransaction = "Delete from internal_transaction where block_number=?";

    //---------------------------VerifiedContract-------------------------
    public static final String SelectFromVerifiedContract =  "SELECT permission from verified_contract where contract_address = ?";



    //---------------------------TxLog------------------------------------
    public static final String InsertTxLog = "Replace into tx_log (" +
            "transaction_hash, " +
            "log_index, " +
            "block_number, " +
            "block_timestamp, " +
            "topics, " +
            "data, " +
            "contract_addr, " +
            "from_addr, " +
            "to_addr, " +
            "contract_type) values(?,?,?,?,?,?,?,?,?,?)";

    public static final String DeleteTxLogByBlockNumber = "delete from tx_log where block_number > ?";



    //---------------------------UpdateState-------------------------------

    public static final String InsertUpdateState = "Replace into update_state (table_id, " +
            " table_name," +
            " run_update," +
            " start," +
            " end) values (?, ?, ?, ?, ?)";

    public static final String SelectFromUpdateState = "Select * from update_state where table_id = ?";

    //------------------------ReorgDetails--------------------------------
    public static final String InsertReorgDetails = "Insert into reorg_details (block_number, server_timestamp, block_depth, affected_addresses, number_of_affected_transactions) value (?,?,?,?,?)";
    //------------------------CirculatingSupply---------------------------
    public static final String SelectCirculatingSupply = "Select * from circulating_supply";
    //
    public static final String INSERT_MINER_STATS = "insert into validator_stats (block_number," +
            " miner_address, " +
            "seal_type," +
            " block_count, " +
            "block_timestamp," +
            " percentage_of_blocks_validated) " +
            "VALUE (?,?,?,?,?,?);";
    public static final String DELETE_VALIDATORS = "delete from validator_stats where block_number = ?";
}