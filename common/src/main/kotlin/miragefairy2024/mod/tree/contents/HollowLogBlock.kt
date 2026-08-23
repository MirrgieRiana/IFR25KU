package miragefairy2024.mod.tree.contents

import com.mojang.serialization.MapCodec
import miragefairy2024.lib.SimpleHorizontalFacingBlock

class HollowLogBlock(settings: Properties) : SimpleHorizontalFacingBlock(settings) {
    companion object {
        val CODEC: MapCodec<HollowLogBlock> = simpleCodec(::HollowLogBlock)
    }

    override fun codec() = CODEC
}
