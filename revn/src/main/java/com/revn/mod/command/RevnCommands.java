package com.revn.mod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.revn.mod.Revn;
import com.revn.mod.herobrine.PlayerMemory;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;

/** Admin/testing commands (permission level 2): /revn herobrine <spawn|memory|forget> [player] */
public final class RevnCommands {
    private RevnCommands() {}

    public static void register(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> d = event.getDispatcher();
        d.register(Commands.literal("revn")
                .requires(s -> s.hasPermission(2))
                .then(Commands.literal("herobrine")
                        .then(Commands.literal("spawn")
                                .executes(c -> spawn(c.getSource(), c.getSource().getPlayerOrException()))
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(c -> spawn(c.getSource(), EntityArgument.getPlayer(c, "player")))))
                        .then(Commands.literal("memory")
                                .executes(c -> memory(c.getSource(), c.getSource().getPlayerOrException()))
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(c -> memory(c.getSource(), EntityArgument.getPlayer(c, "player")))))
                        .then(Commands.literal("forget")
                                .executes(c -> forget(c.getSource(), c.getSource().getPlayerOrException()))
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(c -> forget(c.getSource(), EntityArgument.getPlayer(c, "player")))))));
    }

    private static int spawn(CommandSourceStack src, ServerPlayer target) throws CommandSyntaxException {
        if (Revn.HEROBRINE.manifest(target)) {
            src.sendSuccess(() -> Component.literal("Herobrine manifested for " + target.getName().getString()), true);
            return 1;
        }
        src.sendFailure(Component.literal("No suitable spot found near " + target.getName().getString()));
        return 0;
    }

    private static int memory(CommandSourceStack src, ServerPlayer target) {
        int n = PlayerMemory.encounters(target);
        long last = PlayerMemory.lastEncounter(target);
        src.sendSuccess(() -> Component.literal(target.getName().getString() + ": " + n
                + " encounter(s), last at game time " + last), false);
        return n;
    }

    private static int forget(CommandSourceStack src, ServerPlayer target) {
        PlayerMemory.forget(target);
        src.sendSuccess(() -> Component.literal("Herobrine forgot " + target.getName().getString()), true);
        return 1;
    }
}
