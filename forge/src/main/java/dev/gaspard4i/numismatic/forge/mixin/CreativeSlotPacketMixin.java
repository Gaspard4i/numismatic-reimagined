package dev.gaspard4i.numismatic.forge.mixin;

import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Lifts the hardcoded {@code stack.getCount() <= 64} check in
 * {@code handleSetCreativeModeSlot}. Vanilla silently drops any
 * creative-mode pickup whose count exceeds 64, even when the item declares
 * a higher {@code stacksTo} (e.g., 99). The client still shows the stack
 * (visual only) but the server has nothing — so the next container open
 * resyncs the slot to empty and the items "vanish."
 */
@Mixin(ServerGamePacketListenerImpl.class)
public class CreativeSlotPacketMixin {

    @ModifyConstant(
            method = "handleSetCreativeModeSlot",
            constant = @org.spongepowered.asm.mixin.injection.Constant(intValue = 64)
    )
    private int numismatic$raiseCreativeSlotMax(int original) {
        return 99;
    }
}
