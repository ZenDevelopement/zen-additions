package net.zen.addon.features.commands.commands.info;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.commands.arguments.PlayerListEntryArgumentType;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.command.CommandSource;
import net.minecraft.util.Formatting;

public class PingCommand extends Command {
    public PingCommand() {
        super("ping", "Shows your ping or someone else's ping.", "lat", "latency");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        // Show your own ping
        builder.executes(context -> {
            info("Your ping is " + Formatting.AQUA + PlayerUtils.getPing() + "ms");
            return SINGLE_SUCCESS;
        });

        // Show another player's ping
        builder.then(argument("player", PlayerListEntryArgumentType.create())
            .executes(context -> {
                PlayerListEntry entry = PlayerListEntryArgumentType.get(context);
                int latency = Math.max(entry.getLatency(), 0);
                String name = entry.getProfile().getName();

                info(name + Formatting.GRAY + "'s ping is " + Formatting.AQUA + latency + "ms");
                return SINGLE_SUCCESS;
            })
        );
    }
}