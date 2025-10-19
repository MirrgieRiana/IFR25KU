package miragefairy2024.mod

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mixins.api.BlockCallback
import miragefairy2024.mixins.api.EquippedItemBrokenCallback
import miragefairy2024.mod.tool.ToolBreakDamageTypeCard
import miragefairy2024.platformProxy
import miragefairy2024.util.EnJa
import miragefairy2024.util.en
import miragefairy2024.util.enJa
import miragefairy2024.util.generator
import miragefairy2024.util.get
import miragefairy2024.util.isValid
import miragefairy2024.util.ja
import miragefairy2024.util.registerChild
import miragefairy2024.util.registerDynamicGeneration
import miragefairy2024.util.toItemTag
import miragefairy2024.util.with
import mirrg.kotlin.java.hydrogen.orNull
import net.minecraft.core.registries.Registries
import net.minecraft.tags.EnchantmentTags
import net.minecraft.tags.ItemTags
import net.minecraft.tags.TagKey
import net.minecraft.world.entity.EquipmentSlotGroup
import net.minecraft.world.entity.ExperienceOrb
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.item.Item
import net.minecraft.world.item.crafting.RecipeType
import net.minecraft.world.item.crafting.SingleRecipeInput
import net.minecraft.world.item.enchantment.Enchantment
import net.minecraft.world.item.enchantment.EnchantmentHelper
import net.minecraft.world.item.enchantment.Enchantments
import net.minecraft.world.phys.AABB

val MAGIC_WEAPON_ITEM_TAG = MirageFairy2024.identifier("magic_weapon").toItemTag()
val SCYTHE_ITEM_TAG = MirageFairy2024.identifier("scythe").toItemTag()
val NONE_ITEM_TAG = MirageFairy2024.identifier("none").toItemTag()

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

context(ModContext)
fun initEnchantmentModule() {
    MAGIC_WEAPON_ITEM_TAG.enJa(EnJa("Magic Weapon", "魔法武器"))
    SCYTHE_ITEM_TAG.enJa(EnJa("Scythe", "大鎌"))
    NONE_ITEM_TAG.enJa(EnJa("None", "なし"))

    EnchantmentCard.entries.forEach { card ->
        card.init()
    }

    platformProxy!!.registerModifyItemEnchantmentsHandler { _, mutableItemEnchantments, enchantmentLookup ->
        val fortuneEnchantment = enchantmentLookup[Enchantments.FORTUNE].orNull ?: return@registerModifyItemEnchantmentsHandler
        val fortuneLevel = mutableItemEnchantments.getLevel(fortuneEnchantment)
        if (fortuneLevel == 0) return@registerModifyItemEnchantmentsHandler
        val fortuneUpEnchantment = enchantmentLookup[EnchantmentCard.FORTUNE_UP.key].orNull ?: return@registerModifyItemEnchantmentsHandler
        val fortuneUpLevel = mutableItemEnchantments.getLevel(fortuneUpEnchantment)
        mutableItemEnchantments.set(fortuneEnchantment, fortuneLevel + fortuneUpLevel)
    }

    BlockCallback.GET_DROPS_BY_ENTITY.register { state, level, _, _, _, tool, drops ->
        val smeltingLevel = EnchantmentHelper.getItemEnchantmentLevel(level.registryAccess()[Registries.ENCHANTMENT, EnchantmentCard.SMELTING.key], tool)
        if (smeltingLevel == 0) return@register drops
        if (!tool.isCorrectToolForDrops(state)) return@register drops
        drops.map {
            val recipe = level.recipeManager.getRecipeFor(RecipeType.SMELTING, SingleRecipeInput(it), level).orNull ?: return@map it
            val result = recipe.value.getResultItem(level.registryAccess())
            if (result.isEmpty) return@map it
            result.copyWithCount(it.count)
        }
    }

    run {
        val listener = ThreadLocal<() -> Unit>()
        BlockCallback.BEFORE_DROP_BY_ENTITY.register { _, level, pos, _, entity, tool ->
            if (entity == null) return@register
            val stickyMiningLevel = EnchantmentHelper.getItemEnchantmentLevel(level.registryAccess()[Registries.ENCHANTMENT, EnchantmentCard.STICKY_MINING.key], tool)
            if (stickyMiningLevel == 0) return@register

            val oldItemEntities = level.getEntitiesOfClass(ItemEntity::class.java, AABB(pos)) { it.isValid }.toSet()
            val oldExperienceOrbs = level.getEntitiesOfClass(ExperienceOrb::class.java, AABB(pos)) { it.isValid }.toSet()

            listener.set {
                val newItemEntities = level.getEntitiesOfClass(ItemEntity::class.java, AABB(pos)) { it.isValid }.toSet()
                val newExperienceOrbs = level.getEntitiesOfClass(ExperienceOrb::class.java, AABB(pos)) { it.isValid }.toSet()

                (newItemEntities - oldItemEntities).forEach {
                    it.teleportTo(entity.x, entity.y, entity.z)
                    it.setNoPickUpDelay()
                }
                (newExperienceOrbs - oldExperienceOrbs).forEach {
                    it.teleportTo(entity.x, entity.y, entity.z)
                }
            }
        }
        BlockCallback.AFTER_DROP_BY_ENTITY.register { _, _, _, _, _, _ ->
            val listener2 = listener.get()
            if (listener2 != null) {
                listener2.invoke()
                listener.remove()
            }
        }
    }

    EquippedItemBrokenCallback.EVENT.register { entity, _, slot ->
        if (entity.level().isClientSide) return@register
        val itemStack = entity.getItemBySlot(slot)
        itemStack.grow(1)
        val originalItemStack = itemStack.copy()
        itemStack.shrink(1)
        val enchantLevel = EnchantmentHelper.getItemEnchantmentLevel(entity.level().registryAccess()[Registries.ENCHANTMENT, EnchantmentCard.CURSE_OF_SHATTERING.key], originalItemStack)
        if (enchantLevel == 0) return@register
        entity.hurt(entity.level().damageSources().source(ToolBreakDamageTypeCard.registryKey), 2F * enchantLevel.toFloat())
    }

    ItemTags.MINING_LOOT_ENCHANTABLE.generator.registerChild(SCYTHE_ITEM_TAG)
}
