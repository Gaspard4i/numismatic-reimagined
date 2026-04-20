package dev.gaspard4i.numismatic.shop;

import dev.gaspard4i.numismatic.item.MoneyBagItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.BlockGetter;
import org.jetbrains.annotations.Nullable;

/**
 * The shop block. Right-click opens a vanilla 3-row chest interface backed
 * by the underlying {@link ShopBlockEntity}.
 */
public class ShopBlock extends BaseEntityBlock {

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    // The shop model only fills the bottom 12/16 of the block (4 legs +
    // top shelf). Match the hitbox so the player can't bump into invisible
    // air above the block.
    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 12, 16);

    public ShopBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    /** True for {@link AdminShopBlock}; subclasses override. */
    public boolean isAdminVariant() {
        return false;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (level.isClientSide()) return;
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof ShopBlockEntity shop)) return;
        shop.setAdmin(isAdminVariant());
        if (!isAdminVariant() && placer instanceof Player p) {
            shop.setOwner(p.getUUID());
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof ShopBlockEntity shop) {
            // Owner + no-sneak → owner UI (ShopScreen).
            // Owner + sneak OR non-owner → vanilla merchant trade UI (like the original mod).
            if (shop.canEdit(player) && !player.isShiftKeyDown()) {
                player.openMenu(shop);
                if (player instanceof net.minecraft.server.level.ServerPlayer sp) {
                    dev.gaspard4i.numismatic.network.NumismaticNetworking.syncShopState(sp, shop);
                }
            } else {
                ShopMerchant merchant = new ShopMerchant(shop);
                merchant.refreshOffers();
                merchant.setTradingPlayer(player);
                merchant.openTradingScreen(player,
                        net.minecraft.network.chat.Component.translatable(
                                shop.isAdmin()
                                        ? "block.numismatic_reimagined.admin_shop_block"
                                        : "block.numismatic_reimagined.shop_block"),
                        0);
            }
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof ShopBlockEntity shop && !shop.isAdmin()) {
                Containers.dropContents(level, pos, shop);
                long revenue = shop.withdrawRevenue();
                if (revenue > 0) {
                    Containers.dropItemStack(level,
                            pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                            MoneyBagItem.createWithValue(revenue));
                }
                level.updateNeighbourForOutputSignal(pos, this);
            }
        }
        super.onRemove(state, level, pos, newState, moved);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ShopBlockEntity(ShopRegistry.SHOP_BLOCK_ENTITY.get(), pos, state);
    }
}
