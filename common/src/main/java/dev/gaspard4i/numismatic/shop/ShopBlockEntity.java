package dev.gaspard4i.numismatic.shop;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Block entity backing both regular and admin shop blocks.
 *
 * <p>Owner-managed shops have a stock (27 slots), accumulated revenue in
 * bronze, and an offer list (up to 81). Admin shops ignore stock and
 * revenue (infinite stock, no payout).
 */
public class ShopBlockEntity extends BlockEntity {

    public static final int STOCK_SIZE = 27;

    private static final String TAG_OWNER = "Owner";
    private static final String TAG_IS_ADMIN = "IsAdmin";
    private static final String TAG_OFFERS = "OfferList";
    private static final String TAG_STOCK = "Stock";
    private static final String TAG_REVENUE = "Revenue";

    @Nullable
    private UUID owner;
    private boolean isAdmin = false;
    private OfferList offers = new OfferList();
    private final NonNullList<ItemStack> stock = NonNullList.withSize(STOCK_SIZE, ItemStack.EMPTY);
    private final ShopRevenue revenue = new ShopRevenue();

    /**
     * Constructor used by the registered block entity type. Subclasses or
     * factories may set {@code isAdmin} via {@link #setAdmin(boolean)} after
     * construction (typically driven by the placed block variant).
     */
    public ShopBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    // --- Owner / admin ---

    @Nullable
    public UUID getOwner() {
        return owner;
    }

    public void setOwner(@Nullable UUID owner) {
        this.owner = owner;
        setChanged();
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        this.isAdmin = admin;
        setChanged();
    }

    /**
     * True if this player is the registered owner (UUID match). Always false
     * for admin shops — admin shops have no owner.
     */
    public boolean isOwner(Player player) {
        return !isAdmin && owner != null && owner.equals(player.getUUID());
    }

    /**
     * True if this player is allowed to edit offers, withdraw revenue, or
     * manage stock. Owner for regular shops; OP level 2+ for admin shops
     * (which delegates to permission systems like LuckPerms via vanilla).
     */
    public boolean canEdit(Player player) {
        if (isAdmin) {
            return player.hasPermissions(2);
        }
        return isOwner(player);
    }

    // --- Offers ---

    public OfferList getOffers() {
        return offers;
    }

    // --- Stock ---

    public NonNullList<ItemStack> getStock() {
        return stock;
    }

    // --- Revenue ---

    public long getAccumulatedRevenue() {
        return revenue.get();
    }

    public void addRevenue(long amount) {
        if (isAdmin) return;
        if (revenue.add(amount) > 0) setChanged();
    }

    /**
     * Resets the accumulated revenue to zero and returns the amount that was
     * stored. Caller is responsible for delivering it (e.g. as a money bag).
     */
    public long withdrawRevenue() {
        long current = revenue.withdraw();
        if (current > 0) setChanged();
        return current;
    }

    // --- Stock availability ---

    /**
     * Counts how many items in the stock match the given template (same item,
     * same NBT). Damage and count of the template are ignored — only Item type
     * and the rest of the NBT are compared. Returns the total matching count.
     *
     * <p>Admin shops always return {@link Integer#MAX_VALUE} (infinite stock).
     */
    public int countMatchingItems(ItemStack template) {
        if (isAdmin) return Integer.MAX_VALUE;
        return ShopStockOps.countMatching(stock, template);
    }

    /**
     * True if the shop has enough stock to fulfill one purchase of this offer.
     */
    public boolean hasStockFor(ShopOffer offer) {
        if (isAdmin) return true;
        return countMatchingItems(offer.template()) >= offer.quantityPerPurchase();
    }

    /**
     * Removes {@code offer.quantityPerPurchase()} items matching the offer
     * template from stock, possibly across several slots. Returns true if the
     * deduction completed; false if stock was insufficient (no partial removal
     * is performed in that case). No-op (returns true) for admin shops.
     */
    public boolean consumeStock(ShopOffer offer) {
        if (isAdmin) return true;
        boolean ok = ShopStockOps.consume(stock, offer.template(), offer.quantityPerPurchase());
        if (ok) setChanged();
        return ok;
    }

    // --- NBT ---

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (owner != null) tag.putUUID(TAG_OWNER, owner);
        tag.putBoolean(TAG_IS_ADMIN, isAdmin);
        tag.put(TAG_OFFERS, offers.toTag());
        CompoundTag stockTag = new CompoundTag();
        ContainerHelper.saveAllItems(stockTag, stock);
        tag.put(TAG_STOCK, stockTag);
        tag.putLong(TAG_REVENUE, revenue.get());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        owner = tag.hasUUID(TAG_OWNER) ? tag.getUUID(TAG_OWNER) : null;
        isAdmin = tag.getBoolean(TAG_IS_ADMIN);
        offers = OfferList.fromTag(tag.getCompound(TAG_OFFERS));
        stock.clear();
        for (int i = 0; i < STOCK_SIZE; i++) stock.set(i, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag.getCompound(TAG_STOCK), stock);
        // Re-seed revenue from persisted value
        long stored = tag.getLong(TAG_REVENUE);
        long delta = stored - revenue.get();
        if (delta > 0) revenue.add(delta);
        else if (delta < 0) {
            revenue.withdraw();
            if (stored > 0) revenue.add(stored);
        }
    }
}
