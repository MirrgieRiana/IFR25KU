package miragefairy2024.mod.magicplant.contents

import miragefairy2024.mod.magicplant.MutableTraitEffects
import miragefairy2024.mod.magicplant.plusAssign
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TraitEffectKeyCardTest {

    private val environmentEffectKeys = listOf(TraitEffectKeyCard.NUTRITION, TraitEffectKeyCard.TEMPERATURE, TraitEffectKeyCard.HUMIDITY).map { it.traitEffectKey }
    private val probabilityEffectKeys = listOf(TraitEffectKeyCard.NATURAL_ABSCISSION, TraitEffectKeyCard.CROSSBREEDING, TraitEffectKeyCard.MUTATION).map { it.traitEffectKey }
    private val additiveEffectKeys = TraitEffectKeyCard.entries.map { it.traitEffectKey }.filter { it !in probabilityEffectKeys }

    @Test
    fun `Lv10の単独ビットによる適応効果の実値と表示が100パーセントになるのだ`() {
        val power = 0.1 * getTraitPower(1 shl 9)
        environmentEffectKeys.forEach { effectKey ->
            val value = effectKey.getValue(power)
            assertEquals(1.0, value)
            assertEquals("100%", effectKey.renderValue(value).string)
        }
    }

    @Test
    fun `環境効果は低パワーで0を下回らないのだ`() {
        environmentEffectKeys.forEach { effectKey ->
            listOf(0.0, 0.03, 0.05, 0.1).forEach { power ->
                assertEquals(0.0, effectKey.getValue(power))
            }
        }
    }

    @Test
    fun `パワーの増加に対する環境効果の増分が逓減するのだ`() {
        environmentEffectKeys.forEach { effectKey ->
            var previousValue = effectKey.getValue(0.1)
            var previousIncrement = Double.POSITIVE_INFINITY
            assertEquals(0.0, previousValue)
            (2..496).forEach { traitPower ->
                val value = effectKey.getValue(0.1 * traitPower)
                val increment = value - previousValue
                assertTrue(increment > 0.0)
                assertTrue(increment < previousIncrement)
                previousValue = value
                previousIncrement = increment
            }
        }
    }

    @Test
    fun `確率以外の効果は実効値を加算して表示するのだ`() {
        additiveEffectKeys.forEach { effectKey ->
            val effects = MutableTraitEffects()
            val firstEffects = MutableTraitEffects()
            firstEffects[effectKey] = 0.2
            val secondEffects = MutableTraitEffects()
            secondEffects[effectKey] = 0.5

            effects += firstEffects
            assertEquals(0.2, effects[effectKey], 1.0E-12)
            effects += secondEffects
            assertEquals(0.7, effects[effectKey], 1.0E-12)
            assertEquals("70%", effectKey.renderValue(effects[effectKey]).string)
        }
    }

    @Test
    fun `確率効果は50パーセント同士で75パーセントになり100パーセントを超えないのだ`() {
        probabilityEffectKeys.forEach { effectKey ->
            val effects = MutableTraitEffects()
            val otherEffects = MutableTraitEffects()
            otherEffects[effectKey] = 0.5

            effects += otherEffects
            assertEquals(0.5, effects[effectKey], 1.0E-12)
            effects += otherEffects
            assertEquals(0.75, effects[effectKey], 1.0E-12)
            assertEquals("75%", effectKey.renderValue(effects[effectKey]).string)

            otherEffects[effectKey] = 1.0
            effects += otherEffects
            assertEquals(1.0, effects[effectKey])
            otherEffects[effectKey] = 0.5
            effects += otherEffects
            assertEquals(1.0, effects[effectKey])
        }
    }

    @Test
    fun `適応の合計パワー100で実値と表示が200パーセントになるのだ`() {
        environmentEffectKeys.forEach { effectKey ->
            val value = effectKey.getValue(0.1 * 100)
            assertEquals(2.0, value, 1.0E-12)
            assertEquals("200%", effectKey.renderValue(value).string)
        }
    }

}
