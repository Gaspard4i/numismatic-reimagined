package dev.gaspard4i.numismatic.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.gaspard4i.numismatic.client.widgets.GuiTextures;
import dev.gaspard4i.numismatic.currency.Currency;
import dev.gaspard4i.numismatic.network.ClientCurrencyData;
import dev.gaspard4i.numismatic.network.NumismaticNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * Popup screen showing the player's purse balance with per-denomination
 * extractors. Layout: 4 rows (one per denomination), each with a label,
 * a value display, and -/+ buttons. Bottom row is the Extract button.
 */
public class PurseScreen extends Screen {

    private static final ResourceLocation BG = GuiTextures.PURSE_WIDGET.texture;
    private static final int BG_W = 176;
    private static final int BG_H = 120;
    private static final int ROW_H = 18;
    private static final int LABEL_X = 8;
    private static final int VALUE_X = 64;
    private static final int MINUS_X = 100;
    private static final int PLUS_X = 138;
    private static final int BTN_W = 14;

    private final Screen parent;

    private long bronzeToExtract;
    private long silverToExtract;
    private long goldToExtract;
    private long netheriteToExtract;

    private int leftPos;
    private int topPos;

    public PurseScreen(Screen parent) {
        super(Component.translatable("gui.numismatic_reimagined.purse"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        this.leftPos = (this.width - BG_W) / 2;
        this.topPos = (this.height - BG_H) / 2;

        addRow(0, Currency.BRONZE);
        addRow(1, Currency.SILVER);
        addRow(2, Currency.GOLD);
        addRow(3, Currency.NETHERITE);

        // Extract button: full width below the rows
        addRenderableWidget(Button.builder(
                Component.translatable("gui.numismatic_reimagined.purse.extract"),
                b -> extract())
                .bounds(leftPos + 8, topPos + 18 + 4 * ROW_H + 4, BG_W - 16, 18)
                .build());
    }

    private void addRow(int rowIndex, Currency c) {
        int y = topPos + 18 + rowIndex * ROW_H;

        addRenderableWidget(Button.builder(Component.literal("-"),
                b -> decrement(c))
                .bounds(leftPos + MINUS_X, y, BTN_W, 14).build());

        addRenderableWidget(Button.builder(Component.literal("+"),
                b -> increment(c))
                .bounds(leftPos + PLUS_X, y, BTN_W, 14).build());
    }

    private long getValue(Currency c) {
        return switch (c) {
            case BRONZE -> bronzeToExtract;
            case SILVER -> silverToExtract;
            case GOLD -> goldToExtract;
            case NETHERITE -> netheriteToExtract;
        };
    }

    private void setValue(Currency c, long v) {
        switch (c) {
            case BRONZE -> bronzeToExtract = v;
            case SILVER -> silverToExtract = v;
            case GOLD -> goldToExtract = v;
            case NETHERITE -> netheriteToExtract = v;
        }
    }

    private long availableForCurrency(Currency c) {
        long otherCost = 0;
        for (Currency oc : Currency.values()) {
            if (oc != c) otherCost += getValue(oc) * oc.getValue();
        }
        long remaining = ClientCurrencyData.getBalance() - otherCost;
        return Math.max(0, remaining / c.getValue());
    }

    private void increment(Currency c) {
        long v = getValue(c);
        long max = availableForCurrency(c);
        if (v < max) setValue(c, v + (hasShiftDown() ? Math.min(10, max - v) : 1));
    }

    private void decrement(Currency c) {
        long v = getValue(c);
        if (v > 0) setValue(c, Math.max(0, v - (hasShiftDown() ? 10 : 1)));
    }

    private long totalToExtract() {
        return bronzeToExtract * Currency.BRONZE.getValue()
                + silverToExtract * Currency.SILVER.getValue()
                + goldToExtract * Currency.GOLD.getValue()
                + netheriteToExtract * Currency.NETHERITE.getValue();
    }

    private void extract() {
        long total = totalToExtract();
        if (hasShiftDown() && hasControlDown()) {
            total = ClientCurrencyData.getBalance();
        }
        if (total <= 0) return;
        NumismaticNetworking.sendWithdraw(total);
        Minecraft.getInstance().setScreen(parent);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g);

        // Solid panel background (texture is just decorative if mismatched size)
        g.fill(leftPos, topPos, leftPos + BG_W, topPos + BG_H, 0xFF000000);
        g.fill(leftPos + 1, topPos + 1, leftPos + BG_W - 1, topPos + BG_H - 1, 0xFFC6C6C6);

        // Header
        g.drawCenteredString(this.font, this.title, leftPos + BG_W / 2, topPos + 5, 0x404040);

        // Balance summary
        String balance = String.format("Total: %,d", ClientCurrencyData.getBalance());
        g.drawString(this.font, balance, leftPos + 8, topPos + BG_H - 10, 0x404040, false);

        // Per-row labels (label + amount)
        renderRow(g, 0, Currency.BRONZE, "Bronze", 0xC08050);
        renderRow(g, 1, Currency.SILVER, "Silver", 0x808080);
        renderRow(g, 2, Currency.GOLD, "Gold", 0xC0A040);
        renderRow(g, 3, Currency.NETHERITE, "Netherite", 0x402030);

        super.render(g, mouseX, mouseY, partialTick);
    }

    private void renderRow(GuiGraphics g, int rowIndex, Currency c, String label, int color) {
        int y = topPos + 18 + rowIndex * ROW_H;
        // Row background
        g.fill(leftPos + 4, y - 2, leftPos + BG_W - 4, y + 14, 0xFF8B8B8B);
        // Label
        g.drawString(this.font, label, leftPos + LABEL_X, y + 3, color, false);
        // Value
        String text = String.valueOf(getValue(c));
        g.drawString(this.font, text, leftPos + VALUE_X, y + 3, 0x000000, false);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
