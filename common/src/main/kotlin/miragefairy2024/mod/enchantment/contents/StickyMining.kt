package miragefairy2024.mod.enchantment.contents

import miragefairy2024.ModContext
import miragefairy2024.mixins.api.BlockCallback
import miragefairy2024.mod.enchantment.EnchantmentCard
import miragefairy2024.util.get
import miragefairy2024.util.isValid
import net.minecraft.core.registries.Registries
import net.minecraft.world.entity.ExperienceOrb
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.item.enchantment.EnchantmentHelper
import net.minecraft.world.phys.AABB

context(ModContext)
fun initStickyMining() {
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
