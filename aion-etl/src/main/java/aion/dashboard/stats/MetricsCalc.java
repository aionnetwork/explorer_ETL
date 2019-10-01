package aion.dashboard.stats;

import aion.dashboard.blockchain.Web3ServiceImpl;
import aion.dashboard.blockchain.type.APIBlock;
import aion.dashboard.blockchain.type.APIBlockDetails;
import aion.dashboard.config.Config;
import aion.dashboard.domainobject.Block;
import aion.dashboard.domainobject.SealInfo;
import aion.dashboard.domainobject.ValidatorStats;
import aion.dashboard.service.AccountServiceImpl;
import aion.dashboard.service.DBService;
import aion.dashboard.util.Utils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static aion.dashboard.blockchain.type.APIBlock.SealType.POW;

public class MetricsCalc {

    private MetricsCalc(){
        throw new UnsupportedOperationException();
    }

    /**
     * @param instant     the time for the circulating supply
     * @return
     */
    public static Optional<BigDecimal> networkStakingPercentage(Instant instant, BigDecimal networkStake) {
        Config config = Config.getInstance();
        try {
            BigDecimal circulatingSupply;
            if (config.getNetwork().equalsIgnoreCase("mainnet")) {
                circulatingSupply = DBService.getInstance().getCirculatingSupply(instant);
            } else {
                circulatingSupply = AccountServiceImpl.getInstance().sumBalance(instant);
            }
            return Optional.of(networkStake.divide(circulatingSupply, MathContext.DECIMAL128).multiply(BigDecimal.valueOf(100)));

        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public static BigDecimal networkStake(long blockNumber){
        try {
            String address = Config.getInstance().getStakingContractAddress();
            if (address.length() >= 64) return Utils.toAion(Web3ServiceImpl.getInstance().getBalanceAt(address, blockNumber));
            else return BigDecimal.ZERO;
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    /**
     * Returns the average block reward
     *
     * @param apiBlockDetails
     * @return
     */
    public static BigDecimal avgIssuance(List<APIBlockDetails> apiBlockDetails) {
        validateBlocks(apiBlockDetails);
        return apiBlockDetails.stream()
                .map(APIBlockDetails::getBlockReward)
                .reduce(BigInteger::add)
                .map(BigDecimal::new)
                .map(b -> b.divide(BigDecimal.valueOf(apiBlockDetails.size()), MathContext.DECIMAL32))
                .orElse(BigDecimal.valueOf(-999));
    }

    public static BigDecimal avgDifficulty(List<APIBlockDetails> apiBlockDetails) {
        validateBlocks(apiBlockDetails);
        return apiBlockDetails.stream()
                .map(APIBlockDetails::getDifficulty)//get the block difficulty
                .map(BigDecimal::new)
                .reduce(BigDecimal::add)// sum
                .map(b -> b.divide(BigDecimal.valueOf(apiBlockDetails.size()), MathContext.DECIMAL32))
                .orElse(BigDecimal.valueOf(-999));//avg
    }

    public static BigDecimal avgBlockTime(List<APIBlockDetails> blockDetails) {
        validateBlocks(blockDetails);
        long res = 0L;
        long currentBlockTimestamp = -1;

        for (APIBlockDetails blockDetail : blockDetails) {
            long previousTimestamp = currentBlockTimestamp;// set the previous block timestamp
            currentBlockTimestamp = blockDetail.getTimestamp();// set the current block timestamp to this block
            if (previousTimestamp != -1) {
                //find the block time and sum
                res += (currentBlockTimestamp - previousTimestamp);
            }
        }
        if (blockDetails.size()<=1) {
            return BigDecimal.valueOf(-999);
        } else {
            return BigDecimal.valueOf(res).divide(BigDecimal.valueOf(blockDetails.size()), MathContext.DECIMAL32);
        }
    }

    public static BigDecimal avgBlockTimeDO(List<Block> blockDetails) {
        long res = 0L;
        long currentBlockTimestamp = -1;

        for (Block blockDetail : blockDetails) {
            long previousTimestamp = currentBlockTimestamp;// set the previous block timestamp
            currentBlockTimestamp = blockDetail.getBlockTimestamp();// set the current block timestamp to this block
            if (previousTimestamp != -1) {
                //find the block time and sum
                res += (currentBlockTimestamp - previousTimestamp);
            }
        }
        if (blockDetails.size()<=1) {
            return BigDecimal.valueOf(-999);
        } else {
            return BigDecimal.valueOf(res).divide(BigDecimal.valueOf(blockDetails.size()), MathContext.DECIMAL32);
        }
    }

    public static BigDecimal hashRate(List<APIBlockDetails> blockDetails) {
        validateBlockTypes(blockDetails, POW);
        BigDecimal averageBlockTime = avgBlockTime(blockDetails);
        if (averageBlockTime.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.valueOf(-999L);
        } else {
            return blockDetails.stream()
                    .max(Comparator.comparing(APIBlockDetails::getNumber))
                    .map(APIBlockDetails::getDifficulty)
                    .map(BigDecimal::new)
                    .map(b-> b.divide(averageBlockTime, MathContext.DECIMAL32))
                    .orElse(BigDecimal.valueOf(-999L));
        }

    }

    private static void validateBlockTypes(List<APIBlockDetails> blockDetails, APIBlock.SealType type) {
        if (!blockDetails.stream().allMatch(b -> b.getSealType().equals(type))) throw new IllegalArgumentException();
    }

    private static void validateBlocks(List<APIBlockDetails> blockDetails) {
        if (!blockDetails.isEmpty()) {
            validateBlockTypes(blockDetails, blockDetails.get(0).getSealType());
        }
    }

    public static List<ValidatorStats> calculateStats(List<SealInfo> sealInfos, Block block){
        return countValidators(sealInfos).entrySet()
                .stream()
                .map(e -> {
                    String[] splitStr = e.getKey().split("-");
                    String addr = splitStr[0];// get the address
                    String sealType = splitStr[1]; // get the seal type
                    return new ValidatorStats(block.getBlockNumber(),
                            addr,
                            sealType,
                            e.getValue().size(),
                            block.getBlockTimestamp(),
                            sealInfos.size(),
                            avgTxnForMiner(e.getValue())
                           );
                }).collect(Collectors.toUnmodifiableList());
    }

    static Map<String, List<SealInfo>> countValidators(List<SealInfo> sealInfosToGroup){
        Map<String, List<SealInfo>> validatorInfoMap = new HashMap<>();
        for(SealInfo info: sealInfosToGroup){
            String key = info.getMinerAddress() + "-" + info.getSealType();// create a unique signature for each validator
            //using their coinbase address and sealed block type
            if (!validatorInfoMap.containsKey(key)){// first time this validator was encountered
                validatorInfoMap.put(key, new ArrayList<>());
            }
            validatorInfoMap.get(key).add(info);
        }
        return validatorInfoMap;
    }

    private static BigDecimal avgNums(List<Integer> integers){
        return BigDecimal.valueOf(integers.stream()
                .reduce(0, Integer::sum))
                .divide(BigDecimal.valueOf(integers.size()), MathContext.DECIMAL32);
    }

    private static BigDecimal avgTxnForMiner(List<SealInfo> sealInfos){
        return avgNums(sealInfos.stream()
                .map(SealInfo::getTransactionNum)
                .collect(Collectors.toList()));
    }
}
