package miragefairy2024.util

import net.minecraft.tags.TagKey
import net.minecraft.world.level.material.Fluid
import net.minecraft.world.level.material.FluidState

infix fun FluidState.isIn(fluid: Fluid) = this.`is`(fluid)
infix fun FluidState.isNotIn(fluid: Fluid) = !(this isIn fluid)
infix fun FluidState.isIn(tag: TagKey<Fluid>) = this.`is`(tag)
infix fun FluidState.isNotIn(tag: TagKey<Fluid>) = !(this isIn tag)
