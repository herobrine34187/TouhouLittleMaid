package com.github.tartaricacid.touhoulittlemaid.command.subcommand;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;

public class MaidCommand {
    private static final String MAID_NAME = "maid";
    private static final String TARGETS_NAME = "targets";
    private static final String NO_AI_NAME = "no_ai";
    private static final String RESULT_NAME = "result";

    public static LiteralArgumentBuilder<CommandSourceStack> get() {
        LiteralArgumentBuilder<CommandSourceStack> pack = Commands.literal(MAID_NAME);
        RequiredArgumentBuilder<CommandSourceStack, EntitySelector> targets = Commands.argument(TARGETS_NAME, EntityArgument.entity());
        LiteralArgumentBuilder<CommandSourceStack> noAi = Commands.literal(NO_AI_NAME);
        RequiredArgumentBuilder<CommandSourceStack, Boolean> result = Commands.argument(RESULT_NAME, BoolArgumentType.bool());
        pack.then(targets.then(noAi.then(result.executes(MaidCommand::handleMaidNoAi))));
        return pack;
    }

    private static int handleMaidNoAi(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Entity entity = EntityArgument.getEntity(context, TARGETS_NAME);
        boolean noAi = BoolArgumentType.getBool(context, RESULT_NAME);
        if (entity instanceof EntityMaid maid) {
            maid.setNoAi(noAi);
            context.getSource().sendSuccess(() -> Component.translatable("commands.touhou_little_maid.maid.no_ai.success", String.valueOf(noAi)), true);
        } else {
            context.getSource().sendFailure(Component.translatable("commands.touhou_little_maid.maid.no_ai.fail"));
        }
        return Command.SINGLE_SUCCESS;
    }
}
