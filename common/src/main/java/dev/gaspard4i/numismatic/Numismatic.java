package dev.gaspard4i.numismatic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Numismatic {

    public static final Logger LOGGER = LoggerFactory.getLogger(NumismaticConstants.MOD_NAME);

    private Numismatic() {}

    public static void init() {
        LOGGER.info("Loading {} common initialization", NumismaticConstants.MOD_NAME);
        dev.gaspard4i.numismatic.component.NumismaticDataComponents.register();
        // Blocks first : they register BlockItem entries into the item registry.
        dev.gaspard4i.numismatic.block.PiggyBankBlocks.register();
        dev.gaspard4i.numismatic.shop.NumismaticShops.register();
        dev.gaspard4i.numismatic.request.RequestBoardBlocks.register();
        dev.gaspard4i.numismatic.item.NumismaticItems.register();
        dev.gaspard4i.numismatic.item.NumismaticCreativeTab.register();
        dev.gaspard4i.numismatic.network.NumismaticNetworking.register();
        dev.gaspard4i.numismatic.loot.MobDropEvents.register();
        dev.gaspard4i.numismatic.villager.NumismaticTrades.register();
        dev.gaspard4i.numismatic.command.NumismaticCommands.register();
    }
}
