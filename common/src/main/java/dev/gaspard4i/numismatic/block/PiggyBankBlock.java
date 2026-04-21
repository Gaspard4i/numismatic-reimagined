package dev.gaspard4i.numismatic.block;

import com.mojang.serialization.MapCodec;
import dev.gaspard4i.numismatic.currency.PlayerCurrencyManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Stream;

public class PiggyBankBlock extends BaseEntityBlock {

    public static final MapCodec<PiggyBankBlock> CODEC = simpleCodec(p -> new PiggyBankBlock(p, PiggyBankTier.BASE));
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    // Upstream-accurate hitbox (wisp-forest/numismatic-overhaul, MIT).
    private static final VoxelShape NORTH_SHAPE = Stream.of(
            Block.box(7, 2, 4, 9, 4, 5),
            Block.box(5, 1, 5, 11, 6, 11),
            Block.box(5, 0, 5, 6, 1, 7),
            Block.box(5, 0, 9, 6, 1, 11),
            Block.box(10, 0, 9, 11, 1, 11),
            Block.box(10, 0, 5, 11, 1, 7)
    ).reduce(Shapes::or).orElseThrow();

    private static final VoxelShape SOUTH_SHAPE = Stream.of(
            Block.box(7, 2, 11, 9, 4, 12),
            Block.box(5, 1, 5, 11, 6, 11),
            Block.box(10, 0, 9, 11, 1, 11),
            Block.box(10, 0, 5, 11, 1, 7),
            Block.box(5, 0, 5, 6, 1, 7),
            Block.box(5, 0, 9, 6, 1, 11)
    ).reduce(Shapes::or).orElseThrow();

    private static final VoxelShape EAST_SHAPE = Stream.of(
            Block.box(11, 2, 7, 12, 4, 9),
            Block.box(5, 1, 5, 11, 6, 11),
            Block.box(9, 0, 5, 11, 1, 6),
            Block.box(5, 0, 5, 7, 1, 6),
            Block.box(5, 0, 10, 7, 1, 11),
            Block.box(9, 0, 10, 11, 1, 11)
    ).reduce(Shapes::or).orElseThrow();

    private static final VoxelShape WEST_SHAPE = Stream.of(
            Block.box(4, 2, 7, 5, 4, 9),
            Block.box(5, 1, 5, 11, 6, 11),
            Block.box(5, 0, 10, 7, 1, 11),
            Block.box(9, 0, 10, 11, 1, 11),
            Block.box(9, 0, 5, 11, 1, 6),
            Block.box(5, 0, 5, 7, 1, 6)
    ).reduce(Shapes::or).orElseThrow();

    private final PiggyBankTier tier;

    public PiggyBankBlock(Properties properties, PiggyBankTier tier) {
        super(properties);
        this.tier = tier;
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    public PiggyBankTier tier() { return tier; }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case SOUTH -> SOUTH_SHAPE;
            case EAST -> EAST_SHAPE;
            case WEST -> WEST_SHAPE;
            default -> NORTH_SHAPE;
        };
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PiggyBankBlockEntity(
                PiggyBankBlocks.blockEntityTypeFor(tier), pos, state, tier);
    }

    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state,
                              @Nullable BlockEntity blockEntity, ItemStack tool) {
        super.playerDestroy(level, player, pos, state, blockEntity, tool);
        if (level instanceof ServerLevel serverLevel
                && blockEntity instanceof PiggyBankBlockEntity piggy
                && player instanceof ServerPlayer serverPlayer) {
            long contents = piggy.crush();
            if (contents > 0) {
                PlayerCurrencyManager.get(serverLevel).deposit(serverPlayer.getUUID(), contents);
                level.playSound(null, pos, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.BLOCKS, 0.8f, 1.2f);
            }
        }
    }

    @Override
    public void fallOn(Level level, BlockState state, BlockPos pos, net.minecraft.world.entity.Entity entity, float fallDistance) {
        super.fallOn(level, state, pos, entity, fallDistance);
        if (!(level instanceof ServerLevel serverLevel)) return;
        if (fallDistance < 3.0f) return;
        if (!(entity instanceof LivingEntity living)) return;
        if (living.getType().is(PiggyBankTags.VERY_HEAVY)) {
            serverLevel.destroyBlock(pos, true);
        }
    }
}
