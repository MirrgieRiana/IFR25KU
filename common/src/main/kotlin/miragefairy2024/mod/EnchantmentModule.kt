package miragefairy2024.mod

import dev.architectury.event.EventResult
import dev.architectury.event.events.common.BlockEvent
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mixins.api.BlockCallback
import miragefairy2024.mixins.api.EquippedItemBrokenCallback
import miragefairy2024.mod.tool.ToolBreakDamageTypeCard
import miragefairy2024.platformProxy
import miragefairy2024.util.EnJa
import miragefairy2024.util.MultiMine
import miragefairy2024.util.NeighborType
import miragefairy2024.util.en
import miragefairy2024.util.enJa
import miragefairy2024.util.generator
import miragefairy2024.util.get
import miragefairy2024.util.isIn
import miragefairy2024.util.isInMagicMining
import miragefairy2024.util.isValid
import miragefairy2024.util.ja
import miragefairy2024.util.registerChild
import miragefairy2024.util.registerDynamicGeneration
import miragefairy2024.util.serverSide
import miragefairy2024.util.toItemTag
import miragefairy2024.util.with
import mirrg.kotlin.helium.max
import mirrg.kotlin.java.hydrogen.orNull
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBlockTags
import net.minecraft.core.BlockBox
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.registries.Registries
import net.minecraft.server.level.ServerPlayer
import net.minecraft.tags.BlockTags
import net.minecraft.tags.EnchantmentTags
import net.minecraft.tags.ItemTags
import net.minecraft.tags.TagKey
import net.minecraft.world.entity.EquipmentSlotGroup
import net.minecraft.world.entity.ExperienceOrb
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.RecipeType
import net.minecraft.world.item.crafting.SingleRecipeInput
import net.minecraft.world.item.enchantment.Enchantment
import net.minecraft.world.item.enchantment.EnchantmentHelper
import net.minecraft.world.item.enchantment.Enchantments
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.HitResult
import java.util.UUID

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
    FORWARD_AREA_MINING(
        "forward_area_mining", EnJa("Forward Area Mining", "前方範囲採掘"),
        ItemTags.MINING_LOOT_ENCHANTABLE, NONE_ITEM_TAG, EnchantmentRarity.VERY_RARE,
        5, 25, 25, 50,
        tags = listOf(EnchantmentTags.TREASURE),
    ),
    LATERAL_AREA_MINING(
        "lateral_area_mining", EnJa("Lateral Area Mining", "側方範囲採掘"),
        ItemTags.MINING_LOOT_ENCHANTABLE, NONE_ITEM_TAG, EnchantmentRarity.VERY_RARE,
        5, 25, 25, 50,
        tags = listOf(EnchantmentTags.TREASURE),
    ),
    BACKWARD_AREA_MINING(
        "backward_area_mining", EnJa("Backward Area Mining", "後方範囲採掘"),
        ItemTags.MINING_LOOT_ENCHANTABLE, NONE_ITEM_TAG, EnchantmentRarity.VERY_RARE,
        5, 25, 25, 50,
        tags = listOf(EnchantmentTags.TREASURE),
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

val breakDirectionCache = mutableMapOf<UUID, Direction>()

context(ModContext)
fun initEnchantmentModule() {
    MAGIC_WEAPON_ITEM_TAG.enJa(EnJa("Magic Weapon", "魔法武器"))
    SCYTHE_ITEM_TAG.enJa(EnJa("Scythe", "大鎌"))
    NONE_ITEM_TAG.enJa(EnJa("None", "なし"))

    EnchantmentCard.entries.forEach { card ->
        card.init()
    }

    BlockEvent.BREAK.register { level, pos, state, player, xp ->
        if (level.isClientSide) return@register EventResult.pass()
        val direction = run {
            val d = player.blockInteractionRange() max player.entityInteractionRange()
            val hitResult = player.pick(d, 0F, false)
            if (hitResult.type != HitResult.Type.BLOCK) {
                null
            } else {
                (hitResult as BlockHitResult).direction
            }
        }
        if (direction == null) {
            breakDirectionCache.remove(player.uuid)
        } else {
            breakDirectionCache[player.uuid] = direction
        }
        EventResult.pass()
    }

    // Fortune Up
    platformProxy!!.registerModifyItemEnchantmentsHandler { _, mutableItemEnchantments, enchantmentLookup ->
        val fortuneEnchantment = enchantmentLookup[Enchantments.FORTUNE].orNull ?: return@registerModifyItemEnchantmentsHandler
        val fortuneLevel = mutableItemEnchantments.getLevel(fortuneEnchantment)
        if (fortuneLevel == 0) return@registerModifyItemEnchantmentsHandler
        val fortuneUpEnchantment = enchantmentLookup[EnchantmentCard.FORTUNE_UP.key].orNull ?: return@registerModifyItemEnchantmentsHandler
        val fortuneUpLevel = mutableItemEnchantments.getLevel(fortuneUpEnchantment)
        mutableItemEnchantments.set(fortuneEnchantment, fortuneLevel + fortuneUpLevel)
    }

    // Smelting
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

    // Sticky Mining
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

    // Area Mining
    class AreaMiningMultiMine(
        private val lateralLevel: Int, private val forwardLevel: Int, private val backwardLevel: Int,
        level: Level, blockPos: BlockPos, blockState: BlockState,
        miner: Player, toolItem: Item, toolItemStack: ItemStack,
    ) :
        MultiMine(
            level, blockPos, blockState,
            miner, toolItem, toolItemStack,
        ) {
        override fun visit(visitor: Visitor) {
            visitor.visit(
                listOf(blockPos),
                miningDamage = 1.0,
                region = run {
                    val breakDirection = breakDirectionCache[this.miner.uuid] ?: return // 向きの判定が不正
                    val l = lateralLevel
                    val f = forwardLevel
                    val b = backwardLevel
                    val (xRange, yRange, zRange) = when (breakDirection) {
                        Direction.DOWN -> Triple(-l..l, -b..f, -l..l)
                        Direction.UP -> Triple(-l..l, -f..b, -l..l)
                        Direction.NORTH -> Triple(-l..l, -l..l, -b..f)
                        Direction.SOUTH -> Triple(-l..l, -l..l, -f..b)
                        Direction.WEST -> Triple(-b..f, -l..l, -l..l)
                        Direction.EAST -> Triple(-f..b, -l..l, -l..l)
                    }
                    BlockBox.of(
                        BlockPos(blockPos.x + xRange.first, blockPos.y + yRange.first, blockPos.z + zRange.first),
                        BlockPos(blockPos.x + xRange.last, blockPos.y + yRange.last, blockPos.z + zRange.last),
                    )
                },
                canContinue = { _, blockState -> toolItem.isCorrectToolForDrops(toolItemStack, blockState) },
            )
        }
    }
    BlockCallback.AFTER_BREAK.register { world, player, pos, state, _, tool ->
        if (world.isClientSide) return@register
        if (player !is ServerPlayer) return@register
        if (isInMagicMining.get()) return@register

        val forwardLevel = EnchantmentHelper.getItemEnchantmentLevel(world.registryAccess()[Registries.ENCHANTMENT, EnchantmentCard.FORWARD_AREA_MINING.key], tool)
        val lateralLevel = EnchantmentHelper.getItemEnchantmentLevel(world.registryAccess()[Registries.ENCHANTMENT, EnchantmentCard.LATERAL_AREA_MINING.key], tool)
        val backwardLevel = EnchantmentHelper.getItemEnchantmentLevel(world.registryAccess()[Registries.ENCHANTMENT, EnchantmentCard.BACKWARD_AREA_MINING.key], tool)
        if (forwardLevel <= 0 && lateralLevel <= 0 && backwardLevel <= 0) return@register

        AreaMiningMultiMine(
            forwardLevel, lateralLevel, backwardLevel,
            world, pos, state,
            player, tool.item, tool,
        ).execute(player.serverSide, state.getDestroySpeed(world, pos))
    }

    // Cut All
    BlockCallback.AFTER_BREAK.register { world, player, pos, state, _, tool ->
        if (world.isClientSide) return@register
        if (player !is ServerPlayer) return@register
        if (isInMagicMining.get()) return@register

        val cutAllLevel = EnchantmentHelper.getItemEnchantmentLevel(world.registryAccess()[Registries.ENCHANTMENT, EnchantmentCard.CUT_ALL.key], tool)
        if (cutAllLevel <= 0) return@register

        object : MultiMine(world, pos, state, player, tool.item, tool) {
            override fun isValidBaseBlockState() = blockState isIn BlockTags.LOGS
            override fun visit(visitor: Visitor) {
                val logBlockPosList = mutableListOf<BlockPos>()
                visitor.visit(
                    listOf(pos),
                    miningDamage = 1.0,
                    maxDistance = 19,
                    maxCount = 19,
                    neighborType = NeighborType.VERTICES,
                    canContinue = { _, blockState -> blockState isIn BlockTags.LOGS },
                    onMine = { blockPos ->
                        logBlockPosList += blockPos
                    },
                ).let { if (!it) return }
                visitor.visit(
                    logBlockPosList,
                    miningDamage = 0.1,
                    maxDistance = 8,
                    canContinue = { _, blockState -> blockState isIn BlockTags.LEAVES },
                )
            }
        }.execute(player.serverSide, state.getDestroySpeed(world, pos))
    }

    // Mine All
    BlockCallback.AFTER_BREAK.register { world, player, pos, state, _, tool ->
        if (world.isClientSide) return@register
        if (player !is ServerPlayer) return@register
        if (isInMagicMining.get()) return@register

        val mineAllLevel = EnchantmentHelper.getItemEnchantmentLevel(world.registryAccess()[Registries.ENCHANTMENT, EnchantmentCard.MINE_ALL.key], tool)
        if (mineAllLevel <= 0) return@register

        object : MultiMine(world, pos, state, player, tool.item, tool) {
            override fun isValidBaseBlockState() = blockState isIn ConventionalBlockTags.ORES
            override fun visit(visitor: Visitor) {
                visitor.visit(
                    listOf(pos),
                    miningDamage = 1.0,
                    maxDistance = 19,
                    maxCount = 31,
                    canContinue = { _, blockState -> blockState.block === state.block },
                )
            }
        }.execute(player.serverSide, state.getDestroySpeed(world, pos))
    }

    // Curse of Shattering
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
