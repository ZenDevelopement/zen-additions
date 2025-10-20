package net.zen.addon.utils;

import net.minecraft.SharedConstants;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.particle.ParticleEffect;

/**
 * Helper class to handle version-specific API changes between Minecraft versions.
 * Uses reflection exclusively to avoid compile-time issues across different MC versions.
 */
public class VersionHelper {

    private static final boolean IS_1_21_6_OR_LATER;

    static {
        // Check if we're running 1.21.6 or later
        String version = getMinecraftVersion();
        IS_1_21_6_OR_LATER = compareVersions(version, "1.21.6") >= 0;
    }

    /**
     * Get Minecraft version using reflection to handle API changes
     */
    private static String getMinecraftVersion() {
        try {
            Object gameVersion = SharedConstants.getGameVersion();
            // Try getName() first (1.21.4-1.21.5)
            try {
                return (String) gameVersion.getClass().getMethod("getName").invoke(gameVersion);
            } catch (NoSuchMethodException e) {
                // Try getId() for 1.21.6+
                return (String) gameVersion.getClass().getMethod("getId").invoke(gameVersion);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "1.21.4"; // Fallback
        }
    }

    /**
     * Compare two version strings (e.g., "1.21.4" vs "1.21.6")
     */
    private static int compareVersions(String v1, String v2) {
        String[] parts1 = v1.split("\\.");
        String[] parts2 = v2.split("\\.");

        int length = Math.max(parts1.length, parts2.length);
        for (int i = 0; i < length; i++) {
            int num1 = i < parts1.length ? parseIntSafely(parts1[i]) : 0;
            int num2 = i < parts2.length ? parseIntSafely(parts2[i]) : 0;

            if (num1 != num2) {
                return Integer.compare(num1, num2);
            }
        }
        return 0;
    }

    private static int parseIntSafely(String str) {
        try {
            String cleaned = str.replaceAll("[^\\d]", "");
            return cleaned.isEmpty() ? 0 : Integer.parseInt(cleaned);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Parse NBT string - handles API changes between versions using reflection
     */
    public static NbtCompound parseNbt(String nbtString) throws Exception {
        try {
            Class<?> stringNbtReaderClass = Class.forName("net.minecraft.nbt.StringNbtReader");

            if (IS_1_21_6_OR_LATER) {
                // 1.21.6+ uses parse(String, ParserConfig)
                Class<?> parserConfigClass = Class.forName("net.minecraft.nbt.StringNbtReader$ParserConfig");
                Object defaultConfig = parserConfigClass.getDeclaredField("DEFAULT").get(null);

                return (NbtCompound) stringNbtReaderClass
                    .getMethod("parse", String.class, parserConfigClass)
                    .invoke(null, nbtString, defaultConfig);
            } else {
                // 1.21.4-1.21.5 uses parse(String)
                return (NbtCompound) stringNbtReaderClass
                    .getMethod("parse", String.class)
                    .invoke(null, nbtString);
            }
        } catch (Exception e) {
            throw new Exception("Failed to parse NBT: " + e.getMessage(), e);
        }
    }

    /**
     * Read NBT data into an entity - handles API changes using reflection
     */
    public static void readEntityNbt(Entity entity, NbtCompound nbt) {
        try {
            if (IS_1_21_6_OR_LATER) {
                // 1.21.6+ uses readNbt(NbtCompound, RegistryWrapper.WrapperLookup)
                Class<?> wrapperLookupClass = Class.forName("net.minecraft.registry.RegistryWrapper$WrapperLookup");
                entity.getClass()
                    .getMethod("readNbt", NbtCompound.class, wrapperLookupClass)
                    .invoke(entity, nbt, entity.getWorld().getRegistryManager());
            } else {
                // 1.21.4-1.21.5 uses readNbt(NbtCompound)
                entity.getClass()
                    .getMethod("readNbt", NbtCompound.class)
                    .invoke(entity, nbt);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Write entity NBT data - handles API changes using reflection
     */
    public static void writeEntityNbt(Entity entity, NbtCompound nbt) {
        try {
            if (IS_1_21_6_OR_LATER) {
                // 1.21.6+ uses writeNbt(NbtCompound, RegistryWrapper.WrapperLookup)
                Class<?> wrapperLookupClass = Class.forName("net.minecraft.registry.RegistryWrapper$WrapperLookup");
                entity.getClass()
                    .getMethod("writeNbt", NbtCompound.class, wrapperLookupClass)
                    .invoke(entity, nbt, entity.getWorld().getRegistryManager());
            } else {
                // 1.21.4-1.21.5 uses writeNbt(NbtCompound)
                entity.getClass()
                    .getMethod("writeNbt", NbtCompound.class)
                    .invoke(entity, nbt);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Create a ClickEvent - handles abstract class changes using reflection
     */
    public static ClickEvent createClickEvent(ClickEvent.Action action, String value) {
        try {
            if (IS_1_21_6_OR_LATER) {
                // 1.21.6+ uses static factory methods
                switch (action) {
                    case OPEN_URL:
                        return (ClickEvent) ClickEvent.class.getMethod("openUrl", String.class).invoke(null, value);
                    case RUN_COMMAND:
                        return (ClickEvent) ClickEvent.class.getMethod("runCommand", String.class).invoke(null, value);
                    case SUGGEST_COMMAND:
                        return (ClickEvent) ClickEvent.class.getMethod("suggestCommand", String.class).invoke(null, value);
                    case CHANGE_PAGE:
                        int page = 1;
                        try {
                            page = Integer.parseInt(value);
                        } catch (NumberFormatException ignored) {}
                        return (ClickEvent) ClickEvent.class.getMethod("changePage", int.class).invoke(null, page);
                    case COPY_TO_CLIPBOARD:
                        return (ClickEvent) ClickEvent.class.getMethod("copyToClipboard", String.class).invoke(null, value);
                    default:
                        return (ClickEvent) ClickEvent.class.getMethod("runCommand", String.class).invoke(null, value);
                }
            } else {
                // 1.21.4-1.21.5 uses constructor
                return (ClickEvent) ClickEvent.class
                    .getConstructor(ClickEvent.Action.class, String.class)
                    .newInstance(action, value);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Create a HoverEvent for showing text - handles abstract class changes using reflection
     */
    public static HoverEvent createShowTextHoverEvent(Text text) {
        try {
            if (IS_1_21_6_OR_LATER) {
                // 1.21.6+ uses static factory method
                return (HoverEvent) HoverEvent.class.getMethod("showText", Text.class).invoke(null, text);
            } else {
                // 1.21.4-1.21.5 uses constructor
                return (HoverEvent) HoverEvent.class
                    .getConstructor(HoverEvent.Action.class, Object.class)
                    .newInstance(HoverEvent.Action.SHOW_TEXT, text);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get selected slot from player inventory - handles access changes using reflection
     */
    public static int getSelectedSlot(net.minecraft.entity.player.PlayerInventory inventory) {
        try {
            if (IS_1_21_6_OR_LATER) {
                // 1.21.6+ selectedSlot is private, use getter or reflection
                try {
                    // Try public getter first if it exists
                    return (int) inventory.getClass().getMethod("getSelectedSlot").invoke(inventory);
                } catch (NoSuchMethodException e) {
                    // Use field reflection
                    java.lang.reflect.Field field = inventory.getClass().getDeclaredField("selectedSlot");
                    field.setAccessible(true);
                    return field.getInt(inventory);
                }
            } else {
                // 1.21.4-1.21.5 selectedSlot is public, but use reflection to be safe
                java.lang.reflect.Field field = inventory.getClass().getField("selectedSlot");
                return field.getInt(inventory);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * Add colored particle - handles API changes using reflection
     */
    public static void addColoredParticle(net.minecraft.client.world.ClientWorld world,
                                          ParticleEffect particle,
                                          double x, double y, double z,
                                          double velX, double velY, double velZ) {
        try {
            // Both versions have addParticle but with different parameter orders in 1.21.6+
            // The common signature is: addParticle(double, double, double, double, double, ParticleEffect)
            // where the last 3 doubles before ParticleEffect are velocities
            world.getClass()
                .getMethod("addParticle", double.class, double.class, double.class, double.class, double.class, ParticleEffect.class)
                .invoke(world, x, y, z, velX, velY, velZ, particle);
        } catch (NoSuchMethodException e) {
            // Try alternate signature if that fails
            try {
                world.getClass()
                    .getMethod("addParticle", ParticleEffect.class, double.class, double.class, double.class, double.class, double.class, double.class)
                    .invoke(world, particle, x, y, z, velX, velY, velZ);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean is1_21_6OrLater() {
        return IS_1_21_6_OR_LATER;
    }
}
