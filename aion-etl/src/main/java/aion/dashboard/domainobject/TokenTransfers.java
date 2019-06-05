package aion.dashboard.domainobject;

import aion.dashboard.parser.events.ContractEvent;
import org.aion.api.type.BlockDetails;
import org.aion.api.type.TxDetails;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static aion.dashboard.util.Utils.*;

public class TokenTransfers {

    private String operator;
    private String toAddress;
    private String fromAddress;
    private String contractAddress;
    private BigDecimal scaledValue;
    private String transactionHash;
    private String rawValue;
    private int tokendecimal;
    private BigDecimal granularity;
    private long blockNumber;
    private long transferTimestamp;
    private int blockYear;
    private int blockMonth;
    private int blockDay;
    private double approxValue;


    // linting rule squid:S00107 can be ignored here since the constructor is only accessed via the builder method
    private TokenTransfers(String operator, String toAddress, String fromAddress, BigDecimal value, String contractAddress, String transactionHash, String rawValue, int tokendecimal, BigDecimal granularity, long blockNumber, long transferTimestamp) {
        this.operator = operator;
        this.toAddress = toAddress;
        this.fromAddress = fromAddress;
        this.scaledValue = value;
        this.contractAddress = contractAddress;
        this.transactionHash = transactionHash;
        this.rawValue = rawValue;
        this.tokendecimal = tokendecimal;
        this.granularity = granularity;
        this.blockNumber = blockNumber;
        this.transferTimestamp = transferTimestamp;
        this.approxValue = approximate(new BigDecimal(rawValue), 18);

        ZonedDateTime zdt = getZDT(transferTimestamp);
        blockDay = zdt.getDayOfMonth();
        blockMonth = zdt.getMonthValue();
        blockYear = zdt.getYear();
    }

    public String getToAddress() {
        return toAddress;
    }

    public String getRawValue() {
        return rawValue;
    }

    public int getTokendecimal() {
        return tokendecimal;
    }

    public BigDecimal getGranularity() {
        return granularity;
    }

    public String getFromAddress() {
        return fromAddress;
    }

    public BigDecimal getScaledValue() {
        return scaledValue;
    }

    public String getContractAddress() {
        return contractAddress;
    }

    public String getTransactionHash() {
        return transactionHash;
    }

    public long getBlockNumber() {
        return blockNumber;
    }

    public long getTransferTimestamp() {
        return transferTimestamp;
    }

    public String getOperator() {
        return operator;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TokenTransfers)) return false;
        TokenTransfers tokenTransfers = (TokenTransfers) o;
        return getTransactionHash().equals(tokenTransfers.getTransactionHash()) &&
                getBlockNumber() == tokenTransfers.getBlockNumber() &&
                getTransferTimestamp() == tokenTransfers.getTransferTimestamp() &&
                Objects.equals(getToAddress(), tokenTransfers.getToAddress()) &&
                Objects.equals(getFromAddress(), tokenTransfers.getFromAddress()) &&
                Objects.equals(getScaledValue(), tokenTransfers.getScaledValue()) &&
                Objects.equals(getContractAddress(), tokenTransfers.getContractAddress()) &&
                Objects.equals(getOperator(), tokenTransfers.getOperator());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getToAddress(), getFromAddress(), getScaledValue(), getContractAddress(), getTransactionHash(), getBlockNumber(), getTransferTimestamp(), getFromAddress());
    }
    public int getBlockYear() {
        return blockYear;
    }


    public int getBlockMonth() {
        return blockMonth;
    }

    public int getBlockDay() {
        return blockDay;
    }

    public double getApproxValue() {
        return approxValue;
    }

    private static ThreadLocal<TransferBuilder> builderThreadLocal =  ThreadLocal.withInitial(TransferBuilder::new);


    static Optional<TokenTransfers> from(ContractEvent event, TxDetails tx, BlockDetails b, Token tkn){

        var optionalAmount = event.getInput("amount", BigInteger.class);
        var optionalOperator= event.getInput("operator", String.class);
        var optionalTo = event.getInput("to", String.class);
        var optionalFrom = event.getInput("from", String.class);
        if (tkn !=null && optionalAmount.isPresent() && optionalOperator.isPresent() && optionalTo.isPresent() && optionalFrom.isPresent()) {
            var rawValue = optionalAmount.get();
            var scaledValue = scaleTokenValue(rawValue, tkn.getTokenDecimal());
            TokenTransfers.TransferBuilder builder = builderThreadLocal.get();
            return Optional.of(
                    builder.setTransactionTimestamp(b.getTimestamp())
                            .setContractAddress(event.getAddress().replace("0x",""))
                            .setTransactionHash(tx.getTxHash().toString())
                            .setBlockNumber(b.getNumber())
                            .setOperator(optionalOperator.get())
                            .setToAddress(optionalTo.get().replace("0x",""))
                            .setFromAddress(optionalFrom.get().replace("0x",""))
                            .setScaledTokenValue(scaledValue)
                            .setRawValue(rawValue.toString())
                            .setTokendecimal(tkn.getTokenDecimal())
                            .setGranularity(new BigDecimal(tkn.getGranularity()))
                            .build());


        }
        else {
            return Optional.empty();
        }
    }

    public static List<TokenTransfers> tokenTransfersFrom(List<ContractEvent> eventList, TxDetails tx, BlockDetails b, Token tkn){
        return eventList.stream()
                .filter(event -> event.getEventName().equals("Sent"))
                .map(event -> from(event, tx,b,tkn))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }



    public static class TransferBuilder {
        private String operator;
        private String toAddress;
        private String fromAddress;
        private BigDecimal tokenValue;
        private String contractAddress;
        private String transactionHash;
        private long blockNumber;
        private long transactionTimestamp;
        private String rawValue;
        private int tokendecimal;
        private BigDecimal granularity;

        public TransferBuilder setRawValue(String rawValue) {
            this.rawValue = rawValue;
            return this;
        }

        public TransferBuilder setTokendecimal(int tokendecimal) {
            this.tokendecimal = tokendecimal;
            return this;
        }

        public TransferBuilder setGranularity(BigDecimal granularity) {
            this.granularity = granularity;
            return this;
        }


        public TransferBuilder setToAddress(String toAddress) {
            this.toAddress = toAddress;
            return this;
        }

        public TransferBuilder setFromAddress(String fromAddress) {
            this.fromAddress = fromAddress;
            return this;
        }

        public TransferBuilder setScaledTokenValue(BigDecimal tokenValue) {
            this.tokenValue = tokenValue;
            return this;
        }

        public TransferBuilder setContractAddress(String contractAddress) {
            this.contractAddress = contractAddress;
            return this;
        }

        public TransferBuilder setTransactionHash(String transactionHash) {
            this.transactionHash = transactionHash;
            return this;
        }

        public TransferBuilder setBlockNumber(long blockNumber) {
            this.blockNumber = blockNumber;
            return this;
        }

        public TransferBuilder setTransactionTimestamp(long transactionTimestamp) {
            this.transactionTimestamp = transactionTimestamp;
            return this;
        }


        public TokenTransfers build() {
            return new TokenTransfers(operator, toAddress, fromAddress, tokenValue, contractAddress, transactionHash, rawValue, tokendecimal, granularity, blockNumber, transactionTimestamp);
        }

        public TransferBuilder setOperator(String operator) {
            this.operator = operator;
            return this;
        }
    }
}
