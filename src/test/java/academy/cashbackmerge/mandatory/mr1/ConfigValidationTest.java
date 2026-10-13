package academy.cashbackmerge.mandatory.mr1;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import academy.cashbackmerge.config.CampaignConfig;
import java.math.BigDecimal;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Обязательные тесты: конфиг кампании. */
@DisplayName("MR1. Конфиг кампании")
class ConfigValidationTest {

    @Test
    @DisplayName("Валидный конфиг принимается")
    void validConfigIsAccepted() {
        assertDoesNotThrow(() -> new CampaignConfig(
                4,
                100,
                Map.of(1, new BigDecimal("0.85"), 2, new BigDecimal("0.15")),
                Map.of(2, 0, 4, 50, 8, 150, 16, 500, 32, 2000),
                32,
                new BigDecimal("0.20")));
    }

    @Test
    @DisplayName("Сумма вероятностей spawn-правил не равна 1 — конфиг отклоняется")
    void probabilitiesNotSummingToOneAreRejected() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new CampaignConfig(
                        4,
                        100,
                        Map.of(1, new BigDecimal("0.80"), 2, new BigDecimal("0.15")), // 0.95 ≠ 1
                        Map.of(2, 0, 4, 50, 8, 150, 16, 500, 32, 2000),
                        32,
                        new BigDecimal("0.20")));
    }

    @Test
    @DisplayName("movesLimit <= 0 — конфиг отклоняется")
    void nonPositiveMovesLimitIsRejected() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new CampaignConfig(
                        4,
                        0, // ← невалидно
                        Map.of(1, new BigDecimal("0.85"), 2, new BigDecimal("0.15")),
                        Map.of(2, 0, 4, 50, 8, 150, 16, 500, 32, 2000),
                        32,
                        new BigDecimal("0.20")));
    }

    @Test
    @DisplayName("Пустой список spawn-правил — конфиг отклоняется")
    void emptySpawnRulesAreRejected() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new CampaignConfig(
                        4,
                        100,
                        Map.of(), // ← пустой
                        Map.of(2, 0, 4, 50, 8, 150, 16, 500, 32, 2000),
                        32,
                        new BigDecimal("0.20")));
    }
}
