package net.zen.addon.features.modules;

import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;

import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.entity.projectile.thrown.*;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.Vec3d;

import java.lang.reflect.Field;
import java.util.*;

public class ProjectileTrails extends Module {
    // Setting groups
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgRender = settings.createGroup("Render");
    private final SettingGroup sgParticles = settings.createGroup("Particles");
    private final SettingGroup sgProjectiles = settings.createGroup("Projectiles");
    private final SettingGroup sgAdvanced = settings.createGroup("Advanced");

    // General settings
    private final Setting<Integer> maxPoints = sgGeneral.add(new IntSetting.Builder()
        .name("max-points")
        .description("Maximum number of points in the trail.")
        .defaultValue(300)
        .min(10)
        .sliderRange(10, 1000)
        .build()
    );

    private final Setting<Integer> pointInterval = sgGeneral.add(new IntSetting.Builder()
        .name("point-interval")
        .description("How many ticks between each point. Lower = more dense trails.")
        .defaultValue(1)
        .min(1)
        .sliderRange(1, 10)
        .build()
    );

    private final Setting<Boolean> fadeOut = sgGeneral.add(new BoolSetting.Builder()
        .name("fade-out")
        .description("Fades out the trail over time.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Integer> fadeRate = sgGeneral.add(new IntSetting.Builder()
        .name("fade-rate")
        .description("How quickly the trail fades out (lower = slower fade).")
        .defaultValue(2)
        .min(1)
        .sliderRange(1, 10)
        .visible(fadeOut::get)
        .build()
    );

    // Render settings
    private final Setting<RenderMode> renderMode = sgRender.add(new EnumSetting.Builder<RenderMode>()
        .name("render-mode")
        .description("How to render the trails.")
        .defaultValue(RenderMode.Lines)
        .build()
    );

    private final Setting<ColorMode> colorMode = sgRender.add(new EnumSetting.Builder<ColorMode>()
        .name("color-mode")
        .description("How to color the projectile trails.")
        .defaultValue(ColorMode.Selected)
        .build()
    );

    private final Setting<SettingColor> trailColor = sgRender.add(new ColorSetting.Builder()
        .name("trail-color")
        .description("The color of the projectile trails.")
        .defaultValue(new SettingColor(255, 0, 0, 255))
        .visible(() -> colorMode.get() == ColorMode.Selected)
        .build()
    );

    private final Setting<Boolean> uniqueColors = sgRender.add(new BoolSetting.Builder()
        .name("unique-projectile-colors")
        .description("Use different colors for different types of projectiles.")
        .defaultValue(true)
        .visible(() -> colorMode.get() == ColorMode.Selected)
        .build()
    );

    private final Setting<SettingColor> arrowColor = sgRender.add(new ColorSetting.Builder()
        .name("arrow-color")
        .description("The color for arrows.")
        .defaultValue(new SettingColor(255, 0, 0, 255))
        .visible(() -> colorMode.get() == ColorMode.Selected && uniqueColors.get())
        .build()
    );

    private final Setting<SettingColor> tridentColor = sgRender.add(new ColorSetting.Builder()
        .name("trident-color")
        .description("The color for tridents.")
        .defaultValue(new SettingColor(0, 255, 255, 255))
        .visible(() -> colorMode.get() == ColorMode.Selected && uniqueColors.get())
        .build()
    );

    private final Setting<SettingColor> snowballColor = sgRender.add(new ColorSetting.Builder()
        .name("snowball-color")
        .description("The color for snowballs.")
        .defaultValue(new SettingColor(255, 255, 255, 255))
        .visible(() -> colorMode.get() == ColorMode.Selected && uniqueColors.get())
        .build()
    );

    private final Setting<SettingColor> eggColor = sgRender.add(new ColorSetting.Builder()
        .name("egg-color")
        .description("The color for eggs.")
        .defaultValue(new SettingColor(255, 255, 0, 255))
        .visible(() -> colorMode.get() == ColorMode.Selected && uniqueColors.get())
        .build()
    );

    private final Setting<SettingColor> enderPearlColor = sgRender.add(new ColorSetting.Builder()
        .name("ender-pearl-color")
        .description("The color for ender pearls.")
        .defaultValue(new SettingColor(0, 255, 0, 255))
        .visible(() -> colorMode.get() == ColorMode.Selected && uniqueColors.get())
        .build()
    );

    private final Setting<SettingColor> potionColor = sgRender.add(new ColorSetting.Builder()
        .name("potion-color")
        .description("The color for potions.")
        .defaultValue(new SettingColor(128, 0, 128, 255))
        .visible(() -> colorMode.get() == ColorMode.Selected && uniqueColors.get())
        .build()
    );

    private final Setting<SettingColor> experienceBottleColor = sgRender.add(new ColorSetting.Builder()
        .name("experience-bottle-color")
        .description("The color for experience bottles.")
        .defaultValue(new SettingColor(0, 128, 0, 255))
        .visible(() -> colorMode.get() == ColorMode.Selected && uniqueColors.get())
        .build()
    );

    // Rainbow settings
    private final Setting<Double> rainbowSpeed = sgRender.add(new DoubleSetting.Builder()
        .name("rainbow-speed")
        .description("The speed of the rainbow color cycle.")
        .defaultValue(0.5)
        .min(0.1)
        .sliderRange(0.1, 2.0)
        .visible(() -> colorMode.get() == ColorMode.Rainbow)
        .build()
    );

    private final Setting<Double> rainbowSaturation = sgRender.add(new DoubleSetting.Builder()
        .name("rainbow-saturation")
        .description("The saturation of the rainbow colors.")
        .defaultValue(0.75)
        .min(0.0)
        .max(1.0)
        .sliderRange(0.0, 1.0)
        .visible(() -> colorMode.get() == ColorMode.Rainbow)
        .build()
    );

    private final Setting<Double> rainbowBrightness = sgRender.add(new DoubleSetting.Builder()
        .name("rainbow-brightness")
        .description("The brightness of the rainbow colors.")
        .defaultValue(1.0)
        .min(0.0)
        .max(1.0)
        .sliderRange(0.0, 1.0)
        .visible(() -> colorMode.get() == ColorMode.Rainbow)
        .build()
    );

    private final Setting<Double> rainbowSpread = sgRender.add(new DoubleSetting.Builder()
        .name("rainbow-spread")
        .description("How spread out the rainbow colors are along the trail.")
        .defaultValue(0.05)
        .min(0.001)
        .sliderRange(0.001, 0.1)
        .visible(() -> colorMode.get() == ColorMode.Rainbow)
        .build()
    );

    // Particle settings
    private final Setting<Boolean> useParticles = sgParticles.add(new BoolSetting.Builder()
        .name("use-particles")
        .description("Use Minecraft's particle system for trails.")
        .defaultValue(false)
        .build()
    );

    private final Setting<ParticleType> particleType = sgParticles.add(new EnumSetting.Builder<ParticleType>()
        .name("particle-type")
        .description("The type of particle to use for trails.")
        .defaultValue(ParticleType.Flame)
        .visible(useParticles::get)
        .build()
    );

    private final Setting<Integer> particleDensity = sgParticles.add(new IntSetting.Builder()
        .name("particle-density")
        .description("How many particles to spawn per point.")
        .defaultValue(1)
        .min(1)
        .sliderRange(1, 5)
        .visible(useParticles::get)
        .build()
    );

    private final Setting<Integer> particleLifetime = sgParticles.add(new IntSetting.Builder()
        .name("particle-lifetime")
        .description("How long particles should last in ticks.")
        .defaultValue(20)
        .min(5)
        .sliderRange(5, 100)
        .visible(useParticles::get)
        .build()
    );

    // Projectile settings
    private final Setting<Boolean> showArrows = sgProjectiles.add(new BoolSetting.Builder()
        .name("arrows")
        .description("Show trails for arrows.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> showTridents = sgProjectiles.add(new BoolSetting.Builder()
        .name("tridents")
        .description("Show trails for tridents.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> showSnowballs = sgProjectiles.add(new BoolSetting.Builder()
        .name("snowballs")
        .description("Show trails for snowballs.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> showEggs = sgProjectiles.add(new BoolSetting.Builder()
        .name("eggs")
        .description("Show trails for eggs.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> showEnderPearls = sgProjectiles.add(new BoolSetting.Builder()
        .name("ender-pearls")
        .description("Show trails for ender pearls.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> showPotions = sgProjectiles.add(new BoolSetting.Builder()
        .name("potions")
        .description("Show trails for potions.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> showExperienceBottles = sgProjectiles.add(new BoolSetting.Builder()
        .name("experience-bottles")
        .description("Show trails for experience bottles.")
        .defaultValue(true)
        .build()
    );

    // Advanced settings
    private final Setting<Boolean> removeOnGround = sgAdvanced.add(new BoolSetting.Builder()
        .name("remove-on-ground")
        .description("Removes the trail when the projectile hits the ground.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> showOnlyYours = sgAdvanced.add(new BoolSetting.Builder()
        .name("only-your-projectiles")
        .description("Only show trails for projectiles you threw.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Integer> lineThickness = sgAdvanced.add(new IntSetting.Builder()
        .name("line-thickness")
        .description("Thickness of the trail lines.")
        .defaultValue(1)
        .min(1)
        .sliderRange(1, 5)
        .visible(() -> renderMode.get() == RenderMode.Lines || renderMode.get() == RenderMode.Both)
        .build()
    );

    // Enums
    public enum ColorMode {
        Random,
        Selected,
        Rainbow
    }

    public enum RenderMode {
        Lines,
        Particles,
        Both
    }

    public enum ParticleType {
        Flame,
        Soul,
        Smoke,
        EndRod,
        Portal,
        Enchant,
        Crit,
        Firework
    }

    // Map to store projectile trails
    private final Map<Integer, Trail> projectiles = new HashMap<>();
    private final Map<Integer, Boolean> groundState = new HashMap<>();
    private int tickCounter = 0;

    public ProjectileTrails() {
        super(Categories.Render, "projectile-trails", "Adds customizable trails to projectiles like arrows, tridents, and throwables.");
    }

    // Enum to identify projectile types
    private enum ProjectileType {
        ARROW,
        TRIDENT,
        SNOWBALL,
        EGG,
        ENDER_PEARL,
        POTION,
        EXPERIENCE_BOTTLE,
        OTHER
    }

    // Trail class to store trail data
    private class Trail {
        public final List<Vec3d> points;
        public final Color color;
        public boolean wasInGround = false;
        public final List<ParticlePoint> particles;

        public Trail(ProjectileType type) {
            this.points = new ArrayList<>();
            this.particles = new ArrayList<>();
            
            // Generate color based on selected mode and projectile type
            if (colorMode.get() == ColorMode.Random) {
                this.color = Color.fromHsv(Utils.random(0.0D, 360.0D), 0.75D, 1.0D);
            } else if (colorMode.get() == ColorMode.Selected && uniqueColors.get()) {
                this.color = getColorForType(type);
            } else {
                this.color = trailColor.get().copy();
            }
        }

        private Color getColorForType(ProjectileType type) {
            return switch (type) {
                case ARROW -> arrowColor.get().copy();
                case TRIDENT -> tridentColor.get().copy();
                case SNOWBALL -> snowballColor.get().copy();
                case EGG -> eggColor.get().copy();
                case ENDER_PEARL -> enderPearlColor.get().copy();
                case POTION -> potionColor.get().copy();
                case EXPERIENCE_BOTTLE -> experienceBottleColor.get().copy();
                default -> trailColor.get().copy();
            };
        }

        public void render(Render3DEvent event, Integer entityId) {
            if (points.isEmpty() || points.size() < 2) return;
            
            if (renderMode.get() == RenderMode.Lines || renderMode.get() == RenderMode.Both) {
                renderLines(event);
            }
        }
        
        private void renderLines(Render3DEvent event) {
            // Render trail
            for (int i = 0; i < points.size() - 1; i++) {
                Color renderColor;
                
                if (colorMode.get() == ColorMode.Rainbow) {
                    // Improved rainbow effect
                    double baseHue = (System.currentTimeMillis() * rainbowSpeed.get() / 1000.0) % 360;
                    double pointPosition = (double) i / points.size();
                    double hue = (baseHue + (pointPosition * 360 * rainbowSpread.get())) % 360;
                    renderColor = Color.fromHsv(hue, rainbowSaturation.get(), rainbowBrightness.get());
                } else {
                    renderColor = color.copy();
                }
                
                // Calculate alpha for fade effect - improved to be more visible
                if (fadeOut.get()) {
                    float alpha = (float) i / points.size();
                    renderColor.a = (int) (255 * alpha);
                }
                
                Vec3d current = points.get(i);
                Vec3d next = points.get(i + 1);
                
                // Draw line with specified thickness
                for (int t = 0; t < lineThickness.get(); t++) {
                    event.renderer.line(
                        current.x, current.y, current.z,
                        next.x, next.y, next.z,
                        renderColor
                    );
                }
            }
        }
    }

    // Class to store particle information
    private class ParticlePoint {
        public final Vec3d pos;
        public final int lifetime;
        public int age;

        public ParticlePoint(Vec3d pos, int lifetime) {
            this.pos = pos;
            this.lifetime = lifetime;
            this.age = 0;
        }

        public boolean update() {
            age++;
            return age <= lifetime;
        }
    }

    @Override
    public void onActivate() {
        projectiles.clear();
        groundState.clear();
        tickCounter = 0;
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        tickCounter++;
        
        // Process current projectiles and update trails
        for (Entity entity : mc.world.getEntities()) {
            if (isTrackableProjectile(entity)) {
                // Determine projectile type
                ProjectileType type = getProjectileType(entity);
                
                // Skip if this type is disabled
                if (!isProjectileTypeEnabled(type)) continue;
                
                // Check if it's player's projectile
                boolean isPlayerProjectile = isOwnedByPlayer(entity);
                
                // Skip if we only want player projectiles and this isn't one
                if (showOnlyYours.get() && !isPlayerProjectile) continue;
                
                // Check if projectile is in ground
                boolean inGround = isInGround(entity);
                
                // Store ground state
                Integer entityId = entity.getId();
                groundState.put(entityId, inGround);
                
                // Skip if projectile is in ground and we're removing on ground
                if (inGround && removeOnGround.get()) {
                    projectiles.remove(entityId);
                    continue;
                }
                
                // Get or create trail
                Trail trail = projectiles.computeIfAbsent(entityId, id -> new Trail(type));
                
                // Check if the projectile was previously in ground but now isn't
                if (trail.wasInGround && !inGround) {
                    // Clear the trail if the projectile was thrown again
                    trail.points.clear();
                    trail.particles.clear();
                }
                
                trail.wasInGround = inGround;
                
                // Add point if position changed and interval matches
                if (tickCounter % pointInterval.get() == 0) {
                    Vec3d pos = entity.getPos();
                    if (trail.points.isEmpty() || !trail.points.get(trail.points.size() - 1).equals(pos)) {
                        trail.points.add(pos);
                        
                        // Add particle point
                        if (useParticles.get() || renderMode.get() == RenderMode.Particles || renderMode.get() == RenderMode.Both) {
                            trail.particles.add(new ParticlePoint(pos, particleLifetime.get()));
                        }
                        
                        // Trim points if exceeding max
                        while (trail.points.size() > maxPoints.get()) {
                            trail.points.remove(0);
                        }
                    }
                }
            }
        }

        // Clean up old projectiles
        projectiles.entrySet().removeIf(entry -> 
            mc.world.getEntityById(entry.getKey()) == null
        );

        // Update fade out - completely rewritten
        if (fadeOut.get()) {
            for (Trail trail : projectiles.values()) {
                // Only remove points periodically based on fade rate
                if (tickCounter % (11 - fadeRate.get()) == 0 && !trail.points.isEmpty()) {
                    trail.points.remove(0);
                }
            }
        }

        // Update and spawn particles
        for (Trail trail : projectiles.values()) {
            // Update existing particles
            trail.particles.removeIf(p -> !p.update());
            
            // Spawn new particles
            if (useParticles.get() || renderMode.get() == RenderMode.Particles || renderMode.get() == RenderMode.Both) {
                for (ParticlePoint point : trail.particles) {
                    spawnParticleAtPoint(point, trail);
                }
            }
        }
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        // Render all trails
        projectiles.forEach((id, trail) -> trail.render(event, id));
    }

    private void spawnParticleAtPoint(ParticlePoint point, Trail trail) {
        if (mc.world == null) return;
        
        // Only spawn particles periodically to avoid overwhelming the client
        if (tickCounter % 2 != 0) return;
        
        for (int i = 0; i < particleDensity.get(); i++) {
            ParticleEffect particle = getParticleEffect(trail);
            
            if (particle != null) {
                // Add small random offset for more natural look
                double offsetX = (Math.random() - 0.5) * 0.05;
                double offsetY = (Math.random() - 0.5) * 0.05;
                double offsetZ = (Math.random() - 0.5) * 0.05;
                
                mc.world.addParticle(
                    particle,
                    point.pos.x + offsetX, 
                    point.pos.y + offsetY, 
                    point.pos.z + offsetZ,
                    0, 0, 0
                );
            }
        }
    }

    private ParticleEffect getParticleEffect(Trail trail) {
        // Use standard particles based on the selected type
        return switch (particleType.get()) {
            case Flame -> ParticleTypes.FLAME;
            case Soul -> ParticleTypes.SOUL_FIRE_FLAME;
            case Smoke -> ParticleTypes.SMOKE;
            case EndRod -> ParticleTypes.END_ROD;
            case Portal -> ParticleTypes.PORTAL;
            case Enchant -> ParticleTypes.ENCHANT;
            case Crit -> ParticleTypes.CRIT;
            case Firework -> ParticleTypes.FIREWORK;
        };
    }

    private boolean isTrackableProjectile(Entity entity) {
        return entity instanceof PersistentProjectileEntity || // Arrows and tridents
               entity instanceof SnowballEntity ||
               entity instanceof EggEntity ||
               entity instanceof EnderPearlEntity ||
               entity instanceof PotionEntity ||
               entity instanceof ExperienceBottleEntity;
    }

    private ProjectileType getProjectileType(Entity entity) {
        if (entity instanceof ArrowEntity) return ProjectileType.ARROW;
        if (entity instanceof TridentEntity) return ProjectileType.TRIDENT;
        if (entity instanceof SnowballEntity) return ProjectileType.SNOWBALL;
        if (entity instanceof EggEntity) return ProjectileType.EGG;
        if (entity instanceof EnderPearlEntity) return ProjectileType.ENDER_PEARL;
        if (entity instanceof PotionEntity) return ProjectileType.POTION;
        if (entity instanceof ExperienceBottleEntity) return ProjectileType.EXPERIENCE_BOTTLE;
        return ProjectileType.OTHER;
    }

    private boolean isProjectileTypeEnabled(ProjectileType type) {
        return switch (type) {
            case ARROW -> showArrows.get();
            case TRIDENT -> showTridents.get();
            case SNOWBALL -> showSnowballs.get();
            case EGG -> showEggs.get();
            case ENDER_PEARL -> showEnderPearls.get();
            case POTION -> showPotions.get();
            case EXPERIENCE_BOTTLE -> showExperienceBottles.get();
            default -> true;
        };
    }

    private boolean isOwnedByPlayer(Entity entity) {
        if (entity instanceof ProjectileEntity projectile) {
            return projectile.getOwner() == mc.player;
        }
        return false;
    }

    private boolean isInGround(Entity entity) {
        if (entity instanceof PersistentProjectileEntity) {
            try {
                // Try to access the inGround field using reflection
                Field inGround = PersistentProjectileEntity.class.getDeclaredField("inGround");
                inGround.setAccessible(true);
                return inGround.getBoolean(entity);
            } catch (Exception e) {
                // Fallback method if reflection fails
                return ((PersistentProjectileEntity) entity).isOnGround();
            }
        }
        return entity.isOnGround();
    }
}