package dev.mim1q.derelict.world.feature

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import dev.mim1q.derelict.util.pickWeightedRandom
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate
import net.minecraft.world.level.block.Rotation
import net.minecraft.resources.ResourceLocation
import net.minecraft.core.Direction
import net.minecraft.world.level.levelgen.feature.Feature
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext
import kotlin.jvm.optionals.getOrNull

class NbtFeature :
    Feature<NbtFeature.NbtFeatureConfig>(NbtFeatureConfig.CODEC) {
    override fun place(context: FeaturePlaceContext<NbtFeatureConfig>): Boolean {
        val manager = context.level().getLevel().structureManager
        val chosenTemplateId = context.config().structures.pickWeightedRandom(context.random())
        val template = manager.get(chosenTemplateId).getOrNull() ?: return false
        val rotation = Rotation.getRandom(context.random())

        val blocks = StructureTemplate.processBlockInfos(
            context.level(),
            context.origin(),
            context.origin(),
            StructurePlaceSettings().setRotation(rotation),
            arrayListOf<StructureTemplate.StructureBlockInfo>().apply {
                template.palettes.forEach { addAll(it.blocks()) }
            }
        )

        val yDeclines = hashMapOf<Pair<Int, Int>, Int?>()

        blocks.forEach {
            val yDecline = yDeclines.getOrPut(Pair(it.pos.x, it.pos.z)) {
                val mutPos = it.pos.mutable()
                for (i in 0..10) {
                    if (context.level().getBlockState(mutPos).isAir) {
                        mutPos.move(0, -1, 0)
                    } else {
                        val state = context.level().getBlockState(mutPos)
                        @Suppress("DEPRECATION")
                        if (state.isSolid && state.isFaceSturdy(context.level(), mutPos, Direction.UP)) {
                            return@getOrPut i - 1
                        }
                    }
                }
                return@getOrPut null
            }

            if (yDecline == null) return@forEach

            context.level().placeIfPossible(
                it.pos.below(yDecline), it.state.rotate(rotation)
            )
        }

        return true
    }

    data class NbtFeatureConfig(
        val structures: Map<ResourceLocation, Int>,
    ) : FeatureConfiguration {
        companion object {
            val CODEC: Codec<NbtFeatureConfig> =
                RecordCodecBuilder.create { instance: RecordCodecBuilder.Instance<NbtFeatureConfig> ->
                    instance.group(
                        Codec.unboundedMap(ResourceLocation.CODEC, Codec.INT)
                            .fieldOf("structures")
                            .forGetter(NbtFeatureConfig::structures)
                    ).apply(instance, ::NbtFeatureConfig)
                }
        }
    }
}