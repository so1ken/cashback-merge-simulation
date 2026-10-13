package academy.cashbackmerge.ui;

import academy.cashbackmerge.model.Board;
import academy.cashbackmerge.model.Tile;

public final class ConsoleUI {
    public void printBoard(Board board) {
        for (int row = 0; row < board.getBoardSize(); row++) {
            StringBuilder line = new StringBuilder();
            for (int col = 0; col < board.getBoardSize(); col++) {
                Tile tile = board.getTile(row, col);
                if (tile == null) {
                    line.append(".").append(" ");
                } else {
                    line.append(tile.cashback()).append(" ");
                }
            }
            System.out.println(line);
        }
    }
}
