package dev.mim1q.derelict.vendor.gimm1q.mixin.client.item;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.mim1q.derelict.vendor.gimm1q.client.item.handheld.HandheldItemModelRegistryImpl;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.ItemModelShaper;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.ApiStatus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@ApiStatus.Internal
@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {
    @Shadow
    public abstract ItemModelShaper getItemModelShaper();

    @Shadow public abstract void render(ItemStack stack, ItemDisplayContext renderMode, boolean leftHanded, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay, BakedModel model);

    @Inject(
        method = "getModel(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;I)Lnet/minecraft/client/resources/model/BakedModel;",
        at = @At("HEAD"),
        cancellable = true
    )
    private void derelictGimm1q$getModel(ItemStack stack, Level world, LivingEntity entity, int seed, CallbackInfoReturnable<BakedModel> cir) {
        var model = HandheldItemModelRegistryImpl.MODELS.get(stack.getItem());
        if (model != null) {
            var bakedModel = getItemModelShaper().getModelManager().getModel(model.getA());
            if (bakedModel == null) return;
            var override = bakedModel.getOverrides().resolve(bakedModel, stack, (ClientLevel) world, entity, seed);
            cir.setReturnValue(override);
        }
    }

    @Inject(
        method = "renderStatic(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/level/Level;III)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void derelictGimm1q$render(
        LivingEntity entity,
        ItemStack item,
        ItemDisplayContext renderMode,
        boolean leftHanded,
        PoseStack matrices,
        MultiBufferSource vertexConsumers,
        Level world,
        int light,
        int overlay,
        int seed,
        CallbackInfo ci
    ) {
        if (item.isEmpty()) return;
        if (renderMode == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND
            || renderMode == ItemDisplayContext.FIRST_PERSON_LEFT_HAND
            || renderMode == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND
            || renderMode == ItemDisplayContext.THIRD_PERSON_LEFT_HAND
        ) {
            var model = HandheldItemModelRegistryImpl.MODELS.get(item.getItem());
            if (model != null) {
                var bakedModel = getItemModelShaper().getModelManager().getModel(model.getB());
                if (bakedModel == null) return;
                var override = bakedModel.getOverrides().resolve(bakedModel, item, (ClientLevel) world, entity, seed);

                this.render(item, renderMode, leftHanded, matrices, vertexConsumers, light, overlay, override);
                ci.cancel();
            }
        }
    }
}
