package net.zen.addon.events;

import meteordevelopment.meteorclient.events.Cancellable;
import net.minecraft.network.packet.s2c.common.CustomPayloadS2CPacket;
public class S2CPacketEvent extends Cancellable {
    private static final S2CPacketEvent INSTANCE = new S2CPacketEvent();

    public CustomPayloadS2CPacket packet;

    public static S2CPacketEvent get(CustomPayloadS2CPacket packet) {
        INSTANCE.setCancelled(false);
        INSTANCE.packet = packet;
        return INSTANCE;
    }
}