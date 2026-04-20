package dev.gaspard4i.numismatic.block;

/**
 * Pure logic for a piggy bank storage unit : a capped balance per tier.
 * Callers persist it via BlockEntity data components.
 */
public final class PiggyBankAccount {

    private final PiggyBankTier tier;
    private long stored;

    public PiggyBankAccount(PiggyBankTier tier) {
        this(tier, 0L);
    }

    public PiggyBankAccount(PiggyBankTier tier, long initial) {
        if (tier == null) throw new IllegalArgumentException("tier must not be null");
        if (initial < 0) throw new IllegalArgumentException("initial must be non-negative");
        this.tier = tier;
        this.stored = Math.min(initial, tier.cap());
    }

    public PiggyBankTier tier() { return tier; }
    public long stored() { return stored; }
    public long cap() { return tier.cap(); }
    public long remaining() { return tier.cap() - stored; }
    public boolean isEmpty() { return stored == 0L; }
    public boolean isFull() { return stored >= tier.cap(); }

    /** Returns the amount actually deposited (clamped by remaining capacity). */
    public long tryDeposit(long amount) {
        if (amount < 0) throw new IllegalArgumentException("amount must be non-negative");
        if (amount == 0 || isFull()) return 0L;
        long accepted = Math.min(amount, remaining());
        stored += accepted;
        return accepted;
    }

    /** Empties the bank and returns its previous contents. */
    public long crush() {
        long old = stored;
        stored = 0L;
        return old;
    }
}
