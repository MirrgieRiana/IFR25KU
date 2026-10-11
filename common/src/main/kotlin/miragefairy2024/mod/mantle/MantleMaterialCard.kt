package miragefairy2024.mod.mantle

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
import miragefairy2024.util.generator
import miragefairy2024.util.register
import miragefairy2024.util.registerChild
import miragefairy2024.util.registerGeneratedModelGeneration
import miragefairy2024.util.registerItemGroup
import miragefairy2024.util.toItemTag
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item

/** ブリッジマナイトのアイテムのタグなのだ～🌱 */
val BRIDGMANITES_ITEM_TAG = MirageFairy2024.identifier("bridgmanites").toItemTag()

/** ワズレアイトのアイテムのタグなのだ～🌱 */
val WADSLEYITES_ITEM_TAG = MirageFairy2024.identifier("wadsleyites").toItemTag()

/** リングウッダイトのアイテムのタグなのだ～🌱 */
val RINGWOODITES_ITEM_TAG = MirageFairy2024.identifier("ringwoodites").toItemTag()

enum class MantleMaterialCard(
    path: String,
    val enName: String,
    val jaName: String,
    val poemList: PoemList?,
    val tag: TagKey<Item>,
) {
    WADSLEYITE(
        "wadsleyite", "Wadsleyite", "ワズレアイト",
        null,
        WADSLEYITES_ITEM_TAG,
    ),
    RINGWOODITE(
        "ringwoodite", "Ringwoodite", "リングウッダイト",
        PoemList(6).poem(EnJa("TODO", "TODO")),
        RINGWOODITES_ITEM_TAG,
    ),
    ;

    val identifier = MirageFairy2024.identifier(path)

    val item = Registration(BuiltInRegistries.ITEM, identifier) { Item(Item.Properties()) }
}

context(ModContext)
fun initMantleMaterialCards() {

    BRIDGMANITES_ITEM_TAG.enJa(EnJa("Bridgmanites", "ブリッジマナイト"))
    WADSLEYITES_ITEM_TAG.enJa(EnJa("Wadsleyites", "ワズレアイト"))
    RINGWOODITES_ITEM_TAG.enJa(EnJa("Ringwoodites", "リングウッダイト"))

    BRIDGMANITES_ITEM_TAG.generator.registerChild(MantleBlockCard.BRIDGMANITE.item)
    MantleMaterialCard.entries.forEach { card ->

        card.item.register()

        card.item.registerItemGroup(mirageFairy2024ItemGroupCard.itemGroupKey)

        card.item.registerGeneratedModelGeneration()

        card.tag.generator.registerChild(card.item)

        card.item.enJa(EnJa(card.enName, card.jaName))
        if (card.poemList != null) {
            card.item.registerPoem(card.poemList)
            card.item.registerPoemGeneration(card.poemList)
        }

    }
}
