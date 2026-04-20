package dev.gaspard4i.numismatic.block;

import com.mojang.serialization.MapCodec;
import dev.gaspard4i.numismatic.currency.PlayerCurrencyManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class PiggyBankBlock extends BaseEntityBlock {

    public static final MapCodec<PiggyBankBlock> CODEC = simpleCodec(p -> new PiggyBankBlock(p, PiggyBankTier.BASE));

    private final PiggyBankTier tier;

    public PiggyBankBlock(Properties properties, PiggyBankTier tier) {
        super(properties);
        this.tier = tier;
    }

    public PiggyBankTier tier() { return tier; }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }

    @Override
    public RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }

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
