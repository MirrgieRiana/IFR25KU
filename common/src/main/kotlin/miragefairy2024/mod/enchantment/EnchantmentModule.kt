package miragefairy2024.mod.enchantment

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mixins.api.BlockCallback
import miragefairy2024.mixins.api.EquippedItemBrokenCallback
import miragefairy2024.mixins.api.LevelEvent
import miragefairy2024.mod.CommonRenderingEvents
import miragefairy2024.mod.tool.ToolBreakDamageTypeCard
import miragefairy2024.platformProxy
import miragefairy2024.util.EnJa
import miragefairy2024.util.enJa
import miragefairy2024.util.generator
import miragefairy2024.util.get
import miragefairy2024.util.isInMagicMining
import miragefairy2024.util.isValid
import miragefairy2024.util.registerChild
import miragefairy2024.util.serverSideOrNull
import miragefairy2024.util.toItemTag
import mirrg.kotlin.helium.max
import mirrg.kotlin.java.hydrogen.orNull
import net.minecraft.core.Direction
import net.minecraft.core.registries.Registries
import net.minecraft.tags.ItemTags
import net.minecraft.world.entity.ExperienceOrb
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.crafting.RecipeType
import net.minecraft.world.item.crafting.SingleRecipeInput
import net.minecraft.world.item.enchantment.EnchantmentHelper
import net.minecraft.world.item.enchantment.Enchantments
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.HitResult

val MAGIC_WEAPON_ITEM_TAG = MirageFairy2024.identifier("magic_weapon").toItemTag()
val SCYTHE_ITEM_TAG = MirageFairy2024.identifier("scythe").toItemTag()
val NONE_ITEM_TAG = MirageFairy2024.identifier("none").toItemTag()
val AREA_MINING_ENCHANTABLE_ITEM_TAG = MirageFairy2024.identifier("enchantable/area_mining").toItemTag()

private val latestPlayerMiningDirectionCache = mutableMapOf<Int, Pair<Long, Direction>>()

private fun calculateMiningDirection(player: Player): Direction? {
    val d = player.blockInteractionRange() max player.entityInteractionRange()
    val hitResult = player.pick(d, 0F, false)
    if (hitResult.type != HitResult.Type.BLOCK) return null
    hitResult as BlockHitResult
    return hitResult.direction
}

context(ModContext)
fun initEnchantmentModule() {
    MAGIC_WEAPON_ITEM_TAG.enJa(EnJa("Magic Weapon", "魔法武器"))
    SCYTHE_ITEM_TAG.enJa(EnJa("Scythe", "大鎌"))
    NONE_ITEM_TAG.enJa(EnJa("None", "なし"))
    AREA_MINING_ENCHANTABLE_ITEM_TAG.enJa(EnJa("Area Mining Enchantable", "範囲採掘エンチャント可能"))

    EnchantmentCard.entries.forEach { card ->
        card.init()
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

    // 範囲採掘系
    run {

        // 採掘範囲オーバーレイ
        CommonRenderingEvents.onRenderBlockPosesOutline.add { context ->
            val level = context.level ?: return@add null
            val player = context.player ?: return@add null
            val hitResult = context.hitResult ?: return@add null

            if (hitResult.type != HitResult.Type.BLOCK) return@add null // ブロックをタゲっていない
            hitResult as BlockHitResult

            val miningArea = run {
                val multiMine = run {
                    MultiMineHandler.REGISTRY.firstNotNullOfOrNull {
                        it.create(
                            hitResult.direction,
                            level, hitResult.blockPos, level.getBlockState(hitResult.blockPos),
                            player, player.mainHandItem.item, player.mainHandItem,
                        )
                    }
                } ?: return@run null // 範囲採掘の能力がない
                val miningArea = multiMine.collect() ?: return@run null // 範囲採掘が発動しなかった
                miningArea
            } ?: return@add null // 範囲採掘が発動しなかった

            Pair(
                hitResult.blockPos.relative(hitResult.direction),
                miningArea.visitedBlockEntry.map { it.blockPos }.toSet() + setOf(miningArea.multiMine.blockPos),
            )
        }

        // 両サイドにおいて、採掘の際に採掘速度を上書き
        BlockCallback.OVERRIDE_DESTROY_SPEED.register { state, player, _, pos, f ->
            val miningArea = run {
                val miningDirection = calculateMiningDirection(player) ?: return@run null // なぜかブロックをタゲっていない
                val multiMine = run {
                    MultiMineHandler.REGISTRY.firstNotNullOfOrNull {
                        it.create(
                            miningDirection,
                            player.level(), pos, state,
                            player, player.mainHandItem.item, player.mainHandItem,
                        )
                    }
                } ?: return@run null // 範囲採掘の能力がない
                val miningArea = multiMine.collect() ?: return@run null // 範囲採掘が発動しなかった
                miningArea
            } ?: return@register f // 範囲採掘が発動しなかった
            miningArea.hardness
        }

        // サーバーサイドにおいて、最後にプレイヤーがブロックを採掘した際の向きを記憶
        LevelEvent.HANDLE_PLAYER_ACTION.register { listener, packet ->
            latestPlayerMiningDirectionCache[listener.player.id] = Pair(listener.player.level().gameTime, packet.direction)
        }

        // サーバーサイドにおいて、ブロック破壊後に範囲採掘の効果
        BlockCallback.AFTER_BREAK.register { world, player, pos, state, _, tool ->
            val serverSide = world.serverSideOrNull ?: return@register
            if (isInMagicMining.get()) return@register

            val miningDirectionCache = latestPlayerMiningDirectionCache[player.id] ?: return@register // なぜか向きが記録されていない
            if (miningDirectionCache.first != world.gameTime) return@register // なぜか向きが記録されていない
            val miningArea = run {
                val multiMine = run {
                    MultiMineHandler.REGISTRY.firstNotNullOfOrNull {
                        it.create(
                            miningDirectionCache.second,
                            world, pos, state,
                            player, tool.item, tool,
                        )
                    }
                } ?: return@run null // 範囲採掘の能力がない
                val miningArea = multiMine.collect() ?: return@run null // 範囲採掘が発動しなかった
                miningArea
            } ?: return@register // 範囲採掘が発動しなかった

            miningArea.multiMine.execute(serverSide)
        }

        MultiMineHandler.REGISTRY += AreaMiningMultiMineHandler
        MultiMineHandler.REGISTRY += CutAllMultiMineHandler
        MultiMineHandler.REGISTRY += MineAllMultiMineHandler

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
