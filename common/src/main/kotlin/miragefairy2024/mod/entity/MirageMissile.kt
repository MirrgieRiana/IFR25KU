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
import miragefairy2024.util.Trajectory
import miragefairy2024.util.enJa
import miragefairy2024.util.generator
import miragefairy2024.util.getPointAtHorizontalDistance
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
import kotlin.math.sqrt

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

    const val SEARCH_MIN_DISTANCE = 4.0
    const val SEARCH_RADIUS = 64.0
    const val SEARCH_ANGLE = 45.0

    /** 追尾しなかった場合の弾道で、そこへ到達するまでにこの tick 数を超える相手は、狙わないのだ～🌱 */
    const val SEARCH_MAX_TICKS = 200.0

    // 弓をいっぱいに引いた矢は毎 tick 3 ブロック進むから、寄せる割合が小さいと曲がりきらないのだ～🌱
    const val TURN_RATE = 0.5

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

    /**
     * 毎 tick エンティティのテーブルを引かないための、[miragefairy2024.mod.entity.MirageMissileEntity.targetUuid] のキャッシュなのだ～🌱
     * 追尾先として正規なのは UUID の方で、こちらは、いつ捨てても挙動が変わらないのだ～🌱
     */
    private var target: LivingEntity? = null

    override fun tick() {
        val level = level()

        // 追尾先の判定は射出した瞬間にのみ行うから、速度が減衰する前の最初の tick で済ませるのだ～🌱
        if (level is ServerLevel && !searched) {
            searched = true
            val entity = searchMirageMissileTarget(level, position(), deltaMovement, owner as? LivingEntity)
            targetUuid = entity?.uuid
            target = entity
        }

        super.tick()

        if (level !is ServerLevel) return
        if (inGround) return

        // 追尾先が見つからなかったか、消えた場合は、もう向きを変えないのだ～🌱
        val target = resolveTarget(level) ?: return

        // 向き変更
        val speed = deltaMovement.length()
        if (speed < 0.001) return
        val direction = deltaMovement.normalize()
        val targetDirection = target.boundingBox.center.subtract(position()).normalize()
        deltaMovement = direction.add(targetDirection.subtract(direction).scale(MirageMissileCard.TURN_RATE)).normalize().scale(speed)
        hasImpulse = true
    }

    /**
     * 追尾先のエンティティを返すのだ～🌱
     * キャッシュが空のときだけ、正規の UUID からエンティティのテーブルを引くのだ～🌱
     * 狙う相手として不適格になった時点で UUID を捨てるから、毎 tick 引き直すことはないのだ～🌱
     */
    private fun resolveTarget(level: ServerLevel): LivingEntity? {
        val cached = target
        if (cached != null) {
            // キャッシュが生きている場合なのだ～🌱
            if (cached.isTargetable) return cached
            // 追尾先が不適格になった場合なのだ～🌱
            targetUuid = null
            target = null
            return null
        }
        // キャッシュが空の場合なのだ～🌱 セーブデータから復元した直後か、相手を見つけられなかった場合なのだ～🌱
        val uuid = targetUuid ?: return null
        val entity = level.getEntity(uuid) as? LivingEntity
        if (entity != null && entity.isTargetable) {
            // 引けた場合なのだ～🌱
            target = entity
            return entity
        }
        // 引けなかった場合か、引けたけれど不適格だった場合なのだ～🌱
        targetUuid = null
        return null
    }

    override fun addAdditionalSaveData(compound: CompoundTag) {
        super.addAdditionalSaveData(compound)
        targetUuid?.let { compound.putUUID("Target", it) }
        compound.putBoolean("Searched", searched)
    }

    override fun readAdditionalSaveData(compound: CompoundTag) {
        super.readAdditionalSaveData(compound)
        targetUuid = if (compound.hasUUID("Target")) compound.getUUID("Target") else null
        target = null
        searched = compound.getBoolean("Searched")
    }

    override fun getDefaultPickupItem() = ItemStack(MirageMissileCard.item())
}

/**
 * 追尾しなかった場合の弾道のうち、相手と水平距離が一致する時点の座標が、最も相手に近い相手を返すのだ～🌱
 * [origin] が射出位置で、[initialVelocity] がその時点の速度で、[owner] が撃った本人なのだ～🌱
 * ディスペンサーから撃った場合は撃った本人が居ないから、[owner] が null で、[net.minecraft.world.entity.ai.targeting.TargetingConditions.test] の敵味方の判定を伴わない方の分岐が選ばれるのだ～🌱
 */
fun searchMirageMissileTarget(level: Level, origin: Vec3, initialVelocity: Vec3, owner: LivingEntity?): LivingEntity? {
    if (initialVelocity.length() < 0.001) return null
    val shotDirection = initialVelocity.normalize()
    val trajectory = Trajectory(initialVelocity, MirageMissileCard.AIR_INERTIA, MirageMissileCard.GRAVITY)
    val minDistanceSqr = MirageMissileCard.SEARCH_MIN_DISTANCE * MirageMissileCard.SEARCH_MIN_DISTANCE
    val searchRadiusSqr = MirageMissileCard.SEARCH_RADIUS * MirageMissileCard.SEARCH_RADIUS

    return level.getEntitiesOfClass(LivingEntity::class.java, AABB(origin, origin).inflate(MirageMissileCard.SEARCH_RADIUS)) { TargetingConditions.DEFAULT.test(owner, it) }
        .mapNotNull { entity ->
            if (!entity.isTargetable) return@mapNotNull null
            val center = entity.boundingBox.center
            val distanceSqr = center.distanceToSqr(origin)
            if (distanceSqr < minDistanceSqr) return@mapNotNull null
            if (distanceSqr > searchRadiusSqr) return@mapNotNull null
            if (angleDegrees(shotDirection, center.subtract(origin)) > MirageMissileCard.SEARCH_ANGLE) return@mapNotNull null
            val offset = center.subtract(origin)
            val trajectoryPoint = trajectory.getPointAtHorizontalDistance(sqrt(offset.x * offset.x + offset.z * offset.z)) ?: return@mapNotNull null
            if (trajectoryPoint.ticks > MirageMissileCard.SEARCH_MAX_TICKS) return@mapNotNull null
            Pair(entity, offset.distanceToSqr(trajectoryPoint.offset))
        }
        .minByOrNull { it.second }
        ?.first
}

/** 死んだ相手と、透明で光ってもいない相手は、狙う相手として不適格なのだ～🌱 */
private val LivingEntity.isTargetable get() = isAlive && !(isInvisible && !isCurrentlyGlowing)

/** 単位ベクトル [direction] と、長さを問わない [offset] のなす角を、度数で返すのだ～🌱 */
private fun angleDegrees(direction: Vec3, offset: Vec3): Double {
    val length = offset.length()
    if (length < 1.0e-6) return 0.0
    return Math.toDegrees(acos((direction.dot(offset) / length).coerceIn(-1.0, 1.0)))
}
