package academy.cashbackmerge.config;

import java.math.BigDecimal;
import java.util.Map;

public final class CampaignConfig {
    private final int boardSize;
    private final int movesLimit;
    private final Map<Integer, BigDecimal> spawnRules;
    private final Map<Integer, Integer> rewardTable;
    private final int maxRewardCashback;
    private final BigDecimal maxRewardAllowedRate;

    public CampaignConfig(
            int boardSize,
            int movesLimit,
            Map<Integer, BigDecimal> spawnRules,
            Map<Integer, Integer> rewardTable,
            int maxRewardCashback,
            BigDecimal maxRewardAllowedRate) {

        validateBoardSize(boardSize);
        validateMovesLimit(movesLimit);
        validateSpawnRules(spawnRules);
        validateRewardTable(rewardTable, maxRewardCashback);
        validateMaxRewardAllowedRate(maxRewardAllowedRate);

        this.boardSize = boardSize;
        this.movesLimit = movesLimit;
        this.spawnRules = Map.copyOf(spawnRules);
        this.rewardTable = Map.copyOf(rewardTable);
        this.maxRewardCashback = maxRewardCashback;
        this.maxRewardAllowedRate = maxRewardAllowedRate;
    }

    public int getBoardSize() {
        return boardSize;
    }

    public int getMovesLimit() {
        return movesLimit;
    }

    public Map<Integer, BigDecimal> getSpawnRules() {
        return spawnRules;
    }

    public Map<Integer, Integer> getRewardTable() {
        return rewardTable;
    }

    public int getMaxRewardCashback() {
        return maxRewardCashback;
    }

    public BigDecimal getMaxRewardAllowedRate() {
        return maxRewardAllowedRate;
    }

    private void validateBoardSize(int boardSize) {
        if (boardSize <= 0) {
            throw new IllegalArgumentException("boardSize должен быть > 0");
        }
    }

    private void validateMovesLimit(int movesLimit) {
        if (movesLimit <= 0) {
            throw new IllegalArgumentException("movesLimit должен быть > 0");
        }
    }

    private void validateSpawnRules(Map<Integer, BigDecimal> spawnRules) {
        if (spawnRules == null || spawnRules.isEmpty()) {
            throw new IllegalArgumentException("spawnRules не могут быть пустыми");
        }

        BigDecimal sumProbability = BigDecimal.ZERO;
        for (Map.Entry<Integer, BigDecimal> entry : spawnRules.entrySet()) {
            if (entry.getValue().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Вероятности не могут быть отрицательными");
            }
            sumProbability = sumProbability.add(entry.getValue());
        }

        if (sumProbability.compareTo(BigDecimal.ONE) != 0) {
            throw new IllegalArgumentException("Сумма вероятностей != 1, а " + sumProbability);
        }
    }

    private void validateRewardTable(Map<Integer, Integer> rewardTable, int maxRewardCashback) {
        if (rewardTable == null || rewardTable.isEmpty()) {
            throw new IllegalArgumentException("rewardTable не может быть пуста");
        }

        if (!rewardTable.containsKey(maxRewardCashback)) {
            throw new IllegalArgumentException("maxRewardCashback не найден в rewardTable");
        }
    }

    private void validateMaxRewardAllowedRate(BigDecimal maxRewardAllowedRate) {
        if (maxRewardAllowedRate == null
                || maxRewardAllowedRate.signum() <= 0
                || maxRewardAllowedRate.compareTo(BigDecimal.ONE) > 0) {
            throw new IllegalArgumentException("maxRewardAllowedRate должен быть в диапазоне (0; 1]");
        }
    }
}
