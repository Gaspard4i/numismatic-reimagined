package dev.gaspard4i.numismatic.currency;

import java.util.EnumMap;
import java.util.Map;

/**
 * Resolves currency values into denomination breakdowns and vice versa.
 */
public final class CurrencyResolver {

    private CurrencyResolver() {}

    /**
     * Splits a total bronze value into the optimal combination of denominations.
     * Uses a greedy algorithm from highest to lowest denomination.
     *
     * @param totalBronzeValue the total value in bronze units
     * @return a map of each currency to its count
     * @throws IllegalArgumentException if totalBronzeValue is negative
     */
    public static Map<Currency, Long> splitValue(long totalBronzeValue) {
        if (totalBronzeValue < 0) {
            throw new IllegalArgumentException("Currency value cannot be negative: " + totalBronzeValue);
        }

        Map<Currency, Long> result = new EnumMap<>(Currency.class);
        long remaining = totalBronzeValue;

        for (Currency currency : Currency.valuesDescending()) {
            long count = remaining / currency.getValue();
            result.put(currency, count);
            remaining %= currency.getValue();
        }

        return result;
    }

    /**
     * Combines denomination counts into a total bronze value.
     *
     * @param counts a map of each currency to its count
     * @return the total value in bronze units
     * @throws IllegalArgumentException if any count is negative
     */
    public static long combineValues(Map<Currency, Long> counts) {
        long total = 0;
        for (Map.Entry<Currency, Long> entry : counts.entrySet()) {
            if (entry.getValue() < 0) {
                throw new IllegalArgumentException(
                        "Count cannot be negative for " + entry.getKey() + ": " + entry.getValue()
                );
            }
            total += entry.getKey().getValue() * entry.getValue();
        }
        return total;
    }

    /**
     * Combines individual denomination counts into a total bronze value.
     *
     * @param netherite count of netherite coins
     * @param gold count of gold coins
     * @param silver count of silver coins
     * @param bronze count of bronze coins
     * @return the total value in bronze units
     */
    public static long combineValues(long netherite, long gold, long silver, long bronze) {
        Map<Currency, Long> counts = new EnumMap<>(Currency.class);
        counts.put(Currency.NETHERITE, netherite);
        counts.put(Currency.GOLD, gold);
        counts.put(Currency.SILVER, silver);
        counts.put(Currency.BRONZE, bronze);
        return combineValues(counts);
    }

    /**
     * Formats a bronze value as a human-readable string.
     * Example: 1_020_304 -> "1N 2G 3S 4B"
     *
     * @param totalBronzeValue the value to format
     * @return a formatted string representation
     */
    public static String formatValue(long totalBronzeValue) {
        if (totalBronzeValue == 0) {
            return "0B";
        }

        Map<Currency, Long> split = splitValue(totalBronzeValue);
        StringBuilder sb = new StringBuilder();

        appendIfNonZero(sb, split.get(Currency.NETHERITE), "N");
        appendIfNonZero(sb, split.get(Currency.GOLD), "G");
        appendIfNonZero(sb, split.get(Currency.SILVER), "S");
        appendIfNonZero(sb, split.get(Currency.BRONZE), "B");

        return sb.toString().trim();
    }

    private static void appendIfNonZero(StringBuilder sb, long count, String suffix) {
        if (count > 0) {
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(count).append(suffix);
        }
    }
}
