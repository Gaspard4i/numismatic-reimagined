package dev.gaspard4i.numismatic.item;

import dev.gaspard4i.numismatic.client.tooltip.CurrencyTooltipData;
import dev.gaspard4i.numismatic.currency.Currency;
import dev.gaspard4i.numismatic.currency.PlayerCurrencyManager;
import dev.gaspard4i.numismatic.network.NumismaticNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import dev.gaspard4i.numismatic.block.PiggyBankBlock;
import dev.gaspard4i.numismatic.block.PiggyBankBlockEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

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
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;

        // Sneak + click on a piggy bank: route to the block's bulk-deposit handler
        if (player.isShiftKeyDown() && level.getBlockState(context.getClickedPos()).getBlock() instanceof PiggyBankBlock) {
            if (!level.isClientSide()) {
                BlockEntity be = level.getBlockEntity(context.getClickedPos());
                if (be instanceof PiggyBankBlockEntity piggyBank) {
                    PiggyBankBlock.dumpInventoryIntoPiggyBank(player, piggyBank, level, context.getClickedPos());
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);

        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            long value = getStackValue(stack);
            if (value > 0) {
                ServerLevel overworld = serverPlayer.server.overworld();
                PlayerCurrencyManager manager = PlayerCurrencyManager.get(overworld);
                manager.addBalanceAndTrack(overworld, serverPlayer.getUUID(), value);

                stack.shrink(stack.getCount());

                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS,
                        0.5f, 1.0f + level.getRandom().nextFloat() * 0.4f);

                // Actionbar feedback is batched per-tick by
                // CurrencyTransactions (see addBalanceAndTrack).
                NumismaticNetworking.syncToClient(serverPlayer, manager);
            }
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    /**
     * Coin on slot (cursor=this coin, slot=other):
     * Left click: if the slot holds another coin/bag, merge everything into a
     * money bag placed in the slot. The cursor stack is consumed.
     */
    @Override
    public boolean overrideStackedOnOther(ItemStack thisStack, Slot slot, ClickAction action, Player player) {
        if (action != ClickAction.PRIMARY) return false;
        ItemStack other = slot.getItem();
        if (other.isEmpty()) return false;

        long thisValue = getStackValue(thisStack);
        long otherValue;
        if (other.getItem() instanceof CoinItem otherCoin) {
            otherValue = otherCoin.getStackValue(other);
        } else if (other.getItem() instanceof MoneyBagItem) {
            otherValue = MoneyBagItem.getValue(other);
        } else {
            return false;
        }
        if (thisValue <= 0) return false;

        ItemStack bag = MoneyBagItem.createWithValue(thisValue + otherValue);
        slot.set(bag);
        thisStack.shrink(thisStack.getCount());
        return true;
    }

    /**
     * Coin/bag on this coin (cursor=other, slot=this coin):
     * Left click: merge everything into a money bag placed in the slot.
     */
    @Override
    public boolean overrideOtherStackedOnMe(ItemStack thisStack, ItemStack other, Slot slot, ClickAction action,
                                             Player player, SlotAccess access) {
        if (action != ClickAction.PRIMARY) return false;
        if (other.isEmpty()) return false;

        long thisValue = getStackValue(thisStack);
        long otherValue;
        if (other.getItem() instanceof CoinItem otherCoin) {
            otherValue = otherCoin.getStackValue(other);
        } else if (other.getItem() instanceof MoneyBagItem) {
            otherValue = MoneyBagItem.getValue(other);
        } else {
            return false;
        }
        if (thisValue + otherValue <= 0) return false;

        ItemStack bag = MoneyBagItem.createWithValue(thisValue + otherValue);
        slot.set(bag);
        access.set(ItemStack.EMPTY);
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        // The coin breakdown is rendered as an image tooltip via
        // getTooltipImage -> CurrencyTooltipComponent. No text line here.
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        long totalValue = getStackValue(stack);
        if (totalValue <= 0) return Optional.empty();
        return Optional.of(CurrencyTooltipData.ofRawValue(totalValue));
    }
}
