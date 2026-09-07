package miragefairy2024.mod.tree.contents.blockcards

import miragefairy2024.ModContext
import miragefairy2024.mod.tree.TreeBlockCard
import miragefairy2024.mod.tree.TreeBlockConfiguration
import miragefairy2024.util.Registration
import miragefairy2024.util.registerBlockFamily
import miragefairy2024.util.registerFlammable
import miragefairy2024.util.registerLootTableGeneration
import net.minecraft.data.models.model.TexturedModel
import net.minecraft.world.level.block.Block

class TreePlanksSlabBlockCard(configuration: TreeBlockConfiguration, private val baseBlock: () -> Registration<*, out Block>) : TreeBlockCard(configuration) {
    override fun createSettings() = createPlankSettings()

    context(ModContext)
    override fun init() {
        super.init()

        registerBlockFamily(TexturedModel.CUBE, baseBlock()) { it.slab(block()) }
        block.registerLootTableGeneration { it, _ -> it.createSlabItemTable(block()) }

        // 性質
        block.registerFlammable(5, 20)

    }
}
