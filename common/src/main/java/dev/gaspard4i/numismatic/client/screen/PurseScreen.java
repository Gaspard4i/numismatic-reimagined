package dev.gaspard4i.numismatic.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.gaspard4i.numismatic.NumismaticConstants;
import dev.gaspard4i.numismatic.client.widgets.GuiTextures;
import dev.gaspard4i.numismatic.currency.Currency;
import dev.gaspard4i.numismatic.network.ClientCurrencyData;
import dev.gaspard4i.numismatic.network.NumismaticNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * Popup screen showing the player's purse balance with per-denomination
 * extractors. Users can select how many bronze/silver/gold/netherite coins
 * to extract, then confirm to convert part of their virtual balance into
 * physical coins in their inventory.
 */
public class PurseScreen extends Screen {

    private static final ResourceLocation BG = GuiTextures.PURSE_WIDGET.texture;
    private static final int BG_WIDTH = 128;
    private static final int BG_HEIGHT = 96;

    private final Screen parent;

    private long bronzeToExtract = 0;
    private long silverToExtract = 0;
    private long goldToExtract = 0;
    private long netheriteToExtract = 0;

    private int leftPos;
    private int topPos;

    public PurseScreen(Screen parent) {
        super(Component.translatable("gui.numismatic_reimagined.purse"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        this.leftPos = (this.width - BG_WIDTH) / 2;
        this.topPos = (this.height - BG_HEIGHT) / 2;

        // Row layout: one row per denomination with [-] [count] [+]
        addDenominationRow(Currency.BRONZE, 18, () -> bronzeToExtract, v -> bronzeToExtract = v);
        addDenominationRow(Currency.SILVER, 32, () -> silverToExtract, v -> silverToExtract = v);
        addDenominationRow(Currency.GOLD, 46, () -> goldToExtract, v -> goldToExtract = v);
        addDenominationRow(Currency.NETHERITE, 60, () -> netheriteToExtract, v -> netheriteToExtract = v);

        // Extract button at the bottom
        addRenderableWidget(Button.builder(Component.translatable("gui.numismatic_reimagined.purse.extract"),
                b -> extract())
                .bounds(leftPos + 8, topPos + BG_HEIGHT - 22, BG_WIDTH - 16, 18)
                .build());
    }

    private void addDenominationRow(Currency currency, int yOffset,
                                    java.util.function.LongSupplier getter,
                                    java.util.function.LongConsumer setter) {
        int rowY = topPos + yOffset;

        addRenderableWidget(Button.builder(Component.literal("-"), b -> {
            long v = getter.getAsLong();
            long maxAvail = ClientCurrencyData.getBalance() / currency.getValue();
            if (v > 0) setter.accept(v - 1);
        }).bounds(leftPos + 70, rowY, 14, 12).build());

        addRenderableWidget(Button.builder(Component.literal("+"), b -> {
            long v = getter.getAsLong();
            long maxAvail = availableForDenomination(currency);
            if (v < maxAvail) setter.accept(v + 1);
        }).bounds(leftPos + 110, rowY, 14, 12).build());
    }

    /**
     * How many coins of this denomination can still be extracted, given the
     * current balance and the already-queued extractions of higher priority
     * denominations.
     */
    private long availableForDenomination(Currency c) {
        long remaining = ClientCurrencyData.getBalance()
                - bronzeToExtract * Currency.BRONZE.getValue()
                - silverToExtract * Currency.SILVER.getValue()
                - goldToExtract * Currency.GOLD.getValue()
                - netheriteToExtract * Currency.NETHERITE.getValue();
        // Add back the one we're recomputing so the user can still increment
        // that row up to its local max.
        switch (c) {
            case BRONZE -> remaining += bronzeToExtract * c.getValue();
            case SILVER -> remaining += silverToExtract * c.getValue();
            case GOLD -> remaining += goldToExtract * c.getValue();
            case NETHERITE -> remaining += netheriteToExtract * c.getValue();
        }
        return remaining / c.getValue();
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
            // Extract everything in the purse
            total = ClientCurrencyData.getBalance();
        }
        if (total <= 0) return;
        NumismaticNetworking.sendWithdraw(total);
        if (Minecraft.getInstance() != null) {
            Minecraft.getInstance().setScreen(parent);
        }
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g);
        // Background panel
        RenderSystem.setShaderColor(1, 1, 1, 1);
        g.blit(BG, leftPos, topPos, 0, 0, BG_WIDTH, BG_HEIGHT, 256, 256);

        // Title
        g.drawString(this.font, this.title, leftPos + 8, topPos + 6, 0x404040, false);

        // Balance display
        long balance = ClientCurrencyData.getBalance();
        String balanceText = String.format("%,d ✦", balance);
        g.drawString(this.font, balanceText, leftPos + 8, topPos + BG_HEIGHT - 40, 0xFFD700, false);

        // Draw each denomination row
        drawDenominationRow(g, Currency.BRONZE, 18, bronzeToExtract, 0xC08050);
        drawDenominationRow(g, Currency.SILVER, 32, silverToExtract, 0xC0C0C0);
        drawDenominationRow(g, Currency.GOLD, 46, goldToExtract, 0xFFD700);
        drawDenominationRow(g, Currency.NETHERITE, 60, netheriteToExtract, 0x604060);

        super.render(g, mouseX, mouseY, partialTick);
    }

    private void drawDenominationRow(GuiGraphics g, Currency c, int yOffset, long value, int color) {
        int rowY = topPos + yOffset;
        g.drawString(this.font, c.name().substring(0, 1) + c.name().substring(1).toLowerCase(),
                leftPos + 8, rowY + 2, color, false);
        g.drawString(this.font, String.format("%d", value),
                leftPos + 90, rowY + 2, 0xFFFFFF, false);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
