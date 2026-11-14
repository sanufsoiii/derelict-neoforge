package dev.mim1q.derelict.world.feature

import dev.mim1q.derelict.block.cobweb.FancyCobwebBlock
import dev.mim1q.derelict.block.cobweb.FancyCornerCobwebBlock.Companion.ROTATION
import dev.mim1q.derelict.block.cobweb.FancyCornerCobwebBlock.Companion.TYPE
import dev.mim1q.derelict.block.cobweb.FancyCornerCobwebBlock.Type.BOTTOM
import dev.mim1q.derelict.block.cobweb.FancyCornerCobwebBlock.Type.TOP
import dev.mim1q.derelict.init.ModBlocksAndItems
import dev.mim1q.derelict.init.ModBlocksAndItems.CORNER_COBWEB
import dev.mim1q.derelict.init.ModBlocksAndItems.FANCY_CORNER_COBWEB
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.SupportType
import net.minecraft.Util
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.Vec3i
import net.minecraft.util.RandomSource
import net.minecraft.world.level.WorldGenLevel
import net.minecraft.world.level.LevelAccessor
import net.minecraft.world.level.chunk.ChunkGenerator
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration
import net.minecraft.world.level.levelgen.feature.Feature
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext
import kotlin.math.min
import kotlin.math.round

private const val MIN_PILLAR_HEIGHT = 12
private const val MAX_PILLAR_HEIGHT = 64
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

class SpiderSilkPillar : Feature<NoneFeatureConfiguration>(NoneFeatureConfiguration.CODEC) {
    override fun place(
        config: NoneFeatureConfiguration,
        world: WorldGenLevel,
        chunkGenerator: ChunkGenerator,
        random: RandomSource,
        origin: BlockPos
    ): Boolean {
        val topPos = origin.mutable()
        var found = false
        for (i in 0..<SEARCH_RANGE) {
            val aboveTopPos = topPos.above()
            if (world.getBlockState(topPos).isAir &&
                world.getBlockState(aboveTopPos).isFaceSturdy(world, aboveTopPos, Direction.DOWN, SupportType.FULL)
            ) {
                found = true
                break
            }
            topPos.setY(topPos.y + 1)
        }
        if (!found) return false

        val topOrigin = topPos.immutable()
        val randomDir = POSSIBLE_DIRECTIONS.random()
        val randomSlope = random.nextDouble() * 0.3

        return SilkPlacer().generate(
            world, random,
            { it, i ->
                it.set(
                    topOrigin.x + round(i * randomSlope * randomDir.first.x).toInt(),
                    topOrigin.y - i,
                    topOrigin.z + round(i * randomSlope * randomDir.first.z).toInt(),
                )
            },
            randomDir.second
        )
    }

    override fun place(context: FeaturePlaceContext<NoneFeatureConfiguration>): Boolean {
        return true
    }
}

private class SilkPlacer {
    val positionStack = Array<BlockPos.MutableBlockPos>(64) { BlockPos.ZERO.mutable() }

    fun generate(
        world: LevelAccessor,
        random: RandomSource,
        positionSetter: (BlockPos.MutableBlockPos, Int) -> Unit,
        rotation: Int
    ): Boolean {
        val height = setPositionsAndValidate(world, positionSetter) ?: return false

        var lastX = positionStack[0].x
        var lastZ = positionStack[0].z

        for (i in 0..<height) {
            val pos = positionStack[i]
            world.placeIfPossible(pos, ModBlocksAndItems.SPIDER_SILK.defaultBlockState())
            if (pos.x != lastX || pos.z != lastZ) {
                world.placeIfPossible(
                    pos.above(), getRandomCornerWeb(random)
                        .setValue(ROTATION, (rotation + 4) % 8)
                        .setValue(TYPE, BOTTOM)
                )
                world.placeIfPossible(
                    BlockPos(lastX, pos.y, lastZ), getRandomCornerWeb(random)
                        .setValue(ROTATION, rotation)
                        .setValue(TYPE, TOP)
                )
                lastX = pos.x
                lastZ = pos.z
            }
        }

        var chance = 0.8

        Util.shuffledCopy(POSSIBLE_DIRECTIONS, random).forEach {
            for (i in 3..min(8, height - 1)) {
                if (
                    random.nextDouble() < chance
                    && tryGenerateConnection(world, random, positionStack[i], true, it)
                ) {
                    chance -= 0.2
                    break
                }
            }
        }

        chance = 0.8

        Util.shuffledCopy(POSSIBLE_DIRECTIONS, random).forEach {
            for (i in 3..min(8, height - 1)) {
                if (
                    random.nextDouble() < chance
                    && tryGenerateConnection(world, random, positionStack[height - i], false, it)
                ) {
                    chance -= 0.2
                    break
                }
            }
        }

        return true
    }

    fun tryGenerateConnection(
        world: LevelAccessor,
        random: RandomSource,
        pos: BlockPos,
        up: Boolean,
        dir: Pair<Vec3i, Int>
    ): Boolean {
        val sideOffset = dir.first
        val offset = if (up) Vec3i(sideOffset.x * 4, 4, sideOffset.z * 4) else Vec3i(sideOffset.x * 4, -4, sideOffset.z * 4)
        val checkPos = pos.offset(offset)

        if (up) {
            if (
                world.getBlockState(checkPos).canBeReplaced()
                && world.getBlockState(checkPos.above()).isFaceSturdy(world, checkPos, Direction.DOWN)
            ) {
                generateConnection(world, random, checkPos.mutable(), sideOffset, dir.second, true)
                return true
            }
        } else {
            if (
                world.getBlockState(checkPos).canBeReplaced()
                && world.getBlockState(checkPos.below()).isFaceSturdy(world, checkPos, Direction.UP)
            ) {
                generateConnection(world, random, checkPos.mutable(), sideOffset, dir.second, false)
                return true
            }
        }

        return false
    }

    fun setPositionsAndValidate(world: LevelAccessor, positionSetter: (BlockPos.MutableBlockPos, Int) -> Unit): Int? {
        for (i in 0..<MAX_PILLAR_HEIGHT) {
            positionSetter(positionStack[i], i)
            val currentPos = positionStack[i]
            if (
                world.getBlockState(currentPos).canBeReplaced()
                && world.getBlockState(currentPos.below()).isFaceSturdy(world, currentPos, Direction.DOWN)
            ) {
                return if (i >= MIN_PILLAR_HEIGHT) {
                    i + 1
                } else null
            }
        }
        return null
    }

    private fun generateConnection(
        world: LevelAccessor,
        random: RandomSource,
        mutableOrigin: BlockPos.MutableBlockPos,
        sideOffset: Vec3i,
        rotation: Int,
        up: Boolean
    ) {
        val mainBlock = ModBlocksAndItems.SPIDER_SILK.defaultBlockState()
        val cornerBlock0 = {
            getRandomCornerWeb(random).setValue(ROTATION, rotation).setValue(TYPE, if (up) BOTTOM else TOP)
        }
        val cornerBlock1 = {
            getRandomCornerWeb(random).setValue(ROTATION, (rotation + 4) % 8).setValue(TYPE, if (up) TOP else BOTTOM)
        }
        val negativeOffset = sideOffset.multiply(-1)
        val verticalOffset = Vec3i(0, if (up) -1 else 1, 0)

        world.placeIfPossible(mutableOrigin, cornerBlock1())
        mutableOrigin.move(negativeOffset)
        world.placeIfPossible(mutableOrigin, mainBlock)
        mutableOrigin.move(negativeOffset)
        world.placeIfPossible(mutableOrigin, cornerBlock0())
        mutableOrigin.move(sideOffset)
        mutableOrigin.move(verticalOffset)

        world.placeIfPossible(mutableOrigin, cornerBlock1())
        mutableOrigin.move(negativeOffset)
        world.placeIfPossible(mutableOrigin, mainBlock)
        mutableOrigin.move(negativeOffset)
        world.placeIfPossible(mutableOrigin, cornerBlock0())
        mutableOrigin.move(sideOffset)
        mutableOrigin.move(verticalOffset)

        world.placeIfPossible(mutableOrigin, cornerBlock1())
        mutableOrigin.move(negativeOffset)
        world.placeIfPossible(mutableOrigin, mainBlock)
        mutableOrigin.move(verticalOffset)
        world.placeIfPossible(mutableOrigin, mainBlock)
        mutableOrigin.move(verticalOffset)
        world.placeIfPossible(mutableOrigin, cornerBlock1())
    }
}

fun LevelAccessor.placeIfPossible(pos: BlockPos, state: BlockState): Boolean {
    val currentState = this.getBlockState(pos)
    val block = state.block
    if (currentState.isAir || block == ModBlocksAndItems.SPIDER_SILK || block is FancyCobwebBlock) {
        this.setBlock(pos, state, Block.UPDATE_CLIENTS)
        return true
    }

    return false
}

fun getRandomCornerWeb(random: RandomSource): BlockState =
    if (random.nextDouble() < 0.02) FANCY_CORNER_COBWEB.defaultBlockState() else CORNER_COBWEB.defaultBlockState()