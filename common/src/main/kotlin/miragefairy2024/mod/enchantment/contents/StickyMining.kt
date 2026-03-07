package miragefairy2024.mod.enchantment.contents

import miragefairy2024.ModContext
import miragefairy2024.mixins.api.BlockCallback
import miragefairy2024.mod.enchantment.EnchantmentCard
import miragefairy2024.util.get
import miragefairy2024.util.isValid
import net.minecraft.core.registries.Registries
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.ExperienceOrb
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.item.enchantment.EnchantmentHelper
import net.minecraft.world.level.Level
import net.minecraft.world.phys.AABB

class StickyMiningSnapshot(private val world: Level, private val aabb: AABB) {
    private val oldItemEntities = world.getEntitiesOfClass(ItemEntity::class.java, aabb) { it.isValid }.toSet()
    private val oldExperienceOrbs = world.getEntitiesOfClass(ExperienceOrb::class.java, aabb) { it.isValid }.toSet()

    fun teleportNewEntities(target: Entity) {
        (world.getEntitiesOfClass(ItemEntity::class.java, aabb) { it.isValid }.toSet() - oldItemEntities).forEach {
            it.teleportTo(target.x, target.y, target.z)
            it.setNoPickUpDelay()
        }
        (world.getEntitiesOfClass(ExperienceOrb::class.java, aabb) { it.isValid }.toSet() - oldExperienceOrbs).forEach {
            it.teleportTo(target.x, target.y, target.z)
        }
    }
}

context(ModContext)
fun initStickyMining() {
    val listener = ThreadLocal<() -> Unit>()
    BlockCallback.BEFORE_DROP_BY_ENTITY.register { _, level, pos, _, entity, tool ->
        if (entity == null) return@register
        val stickyMiningLevel = EnchantmentHelper.getItemEnchantmentLevel(level.registryAccess()[Registries.ENCHANTMENT, EnchantmentCard.STICKY_MINING.key], tool)
        if (stickyMiningLevel == 0) return@register

        val snapshot = StickyMiningSnapshot(level, AABB(pos))

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
