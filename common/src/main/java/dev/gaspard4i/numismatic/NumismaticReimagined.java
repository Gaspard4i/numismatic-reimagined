package dev.gaspard4i.numismatic;

import dev.gaspard4i.numismatic.advancement.NumismaticTriggers;
import dev.gaspard4i.numismatic.block.NumismaticBlocks;
import dev.gaspard4i.numismatic.command.NumismaticCommands;
import dev.gaspard4i.numismatic.event.NumismaticEvents;
import dev.gaspard4i.numismatic.item.NumismaticItems;
import dev.gaspard4i.numismatic.network.NumismaticNetworking;
import dev.gaspard4i.numismatic.shop.ShopRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class NumismaticReimagined {

    public static final Logger LOGGER = LoggerFactory.getLogger(NumismaticConstants.MOD_NAME);

    public static void init() {
        LOGGER.info("Initializing {}...", NumismaticConstants.MOD_NAME);
        NumismaticItems.register();
        NumismaticBlocks.register();
        ShopRegistry.register();
        NumismaticNetworking.registerServerReceivers();
        NumismaticTriggers.register();
        NumismaticEvents.register();
        NumismaticCommands.register();
    }
}
