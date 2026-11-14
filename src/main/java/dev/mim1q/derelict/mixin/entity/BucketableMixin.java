package dev.mim1q.derelict.mixin.entity;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.mim1q.derelict.entity.SpiderlingEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Bucketable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Bucketable.class)
public interface BucketableMixin {
    @WrapOperation(
        method = "bucketMobPickup",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/item/ItemStack;getItem()Lnet/minecraft/world/item/Item;",
            ordinal = 0
        )
    )
    private static <T extends LivingEntity & Bucketable> Item derelict$modifyItemInTryBucket(
        ItemStack instance,
        Operation<Item> original,
        Player playerEntity,
        InteractionHand hand,
        T entity
    ) {
        if (entity instanceof SpiderlingEntity.Enemy) {
            if (instance.is(Items.WATER_BUCKET)) return Items.AIR;
            if (instance.is(Items.BUCKET)) return Items.WATER_BUCKET;
        }
        return original.call(instance);
    }
}
