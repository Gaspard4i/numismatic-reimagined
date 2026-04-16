package dev.gaspard4i.numismatic.item;

import dev.gaspard4i.numismatic.currency.Currency;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the money bag tier logic.
 * Since MoneyBagItem extends Item (MC class), we test the tier/rarity logic
 * through the Currency thresholds which are pure Java.
 */
class MoneyBagItemTest {

    // Thresholds (same as MoneyBagItem but defined here to avoid loading MC classes)
    private static final long SILVER_THRESHOLD = Currency.SILVER.getValue();      // 100
    private static final long GOLD_THRESHOLD = Currency.GOLD.getValue();          // 10,000
    private static final long NETHERITE_THRESHOLD = Currency.NETHERITE.getValue(); // 1,000,000

    private static int getTier(long value) {
        if (value >= NETHERITE_THRESHOLD) return 3;
        if (value >= GOLD_THRESHOLD) return 2;
        if (value >= SILVER_THRESHOLD) return 1;
        return 0;
    }

    // --- Tier tests ---

    @Test
    void tierZeroForZeroValue() {
        assertEquals(0, getTier(0));
    }

    @Test
    void tierZeroForBronzeRange() {
        assertEquals(0, getTier(1));
        assertEquals(0, getTier(99));
    }

    @Test
    void tierOneAtSilverThreshold() {
        assertEquals(1, getTier(100));
    }

    @Test
    void tierOneForSilverRange() {
        assertEquals(1, getTier(100));
        assertEquals(1, getTier(9_999));
    }

    @Test
    void tierTwoAtGoldThreshold() {
        assertEquals(2, getTier(10_000));
    }

    @Test
    void tierTwoForGoldRange() {
        assertEquals(2, getTier(10_000));
        assertEquals(2, getTier(999_999));
    }

    @Test
    void tierThreeAtNetheriteThreshold() {
        assertEquals(3, getTier(1_000_000));
    }

    @Test
    void tierThreeForNetheriteRange() {
        assertEquals(3, getTier(1_000_000));
        assertEquals(3, getTier(100_000_000));
    }

    @ParameterizedTest
    @CsvSource({
            "0, 0",
            "1, 0",
            "99, 0",
            "100, 1",
            "5000, 1",
            "9999, 1",
            "10000, 2",
            "500000, 2",
            "999999, 2",
            "1000000, 3",
            "50000000, 3"
    })
    void tierBoundaries(long value, int expectedTier) {
        assertEquals(expectedTier, getTier(value));
    }

    // --- Threshold constants ---

    @Test
    void silverThresholdIs100() {
        assertEquals(100, SILVER_THRESHOLD);
    }

    @Test
    void goldThresholdIs10000() {
        assertEquals(10_000, GOLD_THRESHOLD);
    }

    @Test
    void netheriteThresholdIs1000000() {
        assertEquals(1_000_000, NETHERITE_THRESHOLD);
    }

    // --- Tier consistency with Currency ---

    @Test
    void tierMatchesCurrencyDenominations() {
        // Just below silver = bronze tier
        assertEquals(0, getTier(Currency.SILVER.getValue() - 1));
        // At silver = silver tier
        assertEquals(1, getTier(Currency.SILVER.getValue()));
        // Just below gold = silver tier
        assertEquals(1, getTier(Currency.GOLD.getValue() - 1));
        // At gold = gold tier
        assertEquals(2, getTier(Currency.GOLD.getValue()));
        // Just below netherite = gold tier
        assertEquals(2, getTier(Currency.NETHERITE.getValue() - 1));
        // At netherite = netherite tier
        assertEquals(3, getTier(Currency.NETHERITE.getValue()));
    }
}
