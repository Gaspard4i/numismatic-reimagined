package dev.gaspard4i.numismatic.currency;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CurrencyTest {

    @Test
    void valuesAreInExpectedOrder() {
        Currency[] order = Currency.values();
        assertEquals(Currency.BRONZE, order[0]);
        assertEquals(Currency.SILVER, order[1]);
        assertEquals(Currency.GOLD, order[2]);
        assertEquals(Currency.NETHERITE, order[3]);
    }

    @Test
    void rawValueScalesByDenomination() {
        assertEquals(7L, Currency.BRONZE.getRawValue(7));
        assertEquals(700L, Currency.SILVER.getRawValue(7));
        assertEquals(70_000L, Currency.GOLD.getRawValue(7));
        assertEquals(7_000_000L, Currency.NETHERITE.getRawValue(7));
    }

    @Test
    void rawValueOfZeroIsZero() {
        for (Currency c : Currency.values()) {
            assertEquals(0L, c.getRawValue(0));
        }
    }

    @Test
    void valueGetterMatchesConstants() {
        assertEquals(1L, Currency.BRONZE.getValue());
        assertEquals(100L, Currency.SILVER.getValue());
        assertEquals(10_000L, Currency.GOLD.getValue());
        assertEquals(1_000_000L, Currency.NETHERITE.getValue());
    }

    @Test
    void nameColorIsExposed() {
        assertEquals(0xae5b3c, Currency.BRONZE.getNameColor());
        assertEquals(0x617174, Currency.SILVER.getNameColor());
        assertEquals(0xbd9838, Currency.GOLD.getNameColor());
        assertEquals(0x4a4a4a, Currency.NETHERITE.getNameColor());
    }

    @Test
    void publicConstantsMirrorEnumValues() {
        assertEquals(Currency.BRONZE.getValue(), Currency.BRONZE_VALUE);
        assertEquals(Currency.SILVER.getValue(), Currency.SILVER_VALUE);
        assertEquals(Currency.GOLD.getValue(), Currency.GOLD_VALUE);
        assertEquals(Currency.NETHERITE.getValue(), Currency.NETHERITE_VALUE);
    }
}
