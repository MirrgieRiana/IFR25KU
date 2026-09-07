package miragefairy2024.mod.tree.contents.plastictree

import com.mojang.serialization.MapCodec
import miragefairy2024.MirageFairy2024
import miragefairy2024.mod.tree.TreeBlockCard
import miragefairy2024.mod.tree.contents.TreeLogDecorator
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType

object PlasticTreeTreeDecoratorCard {
    val identifier = MirageFairy2024.identifier("plastic_tree")
    private val codec: MapCodec<TreeLogDecorator> = MapCodec.unit { PlasticTreeTreeDecorator }
    val type: TreeDecoratorType<TreeLogDecorator> = TreeDecoratorType(codec)
}

object PlasticTreeTreeDecorator : TreeLogDecorator({ TreeBlockCard.PLASTIC_TREE_LOG }, { TreeBlockCard.DRIPPING_PLASTIC_TREE_LOG }, 25, null, 0) {
    override fun type() = PlasticTreeTreeDecoratorCard.type
}
