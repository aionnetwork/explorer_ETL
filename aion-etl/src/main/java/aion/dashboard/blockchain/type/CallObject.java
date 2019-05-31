package aion.dashboard.blockchain.type;

import com.fasterxml.jackson.annotation.*;
import org.aion.api.type.TxArgs;
import org.aion.base.type.AionAddress;
import org.aion.base.util.ByteArrayWrapper;
import org.aion.util.bytes.ByteUtil;

import java.math.BigInteger;
import java.util.List;


public class CallObject {


    private final byte[] data;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final String sender;
    private final String recipient;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final Long nrgPrice;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final Long nrgLimit;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final Long nonce;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final BigInteger value;


    public CallObject(byte[] data, String sender, String recipient, Long nrgPrice, Long nrgLimit) {
        this.data = data;
        this.sender = sender;
        this.recipient = recipient;
        this.nrgPrice = nrgPrice;
        this.nrgLimit = nrgLimit;
        nonce=null;
        value=null;

    }


    @JsonCreator
    public CallObject(@JsonProperty("data") String data,
                      @JsonProperty("from") String sender,
                      @JsonProperty("to") String recipient,
                      @JsonProperty("gasPrice") long nrgPrice,
                      @JsonProperty("gas") long nrgLimit,
                      @JsonProperty("nonce") Long nonce,
                      @JsonProperty("value") BigInteger value) {
        this.data = ByteUtil.hexStringToBytes(data);
        this.sender = sender;
        this.recipient = recipient;
        this.nrgPrice = nrgPrice;
        this.nrgLimit = nrgLimit;
        this.nonce = nonce;
        this.value = value;
    }

    @JsonGetter("data")
    public String getData() {
        return ByteUtil.toHexStringWithPrefix(data);
    }

    @JsonGetter("from")
    public String getSender() {
        return sender;
    }

    @JsonGetter("to")
    public String getRecipient() {
        return recipient;
    }

    @JsonGetter("gasPrice")
    public long getNrgPrice() {
        return nrgPrice;
    }

    @JsonGetter("gas")
    public long getNrgLimit() {
        return nrgLimit;
    }

    @JsonGetter("nonce")
    public Long getNonce() {
        return nonce;
    }

    @JsonGetter("value")
    public BigInteger getValue() {
        return value;
    }

    private static final ThreadLocal<TxArgs.TxArgsBuilder> txBuilder = ThreadLocal.withInitial(TxArgs.TxArgsBuilder::new);

    @JsonIgnore
    public TxArgs toTxArgs(){
        return txBuilder.get()
                .data(ByteArrayWrapper.wrap(data))
                .from(sender==null ? AionAddress.ZERO_ADDRESS() : AionAddress.wrap(sender))
                .to(AionAddress.wrap(recipient))
                .nonce(BigInteger.valueOf(nonce==null ? 0L:nonce))
                .nrgLimit(nrgLimit)
                .nrgPrice(nrgPrice)
                .value(value ==null? BigInteger.ZERO: value)
                .createTxArgs();
    }
}
