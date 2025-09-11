package miragefairy2024.mod.fairy

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.ModEvents
import miragefairy2024.mod.BiomeCards
import miragefairy2024.mod.Emoji
import miragefairy2024.mod.FairyStatueFountainBlock
import miragefairy2024.mod.FoodIngredientCategoryCard
import miragefairy2024.mod.ItemTagCard
import miragefairy2024.mod.OreCard
import miragefairy2024.mod.haimeviska.HaimeviskaBlockCard
import miragefairy2024.mod.magicplant.contents.magicplants.MirageFlowerCard
import miragefairy2024.mod.magicplant.contents.magicplants.PhantomFlowerCard
import miragefairy2024.mod.magicplant.contents.magicplants.VeropedaCard
import miragefairy2024.mod.materials.BlockMaterialCard
import miragefairy2024.mod.materials.MaterialCard
import miragefairy2024.mod.passiveskill.PassiveSkillCondition
import miragefairy2024.mod.passiveskill.PassiveSkillEffect
import miragefairy2024.mod.passiveskill.PassiveSkillSpecification
import miragefairy2024.mod.passiveskill.conditions.CategoryFoodIngredientPassiveSkillCondition
import miragefairy2024.mod.passiveskill.conditions.DoubleComparisonPassiveSkillCondition
import miragefairy2024.mod.passiveskill.conditions.IntComparisonPassiveSkillCondition
import miragefairy2024.mod.passiveskill.conditions.ItemFoodIngredientPassiveSkillCondition
import miragefairy2024.mod.passiveskill.conditions.SimplePassiveSkillConditionCard
import miragefairy2024.mod.passiveskill.conditions.StatusEffectPassiveSkillCondition
import miragefairy2024.mod.passiveskill.conditions.ToolMaterialCardPassiveSkillCondition
import miragefairy2024.mod.passiveskill.effects.CollectionPassiveSkillEffect
import miragefairy2024.mod.passiveskill.effects.ElementPassiveSkillEffect
import miragefairy2024.mod.passiveskill.effects.EntityAttributePassiveSkillEffect
import miragefairy2024.mod.passiveskill.effects.ExperiencePassiveSkillEffect
import miragefairy2024.mod.passiveskill.effects.HungerPassiveSkillEffect
import miragefairy2024.mod.passiveskill.effects.IgnitionPassiveSkillEffect
import miragefairy2024.mod.passiveskill.effects.ManaBoostPassiveSkillEffect
import miragefairy2024.mod.passiveskill.effects.MendingPassiveSkillEffect
import miragefairy2024.mod.passiveskill.effects.MiningSpeedPassiveSkillEffect
import miragefairy2024.mod.passiveskill.effects.RegenerationPassiveSkillEffect
import miragefairy2024.mod.passiveskill.effects.StatusEffectPassiveSkillEffect
import miragefairy2024.mod.tool.ToolMaterialCard
import miragefairy2024.util.Registration
import miragefairy2024.util.Translation
import miragefairy2024.util.enJa
import miragefairy2024.util.invoke
import miragefairy2024.util.register
import miragefairy2024.util.registerClientDebugItem
import miragefairy2024.util.text
import miragefairy2024.util.toTextureSource
import miragefairy2024.util.writeAction
import mirrg.kotlin.helium.join
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder
import net.fabricmc.fabric.api.event.registry.RegistryAttribute
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBiomeTags
import net.minecraft.core.Holder
import net.minecraft.core.Registry
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.BlockTags
import net.minecraft.tags.ItemTags
import net.minecraft.tags.TagKey
import net.minecraft.world.effect.MobEffect
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.ai.attributes.Attribute
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.item.Item
import net.minecraft.world.item.Items
import net.minecraft.world.level.biome.Biome
import net.minecraft.world.level.biome.Biomes
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks

val motifRegistryKey: ResourceKey<Registry<Motif>> = ResourceKey.createRegistryKey(MirageFairy2024.identifier("motif"))
val motifRegistry: Registry<Motif> = FabricRegistryBuilder.createSimple(motifRegistryKey).attribute(RegistryAttribute.SYNCED).buildAndRegister()

fun Motif.getIdentifier() = motifRegistry.getKey(this)
fun ResourceLocation.toFairyMotif() = motifRegistry.get(this)

interface Motif {
    val displayName: Component
    val skinColor: Int
    val frontColor: Int
    val backColor: Int
    val hairColor: Int
    val rare: Int
    val parents: List<Motif>
    val passiveSkillSpecifications: List<PassiveSkillSpecification<*>>
}


@Suppress("SpellCheckingInspection")
enum class MotifCard(
    path: String,
    override val rare: Int,
    enName: String,
    jaName: String,
    override val skinColor: Int,
    override val frontColor: Int,
    override val backColor: Int,
    override val hairColor: Int,
    private val parentMotifs: ParentMotifs,
    passiveSkillBuilder: PassiveSkillBuilder,
    val recipes: MotifCardRecipes,
) : Motif {

    // アイリャ
    AIR(
        "air", 0, "Airia", "空気精アイリャ", 0xFFBE80, 0xDEFFFF, 0xDEFFFF, 0xB0FFFF,
        ParentMotifs(),
        PassiveSkillBuilder()
            + speed(1.0),
        MotifCardRecipes().R.always.block { Blocks.AIR },
    ),

    // 通常でない物質
    LIGHT(
        "light", 3, "Lightia", "光精リグチャ", 0xFFFFD8, 0xFFFFD8, 0xFFFFC5, 0xFFFF00,
        ParentMotifs(),
        PassiveSkillBuilder()
            + speed(0.4) * light.atLeast(15)
            + speed(0.4) * light.atLeast(10)
            + speed(0.4) * light.atLeast(5),
        MotifCardRecipes().SR.overworld,
    ),
    ANTIMATTER(
        "antimatter", 12, "Antimatteria", "反物質精アンティマッテーリャ", 0x00ABC2, 0x190063, 0x190063, 0x9800C2,
        ParentMotifs(),
        PassiveSkillBuilder()
            + miningSpeed(1.0),
        MotifCardRecipes().SR.end,
    ),
    VACUUM_DECAY(
        "vacuum_decay", 13, "Vacuume Decia", "真空崩壊精ヴァツーメデーツャ", 0x00003B, 0x000012, 0x000012, 0x000078,
        ParentMotifs(),
        PassiveSkillBuilder()
            + MobEffects.DAMAGE_BOOST(2)
            + overall.attack(0.4)
            + miningSpeed(0.3)
            + MobEffects.WITHER() // TODO 真空浸蝕：死ぬまで徐々にダメージ、近接攻撃時に感染
            + MobEffects.UNLUCK(3), // TODO MOBドロップを減らす効果
        MotifCardRecipes().SR.end.block(BlockMaterialCard.LOCAL_VACUUM_DECAY.block),
    ),

    // 天体
    STAR(
        "star", 10, "Staria", "星精スターリャ", 0xffffff, 0x2C2C2E, 0x0E0E10, 0x191919,
        ParentMotifs(),
        PassiveSkillBuilder()
            + overall.attack(1.0) * overworld * fine * skyVisible
            + mana(5.0) { STAR },
        MotifCardRecipes().SR.overworld,
    ),
    MOON(
        "moon", 10, "Moonia", "月精モーニャ", 0xD9E4FF, 0x747D93, 0x0C121F, 0x2D4272,
        ParentMotifs() + { STAR },
        PassiveSkillBuilder()
            + magic.attack(2.0) * overworld * night * fine * skyVisible
            + regeneration(1.0) * overworld * night * fine * skyVisible,
        MotifCardRecipes().SR.overworld,
    ),
    SUN(
        "sun", 10, "Sunia", "太陽精スーニャ", 0xff2f00, 0xff972b, 0xff7500, 0xffe7b2,
        ParentMotifs() + { STAR } + { FIRE },
        PassiveSkillBuilder()
            + melee.attack(2.0) * overworld * daytime * fine * skyVisible
            + regeneration(1.0) * overworld * daytime * fine * skyVisible,
        MotifCardRecipes().SR.overworld,
    ),

    // プラズマ
    FIRE(
        "fire", 2, "Firia", "火精フィーリャ", 0xFF6C01, 0xF9DFA4, 0xFF7324, 0xFF4000,
        ParentMotifs(),
        PassiveSkillBuilder()
            + overall.attack(1.5) * onFire
            + fire.defence(2.0 * 2.5) * onFire
            + ignition * health.atLeast(6.0)
            + mana(2.0) { FIRE },
        MotifCardRecipes().R.nether.block { Blocks.FIRE },
    ),

    // 液体
    WATER(
        "water", 1, "Wateria", "水精ワテーリャ", 0x5469F2, 0x5985FF, 0x172AD3, 0x2D40F4,
        ParentMotifs() + { FOOD }, // TODO サブ親
        PassiveSkillBuilder()
            + overall.attack(0.5) * underwater
            + overall.defence(0.5) * underwater
            + regeneration(1.0) * underwater
            + mana(2.0) { WATER }, // TODO ネザー以外で消火効果
        MotifCardRecipes().R.overworld.block { Blocks.WATER },
    ),
    ETHANOL(
        "ethanol", 6, "Ethanolia", "酒精エタノーリャ", 0xDE805B, 0xDB5E2C, 0xDB5E2C, 0xD14711,
        ParentMotifs(),
        PassiveSkillBuilder()
            + overall.attack(0.8) * food(FoodIngredientCategoryCard.ALCOHOL)
            + mana(5.0) { ETHANOL },
        MotifCardRecipes().SR,
    ),
    LAVA(
        "lava", 4, "Lavia", "溶岩精ラーヴャ", 0xCD4208, 0xEDB54A, 0xCC4108, 0x4C1500,
        ParentMotifs() + { FIRE },
        PassiveSkillBuilder()
            + melee.attack(0.8) * onFire
            + magic.attack(0.8) * onFire
            + mana(0.4) { FIRE },
        MotifCardRecipes().R.nether.block { Blocks.LAVA },
    ),

    // 土砂
    DIRT(
        "dirt", 1, "Dirtia", "土精ディルチャ", 0xB87440, 0xB9855C, 0x593D29, 0x914A18,
        ParentMotifs(),
        PassiveSkillBuilder()
            + health(0.8) * overworld
            + miningSpeed(0.4) * overworld,
        MotifCardRecipes().R.overworld + BlockTags.DIRT,
    ),
    GRAVEL(
        "gravel", 3, "Gravelia", "砂利精グラヴェーリャ", 0x333333, 0xC0B5B6, 0x968B8E, 0x63565C,
        ParentMotifs(),
        PassiveSkillBuilder()
            + overall.defence(0.2)
            + melee.defence(0.4)
            + miningSpeed(0.7)
            + miningSpeed(0.4) * food { Items.GRAVEL }, // TODO 該当するアイテム
        MotifCardRecipes().R.overworld.nether.block { Blocks.GRAVEL },
    ),
    MYCELIUM(
        "mycelium", 7, "Myceliumia", "菌糸精ミツェリウミャ", 0x8F7E86, 0x8B7071, 0x8B7071, 0x8B6264,
        ParentMotifs() + { DIRT } + { MUSHROOM },
        PassiveSkillBuilder()
            + health(0.3)
            + regeneration(0.3) * light.atMost(12)
            + magic.attack(0.3) * light.atMost(12)
            + miningSpeed(0.3) * light.atMost(12)
            + mana(2.0) { MUSHROOM },
        MotifCardRecipes().R.common(ConventionalBiomeTags.IS_MUSHROOM).block { Blocks.MYCELIUM },
    ),
    SCULK(
        "sculk", 8, "Sculkia", "幽匿塊精スツルキャ", 0x19222C, 0x023F3D, 0x023F3D, 0x19C0C0,
        ParentMotifs(),
        PassiveSkillBuilder()
            + regeneration(0.4) * light.atMost(5)
            + overall.attack(0.4) * light.atMost(5)
            + magic.attack(0.4) * indoor
            + miningSpeed(0.4) * indoor,
        MotifCardRecipes().R.common(Biomes.DEEP_DARK).block { Blocks.SCULK },
    ),

    // 岩石
    STONE(
        "stone", 2, "Stonia", "石精ストーニャ", 0x333333, 0x8F8F8F, 0x686868, 0x747474,
        ParentMotifs(),
        PassiveSkillBuilder()
            + overall.defence(0.8)
            + miningSpeed(0.4)
            + MobEffects.DAMAGE_RESISTANCE() * ToolMaterialCard.STONE()
            + MobEffects.DAMAGE_RESISTANCE(2) * ToolMaterialCard.STONE() * fairyLevel.atLeast(14.0),
        MotifCardRecipes().R.overworld.block { Blocks.STONE },
    ),
    DRIPSTONE(
        "dripstone", 5, "Dripstonia", "鍾乳石精ドリプストーニャ", 0xB19C7E, 0xA97F6F, 0xA97F6F, 0xAD7069,
        ParentMotifs() + { STONE },
        PassiveSkillBuilder()
            + shooting.attack(0.8)
            + overall.defence(0.3)
            + miningSpeed(0.2),
        MotifCardRecipes().R.common(Biomes.DRIPSTONE_CAVES).block { Blocks.DRIPSTONE_BLOCK }.block { Blocks.POINTED_DRIPSTONE },
    ),
    DEEPSLATE(
        "deepslate", 5, "Deepslatia", "深層岩精デープスラーチャ", 0x74747A, 0x54545C, 0x54545C, 0x212129,
        ParentMotifs() + { STONE },
        PassiveSkillBuilder()
            + melee.defence(1.0)
            + miningSpeed(0.5),
        MotifCardRecipes().R.overworld.block { Blocks.DEEPSLATE },
    ),
    NETHERRACK(
        "netherrack", 7, "Netherrackia", "地獄岩精ネテッラッキャ", 0x9B5C5C, 0x703131, 0x703131, 0x8E1111,
        ParentMotifs() + { FIRE },
        PassiveSkillBuilder()
            + overall.defence(0.1)
            + fire.defence(2.0 * 0.5)
            + miningSpeed(0.2)
            + overall.attack(0.2) * onFire,
        MotifCardRecipes().R.nether.block { Blocks.NETHERRACK },
    ),

    // 金属
    COPPER(
        "copper", 3, "Copperia", "銅精ツォッペーリャ", 0xF69D7F, 0xF77653, 0xF77653, 0x5DC09A,
        ParentMotifs() + { THUNDER },
        PassiveSkillBuilder()
            + luck(0.6)
            + overall.defence(0.6)
            + MobEffects.DAMAGE_RESISTANCE() * ToolMaterialCard.COPPER() // TODO 魔法？電気？にちなんだステータス効果
            + MobEffects.DAMAGE_RESISTANCE(2) * ToolMaterialCard.COPPER() * fairyLevel.atLeast(10.0),
        MotifCardRecipes().R.block { Blocks.COPPER_BLOCK }.item { Items.COPPER_INGOT } + BlockTags.COPPER_ORES,
    ),
    IRON(
        "iron", 4, "Ironia", "鉄精イローニャ", 0xA0A0A0, 0xD8D8D8, 0x727272, 0xD8AF93,
        ParentMotifs(),
        PassiveSkillBuilder()
            + melee.attack(0.6)
            + miningSpeed(0.5)
            + MobEffects.DAMAGE_BOOST() * ToolMaterialCard.IRON()
            + MobEffects.DAMAGE_BOOST(2) * ToolMaterialCard.IRON() * fairyLevel.atLeast(10.0),
        MotifCardRecipes().R.block { Blocks.IRON_BLOCK }.item { Items.IRON_INGOT } + BlockTags.IRON_ORES,
    ),
    GOLD(
        "gold", 6, "Goldia", "金精ゴルジャ", 0xEFE642, 0xF4CC17, 0xF4CC17, 0xFDB61E,
        ParentMotifs(),
        PassiveSkillBuilder()
            + luck(0.8)
            + miningSpeed(0.6)
            + MobEffects.LUCK() * ToolMaterialCard.GOLD()
            + MobEffects.LUCK(2) * ToolMaterialCard.GOLD() * fairyLevel.atLeast(12.0),
        MotifCardRecipes().R.block { Blocks.GOLD_BLOCK }.item { Items.GOLD_INGOT } + BlockTags.GOLD_ORES,
    ),
    NETHERITE(
        "netherite", 9, "Netheritia", "地獄合金精ネテリーチャ", 0x8F788F, 0x74585B, 0x705558, 0x77302D,
        ParentMotifs() + { FIRE },
        PassiveSkillBuilder()
            + melee.attack(0.6)
            + luck(0.4)
            + MobEffects.FIRE_RESISTANCE() * ToolMaterialCard.NETHERITE()
            + MobEffects.DAMAGE_BOOST(2) * ToolMaterialCard.NETHERITE() * fairyLevel.atLeast(16.0),
        MotifCardRecipes().R.block { Blocks.NETHERITE_BLOCK }.item { Items.NETHERITE_INGOT },
    ),

    // 鉱物
    OBSIDIAN(
        "obsidian", 5, "Obsidiania", "黒耀石精オブシディアーニャ", 0x775599, 0x6029B3, 0x2E095E, 0x0F0033,
        ParentMotifs(),
        PassiveSkillBuilder()
            + overall.defence(0.8)
            + magic.attack(0.3)
            + luck(0.2),
        MotifCardRecipes().R.end.block { Blocks.OBSIDIAN },
    ),
    XARPITE(
        "xarpite", 5, "Xarpitia", "紅天石精シャルピーチャ", 0xD43333, 0xD45D5D, 0x8A1111, 0xAB0000,
        ParentMotifs() + { MAGNETISM },
        PassiveSkillBuilder()
            + shooting.attack(0.7)
            + mana(0.4)
            + MobEffects.DIG_SPEED() * ToolMaterialCard.XARPITE()
            + MobEffects.DIG_SPEED(2) * ToolMaterialCard.XARPITE() * fairyLevel.atLeast(16.0),
        MotifCardRecipes().R.block(BlockMaterialCard.XARPITE_BLOCK.block).item(MaterialCard.XARPITE.item),
    ),
    MIRANAGITE(
        "miranagite", 5, "Miranagitia", "蒼天石精ミラナギーチャ", 0x4EC5F4, 0x4394D3, 0x004477, 0x0C4CEF,
        ParentMotifs(),
        PassiveSkillBuilder()
            + magic.attack(0.7)
            + mana(0.4)
            + MobEffects.LUCK() * ToolMaterialCard.MIRANAGITE()
            + MobEffects.LUCK(2) * ToolMaterialCard.MIRANAGITE() * fairyLevel.atLeast(12.0),
        MotifCardRecipes().R.block(BlockMaterialCard.MIRANAGITE_BLOCK.block).item(MaterialCard.MIRANAGITE.item).block(OreCard.MIRANAGITE_ORE.block).block(OreCard.DEEPSLATE_MIRANAGITE_ORE.block), // TODO タグ化
    ),
    CHAOS_STONE(
        "chaos_stone", 8, "Chaose Stonia", "混沌石精キャオセストーニャ", 0xDB5F00, 0xB36229, 0x78421C, 0xFFBF40,
        ParentMotifs(),
        PassiveSkillBuilder()
            + shooting.attack(0.4)
            + miningSpeed(0.8)
            + MobEffects.DIG_SPEED() * ToolMaterialCard.CHAOS_STONE()
            + MobEffects.DIG_SPEED(2) * ToolMaterialCard.CHAOS_STONE() * fairyLevel.atLeast(16.0),
        MotifCardRecipes().R.item(MaterialCard.CHAOS_STONE.item),
    ),

    MAGNETITE(
        "magnetite", 4, "Magnetitia", "磁鉄鉱精マグネティーチャ", 0x565942, 0x232900, 0x1D2100, 0x1D2100,
        ParentMotifs() + { MAGNETISM },
        PassiveSkillBuilder()
            + collection(1.0)
            + collection(3.0) * ToolMaterialCard.MAGNETITE(),
        MotifCardRecipes().R.item(MaterialCard.MAGNETITE.item).block(OreCard.MAGNETITE_ORE.block).block(OreCard.DEEPSLATE_MAGNETITE_ORE.block), // TODO タグ化
    ),
    FLUORITE(
        "fluorite", 6, "Fluoritia", "蛍石精フルオリーチャ", 0xC0FEF3, 0x28E1C7, 0xAE91E9, 0x66CDEB,
        ParentMotifs(),
        PassiveSkillBuilder()
            + magic.attack(0.6)
            + luck(0.4)
            + MobEffects.LUCK() * ToolMaterialCard.FLUORITE()
            + MobEffects.LUCK(2) * ToolMaterialCard.FLUORITE() * fairyLevel.atLeast(12.0),
        MotifCardRecipes().R.item(MaterialCard.FLUORITE.item).block(OreCard.FLUORITE_ORE.block).block(OreCard.DEEPSLATE_FLUORITE_ORE.block), // TODO タグ化
    ),
    AMETHYST(
        "amethyst", 6, "Amethystia", "紫水晶精アメティスチャ", 0xCAA9FF, 0xA974FF, 0x9D60FF, 0xBC92FF,
        ParentMotifs(),
        PassiveSkillBuilder()
            + magic.attack(0.5)
            + magic.defence(0.5)
            + luck(0.5),
        MotifCardRecipes().R.block { Blocks.AMETHYST_BLOCK }.item { Items.AMETHYST_SHARD },
    ),
    DIAMOND(
        "diamond", 7, "Diamondia", "金剛石精ディアモンジャ", 0x97FFE3, 0xD1FAF3, 0x70FFD9, 0x30DBBD,
        ParentMotifs(),
        PassiveSkillBuilder()
            + luck(0.6)
            + miningSpeed(0.5)
            + MobEffects.DIG_SPEED() * ToolMaterialCard.DIAMOND()
            + MobEffects.DIG_SPEED(2) * ToolMaterialCard.DIAMOND() * fairyLevel.atLeast(16.0),
        MotifCardRecipes().R.block { Blocks.DIAMOND_BLOCK }.item { Items.DIAMOND } + BlockTags.DIAMOND_ORES,
    ),
    EMERALD(
        "emerald", 6, "Emeraldia", "翠玉精エメラルジャ", 0x9FF9B5, 0x81F99E, 0x17DD62, 0x008A25,
        ParentMotifs(),
        PassiveSkillBuilder()
            + luck(1.0)
            + MobEffects.LUCK() * ToolMaterialCard.EMERALD()
            + MobEffects.LUCK(2) * ToolMaterialCard.EMERALD() * fairyLevel.atLeast(12.0),
        MotifCardRecipes().R.block { Blocks.EMERALD_BLOCK }.item { Items.EMERALD } + BlockTags.EMERALD_ORES,
    ),

    // 動物
    PIG(
        "pig", 2, "Pigia", "豚精ピーギャ", 0xDB98A2, 0xF68C87, 0xC76B73, 0xDC94A1,
        ParentMotifs() + { FOOD },
        PassiveSkillBuilder()
            + miningSpeed(0.4) * food.atLeast(12)
            + miningSpeed(0.2) * food { Items.CARROT }
            + miningSpeed(0.2) * food { Items.POTATO }
            + miningSpeed(0.2) * food { Items.BEETROOT }
            + miningSpeed(0.8) * food { Items.PORKCHOP },
        MotifCardRecipes().R.overworld + EntityType.PIG,
    ),
    COW(
        "cow", 2, "Cowia", "牛精ツォーウャ", 0x433626, 0x644B37, 0x4A3828, 0xADADAD,
        ParentMotifs() + { FOOD },
        PassiveSkillBuilder()
            + melee.attack(0.4) * food.atLeast(12)
            + melee.attack(0.4) * food { Items.WHEAT }
            + melee.attack(0.8) * food { Items.BEEF },
        MotifCardRecipes().R.overworld + EntityType.COW,
    ),
    SHEEP(
        "sheep", 2, "Sheepia", "羊精シェーピャ", 0xB79680, 0xEDEEF0, 0xEDEEF0, 0xD7D7D9,
        ParentMotifs() + { FOOD },
        PassiveSkillBuilder()
            + overall.defence(0.4) * food.atLeast(12)
            + overall.defence(0.4) * food { Items.WHEAT }
            + overall.defence(0.8) * food { Items.MUTTON },
        MotifCardRecipes().R.overworld + EntityType.SHEEP,
    ),
    CHICKEN(
        "chicken", 2, "Chickenia", "鶏精キッケーニャ", 0xF3DE71, 0xEDEDED, 0xEDEDED, 0xD93117,
        ParentMotifs() + { FOOD },
        PassiveSkillBuilder()
            + fall.defence(3.0) * food.atLeast(12)
            + fall.defence(3.0) * food { Items.WHEAT }
            + fall.defence(6.0) * food { Items.CHICKEN }
            + MobEffects.SLOW_FALLING() * food { Items.WHEAT } * fairyLevel.atLeast(11.0),
        MotifCardRecipes().R.overworld + EntityType.CHICKEN,
    ),
    EGG(
        "egg", 2, "Eggia", "卵精エッギャ", 0xF0E6C6, 0xE0CC91, 0xE0CC91, 0xBAAA79,
        ParentMotifs() + { CHICKEN } + { FOOD },
        PassiveSkillBuilder()
            + health(0.8)
            + regeneration(0.8) * food { Items.EGG }
            + mana(5.0) { CHICKEN },
        MotifCardRecipes().R.overworld + EntityType.EGG,
    ),
    RABBIT(
        "rabbit", 5, "Rabbitia", "兎精ラッビーチャ", 0x9E866A, 0x8C7A64, 0x8C7962, 0x615345,
        ParentMotifs() + { FOOD },
        PassiveSkillBuilder()
            + luck(0.4) * food.atLeast(12)
            + luck(0.4) * food { Items.CARROT }
            + luck(0.8) * food { Items.RABBIT }
            + MobEffects.JUMP(2) * food { Items.CARROT } * fairyLevel.atLeast(14.0),
        MotifCardRecipes().R.overworld + EntityType.RABBIT,
    ),
    WOLF(
        "wolf", 4, "Wolfia", "狼精ウォルフャ", 0x827165, 0xBFBDBE, 0x9E9A96, 0x3F3E3A,
        ParentMotifs() + { CARRY },
        PassiveSkillBuilder()
            + melee.attack(0.4) * food { Items.MUTTON } // TODO 肉全般条件
            + melee.attack(0.4) * food.atLeast(12)
            + speed(0.4) * food.atLeast(12),
        MotifCardRecipes().R.common(ConventionalBiomeTags.IS_TAIGA) + EntityType.WOLF,
    ),
    HUMAN(
        "human", 5, "Humania", "人類精フマーニャ", 0xFFCB4C, 0x00AAAA, 0x322976, 0x9E7F2F,
        ParentMotifs() + { CARRY },
        PassiveSkillBuilder()
            + mana(5.0) { HUMAN },
        MotifCardRecipes().SR,
    ),
    PLAYER(
        "player", 5, "Playeria", "人精プライェーリャ", 0xB58D63, 0x00AAAA, 0x322976, 0x4B3422,
        ParentMotifs() + { HUMAN },
        PassiveSkillBuilder()
            + experience(1.0) * level.atMost(29),
        MotifCardRecipes().SR.always + EntityType.PLAYER,
    ),
    VILLAGER(
        "villager", 4, "Villageria", "村人精ヴィッラゲーリャ", 0xB58D63, 0x608C57, 0x608C57, 0x009800,
        ParentMotifs() + { HUMAN },
        PassiveSkillBuilder()
            + experience(0.4) * level.atMost(29) * food { Items.WHEAT }
            + experience(0.4) * level.atMost(29) * food { Items.POTATO }
            + luck(0.6) * food.atLeast(12)
            + mana(5.0 * 0.2) { HUMAN },
        MotifCardRecipes().R.overworld + EntityType.VILLAGER,
    ),
    WITCH(
        "witch", 6, "Witchia", "魔女精ウィツキャ", 0x96856F, 0x4D2180, 0x193308, 0x242424,
        ParentMotifs() + { VILLAGER },
        PassiveSkillBuilder()
            + regeneration(0.1)
            + fire.defence(2.0 * 0.3)
            + magic.attack(0.4)
            + magic.attack(0.4) * food.atLeast(12),
        MotifCardRecipes().R.overworld + EntityType.WITCH,
    ),
    EVOKER(
        "evoker", 7, "Evokeria", "召喚士精エヴォケーリャ", 0x939E9E, 0x292623, 0xCCB34E, 0x363636,
        ParentMotifs() + { VILLAGER },
        PassiveSkillBuilder()
            + magic.attack(0.6)
            + magic.attack(0.6) * food.atLeast(12),
        MotifCardRecipes().R + EntityType.EVOKER,
    ),
    CUPID(
        "cupid", 13, "Cupidia", "恋射手精ツピージャ", 0xCDBBAD, 0xE4F4FF, 0xDDEEFF, 0x893F1E,
        ParentMotifs(),
        PassiveSkillBuilder()
            + shooting.attack(1.0) * health.atLeast(18.0)
            + MobEffects.SLOW_FALLING() * health.atLeast(18.0)
            + MobEffects.JUMP(5) * health.atLeast(18.0),
        MotifCardRecipes().SSR,
    ),
    ENDERMAN(
        "enderman", 6, "Endermania", "終界人精エンデルマーニャ", 0x000000, 0x161616, 0x161616, 0xEF84FA,
        ParentMotifs() + { CARRY },
        PassiveSkillBuilder()
            + collection(1.0) * food.atLeast(12)
            + miningSpeed(0.2),
        MotifCardRecipes().R.overworld.nether.end + EntityType.ENDERMAN,
    ),
    ENDER_EYE(
        "ender_eye", 7, "Endere Ia", "終界眼精エンデーレッイャ", 0x8BC452, 0x568E91, 0x568E91, 0x1E4835,
        ParentMotifs() + { ENDERMAN },
        PassiveSkillBuilder()
            + collection(0.5)
            + magic.attack(0.5),
        MotifCardRecipes().R.item { Items.ENDER_EYE },
    ),
    PIGLIN_BRUTE(
        "piglin_brute", 7, "Pigline Brutia", "豚人畜生精ピグリーネブルーチャ", 0xEB9771, 0x403D11, 0x403D11, 0xE0B000,
        ParentMotifs(),
        PassiveSkillBuilder()
            + melee.attack(2.0) * inNether,
        MotifCardRecipes().R + EntityType.PIGLIN_BRUTE,
    ),
    SILVERFISH(
        "silverfish", 6, "Silverfishia", "紙魚精シルヴェルフィーシャ", 0x474747, 0x71746A, 0x60636B, 0x5E717A,
        ParentMotifs(),
        PassiveSkillBuilder()
            + miningSpeed(0.6) * food.atLeast(12)
            + miningSpeed(0.6) * indoor,
        MotifCardRecipes().R.common(ConventionalBiomeTags.IS_MOUNTAIN) + EntityType.SILVERFISH,
    ),
    WARDEN(
        "warden", 9, "Wardenia", "監守者精ワルデーニャ", 0x0A3135, 0xCFCFA4, 0xA0AA7A, 0x2CD0CA,
        ParentMotifs(),
        PassiveSkillBuilder()
            + melee.attack(0.7) * light.atMost(5)
            + magic.attack(0.7) * light.atMost(5)
            + MobEffects.DAMAGE_BOOST(2) * light.atMost(5),
        MotifCardRecipes().R.common(Biomes.DEEP_DARK) + EntityType.WARDEN,
    ),

    // アンデッド
    ZOMBIE(
        "zombie", 2, "Zombia", "硬屍精ゾンビャ", 0x2B4219, 0x00AAAA, 0x322976, 0x2B4219,
        ParentMotifs() + { CARRY },
        PassiveSkillBuilder()
            + melee.attack(0.6) * food.atMost(6)
            + melee.attack(0.6) * indoor,
        MotifCardRecipes().R.overworld + EntityType.ZOMBIE,
    ),
    ROTTEN_FLESH(
        "rotten_flesh", 2, "Rottene Fleshia", "腐肉精ロッテーネフレーシャ", 0x846129, 0xBD5B2D, 0xBD5B2D, 0xBD422D,
        ParentMotifs() + { ZOMBIE } + { FOOD },
        PassiveSkillBuilder()
            + overall.attack(1.0) * food { Items.ROTTEN_FLESH }
            + regeneration(0.4) * food.atMost(6)
            + hunger(1.0) * food.atLeast(7),
        MotifCardRecipes().R.item { Items.ROTTEN_FLESH },
    ),
    SKELETON(
        "skeleton", 2, "Skeletonia", "骸骨精スケレトーニャ", 0xCACACA, 0xCFCFCF, 0xCFCFCF, 0x494949,
        ParentMotifs(),
        PassiveSkillBuilder()
            + shooting.attack(0.6) * food.atMost(6)
            + shooting.attack(0.6) * indoor,
        MotifCardRecipes().R.overworld + EntityType.SKELETON,
    ),
    WITHER_SKELETON(
        "wither_skeleton", 7, "Withere Skeletonia", "枯骸骨精ウィテーレスケレトーニャ", 0x505252, 0x1C1C1C, 0x1C1C1C, 0x060606,
        ParentMotifs() + { SKELETON },
        PassiveSkillBuilder()
            + speed(0.4) * food.atMost(6)
            + melee.attack(0.5) * food.atMost(6)
            + melee.attack(0.5) * indoor,
        MotifCardRecipes().R + EntityType.WITHER_SKELETON,
    ),
    WITHER_SKELETON_SKULL(
        "wither_skeleton_skull", 8, "Withere Skeletone Skullia", "枯頭蓋骨精ウィテーレスケレトーネスクッリャ", 0x2E2E2E, 0x2E2E2E, 0x2E2E2E, 0x2E2E2E,
        ParentMotifs() + { WITHER_SKELETON },
        PassiveSkillBuilder()
            + magic.attack(1.2) * food.atMost(6),
        MotifCardRecipes().R.block { Blocks.WITHER_SKELETON_SKULL },
    ),
    WITHER(
        "wither", 8, "Witheria", "枯精ウィテーリャ", 0x181818, 0x3C3C3C, 0x141414, 0x557272,
        ParentMotifs(),
        PassiveSkillBuilder()
            + shooting.attack(1.0) * food.atMost(6)
            + shooting.defence(1.0) * food.atMost(6)
            + miningSpeed(0.5) * food.atMost(6)
            + MobEffects.SLOW_FALLING() * food.atMost(6)
            + MobEffects.JUMP(2) * food.atMost(6) * fairyLevel.atLeast(12.0)
            + MobEffects.MOVEMENT_SLOWDOWN(3) * food.atMost(6) * fairyLevel.atMost(16.0),
        MotifCardRecipes().R.nether + EntityType.WITHER,
    ),
    NETHER_STAR(
        "nether_star", 9, "Nethere Staria", "地獄星精ネテーレスターリャ", 0xD8D8FF, 0xF2E3FF, 0xD9E7FF, 0xFFFF68,
        ParentMotifs() + { WITHER } + { STAR },
        PassiveSkillBuilder()
            + luck(0.6)
            + magic.attack(0.4)
            + MobEffects.DIG_SPEED() * ToolMaterialCard.NETHER_STAR()
            + MobEffects.DIG_SPEED(2) * ToolMaterialCard.NETHER_STAR() * fairyLevel.atLeast(16.0),
        MotifCardRecipes().R.item { Items.NETHER_STAR },
    ),

    // 魔法生物
    BLAZE(
        "blaze", 7, "Blazia", "烈炎精ブラージャ", 0xE7DA21, 0xCB6E06, 0xB44500, 0xFF8025,
        ParentMotifs() + { FIRE },
        PassiveSkillBuilder()
            + shooting.attack(2.0) * onFire
            + ignition * MobEffects.FIRE_RESISTANCE,
        MotifCardRecipes().R.nether + EntityType.BLAZE,
    ),
    MAGMA_CUBE(
        "magma_cube", 7, "Magme Cubia", "溶岩賽精マグメツービャ", 0x3A0000, 0x592301, 0x300000, 0xE35C05,
        ParentMotifs() + { FIRE },
        PassiveSkillBuilder()
            + melee.attack(2.0) * onFire
            + ignition * MobEffects.FIRE_RESISTANCE,
        MotifCardRecipes().R.nether + EntityType.MAGMA_CUBE,
    ),
    GOLEM(
        "golem", 6, "Golemia", "鉄魔像精ゴレーミャ", 0xC1AB9E, 0xB5ADA8, 0xABA39D, 0x557725,
        ParentMotifs() + { CARRY },
        PassiveSkillBuilder()
            + melee.attack(0.6)
            + melee.attack(0.6) * outdoor,
        MotifCardRecipes().R + EntityType.IRON_GOLEM,
    ),

    // 菌類
    MUSHROOM(
        "mushroom", 3, "Mushroomia", "茸精ムシュローミャ", 0xDEDBD1, 0xC7C2AF, 0xC7C1AF, 0x8A836E,
        ParentMotifs() + { FOOD },
        PassiveSkillBuilder()
            + health(0.2) * food(FoodIngredientCategoryCard.MUSHROOM)
            + regeneration(0.2) * food(FoodIngredientCategoryCard.MUSHROOM)
            + mana(5.0) { MUSHROOM },
        MotifCardRecipes().SR.overworld.nether,
    ),
    RED_MUSHROOM(
        "red_mushroom", 3, "Rede Mushroomia", "赤茸精レーデムシュローミャ", 0xE6DBA8, 0xFF0A0A, 0xFF0A0A, 0xBFD7D9,
        ParentMotifs() + { MUSHROOM },
        PassiveSkillBuilder()
            + MobEffects.HEALTH_BOOST(1) * food { Items.RED_MUSHROOM }
            + MobEffects.HEALTH_BOOST(2) * food { Items.RED_MUSHROOM } * fairyLevel.atLeast(10.0)
            + magic.attack(0.6) * food.atLeast(12),
        MotifCardRecipes().R.overworld.nether.block { Blocks.RED_MUSHROOM }.item { Items.RED_MUSHROOM },
    ),
    BROWN_MUSHROOM(
        "brown_mushroom", 3, "Browne Mushroomia", "茶茸精ブロウネムシュローミャ", 0xDEB6A2, 0xF0AD8B, 0xC28C70, 0xDE9571,
        ParentMotifs() + { MUSHROOM },
        PassiveSkillBuilder()
            + regeneration(1.0) * food { Items.BROWN_MUSHROOM }
            + magic.defence(0.6) * food.atLeast(12),
        MotifCardRecipes().R.overworld.nether.block { Blocks.BROWN_MUSHROOM }.item { Items.BROWN_MUSHROOM },
    ),

    // 植物
    WHEAT(
        "wheat", 3, "Wheatia", "麦精ウェアーチャ", 0xD8BF7F, 0xDBBB65, 0xDBBB65, 0x896D20,
        ParentMotifs() + { FOOD },
        PassiveSkillBuilder()
            + miningSpeed(0.4) * outdoor
            + miningSpeed(0.8) * food { Items.WHEAT },
        MotifCardRecipes().R.overworld.block { Blocks.WHEAT }.item { Items.WHEAT_SEEDS }.item { Items.WHEAT },
    ),
    CARROT(
        "carrot", 4, "Carrotia", "人参精ツァッローチャ", 0xF98D10, 0xFD7F11, 0xE3710F, 0x248420,
        ParentMotifs() + { FOOD },
        PassiveSkillBuilder()
            + MobEffects.NIGHT_VISION(additionalSeconds = 10) * food { Items.GOLDEN_CARROT }
            + MobEffects.NIGHT_VISION(additionalSeconds = 10) * food { Items.CARROT } * fairyLevel.atLeast(10.0),
        MotifCardRecipes().R.overworld.block { Blocks.CARROTS }.item { Items.CARROT },
    ),
    POTATO(
        "potato", 4, "Potatia", "芋精ポターチャ", 0xEAC278, 0xE7B456, 0xE7B456, 0x248420,
        ParentMotifs() + { FOOD },
        PassiveSkillBuilder()
            + melee.attack(0.4) * outdoor
            + melee.attack(0.8) * food { Items.POTATO },
        MotifCardRecipes().R.overworld.block { Blocks.POTATOES }.item { Items.POTATO },
    ),
    POISONOUS_POTATO(
        "poisonous_potato", 5, "Poisonouse Potatia", "毒芋精ポイソノウセポターチャ", 0xCFE661, 0xE7B456, 0xE7B456, 0x61B835,
        ParentMotifs() + { POTATO },
        PassiveSkillBuilder()
            + overall.attack(0.2) * outdoor
            + overall.attack(0.4) * food { Items.POISONOUS_POTATO },
        MotifCardRecipes().R.overworld.block { Blocks.POTATOES }.item { Items.POISONOUS_POTATO },
    ),
    BEETROOT(
        "beetroot", 4, "Beetrootia", "火焔菜精ベートローチャ", 0xC1727C, 0xA74D55, 0x96383D, 0x01A900,
        ParentMotifs() + { FOOD } + { FIRE },
        PassiveSkillBuilder()
            + fire.attack(0.4) * outdoor // TODO 火属性攻撃
            + fire.attack(0.8) * food { Items.BEETROOT },
        MotifCardRecipes().R.overworld.block { Blocks.BEETROOTS }.item { Items.BEETROOT },
    ),
    PUMPKIN(
        "pumpkin", 4, "Pumpkinia", "南瓜精プンプキーニャ", 0x792D0F, 0xE48A40, 0xE48A40, 0xDCBE00,
        ParentMotifs() + { FOOD },
        PassiveSkillBuilder()
            + magic.attack(0.4) * outdoor
            + magic.attack(0.8) * food { Items.PUMPKIN },
        MotifCardRecipes().R.overworld.block { Blocks.PUMPKIN }.block { Blocks.CARVED_PUMPKIN },
    ),
    MELON(
        "melon", 6, "Melonia", "西瓜精メローニャ", 0xFF5440, 0xA6EE63, 0x195612, 0x01A900,
        ParentMotifs() + { FOOD } + { WATER },
        PassiveSkillBuilder()
            + luck(0.4) * outdoor
            + luck(0.8) * food { Items.MELON_SLICE },
        MotifCardRecipes().R.common(ConventionalBiomeTags.IS_JUNGLE).block { Blocks.MELON }.item { Items.MELON_SLICE },
    ),
    APPLE(
        "apple", 4, "Applia", "林檎精アップーリャ", 0xFF755D, 0xFF564E, 0xFF0000, 0x01A900,
        ParentMotifs() + { FOOD },
        PassiveSkillBuilder()
            + experience(0.4) * level.atMost(29) * outdoor
            + experience(0.8) * level.atMost(29) * food { Items.APPLE },
        MotifCardRecipes().R.overworld.item { Items.APPLE },
    ),
    SWEET_BERRY(
        "sweet_berry", 6, "Sweete Berria", "甘液果精スウェーテベッリャ", 0xB81D37, 0x4A070A, 0x4A070A, 0x126341,
        ParentMotifs() + { FOOD } + { WATER },
        PassiveSkillBuilder()
            + shooting.attack(0.4) * outdoor
            + shooting.attack(0.8) * food { Items.SWEET_BERRIES }
            + spine.defence(2.0),
        MotifCardRecipes().R.common(ConventionalBiomeTags.IS_TAIGA).item { Items.SWEET_BERRIES }.block { Blocks.SWEET_BERRY_BUSH },
    ),
    GLOW_BERRY(
        "glow_berry", 6, "Glowe Berria", "蛍光液果精グローウェベッリャ", 0xFFB73A, 0x8F650C, 0x8F650C, 0x00841A,
        ParentMotifs() + { FOOD } + { WATER },
        PassiveSkillBuilder()
            + magic.attack(0.4) * indoor
            + magic.attack(0.8) * food { Items.GLOW_BERRIES },
        MotifCardRecipes().R.common(Biomes.LUSH_CAVES).item { Items.GLOW_BERRIES }.block { Blocks.CAVE_VINES }.block { Blocks.CAVE_VINES_PLANT },
    ),
    MIRAGE(
        "mirage", 5, "Miragia", "妖精ミラージャ", 0x6DE3BE, 0x43FAFA, 0x43FAFA, 0x00F5F5,
        ParentMotifs(),
        PassiveSkillBuilder()
            + mana(1.0)
            + mending(1.0, ToolMaterialCard.MIRAGE.tag),
        MotifCardRecipes().R.overworld.block(MirageFlowerCard.block),
    ),
    PHANTOM_FLOWER(
        "phantom_flower", 7, "Phantomia", "幻花精ファントーミャ", 0xB78EF5, 0xF2C4FF, 0xF2C4FF, 0x70B7D4,
        ParentMotifs(),
        PassiveSkillBuilder()
            + regeneration(0.4) * outdoor
            + regeneration(0.8) * food(MaterialCard.PHANTOM_DROP.item)
            + mending(1.0, ToolMaterialCard.PHANTOM_DROP.tag),
        MotifCardRecipes().R.common(BiomeCards.FAIRY_FOREST.registryKey).block(PhantomFlowerCard.block),
    ),
    PHANTOM_DROP(
        "phantom_drop", 7, "Phantome Dropia", "幻想雫精ファントーメドローピャ", 0xF091FF, 0xD834E0, 0x9E34E0, 0x5C09B0,
        ParentMotifs() + { PHANTOM_FLOWER },
        PassiveSkillBuilder()
            + mending(1.0, ItemTagCard.PLANT_TOOLS.tag),
        MotifCardRecipes().R.item(MaterialCard.PHANTOM_DROP.item),
    ),
    VELOPEDA(
        "velopeda", 6, "Velopedia", "呪草精ヴェロページャ", 0x8BD100, 0xD52D2D, 0xB51414, 0x840707,
        ParentMotifs(),
        PassiveSkillBuilder()
            + magic.attack(0.2) * outdoor
            + regeneration(0.2) * outdoor
            + magic.attack(0.4) * food(MaterialCard.VEROPEDA_BERRIES.item)
            + regeneration(0.4) * food(MaterialCard.VEROPEDA_BERRIES.item),
        MotifCardRecipes().R.common(ConventionalBiomeTags.IS_DRY).nether.block(VeropedaCard.block),
    ),
    LUMINITE(
        "luminite", 7, "Luminitia", "輝石精ルミニーチャ", 0x9BE8E8, 0x75C7C7, 0x75C7C7, 0x5EE6E6,
        ParentMotifs(),
        PassiveSkillBuilder()
            + overall.defence(1.0)
            + MobEffects.DAMAGE_RESISTANCE() * ToolMaterialCard.LUMINITE()
            + MobEffects.DAMAGE_RESISTANCE(2) * ToolMaterialCard.LUMINITE() * fairyLevel.atLeast(10.0), // TODO 発光効果
        MotifCardRecipes().R.item(MaterialCard.LUMINITE.item),
    ),
    GLOW_LICHEN(
        "glow_lichen", 5, "Glowe Lichenia", "光苔精グローウェリケーニャ", 0x639386, 0x639386, 0x639386, 0xAFB18B,
        ParentMotifs(),
        PassiveSkillBuilder()
            + magic.attack(1.2) * indoor, // TODO 発光パッシブ
        MotifCardRecipes().R.overworld.block { Blocks.GLOW_LICHEN },
    ),
    CACTUS(
        "cactus", 3, "Cactusia", "仙人掌精ツァツトゥーシャ", 0x008200, 0xB0FFAC, 0x00E100, 0x010000,
        ParentMotifs(),
        PassiveSkillBuilder()
            + shooting.attack(1.2) * outdoor
            + regeneration(0.1) * outdoor
            + spine.defence(3.0),
        MotifCardRecipes().R.common(ConventionalBiomeTags.IS_DESERT).block { Blocks.CACTUS },
    ),
    DEAD_BUSH(
        "dead_bush", 3, "Deade Bushia", "枯木精デアデブーシャ", 0xB38247, 0xA17743, 0xA17743, 0x6E583F,
        ParentMotifs() + { WOOD },
        PassiveSkillBuilder()
            + shooting.attack(1.4) * outdoor,
        MotifCardRecipes().R.common(ConventionalBiomeTags.IS_DESERT).common(ConventionalBiomeTags.IS_BADLANDS).block { Blocks.DEAD_BUSH },
    ),

    // 樹木
    WOOD(
        "wood", 2, "Woodia", "木精ウォージャ", 0xE7C697, 0xAD8232, 0xAD8232, 0x8B591C,
        ParentMotifs(),
        PassiveSkillBuilder()
            + shooting.attack(1.0)
            + MobEffects.MOVEMENT_SPEED() * ToolMaterialCard.WOOD() // TODO 射撃攻撃力増加ステータス効果
            + MobEffects.MOVEMENT_SPEED(2) * ToolMaterialCard.WOOD() * fairyLevel.atLeast(12.0)
            + mending(1.0, ToolMaterialCard.WOOD.tag),
        MotifCardRecipes().SR.overworld + BlockTags.LOGS + BlockTags.PLANKS,
    ),
    SPRUCE(
        "spruce", 4, "Sprucia", "松精スプルーツァ", 0x795C36, 0x583E1F, 0x23160A, 0x4C784C,
        ParentMotifs() + { WOOD },
        PassiveSkillBuilder()
            + shooting.attack(0.4)
            + health(0.6),
        MotifCardRecipes().R.common(ConventionalBiomeTags.IS_TAIGA).block { Blocks.SPRUCE_SAPLING }.block { Blocks.SPRUCE_LOG },
    ),
    DARK_OAK(
        "dark_oak", 5, "Darke Oakia", "濃樫精ダルケオアキャ", 0x4A361A, 0x478F1B, 0x2A5410, 0x326313,
        ParentMotifs() + { WOOD },
        PassiveSkillBuilder()
            + magic.attack(0.4)
            + health(0.6),
        MotifCardRecipes().R.common(Biomes.DARK_FOREST).block { Blocks.DARK_OAK_SAPLING }.block { Blocks.DARK_OAK_LOG },
    ),
    HAIMEVISKA(
        "haimeviska", 3, "Haimeviskia", "精樹精ハイメヴィスキャ", 0x8A4C16, 0xB85CC4, 0x3E5918, 0x3C7A4D,
        ParentMotifs() + { WOOD } + { FOOD },
        PassiveSkillBuilder()
            + mana(0.6)
            + experience(0.6) * level.atMost(29) * food(MaterialCard.HAIMEVISKA_SAP.item)
            + experience(0.2) * level.atMost(39) * food(MaterialCard.HAIMEVISKA_SAP.item)
            + mending(1.0, ToolMaterialCard.HAIMEVISKA_ROSIN.tag),
        MotifCardRecipes().R.overworld.block(HaimeviskaBlockCard.SAPLING.block).block(HaimeviskaBlockCard.LOG.block),
    ),

    // 植物モブ
    CREEPER(
        "creeper", 3, "Creeperia", "匠精ツレペーリャ", 0x5BAA53, 0xD6FFCF, 0x5EE74C, 0x000000,
        ParentMotifs(),
        PassiveSkillBuilder()
            + miningSpeed(0.6)
            + overall.attack(0.2),
        MotifCardRecipes().R.overworld + EntityType.CREEPER,
    ),

    // 食べ物
    FOOD(
        "food", 3, "Foodia", "食物精フォージャ", 0xF0AD41, 0xB84933, 0xB84933, 0x589C2C,
        ParentMotifs(),
        PassiveSkillBuilder()
            + mana(3.0) { FOOD }
            + regeneration(0.1) * food.atLeast(12),
        MotifCardRecipes().SR,
    ),
    SUGAR(
        "sugar", 2, "Sugaria", "砂糖精スガーリャ", 0xE3E3E3, 0xE3E3E3, 0xCECED8, 0xF7F7F7,
        ParentMotifs() + { FOOD },
        PassiveSkillBuilder()
            + speed(0.4)
            + speed(0.8) * food { Items.SUGAR },
        MotifCardRecipes().R.item { Items.SUGAR },
    ),
    GOLDEN_APPLE(
        "golden_apple", 7, "Goldene Applia", "金林檎精ゴルデーネアップーリャ", 0xFF755D, 0xDEDE00, 0xDEDE00, 0x01A900,
        ParentMotifs() + { APPLE } + { GOLD },
        PassiveSkillBuilder()
            + luck(0.2) * food.atLeast(12)
            + regeneration(0.2) * food.atLeast(12)
            + luck(0.6) * food { Items.GOLDEN_APPLE }
            + regeneration(0.6) * food { Items.GOLDEN_APPLE },
        MotifCardRecipes().R.item { Items.GOLDEN_APPLE },
    ),
    ENCHANTED_GOLDEN_APPLE(
        "enchanted_golden_apple", 9, "Enchantede Goldene Applia", "付魔金林檎精エンキャンテーデゴルデーネアップーリャ", 0xFF755D, 0xDEDE00, 0xDEDE00, 0xDE4FD7,
        ParentMotifs() + { GOLDEN_APPLE } + { ENCHANT },
        PassiveSkillBuilder()
            + health(0.4) * food.atLeast(12)
            + luck(0.8) * food { Items.ENCHANTED_GOLDEN_APPLE }
            + regeneration(0.8) * food { Items.ENCHANTED_GOLDEN_APPLE }
            + health(0.8) * food { Items.ENCHANTED_GOLDEN_APPLE }
            + overall.defence(0.8) * food { Items.ENCHANTED_GOLDEN_APPLE }
            + MobEffects.FIRE_RESISTANCE() * food { Items.ENCHANTED_GOLDEN_APPLE },
        MotifCardRecipes().R.item { Items.ENCHANTED_GOLDEN_APPLE },
    ),
    COOKIE(
        "cookie", 5, "Cookia", "麭精ツォーキャ", 0xCC850C, 0xE9994F, 0xDA843C, 0x882500,
        ParentMotifs() + { FOOD },
        PassiveSkillBuilder()
            + miningSpeed(0.4) * food.atLeast(12)
            + miningSpeed(0.8) * food { Items.COOKIE },
        MotifCardRecipes().R.item { Items.COOKIE },
    ),
    CAKE(
        "cake", 4, "Cakia", "蛋麭精ツァーキャ", 0xCC850C, 0xF5F0DC, 0xD3D0BF, 0xDE3334,
        ParentMotifs() + { FOOD },
        PassiveSkillBuilder()
            + mana(3.0) { FOOD },
        MotifCardRecipes().R.block { Blocks.CAKE }.item { Items.CAKE } + BlockTags.CANDLE_CAKES,
    ),

    // 道具
    PICKAXE(
        "pickaxe", 3, "Pickaxia", "鶴嘴精ピッカーシャ", 0xFFFFFF, 0xCD9A6A, 0x676D75, 0xC9D0C6,
        ParentMotifs(),
        PassiveSkillBuilder()
            + miningSpeed(1.2) * SimplePassiveSkillConditionCard.PICKAXE,
        MotifCardRecipes().R + ItemTags.PICKAXES,
    ),
    AXE(
        "axe", 3, "Axia", "斧精アーシャ", 0xFFFFFF, 0xCD9A6A, 0x529B3A, 0xC9D0C6,
        ParentMotifs(),
        PassiveSkillBuilder()
            + miningSpeed(1.2) * SimplePassiveSkillConditionCard.AXE,
        MotifCardRecipes().R + ItemTags.AXES,
    ),
    SHOVEL(
        "shovel", 3, "Shovelia", "円匙精ショヴェーリャ", 0xFFFFFF, 0xCD9A6A, 0x8C601D, 0xC9D0C6,
        ParentMotifs(),
        PassiveSkillBuilder()
            + miningSpeed(1.2) * SimplePassiveSkillConditionCard.SHOVEL,
        MotifCardRecipes().R + ItemTags.SHOVELS,
    ),
    HOE(
        "hoe", 3, "Hia", "鍬精ヒャ", 0xFFFFFF, 0xCD9A6A, 0x47FF00, 0xC9D0C6,
        ParentMotifs(),
        PassiveSkillBuilder()
            + luck(1.2) * SimplePassiveSkillConditionCard.HOE,
        MotifCardRecipes().R + ItemTags.HOES,
    ),
    SWORD(
        "sword", 3, "Swordia", "剣精スウォルジャ", 0xFFFFFF, 0xCD9A6A, 0xFF0300, 0xC9D0C6,
        ParentMotifs(),
        PassiveSkillBuilder()
            + melee.attack(1.2) * SimplePassiveSkillConditionCard.SWORD,
        MotifCardRecipes().R + ItemTags.SWORDS,
    ),
    FLINT_AND_STEEL(
        "flint_and_steel", 4, "Flinte Ande Steelia", "火打石打金精フリンテアンデステーリャ", 0xB8BBC2, 0x383838, 0x383838, 0xE55600,
        ParentMotifs() + { FIRE },
        PassiveSkillBuilder()
            + overall.attack(1.2) * onFire
            + ignition * MobEffects.FIRE_RESISTANCE,
        MotifCardRecipes().R.item { Items.FLINT_AND_STEEL },
    ),
    SHIELD(
        "shield", 3, "Shieldia", "盾精シエルジャ", 0xFFFFFF, 0xFFC48E, 0x5A5A8E, 0xFFFFFF,
        ParentMotifs(),
        PassiveSkillBuilder()
            + overall.defence(0.3)
            + shooting.defence(0.8),
        MotifCardRecipes().R.item { Items.SHIELD },
    ),
    ARROW(
        "arrow", 3, "Arrowia", "矢精アッローウャ", 0xAD771F, 0xF2F2F2, 0xF2F2F2, 0x424242,
        ParentMotifs(),
        PassiveSkillBuilder()
            + shooting.attack(1.2),
        MotifCardRecipes().R.item { Items.ARROW },
    ),

    // 建材
    IRON_BARS(
        "iron_bars", 4, "Irone Barsia", "鉄格子精イローネバルシャ", 0xFFFFFF, 0xA1A1A3, 0x404040, 0x404040,
        ParentMotifs(),
        PassiveSkillBuilder()
            + melee.defence(1.5),
        MotifCardRecipes().R.block { Blocks.IRON_BARS },
    ),
    GLASS(
        "glass", 3, "Glassia", "硝子精グラッシャ", 0xFFFFFF, 0xEFF5FF, 0xE8EDF5, 0xADE0E9,
        ParentMotifs(),
        PassiveSkillBuilder()
            + MobEffects.INVISIBILITY() * health.atMost(1.0) * notInNether
            + MobEffects.INVISIBILITY() * fairyLevel.atLeast(12.0)
            + MobEffects.GLOWING() * health.atMost(1.0) * notInNether
            + MobEffects.GLOWING() * fairyLevel.atLeast(12.0),
        MotifCardRecipes().R.block { Blocks.GLASS },
    ),
    MAGENTA_GLAZED_TERRACOTTA(
        "magenta_glazed_terracotta", 3, "Magente Glazede Terracottia", "赤紫釉陶精マゲンテグラゼデテッラツォッチャ", 0xFFFFFF, 0xF4B5CB, 0xCB58C2, 0x9D2D95,
        ParentMotifs(),
        PassiveSkillBuilder()
            + shooting.attack(0.4)
            + shooting.defence(0.4)
            + luck(0.4),
        MotifCardRecipes().R.block { Blocks.MAGENTA_GLAZED_TERRACOTTA },
    ),
    PURPUR(
        "purpur", 8, "Purpuria", "紫珀精プルプーリャ", 0xCBA8CB, 0xC08AC0, 0xC08AC0, 0xBC68BB,
        ParentMotifs(),
        PassiveSkillBuilder()
            + collection(0.5)
            + magic.attack(0.2)
            + overall.defence(0.5),
        MotifCardRecipes().R.block { Blocks.PURPUR_BLOCK },
    ),

    // ユーティリティ
    CANDLE(
        "candle", 4, "Candlia", "蝋燭精ツァンドゥリャ", 0xE0DABF, 0xC9AF89, 0xC9AF89, 0x1B1A24,
        ParentMotifs() + { FIRE },
        PassiveSkillBuilder()
            + magic.attack(0.4)
            + magic.attack(0.4) * indoor
            + magic.attack(0.4) * light.atMost(5)
            + magic.attack(0.4) * onFire,
        MotifCardRecipes().R + BlockTags.CANDLES + BlockTags.CANDLE_CAKES,
    ),
    LIGHTNING_ROD(
        "lightning_rod", 4, "Lightninge Rodia", "避雷針精リグトニンゲロージャ", 0xFFFFFF, 0xF77653, 0xF77653, 0xF77653,
        ParentMotifs() + { COPPER } + { THUNDER },
        PassiveSkillBuilder()
            + magic.attack(3.0) * overworld * thundering * outdoor
            + magic.defence(3.0) * overworld * thundering * outdoor,
        MotifCardRecipes().R.block { Blocks.LIGHTNING_ROD },
    ),
    CHEST(
        "chest", 2, "Chestia", "箱精ケスチャ", 0xD6982D, 0xB3822E, 0xB3822E, 0x42392C,
        ParentMotifs(),
        PassiveSkillBuilder()
            + collection(1.5) * indoor,
        MotifCardRecipes().R.block { Blocks.CHEST },
    ),
    HOPPER(
        "hopper", 4, "Hopperia", "漏斗精ホッペーリャ", 0xFFFFFF, 0x797979, 0x646464, 0x5A5A5A,
        ParentMotifs() + { CARRY },
        PassiveSkillBuilder()
            + collection(0.6)
            + collection(0.6) * indoor,
        MotifCardRecipes().R.block { Blocks.HOPPER },
    ),
    ANVIL(
        "anvil", 4, "Anvilia", "金床精アンヴィーリャ", 0xFFFFFF, 0xA9A9A9, 0x909090, 0xA86F18,
        ParentMotifs(),
        PassiveSkillBuilder()
            + melee.attack(0.8) * indoor
            + melee.attack(0.4),
        MotifCardRecipes().R.block { Blocks.ANVIL },
    ),
    ENCHANTING_TABLE(
        "enchanting_table", 6, "Enchantinge Tablia", "付魔台精エンキャンティンゲターブリャ", 0x472F65, 0xCE2828, 0xCE2828, 0x7BFFDD,
        ParentMotifs(),
        PassiveSkillBuilder()
            + experience(0.6) * level.atMost(29) * indoor
            + magic.attack(0.6) * indoor,
        MotifCardRecipes().R.block { Blocks.ENCHANTING_TABLE },
    ),
    ENCHANT(
        "enchant", 6, "Enchantia", "付魔精エンキャンチャ", 0xD0C2FF, 0xF055FF, 0xC381E3, 0xBE00FF,
        ParentMotifs(),
        PassiveSkillBuilder()
            + luck(0.2)
            + overall.attack(0.2)
            + magic.attack(0.6),
        MotifCardRecipes().R.block { Blocks.ENCHANTING_TABLE }.item { Items.ENCHANTED_BOOK },
    ),
    BREWING_STAND(
        "brewing_stand", 7, "Brewinge Standia", "醸造台精ブレウィンゲスタンジャ", 0xFFFFFF, 0xAE5B5B, 0x7E7E7E, 0xFFDF55,
        ParentMotifs(),
        PassiveSkillBuilder()
            + mana(0.4)
            + speed(0.2) * indoor
            + regeneration(0.2) * indoor
            + overall.attack(0.2) * indoor
            + overall.defence(0.2) * indoor,
        MotifCardRecipes().R.block { Blocks.BREWING_STAND },
    ),
    BEACON(
        "beacon", 11, "Beaconia", "信標精ベアツォーニャ", 0x97FFE3, 0x6029B3, 0x2E095E, 0xD4EAE6,
        ParentMotifs(),
        PassiveSkillBuilder()
            + MobEffects.MOVEMENT_SPEED() * skyVisible
            + MobEffects.DAMAGE_RESISTANCE() * skyVisible
            + MobEffects.JUMP() * skyVisible
            + MobEffects.DAMAGE_BOOST() * skyVisible
            + MobEffects.DIG_SPEED() * skyVisible
            + regeneration(0.1) * skyVisible * fairyLevel.atLeast(12.0)
            + MobEffects.MOVEMENT_SPEED(2) * skyVisible * fairyLevel.atLeast(13.0)
            + MobEffects.DAMAGE_RESISTANCE(2) * skyVisible * fairyLevel.atLeast(14.0)
            + MobEffects.JUMP(2) * skyVisible * fairyLevel.atLeast(15.0)
            + MobEffects.DAMAGE_BOOST(2) * skyVisible * fairyLevel.atLeast(16.0)
            + MobEffects.DIG_SPEED(2) * skyVisible * fairyLevel.atLeast(17.0),
        MotifCardRecipes().R.block { Blocks.BEACON },
    ),

    // 回路
    TRINITROTOLUENE(
        "trinitrotoluene", 5, "Trinitrotoluenia", "茶褐薬精トリニトロトルエーニャ", 0xB4B4B5, 0xD8431A, 0xD8431A, 0x000000,
        ParentMotifs(),
        PassiveSkillBuilder()
            + miningSpeed(0.3)
            + miningSpeed(0.8) * indoor,
        MotifCardRecipes().R.block { Blocks.TNT },
    ),

    // 場所
    AVALON(
        "avalon", 9, "Avalonia", "阿瓦隆精アヴァローニャ", 0xFFE4CA, 0xE1FFCE, 0xD0FFE6, 0xFFCAFF,
        ParentMotifs(),
        PassiveSkillBuilder()
            + luck(1.0) * overworld
            + magic.attack(1.0) * overworld
            + overall.defence(0.5) * overworld
            + regeneration(0.5) * overworld,
        MotifCardRecipes().SSR,
    ),

    // 時間
    TIME(
        "time", 14, "Timia", "時精ティーミャ", 0xCDFFBF, 0xD5DEBC, 0xD8DEA7, 0x8DD586,
        ParentMotifs(),
        PassiveSkillBuilder()
            + MobEffects.MOVEMENT_SPEED(2)
            + speed(0.5) * fairyLevel.atLeast(16.0)
            + mana(5.0) { TIME },
        MotifCardRecipes().SR.always,
    ),
    NIGHT(
        "night", 9, "Nightia", "夜精ニグチャ", 0xFFE260, 0x2C2C2E, 0x0E0E10, 0x2D4272,
        ParentMotifs() + { TIME },
        PassiveSkillBuilder()
            + speed(0.4) * overworld * night * skyVisible
            + regeneration(0.4) * overworld * night * skyVisible
            + MobEffects.NIGHT_VISION(additionalSeconds = 10) * overworld * night * skyVisible,
        MotifCardRecipes().SR.overworld,
    ),

    // 天候
    THUNDER(
        "thunder", 9, "Thunderia", "雷精ツンデーリャ", 0xB4FFFF, 0x4D5670, 0x4D5670, 0xFFEB00,
        ParentMotifs(),
        PassiveSkillBuilder() // TODO 雷に関する効果
            + shooting.attack(1.0) * overworld * thundering
            + magic.attack(1.0) * overworld * thundering
            + mana(2.0) { THUNDER },
        MotifCardRecipes().SR.overworld,
    ),

    // 行為
    CARRY(
        "carry", 2, "Carria", "運搬精ツァッリャ", 0xD8BEEE, 0xC9C9C9, 0xC9C9C9, 0xCFAC8C,
        ParentMotifs(),
        PassiveSkillBuilder()
            + mana(2.0) { CARRY },
        MotifCardRecipes().SR,
    ),

    // 概念
    MAGNETISM(
        "magnetism", 10, "Magnetismia", "磁気精マグネティスミャ", 0xA6A6A6, 0xB33636, 0x3636B3, 0x333333,
        ParentMotifs() + { CARRY },
        PassiveSkillBuilder()
            + collection(1.0)
            + mana(5.0) { MAGNETISM },
        MotifCardRecipes().SR.always,
    ),
    GRAVITY(
        "gravity", 12, "Gravitia", "重力精グラヴィーチャ", 0xC2A7F2, 0x3600FF, 0x2A00B1, 0x110047,
        ParentMotifs(),
        PassiveSkillBuilder()
            + MobEffects.SLOW_FALLING()
            + overall.attack(0.8) * fairyLevel.atLeast(16.0),
        MotifCardRecipes().SR.item { Items.APPLE },
    ),
    ANTI_ENTROPY(
        "anti_entropy", 13, "Ante Entropia", "秩序精アンテエントローピャ", 0xD4FCFF, 0x9EECFF, 0x9EECFF, 0x54C9FF,
        ParentMotifs(),
        PassiveSkillBuilder()
            + MobEffects.LUCK(2)
            + luck(0.5) * fairyLevel.atLeast(16.0),
        MotifCardRecipes().SR.always,
    ),

    // 特殊
    MINA(
        "mina", 5, "Minia", "銀子精ミーニャ", 0xFFFF84, 0xFFFF00, 0xFFFF00, 0xFFC800,
        ParentMotifs(),
        PassiveSkillBuilder(),
        MotifCardRecipes(),
    ),
    ;

    val identifier = MirageFairy2024.identifier(path)
    val translation = Translation({ "${MirageFairy2024.MOD_ID}.motif.${identifier.toLanguageKey()}" }, enName, jaName)
    override val displayName = text { translation() }
    override val parents get() = parentMotifs.get()
    override val passiveSkillSpecifications = passiveSkillBuilder.specifications
}


class ParentMotifs {
    private val list = mutableListOf<() -> Motif>()
    private val compiledList by lazy { list.map { it() } }
    operator fun plus(motifGetter: () -> Motif): ParentMotifs {
        list += motifGetter
        return this
    }

    fun get() = compiledList
}


// レシピ

class MotifCardRecipes {
    val recipes = mutableListOf<context(ModContext)(MotifCard) -> Unit>()
    fun onInit(recipe: context(ModContext)(MotifCard) -> Unit): MotifCardRecipes {
        recipes += recipe
        return this
    }
}

private fun MotifCardRecipes.fairyStatueFountainRecipe(rarity: FairyStatueFountainBlock.Rarity) = this.onInit { FairyStatueFountainBlock.recipes += FairyStatueFountainBlock.Recipe(it, rarity) }
private val MotifCardRecipes.R get() = this.fairyStatueFountainRecipe(FairyStatueFountainBlock.Rarity.R)
private val MotifCardRecipes.SR get() = this.fairyStatueFountainRecipe(FairyStatueFountainBlock.Rarity.SR)
private val MotifCardRecipes.PICKUP_SR get() = this.fairyStatueFountainRecipe(FairyStatueFountainBlock.Rarity.PICKUP_SR).SR
private val MotifCardRecipes.SSR get() = this.fairyStatueFountainRecipe(FairyStatueFountainBlock.Rarity.SSR)
private val MotifCardRecipes.PICKUP_SSR get() = this.fairyStatueFountainRecipe(FairyStatueFountainBlock.Rarity.PICKUP_SSR).SSR

private fun MotifCardRecipes.common() = this.onInit { COMMON_MOTIF_RECIPES += AlwaysCommonMotifRecipe(it) }
private fun MotifCardRecipes.common(biome: ResourceKey<Biome>) = this.onInit { COMMON_MOTIF_RECIPES += BiomeCommonMotifRecipe(it, biome) }
private fun MotifCardRecipes.common(biomeTag: TagKey<Biome>) = this.onInit { COMMON_MOTIF_RECIPES += BiomeTagCommonMotifRecipe(it, biomeTag) }
private val MotifCardRecipes.always get() = this.common()
private val MotifCardRecipes.overworld get() = this.common(ConventionalBiomeTags.IS_OVERWORLD)
private val MotifCardRecipes.nether get() = this.common(ConventionalBiomeTags.IS_NETHER)
private val MotifCardRecipes.end get() = this.common(ConventionalBiomeTags.IS_END)

private fun MotifCardRecipes.item(item: () -> Item) = this.onInit { ModEvents.onInitialize { FairyDreamRecipes.ITEM.register(item(), it) } }
private fun MotifCardRecipes.block(block: () -> Block) = this.onInit { ModEvents.onInitialize { FairyDreamRecipes.BLOCK.register(block(), it) } }
private operator fun MotifCardRecipes.plus(entityType: EntityType<*>) = this.onInit { FairyDreamRecipes.ENTITY_TYPE.register(entityType, it) }

@JvmName("plusItemTag")
private operator fun MotifCardRecipes.plus(tag: TagKey<Item>) = this.onInit { FairyDreamRecipes.ITEM.registerFromTag(tag, it) }

@JvmName("plusBlockTag")
private operator fun MotifCardRecipes.plus(tag: TagKey<Block>) = this.onInit { FairyDreamRecipes.BLOCK.registerFromTag(tag, it) }

@JvmName("plusEntityTypeTag")
private operator fun MotifCardRecipes.plus(tag: TagKey<EntityType<*>>) = this.onInit { FairyDreamRecipes.ENTITY_TYPE.registerFromTag(tag, it) }


// パッシブスキル

private class PassiveSkillBuilder {
    val specifications = mutableListOf<PassiveSkillSpecification<*>>()
}

private operator fun <T : Any> PassiveSkillEffect<T>.invoke(valueProvider: (mana: Double) -> T) = PassiveSkillSpecification(listOf(), this, valueProvider)
private operator fun <T : Any> PassiveSkillSpecification<T>.times(condition: PassiveSkillCondition) = PassiveSkillSpecification(this.conditions + condition, this.effect, this.valueProvider)
private operator fun PassiveSkillBuilder.plus(specification: PassiveSkillSpecification<*>) = this.also { it.specifications += specification }

private val overworld get() = SimplePassiveSkillConditionCard.OVERWORLD
private val outdoor get() = SimplePassiveSkillConditionCard.OUTDOOR
private val indoor get() = SimplePassiveSkillConditionCard.INDOOR
private val skyVisible get() = SimplePassiveSkillConditionCard.SKY_VISIBLE
private val fine get() = SimplePassiveSkillConditionCard.FINE
private val raining get() = SimplePassiveSkillConditionCard.RAINING
private val thundering get() = SimplePassiveSkillConditionCard.THUNDERING
private val daytime get() = SimplePassiveSkillConditionCard.DAYTIME
private val night get() = SimplePassiveSkillConditionCard.NIGHT
private val underwater get() = SimplePassiveSkillConditionCard.UNDERWATER
private val inTheAir get() = SimplePassiveSkillConditionCard.IN_THE_AIR
private val onFire get() = SimplePassiveSkillConditionCard.ON_FIRE
private val inNether get() = SimplePassiveSkillConditionCard.IN_NETHER
private val notInNether get() = SimplePassiveSkillConditionCard.NOT_IN_NETHER

private fun food(item: () -> Item) = ItemFoodIngredientPassiveSkillCondition(item)
private fun food(category: FoodIngredientCategoryCard) = CategoryFoodIngredientPassiveSkillCondition(category)

private fun IntComparisonPassiveSkillCondition.Term.atLeast(threshold: Int) = IntComparisonPassiveSkillCondition(this, true, threshold)
private fun IntComparisonPassiveSkillCondition.Term.atMost(threshold: Int) = IntComparisonPassiveSkillCondition(this, false, threshold)
private fun DoubleComparisonPassiveSkillCondition.Term.atLeast(threshold: Double) = DoubleComparisonPassiveSkillCondition(this, true, threshold)
private fun DoubleComparisonPassiveSkillCondition.Term.atMost(threshold: Double) = DoubleComparisonPassiveSkillCondition(this, false, threshold)
private val light get() = IntComparisonPassiveSkillCondition.LIGHT_LEVEL_TERM
private val food get() = IntComparisonPassiveSkillCondition.FOOD_LEVEL_TERM
private val level get() = IntComparisonPassiveSkillCondition.LEVEL_TERM
private val fairyLevel get() = DoubleComparisonPassiveSkillCondition.FAIRY_LEVEL_TERM
private val health get() = DoubleComparisonPassiveSkillCondition.HEALTH_TERM

private operator fun ToolMaterialCard.invoke() = ToolMaterialCardPassiveSkillCondition(this)

private operator fun <T : Any> PassiveSkillSpecification<T>.times(statusEffect: Holder<MobEffect>) = this * StatusEffectPassiveSkillCondition(statusEffect)

private fun mana(factor: Double, motifGetter: () -> Motif? = { null }) = ManaBoostPassiveSkillEffect { ManaBoostPassiveSkillEffect.Value(mapOf(motifGetter() to it * factor * 0.02)) }
private fun attribute(attribute: Holder<Attribute>, factor: Double) = EntityAttributePassiveSkillEffect { EntityAttributePassiveSkillEffect.Value(mapOf(attribute to it * factor)) }
private fun speed(factor: Double) = attribute(Attributes.MOVEMENT_SPEED, factor * 0.002)
private fun health(factor: Double) = attribute(Attributes.MAX_HEALTH, factor * 0.4)
private fun luck(factor: Double) = attribute(Attributes.LUCK, factor * 0.1)
private operator fun Holder<MobEffect>.invoke(level: Int = 1, additionalSeconds: Int = 0): PassiveSkillSpecification<StatusEffectPassiveSkillEffect.Value> {
    return StatusEffectPassiveSkillEffect { StatusEffectPassiveSkillEffect.Value(mapOf(this@invoke to StatusEffectPassiveSkillEffect.Entry(level, additionalSeconds))) }
}

private val ignition get() = IgnitionPassiveSkillEffect { true }
private fun experience(factor: Double) = ExperiencePassiveSkillEffect { it * factor * 0.005 }
private fun regeneration(factor: Double) = RegenerationPassiveSkillEffect { it * factor * 0.01 }
private fun hunger(factor: Double) = HungerPassiveSkillEffect { it * factor * 0.1 }
private fun mending(factor: Double, tag: TagKey<Item>) = MendingPassiveSkillEffect { MendingPassiveSkillEffect.Value(mapOf(tag to it * factor * 0.01)) }
private fun collection(factor: Double) = CollectionPassiveSkillEffect { it * factor * 0.1 }

private val overall get() = ElementPassiveSkillEffect.Elements.OVERALL
private val melee get() = ElementPassiveSkillEffect.Elements.MELEE
private val shooting get() = ElementPassiveSkillEffect.Elements.SHOOTING
private val magic get() = ElementPassiveSkillEffect.Elements.MAGIC
private val fire get() = ElementPassiveSkillEffect.Elements.FIRE
private val fall get() = ElementPassiveSkillEffect.Elements.FALL
private val spine get() = ElementPassiveSkillEffect.Elements.SPINE
private fun ElementPassiveSkillEffect.Element.attack(factor: Double) = ElementPassiveSkillEffect { ElementPassiveSkillEffect.Value(mapOf(this to it * factor * 0.03), mapOf()) }
private fun ElementPassiveSkillEffect.Element.defence(factor: Double) = ElementPassiveSkillEffect { ElementPassiveSkillEffect.Value(mapOf(), mapOf(this to it * factor * 0.03)) }

private fun miningSpeed(factor: Double) = MiningSpeedPassiveSkillEffect { it * factor * 0.01 }


operator fun Motif.contains(child: Motif): Boolean = child == this || child.parents.any { it in this }

@JvmName("nullableContains")
operator fun Motif?.contains(child: Motif?) = this == null || child != null && child in this


context(ModContext)
fun initMotif() {
    MotifCard.entries.forEach { card ->
        Registration(motifRegistry, card.identifier) { card }.register()
        card.translation.enJa()
        card.recipes.recipes.forEach {
            it(this@ModContext, card)
        }
    }

    registerClientDebugItem("dump_fairy_motifs", Items.STRING.toTextureSource(), 0xFFF200FF.toInt()) { world, player, _, _ ->
        val sb = StringBuilder()
        motifRegistry.sortedBy { if (it is MotifCard) it.ordinal else 99999999 }.forEach { motif ->
            sb.append("|${motif.displayName.string}|${motif.rare}|")
            motif.passiveSkillSpecifications.forEachIndexed { index, specification ->
                fun <T : Any> f(specification: PassiveSkillSpecification<T>) {
                    if (index > 0) sb.append("&br;")
                    sb.append(specification.effect.getText(specification.valueProvider(motif.rare.toDouble())).string)
                }
                f(specification)
            }
            sb.append("|")
            motif.passiveSkillSpecifications.forEachIndexed { index, specification ->
                fun <T : Any> f(specification: PassiveSkillSpecification<T>) {
                    if (index > 0) sb.append("&br;")
                    sb.append(specification.effect.getText(specification.valueProvider(10.0)).string)
                }
                f(specification)
            }
            sb.append("|")
            motif.passiveSkillSpecifications.forEachIndexed { index, specification ->
                fun <T : Any> f(specification: PassiveSkillSpecification<T>) {
                    if (index > 0) sb.append("&br;")
                    sb.append(if (specification.conditions.isNotEmpty()) "[" + specification.conditions.map { it.text.string }.join(",") + "]" else "　")
                }
                f(specification)
            }
            sb.append("|")
            sb.append("\n")
        }
        writeAction(player, "dump_fairy_motifs.txt", Emoji.entries.fold(sb.toString()) { s, e -> s.replace(e.charCode, e.string) })
    }
}
