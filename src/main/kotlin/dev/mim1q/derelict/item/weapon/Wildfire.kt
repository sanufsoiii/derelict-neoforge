package dev.mim1q.derelict.item.weapon

import dev.mim1q.derelict.util.extensions.radians
import dev.mim1q.derelict.vendor.gimm1q.screenshake.ScreenShakeUtils
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.ItemUtils
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.item.UseAnim
import net.minecraft.world.phys.Vec3
import net.minecraft.world.level.Level
import kotlin.math.min

class Wildfire(settings: Properties) : Item(settings) {
    override fun onUseTick(world: Level, user: LivingEntity, stack: ItemStack, remainingUseTicks: Int) {
        if (user !is Player) return

        val shootFlame = user.getTicksUsingItem() >= 20
        if (shootFlame) {
            user.push(user.lookAngle.multiply(-1.0, 0.0, -1.0).scale(0.03))
            repeat(16) {
                val dir = user.getViewVector(1.0f).scale(1.1).offsetRandom(user.getRandom(), 0.1f)
                val sideDir = Vec3(0.0, 0.0, 1.0).yRot((-user.yBodyRot - 90).radians())
                val particle = when (user.getRandom().nextIntBetweenInclusive(0, 5)) {
                    1 -> ParticleTypes.FLAME
                    2 -> ParticleTypes.SMOKE
                    else -> ParticleTypes.SMALL_FLAME
                }
                val rngVec = Vec3.ZERO.offsetRandom(user.getRandom(), 0.2f)
                world.addParticle(
                    particle,
                    user.x + sideDir.x * 0.4 + dir.x * 0.3 + rngVec.x,
                    user.y + 1.25 + rngVec.y,
                    user.z + sideDir.z * 0.4 + dir.z * 0.3 + rngVec.z,
                    dir.x,
                    dir.y,
                    dir.z
                )
            }
        }

        if (world !is ServerLevel) return

        val shakeStrength = min(1f, user.ticksUsingItem * 0.05f)
        ScreenShakeUtils.shakeAround(
            world,
            user.position(),
            shakeStrength,
            40,
            5.0,
            32.0,
            "derelict_wildfire"
        )
    }

    override fun getUseAnimation(stack: ItemStack) = UseAnim.NONE

    override fun getUseDuration(stack: ItemStack, user: LivingEntity) = 3600 * 20

    override fun use(world: Level?, user: Player?, hand: InteractionHand?): InteractionResultHolder<ItemStack> =
        ItemUtils.startUsingInstantly(world, user, hand)
}
