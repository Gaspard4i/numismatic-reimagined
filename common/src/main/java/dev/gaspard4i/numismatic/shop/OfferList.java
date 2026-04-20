package dev.gaspard4i.numismatic.shop;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Bounded list of {@link ShopOffer} entries. The hard cap is {@link #ABSOLUTE_MAX}
 * but a smaller {@code softMax} can be passed in to enforce a per-tier cap
 * (bronze shop = 3, silver = 6, etc.). Persistence never loses entries:
 * {@link #fromTag} preserves existing offers up to {@code ABSOLUTE_MAX},
 * so shrinking the cap (e.g. admin → bronze) cannot silently drop data.
 */
public class OfferList {

    /** Absolute safety cap. Equal to the ADMIN tier's offer limit. */
    public static final int ABSOLUTE_MAX = 56;
    /** Backwards-compatible alias — new code should call {@link #getSoftMax()}. */
    public static final int MAX_OFFERS = ABSOLUTE_MAX;
    private static final String TAG_OFFERS = "Offers";

    private final List<ShopOffer> offers = new ArrayList<>();
    private int softMax;

    public OfferList() { this(ABSOLUTE_MAX); }
    public OfferList(int softMax) {
        this.softMax = Math.max(1, Math.min(softMax, ABSOLUTE_MAX));
    }

    public int getSoftMax() { return softMax; }
    public void setSoftMax(int newMax) {
        this.softMax = Math.max(1, Math.min(newMax, ABSOLUTE_MAX));
    }

    public int size() { return offers.size(); }
    public boolean isFull() { return offers.size() >= softMax; }
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
        return fromTag(tag, ABSOLUTE_MAX);
    }

    public static OfferList fromTag(CompoundTag tag, int softMax) {
        OfferList list = new OfferList(softMax);
        if (!tag.contains(TAG_OFFERS, Tag.TAG_LIST)) return list;
        ListTag offers = tag.getList(TAG_OFFERS, Tag.TAG_COMPOUND);
        for (int i = 0; i < offers.size() && list.offers.size() < ABSOLUTE_MAX; i++) {
            try {
                list.offers.add(ShopOffer.fromTag(offers.getCompound(i)));
            } catch (IllegalArgumentException ignored) {}
        }
        return list;
    }
}
