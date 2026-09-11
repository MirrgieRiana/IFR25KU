package miragefairy2024.mod.magicplant.contents.magicplants

import miragefairy2024.MirageFairy2024
import miragefairy2024.mod.magicplant.MagicPlantDropDrawer
import miragefairy2024.mod.materials.MaterialCard
import miragefairy2024.util.EnJa
import miragefairy2024.util.createCuboidShape
import miragefairy2024.util.createItemStack
import miragefairy2024.util.randomInt
import net.minecraft.util.RandomSource
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.level.block.state.properties.IntegerProperty

abstract class AbstractMirageFlowerCard<B : SimpleMagicPlantBlock> : SimpleMagicPlantCard<B>() {
    override val classification = EnJa("Order Miragales, family Miragaceae", "妖花目ミラージュ科")

    override val ageProperty: IntegerProperty = BlockStateProperties.AGE_3

    override val outlineShapes = listOf(
        createCuboidShape(3.0, 5.0),
        createCuboidShape(6.0, 12.0),
        createCuboidShape(6.0, 15.0),
        createCuboidShape(6.0, 16.0),
    )

    override val family = MirageFairy2024.identifier("mirage")
}

val mirageFlourDropDrawer: MagicPlantDropDrawer = object : MagicPlantDropDrawer {
    override val items = listOf(
        MaterialCard.MIRAGE_FLOUR.item,
        MaterialCard.MIRAGE_FLOUR_OF_NATURE.item,
        MaterialCard.MIRAGE_FLOUR_OF_EARTH.item,
        MaterialCard.MIRAGE_FLOUR_OF_UNDERWORLD.item,
        MaterialCard.MIRAGE_FLOUR_OF_SKY.item,
        MaterialCard.MIRAGE_FLOUR_OF_UNIVERSE.item,
        MaterialCard.MIRAGE_FLOUR_OF_TIME.item,
    )

    override fun draw(count: Int, random: RandomSource): List<ItemStack> {
        var count2 = count.toDouble()
        items.dropLast(1).forEach { item ->
            if (count2 < 3) return listOf(item().createItemStack(random.randomInt(count2)))
            count2 /= 9.0
        }
        val lastItem = items.last()
        return listOf(lastItem().createItemStack(random.randomInt(count2)))
    }
}
