package dev.gaspard4i.numismatic.currency;

/**
 * Represents the different currency denominations in the mod.
 * Each denomination has a fixed value in bronze units.
 *
 * <p>Conversion rates:
 * <ul>
 *   <li>100 Bronze = 1 Silver</li>
 *   <li>100 Silver = 1 Gold (10,000 Bronze)</li>
 *   <li>100 Gold = 1 Netherite (1,000,000 Bronze)</li>
 * </ul>
 */
public enum Currency {

    BRONZE(1, "bronze_coin"),
    SILVER(100, "silver_coin"),
    GOLD(10_000, "gold_coin"),
    NETHERITE(1_000_000, "netherite_coin");

    public static final int CONVERSION_RATE = 100;

    private final long value;
    private final String itemId;

    Currency(long value, String itemId) {
        this.value = value;
        this.itemId = itemId;
    }

    /**
     * @return the value of one unit of this currency in bronze
     */
    public long getValue() {
        return value;
    }

    /**
     * @return the item registry id (without namespace) for this currency
     */
    public String getItemId() {
        return itemId;
    }

    /**
     * Returns the currency with the highest denomination that is at or below the given value.
     *
     * @param bronzeValue the value in bronze to find a currency for
     * @return the highest denomination currency that fits, or BRONZE if value is 0 or less
     */
    public static Currency getHighestDenomination(long bronzeValue) {
        Currency[] values = values();
        for (int i = values.length - 1; i >= 0; i--) {
            if (bronzeValue >= values[i].value) {
                return values[i];
            }
        }
        return BRONZE;
    }

    /**
     * Get all currencies ordered from highest to lowest value.
     */
    public static Currency[] valuesDescending() {
        Currency[] values = values();
        Currency[] descending = new Currency[values.length];
        for (int i = 0; i < values.length; i++) {
            descending[i] = values[values.length - 1 - i];
        }
        return descending;
    }
}
