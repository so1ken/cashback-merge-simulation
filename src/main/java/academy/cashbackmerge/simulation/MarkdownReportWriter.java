package academy.cashbackmerge.simulation;

import java.util.Map;

// Генерирует Markdown-отчёт по результатам симуляции
public final class MarkdownReportWriter {

    private MarkdownReportWriter() {}

    public static String write(SimulationReport report) {
        StringBuilder sb = new StringBuilder();

        sb.append("# Отчёт о симуляции Cashback Merge\n\n");

        sb.append("## Параметры запуска\n");
        sb.append("- **Игроков:** ").append(report.players()).append("\n");
        sb.append("- **Лимит ходов:** ").append(report.movesLimit()).append("\n");
        sb.append("- **Seed:** ").append(report.seed()).append("\n");
        sb.append("- **Стратегия:** ").append(report.strategy()).append("\n\n");

        sb.append("## Результаты\n");
        sb.append("- **Средний score:** ")
                .append(String.format("%.2f", report.averageScore()))
                .append("\n");
        sb.append("- **Максимальный score:** ").append(report.maxScore()).append("\n");
        sb.append("- **Доля игроков с максимальной наградой:** ")
                .append(String.format("%.2f%%", report.actualMaxRewardRate().multiply(new java.math.BigDecimal("100"))))
                .append("\n");
        sb.append("- **Статус бизнес-ограничения:** ").append(report.status()).append("\n\n");

        sb.append("## Распределение максимальных плиток\n");
        sb.append("| Плитка (%) | Количество игроков |\n");
        sb.append("|------------|--------------------|\n");
        for (Map.Entry<Integer, Integer> entry : report.maxTilesDistribution().entrySet()) {
            sb.append("| ")
                    .append(entry.getKey())
                    .append(" | ")
                    .append(entry.getValue())
                    .append(" |\n");
        }
        sb.append("\n");

        sb.append("## Распределение наград\n");
        sb.append("| Награда (руб.) | Количество игроков |\n");
        sb.append("|----------------|--------------------|\n");
        for (Map.Entry<Integer, Integer> entry : report.rewardsDistribution().entrySet()) {
            sb.append("| ")
                    .append(entry.getKey())
                    .append(" | ")
                    .append(entry.getValue())
                    .append(" |\n");
        }
        sb.append("\n");

        if ("OK".equals(report.status())) {
            sb.append("> ✅ **Ограничение выполнено:** доля победителей не превышает лимит.\n");
        } else {
            sb.append("> ❌ **Ограничение нарушено:** доля победителей превышает допустимую. Акция убыточна.\n");
        }

        return sb.toString();
    }
}
