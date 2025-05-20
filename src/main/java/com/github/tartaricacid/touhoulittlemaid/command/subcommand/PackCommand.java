package com.github.tartaricacid.touhoulittlemaid.command.subcommand;

import com.github.tartaricacid.touhoulittlemaid.client.event.ReloadResourceEvent;
import com.github.tartaricacid.touhoulittlemaid.entity.info.ServerCustomPackLoader;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

public final class PackCommand {
    private static final String PACK_NAME = "pack";
    private static final String RELOAD_NAME = "reload";

    public static LiteralArgumentBuilder<CommandSourceStack> get() {
        LiteralArgumentBuilder<CommandSourceStack> pack = Commands.literal(PACK_NAME);
        LiteralArgumentBuilder<CommandSourceStack> reload = Commands.literal(RELOAD_NAME);
        pack.then(reload.executes(PackCommand::reloadAllPack));
        return pack;
    }

    private static int reloadAllPack(CommandContext<CommandSourceStack> context) {
        context.getSource().sendSuccess(() -> Component.translatable("commands.touhou_little_maid.pack.reload.start"), true);
        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> ReloadResourceEvent::asyncReloadAllPack);
        ServerCustomPackLoader.reloadPacks();
        return Command.SINGLE_SUCCESS;
    }
}
