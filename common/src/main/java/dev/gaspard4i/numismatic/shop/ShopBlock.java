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

    // Hitbox ported verbatim from wisp-forest/numismatic-overhaul ShopBlock :
    // main central pillar + top display plate + 4 corner legs.
    private static final VoxelShape MAIN_PILLAR = Block.box(1, 0, 1, 14, 8, 14);
    private static final VoxelShape PLATE = Block.box(0, 8, 0, 16, 12, 16);
    private static final VoxelShape LEG_NE = Block.box(13, 0, 0, 16, 8, 3);
    private static final VoxelShape LEG_NW = Block.box(0, 0, 0, 3, 8, 3);
    private static final VoxelShape LEG_SW = Block.box(0, 0, 13, 3, 8, 16);
    private static final VoxelShape LEG_SE = Block.box(13, 0, 13, 16, 8, 16);
    private static final VoxelShape SHAPE = net.minecraft.world.phys.shapes.Shapes.or(
            MAIN_PILLAR, PLATE, LEG_NE, LEG_NW, LEG_SW, LEG_SE);

    private final ShopTier tier;

    public ShopBlock(Properties properties) {
        this(properties, ShopTier.GOLD);
    }

    public ShopBlock(Properties properties, ShopTier tier) {
        super(properties);
        this.tier = tier;
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    public ShopTier tier() { return tier; }

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

    /** True when the tier is {@link ShopTier#ADMIN}. */
    public boolean isAdminVariant() {
        return tier.isAdmin();
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

    @Nullable
    @Override
    public <T extends BlockEntity> net.minecraft.world.level.block.entity.BlockEntityTicker<T> getTicker(
            Level level, BlockState state,
            net.minecraft.world.level.block.entity.BlockEntityType<T> type) {
        return net.minecraft.world.level.block.BaseEntityBlock.createTickerHelper(
                type, ShopRegistry.SHOP_BLOCK_ENTITY.get(), ShopBlockEntity::tick);
    }
}
