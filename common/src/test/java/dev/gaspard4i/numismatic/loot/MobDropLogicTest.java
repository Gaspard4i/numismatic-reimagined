package dev.gaspard4i.numismatic.loot;

import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MobDropLogicTest {

    @Test
    void zeroBaseValueAlwaysReturnsZero() {
        assertEquals(0L, MobDropLogic.computeDrop(0, 1.0, 0.5, new Random(0)));
    }

    @Test
    void zeroPercentageNeverDrops() {
        assertEquals(0L, MobDropLogic.computeDrop(100, 0, 0.5, new Random(0)));
    }

    @Test
    void fullPercentageAlwaysDropsApproxBase() {
        long sum = 0;
        Random rng = new Random(42);
        for (int i = 0; i < 1000; i++) {
            sum += MobDropLogic.computeDrop(100, 1.0, 0.0, rng);
        }
        assertEquals(100_000L, sum);
    }

    @Test
    void varianceWidensRange() {
        long min = Long.MAX_VALUE;
        long max = Long.MIN_VALUE;
        Random rng = new Random(7);
        for (int i = 0; i < 5000; i++) {
            long v = MobDropLogic.computeDrop(100, 1.0, 0.5, rng);
            if (v < min) min = v;
            if (v > max) max = v;
        }
        assertTrue(min < 80, "expected lower bound below 80, got " + min);
        assertTrue(max > 120, "expected upper bound above 120, got " + max);
    }

    @Test
    void rngNullRejected() {
        assertThrows(IllegalArgumentException.class, () -> MobDropLogic.computeDrop(100, 1.0, 0.0, null));
    }
}
