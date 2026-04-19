package dev.gaspard4i.numismatic.block;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class PiggyBankAccountTest {

    @Test
    void newAccountIsEmpty() {
        PiggyBankAccount a = new PiggyBankAccount(1000);
        assertTrue(a.isEmpty());
        assertFalse(a.isFull());
        assertEquals(0, a.getStored());
        assertEquals(1000, a.getMaxValue());
        assertEquals(1000, a.getRemainingCapacity());
    }

    @Test
    void rejectsNegativeMaxValue() {
        assertThrows(IllegalArgumentException.class, () -> new PiggyBankAccount(-1));
    }

    @Test
    void zeroMaxIsImmediatelyFull() {
        PiggyBankAccount a = new PiggyBankAccount(0);
        assertTrue(a.isFull());
        assertTrue(a.isEmpty());
    }

    @Test
    void addAccumulatesUntilCap() {
        PiggyBankAccount a = new PiggyBankAccount(100);
        assertEquals(50, a.add(50));
        assertEquals(50, a.add(60));   // capped at 100, so only 50 accepted
        assertEquals(100, a.getStored());
        assertTrue(a.isFull());
    }

    @Test
    void addNonPositiveIsNoop() {
        PiggyBankAccount a = new PiggyBankAccount(100);
        a.add(50);
        assertEquals(0, a.add(0));
        assertEquals(0, a.add(-10));
        assertEquals(50, a.getStored());
    }

    @Test
    void addToFullReturnsZero() {
        PiggyBankAccount a = new PiggyBankAccount(100);
        a.add(100);
        assertEquals(0, a.add(50));
        assertTrue(a.isFull());
    }

    @Test
    void remainingCapacityShrinksAsAdded() {
        PiggyBankAccount a = new PiggyBankAccount(1000);
        a.add(300);
        assertEquals(700, a.getRemainingCapacity());
    }

    @Test
    void setStoredClampsToCapacity() {
        PiggyBankAccount a = new PiggyBankAccount(100);
        a.setStoredFromPersistence(150);
        assertEquals(100, a.getStored());
    }

    @Test
    void setStoredClampsNegativeToZero() {
        PiggyBankAccount a = new PiggyBankAccount(100);
        a.setStoredFromPersistence(-50);
        assertEquals(0, a.getStored());
    }

    @Test
    void redstoneSignalEmpty() {
        PiggyBankAccount a = new PiggyBankAccount(1000);
        assertEquals(0, a.getRedstoneSignal());
    }

    @Test
    void redstoneSignalFull() {
        PiggyBankAccount a = new PiggyBankAccount(1000);
        a.add(1000);
        assertEquals(15, a.getRedstoneSignal());
    }

    @ParameterizedTest
    @CsvSource({
            // Formula: 1 + (int)(14.0 * stored / max)
            "1, 1",       // ~0% → 1
            "100, 2",     // 10% → 1 + 1 = 2
            "500, 8",     // 50% → 1 + 7 = 8
            "999, 14",    // 99.9% → 1 + 13 = 14
            "1000, 15"    // 100% → cap at 15
    })
    void redstoneSignalScalesLinearly(long stored, int expectedSignal) {
        PiggyBankAccount a = new PiggyBankAccount(1000);
        a.add(stored);
        assertEquals(expectedSignal, a.getRedstoneSignal());
    }

    @Test
    void redstoneSignalNeverExceeds15() {
        PiggyBankAccount a = new PiggyBankAccount(1000);
        a.add(1000);
        assertTrue(a.getRedstoneSignal() <= 15);
    }
}
