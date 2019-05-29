package aion.dashboard.integrityChecks;

import aion.dashboard.blockchain.APIService;
import aion.dashboard.blockchain.type.APIBlock;
import aion.dashboard.domainobject.Block;
import aion.dashboard.service.BlockService;
import aion.dashboard.service.ParserStateService;
import aion.dashboard.util.Tuple2;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class BlockIntegrityCheck extends IntegrityCheck<Block, Tuple2<Block, APIBlock>> {

    private final BlockService service;
    private final APIService apiService;
    private final ParserStateService parserStateService;

    public BlockIntegrityCheck( BlockService service, APIService apiService, ParserStateService parserStateService) {
        super("block-integrity-check", "Block");
        this.service = service;
        this.apiService = apiService;
        this.parserStateService = parserStateService;
    }

    @Override
    protected List<Tuple2<Block, APIBlock>> integrityCheck(List<Block> candidates) throws Exception {

        List<Tuple2<Block, APIBlock>> res = checkListIsContinuous(candidates);
        for (var block: candidates){
            var apiBlock = apiService.getBlock(block.getBlockNumber());
            if (!apiBlock.compareBlocks(block)){
                res.add(new Tuple2<>(block, apiBlock));
            }
        }
        return res;
    }


    private List<Tuple2<Block,APIBlock>> checkListIsContinuous(List<Block> blocks){
        if (blocks.isEmpty()){
            return new ArrayList<>();
        }
        else {
            Block prevBlock= blocks.get(0);
            List<Tuple2<Block,APIBlock>> res = new ArrayList<>();

            for (int i=1;i<blocks.size();i++){
                if (blocks.get(i).getBlockNumber() != prevBlock.getBlockNumber()+1){
                    res.add(new Tuple2<>(blocks.get(i), null));

                }

                prevBlock = blocks.get(i);
            }

            return res;
        }
    }

    /**
     * @return the random list of values to use for the integrity check
     */
    @Override
    protected List<Block> findCandidates() throws SQLException {
        return getRandomBlocks(parserStateService,service);
    }





    @Override
    protected void printFailure(List<Tuple2<Block, APIBlock>> failedCandidates) {
        if (INTEGRITY_LOGGER.isWarnEnabled()) {
            for(var tuple : failedCandidates){
                INTEGRITY_LOGGER.warn("Block check failed for: {}", tuple);
            }
        }

    }

    @Override
    protected void printSuccess(List<Block> succeededCandidates) {
        if (INTEGRITY_LOGGER.isTraceEnabled()) {
            for(var tuple : succeededCandidates){
                INTEGRITY_LOGGER.trace("Block check succeeded for: {}", tuple);
            }
        }
    }
}
