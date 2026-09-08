package miragefairy2024.mod.tree.contents.plastictree

import com.mojang.serialization.MapCodec
import miragefairy2024.MirageFairy2024
import miragefairy2024.mod.tree.TreeBlockCard
import miragefairy2024.mod.tree.contents.LogReplacingTreeDecorator
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType

object PlasticTreeTreeDecoratorCard {
    val identifier = MirageFairy2024.identifier("plastic_tree")
    private val codec: MapCodec<PlasticTreeTreeDecorator> = MapCodec.unit { PlasticTreeTreeDecorator }
    val type: TreeDecoratorType<PlasticTreeTreeDecorator> = TreeDecoratorType(codec)
}

object PlasticTreeTreeDecorator : LogReplacingTreeDecorator({ TreeBlockCard.PLASTIC_TREE_LOG }, { TreeBlockCard.DRIPPING_PLASTIC_TREE_LOG } to 25, null) {
    override fun type() = PlasticTreeTreeDecoratorCard.type
}
