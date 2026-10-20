package academy.cashbackmerge;

import academy.cashbackmerge.config.CampaignConfig;
import academy.cashbackmerge.config.ConfigReader;
import academy.cashbackmerge.model.GameSession;
import academy.cashbackmerge.service.GameService;
import academy.cashbackmerge.simulation.MarkdownReportWriter;
import academy.cashbackmerge.simulation.RandomStrategy;
import academy.cashbackmerge.simulation.SimulationReport;
import academy.cashbackmerge.simulation.Simulator;
import academy.cashbackmerge.ui.ConsoleUI;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public final class App {
    private static final long DEMO_SEED = 42L;
    private static final String DEFAULT_CONFIG_PATH = "config/campaign.example.json";
    private static final int DEFAULT_PLAYERS = 10000;

    public void run(String[] args) {
        if (args.length > 0 && "simulate".equals(args[0])) {
            runSimulate(args);
        } else {
            runDemo();
        }
    }

    private void runDemo() {
        try {
            CampaignConfig config = ConfigReader.read(DEFAULT_CONFIG_PATH);
            GameService service = new GameService(config);
            GameSession session = service.startGame(DEMO_SEED);

            ConsoleUI ui = new ConsoleUI();
            ui.printBoard(session.getBoard());
        } catch (IOException e) {
            System.err.println("Не удалось загрузить конфиг: " + e.getMessage());
        }
    }

    private void runSimulate(String[] args) {
        try {
            String configPath = getArgValue(args, "--config").orElse(DEFAULT_CONFIG_PATH);
            int players = getArgValue(args, "--players").map(Integer::parseInt).orElse(DEFAULT_PLAYERS);
            String strategyName = getArgValue(args, "--strategy").orElse("random");
            long seed = getArgValue(args, "--seed").map(Long::parseLong).orElse(DEMO_SEED);
            String outputPath = getArgValue(args, "--output").orElse("report.md");

            if (!"random".equals(strategyName)) {
                System.err.println("Неизвестная стратегия: " + strategyName + ". Доступна только 'random'.");
                return;
            }

            CampaignConfig config = ConfigReader.read(configPath);
            GameService service = new GameService(config);
            Simulator simulator = new Simulator(service, config);
            RandomStrategy strategy = new RandomStrategy(seed);

            SimulationReport report = simulator.simulate(players, seed, strategy, strategyName);

            String markdown = MarkdownReportWriter.write(report);
            Files.writeString(Path.of(outputPath), markdown, StandardCharsets.UTF_8);

            System.out.println("Симуляция завершена.");
            System.out.println("Отчёт сохранён в: " + outputPath);
            System.out.println("Статус: " + report.status());

        } catch (IOException e) {
            System.err.println("Ошибка чтения/записи: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.err.println("Ошибка формата числа: " + e.getMessage());
        }
    }

    private Optional<String> getArgValue(String[] args, String flag) {
        for (int i = 0; i < args.length - 1; i++) {
            if (flag.equals(args[i])) {
                return Optional.of(args[i + 1]);
            }
        }
        return Optional.empty();
    }
}
