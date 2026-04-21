package dev.gaspard4i.numismatic.client;

import dev.gaspard4i.numismatic.currency.Currency;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MoneyBagPredicatesTest {

    @Test
    void zeroIsEmpty() {
        assertEquals(MoneyBagPredicates.EMPTY, MoneyBagPredicates.tierFor(0L));
    }

    @Test
    void pureBronzeBelowSilverStaysEmpty() {
        assertEquals(MoneyBagPredicates.EMPTY, MoneyBagPredicates.tierFor(99L));
    }

    @Test
    void exactSilverFlipsToSilver() {
        assertEquals(MoneyBagPredicates.SILVER, MoneyBagPredicates.tierFor(Currency.SILVER.getValue()));
    }

    @Test
    void belowGoldStaysSilver() {
        assertEquals(MoneyBagPredicates.SILVER, MoneyBagPredicates.tierFor(Currency.GOLD.getValue() - 1));
    }

    @Test
    void exactGoldFlipsToGold() {
        assertEquals(MoneyBagPredicates.GOLD, MoneyBagPredicates.tierFor(Currency.GOLD.getValue()));
    }

    @Test
    void belowNetheriteStaysGold() {
        assertEquals(MoneyBagPredicates.GOLD, MoneyBagPredicates.tierFor(Currency.NETHERITE.getValue() - 1));
    }

    @Test
    void exactNetheriteFlipsToNetherite() {
        assertEquals(MoneyBagPredicates.NETHERITE, MoneyBagPredicates.tierFor(Currency.NETHERITE.getValue()));
    }

    @Test
    void hugeValueStaysNetherite() {
        assertEquals(MoneyBagPredicates.NETHERITE, MoneyBagPredicates.tierFor(Long.MAX_VALUE));
    }
}
