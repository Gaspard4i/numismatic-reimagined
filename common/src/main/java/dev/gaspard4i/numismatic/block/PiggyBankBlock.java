package dev.gaspard4i.numismatic.block;

import dev.gaspard4i.numismatic.currency.CurrencyHelper;
import dev.gaspard4i.numismatic.item.CoinItem;
import dev.gaspard4i.numismatic.item.MoneyBagItem;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.BlockGetter;
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
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Piggy bank block. Right-click with coins or money bags to insert money.
 * Break the block to recover the contents (drops as money bags).
 * All variants can be recovered with silk touch pickaxe (contents preserved).
 */
public class PiggyBankBlock extends BaseEntityBlock {

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    // Hitboxes ported verbatim from wisp-forest/numismatic-overhaul
    // PiggyBankBlock (body + snout + 4 feet, rotated per FACING).
    private static final VoxelShape NORTH_SHAPE = Shapes.or(
            Block.box(7, 2, 4, 9, 4, 5),    // snout
            Block.box(5, 1, 5, 11, 6, 11),  // body
            Block.box(5, 0, 5, 6, 1, 7),    // NW foot
            Block.box(5, 0, 9, 6, 1, 11),   // SW foot
            Block.box(10, 0, 9, 11, 1, 11), // SE foot
            Block.box(10, 0, 5, 11, 1, 7)   // NE foot
    );
    private static final VoxelShape SOUTH_SHAPE = Shapes.or(
            Block.box(7, 2, 11, 9, 4, 12),
            Block.box(5, 1, 5, 11, 6, 11),
            Block.box(10, 0, 9, 11, 1, 11),
            Block.box(10, 0, 5, 11, 1, 7),
            Block.box(5, 0, 5, 6, 1, 7),
            Block.box(5, 0, 9, 6, 1, 11)
    );
    private static final VoxelShape EAST_SHAPE = Shapes.or(
            Block.box(11, 2, 7, 12, 4, 9),
            Block.box(5, 1, 5, 11, 6, 11),
            Block.box(5, 0, 5, 7, 1, 6),
            Block.box(5, 0, 10, 7, 1, 11),
            Block.box(9, 0, 10, 11, 1, 11),
            Block.box(9, 0, 5, 11, 1, 6)
    );
    private static final VoxelShape WEST_SHAPE = Shapes.or(
            Block.box(4, 2, 7, 5, 4, 9),
            Block.box(5, 1, 5, 11, 6, 11),
            Block.box(9, 0, 10, 11, 1, 11),
            Block.box(9, 0, 5, 11, 1, 6),
            Block.box(5, 0, 5, 7, 1, 6),
            Block.box(5, 0, 10, 7, 1, 11)
    );

    private static final Map<Direction, VoxelShape> SHAPES = buildShapes();

    private static Map<Direction, VoxelShape> buildShapes() {
        Map<Direction, VoxelShape> map = new EnumMap<>(Direction.class);
        map.put(Direction.NORTH, NORTH_SHAPE);
        map.put(Direction.SOUTH, SOUTH_SHAPE);
        map.put(Direction.EAST, EAST_SHAPE);
        map.put(Direction.WEST, WEST_SHAPE);
        return map;
    }

    public PiggyBankBlock(Properties properties) {
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
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES.get(state.getValue(FACING));
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                  InteractionHand hand, BlockHitResult hit) {
        ItemStack heldItem = player.getItemInHand(hand);

        // Shift-click: dump the entire inventory (coins + money bags) into the piggy bank
        if (player.isShiftKeyDown()) {
            if (!level.isClientSide()) {
                BlockEntity be = level.getBlockEntity(pos);
                if (be instanceof PiggyBankBlockEntity piggyBank) {
                    dumpInventoryIntoPiggyBank(player, piggyBank, level, pos);
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }

        // Only interact with coins or money bags
        long value = getMoneyValue(heldItem);
        if (value <= 0 && !(heldItem.getItem() instanceof MoneyBagItem)) {
            // Show stored value on empty hand right-click
            if (heldItem.isEmpty() && !level.isClientSide()) {
                BlockEntity be = level.getBlockEntity(pos);
                if (be instanceof PiggyBankBlockEntity piggyBank) {
                    if (piggyBank.isEmpty()) {
                        player.displayClientMessage(
                                Component.translatable("block.numismatic_reimagined.piggy_bank.empty")
                                        .withStyle(ChatFormatting.GRAY), true);
                    } else {
                        player.displayClientMessage(
                                Component.translatable("block.numismatic_reimagined.piggy_bank.contains",
                                        String.format("%,d / %,d", piggyBank.getStoredValue(), piggyBank.getMaxValue()))
                                        .withStyle(ChatFormatting.GOLD), true);
                    }
                }
            }
            return heldItem.isEmpty() ? InteractionResult.sidedSuccess(level.isClientSide()) : InteractionResult.PASS;
        }

        if (!level.isClientSide()) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof PiggyBankBlockEntity piggyBank) {
                if (piggyBank.isFull()) {
                    player.displayClientMessage(
                            Component.translatable("block.numismatic_reimagined.piggy_bank.full",
                                    String.format("%,d", piggyBank.getMaxValue()))
                                    .withStyle(ChatFormatting.RED), true);
                    return InteractionResult.sidedSuccess(level.isClientSide());
                }

                // Determine unit value of ONE consumed item (1 coin or 1 money bag)
                long unitValue;
                if (heldItem.getItem() instanceof CoinItem coinItem) {
                    unitValue = coinItem.getCurrency().getValue();
                } else if (heldItem.getItem() instanceof MoneyBagItem) {
                    unitValue = MoneyBagItem.getValue(heldItem);
                } else {
                    return InteractionResult.PASS;
                }

                if (unitValue <= 0) {
                    // Empty money bag — consume it without effect
                    if (heldItem.getItem() instanceof MoneyBagItem) {
                        heldItem.shrink(1);
                    }
                    return InteractionResult.sidedSuccess(level.isClientSide());
                }

                // Consume exactly 1 unit (1 coin or 1 bag) and split between piggy bank + refund
                long inserted = piggyBank.addValue(unitValue);
                long surplus = unitValue - inserted;

                // Consume the item unit
                heldItem.shrink(1);

                // Return surplus as a money bag
                if (surplus > 0) {
                    ItemStack refundBag = MoneyBagItem.createWithValue(surplus);
                    if (!player.getInventory().add(refundBag)) {
                        player.drop(refundBag, false);
                    }
                }

                // Play coin insert sound
                level.playSound(null, pos, SoundEvents.CHAIN_PLACE, SoundSource.BLOCKS,
                        0.6f, 1.2f + level.getRandom().nextFloat() * 0.3f);

                // Show how much was inserted
                player.displayClientMessage(
                        Component.translatable("block.numismatic_reimagined.piggy_bank.inserted",
                                String.format("%,d", inserted))
                                .withStyle(ChatFormatting.GREEN), true);
            }
        }

        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    /**
     * Inserts the entire held stack (coins or money bags) into the piggy bank.
     * Excess value (once the piggy bank is full) is returned as a money bag.
     */
    public static void dumpInventoryIntoPiggyBank(Player player, PiggyBankBlockEntity piggyBank, Level level, BlockPos pos) {
        ItemStack heldItem = player.getMainHandItem();
        if (heldItem.isEmpty()) {
            player.displayClientMessage(
                    Component.translatable("block.numismatic_reimagined.piggy_bank.no_coins_in_inventory")
                            .withStyle(ChatFormatting.GRAY), true);
            return;
        }

        long totalValue;
        if (heldItem.getItem() instanceof CoinItem coinItem) {
            totalValue = coinItem.getStackValue(heldItem);
        } else if (heldItem.getItem() instanceof MoneyBagItem) {
            totalValue = MoneyBagItem.getValue(heldItem);
        } else {
            player.displayClientMessage(
                    Component.translatable("block.numismatic_reimagined.piggy_bank.no_coins_in_inventory")
                            .withStyle(ChatFormatting.GRAY), true);
            return;
        }

        // Always consume the held stack (even empty money bags)
        heldItem.shrink(heldItem.getCount());

        if (totalValue <= 0) {
            return;
        }

        long inserted = piggyBank.addValue(totalValue);
        long surplus = totalValue - inserted;

        if (surplus > 0) {
            ItemStack refundBag = MoneyBagItem.createWithValue(surplus);
            if (!player.getInventory().add(refundBag)) {
                player.drop(refundBag, false);
            }
        }

        level.playSound(null, pos, SoundEvents.CHAIN_PLACE, SoundSource.BLOCKS,
                0.8f, 1.0f + level.getRandom().nextFloat() * 0.3f);

        player.displayClientMessage(
                Component.translatable("block.numismatic_reimagined.piggy_bank.inserted",
                        String.format("%,d", inserted))
                        .withStyle(ChatFormatting.GREEN), true);
    }

    /**
     * Gets the bronze value of a held item if it's a coin or money bag.
     */
    private long getMoneyValue(ItemStack stack) {
        if (stack.isEmpty()) return 0;
        if (stack.getItem() instanceof CoinItem coinItem) {
            return coinItem.getStackValue(stack);
        }
        if (stack.getItem() instanceof MoneyBagItem) {
            return MoneyBagItem.getValue(stack);
        }
        return 0;
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof PiggyBankBlockEntity piggyBank && !level.isClientSide()) {
            ItemStack tool = player.getMainHandItem();
            boolean hasSilkTouch = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH, tool) > 0;

            if (hasSilkTouch && !piggyBank.isEmpty()) {
                // Silk touch: drop block with contents preserved
                piggyBank.setSilkTouched(true);

                ItemStack drop = new ItemStack(this);
                CompoundTag beTag = new CompoundTag();
                piggyBank.saveAdditional(beTag);
                drop.addTagElement("BlockEntityTag", beTag);
                popResource(level, pos, drop);
            } else if (hasSilkTouch && piggyBank.isEmpty()) {
                // Silk touch on empty piggy bank: just drop the block
                piggyBank.setSilkTouched(true);
                popResource(level, pos, new ItemStack(this));
            } else {
                // No silk touch: drop contents as money bags, block is destroyed
                if (!piggyBank.isEmpty()) {
                    CurrencyHelper.dropAsCoins(level, pos, piggyBank.getStoredValue());
                    piggyBank.setContentsDropped(true);
                }
            }
        }
        super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof PiggyBankBlockEntity piggyBank) {
                // If not silk touched and contents not already dropped (e.g., explosion), drop as money bags
                if (!piggyBank.isSilkTouched() && !piggyBank.isContentsDropped() && !piggyBank.isEmpty()) {
                    CurrencyHelper.dropAsCoins(level, pos, piggyBank.getStoredValue());
                }
                level.updateNeighbourForOutputSignal(pos, this);
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, net.minecraft.world.level.storage.loot.LootParams.Builder builder) {
        // Drops handled manually in playerWillDestroy / onRemove
        return Collections.emptyList();
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof PiggyBankBlockEntity piggyBank) {
            return piggyBank.getRedstoneSignal();
        }
        return 0;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PiggyBankBlockEntity(pos, state);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }
}
