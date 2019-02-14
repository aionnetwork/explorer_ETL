package aion.dashboard.blockchain;

import org.aion.api.type.BlockDetails;

import java.util.List;

public interface MockDataGenerator {
    /**
     * A set of enums that specify the fields that can be configure. Each Enum has a one-to-one mapping to an index
     * within the CSV file
     */
    enum TransactionField {

        FROM, TO, TX_HASH, VALUE, NONCE, NRG_CONSUMED, NRG_PRICE, DATA, LOGS, TX_INDEX, CONTRACT, TIMESTAMP,
        ERROR, BLK_NUMBER, DUPLICATE;

        public int getIndex(){//points at the getIndex in the csv file in which the data is found
            switch (this){
                case TX_HASH: return 1;
                case BLK_NUMBER: return 3;
                case TX_INDEX: return 4;
                case FROM: return 5;
                case TO: return 6;
                case NRG_CONSUMED: return 7;
                case NRG_PRICE: return 8;
                case TIMESTAMP: return 9;
                case VALUE: return 11;
                case LOGS: return 12;
                case DATA: return 13;
                case NONCE: return 14;
                case ERROR: return 15;
                case CONTRACT: return 16;
                default: return -1;


            }
        }

    }

    /**
     * A set of enums that specify the fields that can be configured. Each enum has a one-to-one mapping to an index
     * within the CSV file
     */
    enum BlockField{
        BLK_NUMBER, TIMESTAMP, NRG_CONSUMED, NRG_LIMIT, BLOOM, EXTRA_DATA, SOLUTION, PARENT_HASH, HASH, NONCE,
        DIFFICULTY, TOTAL_DIFFICULTY, MINER_ADDRESS, STATE_ROOT, TX_TRIE_ROOT, RECEIPT_TX_ROOT, SIZE, BLOCK_TIME,
        TX_DETAILS, DUPLICATE;
        //Points at the index in the csv file in which the data is found
        public int getIndex(){
            switch (this){
                case BLK_NUMBER: return 0;
                case HASH: return 1;
                case MINER_ADDRESS: return 2;
                case PARENT_HASH: return 3;
                case RECEIPT_TX_ROOT: return 4;
                case STATE_ROOT: return 5;
                case TX_TRIE_ROOT: return 6;
                case EXTRA_DATA: return 7;
                case NONCE: return 8;
                case BLOOM: return 9;
                case SOLUTION: return 10;
                case DIFFICULTY: return 11;
                case TOTAL_DIFFICULTY: return 12;
                case NRG_CONSUMED: return 13;
                case NRG_LIMIT: return 14;
                case SIZE: return 15;
                case TIMESTAMP: return 16;
                case BLOCK_TIME: return 18;
                case TX_DETAILS: return 21;
                default: return -1;

            }
        }
    }


    List<BlockDetails> mockBlockDetailsF() throws Exception;
}
