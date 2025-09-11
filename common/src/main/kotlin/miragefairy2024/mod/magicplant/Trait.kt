package miragefairy2024.mod.magicplant

import com.mojang.serialization.Codec
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.util.en
import miragefairy2024.util.ja
import miragefairy2024.util.text
import miragefairy2024.util.times
import miragefairy2024.util.translate
import mirrg.kotlin.helium.cmp
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder
import net.fabricmc.fabric.api.event.registry.RegistryAttribute
import net.minecraft.core.BlockPos
import net.minecraft.core.Registry
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.Level

// api

val traitRegistryKey: ResourceKey<Registry<Trait>> = ResourceKey.createRegistryKey(MirageFairy2024.identifier("trait"))
val traitRegistry: Registry<Trait> = FabricRegistryBuilder.createSimple(traitRegistryKey).attribute(RegistryAttribute.SYNCED).buildAndRegister()

abstract class Trait(val style: Style, val poem: Component) : Comparable<Trait> {
    companion object {
        val CODEC: Codec<Trait> = traitRegistry.byNameCodec()
        val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, Trait> = ByteBufCodecs.registry(traitRegistryKey)
    }

    abstract val conditions: List<TraitCondition>
    abstract val primaryEffect: TraitEffectKey<*>
    abstract val traitEffectKeyEntries: List<TraitEffectKeyEntry>

    /** 呼び出された時点でそこにブロックの実体が存在しない場合があります。 */
    abstract fun getTraitEffects(world: Level, blockPos: BlockPos, blockEntity: MagicPlantBlockEntity?, level: Int): MutableTraitEffects?

    override fun compareTo(other: Trait): Int {
        (this.primaryEffect.sortValue cmp other.primaryEffect.sortValue).let { if (it != 0) return it }
        (this.getIdentifier() cmp other.getIdentifier()).let { if (it != 0) return it }
        return 0
    }
}

data class TraitEffectKeyEntry(val traitEffectKey: TraitEffectKey<*>, val factor: Double)


// init

context(ModContext)
fun Trait.enJa(enName: String, jaName: String) {
    en { this.getTranslationKey() to enName }
    ja { this.getTranslationKey() to jaName }
}


// util

fun Trait.getIdentifier() = traitRegistry.getKey(this)!!
fun ResourceLocation.toTrait() = traitRegistry.get(this)

fun Trait.getTranslationKey(): String = this.getIdentifier().toLanguageKey("${MirageFairy2024.MOD_ID}.trait")
fun Trait.getName() = run { text { translate(this@run.getTranslationKey()) } }

val Trait.texture get() = "textures/gui/traits/" * this.getIdentifier() * ".png"
