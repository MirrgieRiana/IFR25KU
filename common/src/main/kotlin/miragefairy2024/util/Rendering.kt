package miragefairy2024.util

import miragefairy2024.BlockColorProvider
import miragefairy2024.ItemColorProvider
import miragefairy2024.ModContext
import miragefairy2024.ModEvents
import miragefairy2024.RenderingProxy
import miragefairy2024.RenderingProxyBlockEntity
import miragefairy2024.clientProxy
import mirrg.kotlin.helium.castOrThrow
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType

context(ModContext)
fun (() -> Block).registerCutoutRenderLayer() = ModEvents.onClientInit {
    clientProxy!!.registerCutoutRenderLayer(this)
}

context(ModContext)
fun (() -> Block).registerTranslucentRenderLayer() = ModEvents.onClientInit {
    clientProxy!!.registerTranslucentRenderLayer(this)
}

context(ModContext)
fun (() -> Block).registerColorProvider(provider: BlockColorProvider) = ModEvents.onClientInit {
    clientProxy!!.registerBlockColorProvider(this, provider)
}

context(ModContext)
fun (() -> Block).registerFoliageColorProvider() = this.registerColorProvider { blockState, world, blockPos, tintIndex ->
    clientProxy!!.getFoliageBlockColorProvider().invoke(blockState, world, blockPos, tintIndex)
}

context(ModContext)
fun (() -> Item).registerColorProvider(provider: ItemColorProvider) = ModEvents.onClientInit {
    clientProxy!!.registerItemColorProvider(this(), provider)
}

context(ModContext)
fun (() -> BlockItem).registerRedirectColorProvider() = this.registerColorProvider { itemStack, tintIndex ->
    val block = itemStack.item.castOrThrow<BlockItem>().block
    clientProxy!!.getBlockColorProvider(block)!!.invoke(block.defaultBlockState(), null, null, tintIndex)
}


fun RenderingProxy.renderItemStack(itemStack: ItemStack, dotX: Double, dotY: Double, dotZ: Double, scale: Float = 1.0F, rotate: Float = 0.0F) {
    this.stack {
        this.translate(dotX / 16.0, dotY / 16.0, dotZ / 16.0)
        this.scale(scale, scale, scale)
        this.rotateY(rotate)
        this.renderItemStack(itemStack)
    }
}

context(ModContext)
fun <T> (() -> BlockEntityType<T>).registerRenderingProxyBlockEntityRendererFactory() where T : BlockEntity, T : RenderingProxyBlockEntity = ModEvents.onClientInit {
    clientProxy!!.registerRenderingProxyBlockEntityRendererFactory(this())
}
