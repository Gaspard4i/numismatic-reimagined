package dev.gaspard4i.numismatic.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class PiggyBankBlockEntity extends BlockEntity {

    private final PiggyBankAccount account;

    public PiggyBankBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, PiggyBankTier tier) {
        super(type, pos, state);
        this.account = new PiggyBankAccount(tier);
    }

    public PiggyBankAccount account() { return account; }

    public long deposit(long amount) {
        long accepted = account.tryDeposit(amount);
        if (accepted > 0) setChanged();
        return accepted;
    }

    public long crush() {
        long prev = account.crush();
        if (prev > 0) setChanged();
        return prev;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putLong("Stored", account.stored());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        long stored = tag.getLong("Stored");
        // Reset via crush + redeposit to respect cap invariants.
        account.crush();
        account.tryDeposit(stored);
    }
}
