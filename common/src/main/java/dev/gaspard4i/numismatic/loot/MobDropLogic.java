package dev.gaspard4i.numismatic.loot;

import java.util.Random;

/**
 * Pure logic for computing the bronze-value drop of a mob, given config bounds and randomness.
 */
public final class MobDropLogic {

    private MobDropLogic() {}

    public static long computeDrop(long baseValue, double dropPercentage, double variancePercentage, Random rng) {
        if (rng == null) throw new IllegalArgumentException("rng must not be null");
        if (baseValue <= 0) return 0L;
        if (dropPercentage <= 0) return 0L;
        long dropChanceCheck = (long) (dropPercentage * 10_000.0);
        if (rng.nextInt(10_000) >= dropChanceCheck) return 0L;
        double variance = (rng.nextDouble() * 2.0 - 1.0) * variancePercentage;
        double scaled = baseValue * (1.0 + variance);
        return Math.max(0L, Math.round(scaled));
    }
}
