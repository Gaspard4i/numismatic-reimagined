package dev.gaspard4i.numismatic.network;

import dev.architectury.networking.NetworkManager;
import dev.gaspard4i.numismatic.client.ClientShopState;
import dev.gaspard4i.numismatic.client.screen.ShopScreen;
import dev.gaspard4i.numismatic.client.screen.ShopStockScreen;
import dev.gaspard4i.numismatic.currency.CurrencyResolver;
import dev.gaspard4i.numismatic.shop.NumismaticShop;
import dev.gaspard4i.numismatic.shop.OfferList;
import dev.gaspard4i.numismatic.shop.ShopBlockEntity;
import dev.gaspard4i.numismatic.shop.ShopMenuMode;
import dev.gaspard4i.numismatic.shop.ShopStockMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Client-side cache for the local player's currency balance.
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

        // Shop: open the appropriate screen with initial state
        NetworkManager.registerReceiver(
                NetworkManager.Side.S2C,
                NumismaticNetworking.OPEN_SHOP_SCREEN_S2C,
                (buf, context) -> {
                    BlockPos pos = buf.readBlockPos();
                    int modeOrd = buf.readVarInt();
                    boolean canEdit = buf.readBoolean();
                    boolean isAdmin = buf.readBoolean();
                    CompoundTag offersTag = buf.readNbt();
                    int stockSize = buf.readVarInt();
                    NonNullList<ItemStack> stock = NonNullList.withSize(stockSize, ItemStack.EMPTY);
                    for (int i = 0; i < stockSize; i++) stock.set(i, buf.readItem());
                    long revenue = buf.readLong();
                    OfferList offers = offersTag != null ? OfferList.fromTag(offersTag) : new OfferList();
                    ShopMenuMode mode = ShopMenuMode.values()[modeOrd];
                    context.queue(() -> {
                        ClientShopState.update(pos, canEdit, isAdmin, offers, stock, revenue);
                        ClientShopState.setMode(mode);
                        Minecraft mc = Minecraft.getInstance();
                        if (mode == ShopMenuMode.STOCK && canEdit) {
                            BlockEntity be = mc.level != null ? mc.level.getBlockEntity(pos) : null;
                            ShopBlockEntity shop = be instanceof ShopBlockEntity s ? s : null;
                            Inventory inv = mc.player != null ? mc.player.getInventory() : null;
                            if (inv != null) {
                                ShopStockMenu menu = new ShopStockMenu(0, inv, shop);
                                mc.setScreen(new ShopStockScreen(menu, inv, Component.translatable("gui.numismatic_reimagined.shop")));
                            }
                        } else {
                            mc.setScreen(new ShopScreen());
                        }
                    });
                }
        );

        // Shop: incremental sync after edit/buy/withdraw
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
                    context.queue(() -> ClientShopState.update(pos, canEdit, isAdmin, offers, stock, revenue));
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
