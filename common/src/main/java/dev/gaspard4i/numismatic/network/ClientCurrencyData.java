package dev.gaspard4i.numismatic.network;

import dev.architectury.networking.NetworkManager;
import dev.gaspard4i.numismatic.client.ClientShopState;
import dev.gaspard4i.numismatic.client.screen.ShopScreen;
import dev.gaspard4i.numismatic.currency.CurrencyResolver;
import dev.gaspard4i.numismatic.shop.OfferList;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;

/** Client-side cache + S2C receivers (currency balance + open shop state). */
public final class ClientCurrencyData {

    private static long balance = 0;

    private ClientCurrencyData() {}

    public static void registerClientReceivers() {
        NetworkManager.registerReceiver(NetworkManager.Side.S2C,
                NumismaticNetworking.SYNC_CURRENCY_S2C, (buf, ctx) -> {
                    long newBalance = buf.readLong();
                    ctx.queue(() -> balance = newBalance);
                });

        NetworkManager.registerReceiver(NetworkManager.Side.S2C,
                NumismaticNetworking.SYNC_SHOP_STATE_S2C, (buf, ctx) -> {
                    BlockPos pos = buf.readBlockPos();
                    boolean canEdit = buf.readBoolean();
                    boolean isAdmin = buf.readBoolean();
                    boolean allowsTransfer = buf.readBoolean();
                    CompoundTag offersTag = buf.readNbt();
                    long revenue = buf.readLong();
                    OfferList offers = offersTag != null ? OfferList.fromTag(offersTag) : new OfferList();
                    ctx.queue(() -> {
                        ClientShopState.update(pos, canEdit, isAdmin, offers, revenue, allowsTransfer);
                        Minecraft mc = Minecraft.getInstance();
                        if (mc.screen instanceof ShopScreen shop) {
                            shop.refreshAfterStateSync();
                        }
                    });
                });
    }

    public static long getBalance() { return balance; }
    public static String getFormattedBalance() { return CurrencyResolver.formatValue(balance); }
    public static void reset() { balance = 0; }
}
