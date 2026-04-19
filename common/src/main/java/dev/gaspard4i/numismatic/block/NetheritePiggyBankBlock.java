package dev.gaspard4i.numismatic.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class NetheritePiggyBankBlock extends PreciousPiggyBankBlock {

    public NetheritePiggyBankBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new NetheritePiggyBankBlockEntity(pos, state);
    }
}
