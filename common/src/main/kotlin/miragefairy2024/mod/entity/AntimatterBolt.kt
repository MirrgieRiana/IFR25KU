package miragefairy2024.mod.entity

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.SoundEventCard
import miragefairy2024.mod.particle.ParticleTypeCard
import miragefairy2024.mod.tool.MagicDamageTypeCard
import miragefairy2024.util.Registration
import miragefairy2024.util.generator
import miragefairy2024.util.getValue
import miragefairy2024.util.isServer
import miragefairy2024.util.register
import miragefairy2024.util.registerChild
import miragefairy2024.util.setValue
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.sounds.SoundSource
import net.minecraft.tags.EntityTypeTags
import net.minecraft.world.entity.EntityEvent
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.MobCategory
import net.minecraft.world.entity.projectile.Projectile
import net.minecraft.world.entity.projectile.ProjectileUtil
import net.minecraft.world.level.Level
import net.minecraft.world.phys.EntityHitResult
import net.minecraft.world.phys.HitResult
import net.minecraft.world.phys.Vec3

object AntimatterBoltCard {
    val spawnGroup = MobCategory.MISC
    val width = 0.5F
    val height = 0.5F
    fun createEntity(entityType: EntityType<AntimatterBoltEntity>, world: Level) = AntimatterBoltEntity(entityType, world)
    val identifier = MirageFairy2024.identifier("antimatter_bolt")
    val entityType = Registration(BuiltInRegistries.ENTITY_TYPE, identifier) {
        EntityType.Builder.of({ entityType, world -> createEntity(entityType, world) }, spawnGroup)
            .sized(width, height)
            .build()
    }

    context(ModContext)
    fun init() {
        entityType.register()
        EntityTypeTags.IMPACT_PROJECTILES.generator.registerChild(entityType)
    }
}

class AntimatterBoltEntity(entityType: EntityType<out AntimatterBoltEntity>, world: Level) : Projectile(entityType, world) {
    companion object {
        val DAMAGE: EntityDataAccessor<Float> = SynchedEntityData.defineId(AntimatterBoltEntity::class.java, EntityDataSerializers.FLOAT)
        val MAX_DISTANCE: EntityDataAccessor<Float> = SynchedEntityData.defineId(AntimatterBoltEntity::class.java, EntityDataSerializers.FLOAT)
    }


    var damage by DAMAGE
    var maxDistance by MAX_DISTANCE

    override fun defineSynchedData(builder: SynchedEntityData.Builder) {
        builder.define(DAMAGE, 0F)
        builder.define(MAX_DISTANCE, 0F)
    }

    override fun addAdditionalSaveData(nbt: CompoundTag) {
        nbt.putFloat("Damage", damage)
        nbt.putFloat("MaxDistance", maxDistance)
    }

    override fun readAdditionalSaveData(nbt: CompoundTag) {
        damage = nbt.getFloat("Damage")
        maxDistance = nbt.getFloat("MaxDistance")
    }


    private var prevPos: Vec3? = null
    override fun tick() {
        super.tick()

        // 距離判定
        val movingDistance = deltaMovement.length().toFloat()
        val stopped = if (movingDistance >= maxDistance) { // 10 >= 1 → deltaMovement *= 0.1( = 1 / 10)
            if (movingDistance >= 0.001F) deltaMovement = deltaMovement.scale((maxDistance / movingDistance).toDouble())
            true
        } else {
            false
        }
        maxDistance -= movingDistance

        // 衝突判定
        val hitResult = ProjectileUtil.getHitResultOnMoveVector(this) { canHitEntity(it) }
        if (hitResult.type != HitResult.Type.MISS) onHit(hitResult)
        if (isRemoved) return

        checkInsideBlocks()
        if (isRemoved) return

        val vec3d = deltaMovement
        val nextX = x + vec3d.x
        val nextY = y + vec3d.y
        val nextZ = z + vec3d.z

        updateRotation()

        // 水に触れると消滅
        if (isInWaterOrBubble) {
            discard()
            return
        }

        // パーティクル
        prevPos?.let { prevPos ->
            repeat(2) { i ->
                level().addParticle(
                    ParticleTypeCard.AURA.particleType,
                    prevPos.x + (x - prevPos.x) * (i / 3.0),
                    prevPos.y + (y - prevPos.y) * (i / 3.0),
                    prevPos.z + (z - prevPos.z) * (i / 3.0),
                    (random.nextDouble() - 0.5) * 0.02,
                    (random.nextDouble() - 0.5) * 0.02,
                    (random.nextDouble() - 0.5) * 0.02,
                )
            }
        }

        // 距離判定
        if (stopped) {
            discard()
            return
        }

        // 位置更新
        prevPos = position()
        setPos(nextX, nextY, nextZ)

    }

    override fun onHit(hitResult: HitResult) {
        super.onHit(hitResult)
        if (level().isServer) {
            level().broadcastEntityEvent(this, EntityEvent.DEATH)
            discard()
        }
    }

    override fun onHitEntity(entityHitResult: EntityHitResult) {
        super.onHitEntity(entityHitResult)
        entityHitResult.entity.hurt(level().damageSources().source(MagicDamageTypeCard.registryKey, this, owner as? LivingEntity), damage)
    }

    override fun handleEntityEvent(status: Byte) {
        super.handleEntityEvent(status)
        if (status == EntityEvent.DEATH) {

            level().playLocalSound(x, y, z, SoundEventCard.MAGIC_HIT.soundEvent, SoundSource.NEUTRAL, 0.5F, 0.90F + (level().random.nextFloat() - 0.5F) * 0.3F, true)

            for (i in 0..7) {
                level().addParticle(
                    ParticleTypeCard.AURA.particleType,
                    x, y, z,
                    (random.nextDouble() - 0.5) * 0.3,
                    (random.nextDouble() - 0.5) * 0.3,
                    (random.nextDouble() - 0.5) * 0.3,
                )
            }
        }
    }

}
