package dev.mim1q.derelict.mixin.block;

import dev.mim1q.derelict.init.ModBlocksAndItems;
import dev.mim1q.derelict.interfaces.AbstractBlockAccessor;
import dev.mim1q.derelict.tag.ModBlockTags;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBehaviour.class)
public abstract class AbstractBlockMixin implements AbstractBlockAccessor {
    @Unique
    private boolean waxable = false;
    @Unique
    private boolean ageable = false;

    @Override
    public boolean isWaxable() {
        return waxable;
    }

    @Override
    public void setWaxable(boolean value) {
        waxable = value;
    }

    @Override
    public boolean isAgeable() {
        return ageable;
    }

    @Override
    public void setAgeable(boolean value) {
        ageable = value;
    }

    @Inject(
        method = "getCollisionShape",
        at = @At("HEAD"),
        cancellable = true
    )
    private void derelict$getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context, CallbackInfoReturnable<VoxelShape> cir) {
        if (state.is(ModBlockTags.INSTANCE.getFULL_COBWEBS())
            && derelict$shouldBeSolid(pos, context)
        ) {
            cir.setReturnValue(Shapes.block());
        }
    }

    @Unique
    private static boolean derelict$shouldBeSolid(BlockPos pos, CollisionContext ctx) {
        if (ctx instanceof EntityCollisionContext entityCtx && entityCtx.getEntity() instanceof Player player) {
            return ctx.isAbove(Shapes.block(), pos, false)
                && player.getItemBySlot(EquipmentSlot.LEGS).is(ModBlocksAndItems.INSTANCE.getNETWALKERS())
                && !player.isShiftKeyDown();
        }
        return false;
    }
}
