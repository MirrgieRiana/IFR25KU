package miragefairy2024.mod.mantle

import dev.architectury.registry.level.entity.EntityAttributeRegistry
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.ModEvents
import miragefairy2024.mod.common.mirageFairy2024ItemGroupCard
import miragefairy2024.util.EnJa
import miragefairy2024.util.ItemLootPoolEntry
import miragefairy2024.util.LootPool
import miragefairy2024.util.LootTable
import miragefairy2024.util.Model
import miragefairy2024.util.Registration
import miragefairy2024.util.ResourceLocation
import miragefairy2024.util.enJa
import miragefairy2024.util.register
import miragefairy2024.util.registerItemGroup
import miragefairy2024.util.registerLootTableGeneration
import miragefairy2024.util.registerModelGeneration
import miragefairy2024.util.registerSpawn
import miragefairy2024.util.times
import miragefairy2024.util.unaryPlus
import net.minecraft.core.BlockPos
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.util.Mth
import net.minecraft.util.TimeUtil
import net.minecraft.util.valueproviders.UniformInt
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.MobCategory
import net.minecraft.world.entity.NeutralMob
import net.minecraft.world.entity.PathfinderMob
import net.minecraft.world.entity.SpawnPlacementTypes
import net.minecraft.world.entity.SpawnPlacements
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.ai.control.MoveControl
import net.minecraft.world.entity.ai.goal.Goal
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal
import net.minecraft.world.entity.ai.goal.target.ResetUniversalAngerTargetGoal
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.SpawnEggItem
import net.minecraft.world.level.Level
import net.minecraft.world.level.ServerLevelAccessor
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.levelgen.Heightmap
import net.minecraft.world.phys.Vec3
import java.util.EnumSet
import java.util.UUID

/** マントルのウィスプが、攻撃されてから怒り続ける時間なのだ～🌱 */
private val MANTLE_WISP_ANGER_TIME = TimeUtil.rangeOfSeconds(20, 39)

object MantleWispCard {
    val identifier = MirageFairy2024.identifier("mantle_wisp")
    val name = EnJa("Mantle Wisp", "マントルのフェアリーウィスプ")

    val entityType = Registration(BuiltInRegistries.ENTITY_TYPE, identifier) {
        EntityType.Builder.of({ entityType: EntityType<MantleWispEntity>, level: Level -> MantleWispEntity(entityType, level) }, MobCategory.MONSTER)
            .sized(0.6F, 0.9F)
            .fireImmune()
            .build()
    }

    val spawnEggItem = Registration(BuiltInRegistries.ITEM, identifier * "_egg") { SpawnEggItem(entityType.await(), 0x2A1008, 0xFFAA44, Item.Properties()) }

    context(ModContext)
    fun init() {
        entityType.register()
        ModEvents.onConstruction {
            EntityAttributeRegistry.register({ entityType() }, {
                // ネザーの砦のピグリンブルートと同じ水準の戦闘力なのだ～🌱
                PathfinderMob.createMobAttributes()
                    .add(Attributes.MAX_HEALTH, 50.0)
                    .add(Attributes.ATTACK_DAMAGE, 13.0)
                    .add(Attributes.MOVEMENT_SPEED, 0.28)
                    .add(Attributes.FOLLOW_RANGE, 48.0)
                    .add(Attributes.KNOCKBACK_RESISTANCE, 0.6)
            })
        }
        entityType.enJa(name)

        entityType.registerLootTableGeneration {
            LootTable(
                LootPool(
                    ItemLootPoolEntry(MantleMaterialCard.WADSLEYITE.item()),
                ),
            )
        }

        entityType.registerSpawn(MobCategory.MONSTER, 30, 1, 2) { +mantleBiomeKey }
        ModEvents.onInitialize {
            SpawnPlacements.register(
                entityType(),
                SpawnPlacementTypes.NO_RESTRICTIONS,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                SpawnPlacements.SpawnPredicate<MantleWispEntity> { _, _, _, _, _ -> true },
            )
        }

        spawnEggItem.register()
        spawnEggItem.registerItemGroup(mirageFairy2024ItemGroupCard.itemGroupKey)
        spawnEggItem.registerModelGeneration(Model(ResourceLocation("minecraft", "item/template_spawn_egg")))
        spawnEggItem.enJa(EnJa("${name.en} Spawn Egg", "${name.ja}のスポーンエッグ"))
    }
}

/**
 * マントルディメンションを漂う、中立のフェアリーウィスプなのだ～🌱
 *
 * 普段はプレイヤーの近くをただ漂うだけだけど、攻撃されると反撃してくるのだ～🌱
 */
class MantleWispEntity(entityType: EntityType<out MantleWispEntity>, level: Level) : PathfinderMob(entityType, level), NeutralMob {

    private var remainingPersistentAngerTime = 0
    private var persistentAngerTarget: UUID? = null

    init {
        xpReward = 10
        moveControl = MantleWispMoveControl(this)
        noPhysics = true
    }

    override fun registerGoals() {
        goalSelector.addGoal(4, MantleWispChargeAttackGoal(this))
        goalSelector.addGoal(5, MantleWispFloatAroundGoal(this))
        goalSelector.addGoal(7, LookAtPlayerGoal(this, Player::class.java, 16.0F))
        targetSelector.addGoal(1, HurtByTargetGoal(this).setAlertOthers())
        targetSelector.addGoal(3, ResetUniversalAngerTargetGoal(this, true))
    }

    override fun getRemainingPersistentAngerTime() = remainingPersistentAngerTime
    override fun setRemainingPersistentAngerTime(remainingPersistentAngerTime: Int) {
        this.remainingPersistentAngerTime = remainingPersistentAngerTime
    }

    override fun getPersistentAngerTarget() = persistentAngerTarget
    override fun setPersistentAngerTarget(persistentAngerTarget: UUID?) {
        this.persistentAngerTarget = persistentAngerTarget
    }

    override fun startPersistentAngerTimer() {
        remainingPersistentAngerTime = MANTLE_WISP_ANGER_TIME.sample(random)
    }

    override fun customServerAiStep() {
        super.customServerAiStep()
        updatePersistentAnger(level() as net.minecraft.server.level.ServerLevel, false)
    }

    override fun addAdditionalSaveData(compound: net.minecraft.nbt.CompoundTag) {
        super.addAdditionalSaveData(compound)
        addPersistentAngerSaveData(compound)
    }

    override fun readAdditionalSaveData(compound: net.minecraft.nbt.CompoundTag) {
        super.readAdditionalSaveData(compound)
        readPersistentAngerSaveData(level(), compound)
    }

    override fun getAmbientSound() = null

    override fun checkFallDamage(y: Double, onGround: Boolean, state: BlockState, pos: BlockPos) = Unit

    override fun canBeLeashed() = false

    override fun isPushable() = false

    override fun doPush(entity: Entity) = Unit

    override fun pushEntities() = Unit

    override fun finalizeSpawn(level: ServerLevelAccessor, difficulty: net.minecraft.world.DifficultyInstance, spawnType: net.minecraft.world.entity.MobSpawnType, spawnGroupData: net.minecraft.world.entity.SpawnGroupData?): net.minecraft.world.entity.SpawnGroupData? {
        // 天井や床に埋まらないように、湧いた場所から少し浮かせるのだ～🌱
        return super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData)
    }
}

/** ガストと同様に、空中をふわふわと漂うための移動制御なのだ～🌱 */
private class MantleWispMoveControl(private val entity: MantleWispEntity) : MoveControl(entity) {
    private var floatDuration = 0

    override fun tick() {
        if (operation != Operation.MOVE_TO) return
        if (floatDuration-- > 0) return
        floatDuration += entity.random.nextInt(5) + 2
        val delta = Vec3(wantedX - entity.x, wantedY - entity.y, wantedZ - entity.z)
        val distance = delta.length()
        if (distance < 1.0E-4) return
        entity.deltaMovement = entity.deltaMovement.add(delta.normalize().scale(0.08))
    }
}

/** プレイヤーの近くまで、ゆっくり漂って行くのだ～🌱 */
private class MantleWispFloatAroundGoal(private val entity: MantleWispEntity) : Goal() {
    init {
        flags = EnumSet.of(Flag.MOVE)
    }

    override fun canUse() = entity.target == null

    override fun canContinueToUse() = false

    override fun start() {
        val player = entity.level().getNearestPlayer(entity, 32.0)
        val random = entity.random
        val target = if (player != null) {
            Vec3(player.x, player.y + 1.5, player.z)
        } else {
            Vec3(
                entity.x + (random.nextFloat() * 2.0F - 1.0F) * 8.0F,
                entity.y + (random.nextFloat() * 2.0F - 1.0F) * 4.0F,
                entity.z + (random.nextFloat() * 2.0F - 1.0F) * 8.0F,
            )
        }
        entity.moveControl.setWantedPosition(target.x, target.y, target.z, 1.0)
    }
}

/** 怒っている間は、標的に体当たりを繰り返すのだ～🌱 */
private class MantleWispChargeAttackGoal(private val entity: MantleWispEntity) : Goal() {
    init {
        flags = EnumSet.of(Flag.MOVE)
    }

    override fun canUse(): Boolean {
        val target = entity.target ?: return false
        return target.isAlive
    }

    override fun canContinueToUse() = canUse()

    override fun tick() {
        val target = entity.target ?: return
        entity.moveControl.setWantedPosition(target.x, target.y + target.bbHeight * 0.5, target.z, 1.0)
        if (entity.boundingBox.inflate(0.3).intersects(target.boundingBox)) {
            entity.doHurtTarget(target)
        }
        entity.yRot = -(Mth.atan2(entity.deltaMovement.x, entity.deltaMovement.z).toFloat()) * (180.0F / Mth.PI)
        entity.yBodyRot = entity.yRot
    }
}
