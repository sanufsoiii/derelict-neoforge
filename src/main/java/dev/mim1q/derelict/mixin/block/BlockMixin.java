package dev.mim1q.derelict.mixin.block;

import dev.mim1q.derelict.init.ModBlocksAndItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Block.class)
public class BlockMixin {
    @Inject(
        method = "canSupportCenter",
        at = @At("HEAD"),
        cancellable = true
    )
    private static void derelict$sideCoversSmallSquare(LevelReader world, BlockPos pos, Direction side, CallbackInfoReturnable<Boolean> cir) {
        if (side == Direction.DOWN && world.getBlockState(pos).is(ModBlocksAndItems.INSTANCE.getSPIDER_SILK_STRAND())) {
            cir.setReturnValue(true);
        }
    }
}
