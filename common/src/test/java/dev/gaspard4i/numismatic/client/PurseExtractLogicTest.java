package dev.gaspard4i.numismatic.client;

import dev.gaspard4i.numismatic.currency.Currency;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PurseExtractLogicTest {

    private long[] pending() {
        return new long[Currency.values().length];
    }

    @Test
    void incrementAddsOne() {
        long[] p = pending();
        // 50 000 = 5 GOLD (50_000 / 10_000), SILVER=0, BRONZE=0, NETHERITE=0
        PurseExtractLogic.increment(p, Currency.GOLD, 50_000L, false);
        assertEquals(1L, p[Currency.GOLD.ordinal()]);
    }

    @Test
    void incrementWithShiftAddsTen() {
        long[] p = pending();
        // 99 999 -> BRONZE=99, SILVER=99, GOLD=9
        PurseExtractLogic.increment(p, Currency.BRONZE, 99_999L, true);
        assertEquals(10L, p[Currency.BRONZE.ordinal()]);
    }

    @Test
    void incrementClampsAtOwnedAmount() {
        long[] p = pending();
        PurseExtractLogic.increment(p, Currency.BRONZE, 3L, true);
        assertEquals(3L, p[Currency.BRONZE.ordinal()]);
    }

    @Test
    void incrementRejectsNegativeBalance() {
        long[] p = pending();
        assertThrows(IllegalArgumentException.class,
                () -> PurseExtractLogic.increment(p, Currency.GOLD, -1L, false));
    }

    @Test
    void incrementRejectsMalformedPending() {
        assertThrows(IllegalArgumentException.class,
                () -> PurseExtractLogic.increment(new long[]{0, 0}, Currency.GOLD, 100L, false));
    }

    @Test
    void decrementReducesByOne() {
        long[] p = pending();
        p[Currency.SILVER.ordinal()] = 5;
        PurseExtractLogic.decrement(p, Currency.SILVER, false);
        assertEquals(4L, p[Currency.SILVER.ordinal()]);
    }

    @Test
    void decrementWithShiftReducesByTen() {
        long[] p = pending();
        p[Currency.GOLD.ordinal()] = 50;
        PurseExtractLogic.decrement(p, Currency.GOLD, true);
        assertEquals(40L, p[Currency.GOLD.ordinal()]);
    }

    @Test
    void decrementNeverGoesBelowZero() {
        long[] p = pending();
        p[Currency.BRONZE.ordinal()] = 5;
        PurseExtractLogic.decrement(p, Currency.BRONZE, true);
        assertEquals(0L, p[Currency.BRONZE.ordinal()]);
    }

    @Test
    void decrementRejectsMalformedPending() {
        assertThrows(IllegalArgumentException.class,
                () -> PurseExtractLogic.decrement(new long[]{0, 0}, Currency.GOLD, false));
    }

    @Test
    void totalPendingMultipliesByDenomination() {
        long[] p = pending();
        p[Currency.BRONZE.ordinal()] = 5;
        p[Currency.SILVER.ordinal()] = 3;
        p[Currency.GOLD.ordinal()] = 2;
        p[Currency.NETHERITE.ordinal()] = 1;
        long expected = 5 + 300 + 20_000 + 1_000_000;
        assertEquals(expected, PurseExtractLogic.totalPending(p));
    }

    @Test
    void totalPendingZeroOnEmpty() {
        assertEquals(0L, PurseExtractLogic.totalPending(pending()));
    }

    @Test
    void clearResetsAll() {
        long[] p = pending();
        p[0] = 5;
        p[3] = 10;
        PurseExtractLogic.clear(p);
        assertArrayEquals(new long[]{0, 0, 0, 0}, p);
    }
}
