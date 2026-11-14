package dev.mim1q.derelict.mixin.block.entity;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockEntityType.class)
public abstract class BlockEntityTypeMixin {
    @SuppressWarnings("EqualsBetweenInconvertibleTypes")
    @Inject(method = "isValid", at = @At("HEAD"), cancellable = true)
    private void derelict$supports(BlockState state, CallbackInfoReturnable<Boolean> cir) {
        if (BlockEntityType.SIGN.equals(this) && state.is(BlockTags.SIGNS)) {
            cir.setReturnValue(true);
        }
    }
}
