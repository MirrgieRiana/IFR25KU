package miragefairy2024.mod.fairybuilding

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.haimeviska.HAIMEVISKA_LOGS_BLOCK_TAG
import miragefairy2024.util.Translation
import miragefairy2024.util.enJa
import miragefairy2024.util.registerBlockTagGeneration
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.world.level.block.Block

enum class FairyBuildingModelCard(val identifier: ResourceLocation) {
    LANTERN(MirageFairy2024.identifier("block/fairy_building/lantern")),
    LANTERN_OFF(MirageFairy2024.identifier("block/fairy_building/lantern_off")),
}

val fairyBuildingCards: List<FairyBuildingCard<*, *, *>> = listOf(
    FairyHouseCard,
    FairyCollectorCard,
)

private val identifier = MirageFairy2024.identifier("fairy_building")
val FOLIA_TRANSLATION = Translation({ "gui.${identifier.toLanguageKey()}.folia" }, "Folia", "フォリア")
val SPECIFIED_FAIRY_SLOT_TRANSLATION = Translation({ "gui.${identifier.toLanguageKey()}.specified_fairy_slot" }, "Only %s Family", "%s系統のみ")

val FAIRY_BUILDING_BLOCK_TAG: TagKey<Block> = TagKey.create(Registries.BLOCK, MirageFairy2024.identifier("fairy_building"))

context(ModContext)
fun initFairyBuildingModule() {
    fairyBuildingCards.forEach { card ->
        card.init()
    }

    FOLIA_TRANSLATION.enJa()
    SPECIFIED_FAIRY_SLOT_TRANSLATION.enJa()

    FAIRY_BUILDING_BLOCK_TAG.registerBlockTagGeneration { HAIMEVISKA_LOGS_BLOCK_TAG }
}
