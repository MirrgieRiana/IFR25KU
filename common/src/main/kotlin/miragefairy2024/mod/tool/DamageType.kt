package miragefairy2024.mod.tool

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.util.EnJa
import miragefairy2024.util.ResourceLocation
import miragefairy2024.util.en
import miragefairy2024.util.enJa
import miragefairy2024.util.generator
import miragefairy2024.util.ja
import miragefairy2024.util.registerChild
import miragefairy2024.util.registerDynamicGeneration
import miragefairy2024.util.toDamageTypeTag
import miragefairy2024.util.with
import net.minecraft.core.registries.Registries
import net.minecraft.tags.DamageTypeTags
import net.minecraft.tags.TagKey
import net.minecraft.world.damagesource.DamageType

/**
 * # 死亡メッセージのテンプレ
 * 使い分けは全く謎である。
 * ## 典型的なケースでは戦闘中に発生するもの
 * | 英語版 | 日本語版 |
 * | --- | --- |
 * | `[%1$s ... while fighting %2$s]`                       | `[%1$sは%2$sと戦いながら...]`      |
 * | `%1$s was struck by lightning while fighting %2$s`     | `%1$sは%2$sと戦いながら雷に打たれた` |
 * ## 典型的なケースでは逃走中に発生するもの
 * | 英語版 | 日本語版 |
 * | --- | --- |
 * | `[%1$s ... while trying to escape %2$s]` | `[%1$sは%2$sから逃れようとして...]` |
 * | `%1$s was killed by magic while trying to escape %2$s` | `%1$sは%2$sから逃れようとして魔法で殺された` |
 */
@Suppress("LeakingThis")
abstract class DamageTypeCard {
    abstract fun getPath(): String
    val identifier = MirageFairy2024.identifier(getPath())
    val registryKey = Registries.DAMAGE_TYPE with identifier

    open val exhaustion = 0.1F
    val damageType = DamageType(identifier.toLanguageKey(), exhaustion)

    abstract fun getKillMessage(): EnJa
    abstract fun getPlayerKillMessage(): EnJa
    abstract fun getTags(): List<TagKey<DamageType>>

    context(ModContext)
    fun init() {
        registerDynamicGeneration(registryKey) {
            damageType
        }

        en { identifier.toLanguageKey("death.attack") to getKillMessage().en }
        ja { identifier.toLanguageKey("death.attack") to getKillMessage().ja }
        en { identifier.toLanguageKey("death.attack", "player") to getPlayerKillMessage().en }
        ja { identifier.toLanguageKey("death.attack", "player") to getPlayerKillMessage().ja }
        getTags().forEach {
            it.generator.registerChild(identifier)
        }
    }
}

object MagicDamageTypeCard : DamageTypeCard() {
    override fun getPath() = "magic"
    override fun getKillMessage() = EnJa("%1\$s was killed by magic", "%1\$sは魔法で殺された")
    override fun getPlayerKillMessage() = EnJa("%1\$s was killed by magic while fighting %2\$s", "%1\$sは%2\$sと戦いながら魔法で殺された")
    override fun getTags() = listOf(DamageTypeTags.IS_PROJECTILE, DamageTypeTags.BYPASSES_ARMOR, C_IS_MAGIC_DAMAGE_TYPE_TAG, NEOFORGE_IS_MAGIC_DAMAGE_TYPE_TAG)
}

object PhysicalMagicDamageTypeCard : DamageTypeCard() {
    override fun getPath() = "physical_magic"
    override fun getKillMessage() = EnJa("%1\$s was killed by magic", "%1\$sは魔法で殺された")
    override fun getPlayerKillMessage() = EnJa("%1\$s was killed by magic while fighting %2\$s", "%1\$sは%2\$sと戦いながら魔法で殺された")
    override fun getTags() = listOf(DamageTypeTags.IS_PROJECTILE, C_IS_MAGIC_DAMAGE_TYPE_TAG, NEOFORGE_IS_MAGIC_DAMAGE_TYPE_TAG)
}

object ToolBreakDamageTypeCard : DamageTypeCard() {
    override fun getPath() = "tool_break"
    override fun getKillMessage() = EnJa("%1\$s injured their hand", "%1\$sは手を怪我した")
    override fun getPlayerKillMessage() = EnJa("%1\$s injured their hand while fighting %2\$s", "%1\$sは%2\$sと戦いながら手を怪我した")
    override fun getTags() = listOf(DamageTypeTags.NO_KNOCKBACK)
}

object MirageLeavesDamageTypeCard : DamageTypeCard() {
    override fun getPath() = "mirage_leaves"
    override fun getKillMessage() = EnJa("%1\$s was pricked to death by mirage leaves", "%1\$sはミラージュの葉で手を切って死んだ")
    override fun getPlayerKillMessage() = EnJa("%1\$s was pricked to death by mirage leaves while trying to escape %2\$s", "%1\$sは%2\$sから逃れようとしてミラージュの葉で手を切って死んだ")
    override fun getTags() = listOf(DamageTypeTags.NO_KNOCKBACK)
}

val C_IS_MAGIC_DAMAGE_TYPE_TAG = ResourceLocation("c", "is_magic").toDamageTypeTag()
val NEOFORGE_IS_MAGIC_DAMAGE_TYPE_TAG = ResourceLocation("neoforge", "is_magic").toDamageTypeTag()
val IS_MAGIC_DAMAGE_TYPE_TAG = MirageFairy2024.identifier("is_magic").toDamageTypeTag()

context(ModContext)
fun initDamageType() {
    MagicDamageTypeCard.init()
    PhysicalMagicDamageTypeCard.init()
    ToolBreakDamageTypeCard.init()
    MirageLeavesDamageTypeCard.init()

    C_IS_MAGIC_DAMAGE_TYPE_TAG.enJa(EnJa("Magic", "魔法"))
    NEOFORGE_IS_MAGIC_DAMAGE_TYPE_TAG.enJa(EnJa("Magic", "魔法"))
    IS_MAGIC_DAMAGE_TYPE_TAG.enJa(EnJa("Magic", "魔法"))
    IS_MAGIC_DAMAGE_TYPE_TAG.generator.registerChild(DamageTypeTags.BYPASSES_ARMOR)
    IS_MAGIC_DAMAGE_TYPE_TAG.generator.registerChild(C_IS_MAGIC_DAMAGE_TYPE_TAG)
    IS_MAGIC_DAMAGE_TYPE_TAG.generator.registerChild(NEOFORGE_IS_MAGIC_DAMAGE_TYPE_TAG)
}
