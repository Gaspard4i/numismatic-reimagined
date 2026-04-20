package dev.gaspard4i.numismatic.fabric;

import dev.gaspard4i.numismatic.client.NumismaticClient;
import net.fabricmc.api.ClientModInitializer;

public final class NumismaticFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        NumismaticClient.init();
    }
}
