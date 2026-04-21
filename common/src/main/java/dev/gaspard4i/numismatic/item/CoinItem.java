package dev.gaspard4i.numismatic.item;

import dev.gaspard4i.numismatic.currency.Currency;
import dev.gaspard4i.numismatic.currency.CurrencyFormatter;
import dev.gaspard4i.numismatic.currency.CurrencyNotifications;
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

    public long getStackValue(ItemStack stack) {
        return currency.getRawValue(stack.getCount());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            long value = getStackValue(stack);
            if (value > 0) {
                ServerLevel overworld = serverPlayer.server.overworld();
                PlayerCurrencyManager manager = PlayerCurrencyManager.get(overworld);
                manager.deposit(serverPlayer.getUUID(), value);

                stack.shrink(stack.getCount());

                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS,
                        0.5f, 1.0f + level.getRandom().nextFloat() * 0.4f);

                CurrencyNotifications.sendDeposit(serverPlayer, value);
                NumismaticNetworking.syncBalance(serverPlayer,
                        manager.getBalance(serverPlayer.getUUID()));
            }
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public boolean overrideStackedOnOther(ItemStack thisStack, Slot slot, ClickAction action, Player player) {
        if (action != ClickAction.PRIMARY) return false;
        ItemStack other = slot.getItem();
        if (other.isEmpty()) return false;

        long thisValue = getStackValue(thisStack);
        long otherValue = extractValue(other);
        if (otherValue < 0) return false;
        if (thisValue <= 0) return false;

        ItemStack bag = MoneyBagItem.createWithValue(
                NumismaticItems.MONEY_BAG.get(), thisValue + otherValue);
        slot.set(bag);
        thisStack.shrink(thisStack.getCount());
        return true;
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack thisStack, ItemStack other, Slot slot,
                                             ClickAction action, Player player, SlotAccess access) {
        if (action != ClickAction.PRIMARY) return false;
        if (other.isEmpty()) return false;

        long thisValue = getStackValue(thisStack);
        long otherValue = extractValue(other);
        if (otherValue < 0) return false;
        if (thisValue + otherValue <= 0) return false;

        ItemStack bag = MoneyBagItem.createWithValue(
                NumismaticItems.MONEY_BAG.get(), thisValue + otherValue);
        slot.set(bag);
        access.set(ItemStack.EMPTY);
        return true;
    }

    private static long extractValue(ItemStack stack) {
        if (stack.getItem() instanceof CoinItem coin) {
            return coin.getStackValue(stack);
        }
        if (stack.getItem() instanceof MoneyBagItem) {
            return MoneyBagItem.getValue(stack);
        }
        return -1L;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context,
                                List<Component> tooltip, TooltipFlag flag) {
        long value = getStackValue(stack);
        if (value > 0) {
            tooltip.add(Component.translatable(
                            "tooltip.numismatic_reimagined.coin_value",
                            CurrencyFormatter.format(value))
                    .withStyle(ChatFormatting.GRAY));
        }
    }

    @Nullable
    public static Long tryGetValue(ItemStack stack) {
        if (stack.getItem() instanceof CoinItem coin) return coin.getStackValue(stack);
        if (stack.getItem() instanceof MoneyBagItem) return MoneyBagItem.getValue(stack);
        return null;
    }
}
