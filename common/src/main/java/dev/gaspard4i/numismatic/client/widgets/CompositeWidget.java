package dev.gaspard4i.numismatic.client.widgets;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Container widget that aggregates renderable + event-listener children.
 * Lets a {@code Screen} compose complex panels by stacking simpler widgets
 * (buttons, lists, labels) without leaking the child lifecycle into the
 * screen itself.
 *
 * <p>Pattern adapted from Create's {@code foundation.gui.CompositeWidget}.
 */
public class CompositeWidget extends AbstractContainerEventHandler
        implements Renderable, NarratableEntry {

    protected final List<GuiEventListener> children = new ArrayList<>();
    protected final List<Renderable> renderables = new ArrayList<>();
    protected int x, y, width, height;
    protected boolean visible = true;

    public CompositeWidget(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public <T extends GuiEventListener> T add(T child) {
        this.children.add(child);
        if (child instanceof Renderable r) this.renderables.add(r);
        return child;
    }

    public <T extends Renderable> T addRenderable(T r) {
        this.renderables.add(r);
        if (r instanceof GuiEventListener l) this.children.add(l);
        return r;
    }

    public void clear() {
        this.children.clear();
        this.renderables.clear();
    }

    public boolean isVisible() { return visible; }
    public void setVisible(boolean visible) { this.visible = visible; }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }

    @Override
    public @NotNull List<? extends GuiEventListener> children() {
        return visible ? this.children : Collections.emptyList();
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        if (!visible) return;
        for (Renderable r : this.renderables) r.render(g, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!visible) return false;
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (!visible) return false;
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!visible) return false;
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    // NarratableEntry — quietest default; screen readers ignore the composite.

    @Override
    public @NotNull NarrationPriority narrationPriority() {
        return NarrationPriority.NONE;
    }

    @Override
    public void updateNarration(@NotNull NarrationElementOutput output) {
        // Child narrations are collected by the parent screen.
    }
}
