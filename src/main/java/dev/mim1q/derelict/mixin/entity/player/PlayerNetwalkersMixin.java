package dev.mim1q.derelict.mixin.entity.player;

import dev.mim1q.derelict.init.ModBlocksAndItems;
import dev.mim1q.derelict.tag.ModBlockTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerNetwalkersMixin {
    @Shadow
    public abstract ItemStack getItemBySlot(EquipmentSlot slot);

    @Inject(
        method = "makeStuckInBlock",
        at = @At("HEAD"),
        cancellable = true
    )
    private void derelict$slowMovement(BlockState state, Vec3 multiplier, CallbackInfo ci) {
        if (state.is(ModBlockTags.INSTANCE.getCOBWEBS())
            && getItemBySlot(EquipmentSlot.LEGS).is(ModBlocksAndItems.INSTANCE.getNETWALKERS())
        ) {
            ci.cancel();
        }
    }
}
