package dev.gaspard4i.numismatic.block;

import dev.gaspard4i.numismatic.currency.Currency;
import dev.gaspard4i.numismatic.item.CoinItem;
import dev.gaspard4i.numismatic.item.NumismaticItems;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Menu for the Piggy Bank, ported 1:1 from wisp-forest/numismatic-overhaul upstream :
 * three validating slots (bronze, silver, gold) at x=62/80/98, y=26, plus the player's
 * main inventory and hotbar at (8, 63).
 *
 * <p>Reimagined addition : we only expose the three upstream rows ; netherite coins must
 * be merged/split via the money bag interactions (they still work in the inventory).
 */
public class PiggyBankMenu extends AbstractContainerMenu {

    private final Container piggyBank;
    private final ContainerLevelAccess context;

    /** Client-side constructor (used by Architectury MenuType.create). */
    public PiggyBankMenu(int syncId, Inventory playerInventory) {
        this(syncId, playerInventory, new SimpleContainer(3), ContainerLevelAccess.NULL);
    }

    /** Server-side constructor, bound to the block entity's container. */
    public PiggyBankMenu(int syncId, Inventory playerInventory,
                         Container piggyBank, ContainerLevelAccess context) {
        super(PiggyBankMenus.PIGGY_BANK_MENU.get(), syncId);
        checkContainerSize(piggyBank, 3);
        this.piggyBank = piggyBank;
        this.context = context;

        addSlot(new CoinOnlySlot(piggyBank, 0, 62, 26, Currency.BRONZE));
        addSlot(new CoinOnlySlot(piggyBank, 1, 80, 26, Currency.SILVER));
        addSlot(new CoinOnlySlot(piggyBank, 2, 98, 26, Currency.GOLD));

        // Player main inventory (3x9) starting at y=63.
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 63 + row * 18));
            }
        }
        // Hotbar at y=121.
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, 8 + col * 18, 121));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return piggyBank.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;

        ItemStack source = slot.getItem();
        ItemStack copy = source.copy();
        // Piggy slots are 0..2, player inv slots are 3..38.
        if (index < 3) {
            if (!moveItemStackTo(source, 3, 39, true)) return ItemStack.EMPTY;
        } else {
            if (!moveItemStackTo(source, 0, 3, false)) return ItemStack.EMPTY;
        }
        if (source.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return copy;
    }

    private static final class CoinOnlySlot extends Slot {
        private final Currency currency;

        CoinOnlySlot(Container container, int index, int x, int y, Currency currency) {
            super(container, index, x, y);
            this.currency = currency;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return stack.getItem() instanceof CoinItem coin && coin.getCurrency() == currency;
        }
    }

    // Unused for now but kept for API parity with upstream.
    public ContainerLevelAccess context() { return context; }

    @SuppressWarnings("unused")
    private static final MenuType<?> UNUSED = PiggyBankMenus.PIGGY_BANK_MENU.get();

    @SuppressWarnings("unused")
    private static final NumismaticItems UNUSED_ITEMS = null;
}
