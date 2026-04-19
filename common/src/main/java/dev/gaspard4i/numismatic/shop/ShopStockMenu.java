package dev.gaspard4i.numismatic.shop;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Container menu for the owner-side STOCK tab. Exposes the shop's 27-slot
 * stock as a chest-like grid backed by {@link ShopBlockEntity#getStock()},
 * plus the player's own inventory below.
 */
public class ShopStockMenu extends AbstractContainerMenu {

    private final SimpleContainer stockContainer;
    @Nullable
    private final ShopBlockEntity shop;

    public ShopStockMenu(int containerId, Inventory playerInv) {
        this(containerId, playerInv, null);
    }

    public ShopStockMenu(int containerId, Inventory playerInv, @Nullable ShopBlockEntity shop) {
        super(NumismaticShop.SHOP_STOCK_MENU.get(), containerId);
        this.shop = shop;
        if (shop != null) {
            this.stockContainer = new SimpleContainer(ShopBlockEntity.STOCK_SIZE) {
                @Override
                public ItemStack getItem(int slot) {
                    return shop.getStock().get(slot);
                }
                @Override
                public void setItem(int slot, ItemStack stack) {
                    shop.getStock().set(slot, stack);
                    setChanged();
                }
                @Override
                public ItemStack removeItem(int slot, int amount) {
                    ItemStack s = shop.getStock().get(slot);
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
                    ItemStack s = shop.getStock().get(slot);
                    shop.getStock().set(slot, ItemStack.EMPTY);
                    return s;
                }
                @Override
                public void setChanged() {
                    shop.setChanged();
                }
            };
        } else {
            this.stockContainer = new SimpleContainer(ShopBlockEntity.STOCK_SIZE);
        }

        // Stock grid 9x3
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(stockContainer, col + row * 9, 8 + col * 18, 18 + row * 18));
            }
        }

        // Player inventory
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        // Player hotbar
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInv, col, 8 + col * 18, 142));
        }
    }

    @Nullable
    public ShopBlockEntity getShop() { return shop; }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack ret = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot.hasItem()) {
            ItemStack stack = slot.getItem();
            ret = stack.copy();
            if (index < ShopBlockEntity.STOCK_SIZE) {
                if (!moveItemStackTo(stack, ShopBlockEntity.STOCK_SIZE, this.slots.size(), true)) return ItemStack.EMPTY;
            } else {
                if (!moveItemStackTo(stack, 0, ShopBlockEntity.STOCK_SIZE, false)) return ItemStack.EMPTY;
            }
            if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
            else slot.setChanged();
        }
        return ret;
    }

    @Override
    public boolean stillValid(Player player) {
        if (shop == null) return true;
        return player.distanceToSqr(
                shop.getBlockPos().getX() + 0.5,
                shop.getBlockPos().getY() + 0.5,
                shop.getBlockPos().getZ() + 0.5) < 64.0;
    }
}
