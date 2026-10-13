package academy.cashbackmerge.model;

// Класс игровой доски
public final class Board {
    private final Tile[][] cells;
    private final int boardSize;

    public Board(int boardSize) {
        if (boardSize <= 0) {
            throw new IllegalArgumentException("Размер поля должен быть больше 0");
        }
        this.boardSize = boardSize;

        // Изначально заполняем доску null
        this.cells = new Tile[boardSize][boardSize];
    }

    public int getBoardSize() {
        return boardSize;
    }

    public Tile getTile(int row, int col) {
        checkTileCoordinates(row, col);
        return cells[row][col];
    }

    public void setTile(int row, int col, Tile tile) {
        checkTileCoordinates(row, col);
        cells[row][col] = tile;
    }

    public boolean isEmpty(int row, int col) {
        checkTileCoordinates(row, col);
        return cells[row][col] == null;
    }

    private void checkTileCoordinates(int x, int y) {
        if (x < 0 || y < 0 || x >= boardSize || y >= boardSize) {
            throw new IllegalArgumentException("Неверные координаты плитки");
        }
    }
}
