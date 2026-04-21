package dev.gaspard4i.numismatic.client;

import dev.architectury.hooks.client.screen.ScreenAccess;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;

import java.util.WeakHashMap;

/**
 * Attaches a purse widget to the target screens, matching upstream coordinates :
 * <ul>
 *   <li>InventoryScreen → (leftPos + 160, topPos + 5)</li>
 *   <li>CreativeModeInventoryScreen → (leftPos + 38, topPos + 4) on the Inventory tab only</li>
 *   <li>MerchantScreen → (leftPos + 260, topPos + 5)</li>
 * </ul>
 */
public final class PurseScreenHook {

    private static final WeakHashMap<CreativeModeInventoryScreen, PurseInventoryWidget> CREATIVE_WIDGETS =
            new WeakHashMap<>();
    private static final WeakHashMap<Screen, PurseInventoryWidget> ALL_WIDGETS =
            new WeakHashMap<>();

    private PurseScreenHook() {}

    /**
     * Returns true when the given screen currently has an open purse popup whose area
     * covers the mouse — callers should then drop the click and not forward it to the
     * inventory slots behind.
     */
    public static boolean shouldConsumeClick(Screen screen, double mouseX, double mouseY) {
        PurseInventoryWidget widget = ALL_WIDGETS.get(screen);
        if (widget == null || !widget.visible) return false;
        if (!widget.consumesClick(mouseX, mouseY)) return false;
        widget.mouseClicked(mouseX, mouseY, 0);
        return true;
    }

    public static void attach(Screen screen, ScreenAccess access) {
        if (screen instanceof InventoryScreen inv) {
            int x = inv.leftPos + 160;
            int y = inv.topPos + 5;
            PurseInventoryWidget widget = new PurseInventoryWidget(x, y);
            ALL_WIDGETS.put(inv, widget);
            access.addRenderableWidget(widget);
            return;
        }
        if (screen instanceof CreativeModeInventoryScreen creative) {
            int x = creative.leftPos + 38;
            int y = creative.topPos + 4;
            PurseInventoryWidget widget = new PurseInventoryWidget(x, y);
            CREATIVE_WIDGETS.put(creative, widget);
            ALL_WIDGETS.put(creative, widget);
            access.addRenderableWidget(widget);
            return;
        }
        if (screen instanceof MerchantScreen merchant) {
            int x = ((AbstractContainerScreen<?>) merchant).leftPos + 260;
            int y = ((AbstractContainerScreen<?>) merchant).topPos + 5;
            PurseInventoryWidget widget = new PurseInventoryWidget(x, y);
            ALL_WIDGETS.put(merchant, widget);
            access.addRenderableWidget(widget);
        }
    }

    /**
     * Toggled per frame : the purse is only shown on the Inventory tab of the creative screen,
     * hidden on Combat/Redstone/Search etc. (faithful upstream behavior).
     */
    public static void updateCreativeVisibility(CreativeModeInventoryScreen screen) {
        PurseInventoryWidget widget = CREATIVE_WIDGETS.get(screen);
        if (widget == null) return;
        boolean onInventoryTab = isInventoryTabSelected(screen);
        widget.setShown(onInventoryTab);
    }

    private static boolean isInventoryTabSelected(CreativeModeInventoryScreen screen) {
        // Selected tab is stored in the screen instance : we check if it's the inventory one
        // by comparing the tab's row (inventory tab is the only tab whose key is INVENTORY).
        try {
            java.lang.reflect.Field tabField = null;
            for (Class<?> c = screen.getClass(); c != null; c = c.getSuperclass()) {
                try {
                    tabField = c.getDeclaredField("selectedTab");
                    break;
                } catch (NoSuchFieldException ignored) {}
            }
            if (tabField == null) return false;
            tabField.setAccessible(true);
            Object current = tabField.get(screen);
            if (!(current instanceof CreativeModeTab tab)) return false;
            return BuiltInRegistries.CREATIVE_MODE_TAB.getKey(tab) != null
                    && BuiltInRegistries.CREATIVE_MODE_TAB.getKey(tab).equals(CreativeModeTabs.INVENTORY.location());
        } catch (Exception e) {
            return false;
        }
    }
}
