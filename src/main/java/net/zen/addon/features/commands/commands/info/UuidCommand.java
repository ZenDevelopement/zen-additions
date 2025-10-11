package net.zen.addon.features.commands.commands.info;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.commands.arguments.PlayerArgumentType;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Formatting;

public class UuidCommand extends Command {
    public UuidCommand() {
        super("uuid", "Gets a player's UUID.");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        // Get your own UUID
        builder.executes(context -> {
            if (mc.player == null) {
                error("You need to be in-game to use this command.");
                return SINGLE_SUCCESS;
            }
            
            String uuid = mc.player.getUuid().toString();
            info(Formatting.GRAY + "Your UUID: " + Formatting.WHITE + uuid);
            return SINGLE_SUCCESS;
        });

        // Get another player's UUID
        builder.then(argument("player", PlayerArgumentType.create())
            .executes(context -> {
                PlayerEntity player = PlayerArgumentType.get(context);
                String playerName = player.getName().getString();
                String uuid = player.getUuid().toString();
                info(Formatting.GRAY + playerName + "'s UUID: " + Formatting.WHITE + uuid);
                return SINGLE_SUCCESS;
            })
        );
    }
}