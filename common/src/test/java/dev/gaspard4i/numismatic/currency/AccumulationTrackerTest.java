package dev.gaspard4i.numismatic.currency;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AccumulationTrackerTest {

    private AccumulationTracker tracker;
    private UUID alice;
    private UUID bob;

    @BeforeEach
    void setUp() {
        tracker = new AccumulationTracker();
        alice = UUID.randomUUID();
        bob = UUID.randomUUID();
    }

    @Test
    void unknownPlayerReturnsZeroes() {
        assertArrayEquals(new long[]{0, 0, 0, 0}, tracker.getTotals(alice));
        assertEquals(0L, tracker.getTotal(alice, Currency.NETHERITE));
    }

    @Test
    void addAccumulatesPerCurrency() {
        tracker.add(alice, Currency.GOLD, 5);
        tracker.add(alice, Currency.GOLD, 3);
        tracker.add(alice, Currency.SILVER, 12);
        assertEquals(8L, tracker.getTotal(alice, Currency.GOLD));
        assertEquals(12L, tracker.getTotal(alice, Currency.SILVER));
        assertEquals(0L, tracker.getTotal(alice, Currency.NETHERITE));
    }

    @Test
    void nonPositiveAddIsIgnored() {
        tracker.add(alice, Currency.GOLD, 0);
        tracker.add(alice, Currency.GOLD, -10);
        assertEquals(0L, tracker.getTotal(alice, Currency.GOLD));
    }

    @Test
    void addRawSplitsAcrossDenominations() {
        tracker.addRaw(alice, 1_234_567L);
        long[] expected = CurrencyResolver.splitValues(1_234_567L);
        assertArrayEquals(expected, tracker.getTotals(alice));
    }

    @Test
    void addRawZeroIsNoOp() {
        tracker.addRaw(alice, 0);
        tracker.addRaw(alice, -5);
        assertArrayEquals(new long[]{0, 0, 0, 0}, tracker.getTotals(alice));
    }

    @Test
    void hasReachedRespectsThreshold() {
        tracker.add(alice, Currency.NETHERITE, 1000);
        assertTrue(tracker.hasReached(alice, Currency.NETHERITE, 1000));
        assertTrue(tracker.hasReached(alice, Currency.NETHERITE, 999));
        assertFalse(tracker.hasReached(alice, Currency.NETHERITE, 1001));
    }

    @Test
    void putReplacesValues() {
        tracker.put(alice, new long[]{1, 2, 3, 4});
        assertArrayEquals(new long[]{1, 2, 3, 4}, tracker.getTotals(alice));
        tracker.put(alice, new long[]{10, 20, 30, 40});
        assertArrayEquals(new long[]{10, 20, 30, 40}, tracker.getTotals(alice));
    }

    @Test
    void putRejectsWrongLength() {
        assertThrows(IllegalArgumentException.class, () -> tracker.put(alice, new long[]{1, 2}));
    }

    @Test
    void getTotalsReturnsDefensiveCopy() {
        tracker.add(alice, Currency.BRONZE, 5);
        long[] got = tracker.getTotals(alice);
        got[0] = 999;
        assertEquals(5L, tracker.getTotal(alice, Currency.BRONZE));
    }

    @Test
    void snapshotIsIndependent() {
        tracker.add(alice, Currency.GOLD, 7);
        tracker.add(bob, Currency.SILVER, 3);
        Map<UUID, long[]> snap = tracker.snapshot();
        assertEquals(2, snap.size());
        assertArrayEquals(new long[]{0, 0, 7, 0}, snap.get(alice));
        snap.get(alice)[2] = 0;
        assertEquals(7L, tracker.getTotal(alice, Currency.GOLD));
        assertNotSame(snap.get(alice), tracker.getTotals(alice));
    }

    @Test
    void clearRemovesAllPlayers() {
        tracker.add(alice, Currency.GOLD, 7);
        tracker.add(bob, Currency.SILVER, 3);
        tracker.clear();
        assertEquals(0, tracker.snapshot().size());
        assertEquals(0L, tracker.getTotal(alice, Currency.GOLD));
    }
}
