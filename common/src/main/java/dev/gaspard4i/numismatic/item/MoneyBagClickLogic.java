package dev.gaspard4i.numismatic.item;

import dev.gaspard4i.numismatic.currency.Currency;

/**
 * Pure helpers for the left/right click interactions between a money bag
 * and coin/bag stacks in an inventory slot. Encapsulates the value math
 * (absorb coin, absorb bag, extract largest denomination as a coin stack)
 * so it can be unit-tested without any MC runtime.
 */
public final class MoneyBagClickLogic {

    private MoneyBagClickLogic() {}

    /** Result of {@link #extractLargestDenom}. */
    public record ExtractResult(Currency currency, int count, long remainingBagValue) {
        public static final ExtractResult NONE = new ExtractResult(Currency.BRONZE, 0, 0L);
        public boolean isEmpty() { return count == 0; }
    }

    /** Value produced by absorbing {@code coinCount} coins of {@code currency}
     *  into a bag currently worth {@code bagValue}. Saturates on overflow. */
    public static long absorbCoins(long bagValue, Currency currency, int coinCount) {
        if (coinCount <= 0) return bagValue;
        long added = (long) coinCount * currency.getValue();
        long sum = bagValue + added;
        if (sum < bagValue) return Long.MAX_VALUE; // overflow guard
        return sum;
    }

    /** Value produced by absorbing another bag into a bag currently worth
     *  {@code bagValueA}. Saturates on overflow. */
    public static long absorbBag(long bagValueA, long bagValueB) {
        long sum = bagValueA + bagValueB;
        if (sum < bagValueA || sum < bagValueB) return Long.MAX_VALUE;
        return sum;
    }

    /**
     * Extracts up to one stack (max 64) of the largest denomination that
     * fits in {@code bagValue}. Priority: NETHERITE > GOLD > SILVER >
     * BRONZE. Returns {@link ExtractResult#NONE} when the bag is empty
     * (or negative).
     */
    public static ExtractResult extractLargestDenom(long bagValue) {
        if (bagValue <= 0) return ExtractResult.NONE;
        for (Currency c : Currency.valuesDescending()) {
            long unit = c.getValue();
            if (unit <= 0) continue;
            long maxCount = bagValue / unit;
            if (maxCount <= 0) continue;
            int count = (int) Math.min(64L, maxCount);
            long remaining = bagValue - (long) count * unit;
            return new ExtractResult(c, count, remaining);
        }
        return ExtractResult.NONE;
    }
}
