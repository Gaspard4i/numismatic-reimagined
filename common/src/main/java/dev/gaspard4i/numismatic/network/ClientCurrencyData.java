package dev.gaspard4i.numismatic.network;

import dev.architectury.networking.NetworkManager;
import dev.gaspard4i.numismatic.client.ClientShopState;
import dev.gaspard4i.numismatic.client.screen.ShopScreen;
import dev.gaspard4i.numismatic.currency.CurrencyResolver;
import dev.gaspard4i.numismatic.shop.OfferList;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

/**
 * Client-side cache for the local player's currency balance and shop state.
 * Updated via S2C packets from the server.
 */
public final class ClientCurrencyData {

    private static long balance = 0;

    private ClientCurrencyData() {}

    public static void registerClientReceivers() {
        NetworkManager.registerReceiver(
                NetworkManager.Side.S2C,
                NumismaticNetworking.SYNC_CURRENCY_S2C,
                (buf, context) -> {
                    long newBalance = buf.readLong();
                    context.queue(() -> balance = newBalance);
                }
        );

        // Shop: server pushes state (initial + after every edit/buy/withdraw).
        // The menu itself is opened via vanilla sp.openMenu() — this packet
        // only updates the client-side cache used by the shop screen.
        NetworkManager.registerReceiver(
                NetworkManager.Side.S2C,
                NumismaticNetworking.SYNC_SHOP_STATE_S2C,
                (buf, context) -> {
                    BlockPos pos = buf.readBlockPos();
                    boolean canEdit = buf.readBoolean();
                    boolean isAdmin = buf.readBoolean();
                    CompoundTag offersTag = buf.readNbt();
                    int stockSize = buf.readVarInt();
                    NonNullList<ItemStack> stock = NonNullList.withSize(stockSize, ItemStack.EMPTY);
                    for (int i = 0; i < stockSize; i++) stock.set(i, buf.readItem());
                    long revenue = buf.readLong();
                    OfferList offers = offersTag != null ? OfferList.fromTag(offersTag) : new OfferList();
                    context.queue(() -> {
                        ClientShopState.update(pos, canEdit, isAdmin, offers, stock, revenue);
                        Minecraft mc = Minecraft.getInstance();
                        if (mc.screen instanceof ShopScreen shopScreen) {
                            shopScreen.refreshAfterStateSync();
                        }
                    });
                }
        );
    }

    /**
     * @return the cached balance in bronze units
     */
    public static long getBalance() {
        return balance;
    }

    /**
     * @return the cached balance formatted as a string (e.g., "1N 2G 3S 4B")
     */
    public static String getFormattedBalance() {
        return CurrencyResolver.formatValue(balance);
    }

    /**
     * Resets the client cache (called on disconnect).
     */
    public static void reset() {
        balance = 0;
    }
}
