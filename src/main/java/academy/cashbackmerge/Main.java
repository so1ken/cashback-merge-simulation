package academy.cashbackmerge;

import academy.cashbackmerge.config.CampaignConfig;
import academy.cashbackmerge.config.ConfigReader;
import academy.cashbackmerge.model.GameSession;
import academy.cashbackmerge.service.GameService;
import academy.cashbackmerge.ui.ConsoleUI;
import java.io.IOException;

public final class Main {
    private static final long DEMO_SEED = 42L;

    private Main() {}

    public static void main(String[] args) {
        try {
            CampaignConfig config = ConfigReader.read("config/campaign.example.json");
            GameService service = new GameService(config);
            GameSession session = service.startGame(DEMO_SEED);

            ConsoleUI ui = new ConsoleUI();
            ui.printBoard(session.getBoard());
        } catch (IOException e) {
            System.err.println("Не удалось загрузить конфиг: " + e.getMessage());
        }
    }
}
