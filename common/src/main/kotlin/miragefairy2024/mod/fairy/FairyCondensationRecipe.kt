package miragefairy2024.mod.fairy

import miragefairy2024.ModContext
import miragefairy2024.util.EMPTY_ITEM_STACK
import miragefairy2024.util.SpecialRecipeResult
import miragefairy2024.util.createItemStack
import miragefairy2024.util.isIn
import miragefairy2024.util.isNotEmpty
import miragefairy2024.util.isNotIn
import miragefairy2024.util.registerSpecialRecipe
import net.minecraft.core.NonNullList
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
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

        // すべてのスロットは、空欄か木の棒か妖精でなければならない
        val sticksIndices = mutableListOf<Int>()
        val fairyIndices = mutableListOf<Int>()
        itemStacks.forEachIndexed { index, it ->
            if (it.isEmpty) {
                return@forEachIndexed
            } else if (it isIn Items.STICK) {
                sticksIndices += index
                return@forEachIndexed
            } else if (it isIn FairyCard.item()) {
                fairyIndices += index
                return@forEachIndexed
            } else {
                return@registerSpecialRecipe null
            }
        }

        // 妖精は丁度1個でなければならない
        val fairyIndex = fairyIndices.singleOrNull() ?: return@registerSpecialRecipe null

        // 妖精の分割数は、左上の場合1/10、それ以外の場合、その位置で割る
        val division = if (fairyIndex == 0) 10 else fairyIndex + 1

        // モチーフ取得
        val motif = itemStacks[fairyIndex].getFairyMotif() ?: return@registerSpecialRecipe null // 壊れた妖精アイテムは受け付けない

        // 凝縮数取得
        val condensation = itemStacks[fairyIndex].getFairyCondensation()
        if (condensation < division.toBigInteger()) return@registerSpecialRecipe null // 入力アイテムの凝縮数は、割る数以上でなければならない

        // 分割後の凝縮数
        val dividedCondensation = condensation / division.toBigInteger()

        // 余りの凝縮数
        val remainingCondensation = condensation % division.toBigInteger()

        // 成立
        object : SpecialRecipeResult {
            override fun craft() = motif.createFairyItemStack(condensation = dividedCondensation, count = division)
            override fun getRemainder(): NonNullList<ItemStack>? {
                val list = NonNullList.withSize(inventory.size(), EMPTY_ITEM_STACK)

                // 棒を返す
                sticksIndices.forEach { index ->
                    list[index] = Items.STICK.createItemStack()
                }

                // 余りの凝縮数があれば妖精を返す
                if (remainingCondensation > BigInteger.ZERO) list[fairyIndex] = motif.createFairyItemStack(condensation = remainingCondensation)

                return list
            }
        }
    }
}
