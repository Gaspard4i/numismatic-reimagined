package dev.gaspard4i.numismatic.block;

import dev.gaspard4i.numismatic.currency.CurrencyResolver;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Block item for piggy banks. Always unstackable (max stack 1).
 * Shows stored value in tooltip if the piggy bank has contents.
 */
public class PiggyBankBlockItem extends BlockItem {

    public PiggyBankBlockItem(Block block, Properties properties) {
        super(block, properties.stacksTo(1));
    }

    /**
     * Returns true if this piggy bank item has stored contents.
     */
    public static boolean hasContents(ItemStack stack) {
        CompoundTag beTag = BlockItem.getBlockEntityData(stack);
        if (beTag == null) return false;
        return beTag.contains("StoredValue") && beTag.getLong("StoredValue") > 0;
    }

    /**
     * Returns the stored value in bronze units.
     */
    public static long getStoredValue(ItemStack stack) {
        CompoundTag beTag = BlockItem.getBlockEntityData(stack);
        if (beTag == null) return 0;
        return beTag.getLong("StoredValue");
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        long value = getStoredValue(stack);
        if (value > 0) {
            tooltip.add(Component.translatable("block.numismatic_reimagined.piggy_bank.contains",
                    String.format("%,d", value))
                    .withStyle(ChatFormatting.GOLD));
        }
    }
}
