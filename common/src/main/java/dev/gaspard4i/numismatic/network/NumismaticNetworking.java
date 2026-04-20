package dev.gaspard4i.numismatic.network;

import dev.architectury.networking.NetworkManager;
import dev.gaspard4i.numismatic.NumismaticConstants;
import dev.gaspard4i.numismatic.client.ClientCurrencyData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public final class NumismaticNetworking {

    private NumismaticNetworking() {}

    public static final CustomPacketPayload.Type<SyncBalancePayload> SYNC_BALANCE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(
                    NumismaticConstants.MOD_ID, "sync_balance"));

    public static final CustomPacketPayload.Type<WithdrawPayload> WITHDRAW =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(
                    NumismaticConstants.MOD_ID, "withdraw"));

    public record SyncBalancePayload(long balance) implements CustomPacketPayload {
        public static final StreamCodec<FriendlyByteBuf, SyncBalancePayload> CODEC =
                StreamCodec.of(
                        (buf, p) -> buf.writeVarLong(p.balance),
                        buf -> new SyncBalancePayload(buf.readVarLong())
                );

        @Override public Type<SyncBalancePayload> type() { return SYNC_BALANCE; }
    }

    public record WithdrawPayload(long amount) implements CustomPacketPayload {
        public static final StreamCodec<FriendlyByteBuf, WithdrawPayload> CODEC =
                StreamCodec.of(
                        (buf, p) -> buf.writeVarLong(p.amount),
                        buf -> new WithdrawPayload(buf.readVarLong())
                );

        @Override public Type<WithdrawPayload> type() { return WITHDRAW; }
    }

    public static void register() {
        NetworkManager.registerReceiver(
                NetworkManager.Side.S2C,
                SYNC_BALANCE,
                SyncBalancePayload.CODEC,
                (payload, ctx) -> ClientCurrencyData.setBalance(payload.balance())
        );
        NetworkManager.registerReceiver(
                NetworkManager.Side.C2S,
                WITHDRAW,
                WithdrawPayload.CODEC,
                (payload, ctx) -> {
                    if (!(ctx.getPlayer() instanceof ServerPlayer serverPlayer)) return;
                    WithdrawHandler.handle(serverPlayer, payload.amount());
                }
        );
    }

    public static void syncBalance(ServerPlayer player, long balance) {
        NetworkManager.sendToPlayer(player, new SyncBalancePayload(balance));
    }
}
