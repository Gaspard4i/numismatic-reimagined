package dev.gaspard4i.numismatic.neoforge;

import dev.gaspard4i.numismatic.NumismaticConstants;
import dev.gaspard4i.numismatic.client.NumismaticClient;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.bus.api.SubscribeEvent;

@EventBusSubscriber(modid = NumismaticConstants.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class NumismaticNeoForgeClient {

    private NumismaticNeoForgeClient() {}

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(NumismaticClient::init);
    }

    public static void register(IEventBus modBus) {
        // Reserved for future listeners.
    }
}
