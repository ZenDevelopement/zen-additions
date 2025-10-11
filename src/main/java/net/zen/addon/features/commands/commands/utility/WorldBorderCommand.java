package net.zen.addon.features.commands.commands.utility;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.command.CommandSource;
import net.minecraft.world.border.WorldBorder;

public class WorldBorderCommand extends Command {
    public WorldBorderCommand() {
        super("worldborder", "Manipulate client-side world border", "wb");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        // Set border size
        builder.then(literal("set")
            .then(argument("size", DoubleArgumentType.doubleArg(1))
                .executes(context -> {
                    double size = DoubleArgumentType.getDouble(context, "size");
                    mc.player.getWorld().getWorldBorder().setSize(size);
                    info("Set world border size to %.1f blocks", size);
                    return SINGLE_SUCCESS;
                })
                .then(argument("seconds", IntegerArgumentType.integer(0))
                    .executes(context -> {
                        double size = DoubleArgumentType.getDouble(context, "size");
                        int seconds = IntegerArgumentType.getInteger(context, "seconds");
                        mc.player.getWorld().getWorldBorder().interpolateSize(
                            mc.player.getWorld().getWorldBorder().getSize(),
                            size,
                            seconds * 1000L
                        );
                        info("Changing world border size to %.1f blocks over %d seconds", size, seconds);
                        return SINGLE_SUCCESS;
                    })
                )
            )
        );

        // Set border center
        builder.then(literal("center")
            .then(argument("x", DoubleArgumentType.doubleArg())
                .then(argument("z", DoubleArgumentType.doubleArg())
                    .executes(context -> {
                        double x = DoubleArgumentType.getDouble(context, "x");
                        double z = DoubleArgumentType.getDouble(context, "z");
                        mc.player.getWorld().getWorldBorder().setCenter(x, z);
                        info("Set world border center to %.1f, %.1f", x, z);
                        return SINGLE_SUCCESS;
                    })
                )
            )
            .then(literal("player")
                .executes(context -> {
                    if (mc.player == null) return SINGLE_SUCCESS;
                    double x = mc.player.getX();
                    double z = mc.player.getZ();
                    mc.player.getWorld().getWorldBorder().setCenter(x, z);
                    info("Set world border center to your position (%.1f, %.1f)", x, z);
                    return SINGLE_SUCCESS;
                })
            )
        );

        // Set border damage
        builder.then(literal("damage")
            .then(argument("damagePerBlock", DoubleArgumentType.doubleArg(0))
                .executes(context -> {
                    double damage = DoubleArgumentType.getDouble(context, "damagePerBlock");
                    mc.player.getWorld().getWorldBorder().setDamagePerBlock(damage);
                    info("Set world border damage to %.2f per block", damage);
                    return SINGLE_SUCCESS;
                })
            )
        );

        // Set border warning distance
        builder.then(literal("warning")
            .then(literal("distance")
                .then(argument("blocks", IntegerArgumentType.integer(0))
                    .executes(context -> {
                        int distance = IntegerArgumentType.getInteger(context, "blocks");
                        mc.player.getWorld().getWorldBorder().setWarningBlocks(distance);
                        info("Set world border warning distance to %d blocks", distance);
                        return SINGLE_SUCCESS;
                    })
                )
            )
            .then(literal("time")
                .then(argument("seconds", IntegerArgumentType.integer(0))
                    .executes(context -> {
                        int time = IntegerArgumentType.getInteger(context, "seconds");
                        mc.player.getWorld().getWorldBorder().setWarningTime(time);
                        info("Set world border warning time to %d seconds", time);
                        return SINGLE_SUCCESS;
                    })
                )
            )
        );

        // Get border info
        builder.then(literal("info")
            .executes(context -> {
                WorldBorder border = mc.player.getWorld().getWorldBorder();
                double size = border.getSize();
                double x = border.getCenterX();
                double z = border.getCenterZ();
                double damage = border.getDamagePerBlock();
                int warningDistance = border.getWarningBlocks();
                int warningTime = border.getWarningTime();
                
                info("§b--- World Border Info ---");
                info("§aSize: §f%.1f blocks", size);
                info("§aCenter: §f%.1f, %.1f", x, z);
                info("§aDamage: §f%.2f per block", damage);
                info("§aWarning Distance: §f%d blocks", warningDistance);
                info("§aWarning Time: §f%d seconds", warningTime);
                return SINGLE_SUCCESS;
            })
        );

        // Reset border to default
        builder.then(literal("reset")
            .executes(context -> {
                WorldBorder border = mc.player.getWorld().getWorldBorder();
                border.setSize(5.9999968E7);
                border.setCenter(0, 0);
                border.setDamagePerBlock(0.2);
                border.setWarningBlocks(5);
                border.setWarningTime(15);
                info("Reset world border to default values");
                return SINGLE_SUCCESS;
            })
        );
    }
}