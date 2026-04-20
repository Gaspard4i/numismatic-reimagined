package dev.gaspard4i.numismatic.item;

import dev.gaspard4i.numismatic.currency.Currency;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class CoinItem extends Item {

    private final Currency currency;

    public CoinItem(Currency currency, Properties properties) {
        super(properties);
        this.currency = currency;
    }

    public Currency getCurrency() {
        return currency;
    }

    public long getStackValue(ItemStack stack) {
        return currency.getRawValue(stack.getCount());
    }
}
