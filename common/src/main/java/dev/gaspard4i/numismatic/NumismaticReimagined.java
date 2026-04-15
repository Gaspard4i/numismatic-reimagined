package dev.gaspard4i.numismatic;

import dev.gaspard4i.numismatic.item.NumismaticItems;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class NumismaticReimagined {

    public static final Logger LOGGER = LoggerFactory.getLogger(NumismaticConstants.MOD_NAME);

    public static void init() {
        LOGGER.info("Initializing {}...", NumismaticConstants.MOD_NAME);
        NumismaticItems.register();
    }
}
