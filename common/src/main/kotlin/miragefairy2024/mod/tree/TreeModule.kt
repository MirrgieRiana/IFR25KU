package miragefairy2024.mod.tree

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.tree.contents.PlasticTreeFamilySaplingBlock
import miragefairy2024.mod.tree.contents.haimeviska.initHaimeviska
import miragefairy2024.mod.tree.contents.plastictree.initPlasticTree
import miragefairy2024.util.Registration
import miragefairy2024.util.register
import net.minecraft.core.registries.BuiltInRegistries

context(ModContext)
fun initTreeModule() {

    initTreeBlocks()

    Registration(BuiltInRegistries.BLOCK_TYPE, MirageFairy2024.identifier("plastic_tree_family_sapling")) { PlasticTreeFamilySaplingBlock.CODEC }.register()

    initHaimeviska()
    initPlasticTree()

}
