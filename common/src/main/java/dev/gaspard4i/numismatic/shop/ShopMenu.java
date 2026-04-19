package dev.gaspard4i.numismatic.shop;

import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * The unified shop menu. Always holds 27 stock slots + 36 player inventory
 * slots. Stock slots are hidden (via {@link HideableSlot}) when the client
 * is not in STOCK mode, and read-only when the viewer can't edit.
 *
 * <p>Keeping a single menu across all three tabs (OFFERS / STOCK / CLIENT)
 * avoids re-sending {@code OpenScreen} packets on tab switch and keeps slot
 * indices stable (any transient index mismatch between client and server
 * during re-open would cause "index out of bounds" crashes on clicks).
 */
public class ShopMenu extends AbstractContainerMenu {

    public static final int STOCK_ROWS = 3;
    public static final int STOCK_COLS = 9;
    public static final int STOCK_SIZE = STOCK_ROWS * STOCK_COLS;

    private final Container stockContainer;
    @Nullable
    private final ShopBlockEntity shop;
    private final HideableSlot[] stockSlots = new HideableSlot[STOCK_SIZE];

    /** Factory used by Forge/Fabric NetworkHooks to recreate the menu client-side. */
    public ShopMenu(int containerId, Inventory playerInv) {
        this(containerId, playerInv, null);
    }

    public ShopMenu(int containerId, Inventory playerInv, @Nullable ShopBlockEntity shop) {
        super(NumismaticShop.SHOP_MENU.get(), containerId);
        this.shop = shop;
        this.stockContainer = shop != null ? new BackedContainer(shop) : new SimpleContainer(STOCK_SIZE);

        // Stock grid 9x3 (drawn in STOCK tab; hidden in OFFERS/CLIENT).
        for (int row = 0; row < STOCK_ROWS; row++) {
            for (int col = 0; col < STOCK_COLS; col++) {
                int index = col + row * STOCK_COLS;
                HideableSlot slot = new HideableSlot(stockContainer, index, 8 + col * 18, 18 + row * 18);
                stockSlots[index] = slot;
                addSlot(slot);
            }
        }

        // Player inventory (always visible).
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInv, col, 8 + col * 18, 142));
        }
    }

    @Nullable
    public ShopBlockEntity getShop() {
        return shop;
    }

    /**
     * Shows or hides the stock slots. Called client-side on tab switch so
     * the slots disappear visually and don't swallow clicks in OFFERS/CLIENT
     * mode.
     */
    public void setStockVisible(boolean visible) {
        for (HideableSlot slot : stockSlots) slot.setVisible(visible);
    }

    /**
     * Marks all stock slots read-only (no pickup/place) for buyers in CLIENT
     * mode even if they're somehow visible.
     */
    public void setStockReadOnly(boolean readOnly) {
        for (HideableSlot slot : stockSlots) slot.setReadOnly(readOnly);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack ret = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) return ret;
        ItemStack stack = slot.getItem();
        ret = stack.copy();

        if (index < STOCK_SIZE) {
            // Shop stock -> player inventory
            if (!moveItemStackTo(stack, STOCK_SIZE, this.slots.size(), true)) return ItemStack.EMPTY;
        } else {
            // Player inventory -> shop stock (only if stock slots are active)
            if (!moveItemStackTo(stack, 0, STOCK_SIZE, false)) return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();
        return ret;
    }

    @Override
    public boolean stillValid(Player player) {
        if (shop == null) return true;
        if (shop.isRemoved()) return false;
        return player.distanceToSqr(
                shop.getBlockPos().getX() + 0.5,
                shop.getBlockPos().getY() + 0.5,
                shop.getBlockPos().getZ() + 0.5) < 64.0;
    }

    /**
     * Server-side backing container that forwards to the shop BE's stock list.
     * Client-side instances use a plain SimpleContainer and are updated via
     * SYNC_SHOP_STATE_S2C.
     */
    private static final class BackedContainer extends SimpleContainer {
        private final ShopBlockEntity shop;

        BackedContainer(ShopBlockEntity shop) {
            super(STOCK_SIZE);
            this.shop = shop;
        }

        @Override
        public ItemStack getItem(int slot) {
            NonNullList<ItemStack> stock = shop.getStock();
            return slot >= 0 && slot < stock.size() ? stock.get(slot) : ItemStack.EMPTY;
        }

        @Override
        public void setItem(int slot, ItemStack stack) {
            NonNullList<ItemStack> stock = shop.getStock();
            if (slot >= 0 && slot < stock.size()) {
                stock.set(slot, stack);
                setChanged();
            }
        }

        @Override
        public ItemStack removeItem(int slot, int amount) {
            NonNullList<ItemStack> stock = shop.getStock();
            if (slot < 0 || slot >= stock.size()) return ItemStack.EMPTY;
            ItemStack s = stock.get(slot);
            if (s.isEmpty()) return ItemStack.EMPTY;
            int take = Math.min(amount, s.getCount());
            ItemStack out = s.copy();
            out.setCount(take);
            s.shrink(take);
            setChanged();
            return out;
        }

        @Override
        public ItemStack removeItemNoUpdate(int slot) {
            NonNullList<ItemStack> stock = shop.getStock();
            if (slot < 0 || slot >= stock.size()) return ItemStack.EMPTY;
            ItemStack s = stock.get(slot);
            stock.set(slot, ItemStack.EMPTY);
            return s;
        }

        @Override
        public void setChanged() {
            shop.setChanged();
        }
    }
}
