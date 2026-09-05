package miragefairy2024.mod.tree.contents.plastictree

import com.mojang.serialization.MapCodec
import miragefairy2024.mod.tree.TreeBlockCard
import miragefairy2024.mod.tree.contents.IncisedLogBlock

class IncisedPlasticTreeLogBlock(settings: Properties) : IncisedLogBlock(settings) {
    companion object {
        val CODEC: MapCodec<IncisedPlasticTreeLogBlock> = simpleCodec(::IncisedPlasticTreeLogBlock)
    }

    override fun codec() = CODEC

    override fun getDrippingLogBlock() = TreeBlockCard.DRIPPING_LOG.block() // 樹液が滴るプラノキの原木がまだ無いから、ハイメヴィスカのものをプレースホルダーとして置いてあるのだ～🌱
}
