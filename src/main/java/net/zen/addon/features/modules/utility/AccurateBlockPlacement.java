package net.zen.addon.features.modules.utility;

import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.*;
import net.minecraft.item.BlockItem;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.state.property.Property;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class AccurateBlockPlacement extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> reverse = sgGeneral.add(new BoolSetting.Builder()
        .name("reverse")
        .description("Makes blocks face away from the direction you're looking.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> onlyDirectional = sgGeneral.add(new BoolSetting.Builder()
        .name("only-directional")
        .description("Only affects blocks with directional properties.")
        .defaultValue(true)
        .build()
    );
    
    public AccurateBlockPlacement() {
        super(Categories.Misc, "accurate-block-placement", "Makes blocks face the direction you're looking at when placing them.");
    }

    @EventHandler
    private void onSendPacket(PacketEvent.Send event) {
        if (!(event.packet instanceof PlayerInteractBlockC2SPacket)) return;
        PlayerInteractBlockC2SPacket packet = (PlayerInteractBlockC2SPacket) event.packet;
        if (mc.player == null || mc.world == null) return;
        if (packet.getHand() != Hand.MAIN_HAND) return;

        // Get the block being placed
        if (!(mc.player.getMainHandStack().getItem() instanceof BlockItem)) return;
        BlockItem blockItem = (BlockItem) mc.player.getMainHandStack().getItem();
        
        Block block = blockItem.getBlock();
        BlockState defaultState = block.getDefaultState();
        
        // Skip if only directional is enabled and block has no directional properties
        if (onlyDirectional.get() && !hasDirectionalProperty(defaultState)) return;

        // Calculate the direction based on player's look direction
        Direction facingDirection = getLookDirection();
        if (facingDirection == null) return;

        // Apply reverse if enabled
        if (reverse.get()) {
            facingDirection = facingDirection.getOpposite();
        }

        // For now, we'll use a simpler approach by modifying player rotation
        // This is more reliable than trying to modify packet hit results
        float yaw = mc.player.getYaw();
        float pitch = mc.player.getPitch();
        
        // Set rotation based on desired facing direction
        switch (facingDirection) {
            case NORTH -> { mc.player.setYaw(180f); mc.player.setPitch(0f); }
            case SOUTH -> { mc.player.setYaw(0f); mc.player.setPitch(0f); }
            case EAST -> { mc.player.setYaw(-90f); mc.player.setPitch(0f); }
            case WEST -> { mc.player.setYaw(90f); mc.player.setPitch(0f); }
            case UP -> { mc.player.setPitch(-90f); }
            case DOWN -> { mc.player.setPitch(90f); }
        }
        
        // Restore original rotation after a tick
        mc.execute(() -> {
            if (mc.player != null) {
                mc.player.setYaw(yaw);
                mc.player.setPitch(pitch);
            }
        });
    }
    
    private boolean hasDirectionalProperty(BlockState state) {
        for (Property<?> property : state.getProperties()) {
            String name = property.getName();
            if (name.equals("facing") || name.equals("horizontal_facing") || name.equals("axis")) {
                return true;
            }
        }
        return false;
    }
    
    private Direction getLookDirection() {
        if (mc.player == null) return null;
        
        // Get the direction the player is looking at
        Vec3d lookVec = mc.player.getRotationVec(1.0f);
        
        // Find the dominant axis
        double absX = Math.abs(lookVec.x);
        double absY = Math.abs(lookVec.y);
        double absZ = Math.abs(lookVec.z);
        
        if (absY > absX && absY > absZ) {
            // Y is dominant
            return lookVec.y > 0 ? Direction.UP : Direction.DOWN;
        } else if (absX > absZ) {
            // X is dominant
            return lookVec.x > 0 ? Direction.EAST : Direction.WEST;
        } else {
            // Z is dominant
            return lookVec.z > 0 ? Direction.SOUTH : Direction.NORTH;
        }
    }
}
