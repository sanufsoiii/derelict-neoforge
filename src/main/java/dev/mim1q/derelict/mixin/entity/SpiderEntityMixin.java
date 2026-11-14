package dev.mim1q.derelict.mixin.entity;

import dev.mim1q.derelict.tag.ModBlockTags;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Spider.class)
public class SpiderEntityMixin {
    @Inject(
        method = "makeStuckInBlock",
        at = @At("HEAD"),
        cancellable = true
    )
    private void derelict$slowMovement(BlockState state, Vec3 multiplier, CallbackInfo ci) {
        if (state.is(ModBlockTags.INSTANCE.getCOBWEBS())) {
            ci.cancel();
        }
    }
}
