package academy.cashbackmerge.service;

import academy.cashbackmerge.config.CampaignConfig;
import academy.cashbackmerge.model.Board;
import academy.cashbackmerge.model.GameSession;
import academy.cashbackmerge.model.Motion;
import academy.cashbackmerge.model.MoveResult;
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
        Board board = new Board(config.getBoardSize());
        GameSession session = new GameSession(board, config.getMovesLimit(), seed);

        placeRandomTile(session.getBoard(), session.getRandom());
        placeRandomTile(session.getBoard(), session.getRandom());

        return session;
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

    public MoveResult applyMove(GameSession session, Motion motion) {
        if (session.isFinished()) {
            throw new IllegalStateException("Игра уже завершена");
        }

        Board board = session.getBoard();
        int size = board.getBoardSize();
        boolean anyChanged = false;
        int totalScore = 0;

        for (int index = 0; index < size; index++) {
            List<Tile> line = extractLine(board, motion, index);
            if (needsReverse(motion)) {
                line = reversed(line);
            }

            LineResult result = processLine(line);
            anyChanged |= result.changed();
            totalScore += result.scoreGained();

            List<Tile> outputLine = needsReverse(motion) ? reversed(result.line()) : result.line();
            setLine(board, motion, index, outputLine);
        }

        if (!anyChanged) {
            return new MoveResult(false, 0);
        }

        session.addScore(totalScore);
        session.recordMove();
        placeRandomTile(board, session.getRandom());

        return new MoveResult(true, totalScore);
    }

    private List<Tile> extractLine(Board board, Motion motion, int index) {
        int size = board.getBoardSize();
        List<Tile> line = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            Tile tile = (motion == Motion.LEFT || motion == Motion.RIGHT)
                    ? board.getTile(index, i)
                    : board.getTile(i, index);
            line.add(tile);
        }
        return line;
    }

    private void setLine(Board board, Motion motion, int index, List<Tile> line) {
        int size = board.getBoardSize();
        for (int i = 0; i < size; i++) {
            if (motion == Motion.LEFT || motion == Motion.RIGHT) {
                board.setTile(index, i, line.get(i));
            } else {
                board.setTile(i, index, line.get(i));
            }
        }
    }

    private boolean needsReverse(Motion motion) {
        return motion == Motion.RIGHT || motion == Motion.DOWN;
    }

    private List<Tile> reversed(List<Tile> line) {
        List<Tile> result = new ArrayList<>(line);
        java.util.Collections.reverse(result);
        return result;
    }

    private LineResult processLine(List<Tile> line) {
        List<Tile> compressed = new ArrayList<>();
        for (Tile tile : line) {
            if (tile != null) {
                compressed.add(tile);
            }
        }

        List<Tile> merged = new ArrayList<>();
        int score = 0;
        int i = 0;
        while (i < compressed.size()) {
            if (i + 1 < compressed.size()
                    && compressed.get(i).cashback() == compressed.get(i + 1).cashback()) {
                int newValue = compressed.get(i).cashback() * 2;
                merged.add(new Tile(newValue));
                score += newValue;
                i += 2;
            } else {
                merged.add(compressed.get(i));
                i += 1;
            }
        }

        while (merged.size() < line.size()) {
            merged.add(null);
        }

        boolean changed = !line.equals(merged);

        return new LineResult(merged, score, changed);
    }

    public int getReward(int cashback) {
        if (cashback >= config.getMaxRewardCashback()) {
            return config.getRewardTable().getOrDefault(config.getMaxRewardCashback(), 0);
        }
        return config.getRewardTable().getOrDefault(cashback, 0);
    }

    private record LineResult(List<Tile> line, int scoreGained, boolean changed) {}
}
