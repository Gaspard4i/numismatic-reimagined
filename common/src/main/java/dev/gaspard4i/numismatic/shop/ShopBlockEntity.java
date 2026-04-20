package dev.gaspard4i.numismatic.shop;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.stream.IntStream;

/**
 * Backs both the player shop and the admin shop. Holds:
 * <ul>
 *   <li>27 stock slots (the items the owner sells)</li>
 *   <li>up to {@link OfferList#MAX_OFFERS} offers (price + qty per template)</li>
 *   <li>the owner's UUID (null for admin shops)</li>
 *   <li>accumulated bronze revenue, paid out via withdraw button</li>
 * </ul>
 */
public class ShopBlockEntity extends RandomizableContainerBlockEntity implements WorldlyContainer {

    /** Legacy constant kept for callers that do not know the tier yet.
     *  New code should call {@link #getContainerSize()} on the instance. */
    public static final int STOCK_SIZE = 27;

    private static final String TAG_OWNER = "Owner";
    private static final String TAG_IS_ADMIN = "IsAdmin";
    private static final String TAG_OFFERS = "OfferList";
    private static final String TAG_REVENUE = "Revenue";
    private static final String TAG_ALLOWS_TRANSFER = "AllowsTransfer";
    private static final String TAG_TIER = "Tier";

    private ShopTier tier;
    private NonNullList<ItemStack> items;
    @Nullable
    private UUID owner;
    private boolean isAdmin = false;
    private boolean allowsTransfer = false;
    private OfferList offers;
    private long accumulatedRevenue = 0;

    public ShopBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        // Default tier is determined by the block hosting this BE. If the
        // block implements TierProvider, use it; otherwise fall back to GOLD
        // (legacy shops that pre-date the tier system).
        ShopTier resolved = ShopTier.GOLD;
        if (state != null && state.getBlock() instanceof ShopBlock sb) {
            resolved = sb.tier();
        }
        this.tier = resolved;
        this.isAdmin = resolved.isAdmin();
        this.items = NonNullList.withSize(resolved.stockSize(), ItemStack.EMPTY);
        this.offers = new OfferList(resolved.maxOffers());
    }

    public ShopTier getTier() { return tier; }

    // --- Container ---

    @Override
    public int getContainerSize() {
        return items.size();
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> items) {
        this.items = items;
    }

    public NonNullList<ItemStack> getStock() {
        return items;
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable(isAdmin
                ? "block.numismatic_reimagined.admin_shop_block"
                : "block.numismatic_reimagined.shop_block");
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory playerInventory) {
        return new ShopMenu(containerId, playerInventory, this);
    }

    @Override
    public boolean stillValid(Player player) {
        if (this.level == null || this.level.getBlockEntity(this.worldPosition) != this) return false;
        return player.distanceToSqr(
                this.worldPosition.getX() + 0.5,
                this.worldPosition.getY() + 0.5,
                this.worldPosition.getZ() + 0.5) < 64.0;
    }

    // --- Owner / admin ---

    @Nullable
    public UUID getOwner() { return owner; }

    public void setOwner(@Nullable UUID owner) {
        this.owner = owner;
        setChanged();
    }

    public boolean isAdmin() { return isAdmin; }

    public void setAdmin(boolean admin) {
        this.isAdmin = admin;
        setChanged();
    }

    // --- Transfer (hopper input) toggle ---

    public boolean allowsTransfer() { return allowsTransfer; }

    public void setAllowsTransfer(boolean v) {
        if (this.allowsTransfer == v) return;
        this.allowsTransfer = v;
        setChanged();
    }

    public void toggleTransfer() { setAllowsTransfer(!allowsTransfer); }

    // --- WorldlyContainer (hopper IO) ---

    @Override
    public int[] getSlotsForFace(Direction side) {
        return allowsTransfer ? IntStream.range(0, items.size()).toArray() : new int[0];
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction dir) {
        return ShopTransferLogic.canHopperInsert(stack, offers.asList(), allowsTransfer);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction dir) {
        return false;
    }

    public boolean isOwner(Player player) {
        return !isAdmin && owner != null && owner.equals(player.getUUID());
    }

    public boolean canEdit(Player player) {
        if (isAdmin) return player.hasPermissions(2);
        return isOwner(player);
    }

    // --- Offers ---

    public OfferList getOffers() { return offers; }

    public void setOffers(OfferList offers) {
        this.offers = offers;
        setChanged();
    }

    // --- Revenue ---

    public long getAccumulatedRevenue() { return accumulatedRevenue; }

    public void addRevenue(long amount) {
        if (isAdmin || amount <= 0) return;
        accumulatedRevenue += amount;
        setChanged();
    }

    public long withdrawRevenue() {
        long current = accumulatedRevenue;
        accumulatedRevenue = 0;
        if (current > 0) setChanged();
        return current;
    }

    // --- Stock matching for offers ---

    /**
     * Counts items in stock matching the given template (Item + NBT strict).
     * Admin shops report infinite stock.
     */
    public int countMatchingItems(ItemStack template) {
        if (isAdmin) return Integer.MAX_VALUE;
        if (template.isEmpty()) return 0;
        int total = 0;
        for (ItemStack s : items) {
            if (matchesTemplate(s, template)) total += s.getCount();
        }
        return total;
    }

    public boolean hasStockFor(ShopOffer offer) {
        if (isAdmin) return true;
        return countMatchingItems(offer.template()) >= offer.quantityPerPurchase();
    }

    public boolean consumeStock(ShopOffer offer) {
        if (isAdmin) return true;
        if (!hasStockFor(offer)) return false;
        int remaining = offer.quantityPerPurchase();
        for (int i = 0; i < items.size() && remaining > 0; i++) {
            ItemStack s = items.get(i);
            if (!matchesTemplate(s, offer.template())) continue;
            int toTake = Math.min(remaining, s.getCount());
            s.shrink(toTake);
            remaining -= toTake;
        }
        setChanged();
        return remaining == 0;
    }

    private static boolean matchesTemplate(ItemStack stack, ItemStack template) {
        if (stack.isEmpty()) return false;
        if (!stack.is(template.getItem())) return false;
        CompoundTag a = stack.getTag();
        CompoundTag b = template.getTag();
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.equals(b);
    }

    // --- NBT ---

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (!this.trySaveLootTable(tag)) {
            ContainerHelper.saveAllItems(tag, this.items);
        }
        if (owner != null) tag.putUUID(TAG_OWNER, owner);
        tag.putBoolean(TAG_IS_ADMIN, isAdmin);
        tag.putBoolean(TAG_ALLOWS_TRANSFER, allowsTransfer);
        tag.putString(TAG_TIER, tier.id());
        tag.put(TAG_OFFERS, offers.toTag());
        tag.putLong(TAG_REVENUE, accumulatedRevenue);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        // Tier resolution: saved tag → enum; legacy shops with no tag
        // default to GOLD (preserves 27-slot behaviour from pre-tiers saves).
        if (tag.contains(TAG_TIER)) {
            String id = tag.getString(TAG_TIER);
            for (ShopTier t : ShopTier.values()) {
                if (t.id().equals(id)) { this.tier = t; break; }
            }
        }
        this.items = NonNullList.withSize(tier.stockSize(), ItemStack.EMPTY);
        if (!this.tryLoadLootTable(tag)) {
            ContainerHelper.loadAllItems(tag, this.items);
        }
        owner = tag.hasUUID(TAG_OWNER) ? tag.getUUID(TAG_OWNER) : null;
        isAdmin = tag.getBoolean(TAG_IS_ADMIN);
        allowsTransfer = tag.getBoolean(TAG_ALLOWS_TRANSFER);
        offers = OfferList.fromTag(tag.getCompound(TAG_OFFERS), tier.maxOffers());
        accumulatedRevenue = tag.getLong(TAG_REVENUE);
    }
}
