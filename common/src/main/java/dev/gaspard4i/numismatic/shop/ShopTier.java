package dev.gaspard4i.numismatic.shop;

public enum ShopTier {

    BRONZE("bronze_shop", 9, 3),
    SILVER("silver_shop", 18, 6),
    GOLD("gold_shop", 27, 12),
    NETHERITE("netherite_shop", 36, 24),
    ADMIN("admin_shop", 27, 56);

    private final String id;
    private final int stockSize;
    private final int offersSize;

    ShopTier(String id, int stockSize, int offersSize) {
        this.id = id;
        this.stockSize = stockSize;
        this.offersSize = offersSize;
    }

    public String id() { return id; }
    public int stockSize() { return stockSize; }
    public int offersSize() { return offersSize; }
}
