package dev.gaspard4i.numismatic.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class PiggyBankBlockEntity extends BlockEntity {

    private final PiggyBankAccount account;
    private boolean silkTouched = false;
    private boolean contentsDropped = false;

    public PiggyBankBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, PiggyBankTier tier) {
        super(type, pos, state);
        this.account = new PiggyBankAccount(tier);
    }

    public PiggyBankAccount account() { return account; }

    public void markChanged() { setChanged(); }

    public boolean isSilkTouched() { return silkTouched; }
    public void setSilkTouched(boolean value) { this.silkTouched = value; }
    public boolean isContentsDropped() { return contentsDropped; }
    public void setContentsDropped(boolean value) { this.contentsDropped = value; }

    public int getRedstoneSignal() {
        if (account.isEmpty()) return 0;
        long cap = account.cap();
        if (cap <= 0) return 0;
        double ratio = (double) account.stored() / (double) cap;
        return 1 + (int) Math.floor(ratio * 14.0);
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
        account.crush();
        account.tryDeposit(stored);
    }
}
