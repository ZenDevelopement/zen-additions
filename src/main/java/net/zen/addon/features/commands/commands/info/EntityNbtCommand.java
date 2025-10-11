package net.zen.addon.features.commands.commands.info;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.zen.addon.features.commands.arguments.EntityArgumentType;

public class EntityNbtCommand extends Command {
    public EntityNbtCommand() {
        super("entitynbt", "View or modify an entity's NBT data.");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        // View entity NBT
        builder.then(argument("entity", EntityArgumentType.entity())
            .executes(context -> {
                Entity entity = EntityArgumentType.getEntity(context, "entity");
                displayEntityNbt(entity);
                return SINGLE_SUCCESS;
            })
            
            // Set (replace) entity NBT
            .then(literal("set")
                .then(argument("nbtData", StringArgumentType.greedyString())
                    .executes(context -> {
                        Entity entity = EntityArgumentType.getEntity(context, "entity");
                        String nbtString = StringArgumentType.getString(context, "nbtData");
                        
                        info(Formatting.YELLOW + "Note: " + Formatting.WHITE + "Attempting to modify entity NBT. This may require operator privileges on servers.");
                        
                        try {
                            // Parse the NBT data
                            NbtCompound nbt = StringNbtReader.parse(nbtString);
                            
                            // Apply the NBT data to the entity
                            entity.readNbt(nbt);
                            
                            info("Successfully applied NBT data to " + entity.getName().getString());
                        } catch (CommandSyntaxException e) {
                            error("Invalid NBT format: " + e.getMessage());
                        } catch (Exception e) {
                            error("Failed to modify entity: " + e.getMessage());
                            error("This likely means you don't have the required permissions.");
                        }
                        
                        return SINGLE_SUCCESS;
                    })
                )
            )
            
            // Merge entity NBT (add without removing existing)
            .then(literal("merge")
                .then(argument("nbtData", StringArgumentType.greedyString())
                    .executes(context -> {
                        Entity entity = EntityArgumentType.getEntity(context, "entity");
                        String nbtString = StringArgumentType.getString(context, "nbtData");
                        
                        info(Formatting.YELLOW + "Note: " + Formatting.WHITE + "Attempting to merge entity NBT. This may require operator privileges on servers.");
                        
                        try {
                            // Get current NBT
                            NbtCompound currentNbt = new NbtCompound();
                            entity.writeNbt(currentNbt);
                            
                            // Parse the new NBT data
                            NbtCompound newNbt = StringNbtReader.parse(nbtString);
                            
                            // Merge the NBT data
                            currentNbt.copyFrom(newNbt);
                            
                            // Apply the merged NBT data
                            entity.readNbt(currentNbt);
                            
                            info("Successfully merged NBT data to " + entity.getName().getString());
                        } catch (CommandSyntaxException e) {
                            error("Invalid NBT format: " + e.getMessage());
                        } catch (Exception e) {
                            error("Failed to modify entity: " + e.getMessage());
                            error("This likely means you don't have the required permissions.");
                        }
                        
                        return SINGLE_SUCCESS;
                    })
                )
            )
            
            // Get specific NBT path
            .then(literal("get")
                .then(argument("path", StringArgumentType.word())
                    .executes(context -> {
                        Entity entity = EntityArgumentType.getEntity(context, "entity");
                        String path = StringArgumentType.getString(context, "path");
                        
                        NbtCompound nbt = new NbtCompound();
                        entity.writeNbt(nbt);
                        
                        // Simple path navigation (only top-level for now)
                        if (nbt.contains(path)) {
                            info(Formatting.GRAY + "Path '" + path + "': " + Formatting.WHITE + nbt.get(path).toString());
                        } else {
                            error("Path '" + path + "' not found in entity NBT data.");
                        }
                        
                        return SINGLE_SUCCESS;
                    })
                )
            )
            
            // Copy NBT to clipboard
            .then(literal("copy")
                .executes(context -> {
                    Entity entity = EntityArgumentType.getEntity(context, "entity");
                    NbtCompound nbt = new NbtCompound();
                    entity.writeNbt(nbt);
                    
                    mc.keyboard.setClipboard(nbt.toString());
                    info("Copied entity NBT data to clipboard.");
                    
                    return SINGLE_SUCCESS;
                })
            )
        );
        
        // Add a shortcut for the entity you're looking at
        builder.then(literal("look")
            .executes(context -> {
                if (mc.crosshairTarget == null || mc.crosshairTarget.getType() != HitResult.Type.ENTITY) {
                    error("You need to be looking at an entity.");
                    return SINGLE_SUCCESS;
                }
                
                EntityHitResult hit = (EntityHitResult) mc.crosshairTarget;
                Entity entity = hit.getEntity();
                
                displayEntityNbt(entity);
                
                return SINGLE_SUCCESS;
            })
        );
    }
    
    private void displayEntityNbt(Entity entity) {
        try {
            NbtCompound nbt = new NbtCompound();
            entity.writeNbt(nbt);
            
            String entityName = entity.getName().getString();
            String entityId = String.valueOf(entity.getId());
            String entityType = entity.getType().getName().getString();
            
            // Create copy button
            MutableText copyButton = Text.literal("[Copy NBT]").setStyle(Style.EMPTY
                .withFormatting(Formatting.UNDERLINE, Formatting.AQUA)
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/entitynbt " + entityId + " copy"))
                .withHoverEvent(new HoverEvent(
                    HoverEvent.Action.SHOW_TEXT,
                    Text.literal("Copy the NBT data to your clipboard.")
                )));
            
            // Display entity info
            info(Formatting.AQUA + "=== " + Formatting.GOLD + entityName + Formatting.AQUA + " (ID: " + entityId + ") ===");
            info(Formatting.GRAY + "Entity Type: " + Formatting.WHITE + entityType);
            
            // Display NBT data with copy button
            info(Text.empty().append(Formatting.GRAY + "NBT Data: ").append(copyButton));
            info(Formatting.WHITE + nbt.toString());
            
        } catch (Exception e) {
            error("Failed to get entity data: " + e.getMessage());
        }
    }
}