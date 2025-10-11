package net.zen.addon.features.commands.commands.info;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.commands.arguments.PlayerArgumentType;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Formatting;
import net.minecraft.client.network.PlayerListEntry;

public class StatsCommand extends Command {
    public StatsCommand() {
        super("stats", "Shows detailed information about a player.");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        // Show your own stats
        builder.executes(context -> {
            if (mc.player == null) {
                error("You need to be in-game to use this command.");
                return SINGLE_SUCCESS;
            }
            
            displayStats(mc.player);
            return SINGLE_SUCCESS;
        });

        // Show another player's stats
        builder.then(argument("player", PlayerArgumentType.create())
            .executes(context -> {
                PlayerEntity player = PlayerArgumentType.get(context);
                displayStats(player);
                return SINGLE_SUCCESS;
            })
        );
    }

    private void displayStats(PlayerEntity player) {
        // Get player information
        String name = player.getName().getString();
        String uuid = player.getUuid().toString();
        float health = player.getHealth();
        float maxHealth = player.getMaxHealth();
        int hunger = player.getHungerManager().getFoodLevel();
        float saturation = player.getHungerManager().getSaturationLevel();
        double x = player.getX();
        double y = player.getY();
        double z = player.getZ();
        float pitch = player.getPitch();
        float yaw = player.getYaw();
        
        // Get ping
        int ping = 0;
        PlayerListEntry entry = mc.getNetworkHandler().getPlayerListEntry(player.getUuid());
        if (entry != null) {
            ping = entry.getLatency();
        }
        
        // Display information
        info(Formatting.AQUA + "=== " + Formatting.GOLD + name + Formatting.AQUA + "'s Stats ===");
        info(Formatting.GRAY + "UUID: " + Formatting.WHITE + uuid);
        info(Formatting.GRAY + "Health: " + getHealthColor(health, maxHealth) + String.format("%.1f", health) + 
             Formatting.GRAY + "/" + Formatting.GREEN + String.format("%.1f", maxHealth));
        info(Formatting.GRAY + "Hunger: " + getHungerColor(hunger) + hunger + 
             Formatting.GRAY + " (Saturation: " + Formatting.YELLOW + String.format("%.1f", saturation) + Formatting.GRAY + ")");
        info(Formatting.GRAY + "Position: " + Formatting.WHITE + String.format("%.1f, %.1f, %.1f", x, y, z));
        info(Formatting.GRAY + "Rotation: " + Formatting.WHITE + String.format("Pitch: %.1f, Yaw: %.1f", pitch, yaw));
        info(Formatting.GRAY + "Ping: " + getPingColor(ping) + ping + "ms");
        
        // Try to get IP (only works for yourself)
        if (player == mc.player && mc.getNetworkHandler() != null && mc.getNetworkHandler().getConnection() != null) {
            String address = mc.getNetworkHandler().getConnection().getAddress().toString();
            if (address.startsWith("/")) {
                address = address.substring(1);
            }
            info(Formatting.GRAY + "IP: " + Formatting.WHITE + address);
        }
    }
    
    private Formatting getHealthColor(float health, float maxHealth) {
        float percentage = health / maxHealth;
        if (percentage <= 0.25) return Formatting.RED;
        if (percentage <= 0.5) return Formatting.GOLD;
        if (percentage <= 0.75) return Formatting.YELLOW;
        return Formatting.GREEN;
    }
    
    private Formatting getHungerColor(int hunger) {
        if (hunger <= 6) return Formatting.RED;
        if (hunger <= 12) return Formatting.GOLD;
        if (hunger <= 16) return Formatting.YELLOW;
        return Formatting.GREEN;
    }
    
    private Formatting getPingColor(int ping) {
        if (ping >= 300) return Formatting.RED;
        if (ping >= 150) return Formatting.GOLD;
        if (ping >= 75) return Formatting.YELLOW;
        return Formatting.GREEN;
    }
}