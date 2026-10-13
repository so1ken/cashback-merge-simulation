package academy.cashbackmerge.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ConfigReader {

    private ConfigReader() {}

    public static CampaignConfig read(String path) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        try (InputStream is = Files.newInputStream(Path.of(path))) {
            ConfigDto dto = mapper.readValue(is, ConfigDto.class);
            return toDomain(dto);
        }
    }

    private static CampaignConfig toDomain(ConfigDto dto) {
        Map<Integer, BigDecimal> spawnRules = new LinkedHashMap<>();
        for (SpawnRuleDto r : dto.spawnRules()) {
            spawnRules.put(Integer.parseInt(r.cashback()), new BigDecimal(r.probability()));
        }

        Map<Integer, Integer> rewardTable = new LinkedHashMap<>();
        for (RewardTierDto r : dto.rewardTable()) {
            rewardTable.put(Integer.parseInt(r.cashback()), Integer.parseInt(r.reward()));
        }

        return new CampaignConfig(
                dto.boardSize(),
                dto.movesLimit(),
                spawnRules,
                rewardTable,
                Integer.parseInt(dto.maxRewardCashback()),
                new BigDecimal(dto.maxRewardAllowedRate()));
    }

    private record ConfigDto(
            int boardSize,
            int movesLimit,
            List<SpawnRuleDto> spawnRules,
            List<RewardTierDto> rewardTable,
            String maxRewardCashback,
            String maxRewardAllowedRate) {}

    private record SpawnRuleDto(String cashback, String probability) {}

    private record RewardTierDto(String cashback, String reward) {}
}
