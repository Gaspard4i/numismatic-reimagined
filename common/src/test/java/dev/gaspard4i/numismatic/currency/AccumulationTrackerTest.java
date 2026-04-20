package dev.gaspard4i.numismatic.currency;

import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AccumulationTrackerTest {

    private static final UUID P1 = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID P2 = UUID.fromString("00000000-0000-0000-0000-000000000002");

    @Test
    void newTrackerStartsAtZero() {
        AccumulationTracker t = new AccumulationTracker();
        assertEquals(0L, t.getTotalAccumulated(P1));
        assertFalse(t.hasReachedStarCoinThreshold(P1));
    }

    @Test
    void addIsMonotonic() {
        AccumulationTracker t = new AccumulationTracker();
        t.add(P1, 100);
        t.add(P1, 200);
        assertEquals(300L, t.getTotalAccumulated(P1));
    }

    @Test
    void addZeroNoOp() {
        AccumulationTracker t = new AccumulationTracker();
        t.add(P1, 0);
        t.add(P1, -50);
        assertEquals(0L, t.getTotalAccumulated(P1));
    }

    @Test
    void addSaturatesOnOverflow() {
        AccumulationTracker t = new AccumulationTracker();
        t.add(P1, Long.MAX_VALUE - 10);
        t.add(P1, 100);
        assertEquals(Long.MAX_VALUE, t.getTotalAccumulated(P1));
    }

    @Test
    void separatePlayers() {
        AccumulationTracker t = new AccumulationTracker();
        t.add(P1, 100);
        t.add(P2, 50);
        assertEquals(100L, t.getTotalAccumulated(P1));
        assertEquals(50L, t.getTotalAccumulated(P2));
    }

    @Test
    void thresholdExactAndAbove() {
        AccumulationTracker t = new AccumulationTracker();
        assertFalse(t.hasReachedStarCoinThreshold(P1));
        t.add(P1, AccumulationTracker.STAR_COIN_THRESHOLD - 1);
        assertFalse(t.hasReachedStarCoinThreshold(P1));
        t.add(P1, 1);
        assertTrue(t.hasReachedStarCoinThreshold(P1));
        t.add(P1, 1_000_000L);
        assertTrue(t.hasReachedStarCoinThreshold(P1));
    }

    @Test
    void roundTripNbt() {
        AccumulationTracker t = new AccumulationTracker();
        t.add(P1, 12345L);
        t.add(P2, 6789L);
        CompoundTag tag = new CompoundTag();
        t.save(tag);

        AccumulationTracker reloaded = AccumulationTracker.load(tag);
        assertEquals(12345L, reloaded.getTotalAccumulated(P1));
        assertEquals(6789L, reloaded.getTotalAccumulated(P2));
    }

    @Test
    void loadEmptyTag() {
        AccumulationTracker reloaded = AccumulationTracker.load(new CompoundTag());
        assertEquals(0L, reloaded.getTotalAccumulated(P1));
    }

    @Test
    void loadIgnoresInvalidUuid() {
        CompoundTag tag = new CompoundTag();
        CompoundTag players = new CompoundTag();
        players.putLong("not-a-uuid", 999);
        tag.put("Players", players);
        AccumulationTracker reloaded = AccumulationTracker.load(tag);
        assertEquals(0L, reloaded.getTotalAccumulated(P1));
    }

    @Test
    void thresholdConstantEqualsThousandNetherite() {
        assertEquals(1000L * Currency.NETHERITE.getValue(),
                AccumulationTracker.STAR_COIN_THRESHOLD);
    }
}
