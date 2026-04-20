package dev.gaspard4i.numismatic.command;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.context.CommandContext;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import dev.gaspard4i.numismatic.currency.CurrencyHelper;
import dev.gaspard4i.numismatic.currency.CurrencyResolver;
import dev.gaspard4i.numismatic.currency.PlayerCurrencyManager;
import dev.gaspard4i.numismatic.item.MoneyBagItem;
import dev.gaspard4i.numismatic.network.NumismaticNetworking;
import dev.gaspard4i.numismatic.shop.RequestBoardBlockEntity;
import dev.gaspard4i.numismatic.shop.RequestFulfillLogic;
import dev.gaspard4i.numismatic.shop.RequestOffer;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

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
                                .then(Commands.literal("request")
                                        .then(Commands.literal("fund")
                                                .then(Commands.argument("amount", LongArgumentType.longArg(1))
                                                        .executes(NumismaticCommands::requestFund)))
                                        .then(Commands.literal("add")
                                                .then(Commands.argument("qty", IntegerArgumentType.integer(1))
                                                        .then(Commands.argument("price", LongArgumentType.longArg(1))
                                                                .executes(NumismaticCommands::requestAddLoose)
                                                                .then(Commands.argument("strictNbt", BoolArgumentType.bool())
                                                                        .executes(NumismaticCommands::requestAdd)))))
                                        .then(Commands.literal("remove")
                                                .then(Commands.argument("index", IntegerArgumentType.integer(0))
                                                        .executes(NumismaticCommands::requestRemove)))
                                        .then(Commands.literal("deliver")
                                                .executes(NumismaticCommands::requestDeliver))
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

    // ---------------- Request Board (reverse shop) ----------------

    /** Ray-cast from the player's eyes for the request board they are looking at. */
    private static RequestBoardBlockEntity lookedAtBoard(ServerPlayer player) {
        Vec3 eye = player.getEyePosition(1.0f);
        Vec3 end = eye.add(player.getLookAngle().scale(5.0));
        BlockHitResult hit = player.serverLevel().clip(new ClipContext(eye, end,
                ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
        if (hit == null || hit.getType() == HitResult.Type.MISS) return null;
        BlockPos pos = hit.getBlockPos();
        BlockEntity be = player.serverLevel().getBlockEntity(pos);
        return be instanceof RequestBoardBlockEntity board ? board : null;
    }

    private static int requestFund(CommandContext<CommandSourceStack> context) {
        ServerPlayer sp = context.getSource().getPlayer();
        if (sp == null) return 0;
        long amount = LongArgumentType.getLong(context, "amount");
        RequestBoardBlockEntity board = lookedAtBoard(sp);
        if (board == null) {
            context.getSource().sendFailure(
                    Component.translatable("command.numismatic_reimagined.request.no_board"));
            return 0;
        }
        if (!board.isOwner(sp)) {
            context.getSource().sendFailure(
                    Component.translatable("command.numismatic_reimagined.request.not_owner"));
            return 0;
        }
        PlayerCurrencyManager mgr = PlayerCurrencyManager.get(sp.server.overworld());
        if (!mgr.subtractBalance(sp.getUUID(), amount)) {
            context.getSource().sendFailure(Component.literal("Insufficient purse balance"));
            return 0;
        }
        NumismaticNetworking.syncToClient(sp, mgr);
        board.addFunds(amount);
        context.getSource().sendSuccess(() ->
                Component.translatable("command.numismatic_reimagined.request.fund",
                        String.format("%,d", amount)).withStyle(ChatFormatting.GREEN), false);
        return 1;
    }

    private static int requestAddLoose(CommandContext<CommandSourceStack> context) {
        return requestAddImpl(context, false);
    }

    private static int requestAdd(CommandContext<CommandSourceStack> context) {
        boolean strict = BoolArgumentType.getBool(context, "strictNbt");
        return requestAddImpl(context, strict);
    }

    private static int requestAddImpl(CommandContext<CommandSourceStack> context, boolean strictNbt) {
        ServerPlayer sp = context.getSource().getPlayer();
        if (sp == null) return 0;
        ItemStack held = sp.getMainHandItem();
        if (held.isEmpty()) {
            context.getSource().sendFailure(Component.literal("Hold the template item in your main hand"));
            return 0;
        }
        int qty = IntegerArgumentType.getInteger(context, "qty");
        long price = LongArgumentType.getLong(context, "price");
        RequestBoardBlockEntity board = lookedAtBoard(sp);
        if (board == null) {
            context.getSource().sendFailure(
                    Component.translatable("command.numismatic_reimagined.request.no_board"));
            return 0;
        }
        if (!board.isOwner(sp)) {
            context.getSource().sendFailure(
                    Component.translatable("command.numismatic_reimagined.request.not_owner"));
            return 0;
        }
        ItemStack template = held.copy();
        template.setCount(1);
        RequestOffer offer = new RequestOffer(template, qty, 0, price, strictNbt);
        if (board.getOffers().add(offer) < 0) {
            context.getSource().sendFailure(Component.literal("Board is full"));
            return 0;
        }
        board.setChanged();
        context.getSource().sendSuccess(() ->
                Component.translatable("command.numismatic_reimagined.request.add",
                        template.getHoverName().getString(), qty, price)
                        .withStyle(ChatFormatting.GREEN), false);
        return 1;
    }

    private static int requestRemove(CommandContext<CommandSourceStack> context) {
        ServerPlayer sp = context.getSource().getPlayer();
        if (sp == null) return 0;
        int index = IntegerArgumentType.getInteger(context, "index");
        RequestBoardBlockEntity board = lookedAtBoard(sp);
        if (board == null) {
            context.getSource().sendFailure(
                    Component.translatable("command.numismatic_reimagined.request.no_board"));
            return 0;
        }
        if (!board.isOwner(sp)) {
            context.getSource().sendFailure(
                    Component.translatable("command.numismatic_reimagined.request.not_owner"));
            return 0;
        }
        if (!board.getOffers().remove(index)) {
            context.getSource().sendFailure(Component.literal("Invalid index"));
            return 0;
        }
        board.setChanged();
        context.getSource().sendSuccess(() ->
                Component.translatable("command.numismatic_reimagined.request.remove", index)
                        .withStyle(ChatFormatting.GREEN), false);
        return 1;
    }

    /** Looks for the first offer the held stack can fulfill and applies it. */
    private static int requestDeliver(CommandContext<CommandSourceStack> context) {
        ServerPlayer sp = context.getSource().getPlayer();
        if (sp == null) return 0;
        ItemStack held = sp.getMainHandItem();
        if (held.isEmpty()) {
            context.getSource().sendFailure(Component.literal("Hold the item to deliver"));
            return 0;
        }
        RequestBoardBlockEntity board = lookedAtBoard(sp);
        if (board == null) {
            context.getSource().sendFailure(
                    Component.translatable("command.numismatic_reimagined.request.no_board"));
            return 0;
        }
        int matchIndex = -1;
        RequestFulfillLogic.FulfillResult result = null;
        for (int i = 0; i < board.getOffers().asList().size(); i++) {
            RequestOffer offer = board.getOffers().asList().get(i);
            RequestFulfillLogic.FulfillResult r =
                    RequestFulfillLogic.fulfill(held, offer, board.getFunds());
            if (!r.isNone()) { matchIndex = i; result = r; break; }
        }
        if (result == null) {
            context.getSource().sendFailure(
                    Component.translatable("command.numismatic_reimagined.request.no_match"));
            return 0;
        }
        // Deduct fund + store items.
        board.tryWithdrawFunds(result.payout());
        ItemStack stored = held.copy();
        stored.setCount(result.consumed());
        int slot = board.firstEmptySlot();
        if (slot >= 0) board.setItem(slot, stored);
        else sp.drop(stored, false); // board full → drop back
        held.shrink(result.consumed());
        board.getOffers().replace(matchIndex, result.updatedOffer());
        board.setChanged();
        PlayerCurrencyManager mgr = PlayerCurrencyManager.get(sp.server.overworld());
        mgr.addBalance(sp.getUUID(), result.payout());
        NumismaticNetworking.syncToClient(sp, mgr);
        final int consumed = result.consumed();
        final long payout = result.payout();
        context.getSource().sendSuccess(() ->
                Component.translatable("command.numismatic_reimagined.request.fulfilled",
                        consumed, String.format("%,d", payout))
                        .withStyle(ChatFormatting.GREEN), false);
        return 1;
    }
}
