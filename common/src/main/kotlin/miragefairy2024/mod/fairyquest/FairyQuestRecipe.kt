package miragefairy2024.mod.fairyquest

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.lib.PlacedItemFeature
import miragefairy2024.mod.haimeviska.HaimeviskaBlockCard
import miragefairy2024.mod.materials.BlockMaterialCard
import miragefairy2024.mod.materials.MaterialCard
import miragefairy2024.mod.recipeviewer.Alignment
import miragefairy2024.mod.recipeviewer.ArrowView
import miragefairy2024.mod.recipeviewer.CatalystSlotView
import miragefairy2024.mod.recipeviewer.ColorPair
import miragefairy2024.mod.recipeviewer.InputSlotView
import miragefairy2024.mod.recipeviewer.OutputSlotView
import miragefairy2024.mod.recipeviewer.RecipeViewerCategoryCard
import miragefairy2024.mod.recipeviewer.TextView
import miragefairy2024.mod.recipeviewer.View
import miragefairy2024.mod.recipeviewer.XListView
import miragefairy2024.mod.recipeviewer.XSpaceView
import miragefairy2024.mod.recipeviewer.YListView
import miragefairy2024.mod.recipeviewer.YSpaceView
import miragefairy2024.mod.recipeviewer.noBackground
import miragefairy2024.mod.recipeviewer.plusAssign
import miragefairy2024.util.Chance
import miragefairy2024.util.EnJa
import miragefairy2024.util.IngredientStack
import miragefairy2024.util.Registration
import miragefairy2024.util.Translation
import miragefairy2024.util.createItemStack
import miragefairy2024.util.enJa
import miragefairy2024.util.flower
import miragefairy2024.util.get
import miragefairy2024.util.invoke
import miragefairy2024.util.overworld
import miragefairy2024.util.per
import miragefairy2024.util.placementModifiers
import miragefairy2024.util.register
import miragefairy2024.util.registerChestLoot
import miragefairy2024.util.registerDynamicGeneration
import miragefairy2024.util.registerFeature
import miragefairy2024.util.square
import miragefairy2024.util.surface
import miragefairy2024.util.text
import miragefairy2024.util.toIngredientStack
import miragefairy2024.util.weightedRandom
import miragefairy2024.util.with
import mirrg.kotlin.helium.join
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder
import net.fabricmc.fabric.api.event.registry.RegistryAttribute
import net.minecraft.core.Registry
import net.minecraft.core.RegistryAccess
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.ItemTags
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.levelgen.GenerationStep
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration
import net.minecraft.world.level.storage.loot.BuiltInLootTables
import net.minecraft.world.level.storage.loot.LootContext
import net.minecraft.world.level.storage.loot.LootTable
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition

val fairyQuestRecipeRegistryKey: ResourceKey<Registry<FairyQuestRecipe>> = ResourceKey.createRegistryKey(MirageFairy2024.identifier("fairy_quest_recipe"))
val fairyQuestRecipeRegistry: Registry<FairyQuestRecipe> = FabricRegistryBuilder.createSimple(fairyQuestRecipeRegistryKey).attribute(RegistryAttribute.SYNCED).buildAndRegister()

interface FairyQuestRecipe {
    val color: Int
    val icon: () -> ItemStack
    val title: Component
    val message: Component
    val client: Component
    val duration: Int
    val inputs: List<() -> IngredientStack>
    val outputs: List<() -> ItemStack>
}

@Suppress("SpellCheckingInspection")
enum class FairyQuestRecipeCard(
    path: String,
    override val color: Int,
    val lootCategory: LootCategory,
    enTitle: String,
    jaTitle: String,
    enMessage: String,
    jaMessage: String,
    enClient: String,
    jaClient: String,
    override val inputs: List<() -> IngredientStack>,
    override val outputs: List<() -> ItemStack>,
    override val duration: Int = 20 * 10,
    override val icon: () -> ItemStack = outputs.first(),
) : FairyQuestRecipe {
    EMERGENCY_SITUATION(
        "emergency_situation", 0xFF6900, LootCategory.RARE,
        "Emergency Situation", "非常事態",
        "Help!!!\n\n!!!!\n\n!!!!!\n\nI'm out of toilet paper!!!!!!",
        "助けて！！！\n\n！！！！\n\n！！！！！\n\nトイレットペーパーが無いの！！！！！！",
        "The Institute of Fairy Research\nEthics Department\nTirirknofe Herirmina", "妖精研究所\n倫理部\nティリルクノフェ・ヘリルミーナ",
        listOf({ Items.PAPER.toIngredientStack(24) }),
        listOf { Items.EMERALD.createItemStack(1) },
    ),
    NOTICE_FOR_CERAMIC_BRICK_DONATIONS(
        "notice_for_ceramic_brick_donations", 0xFFFAF2, LootCategory.RARE,
        "Notice for Ceramic Brick Donations", "セラミックレンガ募集のお知らせ",
        "We are actively seeking donations of ceramic bricks for the reconstruction project of the vacuum decay reactor. If you are in need, we will selectively provide reusable building materials.",
        "真空崩壊炉改修工事に向けてセラミックレンガの寄付を広く募集しております。必要な場合は、リユース可能な建築資材を選別してご提供いたします。",
        "The Institute of Fairy Research\nCordelia Branch", "妖精研究所\nコーディリア支部",
        listOf({ Items.BRICKS.toIngredientStack(1) }),
        listOf({ BlockMaterialCard.DRYWALL.item().createItemStack(1) }, { Items.STONE_BRICKS.createItemStack(1) }, { Items.WHITE_CONCRETE.createItemStack(1) }),
    ),
    IMPROMPTU_FANTASTIC_CARNIVAL(
        "impromptu_fantastic_carnival", 0xFCF5DF, LootCategory.RARE,
        "Impromptu Fantastic Carnival", "即席ファンタスティックカーニバル",
        "Help! We're running out of cakes, and the chickens are taking their sweet time laying eggs! Can someone please help out? We don't have time to bake, so substitute it right away!",
        "たいへん！ケーキが足りないのにニワトリがなかなか卵を産まないの！お願い！作ってる時間はないから、今すぐ誰か代わりになって！",
        "Breadia the fairy of bread", "麺麭精ブレアージャ",
        listOf({ MaterialCard.FRACTAL_WISP.item().toIngredientStack(1) }), // TODO -> ケーキ精
        listOf { Items.CAKE.createItemStack(1) },
    ),
    NEW_PRODUCT_FROM_FRI(
        "new_product_from_fri", 0xAC5BD8, LootCategory.COMMON,
        "New product from FRI!", "妖精研究所から新商品登場！",
        listOf(
            "The Institute of Fairy Research proudly presents its latest product, the “Fairy Quest Card”! It's an innovative communication device that allows communication and material transfer with different and parallel worlds!",
            "Have you ever been caught in a sudden thunderstorm while out and about? With this, you won't fear unexpected thunderstorms anymore!",
            "Simply insert the payment into the “Fairy Quest Card” connected to the stores of parallel worlds, and all you need to do is receive the data for the product. The built-in 3D printer will handle the rest, just like this!",
            "",
            "*For receiving items containing special elements, a separate cartridge for special elements (sold separately) may be required.",
            "*For receiving items with low entropy, a separate anti-entropy cartridge (sold separately) may be required.",
            "*The institute assumes no responsibility for disputes arising from transactions with communication destinations.",
            "*Please be cautious to avoid trapping your fingers in the portal.",
            "*Keep out of reach of children.",
        ).join("\n"),
        listOf(
            "妖精研究所が誇りを持ってお届けする新商品、“フェアリークエストカード”が待望の新登場！異世界や並行世界との通信や物質転送が可能な画期的な通信端末です！",
            "外出先で急に雷雨が降りだしちゃったなんて経験、一度はありませんか？これさえあれば、急な雷なんて怖くない！",
            "平行世界のストアに接続された“フェアリークエストカード”に代金を投入すれば、あとは商品のデータを受け取るだけで、内蔵の3Dプリンターが、ほら、この通り！",
            "",
            "※特殊な元素を含む物品の受け取りには、特殊元素用カートリッジ（別売り）が必要となる場合がございます。",
            "※エントロピーの低い物品の受け取りには、反エントロピー用カートリッジ（別売り）が必要となる場合がございます。",
            "※通信先との取引に伴う紛争については、当研究所は一切の責任を負いません。",
            "※ポータルに指等を挟まないようご注意ください。",
            "※お子様の手の届かない場所に保管してください。",
        ).join("\n"),
        "The Institute of Fairy Research\nCreation Department", "妖精研究所\n創製部",
        listOf({ ItemTags.COALS.toIngredientStack(1) }),
        listOf { Items.WHITE_BED.createItemStack(1) },
    ),
    VEGETATION_SURVEY(
        "vegetation_survey", 0x6BAF7C, LootCategory.RARE,
        "Vegetation Survey", "植生調査",
        "The fairy trees...? We should be over a million light-years away from the Habitabilis Zona. I'm curious to divine the past of this star, so would you consider sending me samples of the vegetation?",
        "妖精の樹…？ここはハビタビリスゾーナから100万光年以上も離れた場所のはず…。この星の過去を占ってみたいから、植生サンプルを送ってくれないかしら？",
        "The Pearl Knights of Miranagi\nShinonome Astrology Academy\nRumeri", "みらなぎ聖騎士団\n東雲占卜院\nるめり",
        listOf({ HaimeviskaBlockCard.LOG.item().toIngredientStack(4) }, { HaimeviskaBlockCard.LEAVES.item().toIngredientStack(16) }),
        listOf { MaterialCard.MIRANAGITE.item().createItemStack(1) },
    ),
    FATAL_ACCIDENT(
        "fatal_accident", 0x000027, LootCategory.RARE,
        "Fatal Accident", "重大な事故",
        """
        I pray that this message reaches some human in a parallel world.
        No, at this point, it could be fairies, malformed creatures, anything.
        A catastrophic accident has occurred, and our world is on the verge of ending.
        The vacuum decay reactor at Cordelia Branch has gone out of control, and an asteroid was swallowed by the vacuum decay.
        It's not just Cordelia.
        Europa, Haumea, and even Headquarters are unresponsive.
        Communication has ceased, and even the Divine Ear is silent.
        Anti-entropy sludge is leaking from the warp routes, and there's no telling when this place will be disintegrated to zero.
        Before being engulfed by the vacuum decay, we plan to escape into space with colonization pods.
        Damn it, the vacuum decay reactor was never something humans should have messed with!
        If someone is reading this, please understand.
        The vacuum decay reactor wasn't a safe, environmentally friendly source of energy.
        One wrong move, and it's a terrifying thing that could wipe out an entire planet.
        If anyone is in control of the vacuum decay reactor, please stop it now!
        Before your world ceases to exist!!!
        """.trimIndent().trim().replace("\n", "\n\n"),
        """
        このメッセージが平行世界の人間に届くことを祈る。
        いや、この際妖精でも奇形生物でも何でもいい。
        重大な事故が起こって俺の世界が終わろうとしている。
        コーディリア支部の真空崩壊炉が制御を失い、小惑星ごと真空崩壊に飲み込まれちまった。
        コーディリアだけじゃない。
        エウロパも、ハウメアも、本部も応答しない。
        もう通信も途絶えたし、天耳通も反応しない。
        アンチエントロピースラッジが縮地路から漏れ出て、ここもいつゼロまで分解されるか分からない。
        真空崩壊に飲み込まれる前に、俺たちは植民ポッドで宇宙に脱出するつもりだ。
        クソッ、真空崩壊炉なんて人間が手を出していい代物じゃなかったんだ！
        もし誰かがこれを見ているなら、理解してくれ。
        真空崩壊炉は安全で環境に優しいエネルギーなんかじゃなかった。
        一歩間違えば惑星ごと消し去ってしまうヤバい奴だ。
        真空崩壊炉を管理している奴がいたら、今すぐそれを止めてくれ！
        お前たちの世界がまだ生きているうちに！！！
        """.trimIndent().trim().replace("\n", "\n\n"),
        "The Institute of Fairy Research\nOphelia Branch\nLibrariania the fairy of librarian", "妖精研究所\nオフィーリア支部\n司書精リブラリアーニャ",
        listOf(
            { Items.BEDROCK.toIngredientStack(64) },
            { Items.BEDROCK.toIngredientStack(64) },
            { Items.BEDROCK.toIngredientStack(64) },
            { Items.BEDROCK.toIngredientStack(64) },
        ),
        listOf(
            { BlockMaterialCard.LOCAL_VACUUM_DECAY.item().createItemStack(64) },
            { BlockMaterialCard.LOCAL_VACUUM_DECAY.item().createItemStack(64) },
            { BlockMaterialCard.LOCAL_VACUUM_DECAY.item().createItemStack(64) },
            { BlockMaterialCard.MIRANAGITE_BLOCK.item().createItemStack(64) },
        ),
    ),
    ;

    val identifier = MirageFairy2024.identifier(path)

    val titleTranslation = Translation({ "${MirageFairy2024.MOD_ID}.fairyQuestRecipe.$path.title" }, enTitle, jaTitle)
    val messageTranslation = Translation({ "${MirageFairy2024.MOD_ID}.fairyQuestRecipe.$path.message" }, enMessage, jaMessage)
    val clientTranslation = Translation({ "${MirageFairy2024.MOD_ID}.fairyQuestRecipe.$path.client" }, enClient, jaClient)

    override val title get() = text { titleTranslation() }
    override val message get() = text { messageTranslation() }
    override val client get() = text { clientTranslation() }

    enum class LootCategory {
        NONE,
        COMMON,
        RARE,
    }
}

val SET_FAIRY_QUEST_RECIPE_LOOT_FUNCTION_TYPE = LootItemFunctionType(SetFairyQuestRecipeLootFunction.SERIALIZER)

val FAIRY_QUEST_CARD_FEATURE = FairyQuestCardFeature(NoneFeatureConfiguration.CODEC)

context(ModContext)
fun initFairyQuestRecipe() {
    FairyQuestRecipeCard.entries.forEach { card ->
        Registration(fairyQuestRecipeRegistry, card.identifier) { card }.register()

        card.titleTranslation.enJa()
        card.messageTranslation.enJa()
        card.clientTranslation.enJa()


        // 村チェストドロップ
        run {
            val allVillageChests = listOf(
                BuiltInLootTables.VILLAGE_WEAPONSMITH,
                BuiltInLootTables.VILLAGE_TOOLSMITH,
                BuiltInLootTables.VILLAGE_ARMORER,
                BuiltInLootTables.VILLAGE_CARTOGRAPHER,
                BuiltInLootTables.VILLAGE_MASON,
                BuiltInLootTables.VILLAGE_SHEPHERD,
                BuiltInLootTables.VILLAGE_BUTCHER,
                BuiltInLootTables.VILLAGE_FLETCHER,
                BuiltInLootTables.VILLAGE_FISHER,
                BuiltInLootTables.VILLAGE_TANNERY,
                BuiltInLootTables.VILLAGE_TEMPLE,
                BuiltInLootTables.VILLAGE_DESERT_HOUSE,
                BuiltInLootTables.VILLAGE_PLAINS_HOUSE,
                BuiltInLootTables.VILLAGE_TAIGA_HOUSE,
                BuiltInLootTables.VILLAGE_SNOWY_HOUSE,
                BuiltInLootTables.VILLAGE_SAVANNA_HOUSE,
            )

            fun registerChestLoot(lootTableId: ResourceKey<LootTable>, chance: Float) {
                FairyQuestCardCard.item.registerChestLoot({ lootTableId }, chance) {
                    apply { SetFairyQuestRecipeLootFunction(listOf(), card.identifier) }
                }
            }

            when (card.lootCategory) {
                FairyQuestRecipeCard.LootCategory.NONE -> Unit

                FairyQuestRecipeCard.LootCategory.COMMON -> {
                    allVillageChests.forEach {
                        registerChestLoot(it, 0.15F)
                    }
                }

                FairyQuestRecipeCard.LootCategory.RARE -> {
                    allVillageChests.forEach {
                        registerChestLoot(it, 0.05F)
                    }
                }
            }
        }

    }

    FairyQuestRecipeRecipeViewerCategoryCard.init()

    // 地形生成
    val configuredFeatureKey = registerDynamicGeneration(Registries.CONFIGURED_FEATURE, MirageFairy2024.identifier("fairy_quest_card")) {
        FAIRY_QUEST_CARD_FEATURE with NoneFeatureConfiguration.INSTANCE
    }
    val placedFeatureKey = registerDynamicGeneration(Registries.PLACED_FEATURE, MirageFairy2024.identifier("fairy_quest_card")) {
        val placementModifiers = placementModifiers { per(256) + flower(square, surface) }
        Registries.CONFIGURED_FEATURE[configuredFeatureKey] with placementModifiers
    }
    placedFeatureKey.registerFeature(GenerationStep.Decoration.VEGETAL_DECORATION) { overworld }

    Registration(BuiltInRegistries.LOOT_FUNCTION_TYPE, MirageFairy2024.identifier("set_fairy_quest_recipe")) { SET_FAIRY_QUEST_RECIPE_LOOT_FUNCTION_TYPE }.register()

    Registration(BuiltInRegistries.FEATURE, MirageFairy2024.identifier("fairy_quest_card")) { FAIRY_QUEST_CARD_FEATURE }.register()

}

class FairyQuestCardFeature(codec: Codec<NoneFeatureConfiguration>) : PlacedItemFeature<NoneFeatureConfiguration>(codec) {
    override fun getCount(context: FeaturePlaceContext<NoneFeatureConfiguration>) = 2
    override fun createItemStack(context: FeaturePlaceContext<NoneFeatureConfiguration>): ItemStack? {

        // レシピ抽選
        val table = mutableListOf<Chance<ResourceLocation>>()
        FairyQuestRecipeCard.entries.forEach { recipe ->
            when (recipe.lootCategory) {
                FairyQuestRecipeCard.LootCategory.NONE -> Unit
                FairyQuestRecipeCard.LootCategory.COMMON -> table += Chance(5.0, recipe.identifier)
                FairyQuestRecipeCard.LootCategory.RARE -> table += Chance(1.0, recipe.identifier)
            }
        }
        val recipeId = table.weightedRandom(context.random()) ?: return null // 有効なレシピが一つもない

        return FairyQuestCardCard.item().createItemStack().also { it.setFairyQuestRecipe(fairyQuestRecipeRegistry.get(recipeId)!!) }
    }
}

class SetFairyQuestRecipeLootFunction(conditions: List<LootItemCondition>, private val recipeId: ResourceLocation) : LootItemConditionalFunction(conditions) {
    companion object {
        val SERIALIZER: MapCodec<SetFairyQuestRecipeLootFunction> = RecordCodecBuilder.mapCodec { instance ->
            commonFields(instance)
                .and(ResourceLocation.CODEC.fieldOf("id").forGetter { it.recipeId })
                .apply(instance, ::SetFairyQuestRecipeLootFunction)
        }
    }

    override fun getType() = SET_FAIRY_QUEST_RECIPE_LOOT_FUNCTION_TYPE

    override fun run(stack: ItemStack, context: LootContext): ItemStack {
        stack.setFairyQuestRecipe(fairyQuestRecipeRegistry.get(recipeId)!!)
        return stack
    }
}

object FairyQuestRecipeRecipeViewerCategoryCard : RecipeViewerCategoryCard<FairyQuestRecipe>() {
    override fun getId() = MirageFairy2024.identifier("fairy_quest_recipe")
    override fun getName() = EnJa("Fairy Quest", "フェアリークエスト")
    override fun getIcon() = FairyQuestCardCard.item().createItemStack().also { it.setFairyQuestRecipe(FairyQuestRecipeCard.NEW_PRODUCT_FROM_FRI) }
    override fun getWorkstations() = listOf(MaterialCard.FAIRY_QUEST_CARD_BASE.item().createItemStack())
    override fun getRecipeCodec(registryAccess: RegistryAccess): Codec<FairyQuestRecipe> = fairyQuestRecipeRegistry.byNameCodec()
    override fun getInputs(recipeEntry: RecipeEntry<FairyQuestRecipe>): List<Input> {
        return listOf(
            Input(FairyQuestCardCard.item().createItemStack().also { it.setFairyQuestRecipe(recipeEntry.recipe) }.toIngredientStack(), true),
            *recipeEntry.recipe.inputs.map { input -> Input(input(), false) }.toTypedArray<Input>(),
        )
    }

    override fun getOutputs(recipeEntry: RecipeEntry<FairyQuestRecipe>) = recipeEntry.recipe.outputs.map { it() }

    override fun createRecipeEntries(): Iterable<RecipeEntry<FairyQuestRecipe>> {
        return fairyQuestRecipeRegistry.entrySet().map { (id, recipe) ->
            RecipeEntry(id.location(), recipe, true)
        }
    }

    override fun createView(recipeEntry: RecipeEntry<FairyQuestRecipe>) = View {
        this += YListView {
            minWidth = 120
            this += XListView {
                this += CatalystSlotView(FairyQuestCardCard.item().createItemStack().also { it.setFairyQuestRecipe(recipeEntry.recipe) }.toIngredientStack()).noBackground()
                this += XSpaceView(4)
                this += Alignment.CENTER to TextView(recipeEntry.recipe.title).apply {
                    color = ColorPair.DARK_GRAY
                    shadow = false
                }
            }
            this += YSpaceView(2)
            this += Alignment.CENTER to XListView {
                recipeEntry.recipe.inputs.forEach {
                    this += InputSlotView(it())
                }
                this += XSpaceView(2)
                this += ArrowView().apply {
                    durationMilliSeconds = recipeEntry.recipe.duration * 50
                }
                this += XSpaceView(2)
                recipeEntry.recipe.outputs.forEach {
                    this += OutputSlotView(it())
                }
            }
        }
    }
}
