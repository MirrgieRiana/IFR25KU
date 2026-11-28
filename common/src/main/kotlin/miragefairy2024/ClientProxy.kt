package miragefairy2024

import miragefairy2024.mod.common.KeyMappingType
import miragefairy2024.mod.recipeviewer.vanilla.ScreenEventSource
import miragefairy2024.mod.recipeviewer.vanilla.ViewProviderMenu
import miragefairy2024.mod.recipeviewer.view.View
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.FormattedText
import net.minecraft.resources.ResourceLocation as Identifier // MIGRATION2612 ReplaceImport net.minecraft.resources.Identifier
import net.minecraft.util.FormattedCharSequence
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.MenuType
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState

var clientProxy: ClientProxy<*>? = null


interface ClientProxy<S : Any> {
    fun registerItemTooltipCallback(block: (stack: ItemStack, context: Item.TooltipContext, lines: MutableList<Component>) -> Unit)
    fun getItemTooltip(itemStack: ItemStack): List<Component>
    fun registerCutoutRenderLayer(block: () -> Block)
    fun registerTranslucentRenderLayer(block: () -> Block)
    fun getClientPlayer(): Player?
    fun getBlockColorProvider(block: Block): BlockColorProvider?
    fun registerBlockColorProvider(block: () -> Block, provider: BlockColorProvider)
    fun getGrassBlockColorProvider(): BlockColorProvider
    fun getFoliageBlockColorProvider(): BlockColorProvider
    fun getItemColorProvider(item: Item): ItemColorProvider?
    fun registerItemColorProvider(item: Item, provider: ItemColorProvider)
    fun <T> registerRenderingProxyBlockEntityRendererFactory(blockEntityType: BlockEntityType<T>) where T : BlockEntity, T : RenderingProxyBlockEntity
    fun <T> registerViewScreen(menuType: MenuType<T>) where T : AbstractContainerMenu, T : ViewProviderMenu
    fun getCurrentScreen(): S?
    fun setCurrentScreen(screen: S?)
    fun createMessageViewScreen(parent: S?, definition: MessageViewScreenDefinition): S

    /**
     * [rootView] をトーストに表示するのだ～🌱
     * 表示終了時に [miragefairy2024.mod.recipeviewer.view.ViewController.dispose] が呼ばれる保証はないのだ～🌱
     * トーストの寿命を超えて残る購読がある場合は、終了通知に依存せず購読を解除できる View を渡すのだ～🌱
     */
    fun addToast(rootView: View)

    fun ellipsizeText(text: FormattedText, maxWidth: Int, ellipsis: FormattedText): FormattedCharSequence
}

fun interface BlockColorProvider {
    operator fun invoke(blockState: BlockState, level: BlockGetter?, blockPos: BlockPos?, tintIndex: Int): Int
}

fun interface ItemColorProvider {
    operator fun invoke(itemStack: ItemStack, tintIndex: Int): Int
}

interface RenderingProxy {
    fun stack(block: () -> Unit)

    fun translate(x: Double, y: Double, z: Double)
    fun scale(x: Float, y: Float, z: Float)
    fun rotateX(rad: Float)
    fun rotateY(rad: Float)
    fun rotateZ(rad: Float)

    fun renderItemStack(itemStack: ItemStack)
    fun renderFixedItemStack(itemStack: ItemStack)
    fun renderCutoutBlock(identifier: Identifier, variant: String?, red: Float, green: Float, blue: Float, light: Int, overlay: Int)
}

interface RenderingProxyBlockEntity {
    fun render(renderingProxy: RenderingProxy, tickDelta: Float, light: Int, overlay: Int) = Unit
}

class MessageViewScreenDefinition(val title: Component, val rootViewFactory: (ScreenEventSource) -> View) {

    /** false にすると、設定画面の背景になるのだ～🌱 */
    var transparentBackground = true

    var closeOnInventoryKey = true

    var additionalCloseKey: KeyMappingType? = null

    /** 実際には GUI 枠の中でも、クリックが消化されなければ閉じられるのだ～🌱 */
    var closeOnOutsideClick = true

}
