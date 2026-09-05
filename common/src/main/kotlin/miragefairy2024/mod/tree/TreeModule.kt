package miragefairy2024.mod.tree

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.common.rootAdvancement
import miragefairy2024.mod.tree.contents.haimeviska.GiantHaimeviskaFoliagePlacerCard
import miragefairy2024.mod.tree.contents.haimeviska.GiantHaimeviskaTrunkPlacerCard
import miragefairy2024.mod.tree.contents.haimeviska.HaimeviskaTreeDecoratorCard
import miragefairy2024.mod.tree.contents.haimeviska.SmallHaimeviskaFoliagePlacerCard
import miragefairy2024.mod.tree.contents.haimeviska.SmallHaimeviskaTrunkPlacerCard
import miragefairy2024.mod.tree.contents.haimeviska.initHaimeviska
import miragefairy2024.util.AdvancementCard
import miragefairy2024.util.AdvancementCardType
import miragefairy2024.util.EnJa
import miragefairy2024.util.Registration
import miragefairy2024.util.createItemStack
import miragefairy2024.util.register
import net.minecraft.core.registries.BuiltInRegistries

val haimeviskaAdvancement = AdvancementCard(
    identifier = MirageFairy2024.identifier("haimeviska"),
    context = AdvancementCard.Sub { rootAdvancement.await() },
    icon = { TreeBlockCard.DRIPPING_LOG.item().createItemStack() },
    name = EnJa("What is it like to be a plant?", "植物として生きるとはどのようなことか"),
    description = EnJa("Explore the overworld to find Haimeviska the fairy tree", "地上を探検して精樹ハイメヴィスカを探す"),
    criterion = AdvancementCard.hasItem { TreeBlockCard.LOG.item() },
    type = AdvancementCardType.TOAST_AND_JEWELS,
)

context(ModContext)
fun initTreeModule() {

    initTreeBlocks()
    initTreeWorldGens()

    initHaimeviska()

    Registration(BuiltInRegistries.TRUNK_PLACER_TYPE, GiantHaimeviskaTrunkPlacerCard.identifier) { GiantHaimeviskaTrunkPlacerCard.type }.register()
    Registration(BuiltInRegistries.TRUNK_PLACER_TYPE, SmallHaimeviskaTrunkPlacerCard.identifier) { SmallHaimeviskaTrunkPlacerCard.type }.register()
    Registration(BuiltInRegistries.FOLIAGE_PLACER_TYPE, GiantHaimeviskaFoliagePlacerCard.identifier) { GiantHaimeviskaFoliagePlacerCard.type }.register()
    Registration(BuiltInRegistries.FOLIAGE_PLACER_TYPE, SmallHaimeviskaFoliagePlacerCard.identifier) { SmallHaimeviskaFoliagePlacerCard.type }.register()
    Registration(BuiltInRegistries.TREE_DECORATOR_TYPE, HaimeviskaTreeDecoratorCard.identifier) { HaimeviskaTreeDecoratorCard.type }.register()

    haimeviskaAdvancement.init()

}
