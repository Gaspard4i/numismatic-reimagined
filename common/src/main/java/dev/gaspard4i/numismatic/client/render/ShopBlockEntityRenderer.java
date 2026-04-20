package dev.gaspard4i.numismatic.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.gaspard4i.numismatic.shop.ShopBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Floating hologram of the currently displayed shop offer, ported from
 * wisp-forest/numismatic-overhaul {@code ShopBlockEntityRender}.
 */
public class ShopBlockEntityRenderer implements BlockEntityRenderer<ShopBlockEntity> {

    public ShopBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
    }

    @Override
    public void render(ShopBlockEntity be, float partialTick, PoseStack matrices,
                       MultiBufferSource buffers, int light, int overlay) {
        Minecraft mc = Minecraft.getInstance();
        Level world = be.getLevel();
        if (world == null || be.getOffers().isEmpty()) return;

        ItemStack toRender = be.getItemToRender();
        if (toRender.isEmpty()) return;

        boolean isBlockItem = toRender.getItem() instanceof BlockItem;
        int lightAbove = LevelRenderer.getLightColor(world, be.getBlockPos().above());

        matrices.pushPose();
        matrices.translate(0.5, isBlockItem ? 0.85 : 0.95, 0.5);

        float scale = isBlockItem ? 0.95f : 0.85f;
        matrices.scale(scale, scale, scale);

        matrices.mulPose(Axis.YP.rotationDegrees(
                (float) (System.currentTimeMillis() / 20d % 360d)));

        mc.getItemRenderer().renderStatic(
                toRender, ItemDisplayContext.GROUND,
                lightAbove, OverlayTexture.NO_OVERLAY,
                matrices, buffers, world, 0);

        matrices.popPose();
    }
}
