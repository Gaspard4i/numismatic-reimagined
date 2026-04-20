package dev.gaspard4i.numismatic.shop;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

/**
 * One open "request" on a bounty board: the board owner wants
 * {@code requested} items matching {@code template}, paying
 * {@code pricePerItem} bronze per unit delivered. {@code fulfilled}
 * counts how many have already been supplied.
 *
 * <p>When {@code strictNbt} is true the deliverer must provide an item
 * with matching NBT ({@link ItemStack#isSameItemSameTags}); otherwise
 * item-id equality is enough and damageable items must be at max durability.
 */
public record RequestOffer(ItemStack template, int requested, int fulfilled,
                           long pricePerItem, boolean strictNbt) {

    private static final String TAG_TEMPLATE = "Template";
    private static final String TAG_REQUESTED = "Requested";
    private static final String TAG_FULFILLED = "Fulfilled";
    private static final String TAG_PRICE = "Price";
    private static final String TAG_STRICT_NBT = "StrictNbt";

    public RequestOffer {
        if (template == null || template.isEmpty())
            throw new IllegalArgumentException("template required");
        if (requested <= 0)
            throw new IllegalArgumentException("requested must be > 0");
        if (fulfilled < 0 || fulfilled > requested)
            throw new IllegalArgumentException("fulfilled must be in [0, requested]");
        if (pricePerItem <= 0)
            throw new IllegalArgumentException("pricePerItem must be > 0");
    }

    public int remaining() { return requested - fulfilled; }
    public boolean isComplete() { return fulfilled >= requested; }

    /** Same template/price but with the fulfilled counter bumped by {@code n}. */
    public RequestOffer withFulfilledIncrement(int n) {
        int next = Math.min(requested, fulfilled + Math.max(0, n));
        return new RequestOffer(template, requested, next, pricePerItem, strictNbt);
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        CompoundTag tpl = new CompoundTag();
        template.save(tpl);
        tag.put(TAG_TEMPLATE, tpl);
        tag.putInt(TAG_REQUESTED, requested);
        tag.putInt(TAG_FULFILLED, fulfilled);
        tag.putLong(TAG_PRICE, pricePerItem);
        tag.putBoolean(TAG_STRICT_NBT, strictNbt);
        return tag;
    }

    public static RequestOffer fromTag(CompoundTag tag) {
        ItemStack template = ItemStack.of(tag.getCompound(TAG_TEMPLATE));
        int requested = tag.getInt(TAG_REQUESTED);
        int fulfilled = tag.getInt(TAG_FULFILLED);
        long price = tag.getLong(TAG_PRICE);
        boolean strict = tag.getBoolean(TAG_STRICT_NBT);
        return new RequestOffer(template, requested, fulfilled, price, strict);
    }
}
