package net.zen.addon.features.modules.world;

import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Moses extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> range = sgGeneral.add(new DoubleSetting.Builder()
            .name("range")
            .description("The range around the player to replace liquids with air.")
            .defaultValue(5.0)
            .min(1.0)
            .max(10.0)
            .build()
    );

    private final Setting<Boolean> preventFallDamage = sgGeneral.add(new BoolSetting.Builder()
            .name("prevent-fall-damage")
            .description("Prevents fall damage when landing in liquids.")
            .defaultValue(true)
            .build()
    );
    
    private final Setting<Boolean> affectLava = sgGeneral.add(new BoolSetting.Builder()
            .name("affect-lava")
            .description("Also replaces lava with air.")
            .defaultValue(false)
            .build()
    );
    
    private final Setting<Boolean> handleWaterlogged = sgGeneral.add(new BoolSetting.Builder()
            .name("handle-waterlogged")
            .description("Removes water from waterlogged blocks.")
            .defaultValue(true)
            .build()
    );
    
    private final Setting<Boolean> removeSeaVegetation = sgGeneral.add(new BoolSetting.Builder()
            .name("remove-sea-vegetation")
            .description("Removes seagrass, tall seagrass, and kelp.")
            .defaultValue(true)
            .build()
    );
    
    private final Setting<Boolean> handleBubbleColumns = sgGeneral.add(new BoolSetting.Builder()
            .name("handle-bubble-columns")
            .description("Removes bubble columns.")
            .defaultValue(true)
            .build()
    );

    private final Map<BlockPos, BlockState> originalBlocks = new HashMap<>();
    private final Map<BlockPos, BlockState> originalWaterloggedBlocks = new HashMap<>();
    private final Map<BlockPos, BlockState> originalVegetationBlocks = new HashMap<>();
    private final Map<BlockPos, BlockState> originalBubbleBlocks = new HashMap<>();
    private boolean isInLiquid = false;
    private int tickCounter = 0;

    public Moses() {
        super(Categories.Movement, "moses", "Parts liquids like Moses, allowing you to walk through them as if they were air.");
    }

    @Override
    public void onActivate() {
        originalBlocks.clear();
        originalWaterloggedBlocks.clear();
        originalVegetationBlocks.clear();
        originalBubbleBlocks.clear();
    }

    @Override
    public void onDeactivate() {
        // Restore all modified blocks
        for (BlockPos pos : originalBlocks.keySet()) {
            mc.world.setBlockState(pos, originalBlocks.get(pos));
        }
        
        // Restore all waterlogged blocks
        for (BlockPos pos : originalWaterloggedBlocks.keySet()) {
            mc.world.setBlockState(pos, originalWaterloggedBlocks.get(pos));
        }
        
        // Restore all vegetation blocks
        for (BlockPos pos : originalVegetationBlocks.keySet()) {
            mc.world.setBlockState(pos, originalVegetationBlocks.get(pos));
        }
        
        // Restore all bubble column blocks
        for (BlockPos pos : originalBubbleBlocks.keySet()) {
            mc.world.setBlockState(pos, originalBubbleBlocks.get(pos));
        }
        
        originalBlocks.clear();
        originalWaterloggedBlocks.clear();
        originalVegetationBlocks.clear();
        originalBubbleBlocks.clear();
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.world == null) return;

        tickCounter++;
        
        // Check if player is in liquid
        isInLiquid = mc.player.isTouchingWater() || (affectLava.get() && mc.player.isInLava());

        // Aggressive swimming prevention
        preventSwimming();

        // Prevent fire overlay by extinguishing the player when in lava
        if (affectLava.get() && mc.player.isOnFire()) {
            mc.player.setFireTicks(0);
        }

        // Get player position
        Vec3d playerPos = mc.player.getPos();
        double rangeSq = range.get() * range.get();
        double outerRangeSq = (range.get() + 1) * (range.get() + 1);
        int outerRangeInt = range.get().intValue() + 1;

        // Track which blocks we've seen this tick
        Set<BlockPos> currentInRangeBlocks = new HashSet<>();
        Set<BlockPos> currentInRangeWaterloggedBlocks = new HashSet<>();
        Set<BlockPos> currentInRangeVegetationBlocks = new HashSet<>();
        Set<BlockPos> currentInRangeBubbleBlocks = new HashSet<>();

        // Scan blocks around player
        for (int x = -outerRangeInt; x <= outerRangeInt; x++) {
            for (int y = -outerRangeInt; y <= outerRangeInt; y++) {
                for (int z = -outerRangeInt; z <= outerRangeInt; z++) {
                    BlockPos pos = new BlockPos(
                            (int) Math.floor(playerPos.x) + x,
                            (int) Math.floor(playerPos.y) + y,
                            (int) Math.floor(playerPos.z) + z
                    );

                    // Calculate squared distance to player
                    double distSq = pos.getSquaredDistance(playerPos.x, playerPos.y, playerPos.z);
                    
                    BlockState state = mc.world.getBlockState(pos);
                    boolean isWater = state.getBlock() == Blocks.WATER;
                    boolean isLava = state.getBlock() == Blocks.LAVA;
                    boolean isLiquid = isWater || (affectLava.get() && isLava);
                    
                    // Check for sea vegetation
                    boolean isSeaVegetation = removeSeaVegetation.get() && (
                        state.getBlock() == Blocks.SEAGRASS ||
                        state.getBlock() == Blocks.TALL_SEAGRASS ||
                        state.getBlock() == Blocks.KELP ||
                        state.getBlock() == Blocks.KELP_PLANT
                    );
                    
                    // Check for bubble columns
                    boolean isBubbleColumn = handleBubbleColumns.get() && 
                                            state.getBlock() == Blocks.BUBBLE_COLUMN;
                    
                    // Handle waterlogged blocks
                    boolean isWaterlogged = handleWaterlogged.get() && 
                                           state.contains(Properties.WATERLOGGED) && 
                                           state.get(Properties.WATERLOGGED);
                    
                    // Check if the block is within inner range
                    if (distSq <= rangeSq) {
                        // Handle liquid blocks
                        if (isLiquid) {
                            // Add to current in-range blocks
                            currentInRangeBlocks.add(pos);
                            
                            // Store original block if we haven't modified it yet
                            if (!originalBlocks.containsKey(pos)) {
                                originalBlocks.put(pos, state);
                            }
                            
                            // Set the block to air client-side
                            mc.world.setBlockState(pos, Blocks.AIR.getDefaultState());
                        }
                        
                        // Handle sea vegetation
                        if (isSeaVegetation) {
                            // Add to current in-range vegetation blocks
                            currentInRangeVegetationBlocks.add(pos);
                            
                            // Store original block if we haven't modified it yet
                            if (!originalVegetationBlocks.containsKey(pos)) {
                                originalVegetationBlocks.put(pos, state);
                            }
                            
                            // Set the block to air client-side
                            mc.world.setBlockState(pos, Blocks.AIR.getDefaultState());
                        }
                        
                        // Handle bubble columns
                        if (isBubbleColumn) {
                            // Add to current in-range bubble blocks
                            currentInRangeBubbleBlocks.add(pos);
                            
                            // Store original block if we haven't modified it yet
                            if (!originalBubbleBlocks.containsKey(pos)) {
                                originalBubbleBlocks.put(pos, state);
                            }
                            
                            // Set the block to air client-side
                            mc.world.setBlockState(pos, Blocks.AIR.getDefaultState());
                        }
                        
                        // Handle waterlogged blocks
                        if (isWaterlogged) {
                            // Add to current in-range waterlogged blocks
                            currentInRangeWaterloggedBlocks.add(pos);
                            
                            // Store original block if we haven't modified it yet
                            if (!originalWaterloggedBlocks.containsKey(pos)) {
                                originalWaterloggedBlocks.put(pos, state);
                            }
                            
                            // Set the block to non-waterlogged
                            BlockState newState = state.with(Properties.WATERLOGGED, false);
                            mc.world.setBlockState(pos, newState);
                        }
                    }
                    // Check if block is in the outer ring (range+1) - restore if needed
                    else if (distSq > rangeSq && distSq <= outerRangeSq) {
                        // Restore liquid blocks
                        if (originalBlocks.containsKey(pos)) {
                            mc.world.setBlockState(pos, originalBlocks.get(pos));
                            originalBlocks.remove(pos);
                        }
                        
                        // Restore waterlogged blocks
                        if (originalWaterloggedBlocks.containsKey(pos)) {
                            mc.world.setBlockState(pos, originalWaterloggedBlocks.get(pos));
                            originalWaterloggedBlocks.remove(pos);
                        }
                        
                        // Restore vegetation blocks
                        if (originalVegetationBlocks.containsKey(pos)) {
                            mc.world.setBlockState(pos, originalVegetationBlocks.get(pos));
                            originalVegetationBlocks.remove(pos);
                        }
                        
                        // Restore bubble column blocks
                        if (originalBubbleBlocks.containsKey(pos)) {
                            mc.world.setBlockState(pos, originalBubbleBlocks.get(pos));
                            originalBubbleBlocks.remove(pos);
                        }
                    }
                }
            }
        }

        // Find blocks that are now beyond the outer range
        Set<BlockPos> toRemoveBlocks = new HashSet<>();
        for (BlockPos pos : originalBlocks.keySet()) {
            double distSq = pos.getSquaredDistance(playerPos.x, playerPos.y, playerPos.z);
            
            // If block is beyond outer range
            if (distSq > outerRangeSq) {
                mc.world.setBlockState(pos, originalBlocks.get(pos));
                toRemoveBlocks.add(pos);
            }
        }
        
        // Find waterlogged blocks that are now beyond the outer range
        Set<BlockPos> toRemoveWaterlogged = new HashSet<>();
        for (BlockPos pos : originalWaterloggedBlocks.keySet()) {
            double distSq = pos.getSquaredDistance(playerPos.x, playerPos.y, playerPos.z);
            
            // If block is beyond outer range
            if (distSq > outerRangeSq) {
                mc.world.setBlockState(pos, originalWaterloggedBlocks.get(pos));
                toRemoveWaterlogged.add(pos);
            }
        }
        
        // Find vegetation blocks that are now beyond the outer range
        Set<BlockPos> toRemoveVegetation = new HashSet<>();
        for (BlockPos pos : originalVegetationBlocks.keySet()) {
            double distSq = pos.getSquaredDistance(playerPos.x, playerPos.y, playerPos.z);
            
            // If block is beyond outer range
            if (distSq > outerRangeSq) {
                mc.world.setBlockState(pos, originalVegetationBlocks.get(pos));
                toRemoveVegetation.add(pos);
            }
        }
        
        // Find bubble column blocks that are now beyond the outer range
        Set<BlockPos> toRemoveBubble = new HashSet<>();
        for (BlockPos pos : originalBubbleBlocks.keySet()) {
            double distSq = pos.getSquaredDistance(playerPos.x, playerPos.y, playerPos.z);
            
            // If block is beyond outer range
            if (distSq > outerRangeSq) {
                mc.world.setBlockState(pos, originalBubbleBlocks.get(pos));
                toRemoveBubble.add(pos);
            }
        }
        
        // Clean up the tracking maps
        originalBlocks.keySet().removeAll(toRemoveBlocks);
        originalWaterloggedBlocks.keySet().removeAll(toRemoveWaterlogged);
        originalVegetationBlocks.keySet().removeAll(toRemoveVegetation);
        originalBubbleBlocks.keySet().removeAll(toRemoveBubble);

        // Prevent fall damage when landing in liquid
        if (preventFallDamage.get() && isInLiquid) {
            mc.player.fallDistance = 0;
        }
    }
    
    // Prevent swimming state
    @EventHandler
    private void onPlayerMove(PlayerMoveEvent event) {
        if (mc.player != null) {
            // Force player out of swimming state
            preventSwimming();
            
            // Prevent fire overlay by extinguishing the player
            if (affectLava.get() && mc.player.isOnFire()) {
                mc.player.setFireTicks(0);
            }
            
            // Cancel water movement effects
            if (isInLiquid && !mc.player.isOnGround()) {
                // Only apply this every few ticks to avoid jitter
                if (tickCounter % 3 == 0) {
                    // Apply velocity directly without canceling the event
                    Vec3d currentVel = mc.player.getVelocity();
                    mc.player.setVelocity(
                        currentVel.x,
                        currentVel.y * 1.1, // Slightly boost vertical movement
                        currentVel.z
                    );
                }
            }
        }
    }
    
    // More aggressive swimming prevention
    private void preventSwimming() {
        if (mc.player == null) return;
        
        // Force player out of swimming state
        mc.player.setSwimming(false);
        
        // Prevent swimming animation
        if (mc.player.isInSwimmingPose()) {
            mc.player.setPose(EntityPose.STANDING);
        }
        
        // Remove swimming status effects that might be causing the animation
        if (mc.player.hasStatusEffect(StatusEffects.DOLPHINS_GRACE)) {
            mc.player.removeStatusEffect(StatusEffects.DOLPHINS_GRACE);
        }
        
        // Ensure sprinting is maintained if the player is trying to sprint
        if (mc.options.sprintKey.isPressed() && !mc.player.isSprinting()) {
            mc.player.setSprinting(true);
        }
        
        // Additional swimming prevention
        if (mc.player.isTouchingWater()) {
            // Force player velocity to not be affected by water
            Vec3d vel = mc.player.getVelocity();
            if (vel.y < 0 && mc.options.jumpKey.isPressed()) {
                // If player is pressing jump, help them rise in water
                mc.player.setVelocity(vel.x, 0.1, vel.z);
            }
        }
    }
}