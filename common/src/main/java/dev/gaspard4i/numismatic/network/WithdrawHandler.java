package dev.gaspard4i.numismatic.network;

import dev.gaspard4i.numismatic.currency.CurrencyResolver;
import dev.gaspard4i.numismatic.currency.PlayerCurrencyManager;
import dev.gaspard4i.numismatic.item.NumismaticItems;
import dev.gaspard4i.numismatic.currency.Currency;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public final class WithdrawHandler {

    private WithdrawHandler() {}

    public static void handle(ServerPlayer player, long amount) {
        if (amount <= 0) return;
        PlayerCurrencyManager manager = PlayerCurrencyManager.get(player.serverLevel());
        if (!manager.tryWithdraw(player.getUUID(), amount)) return;

        long[] split = CurrencyResolver.splitValues(amount);
        for (int i = 0; i < split.length; i++) {
            long count = split[i];
            if (count <= 0) continue;
            Currency currency = Currency.values()[i];
            while (count > 0) {
                int stackSize = (int) Math.min(count, 64L);
                ItemStack coins = new ItemStack(NumismaticItems.getCoinItem(currency), stackSize);
                if (!player.getInventory().add(coins)) {
                    player.drop(coins, false);
                }
                count -= stackSize;
            }
        }
        NumismaticNetworking.syncBalance(player, manager.getBalance(player.getUUID()));
    }
}
