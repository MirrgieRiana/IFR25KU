package miragefairy2024.mod.fairy

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.materials.APPEARANCE_RATE_BONUS_TRANSLATION
import miragefairy2024.util.Chance
import miragefairy2024.util.CondensedItem
import miragefairy2024.util.EMPTY_ITEM_STACK
import miragefairy2024.util.Translation
import miragefairy2024.util.blue
import miragefairy2024.util.compressWeight
import miragefairy2024.util.enJa
import miragefairy2024.util.filled
import miragefairy2024.util.get
import miragefairy2024.util.getOrCreate
import miragefairy2024.util.hasSameItemAndComponents
import miragefairy2024.util.invoke
import miragefairy2024.util.isIn
import miragefairy2024.util.mutate
import miragefairy2024.util.obtain
import miragefairy2024.util.plus
import miragefairy2024.util.randomInt
import miragefairy2024.util.register
import miragefairy2024.util.set
import miragefairy2024.util.size
import miragefairy2024.util.text
import miragefairy2024.util.weightedRandom
import miragefairy2024.util.yellow
import mirrg.kotlin.hydrogen.Single
import mirrg.kotlin.hydrogen.cmp
import mirrg.kotlin.hydrogen.formatAs
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.util.RandomSource
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.item.UseAnim
import net.minecraft.world.level.Level
import java.math.BigInteger
import java.math.RoundingMode
import kotlin.math.pow

private val identifier = MirageFairy2024.identifier("mirage_flour")
val MIRAGE_FLOUR_DESCRIPTION_USE_TRANSLATION = Translation({ "item.${identifier.toLanguageKey()}.description.use" }, "Use and hold to summon fairies", "使用時、長押しで妖精を連続召喚")
val MIRAGE_FLOUR_DESCRIPTION_SNEAKING_USE_TRANSLATION = Translation({ "item.${identifier.toLanguageKey()}.description.sneaking_use" }, "Use while sneaking to show loot table", "スニーク中に使用時、提供割合を表示")

context(ModContext)
fun initRandomFairySummoning() {
    MIRAGE_FLOUR_DESCRIPTION_USE_TRANSLATION.enJa()
    MIRAGE_FLOUR_DESCRIPTION_SNEAKING_USE_TRANSLATION.enJa()
    FAIRY_HISTORY_CONTAINER_ATTACHMENT_TYPE.register()
}

class RandomFairySummoningItem(val appearanceRateBonus: Double, settings: Properties) : Item(settings) {
    override fun appendHoverText(stack: ItemStack, context: TooltipContext, tooltipComponents: MutableList<Component>, tooltipFlag: TooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag)
        tooltipComponents += text { (APPEARANCE_RATE_BONUS_TRANSLATION() + ": x"() + (appearanceRateBonus formatAs "%.3f").replace("""\.?0+$""".toRegex(), "")()).blue }
        tooltipComponents += text { MIRAGE_FLOUR_DESCRIPTION_USE_TRANSLATION().yellow }
        tooltipComponents += text { MIRAGE_FLOUR_DESCRIPTION_SNEAKING_USE_TRANSLATION().yellow }
    }

    override fun getUseAnimation(stack: ItemStack) = UseAnim.BOW
    override fun getUseDuration(stack: ItemStack, entity: LivingEntity) = 72000 // 1時間

    override fun use(world: Level, user: Player, hand: InteractionHand): InteractionResultHolder<ItemStack> {
        val itemStack = user.getItemInHand(hand)
        if (!user.isShiftKeyDown) {

            // 使用開始
            user.startUsingItem(hand)

            return InteractionResultHolder.consume(itemStack)
        } else {
            if (world.isClientSide) return InteractionResultHolder.success(itemStack)

            val motifSet: Set<Motif> = getCommonMotifSet(user) + user.fairyDreamContainer.getOrCreate().entries
            val chanceTable = motifSet.toChanceTable(appearanceRateBonus)
                .compressWeight()
                .map { CondensedMotifChance(it.item.item.createFairyItemStack(), it) }
                .sortedWith(CondensedMotifChanceComparator.reversed())

            user.openMenu(object : ExtendedScreenHandlerFactory<List<CondensedMotifChance>> {
                override fun createMenu(syncId: Int, playerInventory: Inventory, player: Player) = MotifTableScreenHandler(syncId, chanceTable)
                override fun getDisplayName() = itemStack.hoverName
                override fun getScreenOpeningData(player: ServerPlayer) = chanceTable
            })
            return InteractionResultHolder.consume(itemStack)
        }
    }

    override fun onUseTick(world: Level, user: LivingEntity, stack: ItemStack, remainingUseTicks: Int) {
        if (world.isClientSide) return
        if (user !is ServerPlayer) return

        run {
            var t = 72000 - remainingUseTicks

            if (t < 40) {
                return@run
            }
            t -= 40

            if (t < 8 * 4) {
                if (t % 8 == 0) craft(user, stack)
                return@run
            }
            t -= 8 * 4

            if (t < 4 * 8) {
                if (t % 4 == 0) craft(user, stack)
                return@run
            }
            t -= 4 * 8

            if (t < 2 * 16) {
                if (t % 2 == 0) craft(user, stack)
                return@run
            }
            t -= 2 * 16

            craft(user, stack)
        }

        if (stack.isEmpty) user.stopUsingItem()

    }

    private fun craft(player: ServerPlayer, itemStack: ItemStack) {
        val world = player.level()

        // 消費
        if (!player.isCreative) {
            if (itemStack.count != 1) {
                // 最後の1個でない場合

                // 普通に消費
                itemStack.shrink(1)

            } else {
                // 最後の1個の場合

                // リロードが可能ならリロードする
                val isReloaded = run {
                    (0 until player.inventory.size).forEach { index ->
                        val searchingItemStack = player.inventory[index]
                        if (searchingItemStack !== itemStack) { // 同一のアイテムスタックでなく、
                            if (searchingItemStack hasSameItemAndComponents itemStack) { // 両者が同一種類のアイテムスタックならば、
                                val count = searchingItemStack.count
                                player.inventory[index] = EMPTY_ITEM_STACK // そのアイテムスタックを消して
                                itemStack.count = count // 手に持っているアイテムスタックに移動する
                                // stack.count == 1なので、このときアイテムが1個消費される
                                return@run true
                            }
                        }
                    }
                    false
                }

                // リロードできなかった場合、最後の1個を減らす
                if (!isReloaded) itemStack.shrink(1)

            }
        }

        // モチーフの判定
        val motifSet: Set<Motif> = getCommonMotifSet(player) + player.fairyDreamContainer.getOrCreate().entries

        // 抽選
        val result = getRandomFairy(world.random, motifSet, appearanceRateBonus) ?: return

        // 入手
        player.obtain(result.motif.createFairyItemStack(condensation = result.condensation, count = result.count))

        // 妖精召喚履歴に追加
        player.fairyHistoryContainer.mutate { it[result.motif] += result.condensation * result.count.toBigInteger() }

        // エフェクト
        world.playSound(null, player.x, player.y, player.z, SoundEvents.DEEPSLATE_BREAK, SoundSource.PLAYERS, 1.0F, 1.0F)

    }
}

class RandomFairyResult(val motif: Motif, val condensation: BigInteger, val count: Int)

fun getRandomFairy(random: RandomSource, motifSet: Set<Motif>, appearanceRateBonus: Double): RandomFairyResult? {

    // 提供割合の生成
    val chanceTable = motifSet.toChanceTable(appearanceRateBonus)
        .compressWeight()
        .map { Chance(it.weight, Single(CondensedMotifChance(it.item.item.createFairyItemStack(), it))) }
        .filled { Single(null) }

    // ガチャ
    val condensedMotif = chanceTable.weightedRandom(random)?.first ?: return null

    // actualCondensation は condensation を超えない最大の3の整数乗
    // condensation = 11.46 の場合、 actualCondensation = 9
    val actualCondensation = getNiceCondensation(condensedMotif.item.item.count).second

    // 上の場合、 count ≒ 1.27
    val count = condensedMotif.item.item.count / actualCondensation.toDouble()
    val actualCount = random.randomInt(count)

    return RandomFairyResult(
        motif = condensedMotif.item.item.item,
        condensation = actualCondensation,
        count = actualCount,
    )
}

fun getCommonMotifSet(player: Player): Set<Motif> {
    val biome = player.level().getBiome(player.blockPosition())
    return COMMON_MOTIF_RECIPES.filter {
        when (it) {
            is AlwaysCommonMotifRecipe -> true
            is BiomeCommonMotifRecipe -> biome isIn it.biome
            is BiomeTagCommonMotifRecipe -> biome isIn it.biomeTag
        }
    }.map { it.motif }.toSet()
}

fun Iterable<Motif>.toChanceTable(amplifier: Double = 1.0) = this.map { Chance((1.0 / 3.0).pow(it.rare - 1) * amplifier, it) } // 通常花粉・レア度1で100%になる

class CondensedMotifChance(val showingItemStack: ItemStack, val item: Chance<CondensedItem<Motif>>)

object CondensedMotifChanceComparator : Comparator<CondensedMotifChance> {
    override fun compare(o1: CondensedMotifChance, o2: CondensedMotifChance): Int {
        (o1.item.weight cmp o2.item.weight).let { if (it != 0) return it }
        (o1.item.item.count cmp o2.item.item.count).let { if (it != 0) return it }
        (o1.item.item.item.getIdentifier()!! cmp o2.item.item.item.getIdentifier()!!).let { if (it != 0) return it }
        return 0
    }
}

fun getNiceCondensation(value: Double) = getNiceCondensation(value.toBigDecimal().setScale(0, RoundingMode.FLOOR).toBigIntegerExact())

/** (power, niceCondensation) */
fun getNiceCondensation(value: BigInteger): Pair<Int, BigInteger> {
    if (value < BigInteger.ONE) return Pair(0, BigInteger.ONE)

    var power = 0
    var t = BigInteger.ONE
    while (true) {
        val nextT = t * 3.toBigInteger()
        if (nextT > value) return Pair(power, t)
        power++
        t = nextT
    }
}
