package dev.mim1q.derelict.client.render.entity.boss.arachne

import dev.mim1q.derelict.Derelict
import dev.mim1q.derelict.client.render.entity.spider.createEyesFeatureRenderer
import dev.mim1q.derelict.entity.boss.ArachneEntity
import dev.mim1q.derelict.init.client.ModRender
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.entity.MobRenderer

class ArachneEntityRenderer(ctx: EntityRendererProvider.Context) :
    MobRenderer<ArachneEntity, ArachneEntityModel>(
        ctx,
        ArachneEntityModel(ctx.bakeLayer(ModRender.ARACHNE_LAYER)),
        1.5F
    ) {
    init {
        addLayer(createEyesFeatureRenderer(this, TEXTURE))
    }

    override fun getTextureLocation(entity: ArachneEntity) = TEXTURE

    override fun getFlipDegrees(entity: ArachneEntity): Float = 0f

    companion object {
        val TEXTURE = Derelict.id("textures/entity/boss/arachne.png")
    }
}