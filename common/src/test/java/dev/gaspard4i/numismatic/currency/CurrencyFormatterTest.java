package dev.gaspard4i.numismatic.currency;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CurrencyFormatterTest {

    @Test
    void symbolIsCent() {
        assertEquals("¢", CurrencyFormatter.symbol());
        assertEquals("¢", CurrencyFormatter.SYMBOL);
    }

    @Test
    void formatSimpleZero() {
        assertEquals("0¢", CurrencyFormatter.formatSimple(0));
    }

    @Test
    void formatSimpleLargeValue() {
        assertEquals("1234567¢", CurrencyFormatter.formatSimple(1_234_567L));
    }

    @Test
    void formatSimpleRejectsNegative() {
        assertThrows(IllegalArgumentException.class, () -> CurrencyFormatter.formatSimple(-1));
    }

    @Test
    void formatZero() {
        assertEquals("0¢", CurrencyFormatter.format(0));
    }

    @Test
    void formatPureBronze() {
        assertEquals("50b¢", CurrencyFormatter.format(50));
    }

    @Test
    void formatPureSilver() {
        assertEquals("3s¢", CurrencyFormatter.format(300));
    }

    @Test
    void formatPureGold() {
        assertEquals("2g¢", CurrencyFormatter.format(20_000));
    }

    @Test
    void formatPureNetherite() {
        assertEquals("5n¢", CurrencyFormatter.format(5_000_000L));
    }

    @Test
    void formatMixedDenoms() {
        // 1n = 1 000 000, 2g = 20 000, 3s = 300, 4b = 4 → 1 020 304
        assertEquals("1n2g3s4b¢", CurrencyFormatter.format(1_020_304L));
    }

    @Test
    void formatSkipsZeroParts() {
        // 1000 = 10 silver exactly → pas de bronze
        assertEquals("10s¢", CurrencyFormatter.format(1000));
    }

    @Test
    void formatRejectsNegative() {
        assertThrows(IllegalArgumentException.class, () -> CurrencyFormatter.format(-1));
    }
}
