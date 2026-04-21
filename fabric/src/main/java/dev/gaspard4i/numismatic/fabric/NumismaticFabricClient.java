package dev.gaspard4i.numismatic.fabric;

import dev.gaspard4i.numismatic.client.NumismaticClient;
import dev.gaspard4i.numismatic.client.tooltip.CurrencyTooltipComponent;
import dev.gaspard4i.numismatic.client.tooltip.CurrencyTooltipData;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback;

public final class NumismaticFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        NumismaticClient.init();
        TooltipComponentCallback.EVENT.register(data -> {
            if (data instanceof CurrencyTooltipData ctd) return new CurrencyTooltipComponent(ctd);
            return null;
        });
    }
}
