package dev.gaspard4i.numismatic.currency;

public enum Currency {

    BRONZE(0xae5b3c, 1L),
    SILVER(0x617174, 100L),
    GOLD(0xbd9838, 10_000L),
    NETHERITE(0x4a4a4a, 1_000_000L);

    public static final long BRONZE_VALUE = BRONZE.value;
    public static final long SILVER_VALUE = SILVER.value;
    public static final long GOLD_VALUE = GOLD.value;
    public static final long NETHERITE_VALUE = NETHERITE.value;

    private final int nameColor;
    private final long value;

    Currency(int nameColor, long value) {
        this.nameColor = nameColor;
        this.value = value;
    }

    public int getNameColor() {
        return nameColor;
    }

    public long getValue() {
        return value;
    }

    public long getRawValue(long amount) {
        return amount * value;
    }
}
