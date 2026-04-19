package dev.gaspard4i.numismatic.shop;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

/**
 * Pure-data operations on a shop's stock list. Extracted from
 * {@link ShopBlockEntity} so the matching/consumption logic can be unit-tested
 * without instantiating a {@code BlockEntity}.
 */
public final class ShopStockOps {

    private ShopStockOps() {}

    /**
     * Counts items in {@code stock} matching the given template (Item + NBT
     * strict equality, ignoring counts and damage).
     */
    public static int countMatching(NonNullList<ItemStack> stock, ItemStack template) {
        if (template.isEmpty()) return 0;
        int total = 0;
        for (ItemStack s : stock) {
            if (matches(s, template)) total += s.getCount();
        }
        return total;
    }

    /**
     * Removes {@code amount} items matching {@code template} from {@code stock}.
     * Items may span multiple slots. Returns true if the full amount was
     * removed; if stock was insufficient, no items are removed and false is
     * returned.
     */
    public static boolean consume(NonNullList<ItemStack> stock, ItemStack template, int amount) {
        if (amount <= 0) return true;
        if (countMatching(stock, template) < amount) return false;
        int remaining = amount;
        for (int i = 0; i < stock.size() && remaining > 0; i++) {
            ItemStack s = stock.get(i);
            if (!matches(s, template)) continue;
            int toTake = Math.min(remaining, s.getCount());
            s.shrink(toTake);
            remaining -= toTake;
        }
        return remaining == 0;
    }

    /** Strict Item + NBT equality. Counts and damage are ignored. */
    public static boolean matches(ItemStack stack, ItemStack template) {
        if (stack.isEmpty()) return false;
        if (!stack.is(template.getItem())) return false;
        CompoundTag a = stack.getTag();
        CompoundTag b = template.getTag();
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.equals(b);
    }
}
