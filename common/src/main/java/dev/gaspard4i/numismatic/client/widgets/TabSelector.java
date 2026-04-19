package dev.gaspard4i.numismatic.client.widgets;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;

/**
 * Vertical column of {@link IconButton} tabs rendered on the left side of
 * a screen. The selected tab is highlighted; clicking another tab raises
 * {@code onSelect} with its index.
 */
public class TabSelector extends CompositeWidget {

    private static final int TAB_W = 24;
    private static final int TAB_H = 28;
    private static final int TAB_SPACING = 2;

    private final List<IconButton> tabs = new ArrayList<>();
    private int selectedIndex = 0;

    public TabSelector(int x, int y) {
        super(x, y, TAB_W, 0);
    }

    public TabSelector addTab(ItemStack icon, Component tooltip, IntConsumer onSelect) {
        int index = tabs.size();
        int tabY = this.y + index * (TAB_H + TAB_SPACING);
        IconButton button = new IconButton(this.x, tabY, TAB_W, TAB_H, icon, tooltip, b -> {
            setSelectedIndex(index);
            onSelect.accept(index);
        }).tooltip(tooltip);
        button.setSelected(index == selectedIndex);
        tabs.add(button);
        add(button);
        this.height = tabs.size() * (TAB_H + TAB_SPACING);
        return this;
    }

    public void setSelectedIndex(int index) {
        this.selectedIndex = index;
        for (int i = 0; i < tabs.size(); i++) {
            tabs.get(i).setSelected(i == index);
        }
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }
}
