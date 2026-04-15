package dev.gaspard4i.numismatic.fabric;

import dev.gaspard4i.numismatic.NumismaticReimagined;
import net.fabricmc.api.ModInitializer;

public class NumismaticReimaginedFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        NumismaticReimagined.init();
    }
}
