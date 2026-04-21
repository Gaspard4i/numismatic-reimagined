package dev.gaspard4i.numismatic.block;

import dev.gaspard4i.numismatic.currency.Currency;

/**
 * Single-tier piggy bank. Cap = 1000 netherite coins = 1,000,000,000 bronze.
 */
public enum PiggyBankTier {

    BASE("piggy_bank", 1000L * Currency.NETHERITE.getValue());

    private final String id;
    private final long cap;

    PiggyBankTier(String id, long cap) {
        this.id = id;
        this.cap = cap;
    }

    public String id() { return id; }
    public long cap() { return cap; }
}
