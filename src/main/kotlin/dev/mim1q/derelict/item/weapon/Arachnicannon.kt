package dev.mim1q.derelict.item.weapon

import dev.mim1q.derelict.block.cobweb.SpiderEggBlock
import dev.mim1q.derelict.entity.projectile.SpiderEggProjectile
import dev.mim1q.derelict.init.ModEntities
import dev.mim1q.derelict.util.extensions.getNbt
import dev.mim1q.derelict.util.extensions.updateNbt
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.ItemUtils
import net.minecraft.world.item.context.UseOnContext
import net.minecraft.world.item.TooltipFlag
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundSource
import net.minecraft.sounds.SoundEvents
import net.minecraft.network.chat.Component
import net.minecraft.ChatFormatting
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.item.UseAnim
import net.minecraft.world.level.Level

const val MAX_EGGS = 20
const val MAX_USE_TIME = 60 * 60 * 20

class Arachnicannon(settings: Properties) : Item(settings) {
    override fun releaseUsing(stack: ItemStack, world: Level, user: LivingEntity, remainingUseTicks: Int) {
        super.releaseUsing(stack, world, user, remainingUseTicks)

        val time = MAX_USE_TIME - remainingUseTicks

        if (!world.isClientSide && time > 10) {
            val count = stack.getLoadedEggs()
            val amount = count.coerceAtMost(5)
            if (amount == 0) return

            stack.setLoadedEggs(count - amount)
            stack.hurtAndBreak(1, world as ServerLevel, user as ServerPlayer) {
                user.onEquippedItemBroken(
                    this,
                    if (user.usedItemHand == InteractionHand.MAIN_HAND) EquipmentSlot.MAINHAND else EquipmentSlot.OFFHAND
                )
            }

            world.playSound(
                null,
                user.blockPosition(),
                SoundEvents.SPLASH_POTION_THROW,
                SoundSource.PLAYERS,
                1f,
                0.8f
            )

            repeat(amount) {
                val projectile = SpiderEggProjectile(ModEntities.SPIDER_EGG_PROJECTILE, world)
                projectile.setPos(user.position().add(0.0, 1.5, 0.0))
                projectile.owner = user
                projectile.shootFromRotation(user, user.xRot, user.yRot, 0f, 2.3f + user.getRandom().nextFloat() * 2f, 5f)
                world.addFreshEntity(projectile)
            }

            (user as? ServerPlayer)?.cooldowns?.addCooldown(this, 200)
        }
    }

    override fun use(world: Level, user: Player, hand: InteractionHand): InteractionResultHolder<ItemStack> =
        user.getItemInHand(hand).let {
            if (it.getLoadedEggs() == 0) {
                InteractionResultHolder.fail(it)
            } else {
                ItemUtils.startUsingInstantly(world, user, hand)
            }
        }

    override fun useOn(context: UseOnContext): InteractionResult {
        val state = context.level.getBlockState(context.clickedPos)
        if (state.block is SpiderEggBlock && context.itemInHand.getLoadedEggs() < MAX_EGGS) {
            context.level.destroyBlock(context.clickedPos, false)
            context.itemInHand.setLoadedEggs(context.itemInHand.getLoadedEggs() + 1)
            context.level.playSound(
                null,
                context.clickedPos,
                SoundEvents.ITEM_PICKUP,
                SoundSource.BLOCKS,
                1.0f,
                1.0f
            )
            return InteractionResult.SUCCESS
        }
        return super.useOn(context)
    }

    override fun getUseDuration(stack: ItemStack, user: LivingEntity): Int = MAX_USE_TIME
    override fun getUseAnimation(stack: ItemStack): UseAnim = UseAnim.BOW

    private fun ItemStack.getLoadedEggs(): Int = getNbt().getInt("eggs") ?: 0

    private fun ItemStack.setLoadedEggs(eggs: Int) = updateNbt {
        putInt("eggs", eggs)
    }


    override fun appendHoverText(
        stack: ItemStack,
        context: TooltipContext,
        tooltip: MutableList<Component>,
        type: TooltipFlag,
    ) {
        super.appendHoverText(stack, context, tooltip, type)
        val loaded = stack.getLoadedEggs()
        if (loaded == 0) {
            tooltip.add(
                Component.translatable("item.derelict.arachnicannon.tooltip.no_eggs")
                    .withStyle(ChatFormatting.GRAY)
            )
        }
        tooltip.add(
            Component.translatable("item.derelict.arachnicannon.tooltip.eggs", loaded, MAX_EGGS)
                .withStyle(ChatFormatting.GRAY)
        )
    }
}