package dev.gaspard4i.numismatic.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.gaspard4i.numismatic.shop.ShopBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * Renders the currently-displayed shop offer as a floating, slowly
 * rotating item hologram above the plate of a shop block.
 */
public class ShopBlockEntityRenderer implements BlockEntityRenderer<ShopBlockEntity> {

    public ShopBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
    }

    @Override
    public void render(ShopBlockEntity be, float partialTick, PoseStack pose,
                       MultiBufferSource buffers, int light, int overlay) {
        ItemStack stack = be.getItemToRender();
        if (stack.isEmpty()) return;

        long gameTime = Minecraft.getInstance().level != null
                ? Minecraft.getInstance().level.getGameTime() : 0L;
        float angle = ((gameTime + partialTick) % 360);
        float bob = Mth.sin((gameTime + partialTick) / 10f) * 0.05f;

        pose.pushPose();
        pose.translate(0.5, 1.15 + bob, 0.5);
        pose.scale(0.6f, 0.6f, 0.6f);
        pose.mulPose(com.mojang.math.Axis.YP.rotationDegrees(angle * 2f));
        Minecraft.getInstance().getItemRenderer().renderStatic(
                stack, ItemDisplayContext.GROUND,
                light, overlay, pose, buffers, be.getLevel(), 0);
        pose.popPose();
    }
}
