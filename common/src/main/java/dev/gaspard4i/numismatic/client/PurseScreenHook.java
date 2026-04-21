package dev.gaspard4i.numismatic.client;

import dev.architectury.hooks.client.screen.ScreenAccess;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;

/**
 * Attaches a purse button widget to the target screens, matching upstream positions :
 * <ul>
 *   <li>InventoryScreen → (leftPos + 160, topPos + 5)</li>
 *   <li>CreativeModeInventoryScreen → (leftPos + 38, topPos + 4) on the inventory tab</li>
 *   <li>MerchantScreen → (leftPos + 260, topPos + 5)</li>
 * </ul>
 */
public final class PurseScreenHook {

    private PurseScreenHook() {}

    public static void attach(Screen screen, ScreenAccess access) {
        if (screen instanceof InventoryScreen inv) {
            int x = inv.leftPos + 160;
            int y = inv.topPos + 5;
            access.addRenderableWidget(new PurseInventoryWidget(x, y));
            return;
        }
        if (screen instanceof CreativeModeInventoryScreen creative) {
            int x = creative.leftPos + 38;
            int y = creative.topPos + 4;
            access.addRenderableWidget(new PurseInventoryWidget(x, y));
            return;
        }
        if (screen instanceof MerchantScreen merchant) {
            int x = ((AbstractContainerScreen<?>) merchant).leftPos + 260;
            int y = ((AbstractContainerScreen<?>) merchant).topPos + 5;
            access.addRenderableWidget(new PurseInventoryWidget(x, y));
        }
    }
}
