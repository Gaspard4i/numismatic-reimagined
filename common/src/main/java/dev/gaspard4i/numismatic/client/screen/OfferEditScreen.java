package dev.gaspard4i.numismatic.client.screen;

import dev.gaspard4i.numismatic.client.ClientShopState;
import dev.gaspard4i.numismatic.network.NumismaticNetworking;
import dev.gaspard4i.numismatic.shop.ShopOffer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * Modal popup to create / edit a single shop offer. Captures the player's
 * main-hand item as the template (item is not consumed) so they can
 * iterate quickly without opening a creative dropper.
 */
public class OfferEditScreen extends Screen {

    private final ShopScreen parent;
    private final int slotIndex;
    private ItemStack template;
    private long price;
    private int qty;

    private EditBox priceBox;
    private EditBox qtyBox;

    public OfferEditScreen(ShopScreen parent, int slotIndex, ItemStack template, long price, int qty) {
        super(Component.translatable("gui.numismatic_reimagined.shop.edit_offer"));
        this.parent = parent;
        this.slotIndex = slotIndex;
        this.template = template.copy();
        this.price = Math.max(1, price);
        this.qty = Math.max(1, qty);
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        int cy = this.height / 2;

        priceBox = new EditBox(this.font, cx - 60, cy - 6, 120, 18,
                Component.translatable("gui.numismatic_reimagined.shop.price"));
        priceBox.setValue(String.valueOf(price));
        priceBox.setFilter(s -> s.matches("\\d{0,15}"));
        addRenderableWidget(priceBox);

        qtyBox = new EditBox(this.font, cx - 60, cy + 18, 120, 18,
                Component.translatable("gui.numismatic_reimagined.shop.quantity"));
        qtyBox.setValue(String.valueOf(qty));
        qtyBox.setFilter(s -> s.matches("\\d{0,3}"));
        addRenderableWidget(qtyBox);

        addRenderableWidget(Button.builder(
                Component.translatable("gui.numismatic_reimagined.shop.use_held_item"),
                b -> {
                    if (Minecraft.getInstance().player != null) {
                        ItemStack held = Minecraft.getInstance().player.getMainHandItem();
                        if (!held.isEmpty()) template = held.copy();
                    }
                })
                .bounds(cx - 60, cy + 42, 120, 18).build());

        addRenderableWidget(Button.builder(Component.translatable("gui.numismatic_reimagined.shop.confirm"),
                b -> confirm())
                .bounds(cx - 90, cy + 70, 80, 18).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.numismatic_reimagined.shop.cancel"),
                b -> Minecraft.getInstance().setScreen(parent))
                .bounds(cx + 10, cy + 70, 80, 18).build());
    }

    private void confirm() {
        if (template.isEmpty() || !ShopOffer.isTemplateAllowed(template)) return;
        long p;
        int q;
        try {
            p = Long.parseLong(priceBox.getValue());
            q = Integer.parseInt(qtyBox.getValue());
        } catch (NumberFormatException ex) { return; }
        if (p <= 0 || q <= 0) return;
        if (ClientShopState.getPos() == null) return;
        NumismaticNetworking.sendEditOffer(ClientShopState.getPos(), slotIndex, template, p, q);
        Minecraft.getInstance().setScreen(parent);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g);
        int cx = this.width / 2;
        int cy = this.height / 2;
        g.fill(cx - 110, cy - 80, cx + 110, cy + 100, 0xE0101010);
        g.drawCenteredString(this.font, this.title, cx, cy - 75, 0xFFFFFF);
        g.drawString(this.font, Component.translatable("gui.numismatic_reimagined.shop.template"),
                cx - 60, cy - 60, 0xAAAAAA, false);
        g.fill(cx - 10, cy - 50, cx + 10, cy - 30, 0xFF333333);
        if (!template.isEmpty()) {
            g.renderItem(template, cx - 8, cy - 48);
            g.renderItemDecorations(this.font, template, cx - 8, cy - 48);
        }
        g.drawString(this.font, Component.translatable("gui.numismatic_reimagined.shop.price"),
                cx - 60, cy - 18, 0xAAAAAA, false);
        g.drawString(this.font, Component.translatable("gui.numismatic_reimagined.shop.quantity"),
                cx - 60, cy + 6, 0xAAAAAA, false);
        super.render(g, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
