package dev.gaspard4i.numismatic.fabric.client;

import dev.gaspard4i.numismatic.client.PurseInventoryWidget;
import dev.gaspard4i.numismatic.fabric.mixin.AbstractContainerScreenAccessor;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;

/**
 * Attaches the {@link PurseInventoryWidget} to the vanilla
 * {@link InventoryScreen}. Rendered to the right of the player's crafting
 * grid, vertically aligned with the inventory.
 */
public final class FabricPurseInventoryHook {

    private FabricPurseInventoryHook() {}

    public static void register() {
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (!(screen instanceof InventoryScreen inv)) return;
            AbstractContainerScreenAccessor acc = (AbstractContainerScreenAccessor) inv;
            int left = acc.numismatic$getLeftPos();
            int top = acc.numismatic$getTopPos();

            // Place to the right of the recipe book button column, aligned with
            // the top of the inventory panel (176x166 vanilla layout).
            int x = left + 180;
            int y = top + 10;
            Screens.getButtons(inv).add(new PurseInventoryWidget(x, y));
        });
    }
}
