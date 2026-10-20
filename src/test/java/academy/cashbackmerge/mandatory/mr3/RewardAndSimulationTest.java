package academy.cashbackmerge.mandatory.mr3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import academy.cashbackmerge.config.CampaignConfig;
import academy.cashbackmerge.model.Board;
import academy.cashbackmerge.model.GameSession;
import academy.cashbackmerge.model.Tile;
import academy.cashbackmerge.service.GameService;
import academy.cashbackmerge.simulation.PlayerStrategy;
import academy.cashbackmerge.simulation.RandomStrategy;
import academy.cashbackmerge.simulation.SimulationReport;
import academy.cashbackmerge.simulation.Simulator;
import java.math.BigDecimal;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Обязательные тесты: награды, симуляция и отчёт. */
@DisplayName("MR3. Награды и симуляция")
class RewardAndSimulationTest {

    private static final long SEED = 42L;

    @Test
    @DisplayName("Максимальная плитка 16% даёт награду соответствующего tier")
    void rewardMatchesHighestTile() {
        GameService service = new GameService(defaultConfig());

        assertEquals(500, service.getReward(16));
        assertEquals(2000, service.getReward(32));

        assertEquals(2000, service.getReward(64));
    }

    @Test
    @DisplayName("Weighted random учитывает вероятности из конфига")
    void spawnRespectsConfiguredProbabilities() {
        GameService service = new GameService(defaultConfig());
        int ones = 0;
        int twos = 0;

        for (int i = 0; i < 5000; i++) {
            GameSession session = service.startGame(i);
            Board board = session.getBoard();
            for (int r = 0; r < board.getBoardSize(); r++) {
                for (int c = 0; c < board.getBoardSize(); c++) {
                    Tile tile = board.getTile(r, c);
                    if (tile != null) {
                        if (tile.cashback() == 1) {
                            ones++;
                        }
                        if (tile.cashback() == 2) {
                            twos++;
                        }
                    }
                }
            }
        }

        assertTrue(ones > twos, "Единиц должно быть больше, так как их вероятность 85%");
        assertTrue(ones > 7500, "Единиц должно быть примерно 85% от 10000");
        assertTrue(twos > 1000, "Двоек должно быть примерно 15% от 10000");
    }

    @Test
    @DisplayName("Отчёт содержит распределение максимальных плиток и наград")
    void reportContainsDistributions() {
        GameService service = new GameService(defaultConfig());
        Simulator simulator = new Simulator(service, defaultConfig());
        PlayerStrategy strategy = new RandomStrategy(SEED);

        SimulationReport report = simulator.simulate(100, SEED, strategy, "random");

        assertNotNull(report.maxTilesDistribution());
        assertFalse(report.maxTilesDistribution().isEmpty(), "Распределение плиток не должно быть пустым");

        assertNotNull(report.rewardsDistribution());
        assertFalse(report.rewardsDistribution().isEmpty(), "Распределение наград не должно быть пустым");
    }

    @Test
    @DisplayName("Отчёт содержит долю игроков с максимальной наградой")
    void reportContainsMaxRewardRate() {
        GameService service = new GameService(defaultConfig());
        Simulator simulator = new Simulator(service, defaultConfig());
        PlayerStrategy strategy = new RandomStrategy(SEED);

        SimulationReport report = simulator.simulate(100, SEED, strategy, "random");

        assertNotNull(report.actualMaxRewardRate(), "Доля должна быть рассчитана");
        assertTrue(report.actualMaxRewardRate().compareTo(BigDecimal.ZERO) >= 0);
        assertTrue(report.actualMaxRewardRate().compareTo(BigDecimal.ONE) <= 0);
    }

    @Test
    @DisplayName("Статус OK, если actualMaxRewardRate <= maxRewardAllowedRate, иначе FAILED")
    void reportStatusReflectsBusinessConstraint() {
        PlayerStrategy strategy = new RandomStrategy(SEED);

        CampaignConfig strictConfig = configWithAllowedRate(new BigDecimal("0.01"));
        Simulator strictSimulator = new Simulator(new GameService(strictConfig), strictConfig);
        SimulationReport strictReport = strictSimulator.simulate(100, SEED, strategy, "random");
        assertEquals("FAILED", strictReport.status());

        CampaignConfig looseConfig = configWithAllowedRate(new BigDecimal("1.0"));
        Simulator looseSimulator = new Simulator(new GameService(looseConfig), looseConfig);
        SimulationReport looseReport = looseSimulator.simulate(100, SEED, strategy, "random");
        assertEquals("OK", looseReport.status());
    }

    private CampaignConfig defaultConfig() {
        return new CampaignConfig(
                4,
                100,
                Map.of(1, new BigDecimal("0.85"), 2, new BigDecimal("0.15")),
                Map.of(2, 0, 4, 50, 8, 150, 16, 500, 32, 2000),
                32,
                new BigDecimal("0.20"));
    }

    private CampaignConfig configWithAllowedRate(BigDecimal allowedRate) {
        return new CampaignConfig(
                4,
                100,
                Map.of(1, new BigDecimal("0.85"), 2, new BigDecimal("0.15")),
                Map.of(2, 0, 4, 50, 8, 150, 16, 500, 32, 2000),
                32,
                allowedRate);
    }
}
