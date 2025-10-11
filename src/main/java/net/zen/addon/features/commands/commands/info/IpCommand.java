package net.zen.addon.features.commands.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.command.CommandSource;
import net.minecraft.util.Formatting;

public class IpCommand extends Command {
    public IpCommand() {
        super("ip", "Gets the current server's IP address.");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
            if (mc.player == null || mc.getNetworkHandler() == null || mc.getNetworkHandler().getConnection() == null) {
                error("You need to be connected to a server to use this command.");
                return SINGLE_SUCCESS;
            }
            
            String address = mc.getNetworkHandler().getConnection().getAddress().toString();
            if (address.startsWith("/")) {
                address = address.substring(1);
            }
            
            // Split address into IP and port
            String[] parts = address.split(":");
            String ip = parts[0];
            String port = parts.length > 1 ? parts[1] : "25565";
            
            info(Formatting.GRAY + "Current Server: " + Formatting.WHITE + ip + Formatting.GRAY + " (Port: " + Formatting.WHITE + port + Formatting.GRAY + ")");
            return SINGLE_SUCCESS;
        });
    }
}