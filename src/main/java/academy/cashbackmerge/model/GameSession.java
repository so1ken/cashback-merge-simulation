package academy.cashbackmerge.model;

import java.util.Random;

public final class GameSession {
    private final Board board;
    private final int movesLimit;
    private final Random random;
    private int movesUsed;
    private int score;
    private GameStatus status;

    public GameSession(Board board, int movesLimit, long seed) {
        if (movesLimit <= 0) {
            throw new IllegalArgumentException("movesLimit должен быть > 0");
        }
        this.board = board;
        this.movesLimit = movesLimit;
        this.random = new Random(seed);
        this.movesUsed = 0;
        this.score = 0;
        this.status = GameStatus.IN_PROCESS;
    }

    public Board getBoard() {
        return board;
    }

    public int getMovesLimit() {
        return movesLimit;
    }

    public int getMovesUsed() {
        return movesUsed;
    }

    public int getScore() {
        return score;
    }

    public GameStatus getStatus() {
        return status;
    }

    public Random getRandom() {
        return random;
    }

    public boolean isFinished() {
        return status == GameStatus.FINISHED;
    }

    public void addScore(int delta) {
        if (delta < 0) {
            throw new IllegalArgumentException("delta не может быть отрицательной");
        }
        this.score += delta;
    }

    public void recordMove() {
        if (isFinished()) {
            throw new IllegalStateException("Игра уже завершена");
        }
        movesUsed++;
        if (movesUsed >= movesLimit) {
            this.status = GameStatus.FINISHED;
        }
    }
}
