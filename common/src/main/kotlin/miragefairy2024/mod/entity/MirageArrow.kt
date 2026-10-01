package miragefairy2024.mod.entity

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.ModEvents
import miragefairy2024.mod.PoemList
import miragefairy2024.mod.common.mirageFairy2024ItemGroupCard
import miragefairy2024.mod.poem
import miragefairy2024.mod.registerPoem
import miragefairy2024.mod.registerPoemGeneration
import miragefairy2024.util.EnJa
import miragefairy2024.util.Registration
import miragefairy2024.util.enJa
import miragefairy2024.util.generator
import miragefairy2024.util.register
import miragefairy2024.util.registerChild
import miragefairy2024.util.registerGeneratedModelGeneration
import miragefairy2024.util.registerItemGroup
import net.minecraft.core.Direction
import net.minecraft.core.Position
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerLevel
import net.minecraft.tags.EntityTypeTags
import net.minecraft.tags.ItemTags
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.MobCategory
import net.minecraft.world.entity.monster.Enemy
import net.minecraft.world.entity.projectile.AbstractArrow
import net.minecraft.world.entity.projectile.Projectile
import net.minecraft.world.item.ArrowItem
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.DispenserBlock
import java.util.UUID

object MirageArrowCard {
    val identifier = MirageFairy2024.identifier("mirage_arrow")
    val name = EnJa("Mirage Arrow", "ミラージュの矢")
    val item = Registration(BuiltInRegistries.ITEM, identifier) { MirageArrowItem(Item.Properties()) }
    val entityType = Registration(BuiltInRegistries.ENTITY_TYPE, identifier) {
        EntityType.Builder.of({ entityType, level -> MirageArrowEntity(entityType, level) }, MobCategory.MISC)
            .sized(0.5F, 0.5F)
            .eyeHeight(0.13F)
            .clientTrackingRange(4)
            .updateInterval(2)
            .build()
    }
    val poemList = PoemList(1).poem("TODO", "TODO") // TODO ミラージュの矢のポエムは、まだ書かれていないのだ～🌱

    const val SEARCH_INTERVAL = 10
    const val SEARCH_RADIUS = 16.0
    const val TURN_RATE = 0.2

    context(ModContext)
    fun init() {
        entityType.register()
        entityType.enJa(name)
        EntityTypeTags.ARROWS.generator.registerChild(entityType)

        item.register()
        item.registerItemGroup(mirageFairy2024ItemGroupCard.itemGroupKey)
        item.registerGeneratedModelGeneration()
        item.enJa(name)
        item.registerPoem(poemList)
        item.registerPoemGeneration(poemList)
        ItemTags.ARROWS.generator.registerChild(item)
        ModEvents.onInitialize {
            DispenserBlock.registerProjectileBehavior(item())
        }
    }
}

class MirageArrowItem(properties: Properties) : ArrowItem(properties) {
    override fun createArrow(level: Level, ammo: ItemStack, shooter: LivingEntity, weapon: ItemStack?): AbstractArrow {
        return MirageArrowEntity(level, shooter, ammo.copyWithCount(1), weapon)
    }

    override fun asProjectile(level: Level, pos: Position, stack: ItemStack, direction: Direction): Projectile {
        val entity = MirageArrowEntity(level, pos.x(), pos.y(), pos.z(), stack.copyWithCount(1), null)
        entity.pickup = AbstractArrow.Pickup.ALLOWED
        return entity
    }
}

class MirageArrowEntity : AbstractArrow {
    constructor(entityType: EntityType<out MirageArrowEntity>, level: Level) : super(entityType, level)
    constructor(level: Level, owner: LivingEntity, pickupItemStack: ItemStack, firedFromWeapon: ItemStack?) : super(MirageArrowCard.entityType(), owner, level, pickupItemStack, firedFromWeapon)
    constructor(level: Level, x: Double, y: Double, z: Double, pickupItemStack: ItemStack, firedFromWeapon: ItemStack?) : super(MirageArrowCard.entityType(), x, y, z, level, pickupItemStack, firedFromWeapon)

    private var targetUuid: UUID? = null

    override fun tick() {
        super.tick()
        val level = level()
        if (level !is ServerLevel) return
        if (inGround) return

        // 追尾先
        var target = targetUuid?.let { level.getEntity(it) as? LivingEntity }?.takeIf { it.isAlive }
        if (target == null && tickCount % MirageArrowCard.SEARCH_INTERVAL == 0) {
            target = level.getEntitiesOfClass(LivingEntity::class.java, boundingBox.inflate(MirageArrowCard.SEARCH_RADIUS)) { it is Enemy && it.isAlive && it != owner }
                .minByOrNull { it.distanceToSqr(this) }
            targetUuid = target?.uuid
        }
        if (target == null) return

        // 向き変更
        val speed = deltaMovement.length()
        if (speed < 0.001) return
        val direction = deltaMovement.normalize()
        val targetDirection = target.boundingBox.center.subtract(position()).normalize()
        deltaMovement = direction.add(targetDirection.subtract(direction).scale(MirageArrowCard.TURN_RATE)).normalize().scale(speed)
        hasImpulse = true
    }

    override fun addAdditionalSaveData(compound: CompoundTag) {
        super.addAdditionalSaveData(compound)
        targetUuid?.let { compound.putUUID("Target", it) }
    }

    override fun readAdditionalSaveData(compound: CompoundTag) {
        super.readAdditionalSaveData(compound)
        targetUuid = if (compound.hasUUID("Target")) compound.getUUID("Target") else null
    }

    override fun getDefaultPickupItem() = ItemStack(MirageArrowCard.item())
}
