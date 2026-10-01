package miragefairy2024.mod.tree.contents.blockcards

import miragefairy2024.ModContext
import miragefairy2024.mod.tree.TreeBlockCard
import miragefairy2024.mod.tree.TreeBlockConfiguration
import miragefairy2024.util.ItemLootPoolEntry
import miragefairy2024.util.LootPool
import miragefairy2024.util.configure
import miragefairy2024.util.get
import miragefairy2024.util.registerComposterInput
import miragefairy2024.util.registerCutoutRenderLayer
import miragefairy2024.util.registerFlammable
import miragefairy2024.util.registerLootTableGeneration
import net.minecraft.core.registries.Registries
import net.minecraft.world.item.Item
import net.minecraft.world.item.enchantment.Enchantments
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.SoundType
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.material.MapColor
import net.minecraft.world.level.material.PushReaction
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction
import net.minecraft.world.level.storage.loot.predicates.BonusLevelTableCondition
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator

abstract class AbstractTreeLeavesBlockCard(configuration: TreeBlockConfiguration, private val sapling: () -> TreeBlockCard, private val extraDrop: (() -> Item)?) : TreeBlockCard(configuration) {
    override fun createSettings(): BlockBehaviour.Properties = super.createSettings()
        .mapColor(MapColor.PLANT)
        .strength(0.2F)
        .randomTicks()
        .sound(SoundType.GRASS)
        .noOcclusion()
        .isValidSpawn(Blocks::ocelotOrParrot)
        .isSuffocating(Blocks::never)
        .isViewBlocking(Blocks::never)
        .ignitedByLava()
        .pushReaction(PushReaction.DESTROY)
        .isRedstoneConductor(Blocks::never)

    context(ModContext)
    override fun init() {
        super.init()

        // レンダリング
        block.registerCutoutRenderLayer()

        // レシピ
        block.registerLootTableGeneration { it, registries ->
            it.createLeavesDrops(block(), sapling().block(), 0.05F / 4F, 0.0625F / 4F, 0.083333336F / 4F, 0.1F / 4F).configure {
                if (extraDrop != null) {
                    withPool(LootPool(it.applyExplosionDecay(block(), ItemLootPoolEntry(extraDrop()) {
                        apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 2.0F)))
                        `when`(BonusLevelTableCondition.bonusLevelFlatChance(registries[Registries.ENCHANTMENT, Enchantments.FORTUNE], 0.02F, 0.022222223F, 0.025F, 0.033333335F, 0.1F))
                    })) {
                        `when`(it.doesNotHaveShearsOrSilkTouch())
                    })
                }
            }
        }
        item.registerComposterInput(0.3F)

        // 性質
        block.registerFlammable(30, 30)

    }
}
