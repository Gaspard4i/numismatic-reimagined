package dev.gaspard4i.numismatic.client;

import dev.gaspard4i.numismatic.currency.Currency;
import dev.gaspard4i.numismatic.currency.CurrencyResolver;

/**
 * Pure logic driving the purse popup: how many coins of each denomination the
 * user has queued for extraction.
 */
public final class PurseExtractLogic {

    private PurseExtractLogic() {}

    public static final int SHIFT_STEP = 10;

    public static void increment(long[] pending, Currency currency, long balance, boolean shift) {
        if (pending.length != Currency.values().length) {
            throw new IllegalArgumentException("pending must match Currency.values().length");
        }
        if (balance < 0) throw new IllegalArgumentException("balance must be non-negative");

        long[] owned = CurrencyResolver.splitValues(balance);
        int idx = currency.ordinal();
        long cap = owned[idx];
        long step = shift ? SHIFT_STEP : 1;
        long next = Math.min(cap, pending[idx] + step);
        pending[idx] = next;
    }

    public static void decrement(long[] pending, Currency currency, boolean shift) {
        if (pending.length != Currency.values().length) {
            throw new IllegalArgumentException("pending must match Currency.values().length");
        }
        int idx = currency.ordinal();
        long step = shift ? SHIFT_STEP : 1;
        pending[idx] = Math.max(0, pending[idx] - step);
    }

    public static long totalPending(long[] pending) {
        long total = 0;
        for (int i = 0; i < pending.length; i++) {
            total += Currency.values()[i].getRawValue(pending[i]);
        }
        return total;
    }

    public static void clear(long[] pending) {
        for (int i = 0; i < pending.length; i++) pending[i] = 0;
    }
}
