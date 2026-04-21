package dev.gaspard4i.numismatic.request;

import dev.gaspard4i.numismatic.shop.ShopBlock;
import dev.gaspard4i.numismatic.shop.ShopTier;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Request board shares the shop's model/texture/hitbox family (user request : "same as a bronze shop").
 * Extends {@link ShopBlock} so it inherits the 6-piece VoxelShape (main pillar + plate + 4 legs).
 * The {@code BRONZE} tier is bound purely for consistency ; request board behavior is independent.
 */
public class RequestBoardBlock extends ShopBlock {

    public RequestBoardBlock(BlockBehaviour.Properties properties) {
        super(properties, ShopTier.BRONZE);
    }
}
