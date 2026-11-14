package dev.mim1q.derelict.world.feature

import dev.mim1q.derelict.block.cobweb.FancyCornerCobwebBlock.Companion.ROTATION
import dev.mim1q.derelict.block.cobweb.FancyCornerCobwebBlock.Companion.TYPE
import dev.mim1q.derelict.block.cobweb.FancyCornerCobwebBlock.Type.BOTTOM
import dev.mim1q.derelict.block.cobweb.FancyCornerCobwebBlock.Type.TOP
import dev.mim1q.derelict.init.ModBlocksAndItems
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.Vec3i
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration
import net.minecraft.world.level.levelgen.feature.Feature
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext
import kotlin.math.abs
import kotlin.math.sign

private const val SEARCH_RANGE = 32

private val POSSIBLE_DIRECTIONS = arrayOf(
    Vec3i(1, 0, 0) to 6,
    Vec3i(1, 0, 1) to 7,
    Vec3i(0, 0, 1) to 0,
    Vec3i(-1, 0, 1) to 1,
    Vec3i(-1, 0, 0) to 2,
    Vec3i(-1, 0, -1) to 3,
    Vec3i(0, 0, -1) to 4,
    Vec3i(1, 0, -1) to 5,
)

class SpiderSilkParabola : Feature<NoneFeatureConfiguration>(NoneFeatureConfiguration.CODEC) {
    override fun place(context: FeaturePlaceContext<NoneFeatureConfiguration>): Boolean {
        val direction = POSSIBLE_DIRECTIONS.random()
        val pos = context.origin().subtract(direction.first.multiply(16)).mutable()
        val world = context.level()

        for (i in 0..SEARCH_RANGE) {
            if (
                world.getBlockState(pos).isAir
                && world.getBlockState(pos.above()).isFaceSturdy(world, pos, Direction.DOWN)
            ) break
            if (i == SEARCH_RANGE) return false
            pos.move(0, 1, 0)
        }

        val heightArrays = arrayOf(
            arrayOf(-2, -2, -1, -1, 0, 1, 1, 1, 2, 2, 4),
            arrayOf(-2, -2, -1, 0, 0, 1, 1, 2, 2, 3),
            arrayOf(-2, -2, -1, -1, -1, 0, 0, 1, 1, 1, 1, 2, 2, 3)
        )

        val heights = heightArrays[context.random().nextInt(heightArrays.size)]

        val positions = arrayListOf<BlockPos>()

        outer@ for (i in 0..32) {
            if (i == 32) return false

            val height = heights.getOrElse(i) { heights.last() }
            for (unused in 0..<abs(height).coerceAtLeast(1)) {
                if (world.getBlockState(pos).isAir) {
                    positions.add(pos.immutable())
                } else {
                    break@outer
                }
                pos.move(0, sign(height.toDouble()).toInt(), 0)
            }
            pos.move(direction.first.x, 0, direction.first.z)
        }

        if (positions.size < 5) return false

        var lastPos = positions.first()
        val rot = direction.second
        val counterRot = (direction.second + 4) % 8
        val random = context.random()
        for (position in positions) {
            world.placeIfPossible(position, ModBlocksAndItems.SPIDER_SILK.defaultBlockState())
            if (position.y < lastPos.y) {
                world.placeIfPossible(lastPos.below(), getRandomCornerWeb(random).setValue(ROTATION, rot).setValue(TYPE, TOP))
                world.placeIfPossible(position.above(), getRandomCornerWeb(random).setValue(ROTATION, counterRot).setValue(TYPE, BOTTOM))
            } else if (position.y > lastPos.y) {
                world.placeIfPossible(position.below(), getRandomCornerWeb(random).setValue(ROTATION, counterRot).setValue(TYPE, TOP))
                world.placeIfPossible(lastPos.above(), getRandomCornerWeb(random).setValue(ROTATION, rot).setValue(TYPE, BOTTOM))
            }

            lastPos = position
        }

        return true
    }
}