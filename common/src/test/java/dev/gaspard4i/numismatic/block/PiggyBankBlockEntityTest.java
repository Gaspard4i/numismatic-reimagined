package dev.gaspard4i.numismatic.block;

import dev.gaspard4i.numismatic.currency.Currency;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the PiggyBankBlockEntity logic.
 * Since PiggyBankBlockEntity extends BlockEntity (MC class),
 * we test pure logic through static methods and constants.
 */
class PiggyBankBlockEntityTest {

    // --- Redstone signal calculation (log scale) ---

    private static int calculateRedstoneSignal(long storedValue) {
        if (storedValue == 0) return 0;
        int signal = (int) (Math.log10(storedValue) * 2) + 1;
        return Math.min(signal, 15);
    }

    @Test
    void redstoneSignalZeroWhenEmpty() {
        assertEquals(0, calculateRedstoneSignal(0));
    }

    @Test
    void redstoneSignalOneForSmallValue() {
        assertEquals(1, calculateRedstoneSignal(1));
    }

    @ParameterizedTest
    @CsvSource({
            "1, 1",
            "10, 3",
            "100, 5",
            "1000, 7",
            "10000, 9",
            "100000, 11",
            "1000000, 13",
            "10000000, 15",
            "100000000, 15"
    })
    void redstoneSignalScalesLogarithmically(long value, int expectedSignal) {
        assertEquals(expectedSignal, calculateRedstoneSignal(value));
    }

    @Test
    void redstoneSignalNeverExceeds15() {
        long[] testValues = {1, 10, 100, 1000, 10000, 100000, 1_000_000, 10_000_000, 100_000_000, Long.MAX_VALUE / 2};
        for (long v : testValues) {
            int signal = calculateRedstoneSignal(v);
            assertTrue(signal >= 0 && signal <= 15,
                    "Signal " + signal + " out of range for value " + v);
        }
    }

    // --- Value storage concept ---

    @Test
    void bronzeCoinValue() {
        assertEquals(1, Currency.BRONZE.getValue());
    }

    @Test
    void silverCoinValue() {
        assertEquals(100, Currency.SILVER.getValue());
    }

    @Test
    void goldCoinValue() {
        assertEquals(10_000, Currency.GOLD.getValue());
    }

    @Test
    void netheriteCoinValue() {
        assertEquals(1_000_000, Currency.NETHERITE.getValue());
    }

    @Test
    void mixedValueCalculation() {
        // 2 netherite + 3 gold + 5 silver + 10 bronze
        long total = 2 * Currency.NETHERITE.getValue()
                + 3 * Currency.GOLD.getValue()
                + 5 * Currency.SILVER.getValue()
                + 10 * Currency.BRONZE.getValue();
        assertEquals(2_030_510, total);
    }
}
