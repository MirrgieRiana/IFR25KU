package miragefairy2024.mod.tree.contents.plastictreefamily

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.util.Registration
import miragefairy2024.util.register
import net.minecraft.core.registries.BuiltInRegistries

context(ModContext)
fun initPlasticTreeFamily() {

    // BlockType
    Registration(BuiltInRegistries.BLOCK_TYPE, MirageFairy2024.identifier("plastic_tree_family_sapling")) { PlasticTreeFamilySaplingBlock.CODEC }.register()


    // 木
    Registration(BuiltInRegistries.TREE_DECORATOR_TYPE, PlasticTreeFamilyTreeDecoratorCard.identifier) { PlasticTreeFamilyTreeDecoratorCard.type }.register()

}
