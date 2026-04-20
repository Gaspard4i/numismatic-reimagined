package dev.gaspard4i.numismatic.currency;

import dev.gaspard4i.numismatic.testutil.McBootstrap;
import net.minecraft.network.chat.Component;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CurrencyNotificationsTest {

    @BeforeAll
    static void setup() { McBootstrap.ensure(); }

    @Test
    void zeroReturnsEmpty() {
        assertEquals("", CurrencyNotifications.buildActionbar(0).getString());
    }

    @Test
    void pureBronzeGain() {
        Component c = CurrencyNotifications.buildActionbar(42);
        String s = c.getString();
        assertTrue(s.startsWith("+ "));
        assertTrue(s.contains("42B"));
    }

    @Test
    void pureSilverLoss() {
        String s = CurrencyNotifications.buildActionbar(-500).getString();
        assertTrue(s.startsWith("- "));
        assertTrue(s.contains("5S"));
    }

    @Test
    void mixedFourDenoms() {
        // 1_234_567 bronze = 1N + 23G + 45S + 67B
        String s = CurrencyNotifications.buildActionbar(1_234_567L).getString();
        assertTrue(s.contains("1N"));
        assertTrue(s.contains("23G"));
        assertTrue(s.contains("45S"));
        assertTrue(s.contains("67B"));
    }

    @Test
    void singleNetherite() {
        String s = CurrencyNotifications.buildActionbar(1_000_000L).getString();
        assertTrue(s.contains("1N"));
        assertFalse(s.contains("B"));
        assertFalse(s.contains("G"));
        assertFalse(s.contains("S"));
    }

    @Test
    void commaSeparator() {
        String s = CurrencyNotifications.buildActionbar(150).getString();
        assertTrue(s.contains(","));
        assertTrue(s.contains("1S"));
        assertTrue(s.contains("50B"));
    }
}
