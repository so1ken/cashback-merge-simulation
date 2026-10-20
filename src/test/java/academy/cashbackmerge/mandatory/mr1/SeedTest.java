package academy.cashbackmerge.mandatory.mr1;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import academy.cashbackmerge.config.CampaignConfig;
import academy.cashbackmerge.config.ConfigReader;
import academy.cashbackmerge.model.Board;
import academy.cashbackmerge.model.GameSession;
import academy.cashbackmerge.service.GameService;
import academy.cashbackmerge.simulation.RandomStrategy;
import academy.cashbackmerge.simulation.SimulationReport;
import academy.cashbackmerge.simulation.Simulator;
import java.io.IOException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Обязательные тесты: воспроизводимость по seed. */
@DisplayName("MR1. Воспроизводимость по seed")
class SeedTest {

    @Test
    @DisplayName("Одинаковый конфиг и seed дают одинаковое начальное поле")
    void sameConfigAndSeedProduceSameInitialBoard() throws IOException {
        CampaignConfig config = ConfigReader.read("config/campaign.example.json");
        GameService service = new GameService(config);

        GameSession first = service.startGame(42L);
        GameSession second = service.startGame(42L);

        assertThat(boardsEqual(first.getBoard(), second.getBoard())).isTrue();
    }

    @Test
    @DisplayName("Одинаковый конфиг, seed и стратегия дают одинаковый отчёт симуляции")
    void sameInputsProduceSameSimulationReport() throws IOException {
        CampaignConfig config = ConfigReader.read("config/campaign.example.json");
        GameService service = new GameService(config);
        Simulator simulator = new Simulator(service, config);

        SimulationReport report1 = simulator.simulate(10, 42L, new RandomStrategy(42L), "random");
        SimulationReport report2 = simulator.simulate(10, 42L, new RandomStrategy(42L), "random");

        assertEquals(report1, report2);
    }

    /** Сравнивает два поля 4x4 поэлементно. */
    private static boolean boardsEqual(Board first, Board second) {
        if (first.getBoardSize() != second.getBoardSize()) {
            return false;
        }
        for (int row = 0; row < first.getBoardSize(); row++) {
            for (int col = 0; col < first.getBoardSize(); col++) {
                if (!tilesEqual(first.getTile(row, col), second.getTile(row, col))) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean tilesEqual(academy.cashbackmerge.model.Tile a, academy.cashbackmerge.model.Tile b) {
        if (a == null && b == null) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }
        return a.cashback() == b.cashback();
    }
}
