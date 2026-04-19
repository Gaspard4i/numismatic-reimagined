package dev.gaspard4i.numismatic.block;

import dev.gaspard4i.numismatic.currency.Currency;
import dev.gaspard4i.numismatic.currency.CurrencyResolver;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Block entity for piggy banks. Stores a total currency value (in bronze units).
 * No inventory/GUI — money is inserted via right-click and recovered by breaking.
 * Each piggy bank variant has a maximum capacity.
 */
public class PiggyBankBlockEntity extends BlockEntity {

    private static final String TAG_VALUE = "StoredValue";

    // Max capacities per piggy bank type (in bronze)
    // Base: 1 Gold - 1 = 9,999 bronze (max value just under next unit)
    public static final long BASE_MAX_VALUE = Currency.GOLD.getValue() - 1;
    // Golden: 1 Netherite - 1 = 999,999 bronze
    public static final long GOLDEN_MAX_VALUE = Currency.NETHERITE.getValue() - 1;
    // Netherite: 1 Star Coin (1000 Netherite) - 1 = 999,999,999 bronze
    public static final long NETHERITE_MAX_VALUE = 1000L * Currency.NETHERITE.getValue() - 1;

    private final PiggyBankAccount account;
    private boolean silkTouched = false;
    private boolean contentsDropped = false;

    public PiggyBankBlockEntity(BlockPos pos, BlockState state) {
        super(NumismaticBlocks.PIGGY_BANK_BLOCK_ENTITY.get(), pos, state);
        this.account = new PiggyBankAccount(BASE_MAX_VALUE);
    }

    protected PiggyBankBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, long maxValue) {
        super(type, pos, state);
        this.account = new PiggyBankAccount(maxValue);
    }

    public long getStoredValue() {
        return account.getStored();
    }

    public long getMaxValue() {
        return account.getMaxValue();
    }

    public long getRemainingCapacity() {
        return account.getRemainingCapacity();
    }

    public boolean isFull() {
        return account.isFull();
    }

    /**
     * Adds value to the piggy bank. Returns the amount actually added (may be less if capacity is reached).
     */
    public long addValue(long amount) {
        long added = account.add(amount);
        if (added > 0) setChanged();
        return added;
    }

    public boolean isEmpty() {
        return account.isEmpty();
    }

    public boolean isSilkTouched() {
        return silkTouched;
    }

    public void setSilkTouched(boolean silkTouched) {
        this.silkTouched = silkTouched;
    }

    public boolean isContentsDropped() {
        return contentsDropped;
    }

    public void setContentsDropped(boolean contentsDropped) {
        this.contentsDropped = contentsDropped;
    }

    /**
     * Returns a redstone signal (0-15) based on stored value relative to max capacity.
     */
    public int getRedstoneSignal() {
        return account.getRedstoneSignal();
    }

    public Component getDisplayName() {
        return Component.translatable("block.numismatic_reimagined.piggy_bank");
    }

    /**
     * Returns a formatted string of the stored value.
     */
    public String getFormattedValue() {
        return CurrencyResolver.formatValue(account.getStored());
    }

    /**
     * Returns a formatted string of the max value.
     */
    public String getFormattedMaxValue() {
        return CurrencyResolver.formatValue(account.getMaxValue());
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putLong(TAG_VALUE, account.getStored());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        account.setStoredFromPersistence(tag.getLong(TAG_VALUE));
    }
}
