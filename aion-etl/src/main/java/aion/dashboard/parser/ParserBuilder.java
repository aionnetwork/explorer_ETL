package aion.dashboard.parser;

import aion.dashboard.blockchain.AionService;
import aion.dashboard.blockchain.interfaces.APIService;
import aion.dashboard.blockchain.Extractor;
import aion.dashboard.parser.type.ParserBatch;
import aion.dashboard.service.RollingBlockMean;

import java.util.List;
import java.util.concurrent.BlockingQueue;

public class ParserBuilder {
    private Extractor extractor;
    private BlockingQueue<List<ParserBatch>> queue;
    private RollingBlockMean rollingBlockMean;
    private IdleProducer<?, String> accountProd;
    private TokenParser tokenProd;
    private AionService apiService;

    public ParserBuilder setExtractor(Extractor extractor) {
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
        return new Parser(extractor, queue, rollingBlockMean, accountProd, tokenProd, apiService);
    }

    public ParserBuilder setApiService(AionService apiService) {
        this.apiService = apiService;
        return this;
    }
}