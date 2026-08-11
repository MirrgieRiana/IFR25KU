package miragefairy2024.mod.plasticwood.cards

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.PoemList
import miragefairy2024.mod.common.mirageFairy2024ItemGroupCard
import miragefairy2024.mod.poem
import miragefairy2024.mod.registerPoem
import miragefairy2024.mod.registerPoemGeneration
import miragefairy2024.util.EnJa
import miragefairy2024.util.Registration
import miragefairy2024.util.enJa
import miragefairy2024.util.register
import miragefairy2024.util.registerFuel
import miragefairy2024.util.registerGeneratedModelGeneration
import miragefairy2024.util.registerItemGroup
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.item.Item

// プラノキの樹液アイテムカードなのだ
class PlasticTreeSapBlockCard {
    val identifier = MirageFairy2024.identifier("plastic_tree_sap")
    val poemList = PoemList(1).poem(EnJa("TODO", "TODO"))
    val item = Registration(BuiltInRegistries.ITEM, identifier) { Item(Item.Properties()) }

    context(ModContext)
    fun init() {

        // 登録
        item.register()

        // カテゴリ
        item.registerItemGroup(mirageFairy2024ItemGroupCard.itemGroupKey)

        // テキスト
        item.enJa(EnJa("Plastic Tree Sap", "プラノキの樹液"))
        item.registerPoem(poemList)
        item.registerPoemGeneration(poemList)

        // 燃料値（ハイメヴィスカの樹液と同等の 200 なのだ）
        item.registerFuel(200)

        // モデル
        item.registerGeneratedModelGeneration()

    }
}
