package aion.dashboard.blockchain.interfaces;

import aion.dashboard.blockchain.ContractType;
import aion.dashboard.blockchain.type.APIBlockDetails;
import aion.dashboard.domainobject.Contract;
import aion.dashboard.domainobject.Token;
import aion.dashboard.domainobject.TokenHolders;
import org.aion.api.type.BlockDetails;
import org.aion.base.type.AionAddress;

import java.io.Closeable;
import java.math.BigInteger;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;


@SuppressWarnings({"WeakerAccess", "unused"})
public abstract class ATSToken implements AutoCloseable {


    protected final String contract;
    protected final ContractType type;
    protected volatile Token token;


    protected ATSToken(String contract, ContractType type){
        this.contract = contract;
        this.type = type;
        setToken(null);
    }
    protected ATSToken (Token token, ContractType type){
        contract=token.getContractAddress();
        this.type = type;
        setToken(token);
    }


    private AtomicBoolean initialized=new AtomicBoolean(true);


    /**
     *
     * @param holderAddress the holder's balance to be queried
     * @return the token holder information found from the blockchain
     */
    public abstract Optional<TokenHolders> getHolderDetails(String holderAddress, APIBlockDetails blockDetails);

    /**
     * @return the details for this token
     * @param contract
     */
    public abstract Optional<Token> getDetails(Contract contract);

    public Token details(){
        return token;
    }

    public final Optional<Token> updateDetails(Contract contract){
        setToken(null);
        return getDetails(contract);
    }

    public abstract BigInteger getBalance(String address) ;

    public void close(){
        checkState();
        initialized.compareAndSet(true, false);

    }
    protected final synchronized void setToken(Token token) {
        this.token = token;
    }

    protected final void checkState(){
        if (!initialized.get()){
            throw new IllegalStateException("ATSToken is not initialized");

        }
    }
}
