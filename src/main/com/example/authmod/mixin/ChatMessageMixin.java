package com.example.authmod.mixin;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import com.example.authmod.AuthMod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public class ChatMessageMixin {
    
    @Inject(method = "sendMessage", at = @At("HEAD"), cancellable = true)
    private void onSendMessage(Text message, CallbackInfo ci) {
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
        
        // Allow login/register messages but block regular chat
        if (!AuthMod.isPlayerAuthenticated(player.getUuid())) {
            String messageString = message.getString();
            // Only allow system messages (not player chat)
            if (messageString.startsWith("§") || 
                messageString.contains("login") || 
                messageString.contains("register") ||
                messageString.contains("password")) {
                return; // Allow these messages
            }
            ci.cancel();
        }
    }
}
