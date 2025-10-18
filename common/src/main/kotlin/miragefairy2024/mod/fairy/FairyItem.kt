package miragefairy2024.mod.fairy

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.clientProxy
import miragefairy2024.mod.Emoji
import miragefairy2024.mod.invoke
import miragefairy2024.mod.magicplant.contents.magicplants.MirageFlowerCard
import miragefairy2024.mod.materials.MaterialCard
import miragefairy2024.mod.passiveskill.PASSIVE_SKILL_TRANSLATION
import miragefairy2024.mod.passiveskill.PassiveSkill
import miragefairy2024.mod.passiveskill.PassiveSkillContext
import miragefairy2024.mod.passiveskill.PassiveSkillProvider
import miragefairy2024.mod.passiveskill.PassiveSkillResult
import miragefairy2024.mod.passiveskill.PassiveSkillSpecification
import miragefairy2024.mod.passiveskill.PassiveSkillStatus
import miragefairy2024.mod.passiveskill.collect
import miragefairy2024.mod.passiveskill.description
import miragefairy2024.mod.passiveskill.effects.ManaBoostPassiveSkillEffect
import miragefairy2024.mod.passiveskill.findPassiveSkillProviders
import miragefairy2024.mod.recipeviewer.RecipeViewerCategoryCard
import miragefairy2024.mod.recipeviewer.registerIdentificationDataComponentTypes
import miragefairy2024.mod.recipeviewer.view.Alignment
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.view.Sizing
import miragefairy2024.mod.recipeviewer.view.ViewTexture
import miragefairy2024.mod.recipeviewer.views.CatalystSlotView
import miragefairy2024.mod.recipeviewer.views.ImageView
import miragefairy2024.mod.recipeviewer.views.OutputSlotView
import miragefairy2024.mod.recipeviewer.views.StackView
import miragefairy2024.mod.recipeviewer.views.View
import miragefairy2024.mod.recipeviewer.views.XListView
import miragefairy2024.mod.recipeviewer.views.YListView
import miragefairy2024.mod.recipeviewer.views.YSpaceView
import miragefairy2024.mod.recipeviewer.views.configure
import miragefairy2024.mod.recipeviewer.views.margin
import miragefairy2024.mod.recipeviewer.views.noBackground
import miragefairy2024.mod.recipeviewer.views.plusAssign
import miragefairy2024.util.AdvancementCard
import miragefairy2024.util.AdvancementCardType
import miragefairy2024.util.BIG_INTEGER_CODEC
import miragefairy2024.util.BIG_INTEGER_STREAM_CODEC
import miragefairy2024.util.EnJa
import miragefairy2024.util.ItemGroupCard
import miragefairy2024.util.Model
import miragefairy2024.util.ModelData
import miragefairy2024.util.ModelTexturesData
import miragefairy2024.util.Registration
import miragefairy2024.util.ResourceLocation
import miragefairy2024.util.Translation
import miragefairy2024.util.aqua
import miragefairy2024.util.createItemStack
import miragefairy2024.util.darkGray
import miragefairy2024.util.empty
import miragefairy2024.util.enJa
import miragefairy2024.util.eyeBlockPos
import miragefairy2024.util.generator
import miragefairy2024.util.getOrDefault
import miragefairy2024.util.gold
import miragefairy2024.util.gray
import miragefairy2024.util.green
import miragefairy2024.util.invoke
import miragefairy2024.util.join
import miragefairy2024.util.plus
import miragefairy2024.util.red
import miragefairy2024.util.register
import miragefairy2024.util.registerChild
import miragefairy2024.util.registerColorProvider
import miragefairy2024.util.registerItemGroup
import miragefairy2024.util.registerModelGeneration
import miragefairy2024.util.sortedEntrySet
import miragefairy2024.util.string
import miragefairy2024.util.text
import miragefairy2024.util.times
import miragefairy2024.util.toHolderSetCodec
import miragefairy2024.util.toIngredientStack
import miragefairy2024.util.yellow
import mirrg.kotlin.hydrogen.formatAs
import net.minecraft.advancements.critereon.InventoryChangeTrigger
import net.minecraft.advancements.critereon.ItemPredicate
import net.minecraft.advancements.critereon.ItemSubPredicate
import net.minecraft.advancements.critereon.MinMaxBounds
import net.minecraft.advancements.critereon.SingleComponentItemPredicate
import net.minecraft.core.HolderSet
import net.minecraft.core.RegistryAccess
import net.minecraft.core.component.DataComponentType
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import java.math.BigInteger
import kotlin.math.log
import kotlin.math.roundToInt

object FairyCard {
    val enName = "Mirage Fairy"
    val jaName = "妖精"
    val identifier = MirageFairy2024.identifier("fairy")
    val item = Registration(BuiltInRegistries.ITEM, identifier) { FairyItem(Item.Properties().fireResistant()) }
}

private val identifier = MirageFairy2024.identifier("fairy")
private val RARE_TRANSLATION = Translation({ "item.${identifier.toLanguageKey()}.rare" }, "Rare", "レア")
private val MANA_TRANSLATION = Translation({ "item.${identifier.toLanguageKey()}.mana" }, "Mana", "魔力")
private val LEVEL_TRANSLATION = Translation({ "item.${identifier.toLanguageKey()}.level" }, "Level", "レベル")
private val CONDENSATION_TRANSLATION = Translation({ "item.${identifier.toLanguageKey()}.condensation" }, "Condensation", "凝縮数")
private val CONDENSATION_RECIPE_TRANSLATION = Translation({ "item.${identifier.toLanguageKey()}.condensation_recipe" }, "Can be (de)condensed by crafting table", "作業台で凝縮・展開")

val fairiesItemGroupCard = ItemGroupCard(
    MirageFairy2024.identifier("fairies"), "Fairies", "妖精",
) { motifRegistry.entrySet().random().value.createFairyItemStack() }

context(ModContext)
fun initFairyItem() {
    FairyCard.let { card ->
        card.item.register()

        card.item.registerItemGroup(fairiesItemGroupCard.itemGroupKey) {
            motifRegistry.sortedEntrySet.map { it.value.createFairyItemStack() }
        }

        card.item.registerModelGeneration(createFairyModel())
        card.item.registerColorProvider { itemStack, tintIndex ->
            if (tintIndex == 4) {
                val condensation = itemStack.getFairyCondensation()
                when (getNiceCondensation(condensation).first) {
                    0 -> 0xFFFF8E8E.toInt() // 赤
                    1 -> 0xFFB90000.toInt()
                    2 -> 0xFFAAAAFF.toInt() // 青
                    3 -> 0xFF0000FF.toInt()
                    4 -> 0xFF00D100.toInt() // 緑
                    5 -> 0xFF007A00.toInt()
                    6 -> 0xFFFFFF60.toInt() // 黄色
                    7 -> 0xFF919100.toInt()
                    8 -> 0xFF00D1D1.toInt() // 水色
                    9 -> 0xFF009E9E.toInt()
                    10 -> 0xFFFF87FF.toInt() // マゼンタ
                    11 -> 0xFFDB00DB.toInt()
                    12 -> 0xFFFFBB77.toInt() // オレンジ
                    13 -> 0xFFCE6700.toInt()
                    14 -> 0xFF66FFB2.toInt() // 草
                    15 -> 0xFF00B758.toInt()
                    16 -> 0xFFD1A3FF.toInt() // 紫
                    17 -> 0xFFA347FF.toInt()
                    18 -> 0xFFCECECE.toInt() // 灰色
                    19 -> 0xFF919191.toInt()
                    else -> 0xFF333333.toInt()
                }
            } else {
                val motif = itemStack.getFairyMotif() ?: return@registerColorProvider 0xFFFF00FF.toInt()
                when (tintIndex) {
                    0 -> motif.skinColor or 0xFF000000.toInt()
                    1 -> motif.frontColor or 0xFF000000.toInt()
                    2 -> motif.backColor or 0xFF000000.toInt()
                    3 -> motif.hairColor or 0xFF000000.toInt()
                    else -> 0xFFFF00FF.toInt()
                }
            }
        }

        card.item.enJa(EnJa(card.enName, card.jaName))

        SOUL_STREAM_CONTAINABLE_TAG.generator.registerChild(card.item)

        card.item.registerIdentificationDataComponentTypes { listOf(FAIRY_MOTIF_DATA_COMPONENT_TYPE) }
    }

    RARE_TRANSLATION.enJa()
    MANA_TRANSLATION.enJa()
    LEVEL_TRANSLATION.enJa()
    CONDENSATION_TRANSLATION.enJa()
    CONDENSATION_RECIPE_TRANSLATION.enJa()

    fairiesItemGroupCard.init()

    Registration(BuiltInRegistries.DATA_COMPONENT_TYPE, MirageFairy2024.identifier("fairy_motif")) { FAIRY_MOTIF_DATA_COMPONENT_TYPE }.register()
    Registration(BuiltInRegistries.DATA_COMPONENT_TYPE, MirageFairy2024.identifier("fairy_condensation")) { FAIRY_CONDENSATION_DATA_COMPONENT_TYPE }.register()

    FairyMotifItemSubPredicate.INSTANCE.register()
    FairyRareItemSubPredicate.INSTANCE.register()

    rare10Advancement.init()
    timiaAdvancement.init()

    FairyFamilyRecipeViewerCategoryCard.init()
}

private fun createFairyModel() = Model {
    ModelData(
        parent = ResourceLocation("item/generated"),
        textures = ModelTexturesData(
            "layer0" to MirageFairy2024.identifier("item/fairy_skin").string,
            "layer1" to MirageFairy2024.identifier("item/fairy_front").string,
            "layer2" to MirageFairy2024.identifier("item/fairy_back").string,
            "layer3" to MirageFairy2024.identifier("item/fairy_hair").string,
            "layer4" to MirageFairy2024.identifier("item/fairy_dress").string,
        ),
    )
}

class FairyItem(settings: Properties) : Item(settings), PassiveSkillProvider {
    override fun getName(stack: ItemStack): Component {
        val originalName = stack.getFairyMotif()?.displayName ?: super.getName(stack)
        val condensation = stack.getFairyCondensation()
        return if (condensation != BigInteger.ONE) text { originalName + " x$condensation"() } else originalName
    }

    override fun appendHoverText(stack: ItemStack, context: TooltipContext, tooltipComponents: MutableList<Component>, tooltipFlag: TooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag)
        val player = clientProxy?.getClientPlayer()
        val motif = stack.getFairyMotif() ?: return

        // パッシブスキル判定
        val (count, manaBoost, status) = if (player != null) { // ワールドなど
            // この妖精の実際に適用されるパッシブスキルを表示

            // プレイヤーのパッシブスキルプロバイダーを取得
            val passiveSkillProviders = player.findPassiveSkillProviders()

            // 一旦魔力用のコレクト
            val result = PassiveSkillResult()
            result.collect(passiveSkillProviders.passiveSkills, player, ManaBoostPassiveSkillEffect.Value(mapOf()), true) // 先行判定

            // この妖精が受けられる魔力ブーストを計算
            val manaBoost = result[ManaBoostPassiveSkillEffect].map.entries.sumOf { (keyMotif, value) -> if (motif in keyMotif) value else 0.0 }

            // 実際のプロバイダを取得
            val provider = passiveSkillProviders.providers.find { it.first === stack }

            if (provider != null) { // この妖精はパッシブスキルに関与している
                // 実際に発動しているレベル分の個数と魔力パッシブを適用して表示
                Triple(provider.third.count, manaBoost, provider.second)
            } else { // この妖精はパッシブスキルに関与していない
                // 魔力パッシブの効果だけ適用して表示
                Triple(stack.getFairyCondensation().toDouble() * stack.count, manaBoost, PassiveSkillStatus.DISABLED)
            }
        } else { // 初期化時など
            // この妖精の固有のパッシブスキルを出力
            Triple(stack.getFairyCondensation().toDouble() * stack.count, 0.0, PassiveSkillStatus.DISABLED)
        }
        val level = motif.rare.toDouble() + log(count, 3.0)
        val mana = level * (1.0 + manaBoost)

        // 魔力・個数
        tooltipComponents += text {
            listOf(
                MANA_TRANSLATION(),
                ": "(),
                Emoji.MANA(),
                (mana formatAs "%.1f")(),
                "  "(),
                "(x${count formatAs "%.0f"})"(),
            ).join().aqua
        }

        // レベル・凝縮数
        tooltipComponents += text {
            listOf(
                LEVEL_TRANSLATION(),
                ": "(),
                Emoji.STAR(),
                (level formatAs "%.1f")(),
                "  "(),
                CONDENSATION_TRANSLATION(),
                ": x${stack.getFairyCondensation()}"(),
                if (stack.count != 1) " *${stack.count}"() else empty(),
                *(if (tooltipFlag.isAdvanced) listOf(
                    "  (History: ${player?.fairyHistoryContainer?.getOrDefault()?.get(motif) ?: BigInteger.ZERO}, Dream: ${player?.fairyDreamContainer?.getOrDefault()?.entries?.size ?: 0})"(), // TODO もっといい表示に
                ) else listOf()).toTypedArray()
            ).join().green
        }

        // レア・ID
        tooltipComponents += text {
            listOf(
                RARE_TRANSLATION(),
                ": ${motif.rare}"(),
                "  "(),
                motif.getIdentifier()!!.path(),
            ).join().green
        }

        // 機能説明
        tooltipComponents += text { CONDENSATION_RECIPE_TRANSLATION().yellow }

        // パッシブスキル
        if (motif.passiveSkillSpecifications.isNotEmpty()) {

            tooltipComponents += text { empty() }

            val isEffectiveItemStack = status == PassiveSkillStatus.EFFECTIVE || status == PassiveSkillStatus.SUPPORTING
            tooltipComponents += text { (PASSIVE_SKILL_TRANSLATION() + ": "() + status.description.let { if (!isEffectiveItemStack) it.red else it }).let { if (isEffectiveItemStack) it.gold else it.gray } }
            val passiveSkillContext = player?.let { PassiveSkillContext(it.level(), it.eyeBlockPos, it) }
            motif.passiveSkillSpecifications.forEach { specification ->
                fun <T : Any> getSpecificationText(specification: PassiveSkillSpecification<T>): Component {
                    val actualMana = if (specification.effect.isPreprocessor) level else level * (1.0 + manaBoost)
                    val conditionValidityList = specification.conditions.map { Pair(it, passiveSkillContext != null && it.test(passiveSkillContext, level, mana)) }
                    val isAvailableSpecification = conditionValidityList.all { it.second }
                    return run {
                        val texts = mutableListOf<Component>()
                        texts += text { " "() }
                        texts += specification.effect.getText(specification.valueProvider(actualMana))
                        if (conditionValidityList.isNotEmpty()) {
                            texts += text { " ["() }
                            conditionValidityList.forEachIndexed { index, (condition, isValidCondition) ->
                                if (index != 0) texts += text { ","() }
                                texts += condition.text.let { if (!isValidCondition) it.red else it }
                            }
                            texts += text { "]"() }
                        }
                        texts.join()
                    }.let { if (isAvailableSpecification) if (isEffectiveItemStack) it.gold else it.gray else it.darkGray }
                }
                tooltipComponents += getSpecificationText(specification)
            }
        }
    }

    override fun isBarVisible(stack: ItemStack): Boolean {
        val condensation = stack.getFairyCondensation()
        val niceCondensation = getNiceCondensation(condensation).second
        return condensation != niceCondensation
    }

    override fun getBarWidth(stack: ItemStack): Int {
        val condensation = stack.getFairyCondensation()
        val niceCondensation = getNiceCondensation(condensation).second
        val nextNiceCondensation = niceCondensation * 3.toBigInteger()
        return (13.0 * (condensation - niceCondensation).toDouble() / (nextNiceCondensation - niceCondensation).toDouble()).roundToInt()
    }

    override fun getBarColor(stack: ItemStack) = 0x00FF00

    override fun getPassiveSkill(itemStack: ItemStack): PassiveSkill? {
        val motif = itemStack.getFairyMotif() ?: return null
        return PassiveSkill(
            "fairy/" * motif.getIdentifier()!!,
            motif,
            motif.rare.toDouble(),
            itemStack.getFairyCondensation().toDouble() * itemStack.count.toDouble(),
            motif.passiveSkillSpecifications,
        )
    }
}


val FAIRY_MOTIF_DATA_COMPONENT_TYPE: DataComponentType<Motif> = DataComponentType.builder<Motif>()
    .persistent(motifRegistry.byNameCodec())
    .networkSynchronized(ByteBufCodecs.registry(motifRegistryKey))
    .cacheEncoding()
    .build()

fun ItemStack.getFairyMotif(): Motif? = this.get(FAIRY_MOTIF_DATA_COMPONENT_TYPE)
fun ItemStack.setFairyMotif(motif: Motif?) = this.set(FAIRY_MOTIF_DATA_COMPONENT_TYPE, motif)

val FAIRY_CONDENSATION_DATA_COMPONENT_TYPE: DataComponentType<BigInteger> = DataComponentType.builder<BigInteger>()
    .persistent(BIG_INTEGER_CODEC)
    .networkSynchronized(BIG_INTEGER_STREAM_CODEC)
    .build()

fun ItemStack.getFairyCondensation(): BigInteger = this.get(FAIRY_CONDENSATION_DATA_COMPONENT_TYPE) ?: BigInteger.ONE
fun ItemStack.setFairyCondensation(condensation: BigInteger) = this.set(FAIRY_CONDENSATION_DATA_COMPONENT_TYPE, condensation)


class FairyMotifItemSubPredicate(val motif: HolderSet<Motif>) : SingleComponentItemPredicate<Motif> {
    companion object {
        val CODEC: Codec<FairyMotifItemSubPredicate> = motifRegistryKey.toHolderSetCodec().xmap(::FairyMotifItemSubPredicate, FairyMotifItemSubPredicate::motif)
        val INSTANCE = Registration(BuiltInRegistries.ITEM_SUB_PREDICATE_TYPE, MirageFairy2024.identifier("fairy_motif")) { ItemSubPredicate.Type(CODEC) }
    }

    override fun componentType() = FAIRY_MOTIF_DATA_COMPONENT_TYPE
    override fun matches(stack: ItemStack, value: Motif) = motif.contains(motifRegistry.wrapAsHolder(value))
}

class FairyRareItemSubPredicate(val rare: MinMaxBounds.Ints) : SingleComponentItemPredicate<Motif> {
    companion object {
        val CODEC: Codec<FairyRareItemSubPredicate> = MinMaxBounds.Ints.CODEC.xmap(::FairyRareItemSubPredicate, FairyRareItemSubPredicate::rare)
        val INSTANCE = Registration(BuiltInRegistries.ITEM_SUB_PREDICATE_TYPE, MirageFairy2024.identifier("fairy_rare")) { ItemSubPredicate.Type(CODEC) }
    }

    override fun componentType() = FAIRY_MOTIF_DATA_COMPONENT_TYPE
    override fun matches(stack: ItemStack, value: Motif) = rare.matches(value.rare)
}


val rare10Advancement = AdvancementCard(
    identifier = MirageFairy2024.identifier("rare_10_fairy"),
    context = AdvancementCard.Sub { MirageFlowerCard.advancement!!.await() },
    icon = { MotifCard.SUN.createFairyItemStack() },
    name = EnJa("Transcendence of Nature", "自然の超越"),
    description = EnJa("Summon a level 10 rarity fairy", "レア度10の妖精を召喚する"),
    criterion = {
        Pair(
            "has_rare_10_fairy", InventoryChangeTrigger.TriggerInstance.hasItems(
                ItemPredicate.Builder.item()
                    .of(FairyCard.item())
                    .withSubPredicate(FairyRareItemSubPredicate.INSTANCE(), FairyRareItemSubPredicate(MinMaxBounds.Ints.atLeast(10)))
            )
        )
    },
    type = AdvancementCardType.NORMAL,
)
val timiaAdvancement = AdvancementCard(
    identifier = MotifCard.TIME.identifier,
    context = AdvancementCard.Sub { MaterialCard.MIRAGE_FLOUR_OF_UNIVERSE.advancement!!.await() },
    icon = { MotifCard.TIME.createFairyItemStack() },
    name = EnJa("Fairy of Time", "時の妖精"),
    description = EnJa("Summon Timia the fairy of time", "時精ティーミャを召喚する"),
    criterion = {
        Pair(
            "has_time_fairy", InventoryChangeTrigger.TriggerInstance.hasItems(
                ItemPredicate.Builder.item()
                    .of(FairyCard.item())
                    .withSubPredicate(FairyMotifItemSubPredicate.INSTANCE(), FairyMotifItemSubPredicate(HolderSet.direct(motifRegistry.wrapAsHolder(MotifCard.TIME))))
            )
        )
    },
    type = AdvancementCardType.CHALLENGE,
)


fun Motif?.createFairyItemStack(@Suppress("UNUSED_PARAMETER") vararg dummy: Void, condensation: BigInteger = BigInteger.ONE, count: Int = 1): ItemStack {
    val itemStack = FairyCard.item().createItemStack(count)
    itemStack.setFairyMotif(this)
    itemStack.setFairyCondensation(condensation)
    return itemStack
}

object FairyFamilyRecipeViewerCategoryCard : RecipeViewerCategoryCard<FairyFamilyRecipeViewerCategoryCard.FairyFamilyNotation>() {
    override fun getId() = MirageFairy2024.identifier("fairy_family")
    override fun getName() = EnJa("Fairy Family", "妖精系統")
    override fun getIcon() = MotifCard.IRON.createFairyItemStack()
    override fun getRecipeCodec(registryAccess: RegistryAccess) = FairyFamilyNotation.CODEC
    override fun getInputs(recipeEntry: RecipeEntry<FairyFamilyNotation>) = listOf(Input(recipeEntry.recipe.motif.createFairyItemStack().toIngredientStack(), true))
    override fun getOutputs(recipeEntry: RecipeEntry<FairyFamilyNotation>) = (recipeEntry.recipe.parents + recipeEntry.recipe.children).map { it.createFairyItemStack() }

    override fun createRecipeEntries(registryAccess: RegistryAccess): Iterable<RecipeEntry<FairyFamilyNotation>> {
        val childrenTable = motifRegistry.entrySet()
            .flatMap { it.value.parents.map { parent -> parent to it.value } }
            .groupBy { it.first }
            .mapValues { it.value.map { pair -> pair.second } }

        // TODO 先祖・子孫
        //fun Motif.getAncestors(): List<Motif> = this.parents.flatMap { it.getAncestors() } + this.parents
        //fun Motif.getDescendants(): List<Motif> = childrenTable.getOrElse(this) { listOf() }.flatMap { it.getDescendants() } + childrenTable.getOrElse(this) { listOf() }

        return motifRegistry.sortedEntrySet.mapNotNull {
            val parents = it.value.parents
            val children = childrenTable[it.value] ?: listOf()
            if (parents.isEmpty() && children.isEmpty()) return@mapNotNull null
            RecipeEntry(registryAccess, it.value.getIdentifier()!!, FairyFamilyNotation(it.value, parents, children), true)
        }
    }

    override fun createView(recipeEntry: RecipeEntry<FairyFamilyNotation>) = View {
        view += YListView().configure {
            view.sizingX = Sizing.FILL
            view.sizingY = Sizing.FILL

            // 上に親妖精
            view += YListView().configure {
                position.alignmentX = Alignment.CENTER
                position.alignmentY = Alignment.END
                position.weight = 1.0
                position.ignoreLayoutY = true
                recipeEntry.recipe.parents.chunked(7).forEach { chunk ->
                    view += XListView().configure {
                        chunk.forEach {
                            view += OutputSlotView(it.createFairyItemStack())
                        }
                    }
                }
            }

            view += YSpaceView(9)

            // 中段に対象の妖精
            view += StackView().configure {
                position.alignmentX = Alignment.CENTER

                val texture = ViewTexture(MirageFairy2024.identifier("textures/gui/sprites/fairy_family_arrow.png"), IntPoint(16, 34), IntRectangle(0, 0, 16, 34))
                view += ImageView(texture).configure { }.margin(1, -8)

                view += CatalystSlotView(recipeEntry.recipe.motif.createFairyItemStack().toIngredientStack()).noBackground()

            }

            view += YSpaceView(9)

            // 下に子妖精
            view += YListView().configure {
                position.alignmentX = Alignment.CENTER
                position.alignmentY = Alignment.START
                position.weight = 2.0
                position.ignoreLayoutY = true
                recipeEntry.recipe.children.chunked(7).forEach { chunk ->
                    view += XListView().configure {
                        chunk.forEach {
                            view += OutputSlotView(it.createFairyItemStack())
                        }
                    }
                }
            }

        }
    }

    class FairyFamilyNotation(val motif: Motif, val parents: List<Motif>, val children: List<Motif>) {
        companion object {
            val CODEC: Codec<FairyFamilyNotation> = RecordCodecBuilder.create { instance ->
                instance.group(
                    motifRegistry.byNameCodec().fieldOf("Motif").forGetter { it.motif },
                    motifRegistry.byNameCodec().listOf().fieldOf("Parents").forGetter { it.parents },
                    motifRegistry.byNameCodec().listOf().fieldOf("Children").forGetter { it.children },
                ).apply(instance, ::FairyFamilyNotation)
            }
        }
    }
}
