package dev.gaspard4i.numismatic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Numismatic {

    public static final Logger LOGGER = LoggerFactory.getLogger(NumismaticConstants.MOD_NAME);

    private Numismatic() {}

    public static void init() {
        LOGGER.info("Loading {} common initialization", NumismaticConstants.MOD_NAME);
        dev.gaspard4i.numismatic.component.NumismaticDataComponents.register();
        dev.gaspard4i.numismatic.item.NumismaticItems.register();
    }
}
