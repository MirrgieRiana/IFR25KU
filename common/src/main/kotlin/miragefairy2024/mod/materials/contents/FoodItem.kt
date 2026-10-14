package miragefairy2024.mod.materials.contents

import miragefairy2024.util.blue
import miragefairy2024.util.invoke
import miragefairy2024.util.plus
import miragefairy2024.util.red
import miragefairy2024.util.text
import miragefairy2024.util.toRomanText
import mirrg.kotlin.hydrogen.formatAs
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.util.StringUtil
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect

open class FoodItem(settings: Properties) : Item(settings) {
    override fun appendHoverText(stack: ItemStack, context: TooltipContext, tooltipComponents: MutableList<Component>, tooltipFlag: TooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag)
        run {
            // 食べたときの効果は、満腹度のコンポーネントではなく、消費のコンポーネントの側が持つのだ～🌱
            val consumable = stack[DataComponents.CONSUMABLE] ?: return@run
            consumable.onConsumeEffects().filterIsInstance<ApplyStatusEffectsConsumeEffect>().forEach { consumeEffect ->
                consumeEffect.effects().forEach { effect ->
                    var text = effect.effect.value().displayName
                    if (effect.amplifier > 0) text = text { text + " "() + (effect.amplifier + 1).toRomanText() }
                    if (!effect.effect.value().isInstantenous) text = text { text + " (${StringUtil.formatTickDuration(effect.duration, context.tickRate())}"() + ")"() }
                    if (consumeEffect.probability() != 1.0F) text = text { text + " (${consumeEffect.probability() * 100 formatAs "%.0f"}%)"() }
                    text = if (effect.effect.value().isBeneficial) text.blue else text.red
                    tooltipComponents += text
                }
            }
        }
    }
}
