package net.zen.addon.features.modules.ghost;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.*;
import net.minecraft.util.math.BlockPos;

import java.util.*;

public class GhostBlockFly extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgBlocks = settings.createGroup("Blocks");
    private final SettingGroup sgAntiKick = settings.createGroup("Anti-Kick");

    // General settings
    private final Setting<Integer> maxGhostBlocks = sgGeneral.add(new IntSetting.Builder()
        .name("max-ghost-blocks")
        .description("Maximum number of ghost blocks before they start disappearing.")
        .defaultValue(3)
        .min(1)
        .sliderMax(10)
        .build()
    );

    // Block settings
    private final Setting<List<Block>> blockSelection = sgBlocks.add(new BlockListSetting.Builder()
        .name("block-selection")
        .description("Blocks to use for ghost blocks.")
        .defaultValue(Collections.singletonList(Blocks.BARRIER))
        .build()
    );
    
    private final Setting<Boolean> randomizeBlocks = sgBlocks.add(new BoolSetting.Builder()
        .name("randomize-blocks")
        .description("Randomizes block placement when multiple blocks are selected.")
        .defaultValue(true)
        .visible(() -> blockSelection.get().size() > 1)
        .build()
    );

    // Anti-kick settings
    private final Setting<Boolean> antiKick = sgAntiKick.add(new BoolSetting.Builder()
        .name("anti-kick")
        .description("Prevents you from being kicked by the server.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Integer> antiKickInterval = sgAntiKick.add(new IntSetting.Builder()
        .name("interval")
        .description("How often to trigger anti-kick in ticks.")
        .defaultValue(80)
        .min(20)
        .sliderMax(200)
        .visible(antiKick::get)
        .build()
    );

    private final Setting<Double> antiKickAmount = sgAntiKick.add(new DoubleSetting.Builder()
        .name("amount")
        .description("How much to move down for anti-kick.")
        .defaultValue(0.1)
        .min(0.05)
        .sliderMax(0.5)
        .visible(antiKick::get)
        .build()
    );

    private final Queue<BlockPosState> placedBlocks = new LinkedList<>();
    private final Random random = new Random();
    private int tickCounter = 0;
    private boolean antiKickActivated = false;

    public GhostBlockFly() {
        super(Categories.Movement, "ghost-block-fly", "Fly using ghost blocks with anti-kick protection.");
    }

    private static class BlockPosState {
        public BlockPos pos;
        public BlockState originalState;

        public BlockPosState(BlockPos pos, BlockState originalState) {
            this.pos = pos;
            this.originalState = originalState;
        }
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player == null || mc.world == null) return;

        // Get current position
        BlockPos currentPos = mc.player.getBlockPos().add(0, -1, 0);
        
        // Handle ghost block placement
        if (!mc.options.sneakKey.isPressed() && mc.world.getBlockState(currentPos).getBlock() instanceof AirBlock) {
            // Save original state before modifying
            BlockState originalState = mc.world.getBlockState(currentPos);
            
            // Select block to place
            Block blockToPlace = getSelectedBlock();
            
            // Place the ghost block
            mc.world.setBlockState(currentPos, blockToPlace.getDefaultState());
            
            // Add to queue
            placedBlocks.add(new BlockPosState(currentPos, originalState));
            
            // Remove oldest blocks if we exceed the limit
            while (placedBlocks.size() > maxGhostBlocks.get()) {
                BlockPosState oldest = placedBlocks.poll();
                if (oldest != null && !oldest.pos.equals(currentPos)) {
                    mc.world.setBlockState(oldest.pos, oldest.originalState);
                }
            }
        }

        // Handle block removal when sneaking
        if (mc.options.sneakKey.isPressed()) {
            BlockState currentState = mc.world.getBlockState(currentPos);
            if (!(currentState.getBlock() instanceof AirBlock)) {
                // Find if this is one of our ghost blocks
                Iterator<BlockPosState> iterator = placedBlocks.iterator();
                while (iterator.hasNext()) {
                    BlockPosState blockPosState = iterator.next();
                    if (blockPosState.pos.equals(currentPos)) {
                        mc.world.setBlockState(currentPos, blockPosState.originalState);
                        iterator.remove();
                        break;
                    }
                }
            }
        }

        // Smart anti-kick
        if (antiKick.get()) {
            tickCounter++;
            
            if (tickCounter >= antiKickInterval.get()) {
                antiKickActivated = true;
                mc.player.setVelocity(mc.player.getVelocity().x, -antiKickAmount.get(), mc.player.getVelocity().z);
                tickCounter = 0;
            } else if (antiKickActivated && tickCounter == 2) {
                // Reset position after anti-kick
                antiKickActivated = false;
                mc.player.setVelocity(mc.player.getVelocity().x, antiKickAmount.get(), mc.player.getVelocity().z);
            }
        }
    }

    private Block getSelectedBlock() {
        List<Block> blocks = blockSelection.get();
        
        if (blocks.isEmpty()) {
            return Blocks.BARRIER; // Default to barrier if list is empty
        }
        
        if (blocks.size() == 1 || !randomizeBlocks.get()) {
            return blocks.get(0); // Return first block if only one or randomization is off
        }
        
        // Randomize block selection
        return blocks.get(random.nextInt(blocks.size()));
    }

    @Override
    public void onDeactivate() {
        if (mc.world != null) {
            // Restore all placed ghost blocks
            for (BlockPosState blockPosState : placedBlocks) {
                mc.world.setBlockState(blockPosState.pos, blockPosState.originalState);
            }
        }
        
        placedBlocks.clear();
        tickCounter = 0;
        antiKickActivated = false;
    }
}