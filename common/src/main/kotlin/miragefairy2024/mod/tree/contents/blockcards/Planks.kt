package miragefairy2024.mod.tree.contents.blockcards

import miragefairy2024.ModContext
import miragefairy2024.mod.tree.TreeBlockCard
import miragefairy2024.mod.tree.TreeBlockConfiguration
import miragefairy2024.mod.tree.createBaseWoodSetting
import miragefairy2024.util.from
import miragefairy2024.util.on
import miragefairy2024.util.registerDefaultLootTableGeneration
import miragefairy2024.util.registerFlammable
import miragefairy2024.util.registerModelGeneration
import miragefairy2024.util.registerShapedRecipeGeneration
import miragefairy2024.util.registerShapelessRecipeGeneration
import miragefairy2024.util.registerSingletonBlockStateGeneration
import net.minecraft.data.models.model.TexturedModel
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.state.BlockBehaviour

fun TreeBlockCard.createPlankSettings(sound: Boolean = true): BlockBehaviour.Properties = createBaseWoodSetting(sound = sound)
    .strength(2.0F, 3.0F)
    .mapColor(configuration.tree.getPlankMapColor())

open class AbstractTreePlanksBlockCard(configuration: TreeBlockConfiguration) : TreeBlockCard(configuration) {
    override fun createSettings(): BlockBehaviour.Properties = createPlankSettings()

    context(ModContext)
    override fun init() {
        super.init()

        block.registerSingletonBlockStateGeneration()
        block.registerModelGeneration(TexturedModel.CUBE)
        block.registerDefaultLootTableGeneration()

        // 性質
        block.registerFlammable(5, 20)

    }
}

class TreePlanksBlockCard(configuration: TreeBlockConfiguration, private val input: () -> Item) : AbstractTreePlanksBlockCard(configuration) {
    context(ModContext)
    override fun init() {
        super.init()
        registerShapelessRecipeGeneration(item, 4) {
            requires(input())
        } on input from input
    }
}

class TreeBricksBlockCard(configuration: TreeBlockConfiguration, private val input: () -> Item) : AbstractTreePlanksBlockCard(configuration) {
    context(ModContext)
    override fun init() {
        super.init()
        registerShapedRecipeGeneration(item) {
            pattern("#")
            pattern("#")
            define('#', input())
        } on input from input
    }
}
