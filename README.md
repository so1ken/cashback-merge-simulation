## MR1 — Модель, конфиг, начальное состояние

### Реализовано
- `Board` — поле 4x4, `Tile[][]` (пустые клетки — `null`).
- `Tile` — record с `int cashback`, валидация `> 0`.
- `GameStatus` — enum: `IN_PROCESS`, `FINISHED`.
- `GameSession` — состояние партии: board, movesLimit, movesUsed, status.
- `CampaignConfig` — конфиг с валидацией (boardSize > 0, movesLimit > 0, spawnRules не пусто, сумма вероятностей = 1 через `BigDecimal.compareTo`, maxRewardCashback в rewardTable).
- `ConfigReader` — чтение JSON через Jackson с DTO-вложенными классами. Вероятности — `BigDecimal`.
- `GameService.startGame(config, seed)` — создаёт поле и расставляет 2 стартовые плитки weighted random.
- `ConsoleUI.printBoard` — текстовый вывод поля.

### Запуск
- Игра: `./gradlew run`
- Тесты: `./gradlew check`

### Принятые решения
- **`BigDecimal` для вероятностей** — ТЗ запрещает `double`. Сумма вероятностей проверяется точно: `compareTo(BigDecimal.ONE) != 0`.
- **DTO внутри `ConfigReader`** — Jackson парсит JSON в `ConfigDto` с String-полями, потом конвертация в домен.
- **`LinkedHashMap`** для spawn-правил — сохраняет порядок вставки, чтобы seed был воспроизводим.

## MR2 — Движок ходов

### Реализовано
- `Motion` — enum: UP, DOWN, LEFT, RIGHT.
- `MoveResult` — record с `moved` и `scoreGained`, валидация инвариантов.
- `GameService.applyMove(session, motion)` — сдвиг и слияние по правилам тз.
- Обработка всех четырёх направлений через `extractLine`/`setLine`/`reversed`.
- Score начисляется только за слияния (значение результата).
- После валидного хода спавнится ровно одна новая плитка.
- `GameSession.addScore`, `recordMove`, `getScore`, `getRandom`.

### Принятые решения
- **Одна плитка не сливается дважды:** после слияния индекс `i += 2` — пропускаем оба «родителя», новая плитка не участвует в следующем сравнении.
- **Унификация направлений:** RIGHT/DOWN — реверс линии, обработка как LEFT/UP, реверс обратно. Один `processLine` на все направления.
- **`Random` в `GameSession`:** seed один на партию, включая spawn после ходов.
- **Валидность хода:** `processLine` возвращает `changed` — сравнивает линию до/после. Если ни одна линия не изменилась — ход невалиден.
