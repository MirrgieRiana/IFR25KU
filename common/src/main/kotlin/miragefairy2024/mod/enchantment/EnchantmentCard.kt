package miragefairy2024.mod.enchantment

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.util.EnJa
import miragefairy2024.util.en
import miragefairy2024.util.generator
import miragefairy2024.util.ja
import miragefairy2024.util.registerChild
import miragefairy2024.util.registerDynamicGeneration
import miragefairy2024.util.with
import net.minecraft.core.registries.Registries
import net.minecraft.tags.EnchantmentTags
import net.minecraft.tags.ItemTags
import net.minecraft.tags.TagKey
import net.minecraft.world.entity.EquipmentSlotGroup
import net.minecraft.world.item.Item
import net.minecraft.world.item.enchantment.Enchantment

enum class EnchantmentRarity(val weight: Int, val anvilCost: Int) {
    COMMON(10, 1),
    UNCOMMON(5, 2),
    RARE(2, 4),
    VERY_RARE(1, 8),
}

enum class EnchantmentCard(
    path: String,
    private val description: EnJa,
    private val targetItemTag: TagKey<Item>,
    private val primaryItemTag: TagKey<Item>,
    private val rarity: EnchantmentRarity,
    private val maxLevel: Int,
    private val basePower: Int,
    private val powerPerLevel: Int,
    private val powerRange: Int,
    private val tags: List<TagKey<Enchantment>> = listOf(),
) {
    MAGIC_POWER(
        "magic_power", EnJa("Magic Power", "魔法ダメージ増加"),
        MAGIC_WEAPON_ITEM_TAG, MAGIC_WEAPON_ITEM_TAG, EnchantmentRarity.COMMON,
        5, 1, 10, 30,
        tags = listOf(EnchantmentTags.NON_TREASURE),
    ),
    MAGIC_REACH(
        "magic_reach", EnJa("Magic Reach", "魔法射程増加"),
        MAGIC_WEAPON_ITEM_TAG, MAGIC_WEAPON_ITEM_TAG, EnchantmentRarity.COMMON,
        5, 1, 10, 30,
        tags = listOf(EnchantmentTags.NON_TREASURE),
    ),
    MAGIC_ACCELERATION(
        "magic_acceleration", EnJa("Magic Acceleration", "魔法加速"),
        MAGIC_WEAPON_ITEM_TAG, MAGIC_WEAPON_ITEM_TAG, EnchantmentRarity.COMMON,
        5, 1, 10, 30,
        tags = listOf(EnchantmentTags.NON_TREASURE),
    ),
    FORTUNE_UP(
        "fortune_up", EnJa("Fortune Up", "幸運強化"),
        ItemTags.MINING_LOOT_ENCHANTABLE, NONE_ITEM_TAG, EnchantmentRarity.VERY_RARE,
        3, 25, 25, 50,
        tags = listOf(EnchantmentTags.TREASURE),
    ),
    SMELTING(
        "smelting", EnJa("Smelting", "精錬"),
        ItemTags.MINING_LOOT_ENCHANTABLE, NONE_ITEM_TAG, EnchantmentRarity.VERY_RARE,
        1, 25, 25, 50,
        tags = listOf(EnchantmentTags.TREASURE),
    ),
    STICKY_MINING(
        "sticky_mining", EnJa("Sticky Mining", "粘着採掘"),
        ItemTags.MINING_LOOT_ENCHANTABLE, NONE_ITEM_TAG, EnchantmentRarity.VERY_RARE,
        1, 25, 25, 50,
        tags = listOf(EnchantmentTags.TREASURE),
    ),
    FORWARD_AREA_MINING(
        "forward_area_mining", EnJa("Forward Area Mining", "前方範囲採掘"),
        ItemTags.MINING_LOOT_ENCHANTABLE, AREA_MINING_ENCHANTABLE_ITEM_TAG, EnchantmentRarity.VERY_RARE,
        5, 25, 25, 50,
        tags = listOf(EnchantmentTags.TREASURE, EnchantmentTags.IN_ENCHANTING_TABLE),
    ),
    LATERAL_AREA_MINING(
        "lateral_area_mining", EnJa("Lateral Area Mining", "側方範囲採掘"),
        ItemTags.MINING_LOOT_ENCHANTABLE, AREA_MINING_ENCHANTABLE_ITEM_TAG, EnchantmentRarity.VERY_RARE,
        5, 25, 25, 50,
        tags = listOf(EnchantmentTags.TREASURE, EnchantmentTags.IN_ENCHANTING_TABLE),
    ),
    BACKWARD_AREA_MINING(
        "backward_area_mining", EnJa("Backward Area Mining", "後方範囲採掘"),
        ItemTags.MINING_LOOT_ENCHANTABLE, AREA_MINING_ENCHANTABLE_ITEM_TAG, EnchantmentRarity.VERY_RARE,
        5, 25, 25, 50,
        tags = listOf(EnchantmentTags.TREASURE, EnchantmentTags.IN_ENCHANTING_TABLE),
    ),
    CUT_ALL(
        "cut_all", EnJa("Cut All", "一括伐採"),
        ItemTags.MINING_LOOT_ENCHANTABLE, NONE_ITEM_TAG, EnchantmentRarity.VERY_RARE,
        1, 25, 25, 50,
        tags = listOf(EnchantmentTags.TREASURE),
    ),
    MINE_ALL(
        "mine_all", EnJa("Mine All", "一括採掘"),
        ItemTags.MINING_LOOT_ENCHANTABLE, NONE_ITEM_TAG, EnchantmentRarity.VERY_RARE,
        1, 25, 25, 50,
        tags = listOf(EnchantmentTags.TREASURE),
    ),
    CURSE_OF_SHATTERING(
        "curse_of_shattering", EnJa("Curse of Shattering", "破断の呪い"),
        ItemTags.DURABILITY_ENCHANTABLE, NONE_ITEM_TAG, EnchantmentRarity.VERY_RARE,
        5, 25, 25, 50,
        tags = listOf(EnchantmentTags.TREASURE, EnchantmentTags.CURSE),
    ),
    ;

    val identifier = MirageFairy2024.identifier(path)
    val key = Registries.ENCHANTMENT with identifier

    context(ModContext)
    fun init() {
        registerDynamicGeneration(key) {
            Enchantment.enchantment(
                Enchantment.definition(
                    lookup(Registries.ITEM).getOrThrow(targetItemTag),
                    lookup(Registries.ITEM).getOrThrow(primaryItemTag),
                    rarity.weight,
                    maxLevel,
                    Enchantment.dynamicCost(basePower, powerPerLevel),
                    Enchantment.dynamicCost(basePower + powerRange, powerPerLevel),
                    rarity.anvilCost,
                    EquipmentSlotGroup.MAINHAND,
                )
            )
                .build(identifier)
        }
        en { identifier.toLanguageKey("enchantment") to description.en }
        ja { identifier.toLanguageKey("enchantment") to description.ja }
        tags.forEach {
            it.generator.registerChild(key)
        }
    }
}
