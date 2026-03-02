package miragefairy2024.mod.magicplant.contents.magicplants

import miragefairy2024.MirageFairy2024
import miragefairy2024.mod.tool.CarnivorousPlantDamageTypeCard
import miragefairy2024.util.EnJa
import miragefairy2024.util.createCuboidShape
import net.minecraft.core.BlockPos
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.level.block.state.properties.IntegerProperty

abstract class AbstractVeropedaCard<B : AbstractVeropedaBlock> : SimpleMagicPlantCard<B>() {
    override val classification = EnJa("Order Miragales, family Veropedaceae", "妖花目ヴェロペダ科")

    override val ageProperty: IntegerProperty = BlockStateProperties.AGE_3

    override val outlineShapes = listOf(
        createCuboidShape(3.0, 5.0),
        createCuboidShape(4.0, 7.0),
        createCuboidShape(7.0, 9.0),
        createCuboidShape(7.0, 16.0),
    )

    override val family = MirageFairy2024.identifier("veropeda")
}

abstract class AbstractVeropedaBlock(card: AbstractVeropedaCard<*>, settings: Properties) : SimpleMagicPlantBlock(card, settings) {
    override fun getAgeProperty(): IntegerProperty = BlockStateProperties.AGE_3

    @Suppress("OVERRIDE_DEPRECATION")
    override fun entityInside(state: BlockState, level: Level, pos: BlockPos, entity: Entity) {
        if (entity is LivingEntity) {
            entity.hurt(level.damageSources().source(CarnivorousPlantDamageTypeCard.registryKey), 1.0F)
        }
    }
}
