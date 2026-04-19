package dev.gaspard4i.numismatic.testutil;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;

/**
 * One-time Minecraft bootstrap for unit tests that touch ItemStack, Item,
 * registries, NBT, etc. Calling {@link #ensure()} from a test's {@code @BeforeAll}
 * is safe and idempotent.
 *
 * <p>This avoids the "Bootstrap not initialized" / "registries frozen" errors
 * that plague tests touching MC types. It does NOT spin up a server, world,
 * or client — only the registry/codec scaffolding.
 */
public final class McBootstrap {

    private static boolean initialized = false;

    private McBootstrap() {}

    public static void ensure() {
        if (initialized) return;
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        Bootstrap.validate();
        initialized = true;
    }
}
