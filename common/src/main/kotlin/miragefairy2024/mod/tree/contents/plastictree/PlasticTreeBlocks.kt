package miragefairy2024.mod.tree.contents.plastictree

import com.mojang.serialization.MapCodec
import miragefairy2024.mod.tree.TreeBlockCard
import miragefairy2024.mod.tree.contents.IncisableLogBlock
import miragefairy2024.mod.tree.contents.IncisedLogBlock

class PlasticTreeLogBlock(settings: Properties) : IncisableLogBlock(settings) {
    companion object {
        val CODEC: MapCodec<PlasticTreeLogBlock> = simpleCodec(::PlasticTreeLogBlock)
    }

    override fun codec() = CODEC

    override fun getIncisedLogBlock() = TreeBlockCard.INCISED_LOG.block() // TODO 傷の付いたプラノキの原木がまだ無いから、ハイメヴィスカのものをプレースホルダーとして置いてあるのだ～🌱
}

class IncisedPlasticTreeLogBlock(settings: Properties) : IncisedLogBlock(settings) {
    companion object {
        val CODEC: MapCodec<IncisedPlasticTreeLogBlock> = simpleCodec(::IncisedPlasticTreeLogBlock)
    }

    override fun codec() = CODEC

    override fun getDrippingLogBlock() = TreeBlockCard.DRIPPING_LOG.block() // TODO 樹液が滴るプラノキの原木がまだ無いから、ハイメヴィスカのものをプレースホルダーとして置いてあるのだ～🌱
}
