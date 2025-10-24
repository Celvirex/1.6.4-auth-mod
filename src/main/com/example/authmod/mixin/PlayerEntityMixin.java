package com.example.authmod.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import com.example.authmod.AuthMod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin {
    
    @Inject(method = "onUpdate", at = @At("HEAD"))
    private void onUpdate(CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        
        // Check if player is not authenticated
        if (!AuthMod.isPlayerAuthenticated(player.getUuid())) {
            // Prevent movement by resetting velocity
            player.velocityX = 0;
            player.velocityY = 0;
            player.velocityZ = 0;
            player.fallDistance = 0.0f; // Prevent fall damage
        }
    }
}
