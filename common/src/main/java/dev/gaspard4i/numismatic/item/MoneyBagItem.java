package dev.gaspard4i.numismatic.item;

import dev.gaspard4i.numismatic.client.tooltip.CurrencyTooltipData;
import dev.gaspard4i.numismatic.component.NumismaticDataComponents;
import dev.gaspard4i.numismatic.currency.CurrencyConverter;
import dev.gaspard4i.numismatic.currency.CurrencyNotifications;
import dev.gaspard4i.numismatic.currency.CurrencyResolver;
import dev.gaspard4i.numismatic.currency.PlayerCurrencyManager;
import dev.gaspard4i.numismatic.network.NumismaticNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;

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

    public static ItemStack createWithValue(long value) {
        return createWithValue(dev.gaspard4i.numismatic.item.NumismaticItems.MONEY_BAG.get(), value);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            long value = getValue(stack);
            if (value > 0) {
                ServerLevel overworld = serverPlayer.server.overworld();
                PlayerCurrencyManager manager = PlayerCurrencyManager.get(overworld);
                manager.deposit(serverPlayer.getUUID(), value);

                stack.shrink(1);

                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS,
                        0.6f, 1.0f + level.getRandom().nextFloat() * 0.3f);

                CurrencyNotifications.sendDeposit(serverPlayer, value);
                NumismaticNetworking.syncBalance(serverPlayer,
                        manager.getBalance(serverPlayer.getUUID()));
            }
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    /**
     * Called when {@code thisStack} is on the cursor and the player clicks a slot holding {@code other}.
     * LEFT click on coin/bag → merge into a bigger money bag placed in the slot.
     */
    @Override
    public boolean overrideStackedOnOther(ItemStack thisStack, Slot slot, ClickAction action, Player player) {
        if (action != ClickAction.PRIMARY) return false;
        ItemStack other = slot.getItem();
        if (other.isEmpty()) return false;

        long thisValue = getValue(thisStack);
        long otherValue = extractValue(other);
        if (otherValue < 0) return false;
        if (thisValue + otherValue <= 0) return false;

        ItemStack bag = createWithValue(this, thisValue + otherValue);
        slot.set(bag);
        thisStack.shrink(1);
        return true;
    }

    /**
     * Called when a money bag is in the slot and the player clicks it with {@code other} on the cursor.
     * - RIGHT click + empty cursor → extract the highest denomination coin stack onto the cursor
     * - LEFT click + coin or bag   → merge into a bigger money bag placed in the slot
     */
    @Override
    public boolean overrideOtherStackedOnMe(ItemStack thisStack, ItemStack other, Slot slot,
                                             ClickAction action, Player player, SlotAccess access) {
        // Extract coin from bag : right click with empty cursor.
        if (action == ClickAction.SECONDARY && other.isEmpty()) {
            long value = getValue(thisStack);
            if (value <= 0) return false;
            var stacks = CurrencyConverter.getAsValidStacks(value);
            if (stacks.isEmpty()) return false;
            ItemStack coinStack = stacks.get(0);
            if (!(coinStack.getItem() instanceof CoinItem coin)) return false;

            access.set(coinStack);

            long[] values = CurrencyResolver.splitValues(value);
            values[coin.getCurrency().ordinal()] -= coinStack.getCount();
            long remaining = CurrencyResolver.combineValues(values);

            if (remaining == 0) {
                slot.set(ItemStack.EMPTY);
            } else if (CurrencyResolver.canBeCompacted(values)
                    && CurrencyConverter.getAsValidStacks(remaining).size() == 1) {
                // Single-stack remainder : prefer showing it as a coin rather than a bag.
                slot.set(CurrencyConverter.getAsValidStacks(remaining).get(0));
            } else {
                slot.set(createWithValue(this, remaining));
            }
            return true;
        }

        if (action != ClickAction.PRIMARY) return false;
        if (other.isEmpty()) return false;

        long thisValue = getValue(thisStack);
        long otherValue = extractValue(other);
        if (otherValue < 0) return false;
        if (thisValue + otherValue <= 0) return false;

        ItemStack bag = createWithValue(this, thisValue + otherValue);
        slot.set(bag);
        access.set(ItemStack.EMPTY);
        return true;
    }

    private static long extractValue(ItemStack stack) {
        if (stack.getItem() instanceof CoinItem coin) {
            return coin.getStackValue(stack);
        }
        if (stack.getItem() instanceof MoneyBagItem) {
            return getValue(stack);
        }
        return -1L;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context,
                                List<Component> tooltip, TooltipFlag flag) {
        long value = getValue(stack);
        if (value <= 0) {
            tooltip.add(Component.translatable("tooltip.numismatic_reimagined.money_bag.empty")
                    .withStyle(ChatFormatting.DARK_GRAY));
        }
        // When value > 0, getTooltipImage returns the visual breakdown (coin icons + count).
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        long value = getValue(stack);
        if (value <= 0) return Optional.empty();
        return Optional.of(CurrencyTooltipData.ofRawValue(value));
    }
}
