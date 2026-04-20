package dev.gaspard4i.numismatic.currency;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PurseAccountTest {

    @Test
    void defaultBalanceIsZero() {
        assertEquals(0L, new PurseAccount().getBalance());
    }

    @Test
    void initialBalanceIsRespected() {
        assertEquals(500L, new PurseAccount(500).getBalance());
    }

    @Test
    void initialBalanceRejectsNegative() {
        assertThrows(IllegalArgumentException.class, () -> new PurseAccount(-1));
    }

    @Test
    void depositIncreasesBalance() {
        PurseAccount account = new PurseAccount(100);
        account.deposit(250);
        assertEquals(350L, account.getBalance());
    }

    @Test
    void depositZeroIsValid() {
        PurseAccount account = new PurseAccount(50);
        account.deposit(0);
        assertEquals(50L, account.getBalance());
    }

    @Test
    void depositRejectsNegative() {
        PurseAccount account = new PurseAccount();
        assertThrows(IllegalArgumentException.class, () -> account.deposit(-1));
    }

    @Test
    void tryWithdrawReducesWhenFundsSufficient() {
        PurseAccount account = new PurseAccount(1000);
        assertTrue(account.tryWithdraw(400));
        assertEquals(600L, account.getBalance());
    }

    @Test
    void tryWithdrawFailsWhenInsufficient() {
        PurseAccount account = new PurseAccount(100);
        assertFalse(account.tryWithdraw(101));
        assertEquals(100L, account.getBalance());
    }

    @Test
    void tryWithdrawZeroIsValid() {
        PurseAccount account = new PurseAccount(100);
        assertTrue(account.tryWithdraw(0));
        assertEquals(100L, account.getBalance());
    }

    @Test
    void tryWithdrawRejectsNegative() {
        PurseAccount account = new PurseAccount();
        assertThrows(IllegalArgumentException.class, () -> account.tryWithdraw(-1));
    }

    @Test
    void setBalanceOverwrites() {
        PurseAccount account = new PurseAccount(100);
        account.setBalance(42);
        assertEquals(42L, account.getBalance());
    }

    @Test
    void setBalanceRejectsNegative() {
        PurseAccount account = new PurseAccount();
        assertThrows(IllegalArgumentException.class, () -> account.setBalance(-1));
    }

    @Test
    void splitMatchesResolver() {
        PurseAccount account = new PurseAccount(1_234_567L);
        assertArrayEquals(CurrencyResolver.splitValues(1_234_567L), account.split());
    }
}
