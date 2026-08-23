package miragefairy2024.mod.tree.contents

import com.mojang.serialization.MapCodec
import miragefairy2024.lib.SimpleHorizontalFacingBlock

class HollowHaimeviskaLogBlock(settings: Properties) : SimpleHorizontalFacingBlock(settings) {
    companion object {
        val CODEC: MapCodec<HollowHaimeviskaLogBlock> = simpleCodec(::HollowHaimeviskaLogBlock)
    }

    override fun codec() = CODEC
}
