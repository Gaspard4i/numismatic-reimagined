package dev.gaspard4i.numismatic.client;

import dev.gaspard4i.numismatic.currency.Currency;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PurseExtractLogicTest {

    private static long[] empty() {
        return new long[Currency.values().length];
    }

    @Test
    void availableForEmptyDesired() {
        long[] d = empty();
        assertEquals(100, PurseExtractLogic.availableFor(Currency.BRONZE, d, 100));
        assertEquals(1, PurseExtractLogic.availableFor(Currency.SILVER, d, 100));
        assertEquals(0, PurseExtractLogic.availableFor(Currency.GOLD, d, 100));
    }

    @Test
    void availableForAccountsForOtherDenominations() {
        long[] d = empty();
        d[Currency.SILVER.ordinal()] = 1; // already using 100 bronze
        assertEquals(0, PurseExtractLogic.availableFor(Currency.BRONZE, d, 100));
        assertEquals(1, PurseExtractLogic.availableFor(Currency.SILVER, d, 100));
    }

    @Test
    void clampFloorsNegative() {
        long[] d = empty();
        d[Currency.BRONZE.ordinal()] = -5;
        PurseExtractLogic.clamp(d, 100);
        assertEquals(0, d[Currency.BRONZE.ordinal()]);
    }

    @Test
    void clampCapsAtBalance() {
        long[] d = empty();
        d[Currency.BRONZE.ordinal()] = 999;
        PurseExtractLogic.clamp(d, 100);
        assertEquals(100, d[Currency.BRONZE.ordinal()]);
    }

    @Test
    void incrementRespectsBalance() {
        long[] d = empty();
        PurseExtractLogic.increment(d, Currency.SILVER, 200, false);
        assertEquals(1, d[Currency.SILVER.ordinal()]);
        // Only 100 bronze left for bronze slot after 1 silver
        PurseExtractLogic.increment(d, Currency.BRONZE, 200, false);
        assertEquals(1, d[Currency.BRONZE.ordinal()]);
    }

    @Test
    void incrementShiftUsesMultiplier() {
        long[] d = empty();
        PurseExtractLogic.increment(d, Currency.BRONZE, 100, true);
        assertEquals(PurseExtractLogic.SHIFT_MULTIPLIER, d[Currency.BRONZE.ordinal()]);
    }

    @Test
    void decrementStopsAtZero() {
        long[] d = empty();
        d[Currency.BRONZE.ordinal()] = 5;
        PurseExtractLogic.decrement(d, Currency.BRONZE, false);
        assertEquals(4, d[Currency.BRONZE.ordinal()]);
        PurseExtractLogic.decrement(d, Currency.BRONZE, true);
        assertEquals(0, d[Currency.BRONZE.ordinal()]);
    }

    @Test
    void totalPendingComputesBronzeSum() {
        long[] d = empty();
        d[Currency.BRONZE.ordinal()] = 25;
        d[Currency.SILVER.ordinal()] = 2;
        d[Currency.GOLD.ordinal()] = 1;
        assertEquals(25 + 200 + 10_000, PurseExtractLogic.totalPending(d));
    }

    @Test
    void incrementCannotExceedBalanceEvenWithShift() {
        long[] d = empty();
        PurseExtractLogic.increment(d, Currency.BRONZE, 5, true);
        assertEquals(5, d[Currency.BRONZE.ordinal()]);
    }

    @Test
    void clampRespectsMultipleDenominations() {
        long[] d = empty();
        // 5 gold = 50_000 ; balance 60_000 → bronze allowed = 10_000
        d[Currency.GOLD.ordinal()] = 5;
        d[Currency.BRONZE.ordinal()] = 20_000;
        PurseExtractLogic.clamp(d, 60_000);
        assertEquals(5, d[Currency.GOLD.ordinal()]);
        assertEquals(10_000, d[Currency.BRONZE.ordinal()]);
    }

    @Test
    void rejectsMalformedInput() {
        assertThrows(IllegalArgumentException.class,
                () -> PurseExtractLogic.clamp(new long[1], 100));
    }
}
