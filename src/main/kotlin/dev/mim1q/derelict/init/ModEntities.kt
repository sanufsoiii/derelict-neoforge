package dev.mim1q.derelict.init

import dev.mim1q.derelict.Derelict
import dev.mim1q.derelict.entity.SpiderlingEntity
import dev.mim1q.derelict.entity.boss.ArachneEntity
import dev.mim1q.derelict.entity.projectile.SpiderEggProjectile
import dev.mim1q.derelict.entity.projectile.SpiderSilkBolaProjectile
import dev.mim1q.derelict.entity.spider.*
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.MobCategory
import net.minecraft.world.entity.ai.attributes.AttributeSupplier
import net.minecraft.world.entity.monster.Spider
import net.minecraft.world.entity.monster.Zombie
import net.minecraft.world.entity.Mob
import net.minecraft.world.entity.SpawnPlacementTypes
import net.minecraft.world.level.levelgen.Heightmap
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent

object ModEntities {
    private val ATTRIBUTES = mutableMapOf<EntityType<out LivingEntity>, AttributeSupplier.Builder>()

    val ARACHNE = registerLiving("arachne", ::ArachneEntity, 2.5f, 1.9f, ArachneEntity.createArachneAttributes())
    val SPIDERLING = registerLiving("spiderling", SpiderlingEntity::Enemy, 0.7f, 0.7f, SpiderlingEntity.createSpiderlingAttributes())
    val SPIDERLING_ALLY =
        registerLiving("spiderling_ally", SpiderlingEntity::Ally, 0.7f, 0.7f, SpiderlingEntity.createSpiderlingAllyAttributes())
    val CHARMING_SPIDER =
        registerLiving("charming_spider", ::CharmingSpiderEntity, 0.8f, 0.8f, CharmingSpiderEntity.createCharmingSpiderAttributes())
    val WEB_CASTER = registerLiving("web_caster", ::WebCasterEntity, 1.9f, 1.2f, WebCasterEntity.createWebCasterAttributes())
    val DADDY_LONG_LEGS = registerLiving("daddy_long_legs", ::DaddyLongLegsEntity, 0.9f, 3.6f, Spider.createAttributes())
    val JUMPING_SPIDER = registerLiving("jumping_spider", ::JumpingSpiderEntity, 1.6f, 0.9f, Spider.createAttributes())
    val SPINY_SPIDER = registerLiving("spiny_spider", ::SpinySpiderEntity, 0.8f, 0.8f, Spider.createAttributes())

    val ARACHNE_EGG = register("arachne_egg", ::ArachneEggEntity, 1.5f, 2.25f)
    val SPIDER_SILK_BOLA = register("spider_silk_bola", ::SpiderSilkBolaProjectile, 1.0f, 1.0f)
    val SPIDER_EGG_PROJECTILE = register("spider_egg_projectile", ::SpiderEggProjectile, 0.5f, 0.5f)

    val ALL: List<EntityType<out Entity>> = listOf(
        ARACHNE, SPIDERLING, SPIDERLING_ALLY, CHARMING_SPIDER, WEB_CASTER,
        DADDY_LONG_LEGS, JUMPING_SPIDER, SPINY_SPIDER, ARACHNE_EGG,
        SPIDER_SILK_BOLA, SPIDER_EGG_PROJECTILE
    )

    fun init() {}

    fun registerAttributes(event: EntityAttributeCreationEvent) {
        ATTRIBUTES.forEach { (type, attributes) ->
            @Suppress("UNCHECKED_CAST")
            event.put(type as EntityType<out LivingEntity>, attributes.build())
        }
    }

    val SPAWN_ON_GROUND = setOf(
        SPIDERLING, CHARMING_SPIDER, WEB_CASTER, DADDY_LONG_LEGS, JUMPING_SPIDER, SPINY_SPIDER
    )

    /**
     * NeoForge requires an explicit spawn placement for every entity that appears in a biome spawn list.
     * The Fabric code defined SPAWN_ON_GROUND but never used it; registering it here keeps the vanilla
     * monster spawn rules for the biome modifiers.
     */
    fun registerSpawnPlacements(event: RegisterSpawnPlacementsEvent) {
        SPAWN_ON_GROUND.forEach { type ->
            @Suppress("UNCHECKED_CAST")
            val mobType = type as EntityType<Mob>
            event.register(
                mobType,
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Mob::checkMobSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.REPLACE
            )
        }
    }

    private fun <E : Entity> register(
        name: String,
        entityFactory: EntityType.EntityFactory<E>,
        width: Float,
        height: Float,
        spawnGroup: MobCategory = MobCategory.MISC
    ): EntityType<E> = Registry.register(
        BuiltInRegistries.ENTITY_TYPE,
        Derelict.id(name),
        EntityType.Builder.of(entityFactory, spawnGroup)
            .sized(width, height)
            .clientTrackingRange(5)
            .updateInterval(3)
            .build(Derelict.id(name).toString())
    )

    private fun <E : LivingEntity> registerLiving(
        name: String,
        entityFactory: EntityType.EntityFactory<E>,
        width: Float,
        height: Float,
        attributes: AttributeSupplier.Builder = Zombie.createMonsterAttributes(),
        spawnGroup: MobCategory = MobCategory.MONSTER
    ): EntityType<E> = register(name, entityFactory, width, height, spawnGroup).also {
        ATTRIBUTES[it] = attributes
    }
}
