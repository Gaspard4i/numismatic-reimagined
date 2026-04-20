package dev.gaspard4i.numismatic.currency;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.gaspard4i.numismatic.NumismaticConstants;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class PlayerCurrencyManager extends SavedData {

    public static final String DATA_NAME = NumismaticConstants.MOD_ID + "_currency";

    private final Map<UUID, PurseAccount> accounts = new HashMap<>();
    private final AccumulationTracker accumulation = new AccumulationTracker();

    public static PlayerCurrencyManager get(ServerLevel level) {
        return level.getServer().overworld()
                .getDataStorage()
                .computeIfAbsent(factory(), DATA_NAME);
    }

    private static SavedData.Factory<PlayerCurrencyManager> factory() {
        return new SavedData.Factory<>(PlayerCurrencyManager::new, PlayerCurrencyManager::load, null);
    }

    public PurseAccount account(UUID player) {
        return accounts.computeIfAbsent(player, k -> new PurseAccount());
    }

    public AccumulationTracker accumulation() {
        return accumulation;
    }

    public long getBalance(UUID player) {
        return account(player).getBalance();
    }

    public void deposit(UUID player, long amount) {
        if (amount <= 0) return;
        account(player).deposit(amount);
        accumulation.addRaw(player, amount);
        setDirty();
    }

    public boolean tryWithdraw(UUID player, long amount) {
        boolean ok = account(player).tryWithdraw(amount);
        if (ok) setDirty();
        return ok;
    }

    public void setBalance(UUID player, long balance) {
        account(player).setBalance(balance);
        setDirty();
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        CompoundTag accountsTag = new CompoundTag();
        for (Map.Entry<UUID, PurseAccount> e : accounts.entrySet()) {
            accountsTag.putLong(e.getKey().toString(), e.getValue().getBalance());
        }
        tag.put("accounts", accountsTag);

        CompoundTag accumTag = new CompoundTag();
        for (Map.Entry<UUID, long[]> e : accumulation.snapshot().entrySet()) {
            CompoundTag totals = new CompoundTag();
            long[] arr = e.getValue();
            for (Currency c : Currency.values()) {
                totals.putLong(c.name(), arr[c.ordinal()]);
            }
            accumTag.put(e.getKey().toString(), totals);
        }
        tag.put("accumulation", accumTag);
        return tag;
    }

    private static PlayerCurrencyManager load(CompoundTag tag, HolderLookup.Provider registries) {
        PlayerCurrencyManager manager = new PlayerCurrencyManager();
        CompoundTag accountsTag = tag.getCompound("accounts");
        for (String key : accountsTag.getAllKeys()) {
            try {
                manager.accounts.put(UUID.fromString(key), new PurseAccount(accountsTag.getLong(key)));
            } catch (IllegalArgumentException ignored) {
                // skip malformed UUID entries
            }
        }
        CompoundTag accumTag = tag.getCompound("accumulation");
        for (String key : accumTag.getAllKeys()) {
            try {
                UUID player = UUID.fromString(key);
                CompoundTag totals = accumTag.getCompound(key);
                long[] arr = new long[Currency.values().length];
                for (Currency c : Currency.values()) {
                    arr[c.ordinal()] = totals.getLong(c.name());
                }
                manager.accumulation.put(player, arr);
            } catch (IllegalArgumentException ignored) {
                // skip malformed entries
            }
        }
        return manager;
    }

    // Codec stub kept for future migration to dynamic registries.
    @SuppressWarnings("unused")
    private static final Codec<Optional<Long>> UNUSED_BALANCE_CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.LONG.optionalFieldOf("balance").forGetter(o -> o)
    ).apply(i, x -> x));

    @SuppressWarnings("unused")
    private static final NbtOps UNUSED_NBT_OPS = NbtOps.INSTANCE;
}
