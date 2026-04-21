package dev.gaspard4i.numismatic.shop;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Shop block with an upstream-accurate hitbox : a main pillar + top plate + 4 corner legs.
 * Port from wisp-forest/numismatic-overhaul (MIT).
 */
public class ShopBlock extends Block {

    private static final VoxelShape MAIN_PILLAR = box(1, 0, 1, 14, 8, 14);
    private static final VoxelShape PLATE       = box(0, 8, 0, 16, 12, 16);
    private static final VoxelShape PILLAR_1    = box(13, 0, 0, 16, 8, 3);
    private static final VoxelShape PILLAR_2    = box(0, 0, 0, 3, 8, 3);
    private static final VoxelShape PILLAR_3    = box(0, 0, 13, 3, 8, 16);
    private static final VoxelShape PILLAR_4    = box(13, 0, 13, 16, 8, 16);

    private static final VoxelShape SHAPE =
            Shapes.or(MAIN_PILLAR, PLATE, PILLAR_1, PILLAR_2, PILLAR_3, PILLAR_4);

    private final ShopTier tier;

    public ShopBlock(BlockBehaviour.Properties properties, ShopTier tier) {
        super(properties);
        this.tier = tier;
    }

    public ShopTier tier() { return tier; }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }
}
