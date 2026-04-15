package dev.gaspard4i.numismatic.forge;

import dev.architectury.platform.forge.EventBuses;
import dev.gaspard4i.numismatic.NumismaticConstants;
import dev.gaspard4i.numismatic.NumismaticReimagined;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(NumismaticConstants.MOD_ID)
public class NumismaticReimaginedForge {

    public NumismaticReimaginedForge() {
        EventBuses.registerModEventBus(NumismaticConstants.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
        NumismaticReimagined.init();
    }
}
