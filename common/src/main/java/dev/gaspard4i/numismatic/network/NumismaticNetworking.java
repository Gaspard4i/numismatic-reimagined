package dev.gaspard4i.numismatic.network;

import dev.architectury.networking.NetworkManager;
import dev.gaspard4i.numismatic.NumismaticConstants;
import dev.gaspard4i.numismatic.currency.CurrencyHelper;
import dev.gaspard4i.numismatic.currency.PlayerCurrencyManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/**
 * Handles network packets for the currency system.
 */
public final class NumismaticNetworking {

    public static final ResourceLocation SYNC_CURRENCY_S2C = new ResourceLocation(
            NumismaticConstants.MOD_ID, "sync_currency"
    );
    public static final ResourceLocation DEPOSIT_ALL_C2S = new ResourceLocation(
            NumismaticConstants.MOD_ID, "deposit_all"
    );
    public static final ResourceLocation WITHDRAW_C2S = new ResourceLocation(
            NumismaticConstants.MOD_ID, "withdraw"
    );

    private NumismaticNetworking() {}

    public static void registerServerReceivers() {
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, DEPOSIT_ALL_C2S, (buf, context) -> {
            context.queue(() -> {
                if (context.getPlayer() instanceof ServerPlayer sp) {
                    ServerLevel overworld = sp.server.overworld();
                    PlayerCurrencyManager manager = PlayerCurrencyManager.get(overworld);
                    long deposited = CurrencyHelper.depositAllCoins(sp, manager);
                    if (deposited > 0) syncToClient(sp, manager);
                }
            });
        });

        NetworkManager.registerReceiver(NetworkManager.Side.C2S, WITHDRAW_C2S, (buf, context) -> {
            long amount = buf.readLong();
            context.queue(() -> {
                if (context.getPlayer() instanceof ServerPlayer sp) {
                    if (amount <= 0) return;
                    ServerLevel overworld = sp.server.overworld();
                    PlayerCurrencyManager manager = PlayerCurrencyManager.get(overworld);
                    if (manager.subtractBalance(sp.getUUID(), amount)) {
                        CurrencyHelper.offerAsCoins(sp, amount);
                        syncToClient(sp, manager);
                    }
                }
            });
        });
    }

    public static void syncToClient(ServerPlayer player, PlayerCurrencyManager manager) {
        long balance = manager.getBalance(player.getUUID());
        FriendlyByteBuf buf = new FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
        buf.writeLong(balance);
        NetworkManager.sendToPlayer(player, SYNC_CURRENCY_S2C, buf);
    }

    public static void sendDepositAll() {
        FriendlyByteBuf buf = new FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
        NetworkManager.sendToServer(DEPOSIT_ALL_C2S, buf);
    }

    public static void sendWithdraw(long amount) {
        FriendlyByteBuf buf = new FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
        buf.writeLong(amount);
        NetworkManager.sendToServer(WITHDRAW_C2S, buf);
    }
}
