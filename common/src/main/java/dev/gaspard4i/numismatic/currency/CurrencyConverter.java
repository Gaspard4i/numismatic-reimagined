package dev.gaspard4i.numismatic.currency;

import dev.gaspard4i.numismatic.item.NumismaticItems;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Port of upstream CurrencyConverter (wisp-forest/numismatic-overhaul, MIT).
 * Produces ItemStacks for raw currency values and splits them into valid (≤maxStackSize) chunks.
 */
public final class CurrencyConverter {

    private CurrencyConverter() {}

    /** Array format : output[ordinal] = stack (count may exceed max stack size). */
    public static ItemStack[] getAsItemStackArray(long value) {
        long[] values = CurrencyResolver.splitValues(value);
        ItemStack[] output = new ItemStack[Currency.values().length];
        for (int i = 0; i < values.length; i++) {
            output[i] = new ItemStack(NumismaticItems.getCoinItem(Currency.values()[i]), asInt(values[i]));
        }
        return output;
    }

    /** Returns non-empty stacks, **MSD first** (upstream uses addFirst loop → highest denom first). */
    public static List<ItemStack> getAsItemStackList(long value) {
        List<ItemStack> list = new ArrayList<>();
        for (ItemStack stack : getAsItemStackArray(value)) {
            if (stack.getCount() > 0) list.add(0, stack);
        }
        return list;
    }

    public static List<ItemStack> getAsItemStackList(long[] values) {
        List<ItemStack> list = new ArrayList<>();
        for (int i = 0; i < values.length; i++) {
            if (values[i] <= 0) continue;
            list.add(0, new ItemStack(
                    NumismaticItems.getCoinItem(Currency.values()[i]),
                    asInt(values[i])));
        }
        return list;
    }

    /** Splits any stack exceeding maxStackSize into multiple valid stacks. */
    public static List<ItemStack> splitAtMaxCount(List<ItemStack> input) {
        List<ItemStack> output = new ArrayList<>();
        for (ItemStack stack : input) {
            int max = stack.getMaxStackSize();
            int count = stack.getCount();
            if (count <= max) {
                output.add(stack);
                continue;
            }
            int full = count / max;
            int rest = count % max;
            for (int i = 0; i < full; i++) {
                ItemStack copy = stack.copy();
                copy.setCount(max);
                output.add(copy);
            }
            if (rest > 0) {
                ItemStack copy = stack.copy();
                copy.setCount(rest);
                output.add(copy);
            }
        }
        return output;
    }

    public static List<ItemStack> getAsValidStacks(long value) {
        return splitAtMaxCount(getAsItemStackList(value));
    }

    public static List<ItemStack> getAsValidStacks(long[] values) {
        return splitAtMaxCount(getAsItemStackList(values));
    }

    public static int asInt(long value) {
        return value > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) value;
    }

    public static int getRequiredCurrencyTypes(long value) {
        return splitAtMaxCount(getAsItemStackList(value)).size();
    }
}
