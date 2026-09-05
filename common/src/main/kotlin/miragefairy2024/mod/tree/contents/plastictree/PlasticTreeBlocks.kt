package miragefairy2024.mod.tree.contents.plastictree

import com.mojang.serialization.MapCodec
import miragefairy2024.mod.tree.TreeBlockCard
import miragefairy2024.mod.tree.contents.IncisableLogBlock

class PlasticTreeLogBlock(settings: Properties) : IncisableLogBlock(settings) {
    companion object {
        val CODEC: MapCodec<PlasticTreeLogBlock> = simpleCodec(::PlasticTreeLogBlock)
    }

    override fun codec() = CODEC

    override fun getIncisedLogBlock() = TreeBlockCard.INCISED_LOG.block() // 傷の付いたプラノキの原木がまだ無いから、ハイメヴィスカのものをプレースホルダーとして置いてあるのだ～🌱
}
