package academy.cashbackmerge.mandatory.mr2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import academy.cashbackmerge.config.CampaignConfig;
import academy.cashbackmerge.model.Board;
import academy.cashbackmerge.model.GameSession;
import academy.cashbackmerge.model.GameStatus;
import academy.cashbackmerge.model.Motion;
import academy.cashbackmerge.model.MoveResult;
import academy.cashbackmerge.model.Tile;
import academy.cashbackmerge.service.GameService;
import java.math.BigDecimal;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Обязательные тесты: движок ходов — что считается ходом и что после него происходит. */
@DisplayName("MR2. Движок ходов")
class MoveEngineTest {

    private static final long SEED = 42L;

    @Test
    @DisplayName("Невалидный ход не меняет поле, не тратит ход и не создаёт плитку")
    void invalidMoveChangesNothing() {
        // Строка уже «упакована» влево, слияний нет → ход LEFT невалиден.
        Board board = new Board(4);
        board.setTile(0, 0, new Tile(1));
        board.setTile(0, 1, new Tile(2));
        board.setTile(0, 2, new Tile(4));
        board.setTile(0, 3, new Tile(8));

        GameSession session = new GameSession(board, 100, SEED);
        int beforeNonEmpty = countNonEmpty(board);

        MoveResult result = service().applyMove(session, Motion.LEFT);

        assertFalse(result.moved(), "Ход должен быть невалидным");
        assertEquals(0, result.scoreGained());
        assertEquals(0, session.getMovesUsed(), "Попытка не должна тратиться");
        assertEquals(0, session.getScore());
        assertEquals(beforeNonEmpty, countNonEmpty(board), "Поле не должно меняться");
        assertEquals(GameStatus.IN_PROCESS, session.getStatus());
    }

    @Test
    @DisplayName("После валидного хода появляется ровно одна новая плитка")
    void validMoveSpawnsExactlyOneTile() {
        Board board = new Board(4);
        board.setTile(0, 3, new Tile(2)); // плитка справа — при LEFT сдвинется влево

        GameSession session = new GameSession(board, 100, SEED);
        int beforeNonEmpty = countNonEmpty(board); // 1 плитка

        MoveResult result = service().applyMove(session, Motion.LEFT);

        assertTrue(result.moved());
        assertEquals(beforeNonEmpty + 1, countNonEmpty(board));
    }

    @Test
    @DisplayName("После N валидных ходов игра завершена")
    void gameEndsAfterMovesLimit() {
        Board board = new Board(4);
        board.setTile(0, 3, new Tile(2));

        GameSession session = new GameSession(board, 1, SEED);
        service().applyMove(session, Motion.LEFT);

        assertEquals(1, session.getMovesUsed());
        assertEquals(GameStatus.FINISHED, session.getStatus());
        assertTrue(session.isFinished());
    }

    @Test
    @DisplayName("Достижение плитки 32% не заканчивает игру — доигрываем до лимита ходов")
    void reachingMaxTileDoesNotEndGame() {
        Board board = new Board(4);
        board.setTile(0, 0, new Tile(16));
        board.setTile(0, 1, new Tile(16)); // при LEFT → 32

        GameSession session = new GameSession(board, 100, SEED);
        service().applyMove(session, Motion.LEFT);

        assertTrue(hasTileOfValue(board, 32), "На поле должна появиться плитка 32");
        assertEquals(GameStatus.IN_PROCESS, session.getStatus(), "32% не завершает игру");
    }

    @Test
    @DisplayName("Score считается по правилам и растёт только при слияниях")
    void scoreIsCalculatedOnMerges() {
        Board mergeBoard = new Board(4);
        mergeBoard.setTile(0, 0, new Tile(1));
        mergeBoard.setTile(0, 1, new Tile(1));
        GameSession mergeSession = new GameSession(mergeBoard, 100, SEED);
        service().applyMove(mergeSession, Motion.LEFT);
        assertEquals(2, mergeSession.getScore(), "За слияние 1+1 начисляется 2");

        Board shiftBoard = new Board(4);
        shiftBoard.setTile(0, 3, new Tile(2));
        GameSession shiftSession = new GameSession(shiftBoard, 100, SEED);
        service().applyMove(shiftSession, Motion.LEFT);
        assertEquals(0, shiftSession.getScore(), "Без слияний score не растёт");
    }

    private static int countNonEmpty(Board board) {
        int count = 0;
        for (int r = 0; r < board.getBoardSize(); r++) {
            for (int c = 0; c < board.getBoardSize(); c++) {
                if (board.getTile(r, c) != null) {
                    count++;
                }
            }
        }
        return count;
    }

    private static boolean hasTileOfValue(Board board, int value) {
        for (int r = 0; r < board.getBoardSize(); r++) {
            for (int c = 0; c < board.getBoardSize(); c++) {
                Tile tile = board.getTile(r, c);
                if (tile != null && tile.cashback() == value) {
                    return true;
                }
            }
        }
        return false;
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
