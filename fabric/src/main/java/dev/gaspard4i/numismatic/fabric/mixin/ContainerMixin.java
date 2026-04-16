package dev.gaspard4i.numismatic.fabric.mixin;

import net.minecraft.world.Container;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

/**
 * Raises the default container max stack size from 64 to 99,
 * allowing coins to stack properly in all inventories.
 */
@Mixin(Container.class)
public interface ContainerMixin {

    /**
     * @author Numismatic Reimagined
     * @reason Allow items with stacksTo(99) to actually stack to 99 in containers
     */
    @Overwrite
    default int getMaxStackSize() {
        return 99;
    }
}
