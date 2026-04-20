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
 * Container menu for the shop. 27 stock slots are hidden in tab=1 (offers)
 * via {@link AutoHidingSlot}. Matches the layout and slot coordinates of
 * wisp-forest/numismatic-overhaul/ShopScreenHandler.
 */
public class ShopMenu extends AbstractContainerMenu {

    public static final int STOCK_ROWS = 3;
    public static final int STOCK_COLS = 9;
    public static final int STOCK_SIZE = STOCK_ROWS * STOCK_COLS;

    @Nullable
    private final ShopBlockEntity shop;
    private final Container stockContainer;

    public ShopMenu(int containerId, Inventory playerInv) {
        this(containerId, playerInv, null);
    }

    public ShopMenu(int containerId, Inventory playerInv, @Nullable ShopBlockEntity shop) {
        super(ShopRegistry.SHOP_MENU.get(), containerId);
        this.shop = shop;
        this.stockContainer = shop != null ? new BackedContainer(shop) : new SimpleContainer(STOCK_SIZE);

        // Stock 9x3 — first slot at (8, 18). One pixel below shop.xml's
        // original y=17 so items sit inside the cell instead of overlapping
        // the top border drawn in shop_gui.png.
        for (int row = 0; row < STOCK_ROWS; row++) {
            for (int col = 0; col < STOCK_COLS; col++) {
                int index = col + row * STOCK_COLS;
                addSlot(new AutoHidingSlot(stockContainer, index, 8 + col * 18, 18 + row * 18));
            }
        }
        // Player inventory at (8, 86) + hotbar at (8, 144) — offset by +1 too.
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 86 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInv, col, 8 + col * 18, 144));
        }
    }

    @Nullable public ShopBlockEntity getShop() { return shop; }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack ret = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) return ret;
        ItemStack stack = slot.getItem();
        ret = stack.copy();
        if (index < STOCK_SIZE) {
            if (!moveItemStackTo(stack, STOCK_SIZE, this.slots.size(), true)) return ItemStack.EMPTY;
        } else {
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

        BackedContainer(ShopBlockEntity shop) {
            super(STOCK_SIZE);
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
