package academy.cashbackmerge.model;

public record MoveResult(boolean moved, int scoreGained) {

    public MoveResult {
        if (scoreGained < 0) {
            throw new IllegalArgumentException("scoreGained не может быть отрицательным");
        }
        if (!moved && scoreGained != 0) {
            throw new IllegalArgumentException("Невалидный ход не может приносить очки");
        }
    }
}
