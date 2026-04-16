package dev.gaspard4i.numismatic;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NumismaticConstantsTest {

    @Test
    void modIdIsCorrect() {
        assertEquals("numismatic_reimagined", NumismaticConstants.MOD_ID);
    }

    @Test
    void modNameIsCorrect() {
        assertEquals("Numismatic Reimagined", NumismaticConstants.MOD_NAME);
    }

    @Test
    void maxCurrencyIsPositive() {
        assertTrue(NumismaticConstants.MAX_CURRENCY > 0);
    }

    @Test
    void maxCurrencyIsHalfMaxLong() {
        assertEquals(Long.MAX_VALUE / 2, NumismaticConstants.MAX_CURRENCY);
    }

    @Test
    void maxCurrencyDoesNotOverflowWhenDoubled() {
        // MAX_CURRENCY * 2 should not overflow long
        assertTrue(NumismaticConstants.MAX_CURRENCY * 2 > 0);
    }
}
