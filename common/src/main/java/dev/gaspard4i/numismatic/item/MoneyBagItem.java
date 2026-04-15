package dev.gaspard4i.numismatic.item;

import dev.gaspard4i.numismatic.currency.CurrencyResolver;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

/**
 * A money bag that contains a variable amount of currency.
 * Right-clicking opens it and drops the coins as individual items.
 */
public class MoneyBagItem extends Item {

    private static final String TAG_VALUE = "Value";

    public MoneyBagItem(Properties properties) {
        super(properties);
    }

    /**
     * Creates a money bag ItemStack with the given bronze value.
     */
    public static ItemStack createWithValue(long bronzeValue) {
        ItemStack stack = new ItemStack(NumismaticItems.MONEY_BAG.get());
        CompoundTag tag = stack.getOrCreateTag();
        tag.putLong(TAG_VALUE, bronzeValue);
        return stack;
    }

    /**
     * Gets the bronze value stored in a money bag stack.
     */
    public static long getValue(ItemStack stack) {
        if (stack.hasTag() && stack.getTag() != null) {
            return stack.getTag().getLong(TAG_VALUE);
        }
        return 0;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);

        if (!level.isClientSide()) {
            long value = getValue(stack);
            if (value > 0) {
                Map<dev.gaspard4i.numismatic.currency.Currency, Long> split = CurrencyResolver.splitValue(value);

                for (var entry : split.entrySet()) {
                    if (entry.getValue() <= 0) continue;

                    Item coinItem = NumismaticItems.getCoinItem(entry.getKey());
                    if (coinItem == null) continue;

                    long remaining = entry.getValue();
                    while (remaining > 0) {
                        int dropCount = (int) Math.min(remaining, 64);
                        ItemStack coinStack = new ItemStack(coinItem, dropCount);
                        ItemEntity itemEntity = player.drop(coinStack, false);
                        if (itemEntity != null) {
                            itemEntity.setNoPickUpDelay();
                            itemEntity.setThrower(player.getUUID());
                        }
                        remaining -= dropCount;
                    }
                }

                stack.shrink(1);
            }
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        long value = getValue(stack);
        if (value > 0) {
            String formatted = CurrencyResolver.formatValue(value);
            tooltipComponents.add(
                    Component.translatable("tooltip.numismatic-reimagined.money_bag_value", formatted)
                            .withStyle(ChatFormatting.GOLD)
            );
        }
    }
}
