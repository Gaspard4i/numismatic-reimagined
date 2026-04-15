package dev.gaspard4i.numismatic.currency;

import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CurrencyResolverTest {

    // --- splitValue tests ---

    @Test
    void splitValueZeroReturnsAllZeros() {
        Map<Currency, Long> result = CurrencyResolver.splitValue(0);
        assertEquals(0L, result.get(Currency.NETHERITE));
        assertEquals(0L, result.get(Currency.GOLD));
        assertEquals(0L, result.get(Currency.SILVER));
        assertEquals(0L, result.get(Currency.BRONZE));
    }

    @Test
    void splitValueOneBronze() {
        Map<Currency, Long> result = CurrencyResolver.splitValue(1);
        assertEquals(0L, result.get(Currency.NETHERITE));
        assertEquals(0L, result.get(Currency.GOLD));
        assertEquals(0L, result.get(Currency.SILVER));
        assertEquals(1L, result.get(Currency.BRONZE));
    }

    @Test
    void splitValueExactlyOneSilver() {
        Map<Currency, Long> result = CurrencyResolver.splitValue(100);
        assertEquals(0L, result.get(Currency.NETHERITE));
        assertEquals(0L, result.get(Currency.GOLD));
        assertEquals(1L, result.get(Currency.SILVER));
        assertEquals(0L, result.get(Currency.BRONZE));
    }

    @Test
    void splitValueExactlyOneGold() {
        Map<Currency, Long> result = CurrencyResolver.splitValue(10_000);
        assertEquals(0L, result.get(Currency.NETHERITE));
        assertEquals(1L, result.get(Currency.GOLD));
        assertEquals(0L, result.get(Currency.SILVER));
        assertEquals(0L, result.get(Currency.BRONZE));
    }

    @Test
    void splitValueExactlyOneNetherite() {
        Map<Currency, Long> result = CurrencyResolver.splitValue(1_000_000);
        assertEquals(1L, result.get(Currency.NETHERITE));
        assertEquals(0L, result.get(Currency.GOLD));
        assertEquals(0L, result.get(Currency.SILVER));
        assertEquals(0L, result.get(Currency.BRONZE));
    }

    @Test
    void splitValueMixedDenominations() {
        // 1N + 2G + 3S + 4B = 1_000_000 + 20_000 + 300 + 4 = 1_020_304
        Map<Currency, Long> result = CurrencyResolver.splitValue(1_020_304);
        assertEquals(1L, result.get(Currency.NETHERITE));
        assertEquals(2L, result.get(Currency.GOLD));
        assertEquals(3L, result.get(Currency.SILVER));
        assertEquals(4L, result.get(Currency.BRONZE));
    }

    @Test
    void splitValueLargeAmount() {
        // 999N + 99G + 99S + 99B
        long value = 999L * 1_000_000 + 99L * 10_000 + 99L * 100 + 99;
        Map<Currency, Long> result = CurrencyResolver.splitValue(value);
        assertEquals(999L, result.get(Currency.NETHERITE));
        assertEquals(99L, result.get(Currency.GOLD));
        assertEquals(99L, result.get(Currency.SILVER));
        assertEquals(99L, result.get(Currency.BRONZE));
    }

    @Test
    void splitValueThrowsOnNegative() {
        assertThrows(IllegalArgumentException.class, () -> CurrencyResolver.splitValue(-1));
    }

    @Test
    void splitValue99Bronze() {
        Map<Currency, Long> result = CurrencyResolver.splitValue(99);
        assertEquals(0L, result.get(Currency.NETHERITE));
        assertEquals(0L, result.get(Currency.GOLD));
        assertEquals(0L, result.get(Currency.SILVER));
        assertEquals(99L, result.get(Currency.BRONZE));
    }

    // --- combineValues tests ---

    @Test
    void combineValuesAllZeros() {
        assertEquals(0, CurrencyResolver.combineValues(0, 0, 0, 0));
    }

    @Test
    void combineValuesOneBronze() {
        assertEquals(1, CurrencyResolver.combineValues(0, 0, 0, 1));
    }

    @Test
    void combineValuesOneSilver() {
        assertEquals(100, CurrencyResolver.combineValues(0, 0, 1, 0));
    }

    @Test
    void combineValuesOneGold() {
        assertEquals(10_000, CurrencyResolver.combineValues(0, 1, 0, 0));
    }

    @Test
    void combineValuesOneNetherite() {
        assertEquals(1_000_000, CurrencyResolver.combineValues(1, 0, 0, 0));
    }

    @Test
    void combineValuesMixed() {
        assertEquals(1_020_304, CurrencyResolver.combineValues(1, 2, 3, 4));
    }

    @Test
    void combineValuesMapVersion() {
        Map<Currency, Long> counts = new EnumMap<>(Currency.class);
        counts.put(Currency.NETHERITE, 1L);
        counts.put(Currency.GOLD, 2L);
        counts.put(Currency.SILVER, 3L);
        counts.put(Currency.BRONZE, 4L);
        assertEquals(1_020_304, CurrencyResolver.combineValues(counts));
    }

    @Test
    void combineValuesThrowsOnNegativeCount() {
        Map<Currency, Long> counts = new EnumMap<>(Currency.class);
        counts.put(Currency.BRONZE, -1L);
        assertThrows(IllegalArgumentException.class, () -> CurrencyResolver.combineValues(counts));
    }

    // --- splitValue and combineValues are inverse ---

    @Test
    void splitAndCombineAreInverseSmall() {
        long original = 42;
        assertEquals(original, CurrencyResolver.combineValues(CurrencyResolver.splitValue(original)));
    }

    @Test
    void splitAndCombineAreInverseLarge() {
        long original = 123_456_789;
        assertEquals(original, CurrencyResolver.combineValues(CurrencyResolver.splitValue(original)));
    }

    @Test
    void splitAndCombineAreInverseZero() {
        assertEquals(0, CurrencyResolver.combineValues(CurrencyResolver.splitValue(0)));
    }

    // --- formatValue tests ---

    @Test
    void formatValueZero() {
        assertEquals("0B", CurrencyResolver.formatValue(0));
    }

    @Test
    void formatValueOneBronze() {
        assertEquals("1B", CurrencyResolver.formatValue(1));
    }

    @Test
    void formatValueOneSilver() {
        assertEquals("1S", CurrencyResolver.formatValue(100));
    }

    @Test
    void formatValueOneGold() {
        assertEquals("1G", CurrencyResolver.formatValue(10_000));
    }

    @Test
    void formatValueOneNetherite() {
        assertEquals("1N", CurrencyResolver.formatValue(1_000_000));
    }

    @Test
    void formatValueMixed() {
        assertEquals("1N 2G 3S 4B", CurrencyResolver.formatValue(1_020_304));
    }

    @Test
    void formatValueOnlySilverAndBronze() {
        assertEquals("5S 50B", CurrencyResolver.formatValue(550));
    }

    @Test
    void formatValueOnlyGold() {
        assertEquals("50G", CurrencyResolver.formatValue(500_000));
    }
}
