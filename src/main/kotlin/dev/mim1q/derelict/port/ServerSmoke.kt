package dev.mim1q.derelict.port

import dev.mim1q.derelict.Derelict
import dev.mim1q.derelict.init.ModEntities
import net.minecraft.server.MinecraftServer
import net.neoforged.neoforge.event.server.ServerStartedEvent
import net.neoforged.neoforge.event.tick.ServerTickEvent

/**
 * Dedicated-server smoke test, enabled only by {@code -Dderelict.port.serverSmoke=true}.
 *
 * Spawns one instance of every entity type registered by the mod at the world spawn, ticks the server
 * for 100 ticks and stops it. This catches client classes in common code, entities without attributes,
 * broken data packs, worldgen, loot tables and recipes.
 */
object ServerSmoke {
    private const val SMOKE_TICKS = 100

    private var active = false
    private var ticksLeft = 0
    private var spawned = 0

    fun onServerStarted(event: ServerStartedEvent) {
        if (!java.lang.Boolean.getBoolean("derelict.port.serverSmoke")) return
        val server = event.server
        active = true
        ticksLeft = SMOKE_TICKS
        server.execute {
            spawned = spawnAll(server)
            Derelict.LOGGER.info("[derelict-port] SERVER_SMOKE_SPAWNED entities={}", spawned)
        }
    }

    fun onServerTick(event: ServerTickEvent.Post) {
        if (!active) return
        if (ticksLeft-- > 0) return
        active = false
        Derelict.LOGGER.info("[derelict-port] SERVER_SMOKE_OK entities={}", spawned)
        event.server.halt(false)
    }

    private fun spawnAll(server: MinecraftServer): Int {
        val level = server.overworld()
        val spawn = level.sharedSpawnPos
        var count = 0
        ModEntities.ALL.forEachIndexed { index, type ->
            val entity = type.create(level)
            if (entity == null) {
                Derelict.LOGGER.warn("[derelict-port] SERVER_SMOKE could not create {}", type)
                return@forEachIndexed
            }
            val x = spawn.x + 2.0 + (index % 8) * 2.0
            val z = spawn.z + 2.0 + (index / 8) * 2.0
            entity.moveTo(x, spawn.y + 1.0, z, 0f, 0f)
            if (level.addFreshEntity(entity)) {
                count++
            } else {
                Derelict.LOGGER.warn("[derelict-port] SERVER_SMOKE could not add {}", type)
            }
        }
        return count
    }
}
