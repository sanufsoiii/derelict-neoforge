package dev.mim1q.derelict.item.weapon

import dev.mim1q.derelict.entity.projectile.SpiderSilkBolaProjectile
import dev.mim1q.derelict.init.ModEntities
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.ItemUtils
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundSource
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.item.UseAnim
import net.minecraft.world.level.Level

class SpiderSilkBola(settings: Properties) : Item(settings) {
    override fun releaseUsing(stack: ItemStack, world: Level, user: LivingEntity, remainingUseTicks: Int) {
        super.releaseUsing(stack, world, user, remainingUseTicks)

        val ticks = getUseDuration(stack, user) - remainingUseTicks
        if (ticks < 10) return

        if (!world.isClientSide) {
            val projectile = SpiderSilkBolaProjectile(ModEntities.SPIDER_SILK_BOLA, world)
            projectile.setPos(user.position().add(0.0, 1.5, 0.0))
            projectile.owner = user
            projectile.shootFromRotation(user, user.xRot, user.yRot, 0f, 1.5f, 0.1f)
            world.addFreshEntity(projectile)

            world.playSound(
                null,
                user.blockPosition(),
                SoundEvents.ARROW_SHOOT,
                SoundSource.PLAYERS,
                1f,
                0.6f + world.random.nextFloat() * 0.2f
            )
            user.getItemInHand(user.usedItemHand).shrink(1)
            (user as? ServerPlayer)?.cooldowns?.addCooldown(this, 20)
        }
    }

    override fun use(world: Level, user: Player, hand: InteractionHand): InteractionResultHolder<ItemStack> =
        ItemUtils.startUsingInstantly(world, user, hand)

    override fun getUseDuration(stack: ItemStack, user: LivingEntity): Int = 3600 * 20
    override fun getUseAnimation(stack: ItemStack): UseAnim = UseAnim.BOW
}