package dev.gaspard4i.numismatic.currency;

public final class CurrencyResolver {

    private CurrencyResolver() {}

    public static long[] splitValues(long rawValue) {
        if (rawValue < 0) {
            throw new IllegalArgumentException("rawValue must be non-negative, got " + rawValue);
        }
        long[] output = new long[Currency.values().length];

        long remaining = rawValue;
        long netherite = remaining / Currency.NETHERITE_VALUE;
        if (netherite > 0) {
            output[3] = netherite;
            remaining -= netherite * Currency.NETHERITE_VALUE;
        }
        long gold = remaining / Currency.GOLD_VALUE;
        if (gold > 0) {
            output[2] = gold;
            remaining -= gold * Currency.GOLD_VALUE;
        }
        long silver = remaining / Currency.SILVER_VALUE;
        if (silver > 0) {
            output[1] = silver;
            remaining -= silver * Currency.SILVER_VALUE;
        }
        if (remaining > 0) {
            output[0] = remaining;
        }
        return output;
    }

    public static long combineValues(long[] values) {
        if (values.length != Currency.values().length) {
            throw new IllegalArgumentException(
                    "values must have " + Currency.values().length + " elements, got " + values.length);
        }
        return Currency.BRONZE.getRawValue(values[0])
                + Currency.SILVER.getRawValue(values[1])
                + Currency.GOLD.getRawValue(values[2])
                + Currency.NETHERITE.getRawValue(values[3]);
    }

    public static long combineValues(long bronze, long silver, long gold, long netherite) {
        return Currency.BRONZE.getRawValue(bronze)
                + Currency.SILVER.getRawValue(silver)
                + Currency.GOLD.getRawValue(gold)
                + Currency.NETHERITE.getRawValue(netherite);
    }

    public static boolean canBeCompacted(long[] values) {
        return values[0] < 100 && values[1] < 100 && values[2] < 100;
    }
}
