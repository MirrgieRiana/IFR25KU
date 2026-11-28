package miragefairy2024.mod.tree.contents.blockcards

import miragefairy2024.ModContext
import miragefairy2024.migration.noCollission__MIGRATION2612_ReplaceMethod // MIGRATION2612 RemoveImport
import miragefairy2024.mod.tree.TreeBlockCard
import miragefairy2024.mod.tree.TreeBlockConfiguration
import miragefairy2024.util.registerBlockFamily
import miragefairy2024.util.registerDefaultLootTableGeneration
import net.minecraft.data.models.model.TexturedModel
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.material.PushReaction

class TreePlanksButtonBlockCard(configuration: TreeBlockConfiguration, private val parent: () -> Block) : TreeBlockCard(configuration) {
    override fun createSettings(): BlockBehaviour.Properties = super.createSettings()
        .noCollission__MIGRATION2612_ReplaceMethod()
        .strength(0.5F)
        .pushReaction(PushReaction.DESTROY)

    context(_: ModContext)
    override fun init() {
        super.init()

        registerBlockFamily(TexturedModel.CUBE, parent) { it.button(block()) }
        block.registerDefaultLootTableGeneration()

    }
}
