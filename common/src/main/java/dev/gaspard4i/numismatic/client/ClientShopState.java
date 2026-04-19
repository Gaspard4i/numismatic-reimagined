package dev.gaspard4i.numismatic.client;

import dev.gaspard4i.numismatic.shop.OfferList;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.Nullable;

/** Client-side cache for the currently open shop. */
public final class ClientShopState {

    @Nullable private static BlockPos pos;
    private static boolean canEdit;
    private static boolean isAdmin;
    private static OfferList offers = new OfferList();
    private static long revenue;

    private ClientShopState() {}

    public static void update(BlockPos pos, boolean canEdit, boolean isAdmin,
                              OfferList offers, long revenue) {
        ClientShopState.pos = pos;
        ClientShopState.canEdit = canEdit;
        ClientShopState.isAdmin = isAdmin;
        ClientShopState.offers = offers;
        ClientShopState.revenue = revenue;
    }

    @Nullable public static BlockPos getPos() { return pos; }
    public static boolean canEdit() { return canEdit; }
    public static boolean isAdmin() { return isAdmin; }
    public static OfferList getOffers() { return offers; }
    public static long getRevenue() { return revenue; }

    public static void clear() {
        pos = null;
        canEdit = false;
        isAdmin = false;
        offers = new OfferList();
        revenue = 0;
    }
}
