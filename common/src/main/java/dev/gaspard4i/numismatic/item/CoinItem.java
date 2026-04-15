package dev.gaspard4i.numismatic.item;

import dev.gaspard4i.numismatic.currency.Currency;
import dev.gaspard4i.numismatic.currency.CurrencyResolver;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CoinItem extends Item {

    private final Currency currency;

    public CoinItem(Currency currency, Properties properties) {
        super(properties);
        this.currency = currency;
    }

    public Currency getCurrency() {
        return currency;
    }

    /**
     * Returns the total bronze value of the given stack.
     */
    public long getStackValue(ItemStack stack) {
        return currency.getValue() * stack.getCount();
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        long totalValue = getStackValue(stack);
        String formatted = CurrencyResolver.formatValue(totalValue);
        tooltipComponents.add(
                Component.translatable("tooltip.numismatic-reimagined.coin_value", formatted)
                        .withStyle(ChatFormatting.GOLD)
        );
    }
}
