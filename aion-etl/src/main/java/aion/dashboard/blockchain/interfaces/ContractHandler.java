package aion.dashboard.blockchain.interfaces;

import aion.dashboard.blockchain.ContractHandlerImpl;
import aion.dashboard.blockchain.ContractType;

import java.util.Optional;

public abstract class ContractHandler {


    public static ContractHandler getInstance() {
        return ContractHandlerImpl.INSTANCE.get();
    }

    public abstract ContractHandler prepareCallForType(ContractType type);

    public abstract ContractHandler withSignature(String functionName);

    public abstract ContractHandler withParams(Object... params);

    public abstract ContractHandler withSender(String sender);

    public abstract ContractHandler withRecipient(String receipient);

    public abstract ContractHandler withNrgPrice(long nrgPrice);

    public abstract ContractHandler withNrigLimit(long nrgLimit);

    public abstract ContractHandler withReturnType(String returnType);

    public abstract <T> Optional<T> executeFunction();

    protected abstract <T> T executeForAVM();

    protected abstract <T> T executeForFVM();


    protected abstract void reset();
}
