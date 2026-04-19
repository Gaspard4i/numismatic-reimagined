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
import org.jetbrains.annotations.Nullable;

/**
 * Player-owned shop block. Owner is set on placement. Right-click opens
 * either the owner UI (offers / stock / client tabs) or the buyer UI
 * (client tab only) depending on the interacting player.
 *
 * <p>Indéplaçable par piston. Drops stock + revenu (as money bag) when
 * destroyed, regardless of who breaks it.
 */
public class ShopBlock extends BaseEntityBlock {

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public ShopBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    /**
     * True if this block variant is the admin shop (no owner, infinite stock,
     * OP-only edit). Subclasses override.
     */
    public boolean isAdminVariant() {
        return false;
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

    // Indéplaçable par piston: set via Block properties (.pushReaction(PushReaction.BLOCK))
    // is preferred in 1.20.1 — done in NumismaticShop registration.

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    /**
     * On placement, record the placer's UUID as the owner of this shop. For
     * admin shops, the owner field stays null and {@code isAdmin} is set.
     */
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
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof ShopBlockEntity shop)) return InteractionResult.PASS;

        ShopMenuMode initialMode = shop.canEdit(player) ? ShopMenuMode.OFFERS : ShopMenuMode.CLIENT;
        ShopMenuOpener.openFor(player, shop, initialMode);
        return InteractionResult.CONSUME;
    }

    /**
     * Drop stock items + accumulated revenue (as money bag) on destruction,
     * regardless of who broke the block. Admin shops drop nothing (no stock,
     * no revenue).
     */
    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide()) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof ShopBlockEntity shop && !shop.isAdmin()) {
                Containers.dropContents(level, pos, shop.getStock());
                long revenue = shop.withdrawRevenue();
                if (revenue > 0) {
                    ItemStack bag = MoneyBagItem.createWithValue(revenue);
                    Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, bag);
                }
            }
        }
        super.playerWillDestroy(level, pos, state, player);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ShopBlockEntity(NumismaticShop.SHOP_BLOCK_ENTITY.get(), pos, state);
    }
}
