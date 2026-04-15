package dev.gaspard4i.numismatic.currency;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

class CurrencyTest {

    @Test
    void bronzeHasValueOne() {
        assertEquals(1, Currency.BRONZE.getValue());
    }

    @Test
    void silverHasValueOneHundred() {
        assertEquals(100, Currency.SILVER.getValue());
    }

    @Test
    void goldHasValueTenThousand() {
        assertEquals(10_000, Currency.GOLD.getValue());
    }

    @Test
    void netheriteHasValueOneMillion() {
        assertEquals(1_000_000, Currency.NETHERITE.getValue());
    }

    @Test
    void conversionRateIsOneHundred() {
        assertEquals(100, Currency.CONVERSION_RATE);
    }

    @Test
    void eachDenominationIsOneHundredTimesThePrevious() {
        assertEquals(Currency.BRONZE.getValue() * 100, Currency.SILVER.getValue());
        assertEquals(Currency.SILVER.getValue() * 100, Currency.GOLD.getValue());
        assertEquals(Currency.GOLD.getValue() * 100, Currency.NETHERITE.getValue());
    }

    @ParameterizedTest
    @EnumSource(Currency.class)
    void eachCurrencyHasAnItemId(Currency currency) {
        assertNotNull(currency.getItemId());
        assertFalse(currency.getItemId().isEmpty());
    }

    @Test
    void itemIdsAreCorrect() {
        assertEquals("bronze_coin", Currency.BRONZE.getItemId());
        assertEquals("silver_coin", Currency.SILVER.getItemId());
        assertEquals("gold_coin", Currency.GOLD.getItemId());
        assertEquals("netherite_coin", Currency.NETHERITE.getItemId());
    }

    @Test
    void getHighestDenominationReturnsNetherite() {
        assertEquals(Currency.NETHERITE, Currency.getHighestDenomination(1_000_000));
        assertEquals(Currency.NETHERITE, Currency.getHighestDenomination(5_000_000));
    }

    @Test
    void getHighestDenominationReturnsGold() {
        assertEquals(Currency.GOLD, Currency.getHighestDenomination(10_000));
        assertEquals(Currency.GOLD, Currency.getHighestDenomination(999_999));
    }

    @Test
    void getHighestDenominationReturnsSilver() {
        assertEquals(Currency.SILVER, Currency.getHighestDenomination(100));
        assertEquals(Currency.SILVER, Currency.getHighestDenomination(9_999));
    }

    @Test
    void getHighestDenominationReturnsBronze() {
        assertEquals(Currency.BRONZE, Currency.getHighestDenomination(1));
        assertEquals(Currency.BRONZE, Currency.getHighestDenomination(99));
    }

    @Test
    void getHighestDenominationReturnsBronzeForZero() {
        assertEquals(Currency.BRONZE, Currency.getHighestDenomination(0));
    }

    @Test
    void getHighestDenominationReturnsBronzeForNegative() {
        assertEquals(Currency.BRONZE, Currency.getHighestDenomination(-1));
    }

    @Test
    void valuesDescendingReturnsCorrectOrder() {
        Currency[] descending = Currency.valuesDescending();
        assertEquals(4, descending.length);
        assertEquals(Currency.NETHERITE, descending[0]);
        assertEquals(Currency.GOLD, descending[1]);
        assertEquals(Currency.SILVER, descending[2]);
        assertEquals(Currency.BRONZE, descending[3]);
    }

    @Test
    void valuesHasFourDenominations() {
        assertEquals(4, Currency.values().length);
    }
}
