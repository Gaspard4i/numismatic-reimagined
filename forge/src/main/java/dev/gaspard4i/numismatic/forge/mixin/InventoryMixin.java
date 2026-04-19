package dev.gaspard4i.numismatic.forge.mixin;

import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Adds an explicit {@code getMaxStackSize()} override on {@link Inventory}
 * so its slots accept stacks up to 99 (matching coin items' stacksTo). The
 * default Container interface returns 64; without this, vanilla auto-merge
 * code paths (shift-click, hopper) silently cap coins at 64 in the player
 * inventory.
 *
 * <p>We avoid mixing the {@code Container} interface itself because Sponge
 * Mixin chokes on default-method transformation across all implementers
 * (LambdaMetafactory class load failure during interface processing).
 */
@Mixin(Inventory.class)
public abstract class InventoryMixin {

    public int getMaxStackSize() {
        return 99;
    }
}
