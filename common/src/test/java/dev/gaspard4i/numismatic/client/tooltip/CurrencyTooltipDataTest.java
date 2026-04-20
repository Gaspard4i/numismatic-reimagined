package dev.gaspard4i.numismatic.client.tooltip;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CurrencyTooltipDataTest {

    @Test
    void zeroSplitsToAllZero() {
        CurrencyTooltipData d = CurrencyTooltipData.ofRawValue(0);
        assertEquals(0, d.bronze());
        assertEquals(0, d.silver());
        assertEquals(0, d.gold());
        assertEquals(0, d.netherite());
        assertTrue(d.isEmpty());
        assertEquals(0, d.activeLines());
    }

    @Test
    void pureBronze() {
        CurrencyTooltipData d = CurrencyTooltipData.ofRawValue(42);
        assertEquals(42, d.bronze());
        assertEquals(0, d.silver());
        assertEquals(0, d.gold());
        assertEquals(0, d.netherite());
        assertEquals(1, d.activeLines());
    }

    @Test
    void pureSilver() {
        CurrencyTooltipData d = CurrencyTooltipData.ofRawValue(100);
        assertEquals(0, d.bronze());
        assertEquals(1, d.silver());
        assertEquals(1, d.activeLines());
    }

    @Test
    void pureGold() {
        CurrencyTooltipData d = CurrencyTooltipData.ofRawValue(10_000);
        assertEquals(1, d.gold());
        assertEquals(0, d.silver());
        assertEquals(0, d.bronze());
        assertEquals(1, d.activeLines());
    }

    @Test
    void pureNetherite() {
        CurrencyTooltipData d = CurrencyTooltipData.ofRawValue(1_000_000);
        assertEquals(1, d.netherite());
        assertEquals(1, d.activeLines());
    }

    @Test
    void mixedTwoDenoms() {
        CurrencyTooltipData d = CurrencyTooltipData.ofRawValue(150); // 1S + 50B
        assertEquals(50, d.bronze());
        assertEquals(1, d.silver());
        assertEquals(0, d.gold());
        assertEquals(0, d.netherite());
        assertEquals(2, d.activeLines());
    }

    @Test
    void mixedFourDenoms() {
        CurrencyTooltipData d = CurrencyTooltipData.ofRawValue(1_234_567); // 1N + 23G + 45S + 67B
        assertEquals(67, d.bronze());
        assertEquals(45, d.silver());
        assertEquals(23, d.gold());
        assertEquals(1, d.netherite());
        assertEquals(4, d.activeLines());
        assertFalse(d.isEmpty());
    }

    @Test
    void negativeClampsToZero() {
        CurrencyTooltipData d = CurrencyTooltipData.ofRawValue(-100);
        assertTrue(d.isEmpty());
    }

    @Test
    void boundarySilverExactlyOneHundred() {
        CurrencyTooltipData d = CurrencyTooltipData.ofRawValue(100);
        assertEquals(0, d.bronze());
        assertEquals(1, d.silver());
    }

    @Test
    void boundaryGoldExactlyTenThousand() {
        CurrencyTooltipData d = CurrencyTooltipData.ofRawValue(10_000);
        assertEquals(1, d.gold());
        assertEquals(0, d.silver());
        assertEquals(0, d.bronze());
    }

    @Test
    void largeAccumulation() {
        CurrencyTooltipData d = CurrencyTooltipData.ofRawValue(5_432_100L);
        assertEquals(5, d.netherite());
        assertEquals(43, d.gold());
        assertEquals(21, d.silver());
        assertEquals(0, d.bronze());
        assertEquals(3, d.activeLines());
    }
}
