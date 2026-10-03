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

    const val SEARCH_MIN_DISTANCE = 4.0
    const val SEARCH_RADIUS = 64.0
    const val SEARCH_ANGLE = 45.0

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

    /** 毎 tick エンティティのテーブルを引かないように、追尾先の参照を持っておくのだ～🌱 */
    private var target: LivingEntity? = null

    override fun tick() {
        val level = level()

        // 追尾先の判定は射出した瞬間にのみ行うから、速度が減衰する前の最初の tick で済ませるのだ～🌱
        if (level is ServerLevel && !searched) {
            searched = true
            target = searchTarget(level)
            targetUuid = target?.uuid
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
     * セーブデータから復元した直後の 1 回だけ、UUID からエンティティのテーブルを引くのだ～🌱
     */
    private fun resolveTarget(level: ServerLevel): LivingEntity? {
        target?.let { return it.takeIf { it.isTargetable } }
        val uuid = targetUuid ?: return null
        // 引けなかった場合に毎 tick 探し直さないように、UUID の方は捨てるのだ～🌱
        targetUuid = null
        val entity = level.getEntity(uuid) as? LivingEntity ?: return null
        target = entity
        return entity.takeIf { it.isTargetable }
    }

    /** 追尾しなかった場合の弾道のうち、相手と水平距離が一致する時点の座標が、最も相手に近い相手を選ぶのだ～🌱 */
    private fun searchTarget(level: ServerLevel): LivingEntity? {
        val initialVelocity = deltaMovement
        if (initialVelocity.length() < 0.001) return null
        val origin = position()
        val shotDirection = initialVelocity.normalize()
        val minDistanceSqr = MirageMissileCard.SEARCH_MIN_DISTANCE * MirageMissileCard.SEARCH_MIN_DISTANCE
        val searchRadiusSqr = MirageMissileCard.SEARCH_RADIUS * MirageMissileCard.SEARCH_RADIUS

        // ディスペンサーから撃った場合は撃った本人が居ないから、敵味方の判定を伴わない方の分岐が選ばれるのだ～🌱
        val owner = owner as? LivingEntity

        return level.getEntitiesOfClass(LivingEntity::class.java, AABB(origin, origin).inflate(MirageMissileCard.SEARCH_RADIUS)) { TargetingConditions.DEFAULT.test(owner, it) }
            .mapNotNull { entity ->
                if (!entity.isTargetable) return@mapNotNull null
                val center = entity.boundingBox.center
                val distanceSqr = center.distanceToSqr(origin)
                if (distanceSqr < minDistanceSqr) return@mapNotNull null
                if (distanceSqr > searchRadiusSqr) return@mapNotNull null
                if (angleDegrees(shotDirection, center.subtract(origin)) > MirageMissileCard.SEARCH_ANGLE) return@mapNotNull null
                val offset = center.subtract(origin)
                val trajectoryOffset = getTrajectoryOffset(initialVelocity, Math.sqrt(offset.x * offset.x + offset.z * offset.z)) ?: return@mapNotNull null
                Pair(entity, offset.distanceToSqr(trajectoryOffset))
            }
            .minByOrNull { it.second }
            ?.first
    }

    override fun addAdditionalSaveData(compound: CompoundTag) {
        super.addAdditionalSaveData(compound)
        (target?.uuid ?: targetUuid)?.let { compound.putUUID("Target", it) }
        compound.putBoolean("Searched", searched)
    }

    override fun readAdditionalSaveData(compound: CompoundTag) {
        super.readAdditionalSaveData(compound)
        targetUuid = if (compound.hasUUID("Target")) compound.getUUID("Target") else null
        searched = compound.getBoolean("Searched")
    }

    override fun getDefaultPickupItem() = ItemStack(MirageMissileCard.item())
}

/** 死んだ相手と、透明で光ってもいない相手は、狙う相手として不適格なのだ～🌱 */
private val LivingEntity.isTargetable get() = isAlive && !(isInvisible && !isCurrentlyGlowing)

/**
 * 初速 [initialVelocity] で撃たれた矢が、射出位置からの水平距離が [horizontalDistance] になった時点で居る、射出位置からの相対位置なのだ～🌱
 * 矢がその水平距離まで届かない場合と、真上や真下に近い向きで撃たれて水平方向にほとんど進まない場合は、null を返すのだ～🌱
 *
 * [net.minecraft.world.entity.projectile.AbstractArrow.tick] は、旧速度で位置を進めてから、速度に慣性を掛けて重力を引くのだ～🌱
 * すると各軸の速度が等比数列になるから、その和として、n tick 後の位置を閉じた式で書けるのだ～🌱
 * 慣性を無視した初速と重力加速度だけの放物線は、20 tick の時点で既に 5 ブロック以上ずれるから、慣性を含めた式でなければならないのだ～🌱
 */
private fun getTrajectoryOffset(initialVelocity: Vec3, horizontalDistance: Double): Vec3? {
    val horizontalSpeed = Math.sqrt(initialVelocity.x * initialVelocity.x + initialVelocity.z * initialVelocity.z)
    if (horizontalSpeed < 1.0e-6) return null

    // 慣性の累乗 k^n は、水平距離が等比数列の和であることから逆算できるのだ～🌱
    val inertiaPower = 1.0 - horizontalDistance * (1.0 - MirageMissileCard.AIR_INERTIA) / horizontalSpeed
    if (inertiaPower <= 0.0) return null // 水平方向の到達距離には上限があって、そこへ届かない場合なのだ～🌱
    val ticks = Math.log(inertiaPower) / Math.log(MirageMissileCard.AIR_INERTIA)

    val velocitySum = horizontalDistance / horizontalSpeed // = (1 - k^n) / (1 - k)
    val y = initialVelocity.y * velocitySum - MirageMissileCard.GRAVITY / (1.0 - MirageMissileCard.AIR_INERTIA) * (ticks - velocitySum)
    return Vec3(initialVelocity.x / horizontalSpeed * horizontalDistance, y, initialVelocity.z / horizontalSpeed * horizontalDistance)
}

/** 単位ベクトル [direction] と、長さを問わない [offset] のなす角を、度数で返すのだ～🌱 */
private fun angleDegrees(direction: Vec3, offset: Vec3): Double {
    val length = offset.length()
    if (length < 1.0e-6) return 0.0
    return Math.toDegrees(acos((direction.dot(offset) / length).coerceIn(-1.0, 1.0)))
}
