package dev.gaspard4i.numismatic.shop;

import dev.gaspard4i.numismatic.item.CoinItem;
import dev.gaspard4i.numismatic.item.MoneyBagItem;
import dev.gaspard4i.numismatic.item.NumismaticItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

/**
 * A single shop offer: an item template (with NBT) sold at a fixed bronze price
 * for a fixed quantity per purchase.
 *
 * <p>Coins and money bags cannot be sold. Star coins are allowed.
 *
 * <p>{@code template} is a reference template — not consumed when set, only
 * compared against stock at purchase time.
 */
public record ShopOffer(ItemStack template, long priceBronze, int quantityPerPurchase) {

    private static final String TAG_TEMPLATE = "Template";
    private static final String TAG_PRICE = "Price";
    private static final String TAG_QUANTITY = "Quantity";

    public ShopOffer {
        if (template == null || template.isEmpty()) {
            throw new IllegalArgumentException("ShopOffer template cannot be empty");
        }
        if (priceBronze <= 0) {
            throw new IllegalArgumentException("ShopOffer price must be > 0");
        }
        if (quantityPerPurchase <= 0) {
            throw new IllegalArgumentException("ShopOffer quantity must be > 0");
        }
        if (!isTemplateAllowed(template)) {
            throw new IllegalArgumentException("Coins and money bags cannot be sold via shop");
        }
    }

    /**
     * Returns true if the given item is allowed as a shop offer template.
     * Forbids coins and money bags. Allows star coin and everything else.
     */
    public static boolean isTemplateAllowed(ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (stack.getItem() instanceof CoinItem) return false;
        if (stack.getItem() instanceof MoneyBagItem) return false;
        return true;
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        CompoundTag templateTag = new CompoundTag();
        template.save(templateTag);
        tag.put(TAG_TEMPLATE, templateTag);
        tag.putLong(TAG_PRICE, priceBronze);
        tag.putInt(TAG_QUANTITY, quantityPerPurchase);
        return tag;
    }

    public static ShopOffer fromTag(CompoundTag tag) {
        ItemStack template = ItemStack.of(tag.getCompound(TAG_TEMPLATE));
        long price = tag.getLong(TAG_PRICE);
        int qty = tag.getInt(TAG_QUANTITY);
        return new ShopOffer(template, price, qty);
    }

    /**
     * Returns a copy of the template (count = quantityPerPurchase) for delivery
     * to the buyer. The original template is never mutated.
     */
    public ItemStack createPurchasedStack() {
        ItemStack copy = template.copy();
        copy.setCount(quantityPerPurchase);
        return copy;
    }
}
