package net.zen.addon.features.commands.commands.utility;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import net.minecraft.command.CommandSource;
import net.minecraft.item.*;
import net.minecraft.util.Formatting;

public class EquipCommand extends Command {
    public EquipCommand() {
        super("equip", "Equips the item in your hand to a specified armor slot.");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        // Equip to head slot
        builder.then(literal("head")
            .executes(context -> equipHeldItem(39, "head"))
        );
        
        // Equip to chest slot
        builder.then(literal("chest")
            .executes(context -> equipHeldItem(38, "chest"))
        );
        
        // Equip to legs slot
        builder.then(literal("legs")
            .executes(context -> equipHeldItem(37, "legs"))
        );
        
        // Equip to feet slot
        builder.then(literal("feet")
            .executes(context -> equipHeldItem(36, "feet"))
        );
        
        // Equip to offhand slot
        builder.then(literal("offhand")
            .executes(context -> equipHeldItem(45, "offhand"))
        );
        
        // Show help if no arguments provided
        builder.executes(context -> {
            info("Usage: .equip <head|chest|legs|feet|offhand>");
            return SINGLE_SUCCESS;
        });
    }

    private int equipHeldItem(int targetSlot, String slotName) {
        if (mc.player == null) {
            error("You need to be in-game to use this command.");
            return SINGLE_SUCCESS;
        }

        // Get the item in the player's hand
        int handSlot = mc.player.getInventory().selectedSlot + 36;
        ItemStack stack = mc.player.getInventory().getStack(handSlot);
        
        if (stack.isEmpty()) {
            error("You're not holding any item.");
            return SINGLE_SUCCESS;
        }

        // Try to move the item to the target slot
        InvUtils.move().from(handSlot).to(targetSlot);
        
        // Check if the move was successful by comparing the item in the target slot
        ItemStack targetStack = mc.player.getInventory().getStack(targetSlot);
        if (targetStack.getItem() == stack.getItem()) {
            info("Equipped " + Formatting.GREEN + stack.getName().getString() + Formatting.WHITE + " to " + slotName + " slot.");
        } else {
            error("Cannot equip " + stack.getName().getString() + " to " + slotName + " slot.");
        }
        
        return SINGLE_SUCCESS;
    }
}