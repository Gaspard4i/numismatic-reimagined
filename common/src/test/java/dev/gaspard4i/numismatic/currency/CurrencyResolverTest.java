package dev.gaspard4i.numismatic.currency;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CurrencyResolverTest {

    @Test
    void splitOfZeroIsAllZero() {
        assertArrayEquals(new long[]{0, 0, 0, 0}, CurrencyResolver.splitValues(0));
    }

    @Test
    void splitMixedDenominations() {
        long raw = 1_234_567L;
        long[] split = CurrencyResolver.splitValues(raw);
        assertEquals(67L, split[0]);
        assertEquals(45L, split[1]);
        assertEquals(23L, split[2]);
        assertEquals(1L, split[3]);
    }

    @Test
    void splitPureBronze() {
        long[] split = CurrencyResolver.splitValues(99L);
        assertArrayEquals(new long[]{99, 0, 0, 0}, split);
    }

    @Test
    void splitExactNetherite() {
        long[] split = CurrencyResolver.splitValues(2_000_000L);
        assertArrayEquals(new long[]{0, 0, 0, 2}, split);
    }

    @Test
    void splitNegativeRejected() {
        assertThrows(IllegalArgumentException.class, () -> CurrencyResolver.splitValues(-1));
    }

    @Test
    void combineRoundTripPreservesValue() {
        long original = 9_876_543L;
        long[] split = CurrencyResolver.splitValues(original);
        assertEquals(original, CurrencyResolver.combineValues(split));
    }

    @Test
    void combineWithExplicitDenominations() {
        long expected = 1L + 2L * 100L + 3L * 10_000L + 4L * 1_000_000L;
        assertEquals(expected, CurrencyResolver.combineValues(1, 2, 3, 4));
    }

    @Test
    void combineRejectsWrongLength() {
        assertThrows(IllegalArgumentException.class, () -> CurrencyResolver.combineValues(new long[]{1, 2, 3}));
        assertThrows(IllegalArgumentException.class, () -> CurrencyResolver.combineValues(new long[]{1, 2, 3, 4, 5}));
    }

    @Test
    void canBeCompactedReturnsTrueWhenAllUnder100() {
        assertTrue(CurrencyResolver.canBeCompacted(new long[]{99, 99, 99, 0}));
    }

    @Test
    void canBeCompactedReturnsFalseWhenAnyOverflow() {
        assertFalse(CurrencyResolver.canBeCompacted(new long[]{100, 0, 0, 0}));
        assertFalse(CurrencyResolver.canBeCompacted(new long[]{0, 100, 0, 0}));
        assertFalse(CurrencyResolver.canBeCompacted(new long[]{0, 0, 100, 0}));
    }
}
