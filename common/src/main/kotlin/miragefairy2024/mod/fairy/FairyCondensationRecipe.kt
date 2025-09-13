package miragefairy2024.mod.fairy

import miragefairy2024.ModContext
import miragefairy2024.util.EMPTY_ITEM_STACK
import miragefairy2024.util.SpecialRecipeResult
import miragefairy2024.util.isNotEmpty
import miragefairy2024.util.isNotIn
import miragefairy2024.util.registerSpecialRecipe
import net.minecraft.core.NonNullList
import net.minecraft.world.item.ItemStack
import java.math.BigInteger

context(ModContext)
fun initFairyCondensationRecipe() {
    registerSpecialRecipe("fairy_condensation", minSlots = 2) { inventory ->
        val itemStacks = inventory.items()

        // 空欄が入っていても無視
        val notEmptyItemStacks = itemStacks.filter { it.isNotEmpty }

        // 余計なアイテムが入っていたら失敗
        if (notEmptyItemStacks.any { it isNotIn FairyCard.item() }) return@registerSpecialRecipe null

        // 2個以上無ければ失敗
        if (notEmptyItemStacks.size < 2) return@registerSpecialRecipe null

        // 壊れたアイテムだと失敗
        val motif = notEmptyItemStacks.first().getFairyMotif() ?: return@registerSpecialRecipe null

        // すべてのモチーフが等しくなければ失敗
        (1 until notEmptyItemStacks.size).forEach { i ->
            if (notEmptyItemStacks[i].getFairyMotif() != motif) return@registerSpecialRecipe null
        }

        val condensation = notEmptyItemStacks.sumOf { it.getFairyCondensation() }

        object : SpecialRecipeResult {
            override fun craft() = motif.createFairyItemStack(condensation = condensation)
        }
    }
    registerSpecialRecipe("fairy_decondensation", minSlots = 1) { inventory ->
        val itemStacks = inventory.items()

        // 空欄無視、インデックス付与
        val entries = itemStacks.mapIndexedNotNull { i, it ->
            if (it.isEmpty) return@mapIndexedNotNull null
            Pair(i, it)
        }

        // アイテムが丁度1個のみのときに反応
        val (index, itemStack) = entries.singleOrNull() ?: return@registerSpecialRecipe null

        // そのアイテムは妖精でなければならない
        if (itemStack isNotIn FairyCard.item()) return@registerSpecialRecipe null

        // 左上の場合1/10、それ以外の場合、その位置で割る
        val division = if (index == 0) 10 else index + 1

        // 壊れた妖精アイテムは受け付けない
        val motif = itemStack.getFairyMotif() ?: return@registerSpecialRecipe null

        // 入力アイテムの凝縮数は、割る数以上でなければならない
        val condensation = itemStack.getFairyCondensation()
        if (condensation < division.toBigInteger()) return@registerSpecialRecipe null

        val remainingCondensation = condensation % division.toBigInteger()
        val dividedCondensation = condensation / division.toBigInteger()

        object : SpecialRecipeResult {
            override fun craft() = motif.createFairyItemStack(condensation = dividedCondensation, count = division)
            override fun getRemainder(): NonNullList<ItemStack>? {
                return if (remainingCondensation > BigInteger.ZERO) {
                    val list = NonNullList.withSize(inventory.size(), EMPTY_ITEM_STACK)
                    list[index] = motif.createFairyItemStack(condensation = remainingCondensation)
                    list
                } else {
                    null
                }
            }
        }
    }
}
