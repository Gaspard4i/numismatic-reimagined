package dev.gaspard4i.numismatic.shop;

import net.minecraft.world.item.ItemStack;

/**
 * Pure helpers for {@link RequestOffer} fulfillment: matching rules and
 * payout math. No MC world or player state.
 */
public final class RequestFulfillLogic {

    private RequestFulfillLogic() {}

    /**
     * Whether {@code submitted} can be used to fulfill {@code offer}:
     * <ul>
     *   <li>not empty</li>
     *   <li>same item type</li>
     *   <li>if {@code offer.strictNbt} → same NBT</li>
     *   <li>if the item is damageable → must be at full durability (no damage)</li>
     * </ul>
     */
    public static boolean matches(ItemStack submitted, RequestOffer offer) {
        if (submitted == null || submitted.isEmpty()) return false;
        if (!submitted.is(offer.template().getItem())) return false;
        if (submitted.isDamageableItem() && submitted.getDamageValue() > 0) return false;
        if (offer.strictNbt()) {
            return ItemStack.isSameItemSameTags(submitted, offer.template());
        }
        return true;
    }

    /**
     * Result of a {@code fulfill} call. {@code consumed} is how many items
     * were accepted from the submission, {@code payout} is the total bronze
     * value owed to the fulfiller.
     */
    public record FulfillResult(int consumed, long payout, RequestOffer updatedOffer) {
        public static final FulfillResult NONE =
                new FulfillResult(0, 0L, null);
        public boolean isNone() { return consumed == 0; }
    }

    /**
     * Computes how many items from {@code submitted} the offer can accept
     * and how much the deliverer gets paid, bounded by (a) the offer's
     * remaining need, (b) the submitted stack size, and (c) the board's
     * available fund.
     */
    public static FulfillResult fulfill(ItemStack submitted, RequestOffer offer, long availableFund) {
        if (!matches(submitted, offer)) return FulfillResult.NONE;
        if (offer.isComplete()) return FulfillResult.NONE;
        if (availableFund < offer.pricePerItem()) return FulfillResult.NONE;

        int maxByNeed = offer.remaining();
        int maxByStack = submitted.getCount();
        int maxByFund = (int) Math.min(Integer.MAX_VALUE, availableFund / offer.pricePerItem());
        int consumed = Math.min(Math.min(maxByNeed, maxByStack), maxByFund);
        if (consumed <= 0) return FulfillResult.NONE;

        long payout = (long) consumed * offer.pricePerItem();
        RequestOffer updated = offer.withFulfilledIncrement(consumed);
        return new FulfillResult(consumed, payout, updated);
    }
}
