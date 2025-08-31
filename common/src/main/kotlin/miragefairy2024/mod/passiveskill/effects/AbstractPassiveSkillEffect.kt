package miragefairy2024.mod.passiveskill.effects

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.PrimitiveCodec
import io.netty.buffer.ByteBuf
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.passiveskill.PassiveSkillEffect
import miragefairy2024.mod.passiveskill.passiveSkillEffectRegistry
import miragefairy2024.util.Registration
import miragefairy2024.util.register
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec

abstract class AbstractPassiveSkillEffect<T : Any>(path: String) : PassiveSkillEffect<T> {
    val identifier = MirageFairy2024.identifier(path)

    context(ModContext)
    open fun init() {
        Registration(passiveSkillEffectRegistry, identifier) { this }.register()
    }

    override val isPreprocessor = false
}

abstract class AbstractDoublePassiveSkillEffect(path: String) : AbstractPassiveSkillEffect<Double>(path) {
    override val unit = 0.0
    override fun castOrNull(value: Any?) = value as? Double
    override fun castOrThrow(value: Any?) = value as Double
    override fun combine(a: Double, b: Double) = a + b
    override fun codec(): PrimitiveCodec<Double> = Codec.DOUBLE
    override fun streamCodec(): StreamCodec<ByteBuf, Double> = ByteBufCodecs.DOUBLE
}

abstract class AbstractBooleanPassiveSkillEffect(path: String) : AbstractPassiveSkillEffect<Boolean>(path) {
    override val unit = false
    override fun castOrNull(value: Any?) = value as? Boolean
    override fun castOrThrow(value: Any?) = value as Boolean
    override fun combine(a: Boolean, b: Boolean) = a || b
    override fun codec(): PrimitiveCodec<Boolean> = Codec.BOOL
    override fun streamCodec(): StreamCodec<ByteBuf, Boolean> = ByteBufCodecs.BOOL
}
