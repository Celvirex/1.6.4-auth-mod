package com.example.authmod.mixin;

import net.minecraft.network.ClientConnection;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import com.example.authmod.AuthMod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {
    
    @Shadow
    public ServerPlayerEntity player;
    
    @Inject(method = "onDisconnected", at = @At("HEAD"))
    private void onDisconnected(Text reason, CallbackInfo ci) {
        if (player != null) {
            AuthMod.playerLoggedOut(player.getUuid());
        }
    }
    
    @Inject(method = "onChatMessage", at = @At("HEAD"), cancellable = true)
    private void onChatMessage(String message, CallbackInfo ci) {
        if (player != null && !AuthMod.isPlayerAuthenticated(player.getUuid())) {
            player.sendMessage(Text.literal("§cPlease log in before chatting!"));
            ci.cancel();
        }
    }
}
