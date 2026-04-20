package dev.gaspard4i.numismatic.client.render;

import dev.gaspard4i.numismatic.shop.ShopBlock;
import dev.gaspard4i.numismatic.shop.ShopTier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Shared color logic for the shop block / item tints. Each tier draws
 * its plate with {@link ShopTier#colorRgb} so the visible colour of the
 * shop matches the coin used to craft it.
 */
public final class ShopTint {

    private ShopTint() {}

    public static int forBlockState(BlockState state, int tintIndex) {
        if (tintIndex != 0) return -1; // only plate faces carry tintindex=0
        Block block = state.getBlock();
        if (block instanceof ShopBlock sb) return sb.tier().colorRgb();
        return ShopTier.GOLD.colorRgb();
    }

    public static int forItemStack(ItemStack stack, int tintIndex) {
        if (tintIndex != 0) return -1;
        if (stack.getItem() instanceof net.minecraft.world.item.BlockItem bi
                && bi.getBlock() instanceof ShopBlock sb) {
            return sb.tier().colorRgb();
        }
        return ShopTier.GOLD.colorRgb();
    }
}
