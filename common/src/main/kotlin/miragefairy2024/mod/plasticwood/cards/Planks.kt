package miragefairy2024.mod.plasticwood.cards

import miragefairy2024.ModContext
import miragefairy2024.mod.plasticwood.PlasticWoodBlockCard
import miragefairy2024.mod.plasticwood.PlasticWoodBlockConfiguration
import miragefairy2024.mod.plasticwood.createPlasticTreeBaseWoodSetting
import miragefairy2024.util.from
import miragefairy2024.util.generator
import miragefairy2024.util.on
import miragefairy2024.util.registerChild
import miragefairy2024.util.registerDefaultLootTableGeneration
import miragefairy2024.util.registerFlammable
import miragefairy2024.util.registerModelGeneration
import miragefairy2024.util.registerShapelessRecipeGeneration
import miragefairy2024.util.registerSingletonBlockStateGeneration
import net.minecraft.data.models.model.TexturedModel
import net.minecraft.tags.BlockTags
import net.minecraft.tags.ItemTags
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.material.MapColor

// プラノキの板材カードなのだ
class PlasticTreePlanksBlockCard(configuration: PlasticWoodBlockConfiguration, private val input: () -> Item) : PlasticWoodBlockCard(configuration) {
    override fun createSettings(): BlockBehaviour.Properties = createPlasticTreeBaseWoodSetting()
        .strength(2.0F, 3.0F)
        .mapColor(MapColor.SAND)

    override suspend fun createBlock(properties: BlockBehaviour.Properties) = Block(properties)

    context(ModContext)
    override fun init() {
        super.init()

        block.registerSingletonBlockStateGeneration()
        block.registerModelGeneration(TexturedModel.CUBE)
        block.registerDefaultLootTableGeneration()

        // レシピ（原木から板材を作るのだ）
        registerShapelessRecipeGeneration(item, 4) {
            requires(input())
        } on input from input

        // 性質
        block.registerFlammable(5, 20)

        // タグ
        BlockTags.PLANKS.generator.registerChild(block)
        ItemTags.PLANKS.generator.registerChild(item)

    }
}
