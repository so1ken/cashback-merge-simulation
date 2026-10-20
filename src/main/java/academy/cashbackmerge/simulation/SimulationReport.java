package academy.cashbackmerge.simulation;

import java.math.BigDecimal;
import java.util.Map;

public record SimulationReport(
        int players,
        int movesLimit,
        long seed,
        String strategy,
        Map<Integer, Integer> maxTilesDistribution,
        Map<Integer, Integer> rewardsDistribution,
        double averageScore,
        int maxScore,
        BigDecimal actualMaxRewardRate,
        String status) {}
