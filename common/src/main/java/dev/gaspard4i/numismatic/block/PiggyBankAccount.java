package dev.gaspard4i.numismatic.block;

/**
 * Pure-data accumulator for piggy bank balance with a hard capacity. Extracted
 * from {@link PiggyBankBlockEntity} so capacity / overflow / redstone math
 * can be unit-tested without instantiating a {@code BlockEntity}.
 */
public final class PiggyBankAccount {

    private final long maxValue;
    private long stored;

    public PiggyBankAccount(long maxValue) {
        if (maxValue < 0) throw new IllegalArgumentException("maxValue must be >= 0");
        this.maxValue = maxValue;
        this.stored = 0;
    }

    public long getStored() { return stored; }
    public long getMaxValue() { return maxValue; }
    public long getRemainingCapacity() { return maxValue - stored; }
    public boolean isFull() { return stored >= maxValue; }
    public boolean isEmpty() { return stored == 0; }

    /**
     * Adds {@code amount} bronze, capped at the remaining capacity. Returns
     * the actual amount added (0 if non-positive input or already full).
     */
    public long add(long amount) {
        if (amount <= 0) return 0;
        long remaining = maxValue - stored;
        long toAdd = Math.min(amount, remaining);
        if (toAdd > 0) stored += toAdd;
        return toAdd;
    }

    /** For loading from NBT — clamps to capacity. */
    public void setStoredFromPersistence(long value) {
        this.stored = Math.max(0, Math.min(value, maxValue));
    }

    /**
     * Comparator-style redstone signal (0..15). Empty = 0, full = 15,
     * intermediate scales linearly.
     */
    public int getRedstoneSignal() {
        if (stored == 0) return 0;
        if (stored >= maxValue) return 15;
        return 1 + (int) (14.0 * stored / maxValue);
    }
}
