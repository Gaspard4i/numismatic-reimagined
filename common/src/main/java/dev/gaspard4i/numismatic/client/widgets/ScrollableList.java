package dev.gaspard4i.numismatic.client.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Generic scrollable list. Each entry is a fixed-height row; the list
 * paginates rows and exposes a scrollbar when content exceeds the visible
 * area. Clicks and scroll wheel are routed to the hovered entry.
 *
 * <p>Entries extend {@link Entry} and implement their own {@code render}
 * and {@code mouseClicked}.
 */
public class ScrollableList<T extends ScrollableList.Entry> extends AbstractContainerEventHandler
        implements Renderable, NarratableEntry {

    protected final int x, y, width, height;
    protected final int rowHeight;
    protected final List<T> entries = new ArrayList<>();
    protected int scroll = 0;

    public ScrollableList(int x, int y, int width, int height, int rowHeight) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.rowHeight = rowHeight;
    }

    public void addEntry(T entry) {
        entries.add(entry);
    }

    public void clearEntries() {
        entries.clear();
        scroll = 0;
    }

    public List<T> getEntries() {
        return entries;
    }

    public int getVisibleRows() {
        return Math.max(1, height / rowHeight);
    }

    public int getMaxScroll() {
        return Math.max(0, entries.size() - getVisibleRows());
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        g.enableScissor(x, y, x + width, y + height);
        int visibleRows = getVisibleRows();
        for (int i = 0; i < visibleRows; i++) {
            int idx = scroll + i;
            if (idx >= entries.size()) break;
            T entry = entries.get(idx);
            int ey = y + i * rowHeight;
            boolean hovered = mouseX >= x && mouseX < x + width
                    && mouseY >= ey && mouseY < ey + rowHeight;
            entry.render(g, x, ey, width, rowHeight, mouseX, mouseY, hovered, partialTick);
        }
        g.disableScissor();

        // Scrollbar on the right edge, 4px wide.
        if (getMaxScroll() > 0) {
            int barX = x + width - 4;
            g.fill(barX, y, barX + 4, y + height, 0xFF202020);
            int thumbH = Math.max(10, height * getVisibleRows() / entries.size());
            int thumbY = y + (height - thumbH) * scroll / Math.max(1, getMaxScroll());
            g.fill(barX, thumbY, barX + 4, thumbY + thumbH, 0xFFAAAAAA);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (mouseX < x || mouseX >= x + width || mouseY < y || mouseY >= y + height) return false;
        int i = (int) ((mouseY - y) / rowHeight);
        int idx = scroll + i;
        if (idx < 0 || idx >= entries.size()) return false;
        return entries.get(idx).mouseClicked(mouseX - x, mouseY - y - i * rowHeight, button, idx);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (mouseX < x || mouseX >= x + width || mouseY < y || mouseY >= y + height) return false;
        int max = getMaxScroll();
        if (delta > 0 && scroll > 0) scroll--;
        else if (delta < 0 && scroll < max) scroll++;
        return true;
    }

    @Override
    public @NotNull List<? extends GuiEventListener> children() {
        return List.of();
    }

    @Override
    public @NotNull NarrationPriority narrationPriority() { return NarrationPriority.NONE; }

    @Override
    public void updateNarration(@NotNull NarrationElementOutput out) {}

    /**
     * Individual row. Override {@link #render} to draw; override
     * {@link #mouseClicked} to react to clicks.
     */
    public interface Entry {
        void render(GuiGraphics g, int x, int y, int width, int height,
                    int mouseX, int mouseY, boolean hovered, float partialTick);

        default boolean mouseClicked(double relX, double relY, int button, int entryIndex) {
            return false;
        }
    }
}
