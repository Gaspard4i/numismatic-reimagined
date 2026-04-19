package dev.gaspard4i.numismatic.shop;

import dev.gaspard4i.numismatic.currency.Currency;
import dev.gaspard4i.numismatic.currency.CurrencyResolver;
import dev.gaspard4i.numismatic.item.CoinItem;
import dev.gaspard4i.numismatic.item.MoneyBagItem;
import dev.gaspard4i.numismatic.item.NumismaticItems;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Map;

/**
 * Payment routines for shop purchases. Unlike {@code CurrencyHelper}, this
 * helper never touches the player's purse — shop payments come exclusively
 * from physical coins and money bags carried in the inventory.
 *
 * <p>The purse is treated as a "vault": you can withdraw to inventory and
 * spend, but a shop transaction will not auto-debit it.
 */
public final class ShopPaymentHelper {

    private ShopPaymentHelper() {}

    /**
     * Sums the bronze value of all coins and money bags in the player's
     * inventory. Does not include the purse balance.
     */
    public static long countCoinsAndBags(Player player) {
        long total = 0;
        Inventory inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack s = inv.getItem(i);
            if (s.isEmpty()) continue;
            if (s.getItem() instanceof CoinItem coin) total += coin.getStackValue(s);
            else if (s.getItem() instanceof MoneyBagItem) total += MoneyBagItem.getValue(s);
        }
        return total;
    }

    /**
     * Attempts to deduct exactly {@code amount} bronze from the player's
     * inventory coins/bags. If the player has less than {@code amount},
     * returns false and the inventory is left unchanged.
     *
     * <p>The deduction is greedy: removes coins/bags until the running total
     * meets or exceeds the price; if it overshoots (because we had to consume
     * a high-denomination coin that's bigger than the remainder), the surplus
     * is refunded as a money bag.
     *
     * @return true if exactly {@code amount} was deducted (with possible
     *         money-bag refund of surplus); false if insufficient funds.
     */
    public static boolean tryDeductFromInventory(Player player, long amount) {
        if (amount <= 0) return true;
        if (countCoinsAndBags(player) < amount) return false;

        Inventory inv = player.getInventory();
        long remaining = amount;

        // Phase 1: consume coins from highest denomination down.
        for (Currency currency : Currency.valuesDescending()) {
            if (remaining <= 0) break;
            for (int i = 0; i < inv.getContainerSize() && remaining > 0; i++) {
                ItemStack s = inv.getItem(i);
                if (!(s.getItem() instanceof CoinItem coin)) continue;
                if (coin.getCurrency() != currency) continue;

                long unit = currency.getValue();
                long stackValue = unit * s.getCount();

                if (stackValue <= remaining) {
                    remaining -= stackValue;
                    inv.setItem(i, ItemStack.EMPTY);
                } else {
                    long coinsNeeded = remaining / unit;
                    long mod = remaining % unit;
                    if (mod != 0) coinsNeeded++; // overshoot, refund later
                    int toRemove = (int) Math.min(coinsNeeded, s.getCount());
                    s.shrink(toRemove);
                    remaining -= (long) toRemove * unit;
                }
            }
        }

        // Phase 2: if still owed, consume money bags (in any order).
        if (remaining > 0) {
            for (int i = 0; i < inv.getContainerSize() && remaining > 0; i++) {
                ItemStack s = inv.getItem(i);
                if (!(s.getItem() instanceof MoneyBagItem)) continue;
                long bagValue = MoneyBagItem.getValue(s);
                if (bagValue <= 0) continue;

                if (bagValue <= remaining) {
                    remaining -= bagValue;
                    inv.setItem(i, ItemStack.EMPTY);
                } else {
                    // Bag bigger than what we need — consume it and refund the surplus.
                    long surplus = bagValue - remaining;
                    inv.setItem(i, ItemStack.EMPTY);
                    remaining = 0;
                    refundAsMoneyBag(player, surplus);
                }
            }
        }

        // If we overshot in phase 1, refund the surplus.
        if (remaining < 0) {
            refundAsMoneyBag(player, -remaining);
        }

        return remaining <= 0;
    }

    private static void refundAsMoneyBag(Player player, long bronzeValue) {
        if (bronzeValue <= 0) return;
        ItemStack bag = MoneyBagItem.createWithValue(bronzeValue);
        if (!player.getInventory().add(bag)) {
            player.drop(bag, false);
        }
    }
}
