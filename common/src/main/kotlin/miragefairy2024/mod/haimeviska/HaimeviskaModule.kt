package miragefairy2024.mod.haimeviska

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.common.rootAdvancement
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
    icon = { HaimeviskaBlockCard.DRIPPING_LOG.item().createItemStack() },
    name = EnJa("What is it like to be a plant?", "植物として生きるとはどのようなことか"),
    description = EnJa("Explore the overworld to find Haimeviska the fairy tree", "地上を探検して精樹ハイメヴィスカを探す"),
    criterion = AdvancementCard.hasItem { HaimeviskaBlockCard.LOG.item() },
    type = AdvancementCardType.TOAST_AND_JEWELS,
)

context(ModContext)
fun initHaimeviskaModule() {

    initHaimeviskaBlocks()
    initHaimeviskaWorldGens()

    Registration(BuiltInRegistries.TRUNK_PLACER_TYPE, HaimeviskaTrunkPlacerCard.identifier) { HaimeviskaTrunkPlacerCard.type }.register()
    Registration(BuiltInRegistries.FOLIAGE_PLACER_TYPE, HaimeviskaFoliagePlacerCard.identifier) { HaimeviskaFoliagePlacerCard.type }.register()
    Registration(BuiltInRegistries.TREE_DECORATOR_TYPE, HaimeviskaTreeDecoratorCard.identifier) { HaimeviskaTreeDecoratorCard.type }.register()

    haimeviskaAdvancement.init()

}
