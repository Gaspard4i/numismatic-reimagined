package dev.gaspard4i.numismatic.shop;

import dev.gaspard4i.numismatic.item.CoinItem;
import dev.gaspard4i.numismatic.item.MoneyBagItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

/**
 * One shop trade: a template item sold in fixed quantity for a fixed
 * bronze price. Coins and money bags can't be sold (star coin allowed).
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
            throw new IllegalArgumentException("Coins and money bags cannot be sold");
        }
    }

    public static boolean isTemplateAllowed(ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (stack.getItem() instanceof CoinItem) return false;
        if (stack.getItem() instanceof MoneyBagItem) return false;
        return true;
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        CompoundTag tplTag = new CompoundTag();
        template.save(tplTag);
        tag.put(TAG_TEMPLATE, tplTag);
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

    public ItemStack createPurchasedStack() {
        ItemStack copy = template.copy();
        copy.setCount(quantityPerPurchase);
        return copy;
    }
}
