package dev.gaspard4i.numismatic.block;

import dev.gaspard4i.numismatic.client.tooltip.CurrencyTooltipData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;

import java.util.List;
import java.util.Optional;

public class PiggyBankBlockItem extends BlockItem {

    private final PiggyBankTier tier;

    public PiggyBankBlockItem(Block block, Item.Properties properties, PiggyBankTier tier) {
        super(block, properties);
        this.tier = tier;
    }

    public PiggyBankTier tier() { return tier; }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        if (readStored(stack) <= 0) {
            tooltip.add(Component.translatable("tooltip.numismatic_reimagined.piggy_bank.empty")
                    .withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        long stored = readStored(stack);
        if (stored <= 0) return Optional.empty();
        return Optional.of(CurrencyTooltipData.ofRawValue(stored));
    }

    public static long readStored(ItemStack stack) {
        var customData = stack.get(DataComponents.BLOCK_ENTITY_DATA);
        if (customData == null) return 0L;
        return customData.copyTag().getLong("Stored");
    }
}
