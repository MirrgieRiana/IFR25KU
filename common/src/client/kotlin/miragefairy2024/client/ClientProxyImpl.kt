package miragefairy2024.client

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis
import dev.architectury.registry.client.rendering.RenderTypeRegistry
import miragefairy2024.BlockColorProvider
import miragefairy2024.ClientProxy
import miragefairy2024.ItemColorProvider
import miragefairy2024.RenderingProxy
import miragefairy2024.RenderingProxyBlockEntity
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.BiomeColors
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers
import net.minecraft.client.resources.model.ModelResourceLocation
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemDisplayContext
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.BlockAndTintGetter
import net.minecraft.world.level.FoliageColor
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType

class ClientProxyImpl : ClientProxy {
    override fun registerItemTooltipCallback(block: (stack: ItemStack, context: Item.TooltipContext, lines: MutableList<Component>) -> Unit) {
        ItemTooltipCallback.EVENT.register { stack, context, _, lines ->
            block(stack, context, lines)
        }
    }

    override fun registerCutoutRenderLayer(block: () -> Block) {
        RenderTypeRegistry.register(RenderType.cutout(), block())
    }

    override fun registerTranslucentRenderLayer(block: () -> Block) {
        RenderTypeRegistry.register(RenderType.translucent(), block())
    }

    override fun getClientPlayer(): Player? = Minecraft.getInstance().player

    override fun getBlockColorProvider(block: Block): BlockColorProvider {
        return BlockColorProvider { blockState, world, blockPos, tintIndex ->
            Minecraft.getInstance().blockColors.getColor(blockState, world as BlockAndTintGetter?, blockPos, tintIndex)
        }
    }

    override fun registerBlockColorProvider(block: () -> Block, provider: BlockColorProvider) {
        ColorProviderRegistry.BLOCK.register({ blockState, world, blockPos, tintIndex ->
            provider(blockState, world, blockPos, tintIndex)
        }, block())
    }

    override fun getFoliageBlockColorProvider() = BlockColorProvider { _, world, blockPos, _ ->
        if (world == null || blockPos == null) FoliageColor.getDefaultColor() else BiomeColors.getAverageFoliageColor(world as BlockAndTintGetter, blockPos)
    }

    override fun getItemColorProvider(item: Item): ItemColorProvider? {
        val provider = ColorProviderRegistry.ITEM.get(item) ?: return null
        return ItemColorProvider { itemStack, tintIndex ->
            provider.getColor(itemStack, tintIndex)
        }
    }

    override fun registerItemColorProvider(item: Item, provider: ItemColorProvider) {
        ColorProviderRegistry.ITEM.register({ itemStack, tintIndex ->
            provider(itemStack, tintIndex)
        }, item)
    }

    override fun <T> registerRenderingProxyBlockEntityRendererFactory(blockEntityType: BlockEntityType<T>) where T : BlockEntity, T : RenderingProxyBlockEntity {
        BlockEntityRenderers.register(blockEntityType, ::RenderingProxyBlockEntityRenderer)
    }
}

class RenderingProxyBlockEntityRenderer<T>(
    @Suppress("UNUSED_PARAMETER") ctx: BlockEntityRendererProvider.Context,
) : BlockEntityRenderer<T> where T : BlockEntity, T : RenderingProxyBlockEntity {
    override fun render(blockEntity: T, tickDelta: Float, matrices: PoseStack, vertexConsumers: MultiBufferSource, light: Int, overlay: Int) {
        val renderingProxy = object : RenderingProxy {
            override fun stack(block: () -> Unit) {
                matrices.pushPose()
                try {
                    block()
                } finally {
                    matrices.popPose()
                }
            }

            override fun translate(x: Double, y: Double, z: Double) = matrices.translate(x, y, z)
            override fun scale(x: Float, y: Float, z: Float) = matrices.scale(x, y, z)
            override fun rotateX(rad: Float) = matrices.mulPose(Axis.XP.rotation(rad))
            override fun rotateY(rad: Float) = matrices.mulPose(Axis.YP.rotation(rad))
            override fun rotateZ(rad: Float) = matrices.mulPose(Axis.ZP.rotation(rad))

            override fun renderItemStack(itemStack: ItemStack) {
                Minecraft.getInstance().itemRenderer.renderStatic(itemStack, ItemDisplayContext.GROUND, light, overlay, matrices, vertexConsumers, blockEntity.level, 0)
            }

            override fun renderFixedItemStack(itemStack: ItemStack) {
                Minecraft.getInstance().itemRenderer.renderStatic(itemStack, ItemDisplayContext.FIXED, light, overlay, matrices, vertexConsumers, blockEntity.level, 0)
            }

            override fun renderCutoutBlock(identifier: ResourceLocation, variant: String?, red: Float, green: Float, blue: Float, light: Int, overlay: Int) {
                val vertexConsumer = vertexConsumers.getBuffer(RenderType.cutout())
                val bakedModel = if (variant != null) {
                    Minecraft.getInstance().modelManager.getModel(ModelResourceLocation(identifier, variant))
                } else {
                    Minecraft.getInstance().modelManager.getModel(identifier)
                }
                Minecraft.getInstance().blockRenderer.modelRenderer.renderModel(matrices.last(), vertexConsumer, null, bakedModel, red, green, blue, light, overlay)
            }
        }
        blockEntity.render(renderingProxy, tickDelta, light, overlay)
    }
}
