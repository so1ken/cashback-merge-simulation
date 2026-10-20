package academy.cashbackmerge.simulation;

import academy.cashbackmerge.model.Board;
import academy.cashbackmerge.model.Motion;
import java.util.Random;

// Бот, который выбирает направление случайно
public final class RandomStrategy implements PlayerStrategy {
    private final Random random;

    public RandomStrategy(long seed) {
        this.random = new Random(seed);
    }

    @Override
    public Motion chooseMove(Board board) {
        Motion[] moves = Motion.values();
        return moves[random.nextInt(moves.length)];
    }
}
