package miragefairy2024.mod.wood

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.wood.cards.WoodHorizontalFacingLogBlock
import miragefairy2024.util.Registration
import miragefairy2024.util.register
import net.minecraft.core.registries.BuiltInRegistries

context(ModContext)
fun initWoodModule() {

    Registration(BuiltInRegistries.BLOCK_TYPE, MirageFairy2024.identifier("wood_horizontal_facing_log")) { WoodHorizontalFacingLogBlock.CODEC }.register()

}
