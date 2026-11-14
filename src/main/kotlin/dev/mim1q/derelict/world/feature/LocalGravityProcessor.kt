package dev.mim1q.derelict.world.feature

import com.mojang.serialization.MapCodec
import dev.mim1q.derelict.init.worldgen.ModFeatures
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType
import net.minecraft.core.BlockPos
import net.minecraft.world.level.LevelReader

class LocalGravityProcessor : StructureProcessor() {
    companion object {
        val CODEC: MapCodec<LocalGravityProcessor> = MapCodec.unit(LocalGravityProcessor())
    }

    override fun processBlock(
        world: LevelReader,
        pos: BlockPos,
        pivot: BlockPos,
        originalBlockInfo: StructureTemplate.StructureBlockInfo,
        currentBlockInfo: StructureTemplate.StructureBlockInfo,
        data: StructurePlaceSettings,
    ): StructureTemplate.StructureBlockInfo {
        val mutPos = currentBlockInfo.pos.mutable()
        val diff = mutPos.y - pos.y

        for (i in 0..(10 + diff)) {
            if (i == 10) return originalBlockInfo

            if (world.getBlockState(mutPos).isAir) {
                mutPos.move(0, -1, 0)
            } else {
                mutPos.move(0, 1, 0)
                break
            }
        }

        return StructureTemplate.StructureBlockInfo(
            mutPos.offset(0, diff, 0),
            originalBlockInfo.state,
            originalBlockInfo.nbt
        )
    }

    override fun getType(): StructureProcessorType<*> = ModFeatures.LOCAL_GRAVITY_PROCESSOR
}