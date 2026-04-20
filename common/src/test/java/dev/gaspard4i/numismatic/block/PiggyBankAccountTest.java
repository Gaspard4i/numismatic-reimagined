package dev.gaspard4i.numismatic.block;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PiggyBankAccountTest {

    @Test
    void defaultConstructorStartsEmpty() {
        PiggyBankAccount a = new PiggyBankAccount(PiggyBankTier.BASE);
        assertEquals(0L, a.stored());
        assertTrue(a.isEmpty());
        assertFalse(a.isFull());
    }

    @Test
    void initialClampsToCap() {
        PiggyBankAccount a = new PiggyBankAccount(PiggyBankTier.BASE, 99_999L);
        assertEquals(PiggyBankTier.BASE.cap(), a.stored());
        assertTrue(a.isFull());
    }

    @Test
    void initialRejectsNegative() {
        assertThrows(IllegalArgumentException.class,
                () -> new PiggyBankAccount(PiggyBankTier.BASE, -1));
    }

    @Test
    void nullTierRejected() {
        assertThrows(IllegalArgumentException.class, () -> new PiggyBankAccount(null, 0));
    }

    @Test
    void tryDepositPartialWhenCapped() {
        PiggyBankAccount a = new PiggyBankAccount(PiggyBankTier.BASE, 9_990L);
        long accepted = a.tryDeposit(100);
        assertEquals(9L, accepted);
        assertEquals(PiggyBankTier.BASE.cap(), a.stored());
    }

    @Test
    void tryDepositFullyWhenRoom() {
        PiggyBankAccount a = new PiggyBankAccount(PiggyBankTier.NETHERITE);
        long accepted = a.tryDeposit(500_000);
        assertEquals(500_000L, accepted);
        assertEquals(500_000L, a.stored());
    }

    @Test
    void tryDepositOnFullReturnsZero() {
        PiggyBankAccount a = new PiggyBankAccount(PiggyBankTier.BASE, PiggyBankTier.BASE.cap());
        assertEquals(0L, a.tryDeposit(100));
    }

    @Test
    void tryDepositZeroIsNoOp() {
        PiggyBankAccount a = new PiggyBankAccount(PiggyBankTier.GOLDEN, 500);
        assertEquals(0L, a.tryDeposit(0));
        assertEquals(500L, a.stored());
    }

    @Test
    void tryDepositRejectsNegative() {
        PiggyBankAccount a = new PiggyBankAccount(PiggyBankTier.BASE);
        assertThrows(IllegalArgumentException.class, () -> a.tryDeposit(-1));
    }

    @Test
    void crushReturnsAndEmptiesContents() {
        PiggyBankAccount a = new PiggyBankAccount(PiggyBankTier.GOLDEN, 500);
        assertEquals(500L, a.crush());
        assertEquals(0L, a.stored());
        assertTrue(a.isEmpty());
    }

    @Test
    void remainingDecreasesAfterDeposit() {
        PiggyBankAccount a = new PiggyBankAccount(PiggyBankTier.GOLDEN, 10);
        assertEquals(PiggyBankTier.GOLDEN.cap() - 10, a.remaining());
        a.tryDeposit(90);
        assertEquals(PiggyBankTier.GOLDEN.cap() - 100, a.remaining());
    }

    @Test
    void tierAccessorMatchesConstructor() {
        for (PiggyBankTier t : PiggyBankTier.values()) {
            assertEquals(t, new PiggyBankAccount(t).tier());
        }
    }

    @Test
    void capMatchesTier() {
        assertEquals(PiggyBankTier.BASE.cap(), new PiggyBankAccount(PiggyBankTier.BASE).cap());
    }
}
