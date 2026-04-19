package dev.gaspard4i.numismatic.shop;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * A {@link Slot} that can be toggled inactive. When hidden, vanilla skips
 * rendering ({@code isActive() == false}) and the shift-click / click paths
 * can't place or pick up items ({@code mayPlace} / {@code mayPickup} both
 * return false).
 *
 * <p>Used by the shop menu to conditionally enable stock slots only in the
 * STOCK tab (owner-edit mode), while keeping the same container menu across
 * all tabs.
 */
public class HideableSlot extends Slot {

    private boolean visible = true;
    private boolean readOnly = false;

    public HideableSlot(Container container, int slot, int x, int y) {
        super(container, slot, x, y);
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    @Override
    public boolean isActive() {
        return visible;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return visible && !readOnly;
    }

    @Override
    public boolean mayPickup(Player player) {
        return visible && !readOnly;
    }
}
