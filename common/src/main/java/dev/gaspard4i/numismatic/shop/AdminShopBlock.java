package dev.gaspard4i.numismatic.shop;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Server/admin variant of the shop. No owner, infinite stock, OP-only edit.
 * Not craftable — only obtainable via creative or {@code /give}.
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
        ShopBlockEntity be = new ShopBlockEntity(NumismaticShop.ADMIN_SHOP_BLOCK_ENTITY.get(), pos, state);
        be.setAdmin(true);
        return be;
    }
}
