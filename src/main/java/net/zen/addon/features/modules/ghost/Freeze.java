package net.zen.addon.features.modules.ghost;

import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

public class Freeze extends Module {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();

    private final Setting<Boolean> freezeMovement = sgGeneral.add(new BoolSetting.Builder()
        .name("freeze-movement")
        .description("Prevents the player from moving.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> freezeRotation = sgGeneral.add(new BoolSetting.Builder()
        .name("freeze-rotation")
        .description("Prevents the player from rotating their view.")
        .defaultValue(true)
        .build()
    );

    private double lastX, lastY, lastZ;
    private float lastYaw, lastPitch;
    private boolean initialized = false;

    public Freeze() {
        super(Categories.World, "freeze", "Completely freezes your player's position and rotation.");
    }

    @Override
    public void onActivate() {
        if (mc.player != null) {
            // Store current position and rotation when module is activated
            lastX = mc.player.getX();
            lastY = mc.player.getY();
            lastZ = mc.player.getZ();
            lastYaw = mc.player.getYaw();
            lastPitch = mc.player.getPitch();
            initialized = true;
        }
    }

    @EventHandler
    private void onPlayerMove(PlayerMoveEvent event) {
        if (!initialized) return;
        
        if (freezeMovement.get()) {
            // Create a new Vec3d instead of modifying the existing one
            event.movement = new net.minecraft.util.math.Vec3d(0, 0, 0);
        }
    }

    @EventHandler
    private void onSendPacket(PacketEvent.Send event) {
        if (!initialized || mc.player == null) return;
        
        if (event.packet instanceof PlayerMoveC2SPacket) {
            // For simplicity, just cancel all movement packets
            // This is the most reliable way to freeze the player
            if (freezeMovement.get() || freezeRotation.get()) {
                event.cancel();
            }
        }
    }

    @EventHandler
    private void onTick(meteordevelopment.meteorclient.events.world.TickEvent.Post event) {
        if (!initialized || mc.player == null) return;
        
        // Apply position freezing
        if (freezeMovement.get()) {
            mc.player.setPos(lastX, lastY, lastZ);
            mc.player.setVelocity(0, 0, 0);
        }
        
        // Apply rotation freezing
        if (freezeRotation.get()) {
            mc.player.setYaw(lastYaw);
            mc.player.setPitch(lastPitch);
        }
    }

    @Override
    public void onDeactivate() {
        initialized = false;
    }
}
