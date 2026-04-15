package dev.gaspard4i.numismatic.currency;

/**
 * Utility class for converting between currency denominations.
 */
public final class CurrencyConverter {

    private CurrencyConverter() {}

    /**
     * Converts a count of one currency denomination to its equivalent value in another.
     *
     * @param count the number of coins
     * @param from the source denomination
     * @param to the target denomination
     * @return the equivalent count in the target denomination (truncated, not rounded)
     * @throws IllegalArgumentException if count is negative
     */
    public static long convert(long count, Currency from, Currency to) {
        if (count < 0) {
            throw new IllegalArgumentException("Count cannot be negative: " + count);
        }
        if (from == to) {
            return count;
        }
        long bronzeValue = count * from.getValue();
        return bronzeValue / to.getValue();
    }

    /**
     * Returns the remainder after converting from one denomination to another.
     * The remainder is expressed in bronze units.
     *
     * @param count the number of coins
     * @param from the source denomination
     * @param to the target denomination
     * @return the remainder in bronze after conversion
     */
    public static long convertRemainder(long count, Currency from, Currency to) {
        if (count < 0) {
            throw new IllegalArgumentException("Count cannot be negative: " + count);
        }
        if (from == to) {
            return 0;
        }
        long bronzeValue = count * from.getValue();
        return bronzeValue % to.getValue();
    }

    /**
     * Calculates the total bronze value for a given count of a denomination.
     *
     * @param count the number of coins
     * @param currency the denomination
     * @return the total value in bronze
     */
    public static long toBronze(long count, Currency currency) {
        if (count < 0) {
            throw new IllegalArgumentException("Count cannot be negative: " + count);
        }
        return count * currency.getValue();
    }

    /**
     * Checks if one denomination can be cleanly exchanged into another
     * (i.e., no remainder).
     *
     * @param count the number of coins
     * @param from the source denomination
     * @param to the target denomination
     * @return true if the conversion is exact
     */
    public static boolean canConvertExactly(long count, Currency from, Currency to) {
        return convertRemainder(count, from, to) == 0;
    }
}
