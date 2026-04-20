package dev.gaspard4i.numismatic.shop;

import dev.gaspard4i.numismatic.currency.Currency;
import dev.gaspard4i.numismatic.item.MoneyBagItem;
import dev.gaspard4i.numismatic.item.NumismaticItems;
import net.minecraft.network.protocol.game.ClientboundMerchantOffersPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import org.jetbrains.annotations.Nullable;

/**
 * Wraps a {@link ShopBlockEntity} as a vanilla {@link Merchant}. Used by
 * non-owner interactions — the buyer sees the vanilla villager-style
 * trade UI, matching the behaviour of wisp-forest/numismatic-overhaul.
 *
 * <p>Prices that fit in a single denomination (ex: 50 bronze, 3 silver,
 * 2 gold, 1 netherite) are represented as a coin stack in the buy slot.
 * Prices that span multiple denominations become a money bag with an NBT
 * tag storing the exact bronze value — the buyer must have that exact bag.
 */
public class ShopMerchant implements Merchant {

    private final ShopBlockEntity shop;
    private MerchantOffers offers = new MerchantOffers();
    @Nullable private Player customer;

    public ShopMerchant(ShopBlockEntity shop) {
        this.shop = shop;
    }

    public void refreshOffers() {
        offers.clear();
        for (ShopOffer o : shop.getOffers().asList()) {
            offers.add(toMerchantOffer(o));
        }
    }

    private MerchantOffer toMerchantOffer(ShopOffer offer) {
        long price = offer.priceBronze();
        // Decompose the price into up to two coin stacks (MerchantOffer allows
        // a primary + secondary buy item). If it doesn't fit, fall back to a
        // single money bag tagged with the exact value — buyer must have that
        // exact bag (limitation inherited from the original mod).
        ItemStack[] costs = priceAsTwoCoinStacks(price);
        ItemStack primary;
        ItemStack secondary = ItemStack.EMPTY;
        if (costs != null) {
            primary = costs[0];
            secondary = costs[1];
        } else {
            primary = MoneyBagItem.createWithValue(price);
        }

        int maxUses = shop.isAdmin()
                ? Integer.MAX_VALUE
                : (offer.quantityPerPurchase() > 0
                    ? shop.countMatchingItems(offer.template()) / offer.quantityPerPurchase()
                    : 0);

        return new MerchantOffer(primary, secondary,
                offer.createPurchasedStack(), maxUses, 0, 0);
    }

    /**
     * Tries to split {@code price} into at most two coin stacks of one
     * denomination each (each stack ≤ 64). Returns {@code null} if more than
     * two denominations are needed or either count exceeds 64.
     */
    @Nullable
    private static ItemStack[] priceAsTwoCoinStacks(long price) {
        long remaining = price;
        ItemStack first = ItemStack.EMPTY;
        ItemStack second = ItemStack.EMPTY;

        for (Currency c : Currency.valuesDescending()) {
            if (c.getValue() == 0) continue;
            long count = remaining / c.getValue();
            if (count <= 0) continue;
            if (count > 64) return null; // single denomination too big
            ItemStack s = new ItemStack(NumismaticItems.getCoinItem(c), (int) count);
            if (first.isEmpty()) first = s;
            else if (second.isEmpty()) second = s;
            else return null; // 3rd denomination needed
            remaining -= count * c.getValue();
            if (remaining == 0) break;
        }
        if (remaining != 0 || first.isEmpty()) return null;
        return new ItemStack[]{ first, second };
    }

    @Override public void setTradingPlayer(@Nullable Player customer) { this.customer = customer; }
    @Nullable @Override public Player getTradingPlayer() { return customer; }
    @Override public MerchantOffers getOffers() { return offers; }

    @Override
    public void overrideOffers(@Nullable MerchantOffers newOffers) {
        this.offers = newOffers == null ? new MerchantOffers() : newOffers;
    }

    @Override
    public void notifyTrade(MerchantOffer offer) {
        offer.increaseUses();
        if (!shop.isAdmin()) {
            for (ShopOffer so : shop.getOffers().asList()) {
                if (ItemStack.isSameItemSameTags(so.template(), offer.getResult())
                        && so.quantityPerPurchase() == offer.getResult().getCount()) {
                    shop.consumeStock(so);
                    shop.addRevenue(so.priceBronze());
                    break;
                }
            }
            // Do NOT rebuild the offers list here — that would reset `uses`
            // on every trade and make vanilla think stock is infinite, causing
            // client-side rollbacks on shift-click. Instead the offer's `uses`
            // counter (incremented above) is compared against maxUses, which
            // we set to current_stock / qty_per_purchase at offer creation.
        }
    }

    @Override public void notifyTradeUpdated(ItemStack stack) {}
    @Override public int getVillagerXp() { return 0; }
    @Override public void overrideXp(int experience) {}
    @Override public boolean showProgressBar() { return false; }
    @Override public SoundEvent getNotifyTradeSound() { return SoundEvents.VILLAGER_YES; }
    @Override public boolean isClientSide() { return false; }
}
