package dev.gaspard4i.numismatic.client;

import dev.gaspard4i.numismatic.client.screen.PurseScreen;
import net.minecraft.client.Minecraft;

/**
 * Shared shortcut handler for opening {@link PurseScreen}. Invoked from
 * the Fabric and Forge keybind hooks so the binding can be declared once
 * per platform while the action stays common.
 */
public final class PurseKeybindState {

    private PurseKeybindState() {}

    public static void openPurseScreen() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        if (mc.screen != null) return;
        mc.setScreen(new PurseScreen(null));
    }
}
