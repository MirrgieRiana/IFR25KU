package miragefairy2024.util

import miragefairy2024.DataGenerationEvents
import miragefairy2024.ModContext
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider
import net.minecraft.core.HolderLookup
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.world.entity.EntityType
import net.minecraft.world.item.Item
import net.minecraft.world.item.enchantment.Enchantments
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.storage.loot.LootPool
import net.minecraft.world.level.storage.loot.LootTable
import net.minecraft.world.level.storage.loot.entries.AlternativesEntry
import net.minecraft.world.level.storage.loot.entries.EmptyLootItem
import net.minecraft.world.level.storage.loot.entries.EntryGroup
import net.minecraft.world.level.storage.loot.entries.LootItem
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer
import net.minecraft.world.level.storage.loot.entries.SequentialEntry
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator

inline fun <T> T.configure(block: T.() -> Unit) = this.apply(block)


@Suppress("FunctionName")
fun LootTable(vararg pools: LootPool.Builder, initializer: LootTable.Builder.() -> Unit = {}): LootTable.Builder = LootTable.lootTable().configure {
    pools.forEach {
        this.withPool(it)
    }
    initializer.invoke(this)
}

@Suppress("FunctionName")
fun LootPool(vararg entries: LootPoolEntryContainer.Builder<*>, initializer: LootPool.Builder.() -> Unit = {}): LootPool.Builder = LootPool.lootPool().configure {
    entries.forEach {
        this.add(it)
    }
    initializer.invoke(this)
}

@Suppress("FunctionName")
fun EmptyLootPoolEntry(initializer: LootPoolSingletonContainer.Builder<*>.() -> Unit = {}): LootPoolSingletonContainer.Builder<*> = EmptyLootItem.emptyItem().configure {
    initializer.invoke(this)
}

@Suppress("FunctionName")
fun ItemLootPoolEntry(item: Item, initializer: LootPoolSingletonContainer.Builder<*>.() -> Unit = {}): LootPoolSingletonContainer.Builder<*> = LootItem.lootTableItem(item).configure {
    initializer.invoke(this)
}

@Suppress("FunctionName")
fun AlternativeLootPoolEntry(vararg children: LootPoolEntryContainer.Builder<*>, initializer: AlternativesEntry.Builder.() -> Unit = {}): AlternativesEntry.Builder = AlternativesEntry.alternatives(*children).configure {
    initializer.invoke(this)
}

@Suppress("FunctionName")
fun GroupLootPoolEntry(vararg children: LootPoolEntryContainer.Builder<*>, initializer: EntryGroup.Builder.() -> Unit = {}): EntryGroup.Builder = EntryGroup.list(*children).configure {
    initializer.invoke(this)
}

@Suppress("FunctionName")
fun SequenceLootPoolEntry(vararg children: LootPoolEntryContainer.Builder<*>, initializer: SequentialEntry.Builder.() -> Unit = {}): SequentialEntry.Builder = SequentialEntry.sequential(*children).configure {
    initializer.invoke(this)
}


context(ModContext)
fun (() -> Block).registerLootTableGeneration(initializer: (FabricBlockLootTableProvider, HolderLookup.Provider) -> LootTable.Builder) = DataGenerationEvents.onGenerateBlockLootTable { it, registries ->
    it.add(this(), initializer(it, registries).setRandomSequence(this().lootTable.location()))
}

context(ModContext)
fun (() -> Block).registerDefaultLootTableGeneration() = this.registerLootTableGeneration { it, _ ->
    it.createSingleItemTable(this())
}

context(ModContext)
fun registerChestLootTableGeneration(lootTableId: ResourceKey<LootTable>, initializer: (HolderLookup.Provider) -> LootTable.Builder) = DataGenerationEvents.onGenerateChestLootTable { it, registries ->
    it(lootTableId, initializer(registries).setRandomSequence(lootTableId.location()))
}

context(ModContext)
fun registerArchaeologyLootTableGeneration(lootTableId: ResourceKey<LootTable>, initializer: (HolderLookup.Provider) -> LootTable.Builder) = DataGenerationEvents.onGenerateArchaeologyLootTable { it, registries ->
    it(lootTableId, initializer(registries).setRandomSequence(lootTableId.location()))
}

context(ModContext)
fun (() -> EntityType<*>).registerLootTableGeneration(initializer: (HolderLookup.Provider) -> LootTable.Builder) = DataGenerationEvents.onGenerateEntityLootTable { it, registries ->
    it(this(), initializer(registries).setRandomSequence(this().defaultLootTable.location()))
}

context(ModContext)
fun registerAdvancementRewardLootTableGeneration(lootTableId: ResourceKey<LootTable>, initializer: (HolderLookup.Provider) -> LootTable.Builder) = DataGenerationEvents.onGenerateAdvancementRewardLootTable { it, registries ->
    it(lootTableId, initializer(registries).setRandomSequence(lootTableId.location()))
}

enum class FortuneEffect {
    IGNORE,
    ORE,
    UNIFORM,
}

context(ModContext)
fun (() -> Block).registerOreLootTableGeneration(drop: () -> Item, additionalCount: ClosedFloatingPointRange<Float>? = null, fortuneEffect: FortuneEffect = FortuneEffect.ORE) = this.registerLootTableGeneration { it, registries ->
    it.createSilkTouchDispatchTable(this(), it.applyExplosionDecay(this(), ItemLootPoolEntry(drop()) {
        if (additionalCount != null) apply(SetItemCountFunction.setCount(UniformGenerator.between(additionalCount.start, additionalCount.endInclusive)))
        when (fortuneEffect) {
            FortuneEffect.IGNORE -> Unit
            FortuneEffect.ORE -> apply(ApplyBonusCount.addOreBonusCount(registries[Registries.ENCHANTMENT, Enchantments.FORTUNE]))
            FortuneEffect.UNIFORM -> apply(ApplyBonusCount.addUniformBonusCount(registries[Registries.ENCHANTMENT, Enchantments.FORTUNE]))
        }
    }))
}
