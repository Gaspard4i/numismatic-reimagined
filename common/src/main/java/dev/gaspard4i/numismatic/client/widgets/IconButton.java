package dev.gaspard4i.numismatic.client.widgets;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * Square button rendering an {@link ItemStack} (or a plain color) centered
 * inside a bordered box. The button is highlighted when hovered. Optional
 * tooltip is rendered by the parent screen via {@link #getTooltipText()}.
 */
public class IconButton extends AbstractButton {

    private final @Nullable ItemStack icon;
    private final Consumer<IconButton> onPress;
    private @Nullable Component tooltip;
    private boolean selected;

    public IconButton(int x, int y, int w, int h,
                      @Nullable ItemStack icon,
                      Component narration,
                      Consumer<IconButton> onPress) {
        super(x, y, w, h, narration);
        this.icon = icon;
        this.onPress = onPress;
    }

    public IconButton tooltip(Component text) {
        this.tooltip = text;
        return this;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isSelected() {
        return selected;
    }

    public @Nullable Component getTooltipText() {
        return tooltip;
    }

    @Override
    public void onPress() {
        onPress.accept(this);
    }

    @Override
    public void renderWidget(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        int bg = selected ? 0xFF5A5A5A : (isHovered() ? 0xFF4A4A4A : 0xFF2A2A2A);
        int border = selected ? 0xFFFFDD00 : 0xFF000000;
        g.fill(getX(), getY(), getX() + width, getY() + height, border);
        g.fill(getX() + 1, getY() + 1, getX() + width - 1, getY() + height - 1, bg);
        if (icon != null && !icon.isEmpty()) {
            int ix = getX() + (width - 16) / 2;
            int iy = getY() + (height - 16) / 2;
            g.renderItem(icon, ix, iy);
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput out) {
        this.defaultButtonNarrationText(out);
    }
}
