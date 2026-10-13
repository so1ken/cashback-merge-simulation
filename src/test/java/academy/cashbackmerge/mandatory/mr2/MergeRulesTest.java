package academy.cashbackmerge.mandatory.mr2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import academy.cashbackmerge.config.CampaignConfig;
import academy.cashbackmerge.model.Board;
import academy.cashbackmerge.model.GameSession;
import academy.cashbackmerge.model.Motion;
import academy.cashbackmerge.model.Tile;
import academy.cashbackmerge.service.GameService;
import java.math.BigDecimal;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/** Обязательные тесты: правила слияния плиток. */
@DisplayName("MR2. Слияние плиток")
class MergeRulesTest {

    private static final int MAX_MOVES = 100;
    private static final long SEED = 42L;

    @ParameterizedTest
    @CsvSource({
        "'1,1,.,.', '2,.,.,.'",
        "'1,1,1,.', '2,1,.,.'",
        "'1,1,1,1', '2,2,.,.'",
        "'2,2,4,4', '4,8,.,.'",
    })
    @DisplayName("Сдвиг влево: {0} -> {1}")
    void rowShiftsLeftAndMerges(String before, String expected) {
        Board board = boardWithFirstRow(before);
        GameSession session = new GameSession(board, MAX_MOVES, SEED);

        service().applyMove(session, Motion.LEFT);
        assertRowStartsWith(board, 0, expected);
    }

    @Test
    @DisplayName("Одна плитка не сливается дважды за ход: [1,1,2,.] -> [2,2,.,.]")
    void tileDoesNotMergeTwiceInOneMove() {
        Board board = boardWithFirstRow("1,1,2,.");
        GameSession session = new GameSession(board, MAX_MOVES, SEED);

        service().applyMove(session, Motion.LEFT);

        assertEquals(2, board.getTile(0, 0).cashback());
        assertEquals(2, board.getTile(0, 1).cashback());
    }

    @Test
    @DisplayName("Слияние работает во всех четырёх направлениях")
    void mergeWorksInAllDirections() {
        // LEFT: [.,.,.,2] -> [2,.,.,.]
        Board left = boardWithFirstRow(".,.,.,2");
        service().applyMove(new GameSession(left, MAX_MOVES, SEED), Motion.LEFT);
        assertEquals(2, left.getTile(0, 0).cashback());

        // RIGHT: [2,.,.,.] -> [.,.,.,2]
        Board right = boardWithFirstRow("2,.,.,.");
        service().applyMove(new GameSession(right, MAX_MOVES, SEED), Motion.RIGHT);
        assertEquals(2, right.getTile(0, 3).cashback());

        // UP: плитка в (3,0) -> (0,0)
        Board up = new Board(4);
        up.setTile(3, 0, new Tile(2));
        service().applyMove(new GameSession(up, MAX_MOVES, SEED), Motion.UP);
        assertEquals(2, up.getTile(0, 0).cashback());

        // DOWN: плитка в (0,0) -> (3,0)
        Board down = new Board(4);
        down.setTile(0, 0, new Tile(2));
        service().applyMove(new GameSession(down, MAX_MOVES, SEED), Motion.DOWN);
        assertEquals(2, down.getTile(3, 0).cashback());
    }

    private static Board boardWithFirstRow(String row) {
        Board board = new Board(4);
        String[] cells = row.split(",");
        for (int col = 0; col < 4; col++) {
            String cell = cells[col].trim();
            if (!".".equals(cell)) {
                board.setTile(0, col, new Tile(Integer.parseInt(cell)));
            }
        }
        return board;
    }

    private static void assertRowStartsWith(Board board, int row, String expected) {
        String[] cells = expected.split(",");
        for (int col = 0; col < cells.length; col++) {
            String cell = cells[col].trim();
            if (".".equals(cell)) {
                return;
            }
            Tile tile = board.getTile(row, col);
            assertNotNull(tile, "Ожидалась плитка в (" + row + "," + col + ")");
            assertEquals(Integer.parseInt(cell), tile.cashback(), "Неверное значение в (" + row + "," + col + ")");
        }
    }

    private static GameService service() {
        CampaignConfig config = new CampaignConfig(
                4,
                100,
                Map.of(1, new BigDecimal("1.0")),
                Map.of(2, 0, 4, 50, 8, 150, 16, 500, 32, 2000),
                32,
                new BigDecimal("0.20"));
        return new GameService(config);
    }
}
