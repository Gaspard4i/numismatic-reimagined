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
        ItemStack payment = priceAsSingleCoinStack(price);
        if (payment == null) payment = MoneyBagItem.createWithValue(price);

        int maxUses = shop.isAdmin()
                ? Integer.MAX_VALUE
                : (offer.quantityPerPurchase() > 0
                    ? shop.countMatchingItems(offer.template()) / offer.quantityPerPurchase()
                    : 0);

        return new MerchantOffer(payment, offer.createPurchasedStack(), maxUses, 0, 0);
    }

    @Nullable
    private static ItemStack priceAsSingleCoinStack(long price) {
        for (Currency c : Currency.valuesDescending()) {
            if (c.getValue() == 0) continue;
            if (price % c.getValue() != 0) continue;
            long count = price / c.getValue();
            if (count <= 0 || count > 64) continue;
            return new ItemStack(NumismaticItems.getCoinItem(c), (int) count);
        }
        return null;
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
            refreshOffers();
            if (customer instanceof ServerPlayer sp) {
                sp.connection.send(new ClientboundMerchantOffersPacket(
                        sp.containerMenu.containerId, offers, 0, 0, false, false));
            }
        }
    }

    @Override public void notifyTradeUpdated(ItemStack stack) {}
    @Override public int getVillagerXp() { return 0; }
    @Override public void overrideXp(int experience) {}
    @Override public boolean showProgressBar() { return false; }
    @Override public SoundEvent getNotifyTradeSound() { return SoundEvents.VILLAGER_YES; }
    @Override public boolean isClientSide() { return false; }
}
