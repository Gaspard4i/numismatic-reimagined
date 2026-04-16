package dev.gaspard4i.numismatic.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * The Star Coin is a unique trophy item awarded to players who accumulate
 * 1000 Netherite Coins. It has no monetary value and is not convertible.
 *
 * <p>It is NOT soulbound — it can be dropped, stolen, traded, or stored.
 */
public class StarCoinItem extends Item {

    public StarCoinItem() {
        super(new Properties().stacksTo(99).rarity(Rarity.EPIC));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        tooltipComponents.add(
                Component.translatable("tooltip.numismatic_reimagined.star_coin")
                        .withStyle(ChatFormatting.LIGHT_PURPLE)
        );
        tooltipComponents.add(
                Component.translatable("tooltip.numismatic_reimagined.star_coin.desc")
                        .withStyle(ChatFormatting.GRAY)
        );
    }
}
