package dev.gaspard4i.numismatic.shop;

import dev.gaspard4i.numismatic.currency.Currency;
import dev.gaspard4i.numismatic.item.CoinItem;
import dev.gaspard4i.numismatic.item.MoneyBagItem;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Payment routines for shop purchases. Shop payments come exclusively from
 * physical coins and money bags carried in the inventory — the player's
 * purse is treated as a "vault" and is never auto-debited.
 */
public final class ShopPaymentHelper {

    private ShopPaymentHelper() {}

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

    public static boolean tryDeductFromInventory(Player player, long amount) {
        if (amount <= 0) return true;
        if (countCoinsAndBags(player) < amount) return false;

        Inventory inv = player.getInventory();
        long remaining = amount;

        for (Currency currency : Currency.valuesDescending()) {
            if (remaining <= 0) break;
            for (int i = 0; i < inv.getContainerSize() && remaining > 0; i++) {
                ItemStack s = inv.getItem(i);
                if (!(s.getItem() instanceof CoinItem coin) || coin.getCurrency() != currency) continue;
                long unit = currency.getValue();
                long stackValue = unit * s.getCount();
                if (stackValue <= remaining) {
                    remaining -= stackValue;
                    inv.setItem(i, ItemStack.EMPTY);
                } else {
                    long coinsNeeded = remaining / unit;
                    if (remaining % unit != 0) coinsNeeded++;
                    int toRemove = (int) Math.min(coinsNeeded, s.getCount());
                    s.shrink(toRemove);
                    remaining -= (long) toRemove * unit;
                }
            }
        }

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
                    long surplus = bagValue - remaining;
                    inv.setItem(i, ItemStack.EMPTY);
                    remaining = 0;
                    refundAsMoneyBag(player, surplus);
                }
            }
        }

        if (remaining < 0) refundAsMoneyBag(player, -remaining);
        return remaining <= 0;
    }

    private static void refundAsMoneyBag(Player player, long bronzeValue) {
        if (bronzeValue <= 0) return;
        ItemStack bag = MoneyBagItem.createWithValue(bronzeValue);
        if (!player.getInventory().add(bag)) player.drop(bag, false);
    }
}
