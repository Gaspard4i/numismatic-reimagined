package dev.gaspard4i.numismatic.currency;

import dev.architectury.event.events.common.TickEvent;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Per-player pending balance delta. When code calls {@code push(player, n)}
 * we accumulate into a map; a server tick callback flushes the accumulated
 * delta as a single coloured actionbar message via
 * {@link CurrencyNotifications}. This mirrors the original mod's
 * {@code pushTransaction / commitTransactions} pattern so a shift-click
 * buying 64 coins fires one "+ 64B" message instead of 64 individual pops.
 *
 * <p>Lives entirely server-side; the actionbar text is sent with
 * {@link ServerPlayer#displayClientMessage(net.minecraft.network.chat.Component, boolean)}.
 */
public final class CurrencyTransactions {

    /** Map<playerId, pending delta>. Non-thread-safe; server tick is single-threaded. */
    private static final Map<UUID, Long> PENDING = new HashMap<>();

    private CurrencyTransactions() {}

    /** Accumulates {@code delta} (positive = gain, negative = loss). */
    public static void push(ServerPlayer player, long delta) {
        if (player == null || delta == 0) return;
        UUID id = player.getUUID();
        long cur = PENDING.getOrDefault(id, 0L);
        long next;
        try { next = Math.addExact(cur, delta); }
        catch (ArithmeticException overflow) { next = Long.MAX_VALUE; }
        PENDING.put(id, next);
    }

    /** Sends the pending actionbar for {@code player} and clears the slot. */
    public static void flush(ServerPlayer player) {
        if (player == null) return;
        UUID id = player.getUUID();
        Long delta = PENDING.remove(id);
        if (delta == null || delta == 0) return;
        CurrencyNotifications.sendActionbar(player, delta);
    }

    /**
     * Registers a server-tick callback that flushes every player's pending
     * queue at the end of each tick. Called once from
     * {@code NumismaticReimagined.init()}.
     */
    public static void registerTickHook() {
        TickEvent.SERVER_POST.register(server -> {
            if (PENDING.isEmpty()) return;
            // Iterate a snapshot of keys so flush() can remove entries.
            var ids = PENDING.keySet().toArray(new UUID[0]);
            for (UUID id : ids) {
                ServerPlayer sp = server.getPlayerList().getPlayer(id);
                if (sp != null) flush(sp);
                else PENDING.remove(id);
            }
        });
    }
}
