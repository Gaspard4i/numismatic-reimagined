package dev.gaspard4i.numismatic.item;

import dev.gaspard4i.numismatic.block.PiggyBankBlock;
import dev.gaspard4i.numismatic.block.PiggyBankBlockEntity;
import dev.gaspard4i.numismatic.client.tooltip.CurrencyTooltipData;
import dev.gaspard4i.numismatic.currency.Currency;
import dev.gaspard4i.numismatic.currency.PlayerCurrencyManager;
import dev.gaspard4i.numismatic.network.NumismaticNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * A money bag that contains a variable amount of currency.
 * Right-clicking adds its value to the player's purse.
 *
 * <p>The bag's appearance and rarity change based on its contents:
 * <ul>
 *   <li>Bronze bag (< 100): COMMON (white)</li>
 *   <li>Silver bag (< 10,000): UNCOMMON (yellow)</li>
 *   <li>Gold bag (< 1,000,000): RARE (blue)</li>
 *   <li>Netherite bag (>= 1,000,000): EPIC (purple)</li>
 * </ul>
 */
public class MoneyBagItem extends Item {

    private static final String TAG_VALUE = "Value";

    // Tier thresholds (exclusive upper bounds)
    public static final long SILVER_THRESHOLD = Currency.SILVER.getValue();       // 100
    public static final long GOLD_THRESHOLD = Currency.GOLD.getValue();           // 10,000
    public static final long NETHERITE_THRESHOLD = Currency.NETHERITE.getValue(); // 1,000,000

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

    /**
     * Returns the tier (0-3) based on the bag's value.
     * Used by model predicates for texture switching.
     *
     * <p>0 = bronze, 1 = silver, 2 = gold, 3 = netherite
     */
    public static int getTier(long value) {
        if (value >= NETHERITE_THRESHOLD) return 3;
        if (value >= GOLD_THRESHOLD) return 2;
        if (value >= SILVER_THRESHOLD) return 1;
        return 0;
    }

    /**
     * Returns the tier as a normalized float for model predicates.
     * Bronze=0.0, Silver=0.33, Gold=0.66, Netherite=1.0.
     */
    public static float getTierFloat(ItemStack stack) {
        return getTier(getValue(stack)) / 3.0f;
    }

    /**
     * Returns the rarity based on the bag's content value.
     */
    public static Rarity getRarityForValue(long value) {
        if (value >= NETHERITE_THRESHOLD) return Rarity.EPIC;
        if (value >= GOLD_THRESHOLD) return Rarity.RARE;
        if (value >= SILVER_THRESHOLD) return Rarity.UNCOMMON;
        return Rarity.COMMON;
    }

    @Override
    public Rarity getRarity(ItemStack stack) {
        // Keep the name color neutral (COMMON). Tier visuals are conveyed
        // by the icon texture + the tooltip icons, not by rarity coloring.
        return Rarity.COMMON;
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

        if (!level.isClientSide() && player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            long value = getValue(stack);

            // Always consume the bag, even if empty (e.g., creative bags with no value)
            stack.shrink(1);

            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    net.minecraft.sounds.SoundEvents.EXPERIENCE_ORB_PICKUP,
                    net.minecraft.sounds.SoundSource.PLAYERS,
                    0.5f, 0.8f + level.getRandom().nextFloat() * 0.4f);

            if (value > 0) {
                net.minecraft.server.level.ServerLevel overworld = serverPlayer.server.overworld();
                PlayerCurrencyManager manager = PlayerCurrencyManager.get(overworld);
                manager.addBalanceAndTrack(overworld, serverPlayer.getUUID(), value);

                // Actionbar feedback is batched per-tick by CurrencyTransactions.
                NumismaticNetworking.syncToClient(serverPlayer, manager);
            }
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    /**
     * Bag (cursor) on top of coin/bag (slot):
     * <ul>
     *   <li>Left click → absorb slot content into the cursor bag.</li>
     *   <li>Right click with empty slot is a no-op (standard extract uses
     *       the opposite override).</li>
     * </ul>
     */
    @Override
    public boolean overrideStackedOnOther(ItemStack thisStack, Slot slot, ClickAction action, Player player) {
        if (action != ClickAction.PRIMARY) return false;
        ItemStack other = slot.getItem();
        if (other.isEmpty()) return false;

        long thisValue = getValue(thisStack);
        long merged;
        if (other.getItem() instanceof CoinItem coin) {
            merged = MoneyBagClickLogic.absorbCoins(thisValue, coin.getCurrency(), other.getCount());
        } else if (other.getItem() instanceof MoneyBagItem) {
            merged = MoneyBagClickLogic.absorbBag(thisValue, getValue(other));
        } else {
            return false;
        }

        setValue(thisStack, merged);
        slot.set(ItemStack.EMPTY);
        return true;
    }

    /**
     * Coin/bag (cursor) on top of bag (slot):
     * <ul>
     *   <li>Left click with coin/bag cursor → absorb cursor into slot bag.</li>
     *   <li>Right click with empty cursor → extract largest denomination as
     *       a coin stack onto the cursor.</li>
     * </ul>
     */
    @Override
    public boolean overrideOtherStackedOnMe(ItemStack thisStack, ItemStack other, Slot slot, ClickAction action,
                                             Player player, SlotAccess access) {
        if (action == ClickAction.SECONDARY && other.isEmpty()) {
            long bagValue = getValue(thisStack);
            MoneyBagClickLogic.ExtractResult r = MoneyBagClickLogic.extractLargestDenom(bagValue);
            if (r.isEmpty()) return false;
            setValue(thisStack, r.remainingBagValue());
            ItemStack extracted = new ItemStack(NumismaticItems.getCoinItem(r.currency()), r.count());
            access.set(extracted);
            return true;
        }

        if (action != ClickAction.PRIMARY || other.isEmpty()) return false;

        long thisValue = getValue(thisStack);
        long merged;
        if (other.getItem() instanceof CoinItem coin) {
            merged = MoneyBagClickLogic.absorbCoins(thisValue, coin.getCurrency(), other.getCount());
        } else if (other.getItem() instanceof MoneyBagItem) {
            merged = MoneyBagClickLogic.absorbBag(thisValue, getValue(other));
        } else {
            return false;
        }

        setValue(thisStack, merged);
        access.set(ItemStack.EMPTY);
        return true;
    }

    /** Mutates an existing money bag stack's stored value in-place. */
    private static void setValue(ItemStack stack, long value) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putLong(TAG_VALUE, value);
    }

    // Name is the plain "Money Bag" translation — the bag's value is
    // conveyed by the coin-icon tooltip, not the item name, to match
    // the upstream mod.

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        long value = getValue(stack);
        if (value <= 0) {
            tooltipComponents.add(
                    Component.translatable("tooltip.numismatic_reimagined.money_bag.empty")
                            .withStyle(ChatFormatting.GRAY));
        }
        // Non-empty bags render their split via getTooltipImage (icons).
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        long value = getValue(stack);
        if (value <= 0) return Optional.empty();
        return Optional.of(CurrencyTooltipData.ofRawValue(value));
    }
}
