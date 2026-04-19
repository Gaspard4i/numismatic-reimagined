package dev.gaspard4i.numismatic.shop;

import dev.gaspard4i.numismatic.network.NumismaticNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/**
 * Server-side helper that opens the appropriate shop menu for a player.
 * Sends an S2C packet so the client opens the right screen with the right
 * mode preselected.
 */
public final class ShopMenuOpener {

    private ShopMenuOpener() {}

    public static void openFor(Player player, ShopBlockEntity shop, ShopMenuMode mode) {
        if (!(player instanceof ServerPlayer sp)) return;
        // Force CLIENT mode for non-editors regardless of requested mode
        ShopMenuMode actualMode = shop.canEdit(player) ? mode : ShopMenuMode.CLIENT;
        NumismaticNetworking.openShopScreen(sp, shop, actualMode);
    }
}
