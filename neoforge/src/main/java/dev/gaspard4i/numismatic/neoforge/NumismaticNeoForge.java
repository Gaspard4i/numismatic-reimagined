package dev.gaspard4i.numismatic.neoforge;

import dev.gaspard4i.numismatic.Numismatic;
import dev.gaspard4i.numismatic.NumismaticConstants;
import net.neoforged.fml.common.Mod;

@Mod(NumismaticConstants.MOD_ID)
public final class NumismaticNeoForge {

    public NumismaticNeoForge() {
        Numismatic.init();
    }
}
