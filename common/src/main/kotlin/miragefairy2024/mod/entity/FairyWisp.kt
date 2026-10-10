package miragefairy2024.mod.entity

import dev.architectury.registry.level.entity.EntityAttributeRegistry
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.ModEvents
import miragefairy2024.mod.common.mirageFairy2024ItemGroupCard
import miragefairy2024.util.EnJa
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
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors
import net.minecraft.core.BlockPos
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.util.Mth
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.FlyingMob
import net.minecraft.world.entity.MobCategory
import net.minecraft.world.entity.SpawnPlacementTypes
import net.minecraft.world.entity.SpawnPlacements
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.ai.control.MoveControl
import net.minecraft.world.entity.ai.goal.Goal
import net.minecraft.world.entity.ambient.Bat
import net.minecraft.world.item.Item
import net.minecraft.world.item.SpawnEggItem
import net.minecraft.world.level.Level
import net.minecraft.world.level.biome.Biomes
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.levelgen.Heightmap
import net.minecraft.world.phys.Vec3
import java.util.EnumSet

object FairyWispCard {
    val spawnGroup = MobCategory.AMBIENT
    val width = 0.5F
    val height = 0.9F
    fun createEntity(entityType: EntityType<FairyWispEntity>, world: Level) = FairyWispEntity(entityType, world)
    val identifier = MirageFairy2024.identifier("fairy_wisp")
    val name = EnJa("Fairy Wisp", "フェアリーウィスプ")
    val entityType = Registration(BuiltInRegistries.ENTITY_TYPE, identifier) {
        EntityType.Builder.of({ entityType, world -> createEntity(entityType, world) }, spawnGroup)
            .sized(width, height)
            .build()
    }
    val spawnEggItem = Registration(BuiltInRegistries.ITEM, identifier * "_egg") { SpawnEggItem(entityType.await(), 0x88BBFF, 0xFF88FF, Item.Properties()) }

    context(ModContext)
    fun init() {
        entityType.register()
        ModEvents.onConstruction {
            // 攻撃しない環境モブなので基本属性のみなのだ～🌱
            EntityAttributeRegistry.register({ entityType() }, {
                FlyingMob.createMobAttributes()
                    .add(Attributes.MAX_HEALTH, 6.0)
                    .add(Attributes.FOLLOW_RANGE, 48.0)
            })
        }
        entityType.enJa(name)

        // ドロップなしなのだ～🌱（コウモリと同じく loot table が空）
        entityType.registerLootTableGeneration { _ ->
            LootTable()
        }

        // 地下の洞窟バイオームにスポーンするのだ～🌱
        entityType.registerSpawn(MobCategory.AMBIENT, 5, 1, 3) { BiomeSelectors.includeByKey(Biomes.DRIPSTONE_CAVES, Biomes.LUSH_CAVES, Biomes.DEEP_DARK) }
        ModEvents.onInitialize {
            // 飛行モブなので地面判定なしで湧くのだ～🌱（コウモリの spawn rules を流用）
            @Suppress("UNCHECKED_CAST")
            SpawnPlacements.register(
                entityType(),
                SpawnPlacementTypes.NO_RESTRICTIONS,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                SpawnPlacements.SpawnPredicate<FairyWispEntity> { _, level, spawnType, blockPos, random ->
                    Bat.checkBatSpawnRules(EntityType.BAT, level, spawnType, blockPos, random)
                },
            )
        }

        spawnEggItem.register()
        spawnEggItem.registerItemGroup(mirageFairy2024ItemGroupCard.itemGroupKey)
        spawnEggItem.registerModelGeneration(Model(ResourceLocation("minecraft", "item/template_spawn_egg")))
        spawnEggItem.enJa(EnJa("${name.en} Spawn Egg", "${name.ja}のスポーンエッグ"))
    }
}

class FairyWispEntity(entityType: EntityType<out FairyWispEntity>, world: Level) : FlyingMob(entityType, world) {

    init {
        // 攻撃されると即消えるコウモリと同様、xpReward は 0 なのだ～🌱
        xpReward = 0
        moveControl = WispMoveControl(this)
    }

    override fun registerGoals() {
        // ガストと同様にランダムに宙を漂う Goal を登録するのだ～🌱
        goalSelector.addGoal(5, RandomFloatAroundGoal(this))
        goalSelector.addGoal(7, LookGoal(this))
        // 攻撃 Goal は一切登録しないのだ～🌱
    }

    override fun getAmbientSound() = null

    override fun checkFallDamage(y: Double, onGround: Boolean, state: BlockState, pos: BlockPos) {
        // 飛行モブなので落下ダメージはなしなのだ～🌱
    }

    /** コウモリと同様、リードに繋げないのだ～🌱 */
    override fun canBeLeashed() = false

    /** コウモリと同様、他のエンティティを押しのけないのだ～🌱 */
    override fun isPushable() = false

    /** コウモリと同様、押しのけ処理をしないのだ～🌱 */
    override fun doPush(entity: Entity) {}

    /** コウモリと同様、他エンティティとの押し合いをしないのだ～🌱 */
    override fun pushEntities() {}

    // ===== AI Goal =====

    /** ガストの GhastMoveControl を参考にした、空中をゆっくり漂う MoveControl なのだ～🌱 */
    private class WispMoveControl(private val entity: FairyWispEntity) : MoveControl(entity) {
        private var floatDuration = 0

        override fun tick() {
            if (operation == Operation.MOVE_TO) {
                if (floatDuration-- <= 0) {
                    floatDuration += entity.random.nextInt(5) + 2
                    val vec3 = Vec3(wantedX - entity.x, wantedY - entity.y, wantedZ - entity.z)
                    val d = vec3.length()
                    val normalized = vec3.normalize()
                    if (canReach(normalized, Mth.ceil(d))) {
                        entity.setDeltaMovement(entity.deltaMovement.add(normalized.scale(0.1)))
                    } else {
                        operation = Operation.WAIT
                    }
                }
            }
        }

        private fun canReach(pos: Vec3, length: Int): Boolean {
            var aabb = entity.boundingBox
            for (i in 1 until length) {
                aabb = aabb.move(pos)
                if (!entity.level().noCollision(entity, aabb)) return false
            }
            return true
        }
    }

    /** ガストの RandomFloatAroundGoal を参考にした、ランダムに漂う Goal なのだ～🌱 */
    private class RandomFloatAroundGoal(private val entity: FairyWispEntity) : Goal() {
        init {
            flags = EnumSet.of(Flag.MOVE)
        }

        override fun canUse(): Boolean {
            val moveControl = entity.moveControl
            if (!moveControl.hasWanted()) return true
            val d = moveControl.wantedX - entity.x
            val e = moveControl.wantedY - entity.y
            val f = moveControl.wantedZ - entity.z
            val g = d * d + e * e + f * f
            return g < 1.0 || g > 3600.0
        }

        override fun canContinueToUse() = false

        override fun start() {
            val random = entity.random
            // ガストより狭い範囲（±8ブロック）でふわふわ漂うのだ～🌱
            val d = entity.x + (random.nextFloat() * 2.0F - 1.0F) * 8.0F
            val e = entity.y + (random.nextFloat() * 2.0F - 1.0F) * 4.0F
            val f = entity.z + (random.nextFloat() * 2.0F - 1.0F) * 8.0F
            entity.moveControl.setWantedPosition(d, e, f, 1.0)
        }
    }

    /** ガストの GhastLookGoal を参考にした、移動方向を向く Goal なのだ～🌱 */
    private class LookGoal(private val entity: FairyWispEntity) : Goal() {
        init {
            flags = EnumSet.of(Flag.LOOK)
        }

        override fun canUse() = true

        override fun requiresUpdateEveryTick() = true

        override fun tick() {
            val vec3 = entity.deltaMovement
            entity.setYRot(-((Math.atan2(vec3.x, vec3.z)).toFloat()) * (180.0F / Math.PI.toFloat()))
            entity.yBodyRot = entity.yRot
        }
    }
}
