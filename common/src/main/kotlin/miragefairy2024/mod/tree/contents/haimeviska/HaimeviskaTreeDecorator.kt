package miragefairy2024.mod.tree.contents.haimeviska

import com.mojang.serialization.MapCodec
import miragefairy2024.MirageFairy2024
import miragefairy2024.mod.tree.TreeBlockCard
import miragefairy2024.mod.tree.contents.HaimeviskaTreeDecorator as HaimeviskaTreeDecorator2
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType

object HaimeviskaTreeDecoratorCard {
    val identifier = MirageFairy2024.identifier("haimeviska")
    private val codec: MapCodec<HaimeviskaTreeDecorator> = MapCodec.unit { HaimeviskaTreeDecorator }
    val type: TreeDecoratorType<HaimeviskaTreeDecorator> = TreeDecoratorType(codec)
}

object HaimeviskaTreeDecorator : HaimeviskaTreeDecorator2({ TreeBlockCard.LOG }, { TreeBlockCard.DRIPPING_LOG } to 12, { TreeBlockCard.HOLLOW_LOG } to 6) {
    override fun type() = HaimeviskaTreeDecoratorCard.type
}
