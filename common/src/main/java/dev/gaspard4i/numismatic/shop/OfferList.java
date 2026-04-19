package dev.gaspard4i.numismatic.shop;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Bounded list of {@link ShopOffer} entries (max 56 in this build).
 */
public class OfferList {

    public static final int MAX_OFFERS = 56;
    private static final String TAG_OFFERS = "Offers";

    private final List<ShopOffer> offers = new ArrayList<>();

    public int size() { return offers.size(); }
    public boolean isFull() { return offers.size() >= MAX_OFFERS; }
    public boolean isEmpty() { return offers.isEmpty(); }

    @Nullable
    public ShopOffer get(int index) {
        if (index < 0 || index >= offers.size()) return null;
        return offers.get(index);
    }

    public List<ShopOffer> asList() {
        return Collections.unmodifiableList(offers);
    }

    public int add(ShopOffer offer) {
        if (offer == null || isFull()) return -1;
        offers.add(offer);
        return offers.size() - 1;
    }

    public boolean replace(int index, ShopOffer offer) {
        if (offer == null || index < 0 || index >= offers.size()) return false;
        offers.set(index, offer);
        return true;
    }

    public boolean remove(int index) {
        if (index < 0 || index >= offers.size()) return false;
        offers.remove(index);
        return true;
    }

    public void clear() { offers.clear(); }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();
        for (ShopOffer o : offers) list.add(o.toTag());
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
            } catch (IllegalArgumentException ignored) {}
        }
        return list;
    }
}
