package dev.gaspard4i.numismatic.item;

import dev.gaspard4i.numismatic.currency.Currency;
import dev.gaspard4i.numismatic.currency.CurrencyResolver;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

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
     * Returns the tier as a float for model predicates (0.0, 1.0, 2.0, 3.0).
     */
    public static float getTierFloat(ItemStack stack) {
        return getTier(getValue(stack));
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
        return getRarityForValue(getValue(stack));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);

        if (!level.isClientSide() && player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            long value = getValue(stack);
            if (value > 0) {
                net.minecraft.server.level.ServerLevel overworld = serverPlayer.server.overworld();
                dev.gaspard4i.numismatic.currency.PlayerCurrencyManager manager =
                        dev.gaspard4i.numismatic.currency.PlayerCurrencyManager.get(overworld);
                manager.addBalance(serverPlayer.getUUID(), value);

                stack.shrink(1);

                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        net.minecraft.sounds.SoundEvents.EXPERIENCE_ORB_PICKUP,
                        net.minecraft.sounds.SoundSource.PLAYERS,
                        0.5f, 0.8f + level.getRandom().nextFloat() * 0.4f);

                dev.gaspard4i.numismatic.network.NumismaticNetworking.syncToClient(serverPlayer, manager);
            }
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public Component getName(ItemStack stack) {
        long value = getValue(stack);
        int tier = getTier(value);
        String key = switch (tier) {
            case 1 -> "item.numismatic_reimagined.money_bag.silver";
            case 2 -> "item.numismatic_reimagined.money_bag.gold";
            case 3 -> "item.numismatic_reimagined.money_bag.netherite";
            default -> "item.numismatic_reimagined.money_bag";
        };
        return Component.translatable(key);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        long value = getValue(stack);
        if (value > 0) {
            // Show total in bronze
            String totalFormatted = String.format("%,d coins", value);
            tooltipComponents.add(
                    Component.translatable("tooltip.numismatic_reimagined.money_bag_value", totalFormatted)
                            .withStyle(ChatFormatting.GOLD)
            );
            // Show breakdown by denomination
            String detailed = CurrencyResolver.formatValue(value);
            tooltipComponents.add(
                    Component.literal("(" + detailed + ")")
                            .withStyle(ChatFormatting.GRAY)
            );
        }
    }
}
