package dev.mim1q.derelict.init.worldgen

import com.mojang.datafixers.util.Pair
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import dev.mim1q.derelict.Derelict
import dev.mim1q.derelict.init.ModBlocksAndItems
import dev.mim1q.derelict.init.ModEntities
import net.minecraft.core.Holder
import net.minecraft.core.HolderSet
import net.minecraft.core.Registry
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.MobCategory
import net.minecraft.world.level.biome.Biome
import net.minecraft.world.level.biome.Biomes
import net.minecraft.world.level.biome.Climate
import net.minecraft.world.level.biome.Climate.Parameter
import net.minecraft.world.level.biome.Climate.ParameterPoint
import net.minecraft.world.level.biome.MobSpawnSettings
import net.minecraft.world.level.levelgen.SurfaceRules
import net.minecraft.world.level.levelgen.SurfaceRules.ifTrue
import net.minecraft.world.level.levelgen.SurfaceRules.state
import net.neoforged.neoforge.common.world.BiomeModifier
import net.neoforged.neoforge.common.world.ModifiableBiomeInfo
import terrablender.api.ParameterUtils.*
import terrablender.api.Region
import terrablender.api.RegionType
import terrablender.api.Regions
import terrablender.api.SurfaceRuleManager
import java.util.function.Consumer

object ModBiomes {
    private val SPIDER_CAVES = biomeKey("spider_caves")
    private val DERELICT_REGION = object : Region(Derelict.id("derelict"), RegionType.OVERWORLD, 1) {
        override fun addBiomes(
            registry: Registry<Biome>,
            mapper: Consumer<Pair<ParameterPoint, ResourceKey<Biome>>>
        ) {
            val spiderCavesPoints = ParameterPointListBuilder()
                .temperature(Temperature.FULL_RANGE)
                .humidity(Humidity.WET, Humidity.HUMID, Humidity.ARID)
                .continentalness(Continentalness.FULL_RANGE)
                .erosion(Erosion.FULL_RANGE)
                .depth(Depth.UNDERGROUND)
                .weirdness(Weirdness.FULL_RANGE)
                .offset(0.38f)
                .build()

            addModifiedVanillaOverworldBiomes(mapper) {
                spiderCavesPoints.forEach { addBiome(mapper, it, SPIDER_CAVES) }
            }
        }
    }

    /** Called from FMLCommonSetupEvent (TerraBlender on NeoForge requires this phase). */
    fun init() {
        Regions.register(DERELICT_REGION)

        SurfaceRuleManager.addSurfaceRules(
            SurfaceRuleManager.RuleCategory.OVERWORLD,
            Derelict.MOD_ID,
            ifTrue(
                SurfaceRules.isBiome(SPIDER_CAVES),
                state(ModBlocksAndItems.ARACHNITE.block.defaultBlockState())
            )
        )
    }

    private fun noiseParameters(
        temperature: ClosedFloatingPointRange<Float> = -1f..1f,
        humidity: ClosedFloatingPointRange<Float> = -1f..1f,
        continentalness: ClosedFloatingPointRange<Float> = -1f..1f,
        erosion: ClosedFloatingPointRange<Float> = -1f..1f,
        depth: ClosedFloatingPointRange<Float> = -1f..1f,
        weirdness: ClosedFloatingPointRange<Float> = -1f..1f,
        offset: Float = 0f
    ) = ParameterPoint(
        Parameter.span(temperature.start, temperature.endInclusive),
        Parameter.span(humidity.start, humidity.endInclusive),
        Parameter.span(continentalness.start, continentalness.endInclusive),
        Parameter.span(erosion.start, erosion.endInclusive),
        Parameter.span(depth.start, depth.endInclusive),
        Parameter.span(weirdness.start, weirdness.endInclusive),
        Climate.quantizeCoord(offset)
    )

    private fun biomeKey(name: String): ResourceKey<Biome> = ResourceKey.create(Registries.BIOME, Derelict.id(name))
}

/**
 * Fabric: BiomeModifications selector "a vanilla spider is already in the MONSTER spawn list".
 * NeoForge biome modifiers cannot express that as a biome tag, so this modifier checks the spawn list itself.
 * Applied by data/derelict/neoforge/biome_modifier/add_jumping_spiders_*.json.
 */
class AddJumpingSpidersBiomeModifier(private val biomes: HolderSet<Biome>) : BiomeModifier {
    override fun modify(
        biome: Holder<Biome>,
        phase: BiomeModifier.Phase,
        builder: ModifiableBiomeInfo.BiomeInfo.Builder
    ) {
        if (phase != BiomeModifier.Phase.ADD || !biomes.contains(biome)) return
        val hasVanillaSpider = builder.mobSpawnSettings.getSpawner(MobCategory.MONSTER)
            .any { it.type == EntityType.SPIDER }
        if (hasVanillaSpider) {
            builder.mobSpawnSettings.addSpawn(
                MobCategory.MONSTER,
                MobSpawnSettings.SpawnerData(ModEntities.JUMPING_SPIDER, 30, 1, 2)
            )
        }
    }

    override fun codec(): MapCodec<out BiomeModifier> = CODEC

    companion object {
        val CODEC: MapCodec<AddJumpingSpidersBiomeModifier> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                Biome.LIST_CODEC.fieldOf("biomes").forGetter(AddJumpingSpidersBiomeModifier::biomes)
            ).apply(instance) { biomes -> AddJumpingSpidersBiomeModifier(biomes) }
        }
    }
}
