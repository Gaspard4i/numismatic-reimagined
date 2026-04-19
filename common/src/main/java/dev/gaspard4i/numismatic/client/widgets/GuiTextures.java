package dev.gaspard4i.numismatic.client.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.gaspard4i.numismatic.NumismaticConstants;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

/**
 * Centralized atlas of GUI texture regions used by the mod. Each constant
 * names a sprite (its owning texture + its x/y/width/height rectangle) and
 * can render itself at a target screen position.
 *
 * <p>Design inspired by the atlas pattern used by Create
 * ({@code AllGuiTextures}), adapted to vanilla {@link GuiGraphics}.
 */
public enum GuiTextures {

    SHOP_GUI           ("shop_gui",          0,   0, 176, 166, 256, 256),
    SHOP_GUI_TRADES    ("shop_gui_trades",   0,   0, 176, 166, 256, 256),
    PURSE_WIDGET       ("purse_widget",      0,   0, 128,  96, 256, 256);

    public final ResourceLocation texture;
    public final int u, v, width, height, textureWidth, textureHeight;

    GuiTextures(String path, int u, int v, int width, int height, int texW, int texH) {
        this.texture = new ResourceLocation(NumismaticConstants.MOD_ID, "textures/gui/" + path + ".png");
        this.u = u;
        this.v = v;
        this.width = width;
        this.height = height;
        this.textureWidth = texW;
        this.textureHeight = texH;
    }

    public void render(GuiGraphics g, int x, int y) {
        RenderSystem.setShaderColor(1, 1, 1, 1);
        g.blit(texture, x, y, u, v, width, height, textureWidth, textureHeight);
    }

    public void render(GuiGraphics g, int x, int y, int width, int height) {
        RenderSystem.setShaderColor(1, 1, 1, 1);
        g.blit(texture, x, y, u, v, width, height, textureWidth, textureHeight);
    }
}
