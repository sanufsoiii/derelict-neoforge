package dev.mim1q.derelict.util

import net.minecraft.util.StringRepresentable

@JvmInline
value class StringWrapper(val value: String) : StringRepresentable {
    override fun getSerializedName() = value
}