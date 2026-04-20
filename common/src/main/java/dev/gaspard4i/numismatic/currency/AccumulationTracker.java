package dev.gaspard4i.numismatic.currency;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Tracks the lifetime accumulation of each currency denomination per player.
 * Pure data structure with no MC dependency, persisted by callers.
 */
public final class AccumulationTracker {

    private final Map<UUID, long[]> totals = new HashMap<>();

    public long[] getTotals(UUID player) {
        Objects.requireNonNull(player, "player");
        long[] stored = totals.get(player);
        return stored != null ? stored.clone() : new long[Currency.values().length];
    }

    public long getTotal(UUID player, Currency currency) {
        long[] stored = totals.get(player);
        return stored != null ? stored[currency.ordinal()] : 0L;
    }

    public void add(UUID player, Currency currency, long amount) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(currency, "currency");
        if (amount <= 0) return;
        long[] arr = totals.computeIfAbsent(player, k -> new long[Currency.values().length]);
        arr[currency.ordinal()] = Math.addExact(arr[currency.ordinal()], amount);
    }

    public void addRaw(UUID player, long rawValue) {
        if (rawValue <= 0) return;
        long[] split = CurrencyResolver.splitValues(rawValue);
        for (int i = 0; i < split.length; i++) {
            if (split[i] > 0) add(player, Currency.values()[i], split[i]);
        }
    }

    public boolean hasReached(UUID player, Currency currency, long threshold) {
        return getTotal(player, currency) >= threshold;
    }

    public void put(UUID player, long[] totals) {
        Objects.requireNonNull(player, "player");
        if (totals.length != Currency.values().length) {
            throw new IllegalArgumentException(
                    "totals must have " + Currency.values().length + " elements, got " + totals.length);
        }
        this.totals.put(player, totals.clone());
    }

    public Map<UUID, long[]> snapshot() {
        Map<UUID, long[]> copy = new HashMap<>();
        for (Map.Entry<UUID, long[]> e : totals.entrySet()) {
            copy.put(e.getKey(), e.getValue().clone());
        }
        return copy;
    }

    public void clear() {
        totals.clear();
    }
}
