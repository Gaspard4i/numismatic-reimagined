package dev.gaspard4i.numismatic.shop;

/**
 * Pure logic describing the shop's hopper-transfer state transitions :
 * whether an external hopper insertion is accepted as a whitelisted offer
 * template, and whether the shop wants to emit revenue as coin items.
 */
public final class ShopTransferLogic {

    private ShopTransferLogic() {}

    public static boolean canAcceptTemplate(boolean transferEnabled, String templateOfferId, String candidateId) {
        if (!transferEnabled) return false;
        if (templateOfferId == null || candidateId == null) return false;
        return templateOfferId.equals(candidateId);
    }

    public static long trySellWithStockAndPrice(long price, int inStock, int qtyPerPurchase) {
        if (price <= 0 || qtyPerPurchase <= 0 || inStock < qtyPerPurchase) return 0L;
        return price;
    }

    public static int remainingStockAfterSale(int inStock, int qtyPerPurchase) {
        if (qtyPerPurchase <= 0 || inStock < qtyPerPurchase) return inStock;
        return inStock - qtyPerPurchase;
    }
}
