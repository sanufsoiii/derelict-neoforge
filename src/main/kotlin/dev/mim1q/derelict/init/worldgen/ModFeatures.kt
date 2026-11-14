package dev.mim1q.derelict.init.worldgen

import com.mojang.serialization.MapCodec
import dev.mim1q.derelict.Derelict
import dev.mim1q.derelict.world.feature.LocalGravityProcessor
import dev.mim1q.derelict.world.feature.NbtFeature
import dev.mim1q.derelict.world.feature.SpiderSilkParabola
import dev.mim1q.derelict.world.feature.SpiderSilkPillar
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.Registry
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType
import net.minecraft.world.level.levelgen.feature.Feature
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration

object ModFeatures {
    val LOCAL_GRAVITY_PROCESSOR = registerProcessor("local_gravity", LocalGravityProcessor.CODEC)

    fun init() {
        register("spider_silk_pillar", SpiderSilkPillar())
        register("spider_silk_parabola", SpiderSilkParabola())
        register("nbt_feature", NbtFeature())
    }

    private fun <C : FeatureConfiguration, F : Feature<C>> register(name: String, feature: F): F {
        return Registry.register(BuiltInRegistries.FEATURE, Derelict.id(name), feature)
    }

    private fun <P : StructureProcessor> registerProcessor(id: String, codec: MapCodec<P>): StructureProcessorType<P> =
        Registry.register(
            BuiltInRegistries.STRUCTURE_PROCESSOR,
            Derelict.id(id),
            StructureProcessorType { codec }
        )

}