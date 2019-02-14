package aion.dashboard.db;

public class DbQuery {
    private DbQuery() {
        throw new UnsupportedOperationException("Cannot create an instance of DBQuery");
    }

    public static final String GET_PARSER_STATE = "select id, block_number, transaction_id from parser_state where id between 1 and 4";

    public static final String UPDATE_PARSER_STATE = "update parser_state set block_number = ?, transaction_id = ? where id = ?";

    public static final String INSERT_BLOCK_MAP = "replace into block_map(block_hash,block_number) values(?,?)";



    public static final String INSERT_TRANSACTION_MAP = "replace into transaction_map(transaction_hash,id) values(?,?)";


    // WARNING: this hits ALL the partitions => slows down with size of database, BUT SQL seems to be smart
    // BUT mySql seems to be limiting the rows based on index and seems to be running in reasonable time
    // https://dev.mysql.com/doc/refman/5.7/en/explain-output.html#explain-join-types
    public static final String REORGANIZE_TRANSACTIONS = "delete from transaction where block_number > ?";

    public static final String MAX_TRANSACTION_ID_FOR_BLOCK_NUMBER = "select transaction_id from block where block_number = ?";


    ///Token

    public static final String TOKEN_INSERT = "REPLACE INTO token " +
            "(`contract_addr`,`transaction_hash`,`name`,`symbol`,`creator_address`," +
            "`total_supply`,`creation_timestamp`,`granularity`,`liquid_supply`) VALUES(?,?,?,?,?,?,?,?,?)";

    public static final String TOKEN_SELECT = "select contract_addr,transaction_hash,name,symbol,creator_address,"  +
            "total_supply,creation_timestamp,granularity,liquid_supply from token where contract_addr = ? limit 1";
    public static final String TOKEN_SELECT_RANGE = "select contract_addr,transaction_hash,name,symbol,creator_address,"+
            "total_supply,creation_timestamp,granularity,liquid_supply  from token  limit ?,?";
    public static final String TOKEN_DELETE = "delete  from token where contract_addr = ? limit 1";
    public static final String TOKEN_DELETE_BY_BLOCK = "delete from token where contract_addr in (select contract_addr from transaction where block_number >=?)";

    //Balance
    public static final String BALANCE_SELECT_BY_ADDRESS = "select * from balance where address = ? limit 1";

    public static final String BALANCE_INSERT = "REPLACE INTO balance " +
            "(`address`,`balance`,`last_block_number`,`contract`,nonce,transaction_id) VALUES(?,?,?,?,?,?)";
    public static final String BALANCE_SELECT_BY_RANGE = "select * from balance  limit ?,?";
    public static final String BALANCE_DELETE_FROM_BLOCK = "delete from balance where last_block_number >= ?";
    public static final String BALANCE_SELECT_BY_CONTRACT_ADDR_AND_TX_ID = "select * from balance where transaction_id = ? and address = ? limit 1";
    public static final String BALANCE_COUNT = "select max(last_block_number) from balance ";
    public static final String BALANCE_SELECT_GREATER_THAN_BLOCK_NUMBER = "select * from balance where last_block_number >=?";
    //Block
    public static final String BLOCKGET_MAX_BLOCK_NUMBER ="select max(block_number) from block ";
    public static final String BLOCKGET_BY_BLOCK_NUMBER ="select block_number,block_hash,miner_address,parent_hash, receipt_tx_root,state_root,tx_trie_root,extra_data, " +
            "nonce, bloom, solution,difficulty, total_difficulty, nrg_consumed, size, " +
            "block_timestamp, num_transactions,block_time,nrg_reward,transaction_id, transaction_list, nrg_limit from block where block_number=?";
    public static final String BLOCKS_DELETE_FROM = "delete from block where block_number >= ?";
    public static final String INSERT_BLOCK = "replace into block(" +
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
            "size," +
            "block_timestamp," +
            "num_transactions," +
            "block_time," +
            "nrg_reward," +
            "transaction_id," +
            "transaction_list) " +
            "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

    public static final String TRANSACTION_INSERT = "replace into transaction(" +
            "id," +
            "transaction_hash," +
            "block_hash," +
            "block_number," +
            "transaction_index," +
            "from_addr," +
            "to_addr," +
            "nrg_consumed," +
            "nrg_price," +
            "transaction_timestamp," +
            "block_timestamp," +
            "value," +
            "transaction_log," +
            "data," +
            "nonce," +
            "tx_error," +
            "contract_addr) " +
            "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

    public static final String TRANSACTION_SELECT_BY_ID_COUNT_BLOCK_NUM = "select count(block_number) , block_number from transaction  where block_number > ? group by block_number";



    public static final String TRANSACTION_SELECT_ID_BY_BLOCK_NUM = "select id from transaction  where block_number > ?";
    public static final String TRANSACTION_BY_BLOCK_NUM = "select id,transaction_hash,block_hash, block_number,transaction_index," +
            " from_addr,to_addr, nrg_consumed,nrg_price, transaction_timestamp,block_timestamp, " +
            " value,transaction_log, data,nonce, tx_error,contract_addr from transaction  where block_number = ?";

    public static final String TRANSACTION_BY_CONTRACT_ADDRESS = "select id,transaction_hash,block_hash, block_number,transaction_index," +
            " from_addr,to_addr, nrg_consumed,nrg_price, transaction_timestamp,block_timestamp, " +
            " value,transaction_log, data,nonce, tx_error,contract_addr from transaction  where block_number = ?";


    public static final String TRANSACTION_DELETE_BY_ID = "delete from transaction where id > ?";


    //block_map
    public static final String BLOCK_MAP_DELETE_BY_BLOCK = "delete from block_map where block_number >= ?";


    public static final String TRANSACTION_MAP_SELECT_ID_BY_HASH = "select id from transaction_map where transaction_hash = ? limit 1";

    public static final String TRANSACTION_MAP_DELETE_BY_ID = "delete from transaction_map where id>?";

    //----------------------------transfer-------------------------------

    public static final String TRANSFER_INSERT = "insert into transfer( to_addr," +
            " from_addr," +
            " operator_addr, " +
            "tkn_value," +
            " contract_addr," +
            " transaction_id," +
            " block_number," +
            " transfer_timestamp)" +
            "values(?,?,?,?,?,?,?,?)";

    public static final String TRANSFER_DELETE = "delete from transfer where block_number >= ?";

    //----------------------------token_balance----------------------------

    public static final String TOKEN_BALANCE_INSERT = "replace into token_balance( tkn_balance," +
            " holder_addr," +
            " contract_addr," +
            " block_number ) " +
            "values ( ?, ?, ?, ? )";

    public static final String TOKEN_BALANCE_SELECT = "select tkn_balance, holder_addr," +
            " contract_addr, block_number from token_balance where block_number >= ? ";


    public static final String TOKEN_BALANCE_DELETE_BY_BLOCK_NUMBER = "delete from token_balance where block_number >= ?";

    //----------------------------event---------------------------------------

    public static final String EVENT_INSERT = "insert into event ( name," +
            " parameter_list," +
            " input_list," +
            " transaction_id," +
            " block_number," +
            " contract_addr," +
            " event_timestamp) " +
            "values(?, ?, ?, ?, ?, ?, ?)";

    public static final String EVENT_DELETE = "delete from event where block_number >= ?";


    //----------------------------contract------------------------------------

    public static final String CONTRACT_INSERT = "insert into contract (contract_addr, " +
            "contract_name, " +
            "contract_creator_addr, " +
            "contract_tx_hash, " +
            "block_number, " +
            "deploy_timestamp) " +
            "values(?, ?, ?, ?, ?, ?)";
    public static final String CONTRACT_DELETE = "delete from contract where block_number >= ?";


    public static final String CONTRACT_SELECT = "select * from contract where contract_addr = ?";

    //----------------------------Graphing------------------------------------
    public static final String GRAPHING_INSERT = "insert into graphing ( value, " +
            "graph_type, " +
            "timestamp, " +
            "block_number, " +
            "detail, " +
            "year, " +
            "month, " +
            "date) " +
            "values(?, ?, ?, ?, ?, ?, ?, ?)";

    public static final String GRAPHING_DELETE = " delete from graphing where block_number >= ?";

    public static final String GRAPHING_SELECT_LAST_RECORD = "select * from aionv4.graphing where block_number = " +
            "(select max(block_number)  from graphing where block_number < ?) limit 1";
    public static final String COUNT_ADDRESSES_BETWEEN = "select count(address) from aionv4.balance where transaction_id" +
            " > (select transaction_id from aionv4.block where block_number = ?)  " +
            "and transaction_id <= (select transaction_id from aionv4.block where block_number = ?)";


    public static final String COUNT_ACTIVE_ADDRESSES = "select @aa:=(select count(distinct from_addr) from transaction where id <= ?) as a," +
            "@cc:=(select count(distinct to_addr) from transaction where id <= ? and to_addr not in (select distinct from_addr from transaction  where id <= ?)) as b, " +
            "(@aa+@cc) as total";


    public static final String FIND_MIN_INCONSISTENT_RECORD = "select block_number from graphing where id = (select min(id) from graphing where value < 100 and graph_type = 'Blocks Mined' and block_number >= ?)";
}
