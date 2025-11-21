package miragefairy2024.mod.materials.contents

import com.mojang.serialization.MapCodec
import net.minecraft.util.StringRepresentable
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.EnumProperty

class MiragidianLampBlock(settings: Properties) : Block(settings) {
    companion object {
        val CODEC: MapCodec<MiragidianLampBlock> = simpleCodec(::MiragidianLampBlock)
        val PART: EnumProperty<Part> = EnumProperty.create("part", Part::class.java)
    }

    override fun codec() = CODEC

    enum class Part(val path: String) : StringRepresentable {
        HEAD("head"),
        POLE("pole"),
        FOOT("foot"),
        ;

        override fun getSerializedName() = path
    }

    init {
        registerDefaultState(defaultBlockState().setValue(PART, Part.FOOT))
    }

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        builder.add(PART)
    }

    // TODO getStateForPlacement

    // TODO 設置時、高さ3あるかチェック
    // TODO 設置時、高さ3個分の範囲に対してブロックを設置

    // TODO ランプで右クリックされた場合、伸長する

    // TODO ヘッドの上部は中心に2x2の平坦な部分がある

    // TODO クリック判定

    // TODO 経路探索を妨害する

    // TODO 光のパーティクル

    // TODO その他マルチブロックに必要なすべて

}
