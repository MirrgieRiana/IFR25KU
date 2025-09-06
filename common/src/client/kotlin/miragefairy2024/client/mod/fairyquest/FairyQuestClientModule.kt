package miragefairy2024.client.mod.fairyquest

import com.mojang.math.Axis
import miragefairy2024.ModContext
import miragefairy2024.client.util.registerHandledScreen
import miragefairy2024.client.util.stack
import miragefairy2024.mixin.client.api.RenderItemHandler
import miragefairy2024.mod.fairyquest.FairyQuestCardCard
import miragefairy2024.mod.fairyquest.fairyQuestCardScreenHandlerType
import miragefairy2024.mod.fairyquest.getFairyQuestRecipe
import miragefairy2024.util.createItemStack
import miragefairy2024.util.isNotIn
import net.minecraft.client.Minecraft
import net.minecraft.client.resources.model.ModelResourceLocation
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.Mth
import net.minecraft.world.item.ItemDisplayContext
import net.minecraft.world.item.Items

context(ModContext)
fun initFairyQuestClientModule() {
    RenderItemHandler.listeners += RenderItemHandler { stack, renderMode, leftHanded, matrices, vertexConsumers, light, overlay, model ->
        if (stack isNotIn FairyQuestCardCard.item()) return@RenderItemHandler
        matrices.stack {
            model.transforms.getTransform(renderMode).apply(leftHanded, matrices)

            // TODO モデルに
            matrices.stack {
                matrices.translate(0F, 1F / 16F, 0F)
                matrices.scale(0.5F, 0.5F, 0.01F)
                val resultModel = Minecraft.getInstance().modelManager.getModel(ModelResourceLocation(ResourceLocation.fromNamespaceAndPath("minecraft", "nether_portal"), "axis=x"))
                Minecraft.getInstance().itemRenderer.render(Items.DIRT.createItemStack(), ItemDisplayContext.GUI, false, matrices, vertexConsumers, light, overlay, resultModel)
            }

            val recipe = stack.getFairyQuestRecipe()
            if (recipe != null) {
                matrices.stack {
                    matrices.translate(0F, 1F / 16F, 0.02F)
                    matrices.scale(0.45F, 0.45F, 0.01F)
                    val resultModel = Minecraft.getInstance().itemRenderer.getModel(recipe.icon(), null, null, 0)
                    Minecraft.getInstance().itemRenderer.render(recipe.icon(), ItemDisplayContext.GUI, false, matrices, vertexConsumers, light, overlay, resultModel)
                }
                matrices.stack {
                    matrices.translate(0F, 1F / 16F, -0.02F)
                    matrices.scale(0.45F, 0.45F, 0.01F)
                    matrices.mulPose(Axis.YP.rotation(Mth.TWO_PI * 0.5F))
                    val resultModel = Minecraft.getInstance().itemRenderer.getModel(recipe.icon(), null, null, 0)
                    Minecraft.getInstance().itemRenderer.render(recipe.icon(), ItemDisplayContext.GUI, false, matrices, vertexConsumers, light, overlay, resultModel)
                }
            }

        }
    }
    fairyQuestCardScreenHandlerType.registerHandledScreen { gui, inventory, title -> FairyQuestCardScreen(gui, inventory, title) }
}
