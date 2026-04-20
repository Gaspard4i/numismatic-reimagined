package dev.gaspard4i.numismatic.shop;

import net.minecraft.world.item.ItemStack;

import java.util.List;

/** Pure predicate helpers for shop hopper input (no MC runtime needed). */
public final class ShopTransferLogic {

    private ShopTransferLogic() {}

    /**
     * Whether a hopper may insert the given stack into a shop.
     * <ul>
     *   <li>{@code allowsTransfer} must be true (owner toggle)</li>
     *   <li>the shop must have at least one offer whose template matches the
     *       stack's item+NBT (so you can't dump random junk in a shop)</li>
     * </ul>
     */
    public static boolean canHopperInsert(ItemStack stack, List<ShopOffer> offers, boolean allowsTransfer) {
        if (!allowsTransfer) return false;
        if (stack == null || stack.isEmpty()) return false;
        if (offers == null || offers.isEmpty()) return false;
        for (ShopOffer offer : offers) {
            if (ItemStack.isSameItemSameTags(stack, offer.template())) return true;
        }
        return false;
    }
}
