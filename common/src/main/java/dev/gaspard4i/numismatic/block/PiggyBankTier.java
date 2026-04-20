package dev.gaspard4i.numismatic.block;

public enum PiggyBankTier {

    BASE("piggy_bank", 9_999L),
    GOLDEN("golden_piggy_bank", 999_999L),
    NETHERITE("netherite_piggy_bank", 999_999_999L);

    private final String id;
    private final long cap;

    PiggyBankTier(String id, long cap) {
        this.id = id;
        this.cap = cap;
    }

    public String id() { return id; }
    public long cap() { return cap; }
}
