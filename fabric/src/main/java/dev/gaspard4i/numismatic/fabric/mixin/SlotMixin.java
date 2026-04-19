package dev.gaspard4i.numismatic.fabric.mixin;

import dev.gaspard4i.numismatic.item.CoinItem;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Allows coin slots to accept stacks up to {@code stack.getMaxStackSize()}
 * (e.g., 99) instead of the container's reported max (typically 64). This
 * fixes the silent server-side rejection of creative-mode pickups when the
 * stack exceeds 64 — without this, items "ghost" in the client and vanish
 * on the next container sync.
 */
@Mixin(Slot.class)
public class SlotMixin {

    @Inject(method = "getMaxStackSize(Lnet/minecraft/world/item/ItemStack;)I", at = @At("HEAD"), cancellable = true)
    private void numismatic$raiseMaxStackSizeForCoins(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        if (stack.getItem() instanceof CoinItem) {
            cir.setReturnValue(stack.getMaxStackSize());
        }
    }
}
