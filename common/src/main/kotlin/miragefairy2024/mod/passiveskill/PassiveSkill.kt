package miragefairy2024.mod.passiveskill

import com.mojang.serialization.Codec
import miragefairy2024.mod.fairy.Motif
import net.minecraft.core.BlockPos
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.chat.Component
import net.minecraft.network.codec.StreamCodec
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level

interface PassiveSkillProvider {
    fun getPassiveSkill(itemStack: ItemStack): PassiveSkill?
}

class PassiveSkill(val providerId: ResourceLocation, val motif: Motif?, val rare: Double, val count: Double, val specifications: List<PassiveSkillSpecification<*>>)

class PassiveSkillSpecification<T : Any>(val conditions: List<PassiveSkillCondition>, val effect: PassiveSkillEffect<T>, val valueProvider: (mana: Double) -> T)

interface PassiveSkillCondition {
    val text: Component
    fun test(context: PassiveSkillContext, level: Double, mana: Double): Boolean
}

interface PassiveSkillEffect<T : Any> {
    val isPreprocessor: Boolean
    fun getText(value: T): Component
    fun getTexts(value: T): List<Component> = listOf(getText(value))
    val unit: T
    fun castOrThrow(value: Any?): T
    fun combine(a: T, b: T): T
    fun codec(): Codec<T> = Codec.unit(Unit).xmap({ unit }, {})
    fun streamCodec(): StreamCodec<in RegistryFriendlyByteBuf, T> = StreamCodec.unit<RegistryFriendlyByteBuf, _>(Unit).map({ unit }, {})
    fun update(context: PassiveSkillContext, oldValue: T, newValue: T)
}

class PassiveSkillContext(val world: Level, val blockPos: BlockPos, val player: Player)
