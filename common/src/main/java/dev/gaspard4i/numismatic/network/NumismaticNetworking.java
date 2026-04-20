package dev.gaspard4i.numismatic.network;

import dev.architectury.networking.NetworkManager;
import dev.gaspard4i.numismatic.NumismaticConstants;
import dev.gaspard4i.numismatic.currency.CurrencyHelper;
import dev.gaspard4i.numismatic.currency.PlayerCurrencyManager;
import dev.gaspard4i.numismatic.item.MoneyBagItem;
import dev.gaspard4i.numismatic.shop.ShopBlockEntity;
import dev.gaspard4i.numismatic.shop.ShopOffer;
import dev.gaspard4i.numismatic.shop.ShopPaymentHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * All network packets for the currency + shop systems.
 */
public final class NumismaticNetworking {

    public static final ResourceLocation SYNC_CURRENCY_S2C = id("sync_currency");
    public static final ResourceLocation DEPOSIT_ALL_C2S = id("deposit_all");
    public static final ResourceLocation WITHDRAW_C2S = id("withdraw");

    public static final ResourceLocation SYNC_SHOP_STATE_S2C = id("sync_shop_state");
    public static final ResourceLocation EDIT_OFFER_C2S = id("edit_offer");
    public static final ResourceLocation REMOVE_OFFER_C2S = id("remove_offer");
    public static final ResourceLocation PURCHASE_OFFER_C2S = id("purchase_offer");
    public static final ResourceLocation WITHDRAW_REVENUE_C2S = id("withdraw_revenue");
    public static final ResourceLocation TOGGLE_TRANSFER_C2S = id("toggle_transfer");

    private NumismaticNetworking() {}

    private static ResourceLocation id(String path) {
        return new ResourceLocation(NumismaticConstants.MOD_ID, path);
    }

    public static void registerServerReceivers() {
        // ---- currency ----
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, DEPOSIT_ALL_C2S, (buf, ctx) -> ctx.queue(() -> {
            if (ctx.getPlayer() instanceof ServerPlayer sp) {
                ServerLevel overworld = sp.server.overworld();
                PlayerCurrencyManager mgr = PlayerCurrencyManager.get(overworld);
                long deposited = CurrencyHelper.depositAllCoins(sp, mgr);
                if (deposited > 0) syncToClient(sp, mgr);
            }
        }));

        NetworkManager.registerReceiver(NetworkManager.Side.C2S, WITHDRAW_C2S, (buf, ctx) -> {
            long amount = buf.readLong();
            ctx.queue(() -> {
                if (ctx.getPlayer() instanceof ServerPlayer sp && amount > 0) {
                    ServerLevel overworld = sp.server.overworld();
                    PlayerCurrencyManager mgr = PlayerCurrencyManager.get(overworld);
                    if (mgr.subtractBalance(sp.getUUID(), amount)) {
                        CurrencyHelper.offerAsCoins(sp, amount);
                        syncToClient(sp, mgr);
                    }
                }
            });
        });

        // ---- shop ----
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, EDIT_OFFER_C2S, (buf, ctx) -> {
            BlockPos pos = buf.readBlockPos();
            int slotIndex = buf.readInt();
            ItemStack template = buf.readItem();
            long price = buf.readLong();
            int qty = buf.readVarInt();
            ctx.queue(() -> {
                if (!(ctx.getPlayer() instanceof ServerPlayer sp)) return;
                ShopBlockEntity shop = resolveShop(sp.serverLevel(), pos);
                if (shop == null || !shop.canEdit(sp)) return;
                ShopOffer offer;
                try {
                    offer = new ShopOffer(template, price, qty);
                } catch (IllegalArgumentException ex) { return; }
                synchronized (shop) {
                    if (slotIndex < 0) shop.getOffers().add(offer);
                    else shop.getOffers().replace(slotIndex, offer);
                    shop.setChanged();
                }
                syncShopState(sp, shop);
            });
        });

        NetworkManager.registerReceiver(NetworkManager.Side.C2S, REMOVE_OFFER_C2S, (buf, ctx) -> {
            BlockPos pos = buf.readBlockPos();
            int slotIndex = buf.readInt();
            ctx.queue(() -> {
                if (!(ctx.getPlayer() instanceof ServerPlayer sp)) return;
                ShopBlockEntity shop = resolveShop(sp.serverLevel(), pos);
                if (shop == null || !shop.canEdit(sp)) return;
                synchronized (shop) {
                    shop.getOffers().remove(slotIndex);
                    shop.setChanged();
                }
                syncShopState(sp, shop);
            });
        });

        NetworkManager.registerReceiver(NetworkManager.Side.C2S, PURCHASE_OFFER_C2S, (buf, ctx) -> {
            BlockPos pos = buf.readBlockPos();
            int slotIndex = buf.readInt();
            ctx.queue(() -> {
                if (!(ctx.getPlayer() instanceof ServerPlayer sp)) return;
                ShopBlockEntity shop = resolveShop(sp.serverLevel(), pos);
                if (shop == null) return;
                synchronized (shop) {
                    ShopOffer offer = shop.getOffers().get(slotIndex);
                    if (offer == null) return;
                    if (!shop.hasStockFor(offer)) return;
                    if (ShopPaymentHelper.countCoinsAndBags(sp) < offer.priceBronze()) return;
                    if (!ShopPaymentHelper.tryDeductFromInventory(sp, offer.priceBronze())) return;
                    shop.consumeStock(offer);
                    ItemStack purchased = offer.createPurchasedStack();
                    if (!sp.getInventory().add(purchased)) sp.drop(purchased, false);
                    shop.addRevenue(offer.priceBronze());
                }
                syncShopState(sp, shop);
            });
        });

        NetworkManager.registerReceiver(NetworkManager.Side.C2S, WITHDRAW_REVENUE_C2S, (buf, ctx) -> {
            BlockPos pos = buf.readBlockPos();
            ctx.queue(() -> {
                if (!(ctx.getPlayer() instanceof ServerPlayer sp)) return;
                ShopBlockEntity shop = resolveShop(sp.serverLevel(), pos);
                if (shop == null || !shop.canEdit(sp) || shop.isAdmin()) return;
                long revenue;
                synchronized (shop) {
                    revenue = shop.withdrawRevenue();
                }
                if (revenue > 0) {
                    ItemStack bag = MoneyBagItem.createWithValue(revenue);
                    if (!sp.getInventory().add(bag)) sp.drop(bag, false);
                }
                syncShopState(sp, shop);
            });
        });

        NetworkManager.registerReceiver(NetworkManager.Side.C2S, TOGGLE_TRANSFER_C2S, (buf, ctx) -> {
            BlockPos pos = buf.readBlockPos();
            ctx.queue(() -> {
                if (!(ctx.getPlayer() instanceof ServerPlayer sp)) return;
                ShopBlockEntity shop = resolveShop(sp.serverLevel(), pos);
                if (shop == null || !shop.canEdit(sp)) return;
                synchronized (shop) { shop.toggleTransfer(); }
                syncShopState(sp, shop);
            });
        });
    }

    private static ShopBlockEntity resolveShop(Level level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        return be instanceof ShopBlockEntity s ? s : null;
    }

    // ---- send helpers ----

    public static void syncToClient(ServerPlayer player, PlayerCurrencyManager manager) {
        long balance = manager.getBalance(player.getUUID());
        FriendlyByteBuf buf = new FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
        buf.writeLong(balance);
        NetworkManager.sendToPlayer(player, SYNC_CURRENCY_S2C, buf);
    }

    public static void syncShopState(ServerPlayer player, ShopBlockEntity shop) {
        FriendlyByteBuf buf = new FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
        buf.writeBlockPos(shop.getBlockPos());
        buf.writeBoolean(shop.canEdit(player));
        buf.writeBoolean(shop.isAdmin());
        buf.writeBoolean(shop.allowsTransfer());
        buf.writeNbt(shop.getOffers().toTag());
        buf.writeLong(shop.getAccumulatedRevenue());
        NetworkManager.sendToPlayer(player, SYNC_SHOP_STATE_S2C, buf);
    }

    public static void sendToggleTransfer(BlockPos pos) {
        FriendlyByteBuf buf = new FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
        buf.writeBlockPos(pos);
        NetworkManager.sendToServer(TOGGLE_TRANSFER_C2S, buf);
    }

    public static void sendDepositAll() {
        FriendlyByteBuf buf = new FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
        NetworkManager.sendToServer(DEPOSIT_ALL_C2S, buf);
    }

    public static void sendWithdraw(long amount) {
        FriendlyByteBuf buf = new FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
        buf.writeLong(amount);
        NetworkManager.sendToServer(WITHDRAW_C2S, buf);
    }

    public static void sendEditOffer(BlockPos pos, int slotIndex, ItemStack template, long price, int qty) {
        FriendlyByteBuf buf = new FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
        buf.writeBlockPos(pos);
        buf.writeInt(slotIndex);
        buf.writeItem(template);
        buf.writeLong(price);
        buf.writeVarInt(qty);
        NetworkManager.sendToServer(EDIT_OFFER_C2S, buf);
    }

    public static void sendRemoveOffer(BlockPos pos, int slotIndex) {
        FriendlyByteBuf buf = new FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
        buf.writeBlockPos(pos);
        buf.writeInt(slotIndex);
        NetworkManager.sendToServer(REMOVE_OFFER_C2S, buf);
    }

    public static void sendPurchaseOffer(BlockPos pos, int slotIndex) {
        FriendlyByteBuf buf = new FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
        buf.writeBlockPos(pos);
        buf.writeInt(slotIndex);
        NetworkManager.sendToServer(PURCHASE_OFFER_C2S, buf);
    }

    public static void sendWithdrawRevenue(BlockPos pos) {
        FriendlyByteBuf buf = new FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
        buf.writeBlockPos(pos);
        NetworkManager.sendToServer(WITHDRAW_REVENUE_C2S, buf);
    }
}
