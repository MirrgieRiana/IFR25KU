package miragefairy2024.client.mod

import miragefairy2024.ModContext
import miragefairy2024.client.util.stack
import miragefairy2024.mixin.client.api.RenderItemHandler
import miragefairy2024.mod.fairy.FairyStatueBlockItem
import miragefairy2024.mod.fairy.createFairyItemStack
import miragefairy2024.mod.fairy.getFairyMotif
import net.minecraft.client.Minecraft
import net.minecraft.world.item.ItemDisplayContext

context(_: ModContext)
fun initFairyStatueClientModule() {
    RenderItemHandler.listeners += RenderItemHandler { stack, renderMode, leftHanded, matrices, vertexConsumers, light, overlay, model ->
        if (stack.item !is FairyStatueBlockItem) return@RenderItemHandler
        val motif = stack.getFairyMotif()
        val itemStack = motif?.createFairyItemStack() ?: return@RenderItemHandler
        matrices.stack {
            model.transforms.getTransform(renderMode).apply(leftHanded, matrices)

            matrices.stack {
                matrices.scale(0.5F, 0.5F, 0.5F)
                val resultModel = Minecraft.getInstance().itemRenderer.getModel(itemStack, null, null, 0)
                Minecraft.getInstance().itemRenderer.render(itemStack, ItemDisplayContext.GUI, false, matrices, vertexConsumers, light, overlay, resultModel)
            }
        }
    }
}
