package dev.gaspard4i.numismatic.shop;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Admin variant of the shop block. Same hitbox, different texture; backed by
 * its own block entity type so loot tables and recipes can target it
 * independently.
 */
public class AdminShopBlock extends ShopBlock {

    public AdminShopBlock(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isAdminVariant() {
        return true;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ShopBlockEntity(ShopRegistry.ADMIN_SHOP_BLOCK_ENTITY.get(), pos, state);
    }
}
