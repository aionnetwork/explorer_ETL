package aion.dashboard.consumer;

import aion.dashboard.parser.Producer;
import aion.dashboard.parser.type.AccountBatch;
import aion.dashboard.parser.type.ParserBatch;
import aion.dashboard.parser.type.TokenBatch;
import aion.dashboard.service.ReorgService;

public class ConsumerBuilder {
    private Producer<ParserBatch> blockProducer;
    private Producer<TokenBatch> tokenProducer;
    private Producer<AccountBatch> accountProducer;
    private WriteTask<ParserBatch> blockWriter;
    private WriteTask<AccountBatch> accountWriter;
    private WriteTask<TokenBatch> tokenWriter;
    private ReorgService service;

    public ConsumerBuilder setBlockProducer(Producer<ParserBatch> blockProducer) {
        this.blockProducer = blockProducer;
        return this;
    }

    public ConsumerBuilder setTokenProducer(Producer<TokenBatch> tokenProducer) {
        this.tokenProducer = tokenProducer;
        return this;
    }

    public ConsumerBuilder setAccountProducer(Producer<AccountBatch> accountProducer) {
        this.accountProducer = accountProducer;
        return this;
    }

    public ConsumerBuilder setBlockWriter(WriteTask<ParserBatch> blockWriter) {
        this.blockWriter = blockWriter;
        return this;
    }

    public ConsumerBuilder setAccountWriter(WriteTask<AccountBatch> accountWriter) {
        this.accountWriter = accountWriter;
        return this;
    }

    public ConsumerBuilder setTokenWriter(WriteTask<TokenBatch> tokenWriter) {
        this.tokenWriter = tokenWriter;
        return this;
    }

    public ConsumerBuilder setService(ReorgService service) {
        this.service = service;
        return this;
    }

    public Consumer createConsumer() {
        return new Consumer(blockProducer, tokenProducer, accountProducer, blockWriter, accountWriter, tokenWriter, service);
    }
}