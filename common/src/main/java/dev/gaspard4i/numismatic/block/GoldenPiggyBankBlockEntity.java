package dev.gaspard4i.numismatic.block;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;

public class GoldenPiggyBankBlockEntity extends PiggyBankBlockEntity {

    public GoldenPiggyBankBlockEntity(BlockPos pos, BlockState state) {
        super(NumismaticBlocks.GOLDEN_PIGGY_BANK_BLOCK_ENTITY.get(), pos, state, GOLDEN_MAX_VALUE);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.numismatic_reimagined.golden_piggy_bank");
    }
}
