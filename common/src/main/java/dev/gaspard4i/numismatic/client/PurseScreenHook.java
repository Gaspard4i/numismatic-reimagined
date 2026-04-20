package dev.gaspard4i.numismatic.client;

import dev.architectury.hooks.client.screen.ScreenAccess;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;

public final class PurseScreenHook {

    private PurseScreenHook() {}

    public static void attach(InventoryScreen screen, ScreenAccess access) {
        int x = screen.leftPos + 152;
        int y = screen.topPos + 6;
        PurseInventoryWidget widget = new PurseInventoryWidget(x, y);
        access.addRenderableWidget(widget);
    }
}
