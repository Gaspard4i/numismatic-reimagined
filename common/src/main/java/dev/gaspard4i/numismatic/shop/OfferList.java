package dev.gaspard4i.numismatic.shop;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A bounded list of {@link ShopOffer} entries. Indices are stable across
 * add/remove operations within an in-memory instance; persistence preserves
 * order via NBT serialization.
 *
 * <p>The maximum capacity is {@link #MAX_OFFERS} = 81 (the design allows up
 * to 3 offers per stock slot for 27 stock slots, but the offers are
 * independent of the stock layout).
 */
public class OfferList {

    public static final int MAX_OFFERS = 81;
    private static final String TAG_OFFERS = "Offers";

    private final List<ShopOffer> offers = new ArrayList<>();

    public int size() {
        return offers.size();
    }

    public boolean isFull() {
        return offers.size() >= MAX_OFFERS;
    }

    public boolean isEmpty() {
        return offers.isEmpty();
    }

    @Nullable
    public ShopOffer get(int index) {
        if (index < 0 || index >= offers.size()) return null;
        return offers.get(index);
    }

    public List<ShopOffer> asList() {
        return Collections.unmodifiableList(offers);
    }

    /**
     * Adds an offer at the end. Returns the index assigned, or -1 if full.
     */
    public int add(ShopOffer offer) {
        if (offer == null) return -1;
        if (isFull()) return -1;
        offers.add(offer);
        return offers.size() - 1;
    }

    /**
     * Replaces the offer at {@code index}. Returns true on success.
     */
    public boolean replace(int index, ShopOffer offer) {
        if (offer == null) return false;
        if (index < 0 || index >= offers.size()) return false;
        offers.set(index, offer);
        return true;
    }

    /**
     * Removes the offer at {@code index}. Subsequent indices shift down by one.
     * Returns true if removed.
     */
    public boolean remove(int index) {
        if (index < 0 || index >= offers.size()) return false;
        offers.remove(index);
        return true;
    }

    public void clear() {
        offers.clear();
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();
        for (ShopOffer offer : offers) {
            list.add(offer.toTag());
        }
        tag.put(TAG_OFFERS, list);
        return tag;
    }

    public static OfferList fromTag(CompoundTag tag) {
        OfferList list = new OfferList();
        if (!tag.contains(TAG_OFFERS, Tag.TAG_LIST)) return list;
        ListTag offers = tag.getList(TAG_OFFERS, Tag.TAG_COMPOUND);
        for (int i = 0; i < offers.size() && list.size() < MAX_OFFERS; i++) {
            try {
                list.offers.add(ShopOffer.fromTag(offers.getCompound(i)));
            } catch (IllegalArgumentException ignored) {
                // Skip invalid offers (e.g., loaded after a config change)
            }
        }
        return list;
    }
}
