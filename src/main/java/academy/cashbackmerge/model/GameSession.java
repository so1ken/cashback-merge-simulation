package academy.cashbackmerge.model;

public final class GameSession {
    private final Board board;
    private final int movesLimit;
    private int movesUsed;
    private GameStatus status;

    public GameSession(Board board, int movesLimit) {
        if (movesLimit <= 0) {
            throw new IllegalArgumentException("movesLimit должен быть > 0");
        }
        this.board = board;
        this.movesLimit = movesLimit;
        this.movesUsed = 0;
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

    public GameStatus getStatus() {
        return status;
    }

    public boolean isFinished() {
        return status == GameStatus.FINISHED;
    }
}
