package miragefairy2024.mod.tree.contents.plastictreefamily

import miragefairy2024.ModContext
import miragefairy2024.util.Registration
import miragefairy2024.util.register
import net.minecraft.core.registries.BuiltInRegistries

context(ModContext)
fun initPlasticTreeFamily() {

    // 木
    Registration(BuiltInRegistries.TREE_DECORATOR_TYPE, PlasticTreeFamilyTreeDecoratorCard.identifier) { PlasticTreeFamilyTreeDecoratorCard.type }.register()

}
