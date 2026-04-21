package dev.gaspard4i.numismatic.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.WorldlyContainer;
import org.jetbrains.annotations.Nullable;

public class PiggyBankBlockEntity extends BlockEntity implements WorldlyContainer, MenuProvider {

    private static final int SIZE = 3;
    private static final int[] ALL_SLOTS = {0, 1, 2};

    private final NonNullList<ItemStack> items = NonNullList.withSize(SIZE, ItemStack.EMPTY);
    private final PiggyBankAccount account;

    public PiggyBankBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, PiggyBankTier tier) {
        super(type, pos, state);
        this.account = new PiggyBankAccount(tier);
    }

    public PiggyBankAccount account() { return account; }

    public long crush() {
        long coinsValue = 0;
        for (ItemStack stack : items) {
            if (stack.getItem() instanceof dev.gaspard4i.numismatic.item.CoinItem coin) {
                coinsValue += coin.getStackValue(stack);
            }
        }
        items.clear();
        long prev = account.crush();
        if (prev > 0 || coinsValue > 0) setChanged();
        return prev + coinsValue;
    }

    // === Container / WorldlyContainer ===

    @Override public int getContainerSize() { return SIZE; }

    @Override
    public boolean isEmpty() {
        for (ItemStack s : items) if (!s.isEmpty()) return false;
        return true;
    }

    @Override public ItemStack getItem(int slot) { return items.get(slot); }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack result = ContainerHelper.removeItem(items, slot, amount);
        if (!result.isEmpty()) setChanged();
        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        items.set(slot, stack);
        if (stack.getCount() > getMaxStackSize()) stack.setCount(getMaxStackSize());
        setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        if (level == null || level.getBlockEntity(worldPosition) != this) return false;
        return player.distanceToSqr(
                worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5) <= 64.0;
    }

    @Override public void clearContent() { items.clear(); }

    @Override public int[] getSlotsForFace(net.minecraft.core.Direction side) { return ALL_SLOTS; }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable net.minecraft.core.Direction direction) {
        return canPlaceItem(slot, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, net.minecraft.core.Direction direction) {
        return true;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if (!(stack.getItem() instanceof dev.gaspard4i.numismatic.item.CoinItem coin)) return false;
        return switch (slot) {
            case 0 -> coin.getCurrency() == dev.gaspard4i.numismatic.currency.Currency.BRONZE;
            case 1 -> coin.getCurrency() == dev.gaspard4i.numismatic.currency.Currency.SILVER;
            case 2 -> coin.getCurrency() == dev.gaspard4i.numismatic.currency.Currency.GOLD;
            default -> false;
        };
    }

    // === MenuProvider ===

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.numismatic_reimagined.piggy_bank");
    }

    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player player) {
        return new PiggyBankMenu(syncId, playerInventory, this,
                ContainerLevelAccess.create(level, worldPosition));
    }

    // === Persistence ===

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putLong("Stored", account.stored());
        ContainerHelper.saveAllItems(tag, items, registries);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        long stored = tag.getLong("Stored");
        account.crush();
        account.tryDeposit(stored);
        items.clear();
        ContainerHelper.loadAllItems(tag, items, registries);
    }
}
