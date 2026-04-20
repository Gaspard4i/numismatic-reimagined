package dev.gaspard4i.numismatic.advancement;

import net.minecraft.advancements.CriteriaTriggers;

/**
 * Registers all mod-specific advancement triggers. Called once during mod
 * init (see {@code NumismaticReimagined.init}).
 */
public final class NumismaticTriggers {

    public static final CollectNetheriteTrigger COLLECT_NETHERITE = new CollectNetheriteTrigger();

    private NumismaticTriggers() {}

    public static void register() {
        CriteriaTriggers.register(COLLECT_NETHERITE);
    }
}
