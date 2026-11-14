package dev.mim1q.derelict.util

import net.minecraft.world.phys.shapes.BooleanOp
import net.minecraft.core.Direction
import net.minecraft.world.phys.shapes.VoxelShape
import net.minecraft.world.phys.shapes.Shapes


object ShapeUtil {
    fun VoxelShape.rotate(from: Direction, to: Direction): VoxelShape {
        val buffer = arrayOf(this, Shapes.empty())
        val times: Int = (to.get2DDataValue() - from.get2DDataValue() + 4) % 4
        for (i in 0 until times) {
            buffer[0].forAllBoxes { minX: Double, minY: Double, minZ: Double, maxX: Double, maxY: Double, maxZ: Double ->
                buffer[1] = Shapes.joinUnoptimized(
                    buffer[1],
                    Shapes.box(1 - maxZ, minY, minX, 1 - minZ, maxY, maxX),
                    BooleanOp.OR
                )
            }
            buffer[0] = buffer[1]
            buffer[1] = Shapes.empty()
        }
        return buffer[0]
    }

    fun VoxelShape.rotate(to: Direction): VoxelShape = rotate(Direction.NORTH, to)
}