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

open class FoodItem(settings: Properties) : Item(settings) {
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
    }
}
