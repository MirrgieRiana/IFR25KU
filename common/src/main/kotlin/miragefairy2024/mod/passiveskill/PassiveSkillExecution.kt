package miragefairy2024.mod.passiveskill

import com.mojang.serialization.Codec
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.fairy.SoulStream
import miragefairy2024.mod.fairy.contains
import miragefairy2024.mod.fairy.soulStream
import miragefairy2024.mod.passiveskill.effects.ManaBoostPassiveSkillEffect
import miragefairy2024.util.Translation
import miragefairy2024.util.enJa
import miragefairy2024.util.eyeBlockPos
import miragefairy2024.util.get
import miragefairy2024.util.getOrCreate
import miragefairy2024.util.invoke
import miragefairy2024.util.isValid
import miragefairy2024.util.register
import miragefairy2024.util.set
import miragefairy2024.util.text
import mirrg.kotlin.helium.Slot
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate
import net.fabricmc.fabric.api.attachment.v1.AttachmentType
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import kotlin.math.log

private val identifier = MirageFairy2024.identifier("passive_skill")
val PASSIVE_SKILL_TRANSLATION = Translation({ "gui.${identifier.toLanguageKey()}" }, "Passive Skills", "パッシブスキル")
val PASSIVE_SKILL_DISABLED_TRANSLATION = Translation({ "gui.${identifier.toLanguageKey()}.disabled" }, "Outside target slot", "対象スロット外")
val PASSIVE_SKILL_OVERFLOWED_TRANSLATION = Translation({ "gui.${identifier.toLanguageKey()}.overflowed" }, "Too many passive skills!", "パッシブスキルが多すぎます！")
val PASSIVE_SKILL_SUPPORTING_TRANSLATION = Translation({ "gui.${identifier.toLanguageKey()}.supporting" }, "Supporting other item", "他のアイテムを支援中")
val PASSIVE_SKILL_EFFECTIVE_TRANSLATION = Translation({ "gui.${identifier.toLanguageKey()}.effective" }, "Effective", "発動中")

context(ModContext)
fun initPassiveSkillExecution() {

    // プレイヤー追加データ登録
    PASSIVE_SKILL_RESULT_ATTACHMENT_TYPE.register()

    // イベント処理
    ServerTickEvents.END_SERVER_TICK.register { server ->
        if (server.tickCount % 20 == 0) {
            server.playerList.players.forEach { player ->
                if (!player.isValid) return@forEach

                // 現在装備しているパッシブスキルの列挙
                val passiveSkillProviders = player.findPassiveSkillProviders()

                // 現在発動しているパッシブスキル効果の計算
                val result = PassiveSkillResult()
                result.collect(passiveSkillProviders.passiveSkills, player, ManaBoostPassiveSkillEffect.Value(mapOf()), true) // 先行判定
                val manaBoostValue = result[ManaBoostPassiveSkillEffect]
                result.collect(passiveSkillProviders.passiveSkills, player, manaBoostValue, false) // 後行判定

                // 効果
                result.update(player)

            }
        }
    }

    // 翻訳
    PASSIVE_SKILL_TRANSLATION.enJa()
    PASSIVE_SKILL_DISABLED_TRANSLATION.enJa()
    PASSIVE_SKILL_OVERFLOWED_TRANSLATION.enJa()
    PASSIVE_SKILL_SUPPORTING_TRANSLATION.enJa()
    PASSIVE_SKILL_EFFECTIVE_TRANSLATION.enJa()

}

enum class PassiveSkillStatus {
    /** アイテムが有効なスロットにありません。 */
    DISABLED,

    /** パッシブスキルがあふれています。 */
    OVERFLOWED,

    /** 同種のアイテムを支援中。 */
    SUPPORTING,

    /** パッシブスキルは有効です。 */
    EFFECTIVE,
}

val PassiveSkillStatus.description
    get() = when (this) {
        PassiveSkillStatus.DISABLED -> text { PASSIVE_SKILL_DISABLED_TRANSLATION() }
        PassiveSkillStatus.OVERFLOWED -> text { PASSIVE_SKILL_OVERFLOWED_TRANSLATION() }
        PassiveSkillStatus.SUPPORTING -> text { PASSIVE_SKILL_SUPPORTING_TRANSLATION() }
        PassiveSkillStatus.EFFECTIVE -> text { PASSIVE_SKILL_EFFECTIVE_TRANSLATION() }
    }

class PassiveSkillProviders(val providers: List<Triple<ItemStack, PassiveSkillStatus, PassiveSkill>>, val passiveSkills: List<PassiveSkill>)

@Suppress("UnusedReceiverParameter")
fun Player.getPassiveSkillCount() = 9

fun Player.findPassiveSkillProviders(): PassiveSkillProviders {
    val passiveSkillCount = this.getPassiveSkillCount()

    val providers = mutableListOf<Triple<ItemStack, PassiveSkillStatus, Slot<PassiveSkill>>>()
    val passiveSkillSlotListTable = mutableMapOf<ResourceLocation, MutableList<Slot<PassiveSkill>>>()

    fun addItemStack(itemStack: ItemStack, enabled: Boolean) {
        val item = itemStack.item
        if (item !is PassiveSkillProvider) return
        val passiveSkill = item.getPassiveSkill(itemStack) ?: return

        val passiveSkills = passiveSkillSlotListTable[passiveSkill.providerId]
        if (passiveSkills != null) { // 既存パッシブスキル
            // 他のアイテムを支援
            val slot = Slot(passiveSkill)
            providers += Triple(itemStack, PassiveSkillStatus.SUPPORTING, slot)
            passiveSkills += slot
        } else { // 新規パッシブスキル
            if (!enabled) { // 発動対象スロットでない場所に配置されている
                // 発動対象でないため新規パッシブスキルを発動しない
            } else { // 発動対象スロットに配置されている
                // パッシブスキルを新しく発動しようとしている
                if (passiveSkillSlotListTable.size >= passiveSkillCount) { // パッシブスキルの枠が満杯
                    // パッシブスキルがあふれた
                    providers += Triple(itemStack, PassiveSkillStatus.OVERFLOWED, Slot(passiveSkill))
                } else { // パッシブスキルの枠に余裕がある
                    // パッシブスキルを新しく発動する
                    val slot = Slot(passiveSkill)
                    providers += Triple(itemStack, PassiveSkillStatus.EFFECTIVE, slot)
                    passiveSkillSlotListTable[passiveSkill.providerId] = mutableListOf(slot)
                }
            }
        }
    }

    // アイテムを検出
    addItemStack(this.offhandItem, true)
    this.armorSlots.forEach {
        addItemStack(it, true)
    }
    repeat(SoulStream.SLOT_COUNT) { index ->
        addItemStack(this.soulStream.getOrCreate()[index], index < SoulStream.PASSIVE_SKILL_SLOT_COUNT)
    }

    // パッシブスキルを統合
    val passiveSkills = passiveSkillSlotListTable.values.map { passiveSkillSlotList ->
        val mainPassiveSkillSlot = passiveSkillSlotList[0]
        val passiveSkill = PassiveSkill(
            mainPassiveSkillSlot.value.providerId,
            mainPassiveSkillSlot.value.motif,
            mainPassiveSkillSlot.value.rare,
            passiveSkillSlotList.sumOf { it.value.count },
            mainPassiveSkillSlot.value.specifications,
        )
        passiveSkillSlotList.forEach {
            it.value = passiveSkill
        }
        passiveSkill
    }
    val actualProviders = providers.map { Triple(it.first, it.second, it.third.value) }

    return PassiveSkillProviders(actualProviders, passiveSkills)
}

fun PassiveSkillResult.collect(passiveSkills: Iterable<PassiveSkill>, player: Player, manaBoostValue: ManaBoostPassiveSkillEffect.Value, isPreprocessing: Boolean) {
    val context = PassiveSkillContext(player.level(), player.eyeBlockPos, player)

    passiveSkills.forEach { passiveSkill ->
        val motif = passiveSkill.motif
        val level = passiveSkill.rare + log(passiveSkill.count, 3.0)
        val mana = level * (1.0 + manaBoostValue.map.entries.sumOf { (keyMotif, value) -> if (motif in keyMotif) value else 0.0 })
        passiveSkill.specifications.forEach { specification ->
            fun <T : Any> f(specification: PassiveSkillSpecification<T>) {
                if (specification.effect.isPreprocessor == isPreprocessing) {
                    if (specification.conditions.all { it.test(context, level, mana) }) {
                        this.add(specification.effect, specification.valueProvider(mana))
                    }
                }
            }
            f(specification)
        }
    }
}

fun PassiveSkillResult.update(player: Player) {
    val context = PassiveSkillContext(player.level(), player.eyeBlockPos, player)

    val oldResult = player.passiveSkillResult.getOrCreate()
    player.passiveSkillResult.set(this)

    passiveSkillEffectRegistry.entrySet().forEach {
        fun <T : Any> f(type: PassiveSkillEffect<T>) {
            val oldValue = oldResult[type]
            val newValue = this[type]
            type.update(context, oldValue, newValue)
        }
        f(it.value)
    }
}


// PassiveSkillResult

val PASSIVE_SKILL_RESULT_ATTACHMENT_TYPE: AttachmentType<PassiveSkillResult> = AttachmentRegistry.create(MirageFairy2024.identifier("passive_skill_result")) {
    it.persistent(PassiveSkillResult.CODEC)
    it.initializer(::PassiveSkillResult)
    it.syncWith(PassiveSkillResult.STREAM_CODEC, AttachmentSyncPredicate.targetOnly())
    it.copyOnDeath()
}

val Entity.passiveSkillResult get() = this[PASSIVE_SKILL_RESULT_ATTACHMENT_TYPE]

class PassiveSkillResult() {
    companion object {
        val CODEC: Codec<PassiveSkillResult> = Codec.dispatchedMap(passiveSkillEffectRegistry.byNameCodec()) { it.codec() }.xmap(::PassiveSkillResult, PassiveSkillResult::map)
        val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, PassiveSkillResult> = object : StreamCodec<RegistryFriendlyByteBuf, PassiveSkillResult> {
            override fun decode(buffer: RegistryFriendlyByteBuf): PassiveSkillResult {
                val value = PassiveSkillResult()
                val size = buffer.readVarInt()
                repeat(size) { _ ->
                    val passiveSkillEffect = passiveSkillEffectRegistry.get(ResourceLocation.STREAM_CODEC.decode(buffer))!!
                    fun <T : Any> f(passiveSkillEffect: PassiveSkillEffect<T>) {
                        val it = passiveSkillEffect.streamCodec().decode(buffer)
                        value.map[passiveSkillEffect] = it
                    }
                    f(passiveSkillEffect)
                }
                return value
            }

            override fun encode(buffer: RegistryFriendlyByteBuf, value: PassiveSkillResult) {
                buffer.writeVarInt(value.map.size)
                value.map.forEach { (passiveSkillEffect, it) ->
                    fun <T : Any> f(passiveSkillEffect: PassiveSkillEffect<T>) {
                        ResourceLocation.STREAM_CODEC.encode(buffer, passiveSkillEffectRegistry.getKey(passiveSkillEffect)!!)
                        passiveSkillEffect.streamCodec().encode(buffer, passiveSkillEffect.castOrThrow(it))
                    }
                    f(passiveSkillEffect)
                }
            }
        }
    }

    constructor (map: Map<PassiveSkillEffect<*>, Any>) : this() {
        this.map += map
    }

    val map = mutableMapOf<PassiveSkillEffect<*>, Any>()

    operator fun <T : Any> get(type: PassiveSkillEffect<T>) = if (type in map) type.castOrThrow(map[type]) else type.unit

    fun <T : Any> add(type: PassiveSkillEffect<T>, value: T) {
        if (type in map) {
            map[type] = type.combine(type.castOrThrow(map[type]), value)
        } else {
            map[type] = value
        }
    }
}
