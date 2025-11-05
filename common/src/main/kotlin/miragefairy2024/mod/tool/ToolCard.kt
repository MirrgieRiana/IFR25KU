package miragefairy2024.mod.tool

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.ItemTagCard
import miragefairy2024.mod.PoemList
import miragefairy2024.mod.enchantment.AREA_MINING_ENCHANTABLE_ITEM_TAG
import miragefairy2024.mod.enchantment.EnchantmentCard
import miragefairy2024.mod.materials.Material
import miragefairy2024.mod.materials.MaterialCard
import miragefairy2024.mod.materials.Shape
import miragefairy2024.mod.materials.tag
import miragefairy2024.mod.materials.tagOf
import miragefairy2024.mod.mirageFairy2024ItemGroupCard
import miragefairy2024.mod.poem
import miragefairy2024.mod.registerPoem
import miragefairy2024.mod.registerPoemGeneration
import miragefairy2024.mod.tool.effects.areaMining
import miragefairy2024.mod.tool.effects.collection
import miragefairy2024.mod.tool.effects.effective
import miragefairy2024.mod.tool.effects.enchantable
import miragefairy2024.mod.tool.effects.enchantment
import miragefairy2024.mod.tool.effects.fireResistant
import miragefairy2024.mod.tool.effects.glint
import miragefairy2024.mod.tool.effects.obtainFairy
import miragefairy2024.mod.tool.effects.selfMending
import miragefairy2024.mod.tool.effects.soulStreamContainable
import miragefairy2024.mod.tool.effects.tag
import miragefairy2024.mod.tool.effects.tillingRecipe
import miragefairy2024.mod.tool.items.AdvancedHoeItem
import miragefairy2024.mod.tool.items.FairyAxeConfiguration
import miragefairy2024.mod.tool.items.FairyBattleAxeConfiguration
import miragefairy2024.mod.tool.items.FairyBuildersRodConfiguration
import miragefairy2024.mod.tool.items.FairyHoeConfiguration
import miragefairy2024.mod.tool.items.FairyKnifeConfiguration
import miragefairy2024.mod.tool.items.FairyPickaxeConfiguration
import miragefairy2024.mod.tool.items.FairyScytheConfiguration
import miragefairy2024.mod.tool.items.FairyShootingStaffConfiguration
import miragefairy2024.mod.tool.items.FairyShovelConfiguration
import miragefairy2024.mod.tool.items.FairySwordConfiguration
import miragefairy2024.mod.tool.items.FairyToolProperties
import miragefairy2024.util.AdvancementCard
import miragefairy2024.util.AdvancementCardType
import miragefairy2024.util.EnJa
import miragefairy2024.util.Registration
import miragefairy2024.util.createItemStack
import miragefairy2024.util.enJa
import miragefairy2024.util.on
import miragefairy2024.util.register
import miragefairy2024.util.registerItemGroup
import miragefairy2024.util.registerModelGeneration
import miragefairy2024.util.registerShapedRecipeGeneration
import miragefairy2024.util.toBlockTag
import mirrg.kotlin.helium.toUpperCamelCase
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.data.models.model.ModelTemplates
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.BlockTags
import net.minecraft.tags.ItemTags
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item
import net.minecraft.world.item.component.Tool
import net.minecraft.world.item.enchantment.Enchantments

val MINEABLE_WITH_NOISE_BLOCK_TAG = MirageFairy2024.identifier("mineable/noise").toBlockTag()

context(ModContext)
fun initToolCard() {
    MINEABLE_WITH_NOISE_BLOCK_TAG.enJa(EnJa("Mineable with noise", "ノイズで採掘可能"))
    ToolCard.entries.forEach {
        it.init()
    }
}

class ToolCard(
    path: String,
    private val name: EnJa,
    private val poemList: PoemList,
    private val configuration: ToolConfiguration,
    private val advancementCreator: (ToolCard.(ResourceLocation) -> AdvancementCard)? = null,
    private val initializer: context(ModContext)ToolCard.() -> Unit = {},
) {
    @Suppress("unused", "MemberVisibilityCanBePrivate")
    companion object {
        val entries = mutableListOf<ToolCard>()
        private operator fun ToolCard.not() = this.also { entries.add(this) }

        val IRON_SCYTHE = !ToolCard(
            "iron_scythe", EnJa("Iron Scythe", "鉄の大鎌"),
            PoemList(null),
            FairyScytheConfiguration(ToolMaterialCard.IRON),
        ) { registerScytheRecipeGeneration(item, tagOf(Shape.INGOT, Material.IRON)) }
        val DIAMOND_SCYTHE = !ToolCard(
            "diamond_scythe", EnJa("Diamond Scythe", "ダイヤモンドの大鎌"),
            PoemList(null),
            FairyScytheConfiguration(ToolMaterialCard.DIAMOND),
        ) { registerScytheRecipeGeneration(item, tagOf(Shape.GEM, Material.DIAMOND)) }

        private fun interface Configurator<in T : ToolConfiguration> {
            fun configure(configuration: T)
        }

        private fun pickaxe(path: String, jaName: String, toolMaterialCard: ToolMaterialCard, input: TagKey<Item>, configurator: Configurator<FairyPickaxeConfiguration>) = !ToolCard(
            "${path}_pickaxe", EnJa("${path.toUpperCamelCase("_", " ")} Pickaxe", "${jaName}のつるはし"),
            FairyPickaxeConfiguration(toolMaterialCard).also { configurator.configure(it) }
        ) { registerPickaxeRecipeGeneration(item, input) }

        private fun axe(path: String, jaName: String, toolMaterialCard: ToolMaterialCard, input: TagKey<Item>, configurator: Configurator<FairyAxeConfiguration>) = !ToolCard(
            "${path}_axe", EnJa("${path.toUpperCamelCase("_", " ")} Axe", "${jaName}の斧"),
            FairyAxeConfiguration(toolMaterialCard).also { configurator.configure(it) }
        ) { registerAxeRecipeGeneration(item, input) }

        private fun shovel(path: String, jaName: String, toolMaterialCard: ToolMaterialCard, input: TagKey<Item>, configurator: Configurator<FairyShovelConfiguration>) = !ToolCard(
            "${path}_shovel", EnJa("${path.toUpperCamelCase("_", " ")} Shovel", "${jaName}のシャベル"),
            FairyShovelConfiguration(toolMaterialCard).also { configurator.configure(it) }
        ) { registerShovelRecipeGeneration(item, input) }

        private fun hoe(path: String, jaName: String, toolMaterialCard: ToolMaterialCard, input: TagKey<Item>, configurator: Configurator<FairyHoeConfiguration>) = !ToolCard(
            "${path}_hoe", EnJa("${path.toUpperCamelCase("_", " ")} Hoe", "${jaName}のクワ"),
            FairyHoeConfiguration(toolMaterialCard).also { configurator.configure(it) }
        ) { registerHoeRecipeGeneration(item, input) }

        private fun sword(path: String, jaName: String, toolMaterialCard: ToolMaterialCard, input: TagKey<Item>, configurator: Configurator<FairySwordConfiguration>) = !ToolCard(
            "${path}_sword", EnJa("${path.toUpperCamelCase("_", " ")} Sword", "${jaName}の剣"),
            FairySwordConfiguration(toolMaterialCard).also { configurator.configure(it) }
        ) { registerSwordRecipeGeneration(item, input) }

        class ToolSet(val pickaxe: ToolCard, val axe: ToolCard, val shovel: ToolCard, val hoe: ToolCard, val sword: ToolCard)

        private fun toolSet(path: String, jaName: String, toolMaterialCard: ToolMaterialCard, input: TagKey<Item>, configurator: Configurator<FairyMiningToolConfiguration>): ToolSet {
            return ToolSet(
                pickaxe(path, jaName, toolMaterialCard, input, configurator),
                axe(path, jaName, toolMaterialCard, input, configurator),
                shovel(path, jaName, toolMaterialCard, input, configurator),
                hoe(path, jaName, toolMaterialCard, input, configurator),
                sword(path, jaName, toolMaterialCard, input, configurator),
            )
        }

        val MAGNETITE = toolSet("magnetite", "磁鉄鉱", ToolMaterialCard.MAGNETITE, MaterialCard.MAGNETITE.ore!!.tag) { if (it is FairySwordConfiguration) it.collection() else it.enchantment(EnchantmentCard.STICKY_MINING.key) }
        val BISMUTH = toolSet("bismuth", "ビスマス", ToolMaterialCard.BISMUTH, MaterialCard.BISMUTH_INGOT.ore!!.tag) { it.enchantment(Enchantments.FORTUNE, 1).enchantment(EnchantmentCard.FORTUNE_UP.key, 1).enchantment(Enchantments.UNBREAKING, 3) }
        val COPPER = toolSet("copper", "銅", ToolMaterialCard.COPPER, tagOf(Shape.INGOT, Material.COPPER)) { it }
        val GLASS = toolSet("glass", "ガラス", ToolMaterialCard.GLASS, ConventionalItemTags.GLASS_BLOCKS) { it.enchantment(Enchantments.SHARPNESS, 2).enchantment(EnchantmentCard.CURSE_OF_SHATTERING.key, 1) }
        val FLINT = toolSet("flint", "火打石", ToolMaterialCard.FLINT, tagOf(Shape.GEM, Material.FLINT)) { it.enchantment(Enchantments.FIRE_ASPECT) }
        val FLUORITE = toolSet("fluorite", "蛍石", ToolMaterialCard.FLUORITE, MaterialCard.FLUORITE.ore!!.tag) { if (it is FairySwordConfiguration) it.enchantment(Enchantments.LOOTING, 1) else it.enchantment(Enchantments.FORTUNE, 1) }
        val AMETHYST = toolSet("amethyst", "アメジスト", ToolMaterialCard.AMETHYST, tagOf(Shape.SHARD, Material.AMETHYST)) { if (it is FairySwordConfiguration) it.enchantment(Enchantments.LOOTING, 2) else it.enchantment(Enchantments.FORTUNE, 2) }
        val OBSIDIAN = toolSet("obsidian", "黒曜石", ToolMaterialCard.OBSIDIAN, ConventionalItemTags.OBSIDIANS) { it.enchantment(Enchantments.SHARPNESS, 4).enchantment(EnchantmentCard.CURSE_OF_SHATTERING.key, 3) }
        val EMERALD = toolSet("emerald", "エメラルド", ToolMaterialCard.EMERALD, tagOf(Shape.GEM, Material.EMERALD)) { if (it is FairySwordConfiguration) it.enchantment(Enchantments.LOOTING, 2) else it.enchantment(Enchantments.FORTUNE, 2) }
        val TOPAZ = toolSet("topaz", "トパーズ", ToolMaterialCard.TOPAZ, MaterialCard.TOPAZ.ore!!.tag) { it.enchantment(Enchantments.SHARPNESS, 3).tag(ItemTags.SHARP_WEAPON_ENCHANTABLE) }
        val ECHO_SHARD = toolSet("echo_shard", "残響", ToolMaterialCard.ECHO_SHARD, ItemTagCard.ECHO_SHARDS.tag) { if (it is FairySwordConfiguration) it.enchantment(Enchantments.SHARPNESS, 5) else it.enchantment(Enchantments.EFFICIENCY, 5) }
        val NETHER_STAR = toolSet("nether_star", "ネザースター", ToolMaterialCard.NETHER_STAR, ConventionalItemTags.NETHER_STARS) { if (it is FairySwordConfiguration) it.enchantment(Enchantments.LOOTING, 4).glint() else it.enchantment(Enchantments.FORTUNE, 4).glint() }

        val FAIRY_CRYSTAL_PICKAXE = !ToolCard(
            "fairy_crystal_pickaxe", EnJa("Fairy Crystal Pickaxe", "フェアリークリスタルのつるはし"),
            PoemList(2).poem(EnJa("A brain frozen in crystal", "闇を打ち砕く、透き通る心。")),
            FairyPickaxeConfiguration(ToolMaterialCard.FAIRY_CRYSTAL).selfMending(10).obtainFairy(9.0).soulStreamContainable(),
        ) { registerPickaxeRecipeGeneration(item, MaterialCard.FAIRY_CRYSTAL.ore!!.tag) }
        val FAIRY_CRYSTAL_AXE = !ToolCard(
            "fairy_crystal_axe", EnJa("Fairy Crystal Axe", "フェアリークリスタルの斧"),
            PoemList(2).poem(EnJa("A tree that raised many fairies", "年輪が語る、自然の姿。")),
            FairyAxeConfiguration(ToolMaterialCard.FAIRY_CRYSTAL).selfMending(10).obtainFairy(9.0).soulStreamContainable(),
        ) { registerAxeRecipeGeneration(item, MaterialCard.FAIRY_CRYSTAL.ore!!.tag) }
        val FAIRY_CRYSTAL_SHOVEL = !ToolCard(
            "fairy_crystal_shovel", EnJa("Fairy Crystal Shovel", "フェアリークリスタルのシャベル"),
            PoemList(2).poem(EnJa("Spiritual cycle of carbon", "いつか妖精になるものたち。")),
            FairyShovelConfiguration(ToolMaterialCard.FAIRY_CRYSTAL).selfMending(10).obtainFairy(9.0).soulStreamContainable(),
        ) { registerShovelRecipeGeneration(item, MaterialCard.FAIRY_CRYSTAL.ore!!.tag) }
        val FAIRY_CRYSTAL_HOE = !ToolCard(
            "fairy_crystal_hoe", EnJa("Fairy Crystal Hoe", "フェアリークリスタルのクワ"),
            PoemList(2).poem(EnJa("Essence of life within the soul stream", "ソウルストリームに宿る、生命の息吹。")),
            FairyHoeConfiguration(ToolMaterialCard.FAIRY_CRYSTAL).selfMending(10).obtainFairy(9.0).soulStreamContainable(),
        ) { registerHoeRecipeGeneration(item, MaterialCard.FAIRY_CRYSTAL.ore!!.tag) }
        val FAIRY_CRYSTAL_SCYTHE = !ToolCard(
            "fairy_crystal_scythe", EnJa("Fairy Crystal Scythe", "フェアリークリスタルの大鎌"),
            PoemList(2).poem(EnJa("What color is fairy blood?", "妖精を刈り取るための道具。")),
            FairyScytheConfiguration(ToolMaterialCard.FAIRY_CRYSTAL).selfMending(10).obtainFairy(9.0).soulStreamContainable(),
        ) { registerScytheRecipeGeneration(item, MaterialCard.FAIRY_CRYSTAL.ore!!.tag) }
        val FAIRY_CRYSTAL_SWORD = !ToolCard(
            "fairy_crystal_sword", EnJa("Fairy Crystal Sword", "フェアリークリスタルの剣"),
            PoemList(2).poem(EnJa("Nutrients for the soul", "妖精はこれをおやつにするという")),
            FairySwordConfiguration(ToolMaterialCard.FAIRY_CRYSTAL).selfMending(10).obtainFairy(9.0).soulStreamContainable(),
        ) { registerSwordRecipeGeneration(item, MaterialCard.FAIRY_CRYSTAL.ore!!.tag) }
        val FAIRY_CRYSTAL_BATTLE_AXE = !ToolCard(
            "fairy_crystal_battle_axe", EnJa("Fairy Crystal Battle Axe", "フェアリークリスタルの戦斧"),
            PoemList(2).poem(EnJa("The embodiment of fighting spirit", "妖精の本能を呼び覚ませ。")),
            FairyBattleAxeConfiguration(ToolMaterialCard.FAIRY_CRYSTAL).selfMending(10).obtainFairy(9.0).soulStreamContainable(),
        ) { registerBattleAxeRecipeGeneration(item, MaterialCard.FAIRY_CRYSTAL.ore!!.tag) }
        val MIRAGIUM_PICKAXE = !ToolCard(
            "miragium_pickaxe", EnJa("Miragium Pickaxe", "ミラジウムのつるはし"),
            PoemList(3).poem(EnJa("More durable than gold", "妖精の肉体労働")),
            FairyPickaxeConfiguration(ToolMaterialCard.MIRAGIUM).selfMending(20).enchantment(EnchantmentCard.MINE_ALL.key).enchantment(EnchantmentCard.AREA_MINING_ACCELERATION.key, 3).soulStreamContainable(),
        ) { registerPickaxeRecipeGeneration(item, MaterialCard.MIRAGIUM_INGOT.ore!!.tag) }
        val MIRAGIUM_AXE = !ToolCard(
            "miragium_axe", EnJa("Miragium Axe", "ミラジウムの斧"),
            PoemList(3).poem(EnJa("Crack! Squish!", "バキッ！ぐにっ")),
            FairyAxeConfiguration(ToolMaterialCard.MIRAGIUM).selfMending(20).enchantment(EnchantmentCard.CUT_ALL.key).enchantment(EnchantmentCard.AREA_MINING_ACCELERATION.key, 3).soulStreamContainable(),
        ) { registerAxeRecipeGeneration(item, MaterialCard.MIRAGIUM_INGOT.ore!!.tag) }
        val LILAGIUM_SCYTHE = !ToolCard(
            "lilagium_scythe", EnJa("Lilagium Scythe", "リラジウムの大鎌"),
            PoemList(3).poem(EnJa("Wish upon the grass", "葉っぱが吸い込まれてくる")),
            FairyScytheConfiguration(ToolMaterialCard.LILAGIUM, range = 2).enchantment(EnchantmentCard.STICKY_MINING.key).soulStreamContainable(),
        ) { registerScytheRecipeGeneration(item, MaterialCard.LILAGIUM_INGOT.ore!!.tag) }
        val LUMINITE_PICKAXE = !ToolCard(
            "luminite_pickaxe", EnJa("Luminite Pickaxe", "ルミナイトのつるはし"),
            PoemList(4).poem(EnJa("Energetic soul extract", "精製された魂の残滓。")),
            FairyPickaxeConfiguration(ToolMaterialCard.LUMINITE).enchantment(Enchantments.EFFICIENCY, 4).enchantment(Enchantments.FORTUNE, 3),
        ) { registerPickaxeRecipeGeneration(item, MaterialCard.LUMINITE.ore!!.tag) }
        val LUMINITE_AXE = !ToolCard(
            "luminite_axe", EnJa("Luminite Axe", "ルミナイトの斧"),
            PoemList(4).poem(EnJa("Spiritual ectoplasm recycler", "失われた記憶の断片。")),
            FairyAxeConfiguration(ToolMaterialCard.LUMINITE).enchantment(Enchantments.EFFICIENCY, 4).enchantment(Enchantments.FORTUNE, 3),
        ) { registerAxeRecipeGeneration(item, MaterialCard.LUMINITE.ore!!.tag) }
        val LUMINITE_SWORD = !ToolCard(
            "luminite_sword", EnJa("Luminite Sword", "ルミナイトの剣"),
            PoemList(4).poem(EnJa("Bionic etheroluminescence illuminator", "光を生み出す力。")),
            FairySwordConfiguration(ToolMaterialCard.LUMINITE).enchantment(Enchantments.SHARPNESS, 4).enchantment(Enchantments.LOOTING, 3),
        ) { registerSwordRecipeGeneration(item, MaterialCard.LUMINITE.ore!!.tag) }
        val RESONITE_PICKAXE = !ToolCard(
            "resonite_pickaxe", EnJa("Resonance Pickaxe", "共鳴のつるはし"),
            PoemList(5).poem(EnJa("The compound of light and sound", "光と闇の純結晶。")),
            FairyPickaxeConfiguration(ToolMaterialCard.RESONITE).enchantment(EnchantmentCard.MINE_ALL.key).enchantment(Enchantments.EFFICIENCY, 6).soulStreamContainable(),
        ) { registerPickaxeRecipeGeneration(item, MaterialCard.RESONITE_INGOT.ore!!.tag) }
        val RESONITE_AXE = !ToolCard(
            "resonite_axe", EnJa("Resonance Axe", "共鳴の斧"),
            PoemList(5).poem(EnJa("Wavelength matched to tree height", "共振する樹の繊維。")),
            FairyAxeConfiguration(ToolMaterialCard.RESONITE).enchantment(EnchantmentCard.CUT_ALL.key).enchantment(Enchantments.EFFICIENCY, 6).soulStreamContainable(),
        ) { registerAxeRecipeGeneration(item, MaterialCard.RESONITE_INGOT.ore!!.tag) }
        val RESONITE_KNIFE = !ToolCard(
            "resonite_knife", EnJa("Resonite Knife", "共鳴のナイフ"),
            PoemList(5).poem(EnJa("The ultrasonic vibration knife", "音波を超えた破壊の力。")),
            FairyKnifeConfiguration(ToolMaterialCard.RESONITE).areaMining(1, 1, 1).enchantment(Enchantments.EFFICIENCY, 6).soulStreamContainable(),
        ) { registerKnifeRecipeGeneration(item, MaterialCard.RESONITE_INGOT.ore!!.tag) }
        val RESONITE_SCYTHE = !ToolCard(
            "resonite_scythe", EnJa("Resonite Scythe", "共鳴の大鎌"),
            PoemList(5).poem(EnJa("Vacuum wave that cuts down grasses", "虚空を切り裂く碧の波。")),
            FairyScytheConfiguration(ToolMaterialCard.RESONITE).enchantment(Enchantments.SWEEPING_EDGE, 3).enchantment(Enchantments.EFFICIENCY, 6).soulStreamContainable(),
        ) { registerScytheRecipeGeneration(item, MaterialCard.RESONITE_INGOT.ore!!.tag) }
        val RESONITE_SWORD = !ToolCard(
            "resonite_sword", EnJa("Resonite Sword", "共鳴の剣"),
            PoemList(5).poem(EnJa("The pulsating ectoplasm", "生命の脈動を砥ぎ澄ませ。")),
            FairySwordConfiguration(ToolMaterialCard.RESONITE).enchantment(Enchantments.SWEEPING_EDGE, 3).enchantment(Enchantments.SHARPNESS, 6).soulStreamContainable(),
        ) { registerSwordRecipeGeneration(item, MaterialCard.RESONITE_INGOT.ore!!.tag) }
        val RESONITE_BATTLE_AXE = !ToolCard(
            "resonite_battle_axe", EnJa("Resonite Battle Axe", "共鳴の戦斧"),
            PoemList(5).poem(EnJa("The mind synchronize with the body", "精神と肉体の調和。")),
            FairyBattleAxeConfiguration(ToolMaterialCard.RESONITE).enchantment(EnchantmentCard.CUT_ALL.key).enchantment(Enchantments.SHARPNESS, 6).soulStreamContainable(),
        ) { registerBattleAxeRecipeGeneration(item, MaterialCard.RESONITE_INGOT.ore!!.tag) }
        val PROMINITE_PICKAXE = !ToolCard(
            "prominite_pickaxe", EnJa("Prominite Pickaxe", "プロミナイトのつるはし"),
            PoemList(4).poem(EnJa("Refined soul fuel.", "打ち砕かれた魂の残骸。")),
            FairyPickaxeConfiguration(ToolMaterialCard.PROMINITE).fireResistant().enchantment(EnchantmentCard.SMELTING.key),
        ) { registerPickaxeRecipeGeneration(item, MaterialCard.PROMINITE.ore!!.tag) }
        val PROMINITE_AXE = !ToolCard(
            "prominite_axe", EnJa("Prominite Axe", "プロミナイトの斧"),
            PoemList(4).poem(EnJa("Unpredictable movement of flames.", "カオスの鉄槌。")),
            FairyAxeConfiguration(ToolMaterialCard.PROMINITE).fireResistant().enchantment(EnchantmentCard.SMELTING.key),
        ) { registerAxeRecipeGeneration(item, MaterialCard.PROMINITE.ore!!.tag) }
        val PROMINITE_SHOVEL = !ToolCard(
            "prominite_shovel", EnJa("Prominite Shovel", "プロミナイトのシャベル"),
            PoemList(4).poem(EnJa("Flame that burns misfortune.", "救済の火。")),
            FairyShovelConfiguration(ToolMaterialCard.PROMINITE).fireResistant().enchantment(EnchantmentCard.SMELTING.key),
        ) { registerShovelRecipeGeneration(item, MaterialCard.PROMINITE.ore!!.tag) }
        val PROMINITE_HOE = !ToolCard(
            "prominite_hoe", EnJa("Prominite Hoe", "プロミナイトのクワ"),
            PoemList(4).poem(EnJa("Regenerated spirit.", "生命の萌芽。")),
            FairyHoeConfiguration(ToolMaterialCard.PROMINITE).fireResistant().enchantment(EnchantmentCard.SMELTING.key).tillingRecipe(AdvancedHoeItem.ROUGHEN_RECIPE),
        ) { registerHoeRecipeGeneration(item, MaterialCard.PROMINITE.ore!!.tag) }
        val PROMINITE_SWORD = !ToolCard(
            "prominite_sword", EnJa("Prominite Sword", "プロミナイトの剣"),
            PoemList(4).poem(EnJa("Struggle for the fate.", "「存在」の争奪。")),
            FairySwordConfiguration(ToolMaterialCard.PROMINITE).fireResistant().enchantment(Enchantments.FIRE_ASPECT),
        ) { registerSwordRecipeGeneration(item, MaterialCard.PROMINITE.ore!!.tag) }
        val PROMINITE_SCYTHE = !ToolCard(
            "prominite_scythe", EnJa("Prominite Scythe", "プロミナイトの大鎌"),
            PoemList(4).poem(EnJa("Piles of what was once consciousness.", "折り重なる沈黙の宴。")),
            FairyScytheConfiguration(ToolMaterialCard.PROMINITE).fireResistant().enchantment(EnchantmentCard.SMELTING.key),
        ) { registerScytheRecipeGeneration(item, MaterialCard.PROMINITE.ore!!.tag) }
        val MIRAGIDIAN_PICKAXE = !ToolCard(
            "miragidian_pickaxe", EnJa("Miragidian Pickaxe", "ミラジディアンのつるはし"),
            PoemList(4).poem(EnJa("The reinforced will of the substance", "高速度鋼という名の誇り。")),
            FairyPickaxeConfiguration(ToolMaterialCard.MIRAGIDIAN).soulStreamContainable().fireResistant(),
        ) { registerPickaxeRecipeGeneration(item, MaterialCard.MIRAGIDIAN.ore!!.tag) }
        val MIRAGIDIAN_AXE = !ToolCard(
            "miragidian_axe", EnJa("Miragidian Axe", "ミラジディアンの斧"),
            PoemList(4).poem(EnJa("The Ancient Future Relic", "古代の未来遺物。")),
            FairyAxeConfiguration(ToolMaterialCard.MIRAGIDIAN).soulStreamContainable().fireResistant(),
        ) { registerAxeRecipeGeneration(item, MaterialCard.MIRAGIDIAN.ore!!.tag) }
        val MIRAGIDIAN_SHOVEL = !ToolCard(
            "miragidian_shovel", EnJa("Miragidian Shovel", "ミラジディアンのシャベル"),
            PoemList(4).poem(EnJa("An alloy that will outlive humanity", "文明の光よりも堅牢な闇。")),
            FairyShovelConfiguration(ToolMaterialCard.MIRAGIDIAN).soulStreamContainable().fireResistant(),
        ) { registerShovelRecipeGeneration(item, MaterialCard.MIRAGIDIAN.ore!!.tag) }
        val MIRAGIDIAN_HOE = !ToolCard(
            "miragidian_hoe", EnJa("Miragidian Hoe", "ミラジディアンのクワ"),
            PoemList(4).poem(EnJa("They were killed by containment targets", "宇宙人類の然程輝きのない栄光。")),
            FairyHoeConfiguration(ToolMaterialCard.MIRAGIDIAN).soulStreamContainable().fireResistant(),
        ) { registerHoeRecipeGeneration(item, MaterialCard.MIRAGIDIAN.ore!!.tag) }
        val MIRAGIDIAN_SWORD = !ToolCard(
            "miragidian_sword", EnJa("Miragidian Sword", "ミラジディアンの剣"),
            PoemList(4).poem(EnJa("Watching the end of civilization.", "この星の運命を写す瞳。")), // TODO 石の意思
            FairySwordConfiguration(ToolMaterialCard.MIRAGIDIAN).soulStreamContainable().fireResistant(),
        ) { registerSwordRecipeGeneration(item, MaterialCard.MIRAGIDIAN.ore!!.tag) }
        val MIRANAGITE_KNIFE = !ToolCard(
            "miranagite_knife", EnJa("Miranagite Knife", "蒼天石のナイフ"),
            PoemList(2).poem(EnJa("Gardener's tool invented by Miranagi", "大自然を駆ける探究者のナイフ。")),
            FairyKnifeConfiguration(ToolMaterialCard.MIRANAGITE).enchantment(Enchantments.SILK_TOUCH),
        ) { registerKnifeRecipeGeneration(item, MaterialCard.MIRANAGITE.ore!!.tag) }
        val MIRANAGITE_PICKAXE = !ToolCard(
            "miranagite_pickaxe", EnJa("Miranagite Pickaxe", "蒼天石のつるはし"),
            PoemList(2).poem(EnJa("Promotes ore recrystallization", "凝集する秩序、蒼穹彩煌が如く。")),
            FairyPickaxeConfiguration(ToolMaterialCard.MIRANAGITE).enchantment(Enchantments.SILK_TOUCH),
        ) { registerPickaxeRecipeGeneration(item, MaterialCard.MIRANAGITE.ore!!.tag) }
        val MIRANAGITE_SCYTHE = !ToolCard(
            "miranagite_scythe", EnJa("Miranagite Scythe", "蒼天石の大鎌"),
            PoemList(2).poem(EnJa("Releases the souls of weeds", "宙を切り裂く創世の刃、草魂を蒼天へ導く。")),
            FairyScytheConfiguration(ToolMaterialCard.MIRANAGITE).enchantment(Enchantments.SILK_TOUCH),
        ) { registerScytheRecipeGeneration(item, MaterialCard.MIRANAGITE.ore!!.tag) }
        val MIRANAGI_STAFF_0 = !ToolCard(
            "miranagi_staff_0", EnJa("Miranagite Staff", "蒼天石のスタッフ"),
            PoemList(2).poem(EnJa("Inflating anti-entropy force", "膨張する秩序の力。")),
            FairyShootingStaffConfiguration(ToolMaterialCard.MIRANAGITE, 5F, 12F).enchantment(Enchantments.SILK_TOUCH),
        ) {
            registerShapedRecipeGeneration(item) {
                pattern(" IG")
                pattern(" RI")
                pattern("I  ")
                define('R', tagOf(Shape.ROD, Material.MIRANAGITE))
                define('G', ConventionalItemTags.GLASS_BLOCKS)
                define('I', tagOf(Shape.INGOT, Material.COPPER))
            } on MaterialCard.MIRANAGITE.ore!!.tag
        }
        val MIRANAGI_STAFF = !ToolCard(
            "miranagi_staff", EnJa("Staff of Miranagi", "みらなぎの杖"),
            PoemList(3).poem(EnJa("Risk of vacuum decay due to anti-entropy", "創世の神光は混沌をも翻す。")),
            FairyShootingStaffConfiguration(ToolMaterialCard.MIRANAGITE, 7F, 16F).enchantment(Enchantments.SILK_TOUCH),
            advancementCreator = {
                AdvancementCard(
                    identifier = it,
                    context = AdvancementCard.Sub { MaterialCard.MIRANAGITE.advancement!!.await() },
                    icon = { item().createItemStack() },
                    name = EnJa("Innocent Dogma", "盲目のドグマ"),
                    description = EnJa("Craft the Staff of Miranagi", "みらなぎの杖を作成する"),
                    criterion = AdvancementCard.hasItem(item),
                    type = AdvancementCardType.NORMAL,
                )
            },
        ) {
            registerShapedRecipeGeneration(item) {
                pattern(" IG")
                pattern(" #I")
                pattern("N  ")
                define('#', MIRANAGI_STAFF_0.item())
                define('G', tagOf(Shape.GEM, Material.DIAMOND))
                define('I', tagOf(Shape.INGOT, Material.IRON))
                define('N', tagOf(Shape.NUGGET, Material.IRON))
            } on MaterialCard.MIRANAGITE.ore!!.tag
        }
        val XARPITE_PICKAXE = !ToolCard(
            "xarpite_pickaxe", EnJa("Xarpite Pickaxe", "紅天石のつるはし"),
            PoemList(2).poem(EnJa("Shears space using astral induction", "鉱石の魂を貪る血塗られた有機質。")),
            FairyPickaxeConfiguration(ToolMaterialCard.XARPITE).enchantment(EnchantmentCard.MINE_ALL.key).enchantment(EnchantmentCard.AREA_MINING_ACCELERATION.key, 5).collection().enchantment(EnchantmentCard.STICKY_MINING.key),
        ) { registerPickaxeRecipeGeneration(item, MaterialCard.XARPITE.ore!!.tag) }
        val XARPITE_AXE = !ToolCard(
            "xarpite_axe", EnJa("Xarpite Axe", "紅天石の斧"),
            PoemList(2).poem(EnJa("Strip the log from the space", "空間にこびりついた丸太の除去に。")),
            FairyAxeConfiguration(ToolMaterialCard.XARPITE).enchantment(EnchantmentCard.CUT_ALL.key).enchantment(EnchantmentCard.AREA_MINING_ACCELERATION.key, 5).collection().enchantment(EnchantmentCard.STICKY_MINING.key),
        ) { registerAxeRecipeGeneration(item, MaterialCard.XARPITE.ore!!.tag) }
        val CHAOS_STONE_PICKAXE = !ToolCard(
            "chaos_stone_pickaxe", EnJa("Chaos Stone Pickaxe", "混沌のつるはし"),
            PoemList(4).poem(EnJa("Is this made of metal? Or clay?", "時空結晶の交点に、古代の産業が芽吹く。")),
            FairyPickaxeConfiguration(ToolMaterialCard.CHAOS_STONE).effective(BlockTags.MINEABLE_WITH_SHOVEL).areaMining(1, 2, 0),
        ) { registerPickaxeRecipeGeneration(item, MaterialCard.CHAOS_STONE.ore!!.tag) }
        val CALCULITE_PICKAXE = !ToolCard(
            "calculite_pickaxe", EnJa("Calculite Pickaxe", "理天石のつるはし"),
            PoemList(5).poem(EnJa("Integrated neural circuits.", "理性の結晶。")),
            FairyPickaxeConfiguration(ToolMaterialCard.CALCULITE).enchantment(EnchantmentCard.AREA_MINING_ACCELERATION.key, 2).enchantable(AREA_MINING_ENCHANTABLE_ITEM_TAG),
        ) { registerPickaxeRecipeGeneration(item, MaterialCard.CALCULITE.ore!!.tag) }
        val CALCULITE_AXE = !ToolCard(
            "calculite_axe", EnJa("Calculite Axe", "理天石の斧"),
            PoemList(5).poem(EnJa("A world where chaos resolves everything.", "秩序がすべてを結束する世界。")),
            FairyAxeConfiguration(ToolMaterialCard.CALCULITE).enchantment(EnchantmentCard.AREA_MINING_ACCELERATION.key, 2).enchantable(AREA_MINING_ENCHANTABLE_ITEM_TAG),
        ) { registerAxeRecipeGeneration(item, MaterialCard.CALCULITE.ore!!.tag) }
        val CALCULITE_SHOVEL = !ToolCard(
            "calculite_shovel", EnJa("Calculite Shovel", "理天石のシャベル"),
            PoemList(5).poem(EnJa("Biological parallel processor.", "汎用デジタル集合知。")),
            FairyShovelConfiguration(ToolMaterialCard.CALCULITE).enchantment(EnchantmentCard.AREA_MINING_ACCELERATION.key, 2).enchantable(AREA_MINING_ENCHANTABLE_ITEM_TAG),
        ) { registerShovelRecipeGeneration(item, MaterialCard.CALCULITE.ore!!.tag) }
        val CALCULITE_HOE = !ToolCard(
            "calculite_hoe", EnJa("Calculite Hoe", "理天石のクワ"),
            PoemList(5).poem(EnJa("A stellar automaton.", "幽星質の脳細胞。")),
            FairyHoeConfiguration(ToolMaterialCard.CALCULITE).enchantment(EnchantmentCard.AREA_MINING_ACCELERATION.key, 2).enchantable(AREA_MINING_ENCHANTABLE_ITEM_TAG),
        ) { registerHoeRecipeGeneration(item, MaterialCard.CALCULITE.ore!!.tag) }
        val CALCULITE_SCYTHE = !ToolCard(
            "calculite_scythe", EnJa("Calculite Scythe", "理天石の大鎌"),
            PoemList(5).poem(EnJa("Perpetual-motion biocomputer.", "人工生命は何を思い、そして感じるのか。")),
            FairyScytheConfiguration(ToolMaterialCard.CALCULITE).enchantment(EnchantmentCard.AREA_MINING_ACCELERATION.key, 2).enchantable(AREA_MINING_ENCHANTABLE_ITEM_TAG),
        ) { registerScytheRecipeGeneration(item, MaterialCard.CALCULITE.ore!!.tag) }
        val CALCULITE_BUILDERS_ROD = !ToolCard(
            "calculite_builders_rod", EnJa("Builder's Rod", "ビルダーズロッド"),
            PoemList(5).poem(EnJa("End-members of Miranagite.", "宇宙一明晰なビスマス化合物。")),
            FairyBuildersRodConfiguration(ToolMaterialCard.CALCULITE).enchantment(EnchantmentCard.AREA_MINING_ACCELERATION.key, 2).enchantable(AREA_MINING_ENCHANTABLE_ITEM_TAG),
        ) {
            registerShapedRecipeGeneration(item) {
                pattern(" GG")
                pattern(" GG")
                pattern("R  ")
                define('G', tagOf(Shape.GEM, Material.CALCULITE))
                define('R', tagOf(Shape.ROD, Material.WOOD))
            } on MaterialCard.CALCULITE.ore!!.tag
        }
        val NOISE_PICKAXE = !ToolCard(
            "noise_pickaxe", EnJa("Noise Pickaxe", "ノイズのつるはし"),
            PoemList(5).poem(EnJa("Demolish the Value.", "無価値の創出。")),
            FairyPickaxeConfiguration(ToolMaterialCard.NOISE).selfMending(100).enchantment(Enchantments.UNBREAKING, 7).enchantment(Enchantments.MENDING).enchantment(Enchantments.INFINITY).effective(MINEABLE_WITH_NOISE_BLOCK_TAG).soulStreamContainable(),
        ) { registerPickaxeRecipeGeneration(item, MaterialCard.NOISE.ore!!.tag) }
        val NOISE_AXE = !ToolCard(
            "noise_axe", EnJa("Noise Axe", "ノイズの斧"),
            PoemList(5).poem(EnJa("What is the value in a work?", "人知れずノイズが現れ、そして消える。")),
            FairyAxeConfiguration(ToolMaterialCard.NOISE).selfMending(100).enchantment(Enchantments.UNBREAKING, 7).enchantment(Enchantments.MENDING).enchantment(Enchantments.INFINITY).effective(MINEABLE_WITH_NOISE_BLOCK_TAG).soulStreamContainable(),
        ) { registerAxeRecipeGeneration(item, MaterialCard.NOISE.ore!!.tag) }
        val NOISE_SHOVEL = !ToolCard(
            "noise_shovel", EnJa("Noise Shovel", "ノイズのシャベル"),
            PoemList(5).poem(EnJa("A world where everything exists.", "可能なすべての表現の集合。")),
            FairyShovelConfiguration(ToolMaterialCard.NOISE).selfMending(100).enchantment(Enchantments.UNBREAKING, 7).enchantment(Enchantments.MENDING).enchantment(Enchantments.INFINITY).effective(MINEABLE_WITH_NOISE_BLOCK_TAG).soulStreamContainable(),
        ) { registerShovelRecipeGeneration(item, MaterialCard.NOISE.ore!!.tag) }
        val NOISE_HOE = !ToolCard(
            "noise_hoe", EnJa("Noise Hoe", "ノイズのクワ"),
            PoemList(5).poem(EnJa("Ideational waste.", "哲学的うんこ。")),
            FairyHoeConfiguration(ToolMaterialCard.NOISE).selfMending(100).enchantment(Enchantments.UNBREAKING, 7).enchantment(Enchantments.MENDING).enchantment(Enchantments.INFINITY).effective(MINEABLE_WITH_NOISE_BLOCK_TAG).soulStreamContainable(),
        ) { registerHoeRecipeGeneration(item, MaterialCard.NOISE.ore!!.tag) }
        val NOISE_SWORD = !ToolCard(
            "noise_sword", EnJa("Noise Sword", "ノイズの剣"),
            PoemList(5).poem(EnJa("Liberation in the name of nonsense.", "意味という名の抑圧。")),
            FairySwordConfiguration(ToolMaterialCard.NOISE).selfMending(100).enchantment(Enchantments.UNBREAKING, 7).enchantment(Enchantments.MENDING).enchantment(Enchantments.INFINITY).effective(MINEABLE_WITH_NOISE_BLOCK_TAG).soulStreamContainable(),
        ) { registerSwordRecipeGeneration(item, MaterialCard.NOISE.ore!!.tag) }
        val PHANTOM_PICKAXE = !ToolCard(
            "phantom_pickaxe", EnJa("Phantom Pickaxe", "幻想のつるはし"),
            PoemList(4).poem(EnJa("\"Creation\" is the true power.", "人間が手にした唯一の幻想。")),
            FairyPickaxeConfiguration(ToolMaterialCard.PHANTOM_DROP).selfMending(20).obtainFairy(9.0 * 9.0),
        ) { registerPickaxeRecipeGeneration(item, MaterialCard.PHANTOM_DROP.ore!!.tag) }
        val PHANTOM_SHOVEL = !ToolCard(
            "phantom_shovel", EnJa("Phantom Shovel", "幻想のシャベル"),
            PoemList(4).poem(EnJa("The sound of the world's end echoed", "破壊された世界の音――")),
            FairyShovelConfiguration(ToolMaterialCard.PHANTOM_DROP).selfMending(20).obtainFairy(9.0 * 9.0),
        ) { registerShovelRecipeGeneration(item, MaterialCard.PHANTOM_DROP.ore!!.tag) }
        val PHANTOM_SWORD = !ToolCard(
            "phantom_sword", EnJa("Phantom Sword", "幻想の剣"),
            PoemList(4).poem(EnJa("Pray. For rebirth.", "闇を切り裂く、再生の光。")),
            FairySwordConfiguration(ToolMaterialCard.PHANTOM_DROP).selfMending(20).obtainFairy(9.0 * 9.0),
        ) { registerSwordRecipeGeneration(item, MaterialCard.PHANTOM_DROP.ore!!.tag) }
        val HAIMEVISKA_ROSIN_PICKAXE = !ToolCard(
            "haimeviska_rosin_pickaxe", EnJa("Rosin Pickaxe", "涙のつるはし"),
            PoemList(2).poem(EnJa("Enduring in the stone", "生きた証は石の中。")),
            FairyPickaxeConfiguration(ToolMaterialCard.HAIMEVISKA_ROSIN).areaMining(1, 0, 0).enchantment(EnchantmentCard.AREA_MINING_ACCELERATION.key, 3).enchantment(EnchantmentCard.STICKY_MINING.key).enchantment(Enchantments.UNBREAKING, 2),
        ) { registerPickaxeRecipeGeneration(item, MaterialCard.HAIMEVISKA_ROSIN.ore!!.tag) }
        val HAIMEVISKA_ROSIN_AXE = !ToolCard(
            "haimeviska_rosin_axe", EnJa("Rosin Axe", "涙の斧"),
            PoemList(2).poem(EnJa("That which might once have been a friend", "したたる樹液は、誰の涙か。")),
            FairyAxeConfiguration(ToolMaterialCard.HAIMEVISKA_ROSIN).areaMining(1, 0, 0).enchantment(EnchantmentCard.AREA_MINING_ACCELERATION.key, 3).enchantment(EnchantmentCard.STICKY_MINING.key).enchantment(Enchantments.UNBREAKING, 2),
        ) { registerAxeRecipeGeneration(item, MaterialCard.HAIMEVISKA_ROSIN.ore!!.tag) }
        val HAIMEVISKA_ROSIN_SHOVEL = !ToolCard(
            "haimeviska_rosin_shovel", EnJa("Rosin Shovel", "涙のシャベル"),
            PoemList(2).poem(EnJa("The story of the lonely tree", "琥珀のしずくが刻む道。")),
            FairyShovelConfiguration(ToolMaterialCard.HAIMEVISKA_ROSIN).areaMining(1, 0, 0).enchantment(EnchantmentCard.AREA_MINING_ACCELERATION.key, 3).enchantment(EnchantmentCard.STICKY_MINING.key).enchantment(Enchantments.UNBREAKING, 2),
        ) { registerShovelRecipeGeneration(item, MaterialCard.HAIMEVISKA_ROSIN.ore!!.tag) }
        val HAIMEVISKA_ROSIN_HOE = !ToolCard(
            "haimeviska_rosin_hoe", EnJa("Rosin Hoe", "涙のクワ"),
            PoemList(2).poem(EnJa("Looking for companions", "そこにあるべき大樹を見上げ。")),
            FairyHoeConfiguration(ToolMaterialCard.HAIMEVISKA_ROSIN).areaMining(1, 0, 0).enchantment(EnchantmentCard.AREA_MINING_ACCELERATION.key, 3)/* TODO 範囲耕作 */.enchantment(EnchantmentCard.STICKY_MINING.key).enchantment(Enchantments.UNBREAKING, 2),
        ) { registerHoeRecipeGeneration(item, MaterialCard.HAIMEVISKA_ROSIN.ore!!.tag) }
        val HAIMEVISKA_ROSIN_SWORD = !ToolCard(
            "haimeviska_rosin_sword", EnJa("Rosin Sword", "涙の剣"),
            PoemList(2).poem(EnJa("The oppressed Viska's desire", "傷つけられた幹の声。")),
            FairySwordConfiguration(ToolMaterialCard.HAIMEVISKA_ROSIN).enchantment(Enchantments.SWEEPING_EDGE, 3).collection().enchantment(Enchantments.UNBREAKING, 2),
        ) { registerSwordRecipeGeneration(item, MaterialCard.HAIMEVISKA_ROSIN.ore!!.tag) }
        // TODO 地脈を流れる大樹の血。 妖精のプラスチックのつるはし

        val CREATIVE_HOE = !ToolCard(
            "creative_hoe", EnJa("Creative Hoe", "アカーシャのクワ"),
            PoemList(null).poem(EnJa("Changes everything into farmland.", "適度に湿った地が現れよ。")),
            FairyHoeConfiguration(ToolMaterialCard.NEUTRONIUM).tillingRecipe(AdvancedHoeItem.CREATIVE_RECIPE),
        )
    }

    val identifier = MirageFairy2024.identifier(path)

    init {
        configuration.apply()
    }

    val item = Registration(BuiltInRegistries.ITEM, identifier) {
        configuration.createItem(run {
            val miningSpeedMultiplier = configuration.miningSpeedMultiplierOverride ?: configuration.toolMaterialCard.toolMaterial.speed

            val rules = mutableListOf<Tool.Rule>()

            rules += Tool.Rule.deniesDrops(configuration.toolMaterialCard.toolMaterial.incorrectBlocksForDrops) // ツールレベル不足で掘れない
            if (configuration.superEffectiveBlocks.isNotEmpty()) rules += Tool.Rule.minesAndDrops(configuration.superEffectiveBlocks, miningSpeedMultiplier * 10F) // 剣の蜘蛛の巣特効とか
            if (configuration.effectiveBlocks.isNotEmpty()) rules += Tool.Rule.minesAndDrops(configuration.effectiveBlocks, miningSpeedMultiplier) // 特別に対応してるブロック
            configuration.effectiveBlockTags.forEach { // タグによる適正
                rules += Tool.Rule.minesAndDrops(it, miningSpeedMultiplier)
            }

            FairyToolProperties(Tool(rules, 1F, configuration.miningDamage))
                .let { if (configuration.fireResistant) it.fireResistant() else it }
        })
    }

    val advancement = advancementCreator?.invoke(this, identifier)

    context(ModContext)
    fun init() {
        item.register()

        item.registerItemGroup(mirageFairy2024ItemGroupCard.itemGroupKey)

        item.registerModelGeneration(ModelTemplates.FLAT_HANDHELD_ITEM)

        item.enJa(name)

        val poemList2 = configuration.appendPoems(poemList)
        item.registerPoem(poemList2)
        item.registerPoemGeneration(poemList2)

        if (advancement != null) advancement.init()

        configuration.init(this)
        initializer(this@ModContext, this)
    }
}

private fun ToolCard(
    path: String,
    name: EnJa,
    configuration: ToolConfiguration,
    advancementCreator: (ToolCard.(ResourceLocation) -> AdvancementCard)? = null,
    initializer: context(ModContext)ToolCard.() -> Unit = {},
) = ToolCard(path, name, PoemList(null), configuration, advancementCreator, initializer)

context(ModContext)
private fun registerPickaxeRecipeGeneration(item: () -> Item, input: TagKey<Item>) = registerShapedRecipeGeneration(item) {
    pattern("###")
    pattern(" R ")
    pattern(" R ")
    define('#', input)
    define('R', tagOf(Shape.ROD, Material.WOOD))
} on input

context(ModContext)
private fun registerAxeRecipeGeneration(item: () -> Item, input: TagKey<Item>) = registerShapedRecipeGeneration(item) {
    pattern("##")
    pattern("#R")
    pattern(" R")
    define('#', input)
    define('R', tagOf(Shape.ROD, Material.WOOD))
} on input

context(ModContext)
private fun registerShovelRecipeGeneration(item: () -> Item, input: TagKey<Item>) = registerShapedRecipeGeneration(item) {
    pattern("#")
    pattern("R")
    pattern("R")
    define('#', input)
    define('R', tagOf(Shape.ROD, Material.WOOD))
} on input

context(ModContext)
private fun registerHoeRecipeGeneration(item: () -> Item, input: TagKey<Item>) = registerShapedRecipeGeneration(item) {
    pattern("##")
    pattern(" R")
    pattern(" R")
    define('#', input)
    define('R', tagOf(Shape.ROD, Material.WOOD))
} on input

context(ModContext)
private fun registerKnifeRecipeGeneration(item: () -> Item, input: TagKey<Item>) = registerShapedRecipeGeneration(item) {
    pattern("#")
    pattern("R")
    define('#', input)
    define('R', tagOf(Shape.ROD, Material.WOOD))
} on input

context(ModContext)
private fun registerScytheRecipeGeneration(item: () -> Item, input: TagKey<Item>) = registerShapedRecipeGeneration(item) {
    pattern(" ##")
    pattern("# R")
    pattern("  R")
    define('#', input)
    define('R', tagOf(Shape.ROD, Material.WOOD))
} on input

context(ModContext)
private fun registerSwordRecipeGeneration(item: () -> Item, input: TagKey<Item>) = registerShapedRecipeGeneration(item) {
    pattern("#")
    pattern("#")
    pattern("R")
    define('#', input)
    define('R', tagOf(Shape.ROD, Material.WOOD))
} on input

context(ModContext)
private fun registerBattleAxeRecipeGeneration(item: () -> Item, input: TagKey<Item>) = registerShapedRecipeGeneration(item) {
    pattern("###")
    pattern("#R#")
    pattern(" R ")
    define('#', input)
    define('R', tagOf(Shape.ROD, Material.WOOD))
} on input
