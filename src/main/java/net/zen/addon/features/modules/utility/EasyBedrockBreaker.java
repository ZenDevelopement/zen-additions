package net.zen.addon.features.modules;

import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInputC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import meteordevelopment.meteorclient.settings.BoolSetting;
import net.minecraft.text.Text;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Style;
import net.minecraft.util.Formatting;

import java.util.Queue;
import java.util.LinkedList;

public class EasyBedrockBreaker extends Module {
    private final SettingGroup sgInfo = settings.createGroup("Info");
    private final SettingGroup sgHelp = settings.createGroup("Help");
    
    // Renamed to follow Java naming conventions (lowercase first letter)
    private final Setting<Boolean> info = sgInfo.add(new BoolSetting.Builder()
        .name("Info")
        .description("Shows information about this module")
        .defaultValue(false)
        .onChanged(this::displayInfo)
        .build()
    );

    private final Setting<Boolean> showTutorial = sgHelp.add(new BoolSetting.Builder()
        .name("show-tutorial")
        .description("Shows a video tutorial on how to use this module.")
        .defaultValue(false)
        .onChanged(this::displayTutorial)
        .build()
    );

    // Using a Queue instead of ArrayList for better performance with packet ordering
    private final Queue<Packet<?>> delayedPackets = new LinkedList<>();
    private boolean isActive = false;

    public EasyBedrockBreaker() {
        super(Categories.Misc, "easy-bedrock-breaker", "Makes breaking bedrock easier and faster");
    }

    @Override
    public void onActivate() {
        isActive = true;
        delayedPackets.clear();
    }

    @EventHandler(priority = EventPriority.HIGHEST + 1)
    private void onSendPacket(PacketEvent.Send event) {
        if (!isActive) return;
        
        Packet<?> packet = event.packet;
        
        // Check if the packet is one of the specific types we want to delay
        if (isTargetPacket(packet)) {
            delayedPackets.add(packet);
            event.cancel();
        }
    }

    private boolean isTargetPacket(Packet<?> packet) {
        return packet instanceof PlayerActionC2SPacket ||
               packet instanceof PlayerInputC2SPacket ||
               packet instanceof PlayerInteractBlockC2SPacket ||
               packet instanceof PlayerInteractItemC2SPacket ||
               packet instanceof UpdateSelectedSlotC2SPacket;
    }

    @Override
    public void onDeactivate() {
        if (mc.getNetworkHandler() != null) {
            // Send all delayed packets in order
            while (!delayedPackets.isEmpty()) {
                mc.getNetworkHandler().sendPacket(delayedPackets.poll());
            }
        }
        isActive = false;
    }
    
    // Extracted methods to handle setting changes
    private void displayInfo(boolean val) {
        if (val && mc.player != null) {
            Text line1 = Text.literal("Inspired by ")
                .formatted(Formatting.GRAY)
                .append(Text.literal("garlic-bred on github,")
                    .setStyle(Style.EMPTY
                        .withFormatting(Formatting.UNDERLINE)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://github.com/garlic-bred/easy-bedrock-breaker"))
                    )
                );
            Text line2 = Text.literal("Code borrowed from ")
                .formatted(Formatting.GRAY)
                .append(Text.literal("cqb13 Numby Hack")
                    .setStyle(Style.EMPTY
                        .withFormatting(Formatting.UNDERLINE)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://github.com/cqb13/Numby-hack"))
                    )
                );
            Text line3 = Text.literal("Suggested using accurateBlockPlacement")
                .formatted(Formatting.GRAY);
            mc.player.sendMessage(line1, false);
            mc.player.sendMessage(line2, false);
            mc.player.sendMessage(line3, false);
            
            // Reset the setting after displaying info
            info.set(false);
        }
    }
    
    private void displayTutorial(boolean val) {
        if (val && mc.player != null) {
            Text message = Text.literal("Tutorial: ")
                .formatted(Formatting.AQUA)
                .append(Text.literal("Click here to watch the video")
                    .setStyle(Style.EMPTY
                        .withFormatting(Formatting.BLUE, Formatting.UNDERLINE)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.youtube.com/watch?v=SLL8uwEe5fc"))
                    )
                );
            
            mc.player.sendMessage(message, false);
            
            // Reset the setting after displaying tutorial
            showTutorial.set(false);
        }
    }
}