package miragefairy2024.util

import net.minecraft.core.Holder
import net.minecraft.world.item.alchemy.Potion
import net.minecraft.world.item.alchemy.PotionContents

infix fun PotionContents.isIn(potion: Holder<Potion>) = this.`is`(potion)
infix fun PotionContents.isNotIn(potion: Holder<Potion>) = !(this isIn potion)
