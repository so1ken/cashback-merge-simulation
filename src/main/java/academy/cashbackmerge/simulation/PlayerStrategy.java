package academy.cashbackmerge.simulation;

import academy.cashbackmerge.model.Board;
import academy.cashbackmerge.model.Motion;

public interface PlayerStrategy {
    Motion chooseMove(Board board);
}
