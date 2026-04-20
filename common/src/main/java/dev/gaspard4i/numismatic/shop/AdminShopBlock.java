package dev.gaspard4i.numismatic.shop;

/**
 * Backwards-compat shim: pre-tier code referred to AdminShopBlock. Now
 * it is just a ShopBlock pre-configured with {@link ShopTier#ADMIN}.
 */
public class AdminShopBlock extends ShopBlock {
    public AdminShopBlock(Properties properties) {
        super(properties, ShopTier.ADMIN);
    }
}
