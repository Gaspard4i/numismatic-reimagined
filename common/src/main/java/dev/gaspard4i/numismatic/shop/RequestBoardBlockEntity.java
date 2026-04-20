package dev.gaspard4i.numismatic.shop;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Backing block entity for a request board (reverse shop). Holds:
 * <ul>
 *   <li>a 9-slot internal inventory receiving fulfilled items</li>
 *   <li>the pool of funds (long, bronze)</li>
 *   <li>the list of {@link RequestOffer}s</li>
 *   <li>the owner's UUID</li>
 * </ul>
 */
public class RequestBoardBlockEntity extends BlockEntity implements Container {

    public static final int INVENTORY_SIZE = 9;

    private static final String TAG_ITEMS = "Items";
    private static final String TAG_OWNER = "Owner";
    private static final String TAG_OFFERS = "Offers";
    private static final String TAG_FUNDS = "Funds";

    private final NonNullList<ItemStack> items = NonNullList.withSize(INVENTORY_SIZE, ItemStack.EMPTY);
    @Nullable private UUID owner;
    private RequestOfferList offers = new RequestOfferList();
    private long funds = 0;

    public RequestBoardBlockEntity(BlockPos pos, BlockState state) {
        super(ShopRegistry.REQUEST_BOARD_BLOCK_ENTITY.get(), pos, state);
    }

    // --- Data accessors ---

    @Nullable public UUID getOwner() { return owner; }
    public void setOwner(@Nullable UUID owner) { this.owner = owner; setChanged(); }
    public boolean isOwner(Player player) { return owner != null && owner.equals(player.getUUID()); }

    public RequestOfferList getOffers() { return offers; }

    public long getFunds() { return funds; }
    public void addFunds(long amount) {
        if (amount <= 0) return;
        long next = funds + amount;
        if (next < funds) next = Long.MAX_VALUE;
        funds = next;
        setChanged();
    }
    public boolean tryWithdrawFunds(long amount) {
        if (amount <= 0 || funds < amount) return false;
        funds -= amount;
        setChanged();
        return true;
    }

    public NonNullList<ItemStack> getInternalItems() { return items; }

    // --- Container implementation ---

    @Override public int getContainerSize() { return INVENTORY_SIZE; }

    @Override
    public boolean isEmpty() {
        for (ItemStack s : items) if (!s.isEmpty()) return false;
        return true;
    }

    @Override public ItemStack getItem(int slot) {
        return slot >= 0 && slot < items.size() ? items.get(slot) : ItemStack.EMPTY;
    }
    @Override public ItemStack removeItem(int slot, int amount) {
        ItemStack out = ContainerHelper.removeItem(items, slot, amount);
        if (!out.isEmpty()) setChanged();
        return out;
    }
    @Override public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(items, slot);
    }
    @Override public void setItem(int slot, ItemStack stack) {
        if (slot >= 0 && slot < items.size()) items.set(slot, stack);
        setChanged();
    }
    @Override public boolean stillValid(Player player) {
        if (this.level == null || this.level.getBlockEntity(worldPosition) != this) return false;
        return player.distanceToSqr(worldPosition.getX() + 0.5,
                worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5) < 64.0;
    }
    @Override public void clearContent() { items.clear(); }

    // --- NBT ---

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ContainerHelper.saveAllItems(tag, items);
        if (owner != null) tag.putUUID(TAG_OWNER, owner);
        tag.put(TAG_OFFERS, offers.toTag());
        tag.putLong(TAG_FUNDS, funds);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        items.clear();
        ContainerHelper.loadAllItems(tag, items);
        owner = tag.hasUUID(TAG_OWNER) ? tag.getUUID(TAG_OWNER) : null;
        offers = RequestOfferList.fromTag(tag.getCompound(TAG_OFFERS));
        funds = tag.getLong(TAG_FUNDS);
    }

    /** First empty slot index, or -1 if the internal inventory is full. */
    public int firstEmptySlot() {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).isEmpty()) return i;
        }
        return -1;
    }
}
