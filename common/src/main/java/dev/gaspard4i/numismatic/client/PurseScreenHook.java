package dev.gaspard4i.numismatic.client;

import dev.architectury.hooks.client.screen.ScreenAccess;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;

public final class PurseScreenHook {

    private PurseScreenHook() {}

    public static void attach(InventoryScreen screen, ScreenAccess access) {
        // Upstream positions the purse button at handled-screen-relative (160, 5),
        // which places it just right of the crafting output slot on the survival inventory.
        int x = screen.leftPos + 160;
        int y = screen.topPos + 5;
        PurseInventoryWidget widget = new PurseInventoryWidget(x, y);
        access.addRenderableWidget(widget);
    }
}
