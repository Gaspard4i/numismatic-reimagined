package dev.gaspard4i.numismatic.block;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;

import java.util.List;

public class PiggyBankBlockItem extends BlockItem {

    private final PiggyBankTier tier;

    public PiggyBankBlockItem(Block block, Item.Properties properties, PiggyBankTier tier) {
        super(block, properties);
        this.tier = tier;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        long stored = readStored(stack);
        tooltip.add(Component.translatable(
                        "block.numismatic_reimagined.piggy_bank.contains",
                        String.format("%,d / %,d", stored, tier.cap()))
                .withStyle(ChatFormatting.GOLD));
    }

    public static long readStored(ItemStack stack) {
        var customData = stack.get(DataComponents.BLOCK_ENTITY_DATA);
        if (customData == null) return 0L;
        return customData.copyTag().getLong("Stored");
    }
}
