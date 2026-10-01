package miragefairy2024.mod.tree

import miragefairy2024.ModContext
import miragefairy2024.mod.tree.contents.haimeviska.initHaimeviska
import miragefairy2024.mod.tree.contents.plastictree.initPlasticTree
import miragefairy2024.mod.tree.contents.plastictreefamily.initPlasticTreeFamily

context(ModContext)
fun initTreeModule() {

    initTreeBlocks()

    initPlasticTreeFamily()
    initHaimeviska()
    initPlasticTree()

}
