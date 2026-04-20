package dev.gaspard4i.numismatic.currency;

import dev.gaspard4i.numismatic.item.CoinItem;
import dev.gaspard4i.numismatic.item.MoneyBagItem;
import dev.gaspard4i.numismatic.item.NumismaticItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.Map;

/**
 * Utility class for interacting between the inventory coin system and the purse system.
 */
public final class CurrencyHelper {

    private CurrencyHelper() {}

    /**
     * Calculates the total bronze value of all coins in the player's inventory.
     */
    public static long getMoneyInInventory(Player player) {
        Inventory inventory = player.getInventory();
        long total = 0;

        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack.getItem() instanceof CoinItem coinItem) {
                total += coinItem.getStackValue(stack);
            }
        }
        return total;
    }

    /**
     * Removes coins worth the specified bronze value from the player's inventory.
     * Uses a greedy approach: removes from highest denomination first.
     *
     * @param player the player
     * @param amount the amount in bronze to deduct
     * @return true if the deduction was successful
     */
    public static boolean deductFromInventory(Player player, long amount) {
        if (amount <= 0) return true;

        long available = getMoneyInInventory(player);
        if (available < amount) return false;

        Inventory inventory = player.getInventory();
        long remaining = amount;

        // Remove coins from highest denomination first
        for (Currency currency : Currency.valuesDescending()) {
            if (remaining <= 0) break;

            for (int i = 0; i < inventory.getContainerSize() && remaining > 0; i++) {
                ItemStack stack = inventory.getItem(i);
                if (stack.getItem() instanceof CoinItem coinItem && coinItem.getCurrency() == currency) {
                    long stackValue = coinItem.getStackValue(stack);
                    if (stackValue <= remaining) {
                        remaining -= stackValue;
                        inventory.setItem(i, ItemStack.EMPTY);
                    } else {
                        // Partial removal: need to split
                        long coinsNeeded = remaining / currency.getValue();
                        if (remaining % currency.getValue() != 0) {
                            coinsNeeded++; // Take one extra, give change later
                        }
                        int toRemove = (int) Math.min(coinsNeeded, stack.getCount());
                        long removedValue = toRemove * currency.getValue();
                        stack.shrink(toRemove);
                        remaining -= removedValue;
                    }
                }
            }
        }

        // If we overcharged, give change back
        if (remaining < 0) {
            offerAsCoins(player, -remaining);
        }

        return true;
    }

    /**
     * Converts a bronze value into optimally-denominated coin stacks and adds them
     * to the player's inventory. Coins that don't fit are dropped on the ground.
     *
     * @param player the player to receive coins
     * @param bronzeValue the total value to convert and give
     */
    public static void offerAsCoins(Player player, long bronzeValue) {
        if (bronzeValue <= 0) return;

        Map<Currency, Long> split = CurrencyResolver.splitValue(bronzeValue);

        for (Map.Entry<Currency, Long> entry : split.entrySet()) {
            if (entry.getValue() <= 0) continue;

            Item coinItem = NumismaticItems.getCoinItem(entry.getKey());
            if (coinItem == null) continue;

            long count = entry.getValue();
            while (count > 0) {
                int stackSize = (int) Math.min(count, coinItem.getMaxStackSize());
                ItemStack coinStack = new ItemStack(coinItem, stackSize);

                if (!player.getInventory().add(coinStack)) {
                    // Inventory full, drop on ground
                    ItemEntity itemEntity = player.drop(coinStack, false);
                    if (itemEntity != null) {
                        itemEntity.setNoPickUpDelay();
                        itemEntity.setThrower(player.getUUID());
                    }
                }
                count -= stackSize;
            }
        }
    }

    /**
     * Drops a value as optimally-denominated coin stacks at the given position.
     * Splits the value into the highest denominations possible, stacked to 64.
     */
    public static void dropAsCoins(Level level, BlockPos pos, long bronzeValue) {
        if (bronzeValue <= 0 || level.isClientSide()) return;

        Map<Currency, Long> split = CurrencyResolver.splitValue(bronzeValue);

        for (Map.Entry<Currency, Long> entry : split.entrySet()) {
            long count = entry.getValue();
            if (count <= 0) continue;

            Item coinItem = NumismaticItems.getCoinItem(entry.getKey());
            if (coinItem == null) continue;

            while (count > 0) {
                int stackSize = (int) Math.min(count, coinItem.getMaxStackSize());
                ItemStack coinStack = new ItemStack(coinItem, stackSize);
                double dx = level.getRandom().nextDouble() * 0.7 + 0.15;
                double dy = level.getRandom().nextDouble() * 0.7 + 0.15;
                double dz = level.getRandom().nextDouble() * 0.7 + 0.15;
                ItemEntity entity = new ItemEntity(level,
                        pos.getX() + dx, pos.getY() + dy, pos.getZ() + dz, coinStack);
                entity.setDefaultPickUpDelay();
                level.addFreshEntity(entity);
                count -= stackSize;
            }
        }
    }

    /**
     * Deposits all coins AND money bags from the player's inventory into their purse.
     *
     * @param player the player
     * @param manager the currency manager
     * @return the total amount deposited
     */
    public static long depositAllCoins(Player player, PlayerCurrencyManager manager) {
        Inventory inventory = player.getInventory();
        long totalDeposited = 0;

        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack.getItem() instanceof CoinItem coinItem) {
                long value = coinItem.getStackValue(stack);
                totalDeposited += value;
                inventory.setItem(i, ItemStack.EMPTY);
            } else if (stack.getItem() instanceof MoneyBagItem) {
                long value = MoneyBagItem.getValue(stack);
                totalDeposited += value;
                inventory.setItem(i, ItemStack.EMPTY);
            }
        }

        if (totalDeposited > 0) {
            net.minecraft.server.level.ServerLevel overworld = null;
            if (player instanceof net.minecraft.server.level.ServerPlayer sp) {
                overworld = sp.server.overworld();
            }
            manager.addBalanceAndTrack(overworld, player.getUUID(), totalDeposited);
        }

        return totalDeposited;
    }
}
