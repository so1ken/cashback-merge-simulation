package academy.cashbackmerge.simulation;

import academy.cashbackmerge.config.CampaignConfig;
import academy.cashbackmerge.model.Board;
import academy.cashbackmerge.model.GameSession;
import academy.cashbackmerge.model.Motion;
import academy.cashbackmerge.model.Tile;
import academy.cashbackmerge.service.GameService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

public final class Simulator {
    private static final int RATE_SCALE = 4;

    private final GameService gameService;
    private final CampaignConfig config;

    public Simulator(GameService gameService, CampaignConfig config) {
        this.gameService = gameService;
        this.config = config;
    }

    public SimulationReport simulate(int players, long seed, PlayerStrategy strategy, String strategyName) {
        Map<Integer, Integer> maxTiles = new HashMap<>();
        Map<Integer, Integer> rewards = new HashMap<>();
        long totalScore = 0;
        int maxScore = 0;
        int maxRewardCount = 0;

        for (int i = 0; i < players; i++) {
            // Каждому игроку даём уникальный seed, чтобы игры были разными
            long playerSeed = seed + i;
            GameSession session = gameService.startGame(playerSeed);

            playGame(session, strategy);

            int maxTile = findMaxTile(session.getBoard());
            int reward = gameService.getReward(maxTile);

            maxTiles.merge(maxTile, 1, Integer::sum);
            rewards.merge(reward, 1, Integer::sum);

            totalScore += session.getScore();
            if (session.getScore() > maxScore) {
                maxScore = session.getScore();
            }

            if (maxTile >= config.getMaxRewardCashback()) {
                maxRewardCount++;
            }
        }

        BigDecimal actualRate = BigDecimal.valueOf(maxRewardCount)
                .divide(BigDecimal.valueOf(players), RATE_SCALE, RoundingMode.HALF_UP);

        String status = actualRate.compareTo(config.getMaxRewardAllowedRate()) <= 0 ? "OK" : "FAILED";
        double avgScore = (double) totalScore / players;

        return new SimulationReport(
                players,
                config.getMovesLimit(),
                seed,
                strategyName,
                maxTiles,
                rewards,
                avgScore,
                maxScore,
                actualRate,
                status);
    }

    // Крутит игру до победного конца или лимита ходов
    private void playGame(GameSession session, PlayerStrategy strategy) {
        while (!session.isFinished()) {
            Motion move = strategy.chooseMove(session.getBoard());
            boolean moved = gameService.applyMove(session, move).moved();

            // Если бот сделал невалидный ход - не изменил поле, то пробуем остальные направления
            if (!moved) {
                moved = tryOtherMoves(session, move);
            }

            // Если ни один ход не сработал — поле "заблокировано"
            if (!moved) {
                break;
            }
        }
    }

    private boolean tryOtherMoves(GameSession session, Motion excludeMove) {
        for (Motion altMove : Motion.values()) {
            if (altMove != excludeMove) {
                if (gameService.applyMove(session, altMove).moved()) {
                    return true;
                }
            }
        }
        return false;
    }

    private int findMaxTile(Board board) {
        int max = 0;
        for (int r = 0; r < board.getBoardSize(); r++) {
            for (int c = 0; c < board.getBoardSize(); c++) {
                Tile tile = board.getTile(r, c);
                if (tile != null && tile.cashback() > max) {
                    max = tile.cashback();
                }
            }
        }
        return max;
    }
}
