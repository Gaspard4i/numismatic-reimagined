package dev.gaspard4i.numismatic.shop;

/**
 * Mutable accumulator for a shop's accumulated revenue. Extracted from
 * {@link ShopBlockEntity} so revenue accounting can be unit-tested without
 * instantiating a {@code BlockEntity}.
 */
public final class ShopRevenue {

    private long amount;

    public ShopRevenue() { this(0); }
    public ShopRevenue(long initial) { this.amount = Math.max(0, initial); }

    public long get() { return amount; }

    /**
     * Adds {@code value} to the accumulated revenue. No-ops on zero/negative
     * input. Returns the actual amount added (0 if input was non-positive).
     */
    public long add(long value) {
        if (value <= 0) return 0;
        amount += value;
        return value;
    }

    /**
     * Resets to zero and returns the previously accumulated value.
     */
    public long withdraw() {
        long current = amount;
        amount = 0;
        return current;
    }
}
