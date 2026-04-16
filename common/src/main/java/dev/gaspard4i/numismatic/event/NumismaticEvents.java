package dev.gaspard4i.numismatic.event;

import dev.architectury.event.events.common.PlayerEvent;
import dev.gaspard4i.numismatic.NumismaticReimagined;
import dev.gaspard4i.numismatic.currency.PlayerCurrencyManager;
import dev.gaspard4i.numismatic.network.NumismaticNetworking;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/**
 * Registers Architectury events for the currency system.
 */
public final class NumismaticEvents {

    private NumismaticEvents() {}

    public static void register() {
        // Sync balance to client on join
        PlayerEvent.PLAYER_JOIN.register(player -> {
            ServerLevel overworld = player.server.overworld();
            PlayerCurrencyManager manager = PlayerCurrencyManager.get(overworld);
            NumismaticNetworking.syncToClient(player, manager);
            NumismaticReimagined.LOGGER.debug("Synced currency to {} on join", player.getName().getString());
        });

        // Sync balance on respawn (dimension change or death)
        PlayerEvent.PLAYER_RESPAWN.register((serverPlayer, conqueredEnd) -> {
            ServerLevel overworld = serverPlayer.server.overworld();
            PlayerCurrencyManager manager = PlayerCurrencyManager.get(overworld);
            NumismaticNetworking.syncToClient(serverPlayer, manager);
        });

        // Copy balance on death -> respawn (keepInventory equivalent for purse)
        PlayerEvent.PLAYER_CLONE.register((newPlayer, oldPlayer, wonGame) -> {
            if (!wonGame) {
                // Player died — balance is already in SavedData, no copy needed.
                // If we wanted to drop a percentage on death, we'd handle it here.
                // For now, purse always persists through death (configurable later in Phase 5).
            }
        });
    }
}
