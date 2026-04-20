package dev.gaspard4i.numismatic.currency;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Tracks the total amount of NETHERITE-worth currency a player has ever
 * received (monotonic counter — it never decreases even when coins are
 * spent). Used to fire the Star Coin advancement at 1000 netherite.
 *
 * <p>Persisted as a separate SavedData so player balance math stays
 * untouched.
 */
public class AccumulationTracker extends SavedData {

    public static final String DATA_NAME = "numismatic_accumulation";
    private static final String TAG_PLAYERS = "Players";

    /** Advancement threshold in bronze units (= 1000 * NETHERITE.value). */
    public static final long STAR_COIN_THRESHOLD = 1000L * Currency.NETHERITE.getValue();

    private final Map<UUID, Long> totalByPlayer = new HashMap<>();

    public static AccumulationTracker get(ServerLevel overworld) {
        return overworld.getDataStorage().computeIfAbsent(
                AccumulationTracker::load,
                AccumulationTracker::new,
                DATA_NAME);
    }

    public static AccumulationTracker load(CompoundTag tag) {
        AccumulationTracker tracker = new AccumulationTracker();
        CompoundTag players = tag.getCompound(TAG_PLAYERS);
        for (String key : players.getAllKeys()) {
            try {
                UUID uuid = UUID.fromString(key);
                long total = players.getLong(key);
                if (total > 0) tracker.totalByPlayer.put(uuid, total);
            } catch (IllegalArgumentException ignored) {}
        }
        return tracker;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        CompoundTag players = new CompoundTag();
        for (Map.Entry<UUID, Long> e : totalByPlayer.entrySet()) {
            players.putLong(e.getKey().toString(), e.getValue());
        }
        tag.put(TAG_PLAYERS, players);
        return tag;
    }

    public long getTotalAccumulated(UUID playerId) {
        return totalByPlayer.getOrDefault(playerId, 0L);
    }

    /**
     * Adds an amount to a player's total. Returns the new total value.
     * Negative/zero additions are no-ops.
     */
    public long add(UUID playerId, long amount) {
        if (amount <= 0) return getTotalAccumulated(playerId);
        long current = getTotalAccumulated(playerId);
        long next;
        try {
            next = Math.addExact(current, amount);
        } catch (ArithmeticException overflow) {
            next = Long.MAX_VALUE;
        }
        totalByPlayer.put(playerId, next);
        setDirty();
        return next;
    }

    public boolean hasReachedStarCoinThreshold(UUID playerId) {
        return getTotalAccumulated(playerId) >= STAR_COIN_THRESHOLD;
    }
}
