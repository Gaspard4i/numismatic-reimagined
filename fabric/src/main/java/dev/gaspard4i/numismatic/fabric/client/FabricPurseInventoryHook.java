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

            // Tiny money-bag icon above the crafting output (top-right of
            // the inventory panel, style matching the original mod).
            int x = left + 152;
            int y = top + 6;
            Screens.getButtons(inv).add(new PurseInventoryWidget(x, y));
        });
    }
}
