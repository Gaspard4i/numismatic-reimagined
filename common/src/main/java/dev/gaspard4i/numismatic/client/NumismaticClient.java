package dev.gaspard4i.numismatic.client;

import dev.architectury.event.events.client.ClientGuiEvent;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;

public final class NumismaticClient {

    private NumismaticClient() {}

    public static void init() {
        ClientGuiEvent.INIT_POST.register((screen, access) -> {
            if (!(screen instanceof InventoryScreen inventoryScreen)) return;
            PurseScreenHook.attach(inventoryScreen, access);
        });
    }
}
