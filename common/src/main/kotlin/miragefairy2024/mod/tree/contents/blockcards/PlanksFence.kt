package miragefairy2024.mod.tree.contents.blockcards

import miragefairy2024.ModContext
import miragefairy2024.mod.tree.TreeBlockCard
import miragefairy2024.mod.tree.TreeBlockConfiguration
import miragefairy2024.util.registerBlockFamily
import miragefairy2024.util.registerDefaultLootTableGeneration
import miragefairy2024.util.registerFlammable
import net.minecraft.data.models.model.TexturedModel
import net.minecraft.world.level.block.Block

class TreePlanksFenceBlockCard(configuration: TreeBlockConfiguration, private val parent: () -> Block) : TreeBlockCard(configuration) {
    override fun createSettings() = createPlankSettings()

    context(ModContext)
    override fun init() {
        super.init()

        registerBlockFamily(TexturedModel.CUBE, parent) { it.fence(block()) }
        block.registerDefaultLootTableGeneration()

        // 性質
        block.registerFlammable(5, 20)

    }
}
