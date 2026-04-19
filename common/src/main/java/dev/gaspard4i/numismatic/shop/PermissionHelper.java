package dev.gaspard4i.numismatic.shop;

import net.minecraft.world.entity.player.Player;

/**
 * Permission lookups for shop operations. Uses vanilla {@code hasPermissions(level)}
 * which is intercepted by permission systems like LuckPerms (via mixin) — so
 * fine-grained permission nodes work transparently without any extra dependency.
 */
public final class PermissionHelper {

    /** OP level required to edit admin shop offers / withdraw revenue. */
    public static final int ADMIN_SHOP_EDIT_LEVEL = 2;

    private PermissionHelper() {}

    /**
     * True if the player can edit the admin shop's offers or otherwise manage
     * it as an admin. Defers to vanilla op level (2+), which permission mods
     * like LuckPerms can override per-permission-node.
     */
    public static boolean canEditAdminShop(Player player) {
        return player.hasPermissions(ADMIN_SHOP_EDIT_LEVEL);
    }
}
