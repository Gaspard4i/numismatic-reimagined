package dev.gaspard4i.numismatic.fabric.mixin;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Exposes {@code leftPos} / {@code topPos} (protected) as public getters
 * so {@code FabricPurseInventoryHook} can position widgets inside the
 * vanilla inventory layout without touching the screen class.
 */
@Mixin(AbstractContainerScreen.class)
public interface AbstractContainerScreenAccessor {
    @Accessor("leftPos")
    int numismatic$getLeftPos();

    @Accessor("topPos")
    int numismatic$getTopPos();
}
