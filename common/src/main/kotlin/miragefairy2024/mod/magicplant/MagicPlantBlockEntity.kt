package miragefairy2024.mod.magicplant

import miragefairy2024.util.Chance
import miragefairy2024.util.compressWeight
import miragefairy2024.util.filled
import miragefairy2024.util.weightedRandom
import mirrg.kotlin.helium.Single
import net.minecraft.core.BlockPos
import net.minecraft.core.HolderLookup
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket
import net.minecraft.util.RandomSource
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import kotlin.math.pow
import kotlin.math.sqrt

class MagicPlantBlockEntity(private val card: MagicPlantCard<*>, pos: BlockPos, state: BlockState) : BlockEntity(card.blockEntityType(), pos, state) {

    private var traitStacks: TraitStacks? = null

    fun getTraitStacks() = traitStacks

    fun setTraitStacks(traitStacks: TraitStacks?) {
        this.traitStacks = traitStacks
        markUpdated()
    }


    private var isRare = false

    fun isRare() = isRare

    fun setRare(isRare: Boolean) {
        this.isRare = isRare
        markUpdated()
    }


    private var isNatural = false

    fun isNatural() = isNatural

    fun setNatural(isNatural: Boolean) {
        this.isNatural = isNatural
        markUpdated()
    }


    private fun markUpdated() {
        setChanged()
        level?.sendBlockUpdated(blockPos, blockState, blockState, 3)
    }


    fun getRandomTraitChances() = card.randomTraitChances


    override fun setLevel(world: Level) {
        super.setLevel(world)
        if (traitStacks == null) {
            val (bits, rare) = applyMutation(card.defaultTraitBits, card.randomTraitChances, world.random)
            setTraitStacks(TraitStacks.of(bits))
            setRare(rare)
            setNatural(true)
        }
    }

    public override fun saveAdditional(nbt: CompoundTag, registries: HolderLookup.Provider) {
        super.saveAdditional(nbt, registries)
        traitStacks?.let { nbt.put("TraitStacks", it.toNbt()) }
        if (isRare) nbt.putBoolean("Rare", true)
        if (isNatural) nbt.putBoolean("Natural", true)
    }

    override fun loadAdditional(nbt: CompoundTag, registries: HolderLookup.Provider) {
        super.loadAdditional(nbt, registries)
        traitStacks = TraitStacks.readFromNbt(nbt)
        isRare = nbt.getBoolean("Rare")
        isNatural = nbt.getBoolean("Natural")
    }

    override fun getUpdateTag(registries: HolderLookup.Provider): CompoundTag {
        val nbt = super.getUpdateTag(registries)
        traitStacks?.let { nbt.put("TraitStacks", it.toNbt()) }
        if (isRare) nbt.putBoolean("Rare", true)
        if (isNatural) nbt.putBoolean("Natural", true)
        return nbt
    }

    override fun getUpdatePacket(): Packet<ClientGamePacketListener>? = ClientboundBlockEntityDataPacket.create(this)

}

fun BlockGetter.getMagicPlantBlockEntity(blockPos: BlockPos) = this.getBlockEntity(blockPos) as? MagicPlantBlockEntity

private val MAX_RANDOM_BIT = 15

fun applyMutation(bits: Map<Trait, Int>, chances: Map<Trait, Double>, random: RandomSource): Pair<Map<Trait, Int>, Boolean> {

    // 特性数上限を加味した抽選リスト
    val defaultTraits = bits.keys
    val actualRandomTraitChances = if (defaultTraits.size >= MAX_TRAIT_COUNT) {
        chances.entries.filter { (trait, _) -> trait in defaultTraits }
    } else {
        chances.entries
    }.map { Chance(it.value, it.key) }

    // 確率があふれていた場合に凝縮率に還元し、ハズレを加味した特性の提供割合
    val actualCondensedTraitChances = actualRandomTraitChances
        .compressWeight()
        .map { Chance(it.weight, Single(it.item)) }
        .filled { Single(null) }

    // 抽選
    val selectedCondensedTraitResult = actualCondensedTraitChances.weightedRandom(random)!!
    val selectedCondensedTrait = selectedCondensedTraitResult.first
    if (selectedCondensedTrait == null) return Pair(bits, false)

    // 凝縮率を加味したビット番号の抽選リスト
    val bitNumberChances = (1..MAX_RANDOM_BIT).map { bitNumber ->
        val a = 1.0 - 1.0 / sqrt(10.0) // 0.6837...
        val b = sqrt(0.1) // 0.3162...
        val c = (bitNumber - 1).toDouble() // 0 .. MAX_RANDOM_BIT - 1
        val weight = a * b.pow(c) * selectedCondensedTrait.count // 2左シフトごとに1/10、全部合わせて約1
        Chance(weight, bitNumber)
    }

    // 確率があふれていた場合に凝縮率に還元し、ハズレを加味したビット番号の提供割合
    val actualCondensedBitNumberChances = bitNumberChances
        .compressWeight()
        .map { Chance(it.weight, Single(it.item)) }
        .filled { Single(null) }

    // ビット番号の抽選
    val selectedCondensedBitNumberResult = actualCondensedBitNumberChances.weightedRandom(random)!!
    val selectedCondensedBitNumber = selectedCondensedBitNumberResult.first
    if (selectedCondensedBitNumber == null) return Pair(bits, false)

    // 抽選結果を加味した特性リストの生成
    // ビット番号の凝縮数があふれた分は、単に無視する
    val selectedTrait = selectedCondensedTrait.item
    val selectedBits = 1 shl (selectedCondensedBitNumber.item - 1)
    val actualTraitBits = bits.toMutableMap()
    val newBits = (actualTraitBits[selectedTrait] ?: 0) xor selectedBits
    if (newBits == 0) {
        actualTraitBits.remove(selectedTrait)
    } else {
        actualTraitBits[selectedTrait] = newBits
    }

    return Pair(actualTraitBits, true)
}
