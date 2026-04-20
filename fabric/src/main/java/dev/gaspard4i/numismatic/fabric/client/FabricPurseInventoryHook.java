package dev.gaspard4i.numismatic.fabric.client;

import dev.gaspard4i.numismatic.client.PurseInventoryWidget;
import dev.gaspard4i.numismatic.fabric.mixin.AbstractContainerScreenAccessor;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;

/**
 * Attaches a {@link PurseInventoryWidget} to the vanilla inventory screen.
 * The widget draws its own popup when open (no separate Screen); this
 * hook also intercepts mouse clicks so the popup's buttons receive input
 * before the inventory slots below.
 */
public final class FabricPurseInventoryHook {

    private FabricPurseInventoryHook() {}

    public static void register() {
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (!(screen instanceof InventoryScreen inv)) return;
            AbstractContainerScreenAccessor acc = (AbstractContainerScreenAccessor) inv;
            int left = acc.numismatic$getLeftPos();
            int top = acc.numismatic$getTopPos();

            // Money-bag icon above the crafting output.
            int x = left + 152;
            int y = top + 6;
            PurseInventoryWidget widget = new PurseInventoryWidget(x, y);
            Screens.getButtons(inv).add(widget);

            // Intercept clicks before the vanilla slot hit-test runs, so
            // the popup's +/- / extract buttons get priority when open.
            ScreenMouseEvents.allowMouseClick(inv).register((s, mx, my, btn) -> {
                // widget.onInventoryClick returns true when it consumed the
                // click (popup button). Returning false in ALLOW blocks the
                // vanilla slot click.
                if (widget.onInventoryClick(mx, my, btn)) return false;
                return true;
            });
        });
    }
}
