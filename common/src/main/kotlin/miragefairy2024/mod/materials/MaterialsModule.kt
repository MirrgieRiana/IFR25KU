package miragefairy2024.mod.materials

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.ModEvents
import miragefairy2024.mod.FoodIngredientCategoryCard
import miragefairy2024.mod.FoodIngredients
import miragefairy2024.mod.FoodIngredientsRegistry
import miragefairy2024.mod.ItemTagCard
import miragefairy2024.mod.PoemList
import miragefairy2024.mod.PoemType
import miragefairy2024.mod.WaterBottleIngredient
import miragefairy2024.mod.description
import miragefairy2024.mod.entity.ChaosCubeCard
import miragefairy2024.mod.experienceStatusEffect
import miragefairy2024.mod.fairy.FairyCard
import miragefairy2024.mod.fairy.MotifCard
import miragefairy2024.mod.fairy.RandomFairySummoningItem
import miragefairy2024.mod.fairy.SOUL_STREAM_CONTAINABLE_TAG
import miragefairy2024.mod.fairy.createFairyItemStack
import miragefairy2024.mod.fairy.getFairyCondensation
import miragefairy2024.mod.fairy.getFairyMotif
import miragefairy2024.mod.haimeviska.haimeviskaAdvancement
import miragefairy2024.mod.machine.AthanorRecipeCard
import miragefairy2024.mod.machine.AuraReflectorFurnaceCard
import miragefairy2024.mod.machine.AuraReflectorFurnaceRecipe
import miragefairy2024.mod.machine.AuraReflectorFurnaceRecipeCard
import miragefairy2024.mod.machine.FermentationBarrelCard
import miragefairy2024.mod.machine.FermentationBarrelRecipeCard
import miragefairy2024.mod.machine.registerSimpleMachineRecipeGeneration
import miragefairy2024.mod.magicplant.contents.magicplants.DiamondLuminariaCard
import miragefairy2024.mod.magicplant.contents.magicplants.MerrrriaCard
import miragefairy2024.mod.magicplant.contents.magicplants.MirageFlowerCard
import miragefairy2024.mod.magicplant.contents.magicplants.PhantomFlowerCard
import miragefairy2024.mod.magicplant.contents.magicplants.ProminariaCard
import miragefairy2024.mod.magicplant.contents.magicplants.XarpaLuminariaCard
import miragefairy2024.mod.materials.contents.ApostleWandItem
import miragefairy2024.mod.materials.contents.DrinkItem
import miragefairy2024.mod.materials.contents.MinaItem
import miragefairy2024.mod.mirageFairy2024ItemGroupCard
import miragefairy2024.mod.plus
import miragefairy2024.mod.poem
import miragefairy2024.mod.registerPoem
import miragefairy2024.mod.registerPoemGeneration
import miragefairy2024.mod.rootAdvancement
import miragefairy2024.mod.structure.WeatheredAncientRemnantsCard
import miragefairy2024.mod.translation
import miragefairy2024.util.AdvancementCard
import miragefairy2024.util.AdvancementCardType
import miragefairy2024.util.EnJa
import miragefairy2024.util.Registration
import miragefairy2024.util.ResourceLocation
import miragefairy2024.util.SpecialRecipeResult
import miragefairy2024.util.Translation
import miragefairy2024.util.createItemStack
import miragefairy2024.util.enJa
import miragefairy2024.util.from
import miragefairy2024.util.generator
import miragefairy2024.util.isIn
import miragefairy2024.util.isNotEmpty
import miragefairy2024.util.modId
import miragefairy2024.util.on
import miragefairy2024.util.pull
import miragefairy2024.util.register
import miragefairy2024.util.registerBlastingRecipeGeneration
import miragefairy2024.util.registerChestLoot
import miragefairy2024.util.registerChild
import miragefairy2024.util.registerComposterInput
import miragefairy2024.util.registerCompressionRecipeGeneration
import miragefairy2024.util.registerExtraOreDrop
import miragefairy2024.util.registerFuel
import miragefairy2024.util.registerGeneratedModelGeneration
import miragefairy2024.util.registerGrassDrop
import miragefairy2024.util.registerItemGroup
import miragefairy2024.util.registerMobDrop
import miragefairy2024.util.registerShapedRecipeGeneration
import miragefairy2024.util.registerShapelessRecipeGeneration
import miragefairy2024.util.registerSinglePoolChestLoot
import miragefairy2024.util.registerSmeltingRecipeGeneration
import miragefairy2024.util.registerSpecialRecipe
import miragefairy2024.util.toIngredient
import miragefairy2024.util.toIngredientStack
import miragefairy2024.util.toItemTag
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.tags.ItemTags
import net.minecraft.tags.TagKey
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.entity.EntityType
import net.minecraft.world.food.FoodProperties
import net.minecraft.world.item.Item
import net.minecraft.world.item.Items
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.storage.loot.BuiltInLootTables
import java.math.BigInteger
import kotlin.math.pow

class MaterialCard(
    path: String,
    val enName: String,
    val jaName: String,
    val poemList: PoemList?,
    val fuelValue: Int? = null,
    val soulStreamContainable: Boolean = false,
    val fireResistant: Boolean = false,
    val foodComponentCreator: (suspend () -> FoodProperties)? = null,
    val recipeRemainder: Item? = null,
    val tags: List<TagKey<Item>>? = null,
    val ore: Ore? = null,
    val creator: (Item.Properties) -> Item = ::Item,
    val advancementCreator: (MaterialCard.() -> AdvancementCard)? = null,
    val initializer: context(ModContext) MaterialCard.() -> Unit = {},
) {
    companion object {
        val entries = mutableListOf<MaterialCard>()
        private operator fun MaterialCard.not() = apply { entries += this }

        val XARPITE: MaterialCard = !MaterialCard(
            "xarpite", "Xarpite", "紅天石",
            PoemList(2).poem("Binds astral flux with magnetic force", "黒鉄の鎖は繋がれる。血腥い魂の檻へ。"),
            fuelValue = 200 * 16, ore = Ore(Shape.GEM, Material.XARPITE),
            advancementCreator = {
                AdvancementCard(
                    identifier = identifier,
                    context = AdvancementCard.Sub { rootAdvancement.await() },
                    icon = { item().createItemStack() },
                    name = EnJa("Aura-Resistant Plastic", "耐霊性プラスチック"),
                    description = EnJa("Pick up the Xarpite lying around nearby", "その辺に落ちている紅天石を拾う"),
                    criterion = AdvancementCard.hasItem(item),
                    type = AdvancementCardType.TOAST_AND_JEWELS,
                )
            },
            // TODO 使えるワード：牢獄
        ) {
            item.registerGrassDrop(0.03F, 1) // TODO 古代の遺構
            item.registerMobDrop(EntityType.WITCH, onlyKilledByPlayer = true, dropRate = Pair(0.2F, 0.1F))
        }
        val MIRANAGITE: MaterialCard = !MaterialCard(
            "miranagite", "Miranagite", "蒼天石",
            PoemList(2).poem("Astral body crystallized by anti-entropy", "秩序の叛乱、天地創造の逆光。"),
            ore = Ore(Shape.GEM, Material.MIRANAGITE),
            advancementCreator = {
                AdvancementCard(
                    identifier = identifier,
                    context = AdvancementCard.Sub { rootAdvancement.await() },
                    icon = { item().createItemStack() },
                    name = EnJa("The Unknown World of Magic", "魔法の世界"),
                    description = EnJa("Mine the Miranagite underground", "地中の蒼天石を採掘する"),
                    criterion = AdvancementCard.hasItem(item),
                    type = AdvancementCardType.TOAST_AND_JEWELS,
                )
            },
            // TODO The origin of the universe 無限の深淵、破壊と再生の輪廻。
        )
        val MIRANAGITE_ROD: MaterialCard = !MaterialCard(
            "miranagite_rod", "Miranagite Rod", "蒼天石の棒",
            PoemList(2).poem("Mana flows well through the core", "蒼天に従える光条は、魔力の祝福を示す。"),
            ore = Ore(Shape.ROD, Material.MIRANAGITE),
        ) {
            registerShapedRecipeGeneration(item) {
                pattern("  #")
                pattern(" # ")
                pattern("#  ")
                define('#', MIRANAGITE.ore!!.tag)
            } on MIRANAGITE.ore!!.tag from MIRANAGITE.item
        }

        // TODO ポエム: エントロピーの極小点
        // TODO ポエム: 予定調和の予定調和
        val CHAOS_STONE: MaterialCard = !MaterialCard(
            "chaos_stone", "Chaos Stone", "混沌の石",
            PoemList(4).poem("Chemical promoting catalyst", "魔力の暴走、加速する無秩序の流れ。"),
            ore = Ore(Shape.GEM, Material.CHAOS_STONE),
            advancementCreator = {
                AdvancementCard(
                    identifier = identifier,
                    context = AdvancementCard.Sub { WeatheredAncientRemnantsCard.advancement.await() },
                    icon = { item().createItemStack() },
                    name = EnJa("The World of Science", "知られざる科学の世界"),
                    description = EnJa("Obtain Chaos Stone from Weathered Ancient Remnants and other locations around the world", "風化した旧世代の遺構やその他の世界中の場所にある混沌の石を入手する"),
                    criterion = AdvancementCard.hasItem(item),
                    type = AdvancementCardType.NORMAL,
                )
            },
        ) {
            item.registerChestLoot({ BuiltInLootTables.SIMPLE_DUNGEON }, 0.1F, 3..5)
            item.registerChestLoot({ BuiltInLootTables.ABANDONED_MINESHAFT }, 0.1F, 3..5)
            item.registerChestLoot({ BuiltInLootTables.ANCIENT_CITY }, 0.2F, 3..5)
            item.registerChestLoot({ BuiltInLootTables.DESERT_PYRAMID }, 0.5F, 3..5)
            item.registerChestLoot({ BuiltInLootTables.VILLAGE_DESERT_HOUSE }, 0.1F, 3..5)
            item.registerSinglePoolChestLoot({ BuiltInLootTables.DESERT_PYRAMID_ARCHAEOLOGY }, 1)
            item.registerSinglePoolChestLoot({ BuiltInLootTables.DESERT_WELL_ARCHAEOLOGY }, 1)
        }

        val NOISE: MaterialCard = !MaterialCard(
            "noise", "Noise", "ノイズ",
            PoemList(5).poem("An object no one sympathizes with.", "新しい「自由」の形。"), // TODO ポエム: かしましい
            ore = Ore(Shape.GEM, Material.NOISE), soulStreamContainable = true,
            advancementCreator = {
                AdvancementCard(
                    identifier = identifier,
                    context = AdvancementCard.Sub { CALCULITE.advancement!!.await() },
                    icon = { item().createItemStack() },
                    name = EnJa("The Essence of KU", "KUの本質"),
                    description = EnJa("Produce Noise from Calculite using a Aura Reflector Furnace", "オーラ反射炉で理天石からノイズを作る"),
                    criterion = AdvancementCard.hasItem(item),
                    type = AdvancementCardType.GOAL,
                )
            },
        ) {
            registerSimpleMachineRecipeGeneration(
                AuraReflectorFurnaceRecipeCard,
                inputs = listOf(
                    { CALCULITE.item().toIngredientStack(1) },
                    { FAIRY_SCALES.item().toIngredientStack(8) },
                    { POISON.item().toIngredientStack(1) },
                ),
                outputs = listOf({ item().createItemStack() }),
                duration = 20 * 10,
            ) on CALCULITE.item
        }

        val MIRAGE_LEAVES: MaterialCard = !MaterialCard(
            "mirage_leaves", "Mirage Leaves", "ミラージュの葉",
            PoemList(1).poem("Don't cut your fingers!", "刻まれる、記憶の破片。"),
            fuelValue = 100,
        ) {
            item.registerComposterInput(0.5F)
        }
        val MIRAGE_STEM: MaterialCard = !MaterialCard(
            "mirage_stem", "Mirage Stem", "ミラージュの茎",
            PoemList(1).poem("Cell wall composed of amorphous ether", "植物が手掛ける、分子レベルの硝子細工。"),
            fuelValue = 100,
        ) {
            registerShapelessRecipeGeneration(item) {
                requires(MIRAGE_LEAVES.item())
            } on MIRAGE_LEAVES.item
            item.registerComposterInput(0.5F)
            registerShapedRecipeGeneration({ Items.STICK }, 2) {
                pattern("#")
                pattern("#")
                define('#', item())
            } on item modId MirageFairy2024.MOD_ID from item
        }
        val FAIRY_GLASS_FIBER: MaterialCard = !MaterialCard(
            "fairy_glass_fiber", "Fairy Glass Fiber", "きらめきの糸",
            PoemList(1).poem("Fiber-optic nervous system", "意識の一部だったもの。"),
            soulStreamContainable = true,
        ) {
            registerShapedRecipeGeneration(item) {
                pattern("###")
                pattern("# #")
                pattern("###")
                define('#', MIRAGE_STEM.item())
            } on MIRAGE_STEM.item
            registerShapedRecipeGeneration({ Items.STRING }) {
                pattern("##")
                pattern("##")
                define('#', item())
            } on item modId MirageFairy2024.MOD_ID from item
        }
        val FAIRY_CRYSTAL: MaterialCard = !MaterialCard(
            "fairy_crystal", "Fairy Crystal", "フェアリークリスタル",
            PoemList(2).poem("Crystallized soul", "生物を生物たらしめるもの"),
            ore = Ore(Shape.GEM, Material.FAIRY_CRYSTAL),
            soulStreamContainable = true,
            advancementCreator = {
                AdvancementCard(
                    identifier = identifier,
                    context = AdvancementCard.Sub { MirageFlowerCard.advancement!!.await() },
                    icon = { item().createItemStack() },
                    name = EnJa("Organic Amorphous Material", "水晶の飴"),
                    description = EnJa("Cultivate Mirage flowers and harvest a rare item", "妖花ミラージュを栽培し希少品を収穫する"),
                    criterion = AdvancementCard.hasItem(item),
                    type = AdvancementCardType.NORMAL,
                )
            },
        )
        val FAIRY_POT: MaterialCard = !MaterialCard(
            "fairy_pot", "Fairy Pot", "妖精のポット",
            PoemList(2).poem("Nanoporous insulation", "とろけるアステリア。"),
            soulStreamContainable = true,
        ) {
            registerShapedRecipeGeneration(item) {
                pattern("# #")
                pattern(" # ")
                define('#', FAIRY_CRYSTAL.ore!!.ingredient)
            } on FAIRY_CRYSTAL.ore!!.tag
        }
        val PHANTOM_LEAVES: MaterialCard = !MaterialCard(
            "phantom_leaves", "Phantom Leaves", "ファントムの葉",
            PoemList(3).poem("The eroding reality", "析出する空想。"),
            fuelValue = 100,
        ) {
            item.registerComposterInput(0.5F)
        }
        val PHANTOM_DROP: MaterialCard = !MaterialCard(
            "phantom_drop", "Phantom Drop", "幻想の雫",
            PoemList(4).poem("Beyond the end of the world", "祈りを形に、再生の蜜。"),
            soulStreamContainable = true, ore = Ore(Shape.GEM, Material.PHANTOM_DROP),
            foodComponentCreator = {
                FoodProperties.Builder()
                    .nutrition(2)
                    .saturationModifier(0.3F)
                    .effect(MobEffectInstance(MobEffects.REGENERATION, 20 * 60), 1.0F)
                    .alwaysEdible()
                    .build()
            },
            advancementCreator = {
                AdvancementCard(
                    identifier = identifier,
                    context = AdvancementCard.Sub { PhantomFlowerCard.advancement!!.await() },
                    icon = { item().createItemStack() },
                    name = EnJa("Materialized Fantasy", "植物が想像できることは植物が実現する"),
                    description = EnJa("Obtain Phantom Drop, rarely harvested from the Phantom Flower", "幻花ファントムから稀に収穫できる幻想の雫を入手する"),
                    criterion = AdvancementCard.hasItem(item),
                    type = AdvancementCardType.NORMAL,
                )
            },
        )
        val MIRAGIUM_NUGGET: MaterialCard = !MaterialCard(
            "miragium_nugget", "Miragium Nugget", "ミラジウムナゲット",
            PoemList(3).poem("Dismembered metallic body", "小分けにされた妖精のインゴット。"),
            soulStreamContainable = true, ore = Ore(Shape.NUGGET, Material.MIRAGIUM),
        ) {
            registerSimpleMachineRecipeGeneration(
                AuraReflectorFurnaceRecipeCard,
                inputs = listOf(
                    { MIRAGE_FLOUR.item().toIngredientStack(1) },
                ),
                outputs = listOf({ item().createItemStack() }),
                duration = 20 * 60,
            ) on MIRAGE_FLOUR.item
        }
        val MIRAGIUM_INGOT: MaterialCard = !MaterialCard(
            "miragium_ingot", "Miragium Ingot", "ミラジウムインゴット",
            PoemList(3).poem("Metallic body", "妖精インゴット。"),
            soulStreamContainable = true, ore = Ore(Shape.INGOT, Material.MIRAGIUM),
            advancementCreator = {
                AdvancementCard(
                    identifier = identifier,
                    context = AdvancementCard.Sub { AuraReflectorFurnaceCard.advancement!!.await() },
                    icon = { item().createItemStack() },
                    name = EnJa("Solid Soul", "固形の魂"), // TODO 魂塊
                    description = EnJa("Use the Aura Reflector Furnace to refine Mirage Flour", "オーラ反射炉を使ってミラージュの花粉を製錬する"),
                    criterion = AdvancementCard.hasItem(item),
                    type = AdvancementCardType.NORMAL,
                )
            },
        )
        val LILAGIUM_INGOT: MaterialCard = !MaterialCard(
            "lilagium_ingot", "Lilagium Ingot", "リラジウムインゴット",
            PoemList(3).poem("Ethereal plant-attractant polysaccharide", "セルロースの精霊よ、エーテルの道を開け。"),
            soulStreamContainable = true, ore = Ore(Shape.INGOT, Material.LILAGIUM),
            advancementCreator = {
                AdvancementCard(
                    identifier = identifier,
                    context = AdvancementCard.Sub { MIRAGIUM_INGOT.advancement!!.await() },
                    icon = { item().createItemStack() },
                    name = EnJa("Alloy with Plants", "植物との合金"),
                    description = EnJa("Create Lilagium using Miragium, lilac, and other materials", "ミラジウム、ライラックおよびその他の素材からリラジウムを作る"),
                    criterion = AdvancementCard.hasItem(item),
                    type = AdvancementCardType.GOAL,
                )
            },
        ) {
            registerSimpleMachineRecipeGeneration(
                AuraReflectorFurnaceRecipeCard,
                inputs = listOf(
                    { MIRAGIUM_INGOT.item().toIngredientStack(1) },
                    { Items.LILAC.toIngredientStack(4) },
                    { Items.PEONY.toIngredientStack(4) },
                ),
                outputs = listOf({ item().createItemStack() }),
                duration = 20 * 60,
            ) on { Items.LILAC }
        } // TODO "Botanical alloy", "牡丹合金。"
        val MIRAGIDIAN_SHARD: MaterialCard = !MaterialCard(
            "miragidian_shard", "Miragidian Shard", "ミラジディアンの欠片",
            PoemList(4).poem("The great collapse 30,000 years ago", "遥か三万年前のミラジウムが見た夢。"),
            soulStreamContainable = true, fireResistant = true, ore = Ore(Shape.SHARD, Material.MIRAGIDIAN),
        ) {
            registerSimpleMachineRecipeGeneration(
                AuraReflectorFurnaceRecipeCard,
                inputs = listOf(
                    { item().toIngredientStack(9) },
                ),
                outputs = listOf({ MIRAGIDIAN.item().createItemStack() }),
                duration = 20 * 60,
            ) on ore!!.tag from item
        }
        val MIRAGIDIAN: MaterialCard = !MaterialCard(
            "miragidian", "Miragidian", "ミラジディアン",
            PoemList(4).poem("A fantasy world told by tungsten", "タングステンが語る幻想世界。"),
            soulStreamContainable = true, fireResistant = true, ore = Ore(Shape.GEM, Material.MIRAGIDIAN),
            advancementCreator = {
                AdvancementCard(
                    identifier = identifier,
                    context = AdvancementCard.Sub { ChaosCubeCard.advancement.await() },
                    icon = { item().createItemStack() },
                    name = EnJa("Ancient Stainless Alloy", "古代のステンレス"),
                    description = EnJa("Process Etheroballistic Bolt Fragments and sinter them using an Aura Reflector Furnace", "エテロバリスティック弾の破片を加工し、オーラ反射炉で焼結する"),
                    criterion = AdvancementCard.hasItem(item),
                    type = AdvancementCardType.NORMAL,
                )
            },
        )
        val ETHEROBALLISTIC_BOLT_FRAGMENT: MaterialCard = !MaterialCard(
            "etheroballistic_bolt_fragment", "Etheroballistic Bolt Fragment", "エテロバリスティック弾の破片",
            PoemList(4).poem("More abrasion resistant than lethal", "合金として生きるということ。"),
            soulStreamContainable = true, fireResistant = true,
        ) {
            registerSmeltingRecipeGeneration(item, MIRAGIDIAN_SHARD.item) on item from item
        }
        val VEROPEDA_LEAF: MaterialCard = !MaterialCard(
            "veropeda_leaf", "Veropeda Leaf", "ヴェロペダの葉",
            PoemList(1).poem("Said to house the soul of a demon", "その身融かされるまでの快楽。"),
            fuelValue = 100,
        ) {
            item.registerComposterInput(0.5F)
            registerSmeltingRecipeGeneration(item, { Items.IRON_NUGGET }, 0.1) on item modId MirageFairy2024.MOD_ID from item
            registerBlastingRecipeGeneration(item, { Items.IRON_NUGGET }, 0.1) on item modId MirageFairy2024.MOD_ID from item
        }
        val VEROPEDA_BERRIES: MaterialCard = !MaterialCard(
            "veropeda_berries", "Veropeda Berries", "ヴェロペダの実",
            PoemList(1)
                .poem("Has analgesic and stimulant effects", "悪魔の囁きを喰らう。")
                .description("Healing and rare nausea by eating", "食べると回復、まれに吐き気"),
            foodComponentCreator = {
                FoodProperties.Builder()
                    .nutrition(1)
                    .saturationModifier(0.1F)
                    .fast()
                    .effect(MobEffectInstance(MobEffects.REGENERATION, 20 * 3), 1.0F)
                    .effect(MobEffectInstance(MobEffects.CONFUSION, 20 * 20), 0.01F)
                    .build()
            },
        ) {
            item.registerComposterInput(0.3F)
        }
        val SARRACENIA_LEAF: MaterialCard = !MaterialCard(
            "sarracenia_leaf", "Sarracenia Leaf", "サラセニアの葉",
            PoemList(1).poem("Fruity and sweet and sour.", "とけた果肉と蜜の味。"),
            fuelValue = 100,
        ) {
            item.registerComposterInput(0.5F)
            registerShapelessRecipeGeneration({ Items.SUGAR }) {
                requires(item)
            } on item modId MirageFairy2024.MOD_ID from item
            // TODO 酸を回収する手段
        }
        val LUMINITE: MaterialCard = !MaterialCard(
            "luminite", "Luminite", "ルミナイト",
            PoemList(4).poem("An end point of reincarnation", "彷徨える魂の行方。"),
            ore = Ore(Shape.GEM, Material.LUMINITE),
            advancementCreator = {
                AdvancementCard(
                    identifier = identifier,
                    context = AdvancementCard.Sub { DiamondLuminariaCard.advancement!!.await() },
                    icon = { item().createItemStack() },
                    name = EnJa("Etheroluminescence", "エテロルミネッセンス"),
                    description = EnJa("Obtain Luminite, a rare item from plants of Luminariaceae family", "ルミナリア科植物のレアドロップであるルミナイトを手に入れる"),
                    criterion = AdvancementCard.hasItem(item),
                    type = AdvancementCardType.NORMAL,
                )
            },
        )
        val RESONITE_INGOT: MaterialCard = !MaterialCard(
            "resonite_ingot", "Resonite", "共鳴石",
            PoemList(5).poem("Synchronized sound and light", "同調する魂の波動。"),
            soulStreamContainable = true, ore = Ore(Shape.INGOT, Material.RESONITE),
            advancementCreator = {
                AdvancementCard(
                    identifier = identifier,
                    context = AdvancementCard.Sub { LUMINITE.advancement!!.await() },
                    icon = { item().createItemStack() },
                    name = EnJa("Ambivalence in Glass", "ガラスの中のアンビバレンス"),
                    description = EnJa("Craft a Resonite using an Aura Reflector Furnace with Fairy Crystal, Luminite, and Echo Shard", "フェアリークリスタル、ルミナイト、および残響の欠片からオーラ反射炉を使って共鳴石を製作する"),
                    criterion = AdvancementCard.hasItem(item),
                    type = AdvancementCardType.NORMAL,
                )
            },
        ) {
            registerSimpleMachineRecipeGeneration(
                AuraReflectorFurnaceRecipeCard,
                inputs = listOf(
                    { FAIRY_CRYSTAL.item().toIngredientStack(1) },
                    { LUMINITE.item().toIngredientStack(1) },
                    { Items.ECHO_SHARD.toIngredientStack(1) },
                ),
                outputs = listOf({ item().createItemStack() }),
                duration = 20 * 60,
            ) on LUMINITE.ore!!.tag
        }
        val CALCULITE: MaterialCard = !MaterialCard(
            "calculite", "Calculite", "理天石", // TODO ポエム: An of The Superphysical Society of Xarpa
            PoemList(5).poem("Class 4 time evolution rule", "時の模様を刻む石。"), // TODO ポエム: Neutralization of anti-entropy
            ore = Ore(Shape.GEM, Material.CALCULITE),
            advancementCreator = {
                AdvancementCard(
                    identifier = identifier,
                    context = AdvancementCard.Sub { XarpaLuminariaCard.advancement!!.await() },
                    icon = { item().createItemStack() },
                    name = EnJa("Edge of Chaos", "混沌の縁"),
                    description = EnJa("Obtain Calculite from the special drop of Xarpie Luminara", "シャルピエ・ルミナーラの特殊品ドロップから理天石を手に入れる"),
                    criterion = AdvancementCard.hasItem(item),
                    type = AdvancementCardType.NORMAL,
                )
            },
        )
        val PROMINARIA_BERRY: MaterialCard = !MaterialCard(
            "prominaria_berry", "Prominaria Berry", "プロミナリアの実",
            PoemList(3)
                .poem("Guardian flame of lost souls.", "心頭滅却のプロミネンス。")
                .description("Grants fire resistance when eaten", "食べると火炎耐性を付与"),
            fireResistant = true, fuelValue = 200,
            foodComponentCreator = {
                FoodProperties.Builder()
                    .nutrition(1)
                    .saturationModifier(0.1F)
                    .fast()
                    .alwaysEdible()
                    .effect(MobEffectInstance(MobEffects.FIRE_RESISTANCE, 20 * 5), 1.0F)
                    .build()
            },
        ) {
            AuraReflectorFurnaceRecipe.registerFuel(item.key, 20 * 5)
            item.registerComposterInput(0.3F)
        }
        val PROMINITE: MaterialCard = !MaterialCard(
            "prominite", "Prominite", "プロミナイト",
            PoemList(4).poem("Arbitrament of randomness.", "炎になる魂、光になる魂。"), // TODO 何かで使う：熱情
            fireResistant = true, fuelValue = 200 * 16, ore = Ore(Shape.GEM, Material.PROMINITE),
            advancementCreator = {
                AdvancementCard(
                    identifier = identifier,
                    context = AdvancementCard.Sub { ProminariaCard.advancement!!.await() },
                    icon = { item().createItemStack() },
                    name = EnJa("Equality before Physical Law", "物理法則の下の平等"),
                    description = EnJa("Harvest Prominite from Prominaria", "ネザーのプロミナリアからプロミナイトを収穫する"),
                    criterion = AdvancementCard.hasItem(item),
                    type = AdvancementCardType.NORMAL,
                )
            },
        ) {
            AuraReflectorFurnaceRecipe.registerFuel(item.key, 20 * 60)
        }
        val GOLD_PROMINARIA_BERRY: MaterialCard = !MaterialCard(
            "gold_prominaria_berry", "Gold Prominaria Berry", "ゴールドプロミナリアの実",
            PoemList(3)
                .poem("Gold-plated prominence.", "摂氏100度の金環食。")
                .description("Grants absorption when eaten", "食べると衝撃吸収を付与"),
            fireResistant = true, fuelValue = 200,
            foodComponentCreator = {
                FoodProperties.Builder()
                    .nutrition(1)
                    .saturationModifier(0.1F)
                    .fast()
                    .alwaysEdible()
                    .effect(MobEffectInstance(MobEffects.ABSORPTION, 20 * 120), 1.0F)
                    .build()
            },
            tags = listOf(ItemTags.PIGLIN_LOVED),
        ) {
            AuraReflectorFurnaceRecipe.registerFuel(item.key, 20 * 5)
            registerSmeltingRecipeGeneration(item, { Items.GOLD_NUGGET }, 0.1) on item modId MirageFairy2024.MOD_ID from item
            registerBlastingRecipeGeneration(item, { Items.GOLD_NUGGET }, 0.1) on item modId MirageFairy2024.MOD_ID from item
            item.registerComposterInput(0.3F)
        }
        val MERRRRIA_DROP: MaterialCard = !MaterialCard(
            "merrrria_drop", "Merrrria Drop", "月のしずく",
            PoemList(3)
                .poem("Tales of latex that charm fairies.", "闇夜に響く、月鈴の詩。")
                .description("Grants night vision when eaten", "食べると暗視を付与"),
            foodComponentCreator = {
                FoodProperties.Builder()
                    .nutrition(2)
                    .saturationModifier(0.3F)
                    .effect(MobEffectInstance(MobEffects.NIGHT_VISION, 20 * 30), 1.0F)
                    .alwaysEdible()
                    .build()
            },
            advancementCreator = {
                AdvancementCard(
                    identifier = identifier,
                    context = AdvancementCard.Sub { MerrrriaCard.advancement!!.await() },
                    icon = { item().createItemStack() },
                    name = EnJa("Nocturnal Nocturne", "真夜中だけのノクターン"),
                    description = EnJa("Harvest a drop from Merrrria", "月鈴花メルルルリアから月のしずくを収穫する"),
                    criterion = AdvancementCard.hasItem(item),
                    type = AdvancementCardType.NORMAL,
                )
            },
        )
        val HAIMEVISKA_SAP: MaterialCard = !MaterialCard(
            "haimeviska_sap", "Haimeviska Sap", "ハイメヴィスカの樹液",
            PoemList(1)
                .poem("Smooth and mellow on the palate", "口福のアナムネシス。")
                .description("Gain experience by eating", "食べると経験値を獲得"),
            fuelValue = 200,
            foodComponentCreator = {
                FoodProperties.Builder()
                    .nutrition(1)
                    .saturationModifier(0.1F)
                    .effect(MobEffectInstance(experienceStatusEffect.awaitHolder(), 10), 1.0F)
                    .build()
            },
        ) {
            // →松明
            registerShapedRecipeGeneration({ Items.TORCH }) {
                pattern("#")
                pattern("S")
                define('#', item())
                define('S', tagOf(Shape.ROD, Material.WOOD))
            } on item modId MirageFairy2024.MOD_ID from item
        }
        val HAIMEVISKA_ROSIN: MaterialCard = !MaterialCard(
            "haimeviska_rosin", "Haimeviska Rosin", "ハイメヴィスカの涙",
            PoemList(2).poem("High-friction material", "琥珀の月が昇るとき、妖精の木は静かに泣く"),
            fuelValue = 200, ore = Ore(Shape.GEM, Material.HAIMEVISKA_ROSIN),
            advancementCreator = {
                AdvancementCard(
                    identifier = identifier,
                    context = AdvancementCard.Sub { haimeviskaAdvancement.await() },
                    icon = { item().createItemStack() },
                    name = EnJa("The Taste of Nectar", "蜜の味"),
                    description = EnJa("Obtain Haimeviska Rosin, rarely harvested from the Dripping Haimeviska Logs", "滴るハイメヴィスカの原木から稀に採取されるハイメヴィスカの涙を入手する"),
                    criterion = AdvancementCard.hasItem(item),
                    type = AdvancementCardType.NORMAL,
                )
            },
        ) {
            // →粘着ピストン
            registerShapedRecipeGeneration({ Blocks.STICKY_PISTON.asItem() }) {
                pattern("S")
                pattern("P")
                define('P', Blocks.PISTON)
                define('S', ore!!.tag)
            } on ore!!.tag modId MirageFairy2024.MOD_ID from item
            // →リード
            registerShapedRecipeGeneration({ Items.LEAD }) {
                pattern("ss ")
                pattern("s# ")
                pattern("  s")
                define('#', ore!!.tag)
                define('s', Items.STRING)
            } on ore!!.tag modId MirageFairy2024.MOD_ID from item
            // →スライムボール
            registerShapedRecipeGeneration({ Items.SLIME_BALL }) {
                pattern("sss")
                pattern("s#s")
                pattern("sss")
                define('s', HAIMEVISKA_SAP.item())
                define('#', ore!!.tag)
            } on ore!!.tag modId MirageFairy2024.MOD_ID from item
        }
        val FAIRY_PLASTIC: MaterialCard = !MaterialCard(
            // TODO add recipe
            // TODO add purpose
            "fairy_plastic", "Fairy Plastic", "妖精のプラスチック",
            PoemList(4).poem("Thermoplastic organic polymer", "凍てつく記憶の宿る石。"),
            fuelValue = 200 * 8, ore = Ore(Shape.GEM, Material.FAIRY_PLASTIC),
            // TODO advancement
            // 琥珀色の～～
        )
        val FAIRY_RUBBER: MaterialCard = !MaterialCard(
            // TODO add purpose
            "fairy_rubber", "Fairy Rubber", "夜のかけら",
            PoemList(3).poem("Minimize the risk of losing belongings", "空は怯える夜精に一握りの温かい闇を与えた"),
            ore = Ore(Shape.GEM, Material.FAIRY_RUBBER),
        )

        val TINY_MIRAGE_FLOUR: MaterialCard = !MaterialCard(
            "tiny_mirage_flour", "Tiny Pile of Mirage Flour", "小さなミラージュの花粉",
            PoemList(1).poem("Compose the body of Mirage fairy", "ささやかな温もりを、てのひらの上に。"),
            soulStreamContainable = true,
            tags = listOf(MIRAGE_FLOUR_TAG),
            creator = { RandomFairySummoningItem(9.0.pow(-1.0), it) },
        )
        val MIRAGE_FLOUR: MaterialCard = !MaterialCard(
            "mirage_flour", "Mirage Flour", "ミラージュの花粉",
            PoemList(1).poem("Containing metallic organic matter", "叡智の根源、創発のファンタジア。"),
            soulStreamContainable = true,
            tags = listOf(MIRAGE_FLOUR_TAG),
            creator = { RandomFairySummoningItem(9.0.pow(0.0), it) },
        )
        val MIRAGE_FLOUR_OF_NATURE: MaterialCard = !MaterialCard(
            "mirage_flour_of_nature", "Mirage Flour of Nature", "自然のミラージュの花粉",
            PoemList(1).poem("Use the difference in ether resistance", "艶やかなほたる色に煌めく鱗粉。"),
            soulStreamContainable = true,
            tags = listOf(MIRAGE_FLOUR_TAG),
            creator = { RandomFairySummoningItem(9.0.pow(1.0), it) },
        )
        val MIRAGE_FLOUR_OF_EARTH: MaterialCard = !MaterialCard(
            "mirage_flour_of_earth", "Mirage Flour of Earth", "大地のミラージュの花粉",
            PoemList(2).poem("As intelligent as humans", "黄金の魂が示す、好奇心の輝き。"),
            soulStreamContainable = true,
            tags = listOf(MIRAGE_FLOUR_TAG),
            creator = { RandomFairySummoningItem(9.0.pow(2.0), it) },
        )
        val MIRAGE_FLOUR_OF_UNDERWORLD: MaterialCard = !MaterialCard(
            "mirage_flour_of_underworld", "Mirage Flour of Underworld", "地底のミラージュの花粉",
            PoemList(2).poem("Awaken fairies in the world and below", "1,300ケルビンの夜景。"),
            soulStreamContainable = true,
            tags = listOf(MIRAGE_FLOUR_TAG),
            creator = { RandomFairySummoningItem(9.0.pow(3.0), it) },
        )
        val MIRAGE_FLOUR_OF_SKY: MaterialCard = !MaterialCard(
            "mirage_flour_of_sky", "Mirage Flour of Sky", "天空のミラージュの花粉",
            PoemList(3).poem("Explore atmosphere and nearby universe", "蒼淵を彷徨う影、導きの光。"),
            soulStreamContainable = true,
            tags = listOf(MIRAGE_FLOUR_TAG),
            creator = { RandomFairySummoningItem(9.0.pow(4.0), it) },
        )
        val MIRAGE_FLOUR_OF_UNIVERSE: MaterialCard = !MaterialCard(
            "mirage_flour_of_universe", "Mirage Flour of Universe", "宇宙のミラージュの花粉",
            PoemList(3)
                .poem("poem1", "Leap spaces by collapsing time crystals,", "運命の束、時の結晶、光速の呪いを退けよ、")
                .poem("poem2", "capture ether beyond observable universe", "讃えよ、アーカーシャに眠る自由の頂きを。"),
            soulStreamContainable = true,
            tags = listOf(MIRAGE_FLOUR_TAG),
            creator = { RandomFairySummoningItem(9.0.pow(5.0), it) },
            advancementCreator = {
                AdvancementCard(
                    identifier = identifier,
                    context = AdvancementCard.Sub { MirageFlowerCard.advancement!!.await() },
                    icon = { item().createItemStack() },
                    name = EnJa("Warping Space", "ゆがむ空間"),
                    description = EnJa("Condense Mirage Flour into Mirage Flour of Universe", "ミラージュの花粉を宇宙のミラージュの花粉まで濃縮する"),
                    criterion = AdvancementCard.hasItem(item),
                    type = AdvancementCardType.CHALLENGE,
                )
            }
        )
        val MIRAGE_FLOUR_OF_TIME: MaterialCard = !MaterialCard(
            "mirage_flour_of_time", "Mirage Flour of Time", "時空のミラージュの花粉",
            PoemList(4)
                .poem("poem1", "Attracts nearby parallel worlds outside", "虚空に眠る時の断片。因果の光が貫くとき、")
                .poem("poem2", "this universe and collects their ether.", "亡失の世界は探し始める。無慈悲な真実を。"),
            soulStreamContainable = true,
            tags = listOf(MIRAGE_FLOUR_TAG),
            creator = { RandomFairySummoningItem(9.0.pow(6.0), it) },
        )

        val FAIRY_SCALES: MaterialCard = !MaterialCard(
            "fairy_scales", "Fairy Scales", "妖精の鱗粉",
            PoemList(1)
                .poem("A catalyst that converts mana into erg", "湧き上がる、エルグの誘い。"),
            soulStreamContainable = true,
            // TODO レシピ 妖精の森バイオームの雑草
            // TODO 妖精からクラフト
            // TODO 用途
        ) {
            item.registerGrassDrop(0.1F, 1)
        }
        val FRACTAL_WISP: MaterialCard = !MaterialCard(
            "fractal_wisp", "Fractal Wisp", "フラクタルウィスプ",
            PoemList(1)
                .poem("poem1", "The fairy of the fairy of the fairy", "妖精の妖精の妖精の妖精の妖精の妖精の妖精")
                .poem("poem2", "of the fairy of the fairy of the f", "の妖精の妖精の妖精の妖精の妖精の妖精の妖"),
            soulStreamContainable = true, fireResistant = true,
            // TODO 用途
        )

        val FAIRY_QUEST_CARD_BASE: MaterialCard = !MaterialCard(
            "fairy_quest_card_base", "Fairy Quest Card Base", "フェアリークエストカードベース",
            PoemList(1).poem("Am I hopeful in the parallel world?", "存在したかもしれない僕たちのかたち。")
        )

        val ASH: MaterialCard = !MaterialCard(
            "ash", "Ash", "灰",
            null,
            ore = Ore(Shape.DUST, Material.ASH),
        ) {
            registerSmeltingRecipeGeneration(BlockMaterialCard.MIRAGE_LEAVES_BLOCK.item, item) on BlockMaterialCard.MIRAGE_LEAVES_BLOCK.item from BlockMaterialCard.MIRAGE_LEAVES_BLOCK.item
        }
        val SALTPETER: MaterialCard = !MaterialCard(
            "saltpeter", "Saltpeter", "硝石",
            null,
            ore = Ore(Shape.GEM, Material.SALTPETER),
        ) {
            fun register(inputItem1: () -> Item, inputCount1: Int, inputItem2: () -> Item, inputCount2: Int) {
                registerSimpleMachineRecipeGeneration(
                    AthanorRecipeCard,
                    inputs = listOf(
                        { inputItem1().toIngredientStack(inputCount1) },
                        { inputItem2().toIngredientStack(inputCount2) },
                    ),
                    outputs = listOf({ item().createItemStack() }),
                    duration = 20 * 60 * 5,
                ) on inputItem1 from inputItem1 from inputItem2
            }
            register(ASH.item, 4, { Items.ROTTEN_FLESH }, 4)
            register(ASH.item, 4, { Items.FERMENTED_SPIDER_EYE }, 2)
            register({ Items.DRIED_KELP }, 8, { Items.ROTTEN_FLESH }, 4)
            register({ Items.DRIED_KELP }, 8, { Items.FERMENTED_SPIDER_EYE }, 2)

            // →火薬
            registerShapelessRecipeGeneration({ Items.GUNPOWDER }, 9) {
                repeat(6) {
                    requires(item())
                }
                repeat(2) {
                    requires(ItemTags.COALS)
                }
                requires(SULFUR.item())
            } on item from item modId MirageFairy2024.MOD_ID
        }

        val MAGNETITE: MaterialCard = !MaterialCard(
            "magnetite", "Magnetite", "磁鉄鉱",
            null,
            ore = Ore(Shape.GEM, Material.MAGNETITE),
        ) {
            registerSmeltingRecipeGeneration(item, { Items.IRON_NUGGET }, 0.7) on ore!!.tag modId MirageFairy2024.MOD_ID from item
        }

        val FLUORITE: MaterialCard = !MaterialCard(
            "fluorite", "Fluorite", "蛍石",
            null,
            ore = Ore(Shape.GEM, Material.FLUORITE),
        )
        val SULFUR: MaterialCard = !MaterialCard(
            "sulfur", "Sulfur", "硫黄",
            null,
            ore = Ore(Shape.GEM, Material.SULFUR),
        )
        val SPHERE_BASE: MaterialCard = !MaterialCard(
            "sphere_base", "Sphere Base", "スフィアベース",
            PoemList(2)
                .poem("A mirror that reflects sadistic desires", "前世が見える。              （らしい）"),
            // TODO 用途
        ) {
            registerShapedRecipeGeneration(item) {
                pattern(" S ")
                pattern("SFS")
                pattern(" S ")
                define('F', FLUORITE.ore!!.tag)
                define('S', FAIRY_SCALES.item())
            } on FLUORITE.ore!!.tag from FLUORITE.item
        }
        val NEPHRITE: MaterialCard = !MaterialCard(
            "nephrite", "Nephrite", "ネフライト",
            null,
            ore = Ore(Shape.GEM, Material.NEPHRITE),
        )
        val TOPAZ: MaterialCard = !MaterialCard(
            "topaz", "Topaz", "トパーズ",
            null,
            ore = Ore(Shape.GEM, Material.TOPAZ),
        )

        val TINY_BISMUTH_DUST: MaterialCard = !MaterialCard(
            "tiny_bismuth_dust", "Tiny Pile of Bismuth Dust", "小さなビスマスの粉",
            null,
            ore = Ore(Shape.TINY_DUST, Material.BISMUTH),
        ) {
            item.registerExtraOreDrop(Blocks.COPPER_ORE, fortuneMultiplier = 1)
            item.registerExtraOreDrop(Blocks.DEEPSLATE_COPPER_ORE, fortuneMultiplier = 1)
        }
        val BISMUTH_DUST: MaterialCard = !MaterialCard(
            "bismuth_dust", "Bismuth Dust", "ビスマスの粉",
            null,
            ore = Ore(Shape.DUST, Material.BISMUTH),
        )
        val BISMUTH_INGOT: MaterialCard = !MaterialCard(
            "bismuth_ingot", "Bismuth Ingot", "ビスマスインゴット",
            null,
            ore = Ore(Shape.INGOT, Material.BISMUTH),
        ) {
            registerSmeltingRecipeGeneration(BISMUTH_DUST.item, item) on BISMUTH_DUST.item from BISMUTH_DUST.item
        }

        val MINA_1: MaterialCard = !MaterialCard(
            "mina_1", "1 Mina", "1ミナ",
            PoemList(null)
                .poem("Put this money to work until I come back", "私が帰って来るまでこれで商売をしなさい")
                .translation(PoemType.DESCRIPTION, MINA_DESCRIPTION_TRANSLATION),
            soulStreamContainable = true, fireResistant = true,
            creator = { MinaItem(BigInteger.ONE, it) },
        )
        val MINA_5: MaterialCard = !MaterialCard(
            "mina_5", "5 Mina", "5ミナ",
            PoemList(null)
                .poem("Fairy snack", "ご縁があるよ")
                .translation(PoemType.DESCRIPTION, MINA_DESCRIPTION_TRANSLATION),
            soulStreamContainable = true, fireResistant = true,
            creator = { MinaItem(5.toBigInteger(), it) },
        )
        val MINA_10: MaterialCard = !MaterialCard(
            "mina_10", "10 Mina", "10ミナ",
            PoemList(null)
                .poem("Can purchase the souls of ten fairies.", "10の妖精が宿る石。")
                .translation(PoemType.DESCRIPTION, MINA_DESCRIPTION_TRANSLATION),
            soulStreamContainable = true, fireResistant = true,
            creator = { MinaItem(BigInteger.TEN, it) },
        )
        val MINA_50: MaterialCard = !MaterialCard(
            "mina_50", "50 Mina", "50ミナ",
            PoemList(null)
                .poem("The Society failed to replicate this.", "形而上学的有機結晶")
                .translation(PoemType.DESCRIPTION, MINA_DESCRIPTION_TRANSLATION),
            soulStreamContainable = true, fireResistant = true,
            creator = { MinaItem(50.toBigInteger(), it) },
        )
        val MINA_100: MaterialCard = !MaterialCard(
            "mina_100", "100 Mina", "100ミナ",
            PoemList(null)
                .poem("Place where fairies and humans intersect", "妖精と人間が交差する場所。")
                .translation(PoemType.DESCRIPTION, MINA_DESCRIPTION_TRANSLATION),
            soulStreamContainable = true, fireResistant = true,
            creator = { MinaItem(100.toBigInteger(), it) },
        )
        val MINA_500: MaterialCard = !MaterialCard(
            "mina_500", "500 Mina", "500ミナ",
            PoemList(null)
                .poem("A brilliance with a hardness of 7.5", "硬度7.5の輝き。")
                .translation(PoemType.DESCRIPTION, MINA_DESCRIPTION_TRANSLATION),
            soulStreamContainable = true, fireResistant = true,
            creator = { MinaItem(500.toBigInteger(), it) },
        )
        val MINA_1000: MaterialCard = !MaterialCard(
            "mina_1000", "1000 Mina", "1000ミナ",
            PoemList(null)
                .poem("Created by the fairies of commerce.", "妖精の業が磨き上げる。")
                .translation(PoemType.DESCRIPTION, MINA_DESCRIPTION_TRANSLATION),
            soulStreamContainable = true, fireResistant = true,
            creator = { MinaItem(1000.toBigInteger(), it) },
        )
        val MINA_5000: MaterialCard = !MaterialCard(
            "mina_5000", "5000 Mina", "5000ミナ",
            PoemList(null)
                .poem("The price of a soul.", "魂の値段。")
                .translation(PoemType.DESCRIPTION, MINA_DESCRIPTION_TRANSLATION),
            soulStreamContainable = true, fireResistant = true,
            creator = { MinaItem(5000.toBigInteger(), it) },
        )
        val MINA_10000: MaterialCard = !MaterialCard(
            "mina_10000", "10000 Mina", "10000ミナ",
            PoemList(null)
                .poem("Become an eternal gemstone.", "妖花の蜜よ、永遠の宝石となれ。")
                .translation(PoemType.DESCRIPTION, MINA_DESCRIPTION_TRANSLATION),
            soulStreamContainable = true, fireResistant = true,
            creator = { MinaItem(10000.toBigInteger(), it) },
        )

        val JEWEL_1: MaterialCard = !MaterialCard(
            "jewel_1", "1 Fairy Jewel", "1フェアリージュエル",
            PoemList(0)
                .poem("Long ago, fairies were the nectar.", "その昔、妖精は木の蜜だった。"),
            soulStreamContainable = true, fireResistant = true,
        )
        val JEWEL_5: MaterialCard = !MaterialCard(
            "jewel_5", "5 Fairy Jewel", "5フェアリージュエル",
            PoemList(0)
                .poem("The nectar bloomed from the ground.", "木の蜜は地に触れ、花を咲かせた。"),
            soulStreamContainable = true, fireResistant = true,
        )
        val JEWEL_10: MaterialCard = !MaterialCard(
            "jewel_10", "10 Fairy Jewel", "10フェアリージュエル",
            PoemList(0)
                .poem("The wind, sky, and sun laughed.", "風と空と太陽が笑った。"),
            soulStreamContainable = true, fireResistant = true,
        )
        val JEWEL_50: MaterialCard = !MaterialCard(
            "jewel_50", "50 Fairy Jewel", "50フェアリージュエル",
            PoemList(0)
                .poem("Fairies simply drifted along.", "妖精はただ漂っていた。"),
            soulStreamContainable = true, fireResistant = true,
        )
        val JEWEL_100: MaterialCard = !MaterialCard(
            "jewel_100", "100 Fairy Jewel", "100フェアリージュエル",
            PoemList(0)
                .poem("One day, humans touched fairies.", "その日、人が現れ、妖精に触れた。"),
            soulStreamContainable = true, fireResistant = true,
        )
        val JEWEL_500: MaterialCard = !MaterialCard(
            "jewel_500", "500 Fairy Jewel", "500フェアリージュエル",
            PoemList(0)
                .poem("Fairies took form and learned emotion.", "妖精は妖精の姿へとなり、感情を知った。"),
            soulStreamContainable = true, fireResistant = true,
        )
        val JEWEL_1000: MaterialCard = !MaterialCard(
            "jewel_1000", "1000 Fairy Jewel", "1000フェアリージュエル",
            PoemList(0)
                .poem("Fairies learned joy and pain.", "妖精は悦びと痛みを知った。"),
            soulStreamContainable = true, fireResistant = true,
        )
        val JEWEL_5000: MaterialCard = !MaterialCard(
            "jewel_5000", "5000 Fairy Jewel", "5000フェアリージュエル",
            PoemList(0)
                .poem("Humans saw the fairies and felt relief.", "人は妖精を見て、安堵した。"),
            soulStreamContainable = true, fireResistant = true,
        )
        val JEWEL_10000: MaterialCard = !MaterialCard(
            "jewel_10000", "10000 Fairy Jewel", "10000フェアリージュエル",
            PoemList(0)
                .poem("Thus, humans lost their form.", "こうして、人は人の姿を失った。"),
            soulStreamContainable = true, fireResistant = true,
        )

        val APOSTLE_WAND: MaterialCard = !MaterialCard(
            "apostle_wand", "Apostle's Wand", "使徒のステッキ",
            PoemList(2).poem("The key to the fairy world", "妖精界への鍵。"),
            creator = { ApostleWandItem(it.stacksTo(1)) },
        ) {
            registerShapedRecipeGeneration(item) {
                pattern(" G")
                pattern("S ")
                define('S', MIRAGE_STEM.item())
                define('G', tagOf(Shape.INGOT, Material.GOLD))
            } on MIRAGE_STEM.item
        }

        val RUM: MaterialCard = !MaterialCard(
            "rum", "Rum", "ラム酒",
            null,
            fuelValue = 200 * 4, recipeRemainder = Items.GLASS_BOTTLE, tags = listOf(ItemTagCard.SPIRITS.tag),
            foodComponentCreator = {
                FoodProperties.Builder()
                    .nutrition(6)
                    .saturationModifier(0.1F)
                    .effect(MobEffectInstance(MobEffects.DAMAGE_BOOST, 20 * 60, 1), 1.0F)
                    .effect(MobEffectInstance(MobEffects.CONFUSION, 20 * 60), 0.1F)
                    .build()
            },
            creator = { DrinkItem(it) },
        ) {
            // TODO 蒸留装置
            registerSimpleMachineRecipeGeneration(
                FermentationBarrelRecipeCard,
                inputs = listOf(
                    { Items.GLASS_BOTTLE.toIngredientStack(1) },
                    { Items.SUGAR_CANE.toIngredientStack(16) },
                    { WaterBottleIngredient.toVanilla().toIngredientStack(1) },
                ),
                outputs = listOf({ item().createItemStack() }),
                duration = 20 * 60 * 5,
            ) on { Items.SUGAR_CANE }
            ModEvents.onInitialize {
                FoodIngredientsRegistry.registry[item()] = FoodIngredients() + FoodIngredientCategoryCard.ALCOHOL + Items.SUGAR_CANE
            }
        }
        val CIDRE: MaterialCard = !MaterialCard(
            "cidre", "Cidre", "シードル",
            null,
            recipeRemainder = Items.GLASS_BOTTLE,
            foodComponentCreator = {
                FoodProperties.Builder()
                    .nutrition(6)
                    .saturationModifier(0.1F)
                    .effect(MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20 * 60), 1.0F)
                    .build()
            },
            creator = { DrinkItem(it) },
        ) {
            registerSimpleMachineRecipeGeneration(
                FermentationBarrelRecipeCard,
                inputs = listOf(
                    { Items.GLASS_BOTTLE.toIngredientStack(1) },
                    { Items.APPLE.toIngredientStack(4) },
                    { WaterBottleIngredient.toVanilla().toIngredientStack(1) },
                ),
                outputs = listOf({ item().createItemStack() }),
                duration = 20 * 60 * 1,
            ) on { Items.APPLE }
            ModEvents.onInitialize {
                FoodIngredientsRegistry.registry[item()] = FoodIngredients() + FoodIngredientCategoryCard.ALCOHOL + Items.APPLE
            }
        }
        val FAIRY_LIQUEUR: MaterialCard = !MaterialCard(
            "fairy_liqueur", "Fairy Liqueur", "妖精のリキュール",
            PoemList(2).poem("Fairies get high, humans get burned", "妖精はハイになり、人間は火傷する。"),
            fuelValue = 200 * 12, recipeRemainder = Items.GLASS_BOTTLE, tags = listOf(ItemTagCard.SPIRITS.tag),
            foodComponentCreator = {
                FoodProperties.Builder()
                    .nutrition(6)
                    .saturationModifier(0.1F)
                    .effect(MobEffectInstance(experienceStatusEffect.awaitHolder(), 10 * 8, 1), 1.0F)
                    .build()
            },
            creator = { DrinkItem(it, flaming = 5) },
        ) {
            // TODO 醸造樽で作れるのは原酒で、リキュールはマンドレイクを使ってクラフト
            registerSimpleMachineRecipeGeneration(
                FermentationBarrelRecipeCard,
                inputs = listOf(
                    { Items.GLASS_BOTTLE.toIngredientStack(1) },
                    { HAIMEVISKA_SAP.item().toIngredientStack(8) },
                    { WaterBottleIngredient.toVanilla().toIngredientStack(1) },
                ),
                outputs = listOf({ item().createItemStack() }),
                duration = 20 * 60 * 5,
            ) on HAIMEVISKA_SAP.item
            ModEvents.onInitialize {
                FoodIngredientsRegistry.registry[item()] = FoodIngredients() + FoodIngredientCategoryCard.ALCOHOL + HAIMEVISKA_SAP.item()
            }
        }
        val VEROPEDELIQUORA: MaterialCard = !MaterialCard(
            "veropedeliquora", "Veropedeliquora", "ヴェロペデリコラ",
            PoemList(2).poem("A dark flavour from the underworld.", "冥界へといざなう、暗黒の味。"),
            fuelValue = 200 * 12, recipeRemainder = Items.GLASS_BOTTLE, tags = listOf(ItemTagCard.SPIRITS.tag),
            foodComponentCreator = {
                FoodProperties.Builder()
                    .nutrition(6)
                    .saturationModifier(0.1F)
                    .effect(MobEffectInstance(MobEffects.REGENERATION, 20 * 60), 1.0F)
                    .effect(MobEffectInstance(MobEffects.BLINDNESS, 20 * 60), 0.1F)
                    .build()
            },
            creator = { DrinkItem(it) },
        ) {
            registerSimpleMachineRecipeGeneration(
                FermentationBarrelRecipeCard,
                inputs = listOf(
                    { Items.GLASS_BOTTLE.toIngredientStack(1) },
                    { VEROPEDA_BERRIES.item().toIngredientStack(8) },
                    { WaterBottleIngredient.toVanilla().toIngredientStack(1) },
                ),
                outputs = listOf({ item().createItemStack() }),
                duration = 20 * 60 * 5,
            ) on VEROPEDA_BERRIES.item
            ModEvents.onInitialize {
                FoodIngredientsRegistry.registry[item()] = FoodIngredients() + FoodIngredientCategoryCard.ALCOHOL + VEROPEDA_BERRIES.item()
            }
        }
        val POISON: MaterialCard = !MaterialCard(
            "poison", "Poison", "毒薬",
            null,
            recipeRemainder = Items.GLASS_BOTTLE,
            foodComponentCreator = {
                FoodProperties.Builder()
                    .nutrition(1)
                    .saturationModifier(0.1F)
                    .effect(MobEffectInstance(MobEffects.HARM, 1, 9), 1.0F)
                    .effect(MobEffectInstance(MobEffects.WITHER, 20 * 60, 4), 1.0F)
                    .build()
            },
            creator = { DrinkItem(it) },
            advancementCreator = {
                AdvancementCard(
                    identifier = identifier,
                    context = AdvancementCard.Sub { FermentationBarrelCard.advancement!!.await() },
                    icon = { item().createItemStack() },
                    name = EnJa("May Contain Trace Toxic", "本品は毒物と共通の設備で製造してます"),
                    description = EnJa("Produce poisons using a Fermentation Barrel", "醸造樽で毒薬を作る"),
                    criterion = AdvancementCard.hasItem(item),
                    type = AdvancementCardType.GOAL,
                )
            },
        ) {
            registerSimpleMachineRecipeGeneration(
                FermentationBarrelRecipeCard,
                inputs = listOf(
                    { Items.GLASS_BOTTLE.toIngredientStack(1) },
                    { Items.PUFFERFISH.toIngredientStack(1) },
                    { WaterBottleIngredient.toVanilla().toIngredientStack(1) },
                ),
                outputs = listOf({ item().createItemStack() }),
                duration = 20 * 5,
            ) on { Items.PUFFERFISH } from { Items.PUFFERFISH }
            registerSimpleMachineRecipeGeneration(
                FermentationBarrelRecipeCard,
                inputs = listOf(
                    { Items.GLASS_BOTTLE.toIngredientStack(1) },
                    { Items.POISONOUS_POTATO.toIngredientStack(4) },
                    { WaterBottleIngredient.toVanilla().toIngredientStack(1) },
                ),
                outputs = listOf({ item().createItemStack() }),
                duration = 20 * 5,
            ) on { Items.POISONOUS_POTATO } from { Items.POISONOUS_POTATO }
            registerSimpleMachineRecipeGeneration(
                FermentationBarrelRecipeCard,
                inputs = listOf(
                    { Items.GLASS_BOTTLE.toIngredientStack(1) },
                    { Items.SPIDER_EYE.toIngredientStack(4) },
                    { WaterBottleIngredient.toVanilla().toIngredientStack(1) },
                ),
                outputs = listOf({ item().createItemStack() }),
                duration = 20 * 5,
            ) on { Items.SPIDER_EYE } from { Items.SPIDER_EYE }
        }
        val SOLID_FUEL: MaterialCard = !MaterialCard(
            "solid_fuel", "Solid Fuel", "固形燃料",
            PoemList(2).poem("Caramelized Ethanol", "なぜかこれを食べる妖精が続出した"),
            fuelValue = 200 * 8,
            foodComponentCreator = {
                FoodProperties.Builder()
                    .nutrition(4)
                    .saturationModifier(0.8F)
                    .effect(MobEffectInstance(MobEffects.FIRE_RESISTANCE, 20 * 40), 1.0F)
                    .build()
            },
        ) {
            // →TNT
            registerShapedRecipeGeneration({ Items.TNT }) {
                pattern(" G ")
                pattern("GFG")
                pattern(" G ")
                define('F', item)
                define('G', Items.GUNPOWDER)
            } on item from item modId MirageFairy2024.MOD_ID
        }
        val TINY_SOLID_FUEL: MaterialCard = !MaterialCard(
            "tiny_solid_fuel", "1/8 Solid Fuel", "1/8の固形燃料",
            PoemList(2).poem("Chewing Fuel", "ひとくちサイズの固形燃料"),
            fuelValue = 200,
            foodComponentCreator = {
                FoodProperties.Builder()
                    .nutrition(1)
                    .saturationModifier(0.1F)
                    .effect(MobEffectInstance(MobEffects.FIRE_RESISTANCE, 20 * 5), 1.0F)
                    .build()
            },
        ) {
            registerCompressionRecipeGeneration(item, { item().toIngredient() }, SOLID_FUEL.item, { SOLID_FUEL.item().toIngredient() }, 8)
        }
    }

    val identifier = MirageFairy2024.identifier(path)
    val item = Registration(BuiltInRegistries.ITEM, identifier) {
        Item.Properties()
            .let { foodComponentCreator?.let { c -> it.food(c()) } ?: it }
            .let { if (recipeRemainder != null) it.craftRemainder(recipeRemainder) else it }
            .let { if (fireResistant) it.fireResistant() else it }
            .let { creator(it) }
    }
    val advancement = advancementCreator?.invoke(this)
}

val MIRAGE_FLOUR_TAG = MirageFairy2024.identifier("mirage_flour").toItemTag()

val APPEARANCE_RATE_BONUS_TRANSLATION = Translation({ "item.${MirageFairy2024.identifier("mirage_flour").toLanguageKey()}.appearance_rate_bonus" }, "Appearance Rate Bonus", "出現率ボーナス")
val MINA_DESCRIPTION_TRANSLATION = Translation({ "item.${MirageFairy2024.identifier("mina").toLanguageKey()}.description" }, "Can exchange for Minia with apostle's wand", "使徒のステッキでミーニャと両替可能")

context(ModContext)
fun initMaterialsModule() {
    MaterialCard.entries.forEach { card ->
        card.item.register()
        card.item.registerItemGroup(mirageFairy2024ItemGroupCard.itemGroupKey)
        card.item.registerGeneratedModelGeneration()
        card.item.enJa(EnJa(card.enName, card.jaName))
        if (card.poemList != null) {
            card.item.registerPoem(card.poemList)
            card.item.registerPoemGeneration(card.poemList)
        }
        if (card.fuelValue != null) card.item.registerFuel(card.fuelValue)
        if (card.soulStreamContainable) SOUL_STREAM_CONTAINABLE_TAG.generator.registerChild(card.item)
        if (card.advancement != null) card.advancement.init()
        if (card.tags != null) {
            card.tags.forEach { tag ->
                tag.generator.registerChild(card.item)
            }
        }
        if (card.ore != null) {
            card.ore.tag.generator.registerChild(card.item)
            card.ore.shape.tag.generator.registerChild(card.ore.tag)
        }
        card.initializer(this@ModContext, card)
    }

    MIRAGE_FLOUR_TAG.enJa(EnJa("Mirage Flour", "ミラージュの花粉"))
    APPEARANCE_RATE_BONUS_TRANSLATION.enJa()
    MINA_DESCRIPTION_TRANSLATION.enJa()
    DrinkItem.FLAMING_TRANSLATION.enJa()

    MaterialCard.entries.mapNotNull { it.ore }.distinct().forEach {
        it.tag.enJa(it.title)
    }
    MaterialCard.entries.mapNotNull { it.ore }.distinct().map { it.shape }.distinct().forEach {
        it.tag.enJa(it.title)
    }

    // ミラジウム圧縮
    registerCompressionRecipeGeneration(MaterialCard.MIRAGIUM_NUGGET.item, { MaterialCard.MIRAGIUM_NUGGET.ore!!.ingredient }, MaterialCard.MIRAGIUM_INGOT.item, { MaterialCard.MIRAGIUM_INGOT.ore!!.ingredient })

    // ミラージュの花粉圧縮
    registerCompressionRecipeGeneration(MaterialCard.TINY_MIRAGE_FLOUR.item, { MaterialCard.TINY_MIRAGE_FLOUR.item().toIngredient() }, MaterialCard.MIRAGE_FLOUR.item, { MaterialCard.MIRAGE_FLOUR.item().toIngredient() }, noGroup = true)
    registerCompressionRecipeGeneration(MaterialCard.MIRAGE_FLOUR.item, { MaterialCard.MIRAGE_FLOUR.item().toIngredient() }, MaterialCard.MIRAGE_FLOUR_OF_NATURE.item, { MaterialCard.MIRAGE_FLOUR_OF_NATURE.item().toIngredient() }, noGroup = true)
    registerCompressionRecipeGeneration(MaterialCard.MIRAGE_FLOUR_OF_NATURE.item, { MaterialCard.MIRAGE_FLOUR_OF_NATURE.item().toIngredient() }, MaterialCard.MIRAGE_FLOUR_OF_EARTH.item, { MaterialCard.MIRAGE_FLOUR_OF_EARTH.item().toIngredient() }, noGroup = true)
    registerCompressionRecipeGeneration(MaterialCard.MIRAGE_FLOUR_OF_EARTH.item, { MaterialCard.MIRAGE_FLOUR_OF_EARTH.item().toIngredient() }, MaterialCard.MIRAGE_FLOUR_OF_UNDERWORLD.item, { MaterialCard.MIRAGE_FLOUR_OF_UNDERWORLD.item().toIngredient() }, noGroup = true)
    registerCompressionRecipeGeneration(MaterialCard.MIRAGE_FLOUR_OF_UNDERWORLD.item, { MaterialCard.MIRAGE_FLOUR_OF_UNDERWORLD.item().toIngredient() }, MaterialCard.MIRAGE_FLOUR_OF_SKY.item, { MaterialCard.MIRAGE_FLOUR_OF_SKY.item().toIngredient() }, noGroup = true)
    registerCompressionRecipeGeneration(MaterialCard.MIRAGE_FLOUR_OF_SKY.item, { MaterialCard.MIRAGE_FLOUR_OF_SKY.item().toIngredient() }, MaterialCard.MIRAGE_FLOUR_OF_UNIVERSE.item, { MaterialCard.MIRAGE_FLOUR_OF_UNIVERSE.item().toIngredient() }, noGroup = true)
    registerCompressionRecipeGeneration(MaterialCard.MIRAGE_FLOUR_OF_UNIVERSE.item, { MaterialCard.MIRAGE_FLOUR_OF_UNIVERSE.item().toIngredient() }, MaterialCard.MIRAGE_FLOUR_OF_TIME.item, { MaterialCard.MIRAGE_FLOUR_OF_TIME.item().toIngredient() }, noGroup = true)

    // ビスマスの粉圧縮
    registerCompressionRecipeGeneration(MaterialCard.TINY_BISMUTH_DUST.item, { MaterialCard.TINY_BISMUTH_DUST.ore!!.ingredient }, MaterialCard.BISMUTH_DUST.item, { MaterialCard.BISMUTH_DUST.ore!!.ingredient })

    // ミナ両替
    registerCompressionRecipeGeneration(MaterialCard.MINA_1.item, { MaterialCard.MINA_1.item().toIngredient() }, MaterialCard.MINA_5.item, { MaterialCard.MINA_5.item().toIngredient() }, 5)
    registerCompressionRecipeGeneration(MaterialCard.MINA_5.item, { MaterialCard.MINA_5.item().toIngredient() }, MaterialCard.MINA_10.item, { MaterialCard.MINA_10.item().toIngredient() }, 2)
    registerCompressionRecipeGeneration(MaterialCard.MINA_10.item, { MaterialCard.MINA_10.item().toIngredient() }, MaterialCard.MINA_50.item, { MaterialCard.MINA_50.item().toIngredient() }, 5)
    registerCompressionRecipeGeneration(MaterialCard.MINA_50.item, { MaterialCard.MINA_50.item().toIngredient() }, MaterialCard.MINA_100.item, { MaterialCard.MINA_100.item().toIngredient() }, 2)
    registerCompressionRecipeGeneration(MaterialCard.MINA_100.item, { MaterialCard.MINA_100.item().toIngredient() }, MaterialCard.MINA_500.item, { MaterialCard.MINA_500.item().toIngredient() }, 5)
    registerCompressionRecipeGeneration(MaterialCard.MINA_500.item, { MaterialCard.MINA_500.item().toIngredient() }, MaterialCard.MINA_1000.item, { MaterialCard.MINA_1000.item().toIngredient() }, 2)
    registerCompressionRecipeGeneration(MaterialCard.MINA_1000.item, { MaterialCard.MINA_1000.item().toIngredient() }, MaterialCard.MINA_5000.item, { MaterialCard.MINA_5000.item().toIngredient() }, 5)
    registerCompressionRecipeGeneration(MaterialCard.MINA_5000.item, { MaterialCard.MINA_5000.item().toIngredient() }, MaterialCard.MINA_10000.item, { MaterialCard.MINA_10000.item().toIngredient() }, 2)

    // ミーニャ⇔ミナ両替
    registerSpecialRecipe("minia_from_mina", minSlots = 1) { inventory ->
        val itemStacks = inventory.items().filter { it.isNotEmpty }.toMutableList()
        if (itemStacks.pull { it isIn MaterialCard.APOSTLE_WAND.item() } == null) return@registerSpecialRecipe null // 使徒のステッキ取得
        val itemStack = itemStacks.pull { true } ?: return@registerSpecialRecipe null // アイテム取得
        if (itemStacks.isNotEmpty()) return@registerSpecialRecipe null // 余計なアイテムが入っている
        val item = itemStack.item as? MinaItem ?: return@registerSpecialRecipe null // そのアイテムはミナでなければならない
        object : SpecialRecipeResult {
            override fun craft() = MotifCard.MINA.createFairyItemStack(condensation = item.mina)
        }
    }
    registerSpecialRecipe("mina_from_minia", minSlots = 1) { inventory ->
        val itemStacks = inventory.items().filter { it.isNotEmpty }.toMutableList()
        if (itemStacks.pull { it isIn MaterialCard.APOSTLE_WAND.item() } == null) return@registerSpecialRecipe null // 使徒のステッキ取得
        val fairyItemStack = itemStacks.pull { it isIn FairyCard.item() && it.getFairyMotif() == MotifCard.MINA } ?: return@registerSpecialRecipe null // ミーニャ取得
        if (itemStacks.isNotEmpty()) return@registerSpecialRecipe null // 余計なアイテムが入っている
        val item = when (fairyItemStack.getFairyCondensation()) {
            BigInteger.ONE -> MaterialCard.MINA_1.item()
            5.toBigInteger() -> MaterialCard.MINA_5.item()
            BigInteger.TEN -> MaterialCard.MINA_10.item()
            50.toBigInteger() -> MaterialCard.MINA_50.item()
            100.toBigInteger() -> MaterialCard.MINA_100.item()
            500.toBigInteger() -> MaterialCard.MINA_500.item()
            1000.toBigInteger() -> MaterialCard.MINA_1000.item()
            5000.toBigInteger() -> MaterialCard.MINA_5000.item()
            10000.toBigInteger() -> MaterialCard.MINA_10000.item()
            else -> return@registerSpecialRecipe null
        }
        object : SpecialRecipeResult {
            override fun craft() = item.createItemStack()
        }
    }

    // フェアリージュエル両替
    registerCompressionRecipeGeneration(MaterialCard.JEWEL_1.item, { MaterialCard.JEWEL_1.item().toIngredient() }, MaterialCard.JEWEL_5.item, { MaterialCard.JEWEL_5.item().toIngredient() }, 5)
    registerCompressionRecipeGeneration(MaterialCard.JEWEL_5.item, { MaterialCard.JEWEL_5.item().toIngredient() }, MaterialCard.JEWEL_10.item, { MaterialCard.JEWEL_10.item().toIngredient() }, 2)
    registerCompressionRecipeGeneration(MaterialCard.JEWEL_10.item, { MaterialCard.JEWEL_10.item().toIngredient() }, MaterialCard.JEWEL_50.item, { MaterialCard.JEWEL_50.item().toIngredient() }, 5)
    registerCompressionRecipeGeneration(MaterialCard.JEWEL_50.item, { MaterialCard.JEWEL_50.item().toIngredient() }, MaterialCard.JEWEL_100.item, { MaterialCard.JEWEL_100.item().toIngredient() }, 2)
    registerCompressionRecipeGeneration(MaterialCard.JEWEL_100.item, { MaterialCard.JEWEL_100.item().toIngredient() }, MaterialCard.JEWEL_500.item, { MaterialCard.JEWEL_500.item().toIngredient() }, 5)
    registerCompressionRecipeGeneration(MaterialCard.JEWEL_500.item, { MaterialCard.JEWEL_500.item().toIngredient() }, MaterialCard.JEWEL_1000.item, { MaterialCard.JEWEL_1000.item().toIngredient() }, 2)
    registerCompressionRecipeGeneration(MaterialCard.JEWEL_1000.item, { MaterialCard.JEWEL_1000.item().toIngredient() }, MaterialCard.JEWEL_5000.item, { MaterialCard.JEWEL_5000.item().toIngredient() }, 5)
    registerCompressionRecipeGeneration(MaterialCard.JEWEL_5000.item, { MaterialCard.JEWEL_5000.item().toIngredient() }, MaterialCard.JEWEL_10000.item, { MaterialCard.JEWEL_10000.item().toIngredient() }, 2)

}

data class Ore(val shape: Shape, val material: Material)

val Ore.title get() = EnJa("${this.material.prefix.en} ${this.shape.title.en}", "${this.material.prefix.ja}の${this.shape.title.ja}")
val Ore.tag get() = ResourceLocation("c", "${this.shape.path}/${this.material.path}").toItemTag()
val Ore.ingredient get() = this.tag.toIngredient()
fun tagOf(shape: Shape, material: Material) = Ore(shape, material).tag
fun ingredientOf(shape: Shape, material: Material) = Ore(shape, material).ingredient

enum class Shape(val path: String, val title: EnJa) {
    TINY_DUST("tiny_dusts", EnJa("Tiny Dusts", "微粉")),
    DUST("dusts", EnJa("Dusts", "粉")),
    NUGGET("nuggets", EnJa("Nuggets", "塊")),
    INGOT("ingots", EnJa("Ingots", "インゴット")),
    ROD("rods", EnJa("Rods", "棒")),
    GEM("gems", EnJa("Gems", "ジェム")),
    ORE("ores", EnJa("Ores", "鉱石")),
    SHARD("shards", EnJa("Shards", "欠片")),
    STORAGE_BLOCKS("storage_blocks", EnJa("Storage Blocks", "貯蔵ブロック")),

    /** 原石 */
    RAW_MATERIAL("raw_materials", EnJa("Raw Materials", "原石")),

    /** ブロック */
    STORAGE_BLOCK("storage_blocks", EnJa("Blocks", "ブロック")),
}

val Shape.tag get() = ResourceLocation("c", this.path).toItemTag()

enum class Material(val path: String, val prefix: EnJa) {
    WOOD("wooden", EnJa("Wooden", "木")),

    COPPER("copper", EnJa("Copper", "銅")),
    IRON("iron", EnJa("Iron", "鉄")),
    GOLD("gold", EnJa("Golden", "金")),
    NETHERITE("netherite", EnJa("Netherite", "ネザライト")),

    AMETHYST("amethyst", EnJa("Amethyst", "アメジスト")),
    QUARTZ("quartz", EnJa("Quartz", "クォーツ")),
    DIAMOND("diamond", EnJa("Diamond", "ダイヤモンド")),
    EMERALD("emerald", EnJa("Emerald", "エメラルド")),

    FLINT("flint", EnJa("Flint", "火打石")),
    COAL("coal", EnJa("Coal", "石炭")),
    LAPIS("lapis", EnJa("Lapis Lazuli", "ラピスラズリ")),
    PRISMARINE("prismarine", EnJa("Prismarine", "プリズマリン")),
    REDSTONE("redstone", EnJa("Redstone", "レッドストーン")),
    GLOWSTONE("glowstone", EnJa("Glowstone", "グロウストーン")),

    XARPITE("xarpite", EnJa("Xarpite", "紅天石")),
    MIRANAGITE("miranagite", EnJa("Miranagite", "蒼天石")),
    CHAOS_STONE("chaos_stone", EnJa("Chaos Stone", "混沌の石")),
    NOISE("noise", EnJa("Noise", "ノイズ")),
    FAIRY_CRYSTAL("fairy_crystal", EnJa("Fairy Crystal", "フェアリークリスタル")),
    PHANTOM_DROP("phantom_drop", EnJa("Phantom Drop", "幻想の雫")),
    MIRAGIUM("miragium", EnJa("Miragium", "ミラジウム")),
    LILAGIUM("lilagium", EnJa("Lilagium", "リラジウム")),
    MIRAGIDIAN("miragidian", EnJa("Miragidian", "ミラジディアン")),
    LUMINITE("luminite", EnJa("Luminite", "ルミナイト")),
    CALCULITE("calculite", EnJa("Calculite", "理天石")),
    RESONITE("resonite", EnJa("Resonite", "共鳴石")),
    PROMINITE("prominite", EnJa("Prominite", "プロミナイト")),
    HAIMEVISKA_ROSIN("haimeviska_rosin", EnJa("Haimeviska Rosin", "ハイメヴィスカの涙")),
    FAIRY_PLASTIC("fairy_plastic", EnJa("Fairy Plastic", "妖精のプラスチック")),
    FAIRY_RUBBER("fairy_rubber", EnJa("Fairy Rubber", "夜のかけら")),
    ASH("ash", EnJa("Ash", "灰")),
    SALTPETER("saltpeter", EnJa("Saltpeter", "硝石")),
    MAGNETITE("magnetite", EnJa("Magnetite", "磁鉄鉱")),
    FLUORITE("fluorite", EnJa("Fluorite", "蛍石")),
    SULFUR("sulfur", EnJa("Sulfur", "硫黄")),
    NEPHRITE("nephrite", EnJa("Nephrite", "ネフライト")),
    TOPAZ("topaz", EnJa("Topaz", "トパーズ")),
    BISMUTH("bismuth", EnJa("Bismuth", "ビスマス")),
}
