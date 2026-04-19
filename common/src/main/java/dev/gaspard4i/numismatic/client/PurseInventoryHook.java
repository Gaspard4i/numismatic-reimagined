package dev.gaspard4i.numismatic.client;

import dev.architectury.event.events.client.ClientGuiEvent;
import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.registry.client.keymappings.KeyMappingRegistry;
import dev.gaspard4i.numismatic.client.screen.PurseScreen;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import org.lwjgl.glfw.GLFW;

/**
 * Lets the player open the {@link PurseScreen} via a keybind (default: P)
 * usable from anywhere except inside another menu. Multi-loader native via
 * Architectury — no mixin needed, works on Fabric and Forge.
 *
 * <p>The keybind also works while the {@link InventoryScreen} is open,
 * so the purse stays accessible "from the inventory" as requested without
 * having to inject a button into the vanilla inventory layout.
 */
public final class PurseInventoryHook {

    private static final KeyMapping OPEN_PURSE = new KeyMapping(
            "key.numismatic_reimagined.open_purse",
            GLFW.GLFW_KEY_P,
            "key.categories.inventory"
    );

    private PurseInventoryHook() {}

    public static void register() {
        KeyMappingRegistry.register(OPEN_PURSE);

        ClientTickEvent.CLIENT_POST.register(mc -> {
            while (OPEN_PURSE.consumeClick()) {
                Minecraft m = Minecraft.getInstance();
                if (m.player == null) continue;
                // Allow opening from world or from the vanilla inventory.
                if (m.screen == null || m.screen instanceof InventoryScreen) {
                    m.setScreen(new PurseScreen(m.screen));
                }
            }
        });
    }
}
