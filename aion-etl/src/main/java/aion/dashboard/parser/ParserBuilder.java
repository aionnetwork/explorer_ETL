package aion.dashboard.parser;

import aion.dashboard.blockchain.Web3Extractor;
import aion.dashboard.blockchain.interfaces.Web3Service;
import aion.dashboard.parser.type.ParserBatch;
import aion.dashboard.stats.RollingBlockMean;

import java.util.List;
import java.util.concurrent.BlockingQueue;

public class ParserBuilder {
    private Web3Extractor extractor;
    private BlockingQueue<List<ParserBatch>> queue;
    private RollingBlockMean rollingBlockMean;
    private IdleProducer<?, String> accountProd;
    private TokenParser tokenProd;
    private Web3Service apiService;
    private InternalTransactionParser internalTransactionProducer;

    public ParserBuilder setExtractor(Web3Extractor extractor) {
        this.extractor = extractor;
        return this;
    }

    public ParserBuilder setQueue(BlockingQueue<List<ParserBatch>> queue) {
        this.queue = queue;
        return this;
    }

    public ParserBuilder setRollingBlockMean(RollingBlockMean rollingBlockMean) {
        this.rollingBlockMean = rollingBlockMean;
        return this;
    }

    public ParserBuilder setAccountProd(IdleProducer<?, String> accountProd) {
        this.accountProd = accountProd;
        return this;
    }

    public ParserBuilder setTokenProd(TokenParser tokenProd) {
        this.tokenProd = tokenProd;
        return this;
    }

    public Parser createParser() {
        return new Parser(extractor, queue, rollingBlockMean, accountProd, tokenProd, apiService, internalTransactionProducer);
    }

    public ParserBuilder setApiService(Web3Service apiService) {
        this.apiService = apiService;
        return this;
    }

    public ParserBuilder setInternalTransactionProducer(InternalTransactionParser internalTransactionProducer) {
        this.internalTransactionProducer = internalTransactionProducer;
        return this;
    }
}