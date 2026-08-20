package miragefairy2024.mod.tool.items

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModifyItemEnchantmentsHandler
import miragefairy2024.mixins.api.NoSlowdownWhileUsingItem
import miragefairy2024.mod.SoundEventCard
import miragefairy2024.mod.enchantment.EnchantmentCard
import miragefairy2024.mod.enchantment.MAGIC_WEAPON_ITEM_TAG
import miragefairy2024.mod.entity.AntimatterBoltCard
import miragefairy2024.mod.entity.AntimatterBoltEntity
import miragefairy2024.mod.tool.ToolConfiguration
import miragefairy2024.mod.tool.ToolMaterialCard
import miragefairy2024.util.Model
import miragefairy2024.util.ModelData
import miragefairy2024.util.ModelDisplayData
import miragefairy2024.util.ModelDisplayEntryData
import miragefairy2024.util.ModelTexturesData
import miragefairy2024.util.ResourceLocation
import miragefairy2024.util.Translation
import miragefairy2024.util.get
import miragefairy2024.util.getLevel
import miragefairy2024.util.getRate
import miragefairy2024.util.invoke
import miragefairy2024.util.string
import miragefairy2024.util.text
import miragefairy2024.util.yellow
import net.minecraft.core.BlockPos
import net.minecraft.core.HolderLookup
import net.minecraft.core.registries.Registries
import net.minecraft.data.models.model.TextureSlot
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.sounds.SoundSource
import net.minecraft.stats.Stats
import net.minecraft.tags.ItemTags
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Tier
import net.minecraft.world.item.TieredItem
import net.minecraft.world.item.Tiers
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.item.UseAnim
import net.minecraft.world.item.enchantment.Enchantment
import net.minecraft.world.item.enchantment.ItemEnchantments
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState
import kotlin.math.ceil

open class FairyShootingStaffConfiguration(
    override val toolMaterialCard: ToolMaterialCard,
    var basePower: Float,
    var baseMaxDistance: Float,
) : ToolConfiguration() {
    override fun createItem(properties: Item.Properties) = FairyShootingStaffItem(this, properties)

    init {
        this.tags += MAGIC_WEAPON_ITEM_TAG
        this.tags += ItemTags.DURABILITY_ENCHANTABLE
        this.miningDamage = 2
        this.modelTemplateOverride = SHOOTING_STAFF_MODEL_TEMPLATE
    }
}

/**
 * 杖を立てて構えた姿勢で持つためのアイテムモデルなのだ～🌱
 *
 * `minecraft:item/handheld` を親にして、手持ちの座標変換だけを差し替えているのだ～🌱
 * GUI や地面に落ちているときの見た目は、親から受け継いだ通常のアイテムのままなのだ～🌱
 *
 * テクスチャの柄は、左下の隅から右上の頭に向かう、傾き 47.2 度の帯なのだ～🌱
 * 手に来る点はモデルの中心であり、そこから `rotation` の Z 成分だけ柄が回るから、
 * 柄の傾きは 47.2 度に Z 成分を足した値になるのだ～🌱
 * 第三者視点では、傾き 0 度がちょうど鉛直の上向きに対応するから、Z を -30 度にすると、
 * 柄は鉛直から 17.2 度だけ前に倒れた、杖を立てて構えた姿勢になるのだ～🌱
 * 第一人称視点では、傾き 90 度が鉛直の上向きに対応するから、同じ姿勢にするには Z を 60 度にするのだ～🌱
 *
 * `translation` は、柄の下端から 3 分の 1 の位置が手に来るように定めた値なのだ～🌱
 * 元の `handheld` も、柄の下端から 3 割ほどの位置を握る値になっているのだ～🌱
 *
 * `scale` は、第三者視点で全長 1.5 ブロックになる 1.2 なのだ～🌱
 * プレイヤーの身長 1.8 ブロックの 8 割強で、頭が顔の高さに、柄の下端が膝の高さに来るのだ～🌱
 */
private val SHOOTING_STAFF_MODEL_TEMPLATE = Model { textureMapping ->
    ModelData(
        parent = ResourceLocation("item/handheld"),
        textures = ModelTexturesData(
            TextureSlot.LAYER0.id to textureMapping.get(TextureSlot.LAYER0).string,
        ),
        display = ModelDisplayData(
            thirdPersonRightHand = ModelDisplayEntryData(rotation = listOf(0, -90, -30), translation = listOf(0, 1, 4), scale = listOf(1.2F, 1.2F, 1.2F)),
            thirdPersonLeftHand = ModelDisplayEntryData(rotation = listOf(0, 90, 30), translation = listOf(0, 1, 4), scale = listOf(1.2F, 1.2F, 1.2F)),
            firstPersonRightHand = ModelDisplayEntryData(rotation = listOf(0, -90, 60), translation = listOf(1.5F, 4.33F, -1.2F), scale = listOf(0.9F, 0.9F, 0.9F)),
            firstPersonLeftHand = ModelDisplayEntryData(rotation = listOf(0, 90, -60), translation = listOf(1.5F, 4.33F, -1.2F), scale = listOf(0.9F, 0.9F, 0.9F)),
        ),
    )
}

class FairyShootingStaffItem(override val configuration: FairyShootingStaffConfiguration, settings: Properties) :
    ShootingStaffItem(configuration.toolMaterialCard.toolMaterial, configuration.basePower, configuration.baseMaxDistance, settings),
    FairyToolItem,
    ModifyItemEnchantmentsHandler {

    override fun mineBlock(stack: ItemStack, world: Level, state: BlockState, pos: BlockPos, miner: LivingEntity): Boolean {
        super.mineBlock(stack, world, state, pos, miner)
        postMineImpl(stack, world, state, pos, miner)
        return true
    }

    override fun hurtEnemy(stack: ItemStack, target: LivingEntity, attacker: LivingEntity): Boolean {
        super.hurtEnemy(stack, target, attacker)
        postHitImpl(stack, target, attacker)
        return true
    }

    override fun inventoryTick(stack: ItemStack, world: Level, entity: Entity, slot: Int, selected: Boolean) {
        super.inventoryTick(stack, world, entity, slot, selected)
        inventoryTickImpl(stack, world, entity, slot, selected)
    }

    override fun modifyItemEnchantments(itemStack: ItemStack, mutableItemEnchantments: ItemEnchantments.Mutable, enchantmentLookup: HolderLookup.RegistryLookup<Enchantment>) = modifyItemEnchantmentsImpl(itemStack, mutableItemEnchantments, enchantmentLookup)

    override fun isFoil(stack: ItemStack) = super.isFoil(stack) || hasGlintImpl(stack)

}

open class ShootingStaffItem(toolMaterial: Tier, private val basePower: Float, private val baseMaxDistance: Float, settings: Properties) : TieredItem(toolMaterial, settings), NoSlowdownWhileUsingItem {
    companion object {
        val NOT_ENOUGH_EXPERIENCE_TRANSLATION = Translation({ "item.${MirageFairy2024.identifier("fairy_tool_item").toLanguageKey()}.not_enough_experience" }, "Not enough experience", "経験値が足りません")
        val DESCRIPTION_TRANSLATION = Translation({ "item.${MirageFairy2024.identifier("shooting_staff").toLanguageKey()}.description" }, "Charge while held, then perform a ranged attack when released", "使用中、チャージし、解除時に射撃攻撃")
        const val BASE_EXPERIENCE_COST = 2

        /** チャージの基準となる素材なのだ～🌱 この素材の採掘速度で、チャージ時間が[BASE_CHARGE_TICKS]になるのだ～🌱 */
        private val BASE_CHARGE_TIER = Tiers.IRON

        /** [BASE_CHARGE_TIER]の採掘速度におけるチャージ時間なのだ～🌱 */
        private const val BASE_CHARGE_TICKS = 2 * 20

        /** エンチャント適性が[BASE_CHARGE_TIER]から 1 離れるごとに変わる基本攻撃力なのだ～🌱 */
        private const val ENCHANTMENT_VALUE_POWER_FACTOR = 0.25F

        /** 使用を継続できる上限のティック数なのだ～🌱 弓と同じく、事実上の無制限なのだ～🌱 */
        private const val MAX_USE_TICKS = 72000
    }

    /**
     * チャージに要するティック数なのだ～🌱
     *
     * 採掘速度に反比例するのだ～🌱 素材を通して魔力を汲み出す速さが、そのまま岩を掘り崩す速さと同じ性質だとみなしているのだ～🌱
     * 魔法加速のエンチャントは、撤廃されたクールタイムの短縮の代わりに、チャージ時間を短縮するのだ～🌱
     */
    private fun getChargeTicks(world: Level, itemStack: ItemStack): Int {
        val acceleration = 1.0 + world.registryAccess()[Registries.ENCHANTMENT, EnchantmentCard.MAGIC_ACCELERATION.key].getRate(itemStack)
        return ceil(BASE_CHARGE_TICKS * BASE_CHARGE_TIER.speed / tier.speed / acceleration).toInt()
    }

    /**
     * 魔法射撃攻撃のダメージなのだ～🌱
     *
     * 剣の攻撃力が武器種の補正と素材の補正の和であるのと同じように、武器種の補正である[basePower]に、素材の補正を加えるのだ～🌱
     * 素材の補正は、攻撃力ではなく、魔力の通りやすさを表すエンチャント適性を参照するのだ～🌱
     * [BASE_CHARGE_TIER]のエンチャント適性を並のものとみなして、そこからの差に[ENCHANTMENT_VALUE_POWER_FACTOR]を掛けるのだ～🌱
     */
    private fun getDamage(world: Level, itemStack: ItemStack): Float {
        val materialPower = ENCHANTMENT_VALUE_POWER_FACTOR * (tier.enchantmentValue - BASE_CHARGE_TIER.enchantmentValue)
        return basePower + materialPower + 0.5F * world.registryAccess()[Registries.ENCHANTMENT, EnchantmentCard.MAGIC_POWER.key].getLevel(itemStack).toFloat()
    }

    private fun getExperienceCost(world: Level, itemStack: ItemStack) = BASE_EXPERIENCE_COST + 1 * world.registryAccess()[Registries.ENCHANTMENT, EnchantmentCard.MAGIC_POWER.key].getLevel(itemStack)

    override fun appendHoverText(stack: ItemStack, context: TooltipContext, tooltipComponents: MutableList<Component>, tooltipFlag: TooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag)
        tooltipComponents += text { DESCRIPTION_TRANSLATION().yellow }
    }

    override fun getUseDuration(stack: ItemStack, entity: LivingEntity) = MAX_USE_TICKS

    override fun getUseAnimation(stack: ItemStack) = UseAnim.NONE

    override fun use(world: Level, user: Player, hand: InteractionHand): InteractionResultHolder<ItemStack> {
        val itemStack = user.getItemInHand(hand)

        if (!user.isCreative) {
            if (user.totalExperience < getExperienceCost(world, itemStack)) {
                if (!world.isClientSide) user.displayClientMessage(text { NOT_ENOUGH_EXPERIENCE_TRANSLATION() }, true)
                return InteractionResultHolder.fail(itemStack)
            }
        }

        user.startUsingItem(hand)
        return InteractionResultHolder.consume(itemStack)
    }

    override fun onUseTick(level: Level, livingEntity: LivingEntity, stack: ItemStack, remainingUseDuration: Int) {
        if (level.isClientSide) return
        if (MAX_USE_TICKS - remainingUseDuration != getChargeTicks(level, stack)) return // チャージが満ちた瞬間だけ知らせるのだ～🌱
        level.playSound(null, livingEntity.x, livingEntity.y, livingEntity.z, SoundEventCard.MAGIC2.soundEvent, SoundSource.PLAYERS, 0.3F, 1.6F)
    }

    override fun releaseUsing(stack: ItemStack, level: Level, livingEntity: LivingEntity, timeCharged: Int) {
        if (level.isClientSide) return
        val user = livingEntity as? Player ?: return
        if (MAX_USE_TICKS - timeCharged < getChargeTicks(level, stack)) return // チャージが満ちる前に離したら不発なのだ～🌱

        val damage = getDamage(level, stack)
        val maxDistance = baseMaxDistance + 3F * level.registryAccess()[Registries.ENCHANTMENT, EnchantmentCard.MAGIC_REACH.key].getLevel(stack)
        val speed = 2.0F + 2.0F * level.registryAccess()[Registries.ENCHANTMENT, EnchantmentCard.MAGIC_REACH.key].getRate(stack).toFloat()
        val experienceCost = getExperienceCost(level, stack)

        if (!user.isCreative) {
            if (user.totalExperience < experienceCost) {
                user.displayClientMessage(text { NOT_ENOUGH_EXPERIENCE_TRANSLATION() }, true)
                return
            }
        }

        // 生成
        val entity = AntimatterBoltEntity(AntimatterBoltCard.entityType(), level)
        entity.setPos(user.x, user.eyeY - 0.3, user.z)
        entity.shootFromRotation(user, user.xRot, user.yRot, 0.0F, speed, 1.0F)
        entity.owner = user
        entity.damage = damage
        entity.maxDistance = maxDistance
        level.addFreshEntity(entity)

        // 消費
        stack.hurtAndBreak(1, user, LivingEntity.getSlotForHand(user.usedItemHand))
        if (!user.isCreative) user.giveExperiencePoints(-experienceCost)

        // 統計
        user.awardStat(Stats.ITEM_USED.get(this))

        // エフェクト
        level.playSound(null, user.x, user.y, user.z, SoundEventCard.MAGIC2.soundEvent, SoundSource.PLAYERS, 0.6F, 0.90F + (level.random.nextFloat() - 0.5F) * 0.3F)
    }

    override fun hurtEnemy(stack: ItemStack, target: LivingEntity, attacker: LivingEntity): Boolean {
        return true
    }

    override fun postHurtEnemy(stack: ItemStack, target: LivingEntity, attacker: LivingEntity) {
        stack.hurtAndBreak(2, attacker, EquipmentSlot.MAINHAND)
    }
}
