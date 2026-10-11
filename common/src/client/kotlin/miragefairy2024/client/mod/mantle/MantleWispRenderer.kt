package miragefairy2024.client.mod.mantle

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.VertexConsumer
import miragefairy2024.client.mod.entity.EntityModelLayerCard
import miragefairy2024.client.util.stack
import miragefairy2024.mod.mantle.MantleWispCard
import miragefairy2024.mod.mantle.MantleWispEntity
import miragefairy2024.util.times
import net.minecraft.client.model.EntityModel
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.client.model.geom.PartPose
import net.minecraft.client.model.geom.builders.CubeListBuilder
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.entity.MobRenderer
import net.minecraft.util.Mth

class MantleWispEntityRenderer(context: EntityRendererProvider.Context) : MobRenderer<MantleWispEntity, MantleWispEntityModel>(context, MantleWispEntityModel(context.bakeLayer(MAIN.entityModelLayer)), 0.3F) {
    companion object {
        val MAIN = EntityModelLayerCard(MantleWispCard.identifier, "main", 64, 32) { root ->
            root.addOrReplaceChild(
                "body",
                CubeListBuilder.create().texOffs(0, 0).addBox(-4F, -4F, -4F, 8F, 8F, 8F),
                PartPose.offset(0F, 0F, 0F),
            )
        }
    }

    private val texture = "textures/entity/" * MantleWispCard.identifier * ".png"

    override fun getTextureLocation(entity: MantleWispEntity) = texture
}

class MantleWispEntityModel(private val root: ModelPart) : EntityModel<MantleWispEntity>() {
    private val body = root.getChild("body")

    override fun setupAnim(entity: MantleWispEntity, limbAngle: Float, limbDistance: Float, animationProgress: Float, headYaw: Float, headPitch: Float) {
        // ゆっくり回りながら、上下に揺れるのだ～🌱
        body.yRot = animationProgress * 2F * Mth.PI / 80F
        body.xRot = Mth.sin(animationProgress * 0.08F) * 0.15F
        body.y = Mth.sin(animationProgress * 0.12F) * 1.5F
    }

    override fun renderToBuffer(matrices: PoseStack, vertices: VertexConsumer, light: Int, overlay: Int, color: Int) {
        if (!root.visible) return
        matrices.stack {
            root.translateAndRotate(matrices)
            body.render(matrices, vertices, light, overlay, color)
        }
    }
}
