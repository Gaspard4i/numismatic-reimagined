package dev.gaspard4i.numismatic.item;

import dev.gaspard4i.numismatic.component.NumismaticDataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class MoneyBagItem extends Item {

    public MoneyBagItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    public static long getValue(ItemStack stack) {
        Long stored = stack.get(NumismaticDataComponents.MONEY_BAG_VALUE.get());
        return stored != null ? stored : 0L;
    }

    public static ItemStack createWithValue(Item moneyBag, long value) {
        ItemStack stack = new ItemStack(moneyBag);
        stack.set(NumismaticDataComponents.MONEY_BAG_VALUE.get(), value);
        return stack;
    }
}
