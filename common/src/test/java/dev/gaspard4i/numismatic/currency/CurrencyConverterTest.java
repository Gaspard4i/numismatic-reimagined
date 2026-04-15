package dev.gaspard4i.numismatic.currency;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

class CurrencyConverterTest {

    // --- convert tests ---

    @ParameterizedTest
    @EnumSource(Currency.class)
    void convertSameTypeReturnsSameCount(Currency currency) {
        assertEquals(42, CurrencyConverter.convert(42, currency, currency));
    }

    @Test
    void convertBronzeToSilver() {
        assertEquals(1, CurrencyConverter.convert(100, Currency.BRONZE, Currency.SILVER));
    }

    @Test
    void convertBronzeToSilverTruncates() {
        assertEquals(0, CurrencyConverter.convert(99, Currency.BRONZE, Currency.SILVER));
    }

    @Test
    void convertSilverToBronze() {
        assertEquals(100, CurrencyConverter.convert(1, Currency.SILVER, Currency.BRONZE));
    }

    @Test
    void convertBronzeToGold() {
        assertEquals(1, CurrencyConverter.convert(10_000, Currency.BRONZE, Currency.GOLD));
    }

    @Test
    void convertGoldToBronze() {
        assertEquals(10_000, CurrencyConverter.convert(1, Currency.GOLD, Currency.BRONZE));
    }

    @Test
    void convertBronzeToNetherite() {
        assertEquals(1, CurrencyConverter.convert(1_000_000, Currency.BRONZE, Currency.NETHERITE));
    }

    @Test
    void convertNetheriteToBronze() {
        assertEquals(1_000_000, CurrencyConverter.convert(1, Currency.NETHERITE, Currency.BRONZE));
    }

    @Test
    void convertSilverToGold() {
        assertEquals(1, CurrencyConverter.convert(100, Currency.SILVER, Currency.GOLD));
    }

    @Test
    void convertGoldToNetherite() {
        assertEquals(1, CurrencyConverter.convert(100, Currency.GOLD, Currency.NETHERITE));
    }

    @Test
    void convertZeroReturnsZero() {
        assertEquals(0, CurrencyConverter.convert(0, Currency.BRONZE, Currency.NETHERITE));
    }

    @Test
    void convertNegativeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> CurrencyConverter.convert(-1, Currency.BRONZE, Currency.SILVER));
    }

    // --- convertRemainder tests ---

    @ParameterizedTest
    @EnumSource(Currency.class)
    void convertRemainderSameTypeIsZero(Currency currency) {
        assertEquals(0, CurrencyConverter.convertRemainder(42, currency, currency));
    }

    @Test
    void convertRemainderBronzeToSilverExact() {
        assertEquals(0, CurrencyConverter.convertRemainder(100, Currency.BRONZE, Currency.SILVER));
    }

    @Test
    void convertRemainderBronzeToSilverWithRemainder() {
        assertEquals(50, CurrencyConverter.convertRemainder(150, Currency.BRONZE, Currency.SILVER));
    }

    @Test
    void convertRemainderBronzeToGoldWithRemainder() {
        assertEquals(5_555, CurrencyConverter.convertRemainder(15_555, Currency.BRONZE, Currency.GOLD));
    }

    @Test
    void convertRemainderNegativeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> CurrencyConverter.convertRemainder(-1, Currency.BRONZE, Currency.SILVER));
    }

    // --- toBronze tests ---

    @Test
    void toBronzeBronze() {
        assertEquals(42, CurrencyConverter.toBronze(42, Currency.BRONZE));
    }

    @Test
    void toBronzeSilver() {
        assertEquals(4200, CurrencyConverter.toBronze(42, Currency.SILVER));
    }

    @Test
    void toBronzeGold() {
        assertEquals(420_000, CurrencyConverter.toBronze(42, Currency.GOLD));
    }

    @Test
    void toBronzeNetherite() {
        assertEquals(42_000_000, CurrencyConverter.toBronze(42, Currency.NETHERITE));
    }

    @Test
    void toBronzeZero() {
        assertEquals(0, CurrencyConverter.toBronze(0, Currency.NETHERITE));
    }

    @Test
    void toBronzeNegativeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> CurrencyConverter.toBronze(-1, Currency.BRONZE));
    }

    // --- canConvertExactly tests ---

    @Test
    void canConvertExactlyTrue() {
        assertTrue(CurrencyConverter.canConvertExactly(100, Currency.BRONZE, Currency.SILVER));
    }

    @Test
    void canConvertExactlyFalse() {
        assertFalse(CurrencyConverter.canConvertExactly(99, Currency.BRONZE, Currency.SILVER));
    }

    @ParameterizedTest
    @EnumSource(Currency.class)
    void canConvertExactlySameTypeAlwaysTrue(Currency currency) {
        assertTrue(CurrencyConverter.canConvertExactly(42, currency, currency));
    }

    @Test
    void canConvertExactlyZeroAlwaysTrue() {
        assertTrue(CurrencyConverter.canConvertExactly(0, Currency.BRONZE, Currency.NETHERITE));
    }
}
