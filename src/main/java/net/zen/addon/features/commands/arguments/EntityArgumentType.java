package net.zen.addon.features.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class EntityArgumentType implements ArgumentType<Entity> {
    private static final DynamicCommandExceptionType NO_SUCH_ENTITY = new DynamicCommandExceptionType(
        id -> Text.literal("Entity with ID " + id + " doesn't exist.")
    );

    public static EntityArgumentType entity() {
        return new EntityArgumentType();
    }

    public static Entity getEntity(CommandContext<?> context, String name) {
        return context.getArgument(name, Entity.class);
    }

    @Override
    public Entity parse(StringReader reader) throws CommandSyntaxException {
        int start = reader.getCursor();
        while (reader.canRead() && (Character.isDigit(reader.peek()) || reader.peek() == '-')) {
            reader.skip();
        }

        String argument = reader.getString().substring(start, reader.getCursor());
        
        try {
            int id = Integer.parseInt(argument);
            
            if (mc.world == null) throw NO_SUCH_ENTITY.create(argument);
            
            for (Entity entity : mc.world.getEntities()) {
                if (entity.getId() == id) {
                    return entity;
                }
            }
            
            throw NO_SUCH_ENTITY.create(argument);
        } catch (NumberFormatException e) {
            throw NO_SUCH_ENTITY.create(argument);
        }
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        if (mc.world == null) return Suggestions.empty();
        
        List<String> suggestions = new ArrayList<>();
        
        // Add the entity the player is looking at
        if (mc.crosshairTarget != null && mc.crosshairTarget.getType() == HitResult.Type.ENTITY) {
            EntityHitResult hit = (EntityHitResult) mc.crosshairTarget;
            Entity entity = hit.getEntity();
            String name = entity.getName().getString();
            String id = String.valueOf(entity.getId());
            suggestions.add(id + " (" + name + ")");
        }
        
        // Add nearby entities - fixed by converting Iterable to List
        List<Entity> entities = new ArrayList<>();
        mc.world.getEntities().forEach(entities::add);
        
        for (Entity entity : entities) {
            if (entity == mc.player) continue;
            
            String name = entity.getName().getString();
            String id = String.valueOf(entity.getId());
            
            // Only add if it's not already the looked-at entity
            if (!suggestions.contains(id + " (" + name + ")")) {
                suggestions.add(id + " (" + name + ")");
            }
        }
        
        return CommandSource.suggestMatching(suggestions.stream()
            .filter(s -> s.toLowerCase().startsWith(builder.getRemaining().toLowerCase()))
            .collect(Collectors.toList()), builder);
    }

    @Override
    public Collection<String> getExamples() {
        return List.of("42", "123");
    }
}