package miragefairy2024.mod.tool

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.ItemTagCard
import miragefairy2024.mod.materials.Material
import miragefairy2024.mod.materials.Shape
import miragefairy2024.mod.materials.tagOf
import miragefairy2024.util.EnJa
import miragefairy2024.util.enJa
import miragefairy2024.util.generator
import miragefairy2024.util.registerChild
import miragefairy2024.util.toItemTag
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item
import net.minecraft.world.item.Items
import net.minecraft.world.item.ToolMaterial

enum class FairyToolMaterials(
    private val miningLevel: ToolMaterial,
    private val durability: Int,
    private val miningSpeedMultiplier: Float,
    private val attackDamage: Float,
    val axeAttackDamage: Float,
    private val enchantability: Int,
    private val repairItems: () -> TagKey<Item>,
) {
    COPPER(ToolMaterial.IRON, 196, 5.0F, 1.0F, 2.0F, 18, { tagOf(Shape.INGOT, Material.COPPER) }),
    GLASS(ToolMaterial.STONE, 8, 6.0F, 4.0F, 3.5F, 14, { ConventionalItemTags.GLASS_BLOCKS }),
    LAPIS_LAZULI(ToolMaterial.STONE, 140, 4.0F, 1.0F, 1.5F, 21, { tagOf(Shape.GEM, Material.LAPIS) }),
    AMETHYST(ToolMaterial.IRON, 218, 5.5F, 2.0F, 2.5F, 23, { tagOf(Shape.SHARD, Material.AMETHYST) }),
    OBSIDIAN(ToolMaterial.IRON, 804, 5.0F, 4.5F, 3.0F, 20, { ConventionalItemTags.OBSIDIANS }),
    EMERALD(ToolMaterial.DIAMOND, 1192, 7.0F, 2.5F, 2.0F, 16, { tagOf(Shape.GEM, Material.EMERALD) }),
    ECHO_SHARD(ToolMaterial.NETHERITE, 1366, 12.0F, 4.0F, 3.0F, 12, { ItemTagCard.ECHO_SHARDS.tag }),
    NETHER_STAR(ToolMaterial.NETHERITE, 5048, 11.0F, 5.0F, 3.5F, 25, { ConventionalItemTags.NETHER_STARS }),

    MAGNETITE(ToolMaterial.STONE, 220, 5.0F, 1.5F, 2.0F, 3, { tagOf(Shape.GEM, Material.MAGNETITE) }),
    BISMUTH(ToolMaterial.IRON, 4, 4.0F, 1.0F, 1.5F, 19, { tagOf(Shape.INGOT, Material.BISMUTH) }),
    FLUORITE(ToolMaterial.STONE, 96, 3.0F, 1.0F, 1.5F, 20, { tagOf(Shape.GEM, Material.FLUORITE) }),
    NEPHRITE(ToolMaterial.IRON, 420, 1.2F, 1.0F, 2.0F, 16, { tagOf(Shape.GEM, Material.NEPHRITE) }),
    TOPAZ(ToolMaterial.DIAMOND, 1285, 6.5F, 4.0F, 1.5F, 12, { tagOf(Shape.GEM, Material.TOPAZ) }),
    FLINT(ToolMaterial.STONE, 150, 3.5F, 1.5F, 1.0F, 6, { tagOf(Shape.GEM, Material.FLINT) }),

    MIRAGE(ToolMaterial.WOOD, 48, 1.6F, 0.0F, 0.0F, 17, { ItemTagCard.MIRAGE_STEMS.tag }),
    MIRAGIUM(ToolMaterial.IRON, 478, 2.0F, 2.0F, 0.0F, 26, { tagOf(Shape.INGOT, Material.MIRAGIUM) }),
    LILAGIUM(ToolMaterial.IRON, 505, 2.0F, 2.0F, 0.0F, 9, { tagOf(Shape.INGOT, Material.LILAGIUM) }),
    MIRAGIDIAN(ToolMaterial.NETHERITE, 7826, 7.0F, 2.5F, 1.5F, 2, { tagOf(Shape.GEM, Material.MIRAGIDIAN) }),
    FAIRY_CRYSTAL(ToolMaterial.IRON, 235, 5.0F, 1.5F, 1.5F, 23, { tagOf(Shape.GEM, Material.FAIRY_CRYSTAL) }),
    PHANTOM_DROP(ToolMaterial.NETHERITE, 777, 9.0F, 2.0F, 1.0F, 12, { tagOf(Shape.GEM, Material.PHANTOM_DROP) }),
    LUMINITE(ToolMaterial.DIAMOND, 1361, 9.0F, 4.0F, 3.0F, 21, { tagOf(Shape.GEM, Material.LUMINITE) }),
    RESONITE(ToolMaterial.NETHERITE, 2705, 16.0F, 9.0F, 7.0F, 7, { tagOf(Shape.INGOT, Material.RESONITE) }),
    PROMINITE(ToolMaterial.DIAMOND, 925, 7.0F, 2.0F, 0.0F, 13, { tagOf(Shape.GEM, Material.PROMINITE) }),
    XARPITE(ToolMaterial.IRON, 283, 1.2F, 2.0F, 2.0F, 1, { tagOf(Shape.GEM, Material.XARPITE) }),
    MIRANAGITE(ToolMaterial.IRON, 256, 6.5F, 2.0F, 2.5F, 24, { tagOf(Shape.GEM, Material.MIRANAGITE) }),
    CHAOS_STONE(ToolMaterial.NETHERITE, 666, 9.0F, 2.0F, 2.0F, 6, { tagOf(Shape.GEM, Material.CHAOS_STONE) }),
    CALCULITE(ToolMaterial.NETHERITE, 2048, 7.0F, 2.0F, 2.0F, 16, { tagOf(Shape.GEM, Material.CALCULITE) }),
    NOISE(ToolMaterial.NETHERITE, 101, 8.9F, 1.1F, 1.3F, 3, { tagOf(Shape.GEM, Material.NOISE) }),
    HAIMEVISKA_ROSIN(ToolMaterial.WOOD, 73, 1.2F, 0.0F, 0.0F, 16, { tagOf(Shape.GEM, Material.HAIMEVISKA_ROSIN) }),
    COPAL(ToolMaterial.IRON, 182, 5.0F, 1.5F, 1.5F, 19, { tagOf(Shape.GEM, Material.COPAL) }),
    FAIRY_PLASTIC(ToolMaterial.IRON, 966, 3.0F, 2.0F, 0.0F, 24, { tagOf(Shape.GEM, Material.FAIRY_PLASTIC) }),

    NEUTRONIUM(ToolMaterial.NETHERITE, Int.MAX_VALUE - 100, 8.0F, 3.0F, 3.0F, 10, { ItemTagCard.BEDROCKS.tag }),
    ;

    val toolMaterial by lazy { ToolMaterial(miningLevel.incorrectBlocksForDrops(), durability, miningSpeedMultiplier, attackDamage, enchantability, repairItems()) }
}

/** 採掘にも斬撃にも使わないツールに、[net.minecraft.world.item.ToolMaterial] の耐久値とエンチャント性と修理材料だけを乗せるのだ～🌱 */
fun Item.Properties.applyToolMaterialProperties(toolMaterial: ToolMaterial): Item.Properties = this
    .durability(toolMaterial.durability())
    .enchantable(toolMaterial.enchantmentValue())
    .repairable(toolMaterial.repairItems())

enum class ToolMaterialCard(private val toolMaterialGetter: () -> ToolMaterial, val axeAttackDamageBonus: Float, path: String, val title: EnJa) {
    WOOD({ ToolMaterial.WOOD }, 0.0F, "wooden_tool", EnJa("Wooden Tool", "木ツール")),
    STONE({ ToolMaterial.STONE }, 2.0F, "stone_tool", EnJa("Stone Tool", "石ツール")),
    IRON({ ToolMaterial.IRON }, 2.0F, "iron_tool", EnJa("Iron Tool", "鉄ツール")),
    GOLD({ ToolMaterial.GOLD }, 0.0F, "golden_tool", EnJa("Golden Tool", "金ツール")),
    DIAMOND({ ToolMaterial.DIAMOND }, 2.0F, "diamond_tool", EnJa("Diamond Tool", "ダイヤモンドツール")),
    NETHERITE({ ToolMaterial.NETHERITE }, 3.0F, "netherite_tool", EnJa("Netherite Tool", "ネザライトツール")),

    COPPER(FairyToolMaterials.COPPER, "copper_tool", EnJa("Copper Tool", "銅ツール")),
    GLASS(FairyToolMaterials.GLASS, "glass_tool", EnJa("Glass Tool", "ガラスツール")),
    LAPIS_LAZULI(FairyToolMaterials.LAPIS_LAZULI, "lapis_lazuli_tool", EnJa("Lapis Lazuli Tool", "ラピスラズリツール")),
    AMETHYST(FairyToolMaterials.AMETHYST, "amethyst_tool", EnJa("Amethyst Tool", "アメジストツール")),
    OBSIDIAN(FairyToolMaterials.OBSIDIAN, "obsidian_tool", EnJa("Obsidian Tool", "黒曜石ツール")),
    EMERALD(FairyToolMaterials.EMERALD, "emerald_tool", EnJa("Emerald Tool", "エメラルドツール")),
    ECHO_SHARD(FairyToolMaterials.ECHO_SHARD, "echo_shard_tool", EnJa("Echo Shard Tool", "残響ツール")),
    NETHER_STAR(FairyToolMaterials.NETHER_STAR, "nether_star_tool", EnJa("Nether Star Tool", "ネザースターツール")),

    MAGNETITE(FairyToolMaterials.MAGNETITE, "magnetite_tool", EnJa("Magnetite Tool", "磁鉄鉱ツール")),
    BISMUTH(FairyToolMaterials.BISMUTH, "bismuth_tool", EnJa("Bismuth Tool", "ビスマスツール")),
    FLUORITE(FairyToolMaterials.FLUORITE, "fluorite_tool", EnJa("Fluorite Tool", "蛍石ツール")),
    NEPHRITE(FairyToolMaterials.NEPHRITE, "nephrite_tool", EnJa("Nephrite Tool", "ネフライトツール")),
    TOPAZ(FairyToolMaterials.TOPAZ, "topaz_tool", EnJa("Topaz Tool", "トパーズツール")),
    FLINT(FairyToolMaterials.FLINT, "flint_tool", EnJa("Flint Tool", "火打石ツール")),

    MIRAGE(FairyToolMaterials.MIRAGE, "mirage_tool", EnJa("Mirage Tool", "ミラージュツール")), // TODO 用途
    MIRAGIUM(FairyToolMaterials.MIRAGIUM, "miragium_tool", EnJa("Miragium Tool", "ミラジウムツール")),
    LILAGIUM(FairyToolMaterials.LILAGIUM, "lilagium_tool", EnJa("Lilagium Tool", "リラジウムツール")),
    MIRAGIDIAN(FairyToolMaterials.MIRAGIDIAN, "miragidian_tool", EnJa("Miragidian Tool", "ミラジディアンツール")),
    FAIRY_CRYSTAL(FairyToolMaterials.FAIRY_CRYSTAL, "fairy_crystal_tool", EnJa("Fairy Crystal", "フェアリークリスタルツール")),
    PHANTOM_DROP(FairyToolMaterials.PHANTOM_DROP, "phantom_tool", EnJa("Phantom Tool", "幻想ツール")),
    LUMINITE(FairyToolMaterials.LUMINITE, "luminite_tool", EnJa("Luminite Tool", "ルミナイトツール")),
    RESONITE(FairyToolMaterials.RESONITE, "resonite_tool", EnJa("Resonance Tool", "共鳴ツール")),
    PROMINITE(FairyToolMaterials.PROMINITE, "prominite_tool", EnJa("Prominite Tool", "プロミナイトツール")),
    XARPITE(FairyToolMaterials.XARPITE, "xarpite_tool", EnJa("Xarpite Tool", "紅天石ツール")),
    MIRANAGITE(FairyToolMaterials.MIRANAGITE, "miranagite_tool", EnJa("Miranagite Tool", "蒼天石ツール")),
    CHAOS_STONE(FairyToolMaterials.CHAOS_STONE, "chaos_tool", EnJa("Chaos Tool", "混沌ツール")),
    CALCULITE(FairyToolMaterials.CALCULITE, "calculite_tool", EnJa("Calculite Tool", "理天石ツール")),
    NOISE(FairyToolMaterials.NOISE, "noise_tool", EnJa("Noise Tool", "ノイズツール")),
    HAIMEVISKA_ROSIN(FairyToolMaterials.HAIMEVISKA_ROSIN, "haimeviska_rosin_tool", EnJa("Resin Tool", "涙ツール")),
    COPAL(FairyToolMaterials.COPAL, "copal_tool", EnJa("Copal Tool", "コーパルツール")),
    FAIRY_PLASTIC(FairyToolMaterials.FAIRY_PLASTIC, "fairy_plastic_tool", EnJa("Fairy Plastic Tool", "妖精のプラスチックツール")),

    NEUTRONIUM(FairyToolMaterials.NEUTRONIUM, "neutronium_tool", EnJa("Neutronium Tool", "ニュートロニウムツール")),
    ;

    constructor(fairyToolMaterials: FairyToolMaterials, path: String, title: EnJa) : this({ fairyToolMaterials.toolMaterial }, fairyToolMaterials.axeAttackDamage, path, title)

    val toolMaterial by lazy { toolMaterialGetter() }
    val identifier = MirageFairy2024.identifier(path)
    val tag = identifier.toItemTag()
}

context(ModContext)
fun initToolMaterial() {

    ToolMaterialCard.entries.forEach { card ->
        card.tag.enJa(card.title)
    }


    fun register(card: ToolMaterialCard, item: () -> Item) = card.tag.generator.registerChild(item)

    // WOOD
    register(ToolMaterialCard.WOOD) { Items.WOODEN_SWORD }
    register(ToolMaterialCard.WOOD) { Items.WOODEN_SHOVEL }
    register(ToolMaterialCard.WOOD) { Items.WOODEN_PICKAXE }
    register(ToolMaterialCard.WOOD) { Items.WOODEN_AXE }
    register(ToolMaterialCard.WOOD) { Items.WOODEN_HOE }
    register(ToolMaterialCard.WOOD) { Items.BOW }
    register(ToolMaterialCard.WOOD) { Items.CROSSBOW }
    register(ToolMaterialCard.WOOD) { Items.FISHING_ROD }
    register(ToolMaterialCard.WOOD) { Items.CARROT_ON_A_STICK }
    register(ToolMaterialCard.WOOD) { Items.WARPED_FUNGUS_ON_A_STICK }

    // STONE
    register(ToolMaterialCard.STONE) { Items.STONE_SWORD }
    register(ToolMaterialCard.STONE) { Items.STONE_SHOVEL }
    register(ToolMaterialCard.STONE) { Items.STONE_PICKAXE }
    register(ToolMaterialCard.STONE) { Items.STONE_AXE }
    register(ToolMaterialCard.STONE) { Items.STONE_HOE }

    // IRON
    register(ToolMaterialCard.IRON) { Items.IRON_SWORD }
    register(ToolMaterialCard.IRON) { Items.IRON_SHOVEL }
    register(ToolMaterialCard.IRON) { Items.IRON_PICKAXE }
    register(ToolMaterialCard.IRON) { Items.IRON_AXE }
    register(ToolMaterialCard.IRON) { Items.IRON_HOE }
    register(ToolMaterialCard.IRON) { Items.FLINT_AND_STEEL }
    register(ToolMaterialCard.IRON) { Items.SHEARS }

    // COPPER
    register(ToolMaterialCard.COPPER) { Items.SPYGLASS }
    register(ToolMaterialCard.COPPER) { Items.BRUSH }
    register(ToolMaterialCard.COPPER) { Items.TRIDENT }

    // GOLD
    register(ToolMaterialCard.GOLD) { Items.GOLDEN_SWORD }
    register(ToolMaterialCard.GOLD) { Items.GOLDEN_SHOVEL }
    register(ToolMaterialCard.GOLD) { Items.GOLDEN_PICKAXE }
    register(ToolMaterialCard.GOLD) { Items.GOLDEN_AXE }
    register(ToolMaterialCard.GOLD) { Items.GOLDEN_HOE }

    // DIAMOND
    register(ToolMaterialCard.DIAMOND) { Items.DIAMOND_SWORD }
    register(ToolMaterialCard.DIAMOND) { Items.DIAMOND_SHOVEL }
    register(ToolMaterialCard.DIAMOND) { Items.DIAMOND_PICKAXE }
    register(ToolMaterialCard.DIAMOND) { Items.DIAMOND_AXE }
    register(ToolMaterialCard.DIAMOND) { Items.DIAMOND_HOE }

    // NETHERITE
    register(ToolMaterialCard.NETHERITE) { Items.NETHERITE_SWORD }
    register(ToolMaterialCard.NETHERITE) { Items.NETHERITE_SHOVEL }
    register(ToolMaterialCard.NETHERITE) { Items.NETHERITE_PICKAXE }
    register(ToolMaterialCard.NETHERITE) { Items.NETHERITE_AXE }
    register(ToolMaterialCard.NETHERITE) { Items.NETHERITE_HOE }

    // PLANT_TOOLS
    ItemTagCard.PLANT_TOOLS.tag.generator.registerChild(ToolMaterialCard.WOOD.tag)
    ItemTagCard.PLANT_TOOLS.tag.generator.registerChild(ToolMaterialCard.MIRAGE.tag)
    ItemTagCard.PLANT_TOOLS.tag.generator.registerChild(ToolMaterialCard.PHANTOM_DROP.tag)
    ItemTagCard.PLANT_TOOLS.tag.generator.registerChild(ToolMaterialCard.LUMINITE.tag)
    ItemTagCard.PLANT_TOOLS.tag.generator.registerChild(ToolMaterialCard.HAIMEVISKA_ROSIN.tag)
    ItemTagCard.PLANT_TOOLS.tag.generator.registerChild(ToolMaterialCard.FAIRY_PLASTIC.tag)

}
