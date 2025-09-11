package miragefairy2024.mod.magicplant

import mirrg.kotlin.helium.max
import net.minecraft.util.RandomSource

const val MAX_TRAIT_COUNT = 15

fun crossTraitStacks(a: Map<Trait, Int>, b: Map<Trait, Int>, random: RandomSource): Map<Trait, Int> {

    class Entry(val trait: Trait, val level: Int, val isDecided: Boolean)

    // 両親から、一旦枠数制限を無視して交配する
    val traits = a.keys + b.keys
    val entries = traits.map { trait ->
        val aLevel = a[trait] ?: 0
        val bLevel = b[trait] ?: 0
        val bits = (aLevel max bLevel).toString(2).length

        var level = 0
        var isDecided = false
        (0 until bits).forEach { bit ->
            val mask = 1 shl bit
            val aPossession = aLevel and mask != 0
            val bPossession = bLevel and mask != 0
            when {
                aPossession && bPossession -> { // 両親所持ビットは必ず継承しつつ、特性も確定特性にする
                    level = level or mask
                    isDecided = true
                }

                !aPossession && !bPossession -> Unit // 両親不所持ビットは継承しない

                else -> { // 片親所持ビットは50%の確率で継承
                    if (random.nextDouble() < 0.5) level = level or mask
                }
            }
        }

        Entry(trait, level, isDecided)
    }

    // 交配の結果全部のビットが消えた特性はリストから外す
    val entries2 = entries.filter { it.level != 0 }

    // 枠数を超えていて不確定特性を持っている限り、不確定特性をランダムに消していく
    val decidedTraitStackList = entries2.filter { it.isDecided }.map { Pair(it.trait, it.level) }
    val undecidedTraitStackList = entries2.filter { !it.isDecided }.map { Pair(it.trait, it.level) }.toMutableList()
    while (decidedTraitStackList.size + undecidedTraitStackList.size > MAX_TRAIT_COUNT) {
        if (undecidedTraitStackList.isEmpty()) break
        undecidedTraitStackList.removeAt(random.nextInt(undecidedTraitStackList.size))
    }

    return decidedTraitStackList.toMap() + undecidedTraitStackList.toMap()
}
