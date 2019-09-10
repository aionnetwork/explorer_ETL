package aion.dashboard.parser.events;

import aion.dashboard.blockchain.AionService;
import aion.dashboard.blockchain.ContractType;
import aion.dashboard.blockchain.interfaces.Web3Service;
import aion.dashboard.blockchain.type.APIBlockDetails;
import aion.dashboard.blockchain.type.APITxDetails;
import aion.dashboard.exception.AionApiException;
import aion.dashboard.exception.Web3ApiException;
import aion.dashboard.util.Utils;
import org.aion.api.type.BlockDetails;
import org.aion.api.type.TxDetails;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * This test depends on a mastery kernel
 */
class EventDecoderTest {

    static Web3Service web3Service = Web3Service.getInstance();

    @AfterAll
    static void tearDown(){
        web3Service.close();
    }
    @Test
    void testDecodeFVM() throws Web3ApiException {
        EventDecoder decoder = EventDecoder.decoderFor(ContractType.DEFAULT);

        List<APIBlockDetails> blocks = web3Service.getBlockDetailsInRange(2270933, 2270933);

        APITxDetails tx = blocks.stream()
                .flatMap(blk -> blk.getTxDetails().stream())
                .filter(transaction->transaction.getTransactionHash().toString().replace("0x","").equalsIgnoreCase("26f467106b04dfd0bfdfab767c05c3fd56dbfd27f611d94aa45bec7796f5d3d1"))
                .findFirst().orElseThrow();


        var success = false;
        for (var txlog: tx.getLogs()) {
            var res = decoder.decodeEvent(txlog);
            if (res.isPresent()){
                res.ifPresent(System.out::println);
                success=true;
                break;
            }
        }

        if (!success) fail();

    }


    @Test
    void testDecodeAVM() throws Web3ApiException {
        EventDecoder decoder = EventDecoder.decoderFor(ContractType.AVM);
        List<APIBlockDetails> blocks = web3Service.getBlockDetailsInRange(2469227,2469227);

        APITxDetails tx = blocks.stream()
                .flatMap(blk -> blk.getTxDetails().stream())
                .filter(transaction-> Utils.sanitizeHex(transaction.getTransactionHash().toString()).equalsIgnoreCase("ef0c55b47fe2098c4d48d5190c7a67d3c49bc28a9b15f13ca7bd31a60a05d430"))
                .findFirst().orElseThrow();


        var success = false;
        for (var txlog: tx.getLogs()) {
            var res = decoder.decodeEvent(txlog);
            if (res.isPresent()){
                res.ifPresent(System.out::println);
                success=true;
                break;
            }
        }

        if (!success) fail();
         blocks = web3Service.getBlockDetailsInRange(2560675,2560675);
         tx = blocks.stream()
                .flatMap(blk -> blk.getTxDetails().stream())
                .filter(transaction-> Utils.sanitizeHex(transaction.getTransactionHash()).equalsIgnoreCase("bea3d05a2d81473bfc5f0d93eea26ee7595b77d219cc8c47cc281ee6fb9b4d37"))
                .findFirst().orElseThrow();


        success = false;
        for (var txlog: tx.getLogs()) {
            var res = decoder.decodeEvent(txlog);
            if (res.isPresent()){
                res.ifPresent(System.out::println);
                success=true;
                break;
            }
        }

        if (!success) fail();

    }

}