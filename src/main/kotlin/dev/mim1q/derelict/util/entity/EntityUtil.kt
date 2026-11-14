package dev.mim1q.derelict.util.entity

import net.minecraft.world.entity.Entity
import net.minecraft.network.syncher.EntityDataAccessor
import java.util.*
import kotlin.enums.EnumEntries

class TrackedDataDelegate<T>(private val data: EntityDataAccessor<T>) {
    operator fun getValue(entity: Entity, property: Any): T = entity.getEntityData().get(data)
    operator fun setValue(entity: Entity, property: Any, value: T) = entity.getEntityData().set(data, value)
}

class OptionalTrackedDataDelegate<T : Any>(private val data: EntityDataAccessor<Optional<T>>) {
    operator fun getValue(entity: Entity, property: Any): T? = entity.getEntityData().get(data).orElse(null)
    operator fun setValue(entity: Entity, property: Any, value: T?) = entity.getEntityData().set(data, Optional.ofNullable(value))
}

class TrackedDataIntAsEnumDelegate<T : Enum<T>>(private val data: EntityDataAccessor<Int>, private val values: Array<T>) {
    operator fun getValue(entity: Entity, property: Any): T = values.getOrElse(entity.getEntityData().get(data)) { values[0] }
    operator fun setValue(entity: Entity, property: Any, value: T) = entity.getEntityData().set(data, values.indexOf(value))
}

fun <T> tracked(data: EntityDataAccessor<T>): TrackedDataDelegate<T> = TrackedDataDelegate(data)
fun <T : Any> nullableTracked(data: EntityDataAccessor<Optional<T>>): OptionalTrackedDataDelegate<T> =
    OptionalTrackedDataDelegate(data)

inline fun <reified T : Enum<T>> trackedEnum(data: EntityDataAccessor<Int>, values: EnumEntries<T>): TrackedDataIntAsEnumDelegate<T> =
    TrackedDataIntAsEnumDelegate(data, values.toTypedArray())