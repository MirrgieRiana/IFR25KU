package miragefairy2024.mod.enchantment.contents

import miragefairy2024.ModContext
import miragefairy2024.mixins.api.BlockCallback
import miragefairy2024.mod.enchantment.EnchantmentCard
import miragefairy2024.util.get
import miragefairy2024.util.isValid
import net.minecraft.core.BlockPos
import net.minecraft.core.registries.Registries
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.ExperienceOrb
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.item.enchantment.EnchantmentHelper
import net.minecraft.world.level.Level
import net.minecraft.world.phys.AABB

context(ModContext)
fun initStickyMining() {
    val listener = ThreadLocal<() -> Unit>()
    BlockCallback.BEFORE_DROP_BY_ENTITY.register { _, level, pos, _, entity, tool ->
        if (entity == null) return@register
        val stickyMiningLevel = EnchantmentHelper.getItemEnchantmentLevel(level.registryAccess()[Registries.ENCHANTMENT, EnchantmentCard.STICKY_MINING.key], tool)
        if (stickyMiningLevel == 0) return@register

        val snapshot = StickyMiningSnapshot.take(level, pos)
        listener.set {
            snapshot.teleportNewEntities(entity)
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

class StickyMiningSnapshot private constructor(
    private val level: Level,
    private val pos: BlockPos,
    private val oldItemEntities: Set<ItemEntity>,
    private val oldExperienceOrbs: Set<ExperienceOrb>,
) {
    fun teleportNewEntities(entity: Entity) {
        val newItemEntities = getItemEntities(level, pos)
        val newExperienceOrbs = getExperienceOrbs(level, pos)

        (newItemEntities - oldItemEntities).forEach {
            it.teleportTo(entity.x, entity.y, entity.z)
            it.setNoPickUpDelay()
        }
        (newExperienceOrbs - oldExperienceOrbs).forEach {
            it.teleportTo(entity.x, entity.y, entity.z)
        }
    }

    companion object {
        private fun getItemEntities(level: Level, pos: BlockPos): Set<ItemEntity> {
            return level.getEntitiesOfClass(ItemEntity::class.java, AABB(pos)) { it.isValid }.toSet()
        }

        private fun getExperienceOrbs(level: Level, pos: BlockPos): Set<ExperienceOrb> {
            return level.getEntitiesOfClass(ExperienceOrb::class.java, AABB(pos)) { it.isValid }.toSet()
        }

        fun take(level: Level, pos: BlockPos): StickyMiningSnapshot {
            return StickyMiningSnapshot(level, pos, getItemEntities(level, pos), getExperienceOrbs(level, pos))
        }
    }
}
