package dev.gaspard4i.numismatic.fabric;

import dev.gaspard4i.numismatic.Numismatic;
import net.fabricmc.api.ModInitializer;

public final class NumismaticFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        Numismatic.init();
    }
}
