package net.zen.addon.mixins;

import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.network.listener.ClientCommonPacketListener;
import net.minecraft.network.packet.s2c.common.CustomPayloadS2CPacket;
import net.zen.addon.events.S2CPacketEvent;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CustomPayloadS2CPacket.class)
public class S2CPacketMixin {
    @Inject(method = "apply(Lnet/minecraft/network/listener/ClientCommonPacketListener;)V",
            at = @At(value = "HEAD"), cancellable = true)
    private void onApply(ClientCommonPacketListener clientCommonPacketListener, CallbackInfo info) {
        CustomPayloadS2CPacket packet = (CustomPayloadS2CPacket) (Object) this;
        S2CPacketEvent event = MeteorClient.EVENT_BUS.post(S2CPacketEvent.get(packet));
        if (event.isCancelled()) {
            info.cancel();
        }
    }
}