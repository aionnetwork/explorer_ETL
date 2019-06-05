package aion.dashboard.parser.type;

import org.aion.api.type.BlockDetails;
import org.aion.api.type.TxDetails;

import java.util.List;
import java.util.Objects;

//Used to send messages between threads
public class Message<T> {



    private final List<T> item;// The item to be consumed
    private final BlockDetails blockDetails;// details from this api request
    private final TxDetails txDetails;// tx detail from this api request

    public Message(List<T> item, BlockDetails blockDetails, TxDetails txDetails){

        this.item = item;
        this.blockDetails = blockDetails;
        this.txDetails = txDetails;
    }


    public List<T> getItem() {
        return item;
    }

    public BlockDetails getBlockDetails() {
        return blockDetails;
    }

    public TxDetails getTxDetails() {
        return txDetails;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Message)) return false;
        Message<?> message = (Message<?>) o;
        return item.equals(message.item);
    }

    @Override
    public int hashCode() {
        return Objects.hash(item);
    }
}
