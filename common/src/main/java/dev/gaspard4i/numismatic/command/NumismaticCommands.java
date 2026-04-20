package dev.gaspard4i.numismatic.command;

import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.context.CommandContext;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import dev.gaspard4i.numismatic.currency.CurrencyHelper;
import dev.gaspard4i.numismatic.currency.CurrencyResolver;
import dev.gaspard4i.numismatic.currency.PlayerCurrencyManager;
import dev.gaspard4i.numismatic.item.MoneyBagItem;
import dev.gaspard4i.numismatic.network.NumismaticNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Server-side commands for the Numismatic currency system.
 */
public final class NumismaticCommands {

    private NumismaticCommands() {}

    /**
     * Formats a balance for display.
     * Default: total bronze value + "coins" (e.g., "1,234,567 coins")
     */
    private static String formatBalance(long balance) {
        return String.format("%,d coins", balance);
    }

    /**
     * Formats a balance with detailed denomination breakdown.
     * (e.g., "1N 2G 3S 4B")
     */
    private static String formatBalanceDetailed(long balance) {
        return CurrencyResolver.formatValue(balance);
    }

    public static void register() {
        CommandRegistrationEvent.EVENT.register((dispatcher, registry, selection) ->
                dispatcher.register(
                        Commands.literal("numismatic")
                                .then(Commands.literal("balance")
                                        .executes(NumismaticCommands::balanceSelf)
                                        .then(Commands.literal("detailed")
                                                .executes(NumismaticCommands::balanceSelfDetailed)
                                        )
                                        .then(Commands.argument("player", EntityArgument.player())
                                                .requires(source -> source.hasPermission(2))
                                                .executes(NumismaticCommands::balanceOther)
                                                .then(Commands.literal("detailed")
                                                        .executes(NumismaticCommands::balanceOtherDetailed)
                                                )
                                        )
                                )
                                .then(Commands.literal("deposit")
                                        .executes(NumismaticCommands::depositAll)
                                )
                                .then(Commands.literal("withdraw")
                                        .then(Commands.literal("all")
                                                .executes(NumismaticCommands::withdrawAll)
                                        )
                                        .then(Commands.argument("amount", LongArgumentType.longArg(1))
                                                .executes(NumismaticCommands::withdraw)
                                        )
                                )
                                .then(Commands.literal("set")
                                        .requires(source -> source.hasPermission(2))
                                        .then(Commands.argument("player", EntityArgument.player())
                                                .then(Commands.argument("amount", LongArgumentType.longArg(0))
                                                        .executes(NumismaticCommands::setBalance)
                                                )
                                        )
                                )
                                .then(Commands.literal("give")
                                        .requires(source -> source.hasPermission(2))
                                        .then(Commands.argument("player", EntityArgument.player())
                                                .then(Commands.argument("amount", LongArgumentType.longArg(1))
                                                        .executes(NumismaticCommands::giveBalance)
                                                )
                                        )
                                )
                                .then(Commands.literal("give_bag")
                                        .requires(source -> source.hasPermission(2))
                                        .executes(NumismaticCommands::giveBagRandom)
                                        .then(Commands.argument("amount", LongArgumentType.longArg(1))
                                                .executes(NumismaticCommands::giveBagAmount)
                                        )
                                )
                )
        );
    }

    private static int balanceSelf(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) {
            context.getSource().sendFailure(Component.literal("This command can only be run by a player."));
            return 0;
        }
        ServerLevel overworld = player.server.overworld();
        PlayerCurrencyManager manager = PlayerCurrencyManager.get(overworld);
        long balance = manager.getBalance(player.getUUID());
        String formatted = formatBalance(balance);

        context.getSource().sendSuccess(() ->
                Component.translatable("command.numismatic_reimagined.balance.self", formatted)
                        .withStyle(ChatFormatting.GOLD), false);
        return 1;
    }

    private static int balanceSelfDetailed(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) {
            context.getSource().sendFailure(Component.literal("This command can only be run by a player."));
            return 0;
        }
        ServerLevel overworld = player.server.overworld();
        PlayerCurrencyManager manager = PlayerCurrencyManager.get(overworld);
        long balance = manager.getBalance(player.getUUID());
        String formatted = formatBalanceDetailed(balance);

        context.getSource().sendSuccess(() ->
                Component.translatable("command.numismatic_reimagined.balance.self", formatted)
                        .withStyle(ChatFormatting.GOLD), false);
        return 1;
    }

    private static int balanceOther(CommandContext<CommandSourceStack> context) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(context, "player");
        ServerLevel overworld = target.server.overworld();
        PlayerCurrencyManager manager = PlayerCurrencyManager.get(overworld);
        long balance = manager.getBalance(target.getUUID());
        String formatted = formatBalance(balance);

        context.getSource().sendSuccess(() ->
                Component.translatable("command.numismatic_reimagined.balance.other",
                        target.getName().getString(), formatted)
                        .withStyle(ChatFormatting.GOLD), false);
        return 1;
    }

    private static int balanceOtherDetailed(CommandContext<CommandSourceStack> context) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(context, "player");
        ServerLevel overworld = target.server.overworld();
        PlayerCurrencyManager manager = PlayerCurrencyManager.get(overworld);
        long balance = manager.getBalance(target.getUUID());
        String formatted = formatBalanceDetailed(balance);

        context.getSource().sendSuccess(() ->
                Component.translatable("command.numismatic_reimagined.balance.other",
                        target.getName().getString(), formatted)
                        .withStyle(ChatFormatting.GOLD), false);
        return 1;
    }

    private static int depositAll(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) {
            context.getSource().sendFailure(Component.literal("This command can only be run by a player."));
            return 0;
        }
        ServerLevel overworld = player.server.overworld();
        PlayerCurrencyManager manager = PlayerCurrencyManager.get(overworld);
        long deposited = CurrencyHelper.depositAllCoins(player, manager);

        if (deposited > 0) {
            String formatted = formatBalance(deposited);
            NumismaticNetworking.syncToClient(player, manager);
            context.getSource().sendSuccess(() ->
                    Component.translatable("command.numismatic_reimagined.deposit", formatted)
                            .withStyle(ChatFormatting.GREEN), false);
        } else {
            context.getSource().sendFailure(
                    Component.translatable("command.numismatic_reimagined.deposit.none"));
        }
        return deposited > 0 ? 1 : 0;
    }

    private static int withdraw(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) {
            context.getSource().sendFailure(Component.literal("This command can only be run by a player."));
            return 0;
        }
        long amount = LongArgumentType.getLong(context, "amount");
        ServerLevel overworld = player.server.overworld();
        PlayerCurrencyManager manager = PlayerCurrencyManager.get(overworld);

        if (manager.subtractBalance(player.getUUID(), amount)) {
            CurrencyHelper.offerAsCoins(player, amount);
            String formatted = formatBalance(amount);
            NumismaticNetworking.syncToClient(player, manager);
            context.getSource().sendSuccess(() ->
                    Component.translatable("command.numismatic_reimagined.withdraw", formatted)
                            .withStyle(ChatFormatting.GREEN), false);
            return 1;
        } else {
            context.getSource().sendFailure(
                    Component.translatable("command.numismatic_reimagined.withdraw.insufficient"));
            return 0;
        }
    }

    private static int withdrawAll(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) {
            context.getSource().sendFailure(Component.literal("This command can only be run by a player."));
            return 0;
        }
        ServerLevel overworld = player.server.overworld();
        PlayerCurrencyManager manager = PlayerCurrencyManager.get(overworld);
        long balance = manager.getBalance(player.getUUID());

        if (balance <= 0) {
            context.getSource().sendFailure(
                    Component.translatable("command.numismatic_reimagined.withdraw.insufficient"));
            return 0;
        }

        manager.setBalance(player.getUUID(), 0);
        CurrencyHelper.offerAsCoins(player, balance);
        String formatted = formatBalance(balance);
        NumismaticNetworking.syncToClient(player, manager);
        context.getSource().sendSuccess(() ->
                Component.translatable("command.numismatic_reimagined.withdraw", formatted)
                        .withStyle(ChatFormatting.GREEN), false);
        return 1;
    }

    private static int setBalance(CommandContext<CommandSourceStack> context) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(context, "player");
        long amount = LongArgumentType.getLong(context, "amount");
        ServerLevel overworld = target.server.overworld();
        PlayerCurrencyManager manager = PlayerCurrencyManager.get(overworld);
        manager.setBalance(target.getUUID(), amount);

        String formatted = formatBalance(amount);
        NumismaticNetworking.syncToClient(target, manager);
        context.getSource().sendSuccess(() ->
                Component.translatable("command.numismatic_reimagined.set",
                        target.getName().getString(), formatted)
                        .withStyle(ChatFormatting.GREEN), false);
        return 1;
    }

    private static int giveBalance(CommandContext<CommandSourceStack> context) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(context, "player");
        long amount = LongArgumentType.getLong(context, "amount");
        ServerLevel overworld = target.server.overworld();
        PlayerCurrencyManager manager = PlayerCurrencyManager.get(overworld);
        manager.addBalanceAndTrack(overworld, target.getUUID(), amount);

        String formatted = formatBalance(amount);
        NumismaticNetworking.syncToClient(target, manager);
        context.getSource().sendSuccess(() ->
                Component.translatable("command.numismatic_reimagined.give",
                        formatted, target.getName().getString())
                        .withStyle(ChatFormatting.GREEN), false);
        return 1;
    }

    private static int giveBagRandom(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) {
            context.getSource().sendFailure(Component.literal("This command can only be run by a player."));
            return 0;
        }
        // Random value between 1 and 10,000,000 (up to 10N)
        long randomValue = ThreadLocalRandom.current().nextLong(1, 10_000_001);
        ItemStack bag = MoneyBagItem.createWithValue(randomValue);
        if (!player.getInventory().add(bag)) {
            player.drop(bag, false);
        }
        String formatted = formatBalance(randomValue);
        context.getSource().sendSuccess(() ->
                Component.translatable("command.numismatic_reimagined.give_bag", formatted)
                        .withStyle(ChatFormatting.GREEN), false);
        return 1;
    }

    private static int giveBagAmount(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) {
            context.getSource().sendFailure(Component.literal("This command can only be run by a player."));
            return 0;
        }
        long amount = LongArgumentType.getLong(context, "amount");
        ItemStack bag = MoneyBagItem.createWithValue(amount);
        if (!player.getInventory().add(bag)) {
            player.drop(bag, false);
        }
        String formatted = formatBalance(amount);
        context.getSource().sendSuccess(() ->
                Component.translatable("command.numismatic_reimagined.give_bag", formatted)
                        .withStyle(ChatFormatting.GREEN), false);
        return 1;
    }
}
