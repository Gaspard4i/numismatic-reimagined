package dev.gaspard4i.numismatic.shop;

import net.minecraft.client.Minecraft;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Container menu for the shop. Stock size is now driven by the tier
 * ({@link ShopTier#stockSize()}), 9..36 slots arranged as N rows of 9.
 *
 * <p>Stock slots are hidden when the client's {@code ShopScreen} is on the
 * offers tab (tab=1), via {@link AutoHidingSlot}.
 */
public class ShopMenu extends AbstractContainerMenu {

    public static final int STOCK_COLS = 9;
    /** Legacy constant — prefer {@link #stockSize()} on an instance. */
    public static final int STOCK_SIZE = 27;

    @Nullable
    private final ShopBlockEntity shop;
    private final Container stockContainer;
    private final int stockSize;

    public ShopMenu(int containerId, Inventory playerInv) {
        this(containerId, playerInv, null);
    }

    public ShopMenu(int containerId, Inventory playerInv, @Nullable ShopBlockEntity shop) {
        super(ShopRegistry.SHOP_MENU.get(), containerId);
        this.shop = shop;
        int size = shop != null ? shop.getContainerSize() : STOCK_SIZE;
        this.stockSize = size;
        this.stockContainer = shop != null ? new BackedContainer(shop, size) : new SimpleContainer(size);

        int rows = Math.max(1, size / STOCK_COLS);
        int cols = STOCK_COLS;

        // Stock grid : rows×9, starting at (8, 18). The PNG has its dark
        // border at y=17 and the cell interior at y=18..33, so the slot
        // Y must be 18 for the hover square to sit inside the cell.
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                int index = col + row * cols;
                addSlot(new AutoHidingSlot(stockContainer, index, 8 + col * 18, 18 + row * 18));
            }
        }
        int invY = 18 + rows * 18 + 13;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, invY + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInv, col, 8 + col * 18, invY + 58));
        }
    }

    @Nullable public ShopBlockEntity getShop() { return shop; }

    public int stockSize() { return stockSize; }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack ret = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) return ret;
        ItemStack stack = slot.getItem();
        ret = stack.copy();
        if (index < stockSize) {
            if (!moveItemStackTo(stack, stockSize, this.slots.size(), true)) return ItemStack.EMPTY;
        } else {
            if (!moveItemStackTo(stack, 0, stockSize, false)) return ItemStack.EMPTY;
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

    /** Stock slot disabled (hidden) when {@code ShopScreen} is on tab 1 (offers). */
    public static final class AutoHidingSlot extends Slot {
        public AutoHidingSlot(Container inv, int index, int x, int y) {
            super(inv, index, x, y);
        }

        @Override
        public boolean isActive() {
            var screen = Minecraft.getInstance().screen;
            if (screen instanceof dev.gaspard4i.numismatic.client.screen.ShopScreen s) {
                return s.currentTab() == 0;
            }
            return true;
        }
    }

    /** Server-side container forwarding to the BE's stock list. */
    private static final class BackedContainer extends SimpleContainer {
        private final ShopBlockEntity shop;

        BackedContainer(ShopBlockEntity shop, int size) {
            super(size);
            this.shop = shop;
        }

        @Override public ItemStack getItem(int slot) {
            return slot >= 0 && slot < shop.getStock().size() ? shop.getStock().get(slot) : ItemStack.EMPTY;
        }

        @Override public void setItem(int slot, ItemStack stack) {
            if (slot >= 0 && slot < shop.getStock().size()) {
                shop.getStock().set(slot, stack);
                setChanged();
            }
        }

        @Override public ItemStack removeItem(int slot, int amount) {
            ItemStack s = getItem(slot);
            if (s.isEmpty()) return ItemStack.EMPTY;
            int take = Math.min(amount, s.getCount());
            ItemStack out = s.copy();
            out.setCount(take);
            s.shrink(take);
            setChanged();
            return out;
        }

        @Override public ItemStack removeItemNoUpdate(int slot) {
            ItemStack s = getItem(slot);
            shop.getStock().set(slot, ItemStack.EMPTY);
            return s;
        }

        @Override public void setChanged() { shop.setChanged(); }
    }
}
