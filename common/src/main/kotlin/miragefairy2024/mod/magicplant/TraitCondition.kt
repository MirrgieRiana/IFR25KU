package miragefairy2024.mod.magicplant

import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.world.level.Level

// api

interface TraitCondition {
    val emoji: Component
    val name: Component
    fun getFactor(context: TraitConditionContext): Double
}

class TraitConditionContext(val level: Level, val blockPos: BlockPos, val blockEntity: MagicPlantBlockEntity?)
