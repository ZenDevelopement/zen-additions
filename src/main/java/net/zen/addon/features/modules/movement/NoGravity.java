package net.zen.addon.features.modules.movement;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;

public class NoGravity extends Module {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();

    // Setting to control whether to completely disable gravity or just modify it
    private final Setting<Boolean> disableCompletely = sgGeneral.add(new BoolSetting.Builder()
        .name("disable-completely")
        .description("Completely disables gravity when enabled.")
        .defaultValue(true)
        .build()
    );

    // Setting to control custom gravity when not completely disabled
    private final Setting<Double> gravityValue = sgGeneral.add(new DoubleSetting.Builder()
        .name("gravity-value")
        .description("The custom gravity value to apply (0.0 = no gravity, 0.08 = normal gravity).")
        .defaultValue(0.04)
        .range(0.0, 0.08)
        .sliderRange(0.0, 0.08)
        .visible(() -> !disableCompletely.get())
        .build()
    );

    // Setting to control jump behavior
    private final Setting<Boolean> affectJumping = sgGeneral.add(new BoolSetting.Builder()
        .name("affect-jumping")
        .description("Apply custom gravity during jumps as well.")
        .defaultValue(true)
        .build()
    );

    public NoGravity() {
        super(Categories.World, "no-gravity", "Disables or modifies player gravity.");
    }

    @Override
    public void onActivate() {
        if (mc.player != null && disableCompletely.get()) {
            mc.player.setNoGravity(true);
        }
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player == null) return;
        
        // Update NoGravity status if setting changes while module is active
        if (disableCompletely.get()) {
            mc.player.setNoGravity(true);
        } else {
            // When using custom gravity, we need to have gravity enabled
            mc.player.setNoGravity(false);
            
            // Apply custom gravity when not on ground
            if (!mc.player.isOnGround()) {
                // Check if we should apply during jumping (positive Y velocity)
                boolean shouldApply = mc.player.getVelocity().y < 0 || 
                                     (affectJumping.get() && mc.player.getVelocity().y > 0);
                
                if (shouldApply) {
                    // Apply custom gravity by modifying velocity
                    mc.player.setVelocity(
                        mc.player.getVelocity().x,
                        mc.player.getVelocity().y + (0.08 - gravityValue.get()),
                        mc.player.getVelocity().z
                    );
                }
            }
        }
    }

    @Override
    public void onDeactivate() {
        if (mc.player != null) {
            mc.player.setNoGravity(false);
        }
    }

    @Override
    public String getInfoString() {
        return disableCompletely.get() ? "Off" : String.format("%.3f", gravityValue.get());
    }
}