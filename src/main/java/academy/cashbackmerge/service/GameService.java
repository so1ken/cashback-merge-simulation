package academy.cashbackmerge.service;

import academy.cashbackmerge.config.CampaignConfig;
import academy.cashbackmerge.model.Board;
import academy.cashbackmerge.model.GameSession;
import academy.cashbackmerge.model.Tile;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public final class GameService {
    private final CampaignConfig config;

    public GameService(CampaignConfig config) {
        this.config = config;
    }

    public GameSession startGame(long seed) {
        Random random = new Random(seed);
        Board board = new Board(config.getBoardSize());

        placeRandomTile(board, random);
        placeRandomTile(board, random);

        return new GameSession(board, config.getMovesLimit());
    }

    private void placeRandomTile(Board board, Random random) {
        List<int[]> emptyTiles = new ArrayList<>();
        for (int i = 0; i < config.getBoardSize(); i++) {
            for (int j = 0; j < config.getBoardSize(); j++) {
                if (board.getTile(i, j) == null) {
                    emptyTiles.add(new int[] {i, j});
                }
            }
        }

        int randomIndex = random.nextInt(emptyTiles.size());
        int[] position = emptyTiles.get(randomIndex);
        int row = position[0];
        int col = position[1];

        int cashback = pickCashback(random);

        board.setTile(row, col, new Tile(cashback));
    }

    private int pickCashback(Random random) {
        BigDecimal r = BigDecimal.valueOf(random.nextDouble());
        BigDecimal cumulative = BigDecimal.ZERO;
        int lastCashback = 0;

        for (Map.Entry<Integer, BigDecimal> entry : config.getSpawnRules().entrySet()) {
            cumulative = cumulative.add(entry.getValue());
            lastCashback = entry.getKey();
            if (r.compareTo(cumulative) < 0) {
                return entry.getKey();
            }
        }
        return lastCashback;
    }
}
