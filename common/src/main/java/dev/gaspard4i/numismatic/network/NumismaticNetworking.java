package dev.gaspard4i.numismatic.network;

import dev.architectury.networking.NetworkManager;
import dev.gaspard4i.numismatic.NumismaticConstants;
import dev.gaspard4i.numismatic.currency.CurrencyHelper;
import dev.gaspard4i.numismatic.currency.CurrencyResolver;
import dev.gaspard4i.numismatic.currency.PlayerCurrencyManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/**
 * Handles all network packets for the currency system.
 */
public final class NumismaticNetworking {

    // S2C: Sync the player's balance to the client
    public static final ResourceLocation SYNC_CURRENCY_S2C = new ResourceLocation(
            NumismaticConstants.MOD_ID, "sync_currency"
    );

    // C2S: Player requests to deposit all coins into purse
    public static final ResourceLocation DEPOSIT_ALL_C2S = new ResourceLocation(
            NumismaticConstants.MOD_ID, "deposit_all"
    );

    // C2S: Player requests to withdraw a specific amount from purse
    public static final ResourceLocation WITHDRAW_C2S = new ResourceLocation(
            NumismaticConstants.MOD_ID, "withdraw"
    );

    private NumismaticNetworking() {}

    public static void registerServerReceivers() {
        // Handle deposit all request
        NetworkManager.registerReceiver(
                NetworkManager.Side.C2S,
                DEPOSIT_ALL_C2S,
                (buf, context) -> {
                    context.queue(() -> {
                        if (context.getPlayer() instanceof ServerPlayer serverPlayer) {
                            ServerLevel overworld = serverPlayer.server.overworld();
                            PlayerCurrencyManager manager = PlayerCurrencyManager.get(overworld);
                            long deposited = CurrencyHelper.depositAllCoins(serverPlayer, manager);
                            if (deposited > 0) {
                                syncToClient(serverPlayer, manager);
                            }
                        }
                    });
                }
        );

        // Handle withdraw request
        NetworkManager.registerReceiver(
                NetworkManager.Side.C2S,
                WITHDRAW_C2S,
                (buf, context) -> {
                    long amount = buf.readLong();
                    context.queue(() -> {
                        if (context.getPlayer() instanceof ServerPlayer serverPlayer) {
                            if (amount <= 0) return;
                            ServerLevel overworld = serverPlayer.server.overworld();
                            PlayerCurrencyManager manager = PlayerCurrencyManager.get(overworld);
                            if (manager.subtractBalance(serverPlayer.getUUID(), amount)) {
                                CurrencyHelper.offerAsCoins(serverPlayer, amount);
                                syncToClient(serverPlayer, manager);
                            }
                        }
                    });
                }
        );
    }

    /**
     * Sends the player's current balance to their client.
     */
    public static void syncToClient(ServerPlayer player, PlayerCurrencyManager manager) {
        long balance = manager.getBalance(player.getUUID());
        FriendlyByteBuf buf = new FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
        buf.writeLong(balance);
        NetworkManager.sendToPlayer(player, SYNC_CURRENCY_S2C, buf);
    }

    /**
     * Sends a deposit all request to the server.
     */
    public static void sendDepositAll() {
        FriendlyByteBuf buf = new FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
        NetworkManager.sendToServer(DEPOSIT_ALL_C2S, buf);
    }

    /**
     * Sends a withdraw request to the server.
     */
    public static void sendWithdraw(long amount) {
        FriendlyByteBuf buf = new FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
        buf.writeLong(amount);
        NetworkManager.sendToServer(WITHDRAW_C2S, buf);
    }
}
