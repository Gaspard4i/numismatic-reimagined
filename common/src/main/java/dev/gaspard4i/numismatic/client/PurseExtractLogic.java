package dev.gaspard4i.numismatic.client;

import dev.gaspard4i.numismatic.currency.Currency;

/**
 * Pure helpers for the purse popup : per-denomination "withdraw amount"
 * accumulator with clamping against the player's current balance.
 */
public final class PurseExtractLogic {

    private PurseExtractLogic() {}

    /** Shift + click multiplier applied to increment / decrement steps. */
    public static final int SHIFT_MULTIPLIER = 10;

    /**
     * Clamps {@code desired[currency.ordinal()]} to {@code [0, maxFor(currency)]},
     * where the max is derived from the remaining balance AFTER the other
     * denominations have been accounted for. Mutates {@code desired} in
     * place and returns it.
     */
    public static long[] clamp(long[] desired, long totalBalance) {
        if (desired.length != Currency.values().length)
            throw new IllegalArgumentException("desired must have one slot per denomination");
        for (Currency c : Currency.values()) {
            int i = c.ordinal();
            long max = availableFor(c, desired, totalBalance);
            if (desired[i] < 0) desired[i] = 0;
            if (desired[i] > max) desired[i] = max;
        }
        return desired;
    }

    /**
     * Returns how many coins of {@code currency} the player can still take
     * given the already-pending amounts in {@code desired} and the live
     * {@code totalBalance}.
     */
    public static long availableFor(Currency currency, long[] desired, long totalBalance) {
        long usedByOthers = 0;
        for (Currency c : Currency.values()) {
            if (c == currency) continue;
            usedByOthers += Math.max(0, desired[c.ordinal()]) * c.getValue();
        }
        long remaining = Math.max(0, totalBalance - usedByOthers);
        return remaining / Math.max(1, currency.getValue());
    }

    /**
     * Increments the currency's desired count by one "step" (or by
     * {@code SHIFT_MULTIPLIER} when {@code shift} is true). The result is
     * clamped.
     */
    public static void increment(long[] desired, Currency currency, long totalBalance, boolean shift) {
        long step = shift ? SHIFT_MULTIPLIER : 1;
        desired[currency.ordinal()] += step;
        clamp(desired, totalBalance);
    }

    /** Decrement by one step, floor at zero. */
    public static void decrement(long[] desired, Currency currency, boolean shift) {
        long step = shift ? SHIFT_MULTIPLIER : 1;
        int i = currency.ordinal();
        desired[i] -= step;
        if (desired[i] < 0) desired[i] = 0;
    }

    /** Total bronze value that would be withdrawn from {@code desired}. */
    public static long totalPending(long[] desired) {
        long sum = 0;
        for (Currency c : Currency.values()) {
            sum += Math.max(0, desired[c.ordinal()]) * c.getValue();
        }
        return sum;
    }
}
