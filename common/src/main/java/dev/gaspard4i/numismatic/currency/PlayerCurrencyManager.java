package dev.gaspard4i.numismatic.currency;

import dev.gaspard4i.numismatic.NumismaticReimagined;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Server-side manager for all player currency balances.
 * Persisted via SavedData attached to the overworld.
 */
public class PlayerCurrencyManager extends SavedData {

    private static final String DATA_NAME = "numismatic_currency";
    private static final String TAG_PLAYERS = "Players";

    private final Map<UUID, SimpleCurrencyStorage> playerBalances = new HashMap<>();

    public PlayerCurrencyManager() {
    }

    public static PlayerCurrencyManager get(ServerLevel overworld) {
        return overworld.getDataStorage().computeIfAbsent(
                PlayerCurrencyManager::load,
                PlayerCurrencyManager::new,
                DATA_NAME
        );
    }

    public static PlayerCurrencyManager load(CompoundTag tag) {
        PlayerCurrencyManager manager = new PlayerCurrencyManager();
        CompoundTag playersTag = tag.getCompound(TAG_PLAYERS);
        for (String key : playersTag.getAllKeys()) {
            try {
                UUID uuid = UUID.fromString(key);
                long value = playersTag.getLong(key);
                manager.playerBalances.put(uuid, new SimpleCurrencyStorage(Math.max(0, value)));
            } catch (IllegalArgumentException e) {
                NumismaticReimagined.LOGGER.warn("Invalid player UUID in currency data: {}", key);
            }
        }
        return manager;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        CompoundTag playersTag = new CompoundTag();
        for (Map.Entry<UUID, SimpleCurrencyStorage> entry : playerBalances.entrySet()) {
            playersTag.putLong(entry.getKey().toString(), entry.getValue().getValue());
        }
        tag.put(TAG_PLAYERS, playersTag);
        return tag;
    }

    /**
     * Gets the currency storage for a player, creating one if it doesn't exist.
     */
    public CurrencyStorage getStorage(UUID playerId) {
        return playerBalances.computeIfAbsent(playerId, k -> new SimpleCurrencyStorage());
    }

    /**
     * Gets the currency storage for a server player.
     */
    public CurrencyStorage getStorage(ServerPlayer player) {
        return getStorage(player.getUUID());
    }

    /**
     * Gets the current balance for a player.
     */
    public long getBalance(UUID playerId) {
        return getStorage(playerId).getValue();
    }

    /**
     * Adds currency to a player's purse.
     *
     * @return the new balance
     */
    public long addBalance(UUID playerId, long amount) {
        long result = getStorage(playerId).add(amount);
        setDirty();
        return result;
    }

    /**
     * Adds currency AND records the inflow into the server-wide
     * {@link AccumulationTracker}, fires the StarCoin advancement trigger
     * when the threshold is crossed, and pushes a transaction into the
     * per-player queue so the actionbar notification gets batched.
     */
    public long addBalanceAndTrack(ServerLevel overworld, UUID playerId, long amount) {
        long newBalance = addBalance(playerId, amount);
        if (overworld != null) {
            ServerPlayer sp = overworld.getServer().getPlayerList().getPlayer(playerId);
            if (amount > 0) {
                long newTotal = AccumulationTracker.get(overworld).add(playerId, amount);
                if (sp != null) {
                    dev.gaspard4i.numismatic.advancement.NumismaticTriggers.COLLECT_NETHERITE
                            .trigger(sp, newTotal);
                }
            }
            if (sp != null) {
                CurrencyTransactions.push(sp, amount);
            }
        }
        return newBalance;
    }

    /** Same semantics as {@link #addBalanceAndTrack} but for withdrawals
     *  (negative delta). Shorthand helper so callers don't need to branch. */
    public boolean subtractBalanceAndNotify(ServerLevel overworld, UUID playerId, long amount) {
        boolean ok = subtractBalance(playerId, amount);
        if (ok && overworld != null) {
            ServerPlayer sp = overworld.getServer().getPlayerList().getPlayer(playerId);
            if (sp != null) CurrencyTransactions.push(sp, -amount);
        }
        return ok;
    }

    /**
     * Subtracts currency from a player's purse.
     *
     * @return true if successful
     */
    public boolean subtractBalance(UUID playerId, long amount) {
        boolean result = getStorage(playerId).subtract(amount);
        if (result) {
            setDirty();
        }
        return result;
    }

    /**
     * Sets a player's balance to an exact value.
     */
    public void setBalance(UUID playerId, long value) {
        getStorage(playerId).setValue(value);
        setDirty();
    }

    /**
     * Copies balance from one player to another (used on death/respawn).
     */
    public void copyBalance(UUID from, UUID to) {
        long value = getBalance(from);
        setBalance(to, value);
    }
}
