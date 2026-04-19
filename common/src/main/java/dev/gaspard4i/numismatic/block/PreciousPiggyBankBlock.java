package dev.gaspard4i.numismatic.block;

/**
 * A precious piggy bank (golden/netherite) that requires a pickaxe to mine.
 * Silk touch behavior is inherited from PiggyBankBlock.
 */
public abstract class PreciousPiggyBankBlock extends PiggyBankBlock {

    public PreciousPiggyBankBlock(Properties properties) {
        super(properties.requiresCorrectToolForDrops());
    }
}
