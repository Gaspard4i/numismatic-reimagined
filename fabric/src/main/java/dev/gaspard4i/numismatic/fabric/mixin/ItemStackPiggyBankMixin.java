package dev.gaspard4i.numismatic.fabric.mixin;

import dev.gaspard4i.numismatic.block.PiggyBankBlockItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Makes {@link ItemStack#getMaxStackSize()} return 1 for piggy-bank block
 * items that currently hold any stored value. Empty piggy banks remain
 * stackable (default 64 from the item's {@code stacksTo(64)} setting).
 */
@Mixin(ItemStack.class)
public abstract class ItemStackPiggyBankMixin {

    @Inject(method = "getMaxStackSize()I", at = @At("RETURN"), cancellable = true)
    private void numismatic$piggyBankMaxStack(CallbackInfoReturnable<Integer> cir) {
        ItemStack self = (ItemStack) (Object) this;
        if (self.getItem() instanceof PiggyBankBlockItem && PiggyBankBlockItem.hasContents(self)) {
            cir.setReturnValue(1);
        }
    }
}
