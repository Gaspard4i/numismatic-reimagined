package dev.gaspard4i.numismatic.currency;

import dev.gaspard4i.numismatic.NumismaticConstants;

/**
 * Simple in-memory implementation of CurrencyStorage.
 * Used by PlayerCurrencyManager to back each player's purse.
 */
public class SimpleCurrencyStorage implements CurrencyStorage {

    private long value;

    public SimpleCurrencyStorage() {
        this.value = 0;
    }

    public SimpleCurrencyStorage(long initialValue) {
        if (initialValue < 0) {
            throw new IllegalArgumentException("Initial value cannot be negative: " + initialValue);
        }
        this.value = Math.min(initialValue, NumismaticConstants.MAX_CURRENCY);
    }

    @Override
    public long getValue() {
        return value;
    }

    @Override
    public void setValue(long value) {
        if (value < 0) {
            throw new IllegalArgumentException("Value cannot be negative: " + value);
        }
        this.value = Math.min(value, NumismaticConstants.MAX_CURRENCY);
    }

    @Override
    public long add(long amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Amount cannot be negative: " + amount);
        }
        long newValue = this.value + amount;
        if (newValue < this.value) {
            // overflow
            this.value = NumismaticConstants.MAX_CURRENCY;
        } else {
            this.value = Math.min(newValue, NumismaticConstants.MAX_CURRENCY);
        }
        return this.value;
    }

    @Override
    public boolean subtract(long amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Amount cannot be negative: " + amount);
        }
        if (this.value < amount) {
            return false;
        }
        this.value -= amount;
        return true;
    }

    @Override
    public boolean canAfford(long amount) {
        return amount >= 0 && this.value >= amount;
    }
}
