package miragefairy2024.mod.magicplant

import miragefairy2024.util.createItemStack
import net.minecraft.util.RandomSource
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack

/**
 * 魔法植物の収穫物の1カテゴリ分の抽選を司るのだ～🌱
 * 抽選の結果と、抽選に現れうる品目の一覧とが食い違わないように、両方をここで定めるのだ～🌱
 */
interface MagicPlantDropProvider {
    companion object {
        val EMPTY: MagicPlantDropProvider = object : MagicPlantDropProvider {
            override val items = listOf<() -> Item>()
            override fun draw(count: Int, random: RandomSource) = listOf<ItemStack>()
        }
    }

    /** [miragefairy2024.mod.magicplant.MagicPlantDropProvider.draw]が返しうるすべての品目なのだ～🌱 */
    val items: List<() -> Item>

    /** 生産量[count]から、実際に落ちるアイテムを決めるのだ～🌱 */
    fun draw(count: Int, random: RandomSource): List<ItemStack>
}

/** 生産量をそのまま個数として、単一の品目を落とす抽選器なのだ～🌱 */
class SingleItemMagicPlantDropProvider(private val item: () -> Item) : MagicPlantDropProvider {
    override val items = listOf(item)
    override fun draw(count: Int, random: RandomSource) = listOf(item().createItemStack(count))
}
