package miragefairy2024.util

import kotlinx.coroutines.CompletableDeferred
import miragefairy2024.DataGenerationEvents
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.materials.MaterialCard
import net.minecraft.advancements.Advancement
import net.minecraft.advancements.AdvancementHolder
import net.minecraft.advancements.AdvancementRewards
import net.minecraft.advancements.AdvancementType
import net.minecraft.advancements.Criterion
import net.minecraft.advancements.critereon.EntityPredicate
import net.minecraft.advancements.critereon.InventoryChangeTrigger
import net.minecraft.advancements.critereon.ItemPredicate
import net.minecraft.advancements.critereon.KilledTrigger
import net.minecraft.advancements.critereon.LocationPredicate
import net.minecraft.advancements.critereon.PlayerTrigger
import net.minecraft.core.HolderLookup
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation as Identifier // MIGRATION2612 ReplaceImport net.minecraft.resources.Identifier
import net.minecraft.tags.TagKey
import net.minecraft.world.entity.EntityType
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.biome.Biome
import net.minecraft.world.level.levelgen.structure.Structure
import net.minecraft.world.level.storage.loot.LootTable
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue

object FairyJewelsLootTableHelper {
    val cache = mutableMapOf<Int, ResourceKey<LootTable>>()

    context(_: ModContext)
    fun getOrInit(fairyJewels: Int): ResourceKey<LootTable> {
        return cache.getOrPut(fairyJewels) {
            val lootTableId = Registries.LOOT_TABLE with MirageFairy2024.identifier("jewels_$fairyJewels")
            registerAdvancementRewardLootTableGeneration(lootTableId) {
                LootTable {
                    var i = fairyJewels

                    fun f(item: Item, price: Int) {
                        val count = i / price
                        check(count <= 64)
                        withPool(LootPool(ItemLootPoolEntry(item).apply(SetItemCountFunction.setCount(ConstantValue.exactly(count.toFloat())))))
                        i -= price * count
                    }

                    f(MaterialCard.JEWEL_10000.item(), 10000)
                    f(MaterialCard.JEWEL_5000.item(), 5000)
                    f(MaterialCard.JEWEL_1000.item(), 1000)
                    f(MaterialCard.JEWEL_500.item(), 500)
                    f(MaterialCard.JEWEL_100.item(), 100)
                    f(MaterialCard.JEWEL_50.item(), 50)
                    f(MaterialCard.JEWEL_10.item(), 10)
                    f(MaterialCard.JEWEL_5.item(), 5)
                    f(MaterialCard.JEWEL_1.item(), 1)

                    check(i == 0)
                }
            }
            lootTableId
        }
    }
}

enum class AdvancementCardType(
    val type: AdvancementType,
    val announceChat: Boolean,
    val showToast: Boolean,
    val fairyJewels: Int?,
) {
    SILENT(AdvancementType.TASK, false, false, null),
    TOAST_ONLY(AdvancementType.TASK, false, true, null),
    TOAST_AND_JEWELS(AdvancementType.TASK, false, true, 50),
    NORMAL(AdvancementType.TASK, true, true, 100),
    GOAL(AdvancementType.GOAL, true, true, 300),
    CHALLENGE(AdvancementType.CHALLENGE, true, true, 1000),
}

class AdvancementCard(
    private val identifier: Identifier,
    private val context: Context,
    private val icon: () -> ItemStack,
    name: EnJa,
    description: EnJa,
    private val criterion: (HolderLookup.Provider) -> Pair<String, Criterion<*>>,
    private val type: AdvancementCardType,
) {
    companion object {
        fun hasAnyItem(): (HolderLookup.Provider) -> Pair<String, Criterion<*>> {
            return { _ ->
                Pair("has_item", InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item()))
            }
        }

        @JvmName("hasItem")
        fun hasItem(itemGetter: () -> Item): (HolderLookup.Provider) -> Pair<String, Criterion<*>> {
            return { _ ->
                val item = itemGetter()
                Pair("has_${item.getIdentifier().path}", InventoryChangeTrigger.TriggerInstance.hasItems(item))
            }
        }

        @JvmName("hasItemTag")
        fun hasItemTag(itemTagGetter: () -> TagKey<Item>): (HolderLookup.Provider) -> Pair<String, Criterion<*>> {
            return { _ ->
                val itemTag = itemTagGetter()
                Pair("has_${itemTag.location.path}", InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(itemTag)))
            }
        }

        @JvmName("visitStructure")
        fun visit(structure: ResourceKey<Structure>): (HolderLookup.Provider) -> Pair<String, Criterion<*>> {
            return { registries ->
                Pair(
                    "visit_${structure.location().path}",
                    PlayerTrigger.TriggerInstance.located(
                        LocationPredicate.Builder.inStructure(
                            registries.lookupOrThrow(Registries.STRUCTURE).getOrThrow(structure)
                        )
                    ),
                )
            }
        }

        @JvmName("visitBiome")
        fun visit(biome: ResourceKey<Biome>): (HolderLookup.Provider) -> Pair<String, Criterion<*>> {
            return { registries ->
                Pair(
                    biome.location().path,
                    PlayerTrigger.TriggerInstance.located(
                        LocationPredicate.Builder.inBiome(
                            registries.lookupOrThrow(Registries.BIOME).getOrThrow(biome)
                        )
                    ),
                )
            }
        }

        fun kill(entityType: () -> EntityType<*>): (HolderLookup.Provider) -> Pair<String, Criterion<*>> {
            return { _ ->
                Pair(
                    "kill_${BuiltInRegistries.ENTITY_TYPE.getKey(entityType()).path}",
                    KilledTrigger.TriggerInstance.playerKilledEntity(EntityPredicate.Builder.entity().of(entityType())),
                )
            }
        }
    }

    sealed class Context
    class Root(val texture: Identifier) : Context()
    class Sub(val parent: suspend () -> AdvancementHolder) : Context()

    private val nameTranslation = Translation({ identifier.toLanguageKey("advancements", "title").replace('/', '.') }, name)
    private val descriptionTranslation = Translation({ identifier.toLanguageKey("advancements", "description").replace('/', '.') }, description)

    private val deferred = CompletableDeferred<AdvancementHolder>()
    suspend fun await() = deferred.await()

    context(_: ModContext)
    fun init() {
        check(nameTranslation.en.length <= 30) { "Advancement name is too long: $identifier = ${nameTranslation.en}" }
        check(nameTranslation.ja.length <= 18) { "Advancement name is too long: $identifier = ${nameTranslation.ja}" }
        nameTranslation.enJa()
        descriptionTranslation.enJa()

        val lootTableId = type.fairyJewels?.let { FairyJewelsLootTableHelper.getOrInit(it) }
        DataGenerationEvents.onGenerateAdvancement { registries, writer ->
            val advancement = Advancement.Builder.advancement()
                .let { if (context is Sub) it.parent(context.parent()) else it }
                .display(
                    icon(),
                    text { nameTranslation() },
                    text { descriptionTranslation() },
                    if (context is Root) context.texture else null,
                    type.type,
                    type.showToast,
                    type.announceChat,
                    false,
                )
                .let {
                    val pair = criterion(registries)
                    it.addCriterion(pair.first, pair.second)
                }
                .let { if (lootTableId != null) it.rewards(AdvancementRewards.Builder().addLootTable(lootTableId).build()) else it }
                .save(writer, identifier.string)
            deferred.complete(advancement)
        }
    }
}
