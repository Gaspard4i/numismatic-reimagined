package dev.gaspard4i.numismatic.currency;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Displays actionbar / chat messages on currency changes, matching
 * upstream {@code CurrencyComponent.modify} output :
 * {@code §a+ §7[§b12 §6Gold§7, §b4 §cSilver§7]}
 */
public final class CurrencyNotifications {

    private CurrencyNotifications() {}

    public static void sendDeposit(ServerPlayer player, long amount) {
        if (amount == 0) return;
        List<ItemStack> stacks = CurrencyConverter.getAsItemStackList(Math.abs(amount));
        if (stacks.isEmpty()) return;

        MutableComponent message = Component.empty();
        message.append(Component.literal(amount < 0 ? "- " : "+ ")
                .withStyle(amount < 0 ? ChatFormatting.RED : ChatFormatting.GREEN));
        message.append(Component.literal("[").withStyle(ChatFormatting.GRAY));

        for (int i = 0; i < stacks.size(); i++) {
            ItemStack stack = stacks.get(i);
            Currency currency = currencyOf(stack);
            Style coinStyle = Style.EMPTY.withColor(TextColor.fromRgb(currency.getNameColor()));
            message.append(Component.literal(stack.getCount() + " ")
                    .withStyle(Style.EMPTY.withColor(ChatFormatting.AQUA)));
            message.append(stack.getHoverName().copy().setStyle(coinStyle));
            if (i < stacks.size() - 1) {
                message.append(Component.literal(", ").withStyle(ChatFormatting.GRAY));
            }
        }
        message.append(Component.literal("]").withStyle(ChatFormatting.GRAY));

        // true = actionbar (upstream default location).
        player.displayClientMessage(message, true);
    }

    private static Currency currencyOf(ItemStack stack) {
        if (stack.getItem() instanceof dev.gaspard4i.numismatic.item.CoinItem coin) {
            return coin.getCurrency();
        }
        return Currency.BRONZE;
    }
}
