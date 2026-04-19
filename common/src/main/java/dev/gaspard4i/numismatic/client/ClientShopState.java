package dev.gaspard4i.numismatic.client;

import dev.gaspard4i.numismatic.shop.OfferList;
import dev.gaspard4i.numismatic.shop.ShopMenuMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Client-side cache for the currently open shop. Updated by S2C packets,
 * read by the shop screens. Single instance is fine because only one shop
 * menu can be open at a time.
 */
public final class ClientShopState {

    @Nullable private static BlockPos pos;
    private static boolean canEdit;
    private static boolean isAdmin;
    private static OfferList offers = new OfferList();
    private static NonNullList<ItemStack> stock = NonNullList.withSize(27, ItemStack.EMPTY);
    private static long revenue;
    private static ShopMenuMode mode = ShopMenuMode.CLIENT;

    private ClientShopState() {}

    public static void update(BlockPos pos, boolean canEdit, boolean isAdmin,
                              OfferList offers, NonNullList<ItemStack> stock, long revenue) {
        ClientShopState.pos = pos;
        ClientShopState.canEdit = canEdit;
        ClientShopState.isAdmin = isAdmin;
        ClientShopState.offers = offers;
        ClientShopState.stock = stock;
        ClientShopState.revenue = revenue;
    }

    public static void setMode(ShopMenuMode mode) { ClientShopState.mode = mode; }
    public static ShopMenuMode getMode() { return mode; }
    @Nullable public static BlockPos getPos() { return pos; }
    public static boolean canEdit() { return canEdit; }
    public static boolean isAdmin() { return isAdmin; }
    public static OfferList getOffers() { return offers; }
    public static NonNullList<ItemStack> getStock() { return stock; }
    public static long getRevenue() { return revenue; }

    public static void clear() {
        pos = null;
        canEdit = false;
        isAdmin = false;
        offers = new OfferList();
        stock = NonNullList.withSize(27, ItemStack.EMPTY);
        revenue = 0;
    }
}
