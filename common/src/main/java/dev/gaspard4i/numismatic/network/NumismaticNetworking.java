package dev.gaspard4i.numismatic.network;

import dev.architectury.networking.NetworkManager;
import dev.gaspard4i.numismatic.NumismaticConstants;
import dev.gaspard4i.numismatic.currency.CurrencyHelper;
import dev.gaspard4i.numismatic.currency.CurrencyResolver;
import dev.gaspard4i.numismatic.currency.PlayerCurrencyManager;
import dev.gaspard4i.numismatic.item.MoneyBagItem;
import dev.gaspard4i.numismatic.shop.OfferList;
import dev.gaspard4i.numismatic.shop.ShopBlockEntity;
import dev.gaspard4i.numismatic.shop.ShopMenuMode;
import dev.gaspard4i.numismatic.shop.ShopOffer;
import dev.gaspard4i.numismatic.shop.ShopPaymentHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Handles all network packets for the currency system.
 */
public final class NumismaticNetworking {

    // S2C: Sync the player's balance to the client
    public static final ResourceLocation SYNC_CURRENCY_S2C = new ResourceLocation(
            NumismaticConstants.MOD_ID, "sync_currency"
    );

    // C2S: Player requests to deposit all coins into purse
    public static final ResourceLocation DEPOSIT_ALL_C2S = new ResourceLocation(
            NumismaticConstants.MOD_ID, "deposit_all"
    );

    // C2S: Player requests to withdraw a specific amount from purse
    public static final ResourceLocation WITHDRAW_C2S = new ResourceLocation(
            NumismaticConstants.MOD_ID, "withdraw"
    );

    // S2C: Server tells client to open the shop screen with a given mode + state
    public static final ResourceLocation OPEN_SHOP_SCREEN_S2C = new ResourceLocation(
            NumismaticConstants.MOD_ID, "open_shop_screen"
    );

    // S2C: Server pushes updated shop state to a viewing client
    public static final ResourceLocation SYNC_SHOP_STATE_S2C = new ResourceLocation(
            NumismaticConstants.MOD_ID, "sync_shop_state"
    );

    // C2S: Owner edits an offer (slot index < 0 means append)
    public static final ResourceLocation EDIT_OFFER_C2S = new ResourceLocation(
            NumismaticConstants.MOD_ID, "edit_offer"
    );

    // C2S: Owner removes an offer
    public static final ResourceLocation REMOVE_OFFER_C2S = new ResourceLocation(
            NumismaticConstants.MOD_ID, "remove_offer"
    );

    // C2S: Buyer purchases an offer
    public static final ResourceLocation PURCHASE_OFFER_C2S = new ResourceLocation(
            NumismaticConstants.MOD_ID, "purchase_offer"
    );

    // C2S: Owner withdraws accumulated revenue
    public static final ResourceLocation WITHDRAW_REVENUE_C2S = new ResourceLocation(
            NumismaticConstants.MOD_ID, "withdraw_revenue"
    );

    // C2S: Player switches between shop tabs (owner mode)
    public static final ResourceLocation SWITCH_SHOP_TAB_C2S = new ResourceLocation(
            NumismaticConstants.MOD_ID, "switch_shop_tab"
    );

    private NumismaticNetworking() {}

    public static void registerServerReceivers() {
        // Handle deposit all request
        NetworkManager.registerReceiver(
                NetworkManager.Side.C2S,
                DEPOSIT_ALL_C2S,
                (buf, context) -> {
                    context.queue(() -> {
                        if (context.getPlayer() instanceof ServerPlayer serverPlayer) {
                            ServerLevel overworld = serverPlayer.server.overworld();
                            PlayerCurrencyManager manager = PlayerCurrencyManager.get(overworld);
                            long deposited = CurrencyHelper.depositAllCoins(serverPlayer, manager);
                            if (deposited > 0) {
                                syncToClient(serverPlayer, manager);
                            }
                        }
                    });
                }
        );

        // Handle withdraw request
        NetworkManager.registerReceiver(
                NetworkManager.Side.C2S,
                WITHDRAW_C2S,
                (buf, context) -> {
                    long amount = buf.readLong();
                    context.queue(() -> {
                        if (context.getPlayer() instanceof ServerPlayer serverPlayer) {
                            if (amount <= 0) return;
                            ServerLevel overworld = serverPlayer.server.overworld();
                            PlayerCurrencyManager manager = PlayerCurrencyManager.get(overworld);
                            if (manager.subtractBalance(serverPlayer.getUUID(), amount)) {
                                CurrencyHelper.offerAsCoins(serverPlayer, amount);
                                syncToClient(serverPlayer, manager);
                            }
                        }
                    });
                }
        );

        // ---------------- Shop receivers ----------------

        NetworkManager.registerReceiver(NetworkManager.Side.C2S, EDIT_OFFER_C2S, (buf, context) -> {
            BlockPos pos = buf.readBlockPos();
            int slotIndex = buf.readInt();
            ItemStack template = buf.readItem();
            long price = buf.readLong();
            int qty = buf.readVarInt();
            context.queue(() -> {
                if (!(context.getPlayer() instanceof ServerPlayer sp)) return;
                ShopBlockEntity shop = resolveShop(sp.serverLevel(), pos);
                if (shop == null || !shop.canEdit(sp)) return;
                ShopOffer offer;
                try {
                    offer = new ShopOffer(template, price, qty);
                } catch (IllegalArgumentException ex) {
                    return;
                }
                synchronized (shop) {
                    if (slotIndex < 0) shop.getOffers().add(offer);
                    else shop.getOffers().replace(slotIndex, offer);
                    shop.setChanged();
                }
                syncShopState(sp, shop);
            });
        });

        NetworkManager.registerReceiver(NetworkManager.Side.C2S, REMOVE_OFFER_C2S, (buf, context) -> {
            BlockPos pos = buf.readBlockPos();
            int slotIndex = buf.readInt();
            context.queue(() -> {
                if (!(context.getPlayer() instanceof ServerPlayer sp)) return;
                ShopBlockEntity shop = resolveShop(sp.serverLevel(), pos);
                if (shop == null || !shop.canEdit(sp)) return;
                synchronized (shop) {
                    shop.getOffers().remove(slotIndex);
                    shop.setChanged();
                }
                syncShopState(sp, shop);
            });
        });

        NetworkManager.registerReceiver(NetworkManager.Side.C2S, PURCHASE_OFFER_C2S, (buf, context) -> {
            BlockPos pos = buf.readBlockPos();
            int slotIndex = buf.readInt();
            context.queue(() -> {
                if (!(context.getPlayer() instanceof ServerPlayer sp)) return;
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

        NetworkManager.registerReceiver(NetworkManager.Side.C2S, WITHDRAW_REVENUE_C2S, (buf, context) -> {
            BlockPos pos = buf.readBlockPos();
            context.queue(() -> {
                if (!(context.getPlayer() instanceof ServerPlayer sp)) return;
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

        NetworkManager.registerReceiver(NetworkManager.Side.C2S, SWITCH_SHOP_TAB_C2S, (buf, context) -> {
            BlockPos pos = buf.readBlockPos();
            ShopMenuMode requested = ShopMenuMode.values()[buf.readVarInt()];
            context.queue(() -> {
                if (!(context.getPlayer() instanceof ServerPlayer sp)) return;
                ShopBlockEntity shop = resolveShop(sp.serverLevel(), pos);
                if (shop == null) return;
                ShopMenuMode mode = shop.canEdit(sp) ? requested : ShopMenuMode.CLIENT;
                openShopScreen(sp, shop, mode);
            });
        });
    }

    private static ShopBlockEntity resolveShop(Level level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        return be instanceof ShopBlockEntity s ? s : null;
    }

    /**
     * Sends the player's current balance to their client.
     */
    public static void syncToClient(ServerPlayer player, PlayerCurrencyManager manager) {
        long balance = manager.getBalance(player.getUUID());
        FriendlyByteBuf buf = new FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
        buf.writeLong(balance);
        NetworkManager.sendToPlayer(player, SYNC_CURRENCY_S2C, buf);
    }

    /**
     * Sends a deposit all request to the server.
     */
    public static void sendDepositAll() {
        FriendlyByteBuf buf = new FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
        NetworkManager.sendToServer(DEPOSIT_ALL_C2S, buf);
    }

    /**
     * Sends a withdraw request to the server.
     */
    public static void sendWithdraw(long amount) {
        FriendlyByteBuf buf = new FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
        buf.writeLong(amount);
        NetworkManager.sendToServer(WITHDRAW_C2S, buf);
    }

    // ---------------- Shop S2C / send helpers ----------------

    /**
     * Tells the client to open the shop screen with a fresh state snapshot.
     */
    public static void openShopScreen(ServerPlayer player, ShopBlockEntity shop, ShopMenuMode mode) {
        FriendlyByteBuf buf = new FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
        buf.writeBlockPos(shop.getBlockPos());
        buf.writeVarInt(mode.ordinal());
        buf.writeBoolean(shop.canEdit(player));
        buf.writeBoolean(shop.isAdmin());
        writeShopStatePayload(buf, shop);
        NetworkManager.sendToPlayer(player, OPEN_SHOP_SCREEN_S2C, buf);
    }

    /**
     * Pushes the current shop state to the player (after edit / purchase /
     * withdraw). The client uses this to refresh the open shop screen.
     */
    public static void syncShopState(ServerPlayer player, ShopBlockEntity shop) {
        FriendlyByteBuf buf = new FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
        buf.writeBlockPos(shop.getBlockPos());
        buf.writeBoolean(shop.canEdit(player));
        buf.writeBoolean(shop.isAdmin());
        writeShopStatePayload(buf, shop);
        NetworkManager.sendToPlayer(player, SYNC_SHOP_STATE_S2C, buf);
    }

    private static void writeShopStatePayload(FriendlyByteBuf buf, ShopBlockEntity shop) {
        CompoundTag offersTag = shop.getOffers().toTag();
        buf.writeNbt(offersTag);
        NonNullList<ItemStack> stock = shop.getStock();
        buf.writeVarInt(stock.size());
        for (ItemStack s : stock) buf.writeItem(s);
        buf.writeLong(shop.getAccumulatedRevenue());
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

    public static void sendSwitchShopTab(BlockPos pos, ShopMenuMode mode) {
        FriendlyByteBuf buf = new FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
        buf.writeBlockPos(pos);
        buf.writeVarInt(mode.ordinal());
        NetworkManager.sendToServer(SWITCH_SHOP_TAB_C2S, buf);
    }
}
