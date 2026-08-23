package miragefairy2024.mod.materials.contents

import miragefairy2024.MirageFairy2024
import miragefairy2024.util.Translation
import miragefairy2024.util.blue
import miragefairy2024.util.createItemStack
import miragefairy2024.util.invoke
import miragefairy2024.util.obtain
import miragefairy2024.util.plus
import miragefairy2024.util.red
import miragefairy2024.util.text
import miragefairy2024.util.toRomanText
import mirrg.kotlin.hydrogen.formatAs
import net.minecraft.advancements.CriteriaTriggers
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.stats.Stats
import net.minecraft.util.StringUtil
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.ItemUtils
import net.minecraft.world.item.Items
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.item.UseAnim
import net.minecraft.world.level.Level
import net.minecraft.world.level.gameevent.GameEvent

class DrinkItem(settings: Properties, private val flaming: Int? = null) : Item(settings) {
    companion object {
        val FLAMING_TRANSLATION = Translation({ "item.${MirageFairy2024.identifier("drink").toLanguageKey()}.burning" }, "Flaming", "炎上")
    }

    override fun appendHoverText(stack: ItemStack, context: TooltipContext, tooltipComponents: MutableList<Component>, tooltipFlag: TooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag)

        run {
            val foodComponent = stack[DataComponents.FOOD] ?: return@run
            foodComponent.effects.forEach { entry ->
                var text = entry.effect.effect.value().displayName
                if (entry.effect.amplifier > 0) text = text { text + " "() + (entry.effect.amplifier + 1).toRomanText() }
                if (!entry.effect.effect.value().isInstantenous) text = text { text + " (${StringUtil.formatTickDuration(entry.effect.duration, context.tickRate())}"() + ")"() }
                if (entry.probability != 1.0F) text = text { text + " (${entry.probability * 100 formatAs "%.0f"}%)"() }
                text = if (entry.effect.effect.value().isBeneficial) text.blue else text.red
                tooltipComponents += text
            }
        }

        if (flaming != null) tooltipComponents += text { (FLAMING_TRANSLATION() + " (${StringUtil.formatTickDuration(flaming * 20, context.tickRate())}"() + ")"()).red }
    }

    override fun finishUsingItem(stack: ItemStack, level: Level, user: LivingEntity): ItemStack {
        super.finishUsingItem(stack, level, user)
        if (user is ServerPlayer) CriteriaTriggers.CONSUME_ITEM.trigger(user, stack)
        if (user is Player) user.awardStat(Stats.ITEM_USED.get(this))
        user.gameEvent(GameEvent.DRINK)
        if (!level.isClientSide) {
            if (flaming != null) user.igniteForSeconds(flaming.toFloat())
        }
        return if (stack.isEmpty) {
            Items.GLASS_BOTTLE.createItemStack()
        } else {
            if (user !is Player || !user.abilities.instabuild) user.obtain(Items.GLASS_BOTTLE.createItemStack())
            stack
        }
    }

    override fun getUseDuration(stack: ItemStack, entity: LivingEntity) = 32
    override fun getUseAnimation(stack: ItemStack) = UseAnim.DRINK
    override fun use(level: Level, user: Player, hand: InteractionHand): InteractionResultHolder<ItemStack> = ItemUtils.startUsingInstantly(level, user, hand)
}
