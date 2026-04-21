package dev.gaspard4i.numismatic.command;

import com.mojang.brigadier.arguments.LongArgumentType;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import dev.gaspard4i.numismatic.currency.CurrencyFormatter;
import dev.gaspard4i.numismatic.currency.PlayerCurrencyManager;
import dev.gaspard4i.numismatic.network.NumismaticNetworking;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public final class NumismaticCommands {

    private NumismaticCommands() {}

    public static void register() {
        CommandRegistrationEvent.EVENT.register((dispatcher, registry, selection) -> {
            dispatcher.register(Commands.literal("numismatic")
                    .then(Commands.literal("balance")
                            .executes(ctx -> {
                                ServerPlayer player = ctx.getSource().getPlayerOrException();
                                long balance = PlayerCurrencyManager.get(player.serverLevel())
                                        .getBalance(player.getUUID());
                                ctx.getSource().sendSuccess(
                                        () -> Component.translatable(
                                                "command.numismatic_reimagined.balance.self",
                                                CurrencyFormatter.format(balance)),
                                        false);
                                return 1;
                            })
                    )
                    .then(Commands.literal("give")
                            .requires(s -> s.hasPermission(2))
                            .then(Commands.argument("target", EntityArgument.player())
                                    .then(Commands.argument("amount", LongArgumentType.longArg(1))
                                            .executes(ctx -> {
                                                ServerPlayer target = EntityArgument.getPlayer(ctx, "target");
                                                long amount = LongArgumentType.getLong(ctx, "amount");
                                                ServerLevel level = target.serverLevel();
                                                PlayerCurrencyManager mgr = PlayerCurrencyManager.get(level);
                                                mgr.deposit(target.getUUID(), amount);
                                                NumismaticNetworking.syncBalance(target,
                                                        mgr.getBalance(target.getUUID()));
                                                ctx.getSource().sendSuccess(
                                                        () -> Component.translatable(
                                                                "command.numismatic_reimagined.give",
                                                                CurrencyFormatter.format(amount),
                                                                target.getName().getString()),
                                                        true);
                                                return 1;
                                            })
                                    )
                            )
                    )
                    .then(Commands.literal("set")
                            .requires(s -> s.hasPermission(2))
                            .then(Commands.argument("target", EntityArgument.player())
                                    .then(Commands.argument("amount", LongArgumentType.longArg(0))
                                            .executes(ctx -> {
                                                ServerPlayer target = EntityArgument.getPlayer(ctx, "target");
                                                long amount = LongArgumentType.getLong(ctx, "amount");
                                                ServerLevel level = target.serverLevel();
                                                PlayerCurrencyManager mgr = PlayerCurrencyManager.get(level);
                                                mgr.setBalance(target.getUUID(), amount);
                                                NumismaticNetworking.syncBalance(target,
                                                        mgr.getBalance(target.getUUID()));
                                                ctx.getSource().sendSuccess(
                                                        () -> Component.translatable(
                                                                "command.numismatic_reimagined.set",
                                                                target.getName().getString(),
                                                                CurrencyFormatter.format(amount)),
                                                        true);
                                                return 1;
                                            })
                                    )
                            )
                    )
            );
        });
    }
}
