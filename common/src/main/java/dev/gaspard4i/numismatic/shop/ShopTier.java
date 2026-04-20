package dev.gaspard4i.numismatic.shop;

import dev.gaspard4i.numismatic.currency.Currency;

/**
 * Shop tier: controls the stock inventory size, the offer cap, the display
 * colour tint, and the recipe coin. The admin tier is inexhaustible (infinite
 * stock) and has the loosest offer cap; player tiers scale from bronze (low
 * capacity) to netherite (high capacity).
 */
public enum ShopTier {
    BRONZE("bronze", 9, 3, 0xCD7F32, Currency.BRONZE, false),
    SILVER("silver", 18, 6, 0xC0C0C0, Currency.SILVER, false),
    GOLD("gold", 27, 12, 0xFFD700, Currency.GOLD, false),
    NETHERITE("netherite", 36, 24, 0x4A4A4A, Currency.NETHERITE, false),
    ADMIN("admin", 27, 56, 0x9B30FF, null, true);

    private final String id;
    private final int stockSize;
    private final int maxOffers;
    private final int colorRgb;
    private final Currency coin;
    private final boolean admin;

    ShopTier(String id, int stockSize, int maxOffers, int colorRgb,
             Currency coin, boolean admin) {
        this.id = id;
        this.stockSize = stockSize;
        this.maxOffers = maxOffers;
        this.colorRgb = colorRgb;
        this.coin = coin;
        this.admin = admin;
    }

    public String id() { return id; }
    public int stockSize() { return stockSize; }
    public int maxOffers() { return maxOffers; }
    public int colorRgb() { return colorRgb; }
    public Currency coin() { return coin; }
    public boolean isAdmin() { return admin; }

    /**
     * Registry id for the block + block item. GOLD / ADMIN preserve legacy
     * names ("shop_block" / "admin_shop_block") so existing world saves and
     * json assets keep working; the other tiers get fresh ids.
     */
    public String blockId() {
        return switch (this) {
            case GOLD -> "shop_block";
            case ADMIN -> "admin_shop_block";
            case BRONZE -> "bronze_shop";
            case SILVER -> "silver_shop";
            case NETHERITE -> "netherite_shop";
        };
    }
}
