package miragefairy2024.mod.entity

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.ModEvents
import miragefairy2024.mod.PoemList
import miragefairy2024.mod.common.mirageFairy2024ItemGroupCard
import miragefairy2024.mod.materials.MaterialCard
import miragefairy2024.mod.poem
import miragefairy2024.mod.registerPoem
import miragefairy2024.mod.registerPoemGeneration
import miragefairy2024.util.EnJa
import miragefairy2024.util.Registration
import miragefairy2024.util.enJa
import miragefairy2024.util.generator
import miragefairy2024.util.on
import miragefairy2024.util.register
import miragefairy2024.util.registerChild
import miragefairy2024.util.registerGeneratedModelGeneration
import miragefairy2024.util.registerItemGroup
import miragefairy2024.util.registerShapedRecipeGeneration
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
import net.minecraft.world.entity.ai.targeting.TargetingConditions
import net.minecraft.world.entity.projectile.AbstractArrow
import net.minecraft.world.entity.projectile.Projectile
import net.minecraft.world.item.ArrowItem
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.DispenserBlock
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import java.util.UUID
import kotlin.math.acos

object MirageMissileCard {
    val identifier = MirageFairy2024.identifier("mirage_missile")
    val name = EnJa("Mirage Missile", "ミラージュミサイル")
    val item = Registration(BuiltInRegistries.ITEM, identifier) { MirageMissileItem(Item.Properties()) }
    val entityType = Registration(BuiltInRegistries.ENTITY_TYPE, identifier) {
        EntityType.Builder.of({ entityType, level -> MirageMissileEntity(entityType, level) }, MobCategory.MISC)
            .sized(0.5F, 0.5F)
            .eyeHeight(0.13F)
            .clientTrackingRange(4)
            .updateInterval(2)
            .build()
    }
    val poemList = PoemList(1).poem("TODO", "TODO") // TODO ミラージュミサイルの日英のポエムが入るのだ～🌱

    const val SEARCH_RADIUS = 64.0
    const val SEARCH_ANGLE = 45.0

    // 弓をいっぱいに引いた矢は毎 tick 3 ブロック進むから、寄せる割合が小さいと曲がりきらないのだ～🌱
    const val TURN_RATE = 0.5

    // 真上へ撃った矢は最高点が反応距離を超えるから、弾道の追跡が終わらない場合の打ち切りなのだ～🌱
    const val MAX_TRAJECTORY_TICKS = 200

    // AbstractArrow が水の外で毎 tick 速度に掛ける値と、既定の重力なのだ～🌱
    const val AIR_INERTIA = 0.99
    const val GRAVITY = 0.05

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
        registerShapedRecipeGeneration(item, 4) {
            pattern("  F")
            pattern(" S ")
            pattern("L  ")
            define('F', Items.FLINT)
            define('S', MaterialCard.MIRAGE_STEM.item())
            define('L', MaterialCard.MIRAGE_LEAVES.item())
        } on MaterialCard.MIRAGE_STEM.item
        ModEvents.onInitialize {
            DispenserBlock.registerProjectileBehavior(item())
        }
    }
}

class MirageMissileItem(properties: Properties) : ArrowItem(properties) {
    override fun createArrow(level: Level, ammo: ItemStack, shooter: LivingEntity, weapon: ItemStack?): AbstractArrow {
        return MirageMissileEntity(level, shooter, ammo.copyWithCount(1), weapon)
    }

    override fun asProjectile(level: Level, pos: Position, stack: ItemStack, direction: Direction): Projectile {
        val entity = MirageMissileEntity(level, pos.x(), pos.y(), pos.z(), stack.copyWithCount(1), null)
        entity.pickup = AbstractArrow.Pickup.ALLOWED
        return entity
    }
}

class MirageMissileEntity : AbstractArrow {
    constructor(entityType: EntityType<out MirageMissileEntity>, level: Level) : super(entityType, level)
    constructor(level: Level, owner: LivingEntity, pickupItemStack: ItemStack, firedFromWeapon: ItemStack?) : super(MirageMissileCard.entityType(), owner, level, pickupItemStack, firedFromWeapon)
    constructor(level: Level, x: Double, y: Double, z: Double, pickupItemStack: ItemStack, firedFromWeapon: ItemStack?) : super(MirageMissileCard.entityType(), x, y, z, level, pickupItemStack, firedFromWeapon)

    private var targetUuid: UUID? = null
    private var searched = false

    override fun tick() {
        val level = level()

        // 追尾先の判定は射出した瞬間にのみ行うから、速度が減衰する前の最初の tick で済ませるのだ～🌱
        if (level is ServerLevel && !searched) {
            searched = true
            targetUuid = searchTarget(level)?.uuid
        }

        super.tick()

        if (level !is ServerLevel) return
        if (inGround) return

        // 追尾先が見つからなかったか、消えた場合は、もう向きを変えないのだ～🌱
        val target = targetUuid?.let { level.getEntity(it) as? LivingEntity }?.takeIf { it.isAlive } ?: return

        // 向き変更
        val speed = deltaMovement.length()
        if (speed < 0.001) return
        val direction = deltaMovement.normalize()
        val targetDirection = target.boundingBox.center.subtract(position()).normalize()
        deltaMovement = direction.add(targetDirection.subtract(direction).scale(MirageMissileCard.TURN_RATE)).normalize().scale(speed)
        hasImpulse = true
    }

    /** 追尾しなかった場合の弾道を先に辿って、その道のりのどこかで最も角度が近くなる敵を選ぶのだ～🌱 */
    private fun searchTarget(level: ServerLevel): LivingEntity? {
        val owner = owner as? LivingEntity ?: return null
        val initialVelocity = deltaMovement
        if (initialVelocity.length() < 0.001) return null
        val origin = position()
        val shotDirection = initialVelocity.normalize()
        val searchRadiusSqr = MirageMissileCard.SEARCH_RADIUS * MirageMissileCard.SEARCH_RADIUS

        // 追尾しなかった場合に矢が通る各時点の、位置と進む向きなのだ～🌱
        val trajectory = mutableListOf<Pair<Vec3, Vec3>>()
        var position = origin
        var velocity = initialVelocity
        while (trajectory.size < MirageMissileCard.MAX_TRAJECTORY_TICKS && position.distanceToSqr(origin) <= searchRadiusSqr) {
            trajectory += Pair(position, velocity.normalize())
            // AbstractArrow は旧速度で位置を進めた後に、空気の慣性を掛けて重力を引くのだ～🌱
            position = position.add(velocity)
            velocity = velocity.scale(MirageMissileCard.AIR_INERTIA).subtract(0.0, MirageMissileCard.GRAVITY, 0.0)
        }
        if (trajectory.isEmpty()) return null

        return level.getEntitiesOfClass(LivingEntity::class.java, AABB(origin, origin).inflate(MirageMissileCard.SEARCH_RADIUS)) { TargetingConditions.DEFAULT.test(owner, it) }
            .mapNotNull { entity ->
                val center = entity.boundingBox.center
                if (center.distanceToSqr(origin) > searchRadiusSqr) return@mapNotNull null
                if (angleDegrees(shotDirection, center.subtract(origin)) > MirageMissileCard.SEARCH_ANGLE) return@mapNotNull null
                Pair(entity, trajectory.minOf { (trajectoryPosition, trajectoryDirection) -> angleDegrees(trajectoryDirection, center.subtract(trajectoryPosition)) })
            }
            .minByOrNull { it.second }
            ?.first
    }

    override fun addAdditionalSaveData(compound: CompoundTag) {
        super.addAdditionalSaveData(compound)
        targetUuid?.let { compound.putUUID("Target", it) }
        compound.putBoolean("Searched", searched)
    }

    override fun readAdditionalSaveData(compound: CompoundTag) {
        super.readAdditionalSaveData(compound)
        targetUuid = if (compound.hasUUID("Target")) compound.getUUID("Target") else null
        searched = compound.getBoolean("Searched")
    }

    override fun getDefaultPickupItem() = ItemStack(MirageMissileCard.item())
}

/** 単位ベクトル [direction] と、長さを問わない [offset] のなす角を、度数で返すのだ～🌱 */
private fun angleDegrees(direction: Vec3, offset: Vec3): Double {
    val length = offset.length()
    if (length < 1.0e-6) return 0.0
    return Math.toDegrees(acos((direction.dot(offset) / length).coerceIn(-1.0, 1.0)))
}
