package dev.gaspard4i.numismatic.item;

import dev.gaspard4i.numismatic.currency.Currency;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MoneyBagClickLogicTest {

    @Test
    void absorbCoinsZeroCountIsNoOp() {
        assertEquals(10L, MoneyBagClickLogic.absorbCoins(10L, Currency.BRONZE, 0));
        assertEquals(10L, MoneyBagClickLogic.absorbCoins(10L, Currency.SILVER, -5));
    }

    @Test
    void absorbCoinsBronze() {
        assertEquals(64L, MoneyBagClickLogic.absorbCoins(0L, Currency.BRONZE, 64));
    }

    @Test
    void absorbCoinsSilver() {
        assertEquals(200L, MoneyBagClickLogic.absorbCoins(0L, Currency.SILVER, 2));
    }

    @Test
    void absorbCoinsAdditive() {
        assertEquals(150L, MoneyBagClickLogic.absorbCoins(100L, Currency.BRONZE, 50));
    }

    @Test
    void absorbCoinsSaturatesOnOverflow() {
        assertEquals(Long.MAX_VALUE,
                MoneyBagClickLogic.absorbCoins(Long.MAX_VALUE - 10L, Currency.NETHERITE, 64));
    }

    @Test
    void absorbBagSum() {
        assertEquals(150L, MoneyBagClickLogic.absorbBag(100L, 50L));
    }

    @Test
    void absorbBagSaturates() {
        assertEquals(Long.MAX_VALUE,
                MoneyBagClickLogic.absorbBag(Long.MAX_VALUE, 1L));
    }

    @Test
    void extractEmptyBagReturnsNone() {
        MoneyBagClickLogic.ExtractResult r = MoneyBagClickLogic.extractLargestDenom(0L);
        assertTrue(r.isEmpty());
        assertSame(MoneyBagClickLogic.ExtractResult.NONE, r);
    }

    @Test
    void extractNegativeReturnsNone() {
        assertTrue(MoneyBagClickLogic.extractLargestDenom(-100L).isEmpty());
    }

    @Test
    void extractPureBronze() {
        MoneyBagClickLogic.ExtractResult r = MoneyBagClickLogic.extractLargestDenom(50L);
        assertEquals(Currency.BRONZE, r.currency());
        assertEquals(50, r.count());
        assertEquals(0L, r.remainingBagValue());
    }

    @Test
    void extractPureSilver() {
        MoneyBagClickLogic.ExtractResult r = MoneyBagClickLogic.extractLargestDenom(500L);
        assertEquals(Currency.SILVER, r.currency());
        assertEquals(5, r.count());
        assertEquals(0L, r.remainingBagValue());
    }

    @Test
    void extractGoldPrioritizedOverSilver() {
        // 10 200 = 1 gold + 2 silver → extract gold first
        MoneyBagClickLogic.ExtractResult r = MoneyBagClickLogic.extractLargestDenom(10_200L);
        assertEquals(Currency.GOLD, r.currency());
        assertEquals(1, r.count());
        assertEquals(200L, r.remainingBagValue());
    }

    @Test
    void extractNetheritePrioritized() {
        MoneyBagClickLogic.ExtractResult r = MoneyBagClickLogic.extractLargestDenom(2_500_000L);
        assertEquals(Currency.NETHERITE, r.currency());
        assertEquals(2, r.count());
        assertEquals(500_000L, r.remainingBagValue());
    }

    @Test
    void extractCapsAtSixtyFour() {
        // 100 000 bronze → would be 1000 silver, cap at 64 silver = 6400 bronze
        MoneyBagClickLogic.ExtractResult r = MoneyBagClickLogic.extractLargestDenom(100_000L);
        // 100_000 / 10_000 = 10 golds available (no cap)
        assertEquals(Currency.GOLD, r.currency());
        assertEquals(10, r.count());
        assertEquals(0L, r.remainingBagValue());
    }

    @Test
    void extractCapsAtSixtyFourSilver() {
        // 7 000 bronze = 70 silver → cap 64
        MoneyBagClickLogic.ExtractResult r = MoneyBagClickLogic.extractLargestDenom(7_000L);
        assertEquals(Currency.SILVER, r.currency());
        assertEquals(64, r.count());
        assertEquals(600L, r.remainingBagValue()); // 7000 - 6400
    }

    @Test
    void extractPreservesPartialRemainder() {
        // 157 → 1 silver + 57 bronze → first extract gives 1 silver, remainder 57
        MoneyBagClickLogic.ExtractResult r = MoneyBagClickLogic.extractLargestDenom(157L);
        assertEquals(Currency.SILVER, r.currency());
        assertEquals(1, r.count());
        assertEquals(57L, r.remainingBagValue());
    }
}
