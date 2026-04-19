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

    private long storedValue = 0;
    private long maxValue = BASE_MAX_VALUE;
    private boolean silkTouched = false;
    private boolean contentsDropped = false;

    public PiggyBankBlockEntity(BlockPos pos, BlockState state) {
        super(NumismaticBlocks.PIGGY_BANK_BLOCK_ENTITY.get(), pos, state);
    }

    protected PiggyBankBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, long maxValue) {
        super(type, pos, state);
        this.maxValue = maxValue;
    }

    public long getStoredValue() {
        return storedValue;
    }

    public long getMaxValue() {
        return maxValue;
    }

    public long getRemainingCapacity() {
        return maxValue - storedValue;
    }

    public boolean isFull() {
        return storedValue >= maxValue;
    }

    /**
     * Adds value to the piggy bank. Returns the amount actually added (may be less if capacity is reached).
     */
    public long addValue(long amount) {
        if (amount <= 0) return 0;
        long remaining = maxValue - storedValue;
        long toAdd = Math.min(amount, remaining);
        if (toAdd > 0) {
            storedValue += toAdd;
            setChanged();
        }
        return toAdd;
    }

    public boolean isEmpty() {
        return storedValue == 0;
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
        if (storedValue == 0) return 0;
        if (storedValue >= maxValue) return 15;
        return 1 + (int) (14.0 * storedValue / maxValue);
    }

    public Component getDisplayName() {
        return Component.translatable("block.numismatic_reimagined.piggy_bank");
    }

    /**
     * Returns a formatted string of the stored value.
     */
    public String getFormattedValue() {
        return CurrencyResolver.formatValue(storedValue);
    }

    /**
     * Returns a formatted string of the max value.
     */
    public String getFormattedMaxValue() {
        return CurrencyResolver.formatValue(maxValue);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putLong(TAG_VALUE, storedValue);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        storedValue = tag.getLong(TAG_VALUE);
    }
}
