package dev.gaspard4i.numismatic.currency;

import dev.gaspard4i.numismatic.NumismaticConstants;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SimpleCurrencyStorageTest {

    // --- Constructor tests ---

    @Test
    void defaultConstructorStartsAtZero() {
        SimpleCurrencyStorage storage = new SimpleCurrencyStorage();
        assertEquals(0, storage.getValue());
    }

    @Test
    void constructorWithInitialValue() {
        SimpleCurrencyStorage storage = new SimpleCurrencyStorage(1000);
        assertEquals(1000, storage.getValue());
    }

    @Test
    void constructorWithZero() {
        SimpleCurrencyStorage storage = new SimpleCurrencyStorage(0);
        assertEquals(0, storage.getValue());
    }

    @Test
    void constructorWithNegativeThrows() {
        assertThrows(IllegalArgumentException.class, () -> new SimpleCurrencyStorage(-1));
    }

    @Test
    void constructorCapsAtMaxCurrency() {
        SimpleCurrencyStorage storage = new SimpleCurrencyStorage(Long.MAX_VALUE);
        assertEquals(NumismaticConstants.MAX_CURRENCY, storage.getValue());
    }

    // --- setValue tests ---

    @Test
    void setValueUpdatesBalance() {
        SimpleCurrencyStorage storage = new SimpleCurrencyStorage();
        storage.setValue(500);
        assertEquals(500, storage.getValue());
    }

    @Test
    void setValueToZero() {
        SimpleCurrencyStorage storage = new SimpleCurrencyStorage(100);
        storage.setValue(0);
        assertEquals(0, storage.getValue());
    }

    @Test
    void setValueNegativeThrows() {
        SimpleCurrencyStorage storage = new SimpleCurrencyStorage();
        assertThrows(IllegalArgumentException.class, () -> storage.setValue(-1));
    }

    @Test
    void setValueCapsAtMaxCurrency() {
        SimpleCurrencyStorage storage = new SimpleCurrencyStorage();
        storage.setValue(Long.MAX_VALUE);
        assertEquals(NumismaticConstants.MAX_CURRENCY, storage.getValue());
    }

    // --- add tests ---

    @Test
    void addIncreasesBalance() {
        SimpleCurrencyStorage storage = new SimpleCurrencyStorage(100);
        long result = storage.add(50);
        assertEquals(150, result);
        assertEquals(150, storage.getValue());
    }

    @Test
    void addZero() {
        SimpleCurrencyStorage storage = new SimpleCurrencyStorage(100);
        long result = storage.add(0);
        assertEquals(100, result);
    }

    @Test
    void addNegativeThrows() {
        SimpleCurrencyStorage storage = new SimpleCurrencyStorage(100);
        assertThrows(IllegalArgumentException.class, () -> storage.add(-1));
    }

    @Test
    void addCapsAtMaxCurrency() {
        SimpleCurrencyStorage storage = new SimpleCurrencyStorage(NumismaticConstants.MAX_CURRENCY - 10);
        long result = storage.add(100);
        assertEquals(NumismaticConstants.MAX_CURRENCY, result);
    }

    @Test
    void addHandlesOverflow() {
        SimpleCurrencyStorage storage = new SimpleCurrencyStorage(NumismaticConstants.MAX_CURRENCY);
        long result = storage.add(Long.MAX_VALUE / 2);
        assertEquals(NumismaticConstants.MAX_CURRENCY, result);
    }

    @Test
    void addReturnsNewBalance() {
        SimpleCurrencyStorage storage = new SimpleCurrencyStorage();
        assertEquals(42, storage.add(42));
    }

    // --- subtract tests ---

    @Test
    void subtractDecreasesBalance() {
        SimpleCurrencyStorage storage = new SimpleCurrencyStorage(100);
        assertTrue(storage.subtract(30));
        assertEquals(70, storage.getValue());
    }

    @Test
    void subtractExactBalance() {
        SimpleCurrencyStorage storage = new SimpleCurrencyStorage(100);
        assertTrue(storage.subtract(100));
        assertEquals(0, storage.getValue());
    }

    @Test
    void subtractMoreThanBalanceFails() {
        SimpleCurrencyStorage storage = new SimpleCurrencyStorage(100);
        assertFalse(storage.subtract(101));
        assertEquals(100, storage.getValue());
    }

    @Test
    void subtractFromZeroFails() {
        SimpleCurrencyStorage storage = new SimpleCurrencyStorage();
        assertFalse(storage.subtract(1));
        assertEquals(0, storage.getValue());
    }

    @Test
    void subtractZeroSucceeds() {
        SimpleCurrencyStorage storage = new SimpleCurrencyStorage(100);
        assertTrue(storage.subtract(0));
        assertEquals(100, storage.getValue());
    }

    @Test
    void subtractNegativeThrows() {
        SimpleCurrencyStorage storage = new SimpleCurrencyStorage(100);
        assertThrows(IllegalArgumentException.class, () -> storage.subtract(-1));
    }

    // --- canAfford tests ---

    @Test
    void canAffordWithEnoughFunds() {
        SimpleCurrencyStorage storage = new SimpleCurrencyStorage(100);
        assertTrue(storage.canAfford(50));
    }

    @Test
    void canAffordExactAmount() {
        SimpleCurrencyStorage storage = new SimpleCurrencyStorage(100);
        assertTrue(storage.canAfford(100));
    }

    @Test
    void canAffordWithInsufficientFunds() {
        SimpleCurrencyStorage storage = new SimpleCurrencyStorage(100);
        assertFalse(storage.canAfford(101));
    }

    @Test
    void canAffordZero() {
        SimpleCurrencyStorage storage = new SimpleCurrencyStorage(100);
        assertTrue(storage.canAfford(0));
    }

    @Test
    void canAffordNegativeReturnsFalse() {
        SimpleCurrencyStorage storage = new SimpleCurrencyStorage(100);
        assertFalse(storage.canAfford(-1));
    }

    @Test
    void canAffordZeroBalance() {
        SimpleCurrencyStorage storage = new SimpleCurrencyStorage();
        assertTrue(storage.canAfford(0));
        assertFalse(storage.canAfford(1));
    }

    // --- Integration tests ---

    @Test
    void addThenSubtract() {
        SimpleCurrencyStorage storage = new SimpleCurrencyStorage();
        storage.add(1000);
        storage.subtract(300);
        assertEquals(700, storage.getValue());
    }

    @Test
    void multipleOperations() {
        SimpleCurrencyStorage storage = new SimpleCurrencyStorage();
        storage.add(100);
        storage.add(200);
        assertTrue(storage.subtract(150));
        assertEquals(150, storage.getValue());
        assertFalse(storage.subtract(200));
        assertEquals(150, storage.getValue());
    }
}
