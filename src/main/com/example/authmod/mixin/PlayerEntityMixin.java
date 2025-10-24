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
    
    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        
        // Check if player is not authenticated
        if (!AuthMod.isPlayerAuthenticated(player.getUuid())) {
            // Prevent movement by resetting velocity
            player.setVelocity(Vec3d.ZERO);
            player.fallDistance = 0.0f; // Prevent fall damage
        }
    }
    
    @Inject(method = "attack", at = @At("HEAD"), cancellable = true)
    private void onAttack(CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        if (!AuthMod.isPlayerAuthenticated(player.getUuid())) {
            ci.cancel();
        }
    }
    
    @Inject(method = "interact", at = @At("HEAD"), cancellable = true)
    private void onInteract(CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        if (!AuthMod.isPlayerAuthenticated(player.getUuid())) {
            ci.cancel();
        }
    }
}
