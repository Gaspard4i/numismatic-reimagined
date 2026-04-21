package dev.gaspard4i.numismatic.block;

import com.mojang.serialization.MapCodec;
import dev.gaspard4i.numismatic.item.CoinItem;
import dev.gaspard4i.numismatic.item.MoneyBagItem;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Stream;

public class PiggyBankBlock extends BaseEntityBlock {

    public static final MapCodec<PiggyBankBlock> CODEC = simpleCodec(p -> new PiggyBankBlock(p, PiggyBankTier.BASE));
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

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
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level,
                                              BlockPos pos, Player player, InteractionHand hand,
                                              BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof PiggyBankBlockEntity piggy)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (player.isShiftKeyDown()) {
            if (!level.isClientSide()) {
                dumpHeldStackIntoPiggy(player, piggy, level, pos);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide());
        }

        long unit = unitValueOf(stack);
        if (unit <= 0 && !(stack.getItem() instanceof MoneyBagItem)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (!level.isClientSide()) {
            if (piggy.account().isFull()) {
                player.displayClientMessage(
                        Component.translatable("block.numismatic_reimagined.piggy_bank.full",
                                        String.format("%,d", piggy.account().cap()))
                                .withStyle(ChatFormatting.RED), true);
                return ItemInteractionResult.sidedSuccess(false);
            }

            if (unit <= 0) {
                stack.shrink(1);
                return ItemInteractionResult.sidedSuccess(false);
            }

            long inserted = piggy.account().tryDeposit(unit);
            long surplus = unit - inserted;
            stack.shrink(1);
            if (inserted > 0) piggy.markChanged();

            if (surplus > 0) {
                ItemStack refund = MoneyBagItem.createWithValue(surplus);
                if (!player.getInventory().add(refund)) {
                    player.drop(refund, false);
                }
            }

            level.playSound(null, pos, SoundEvents.CHAIN_PLACE, SoundSource.BLOCKS,
                    0.6f, 1.2f + level.getRandom().nextFloat() * 0.3f);
            player.displayClientMessage(
                    Component.translatable("block.numismatic_reimagined.piggy_bank.inserted",
                                    String.format("%,d", inserted))
                            .withStyle(ChatFormatting.GREEN), true);
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof PiggyBankBlockEntity piggy)) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide()) {
            if (piggy.account().isEmpty()) {
                player.displayClientMessage(
                        Component.translatable("block.numismatic_reimagined.piggy_bank.empty")
                                .withStyle(ChatFormatting.GRAY), true);
            } else {
                player.displayClientMessage(
                        Component.translatable("block.numismatic_reimagined.piggy_bank.contains",
                                        String.format("%,d / %,d",
                                                piggy.account().stored(),
                                                piggy.account().cap()))
                                .withStyle(ChatFormatting.GOLD), true);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    private static long unitValueOf(ItemStack stack) {
        if (stack.isEmpty()) return 0;
        if (stack.getItem() instanceof CoinItem coin) return coin.getCurrency().getValue();
        if (stack.getItem() instanceof MoneyBagItem) return MoneyBagItem.getValue(stack);
        return 0;
    }

    private static long stackValueOf(ItemStack stack) {
        if (stack.isEmpty()) return 0;
        if (stack.getItem() instanceof CoinItem coin) return coin.getStackValue(stack);
        if (stack.getItem() instanceof MoneyBagItem) return MoneyBagItem.getValue(stack);
        return 0;
    }

    private static void dumpHeldStackIntoPiggy(Player player, PiggyBankBlockEntity piggy,
                                               Level level, BlockPos pos) {
        ItemStack held = player.getMainHandItem();
        long total = stackValueOf(held);
        if (!(held.getItem() instanceof CoinItem) && !(held.getItem() instanceof MoneyBagItem)) {
            player.displayClientMessage(
                    Component.translatable("block.numismatic_reimagined.piggy_bank.no_coins_in_inventory")
                            .withStyle(ChatFormatting.GRAY), true);
            return;
        }
        held.shrink(held.getCount());
        if (total <= 0) return;

        long inserted = piggy.account().tryDeposit(total);
        long surplus = total - inserted;
        if (inserted > 0) piggy.markChanged();

        if (surplus > 0) {
            ItemStack refund = MoneyBagItem.createWithValue(surplus);
            if (!player.getInventory().add(refund)) player.drop(refund, false);
        }

        level.playSound(null, pos, SoundEvents.CHAIN_PLACE, SoundSource.BLOCKS,
                0.8f, 1.0f + level.getRandom().nextFloat() * 0.3f);
        player.displayClientMessage(
                Component.translatable("block.numismatic_reimagined.piggy_bank.inserted",
                                String.format("%,d", inserted))
                        .withStyle(ChatFormatting.GREEN), true);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PiggyBankBlockEntity(
                PiggyBankBlocks.blockEntityTypeFor(tier), pos, state, tier);
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof PiggyBankBlockEntity piggy) {
            ItemStack tool = player.getMainHandItem();
            var silkTouch = level.registryAccess()
                    .lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT)
                    .getOrThrow(Enchantments.SILK_TOUCH);
            boolean silk = EnchantmentHelper.getItemEnchantmentLevel(silkTouch, tool) > 0;

            if (silk) {
                piggy.setSilkTouched(true);
                ItemStack drop = new ItemStack(this);
                if (!piggy.account().isEmpty()) {
                    CompoundTag beTag = new CompoundTag();
                    beTag.putLong("Stored", piggy.account().stored());
                    var beTypeId = net.minecraft.core.registries.BuiltInRegistries.BLOCK_ENTITY_TYPE
                            .getKey(PiggyBankBlocks.blockEntityTypeFor(tier));
                    if (beTypeId != null) beTag.putString("id", beTypeId.toString());
                    drop.set(net.minecraft.core.component.DataComponents.BLOCK_ENTITY_DATA,
                            net.minecraft.world.item.component.CustomData.of(beTag));
                }
                popResource(level, pos, drop);
            } else if (!piggy.account().isEmpty()) {
                long contents = piggy.account().crush();
                piggy.setContentsDropped(true);
                popResource(level, pos, MoneyBagItem.createWithValue(contents));
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
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

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) { return true; }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof PiggyBankBlockEntity piggy) {
            return piggy.getRedstoneSignal();
        }
        return 0;
    }
}
