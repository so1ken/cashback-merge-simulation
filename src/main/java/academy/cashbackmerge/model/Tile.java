package academy.cashbackmerge.model;

// Плитка — иммутабельный носитель числа. Одна плитка = одно значение => делаем record
public record Tile(int cashback) {
    public Tile {
        if (cashback <= 0) {
            throw new IllegalArgumentException("Cashback должен быть > 0");
        }
    }
}
