package miragefairy2024.client.mod.entity

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.VertexConsumer
import miragefairy2024.client.util.stack
import miragefairy2024.mod.entity.FairyWispCard
import miragefairy2024.mod.entity.FairyWispEntity
import miragefairy2024.util.times
import net.minecraft.client.model.EntityModel
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.client.model.geom.PartPose
import net.minecraft.client.model.geom.builders.CubeListBuilder
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.entity.MobRenderer
import net.minecraft.util.Mth

class FairyWispEntityRenderer(context: EntityRendererProvider.Context) : MobRenderer<FairyWispEntity, FairyWispEntityModel>(context, FairyWispEntityModel(context.bakeLayer(MAIN.entityModelLayer)), 0.25F) {
    companion object {
        // 本体: 球っぽく見える小さなキューブ1つ（8x8x8）なのだ～🌱
        // テクスチャサイズ 64x32
        val MAIN = EntityModelLayerCard(FairyWispCard.identifier, "main", 64, 32) { root ->
            // 中央に小さなキューブを1つ置くだけのシンプルなモデルなのだ～🌱
            root.addOrReplaceChild(
                "body",
                CubeListBuilder.create().texOffs(0, 0).addBox(-4F, -4F, -4F, 8F, 8F, 8F),
                PartPose.offset(0F, 0F, 0F),
            )
        }
    }

    private val texture = "textures/entity/" * FairyWispCard.identifier * ".png"

    override fun getTextureLocation(entity: FairyWispEntity) = texture
}

class FairyWispEntityModel(private val root: ModelPart) : EntityModel<FairyWispEntity>() {
    private val body = root.getChild("body")

    override fun setupAnim(entity: FairyWispEntity, limbAngle: Float, limbDistance: Float, animationProgress: Float, headYaw: Float, headPitch: Float) {
        // ゆっくり回転してふわふわ感を演出するのだ～🌱
        val f = animationProgress * 2F * Mth.PI / 60F
        body.yRot = f
        body.xRot = Mth.sin(animationProgress * 0.1F) * 0.2F
    }

    override fun renderToBuffer(matrices: PoseStack, vertices: VertexConsumer, light: Int, overlay: Int, color: Int) {
        if (!root.visible) return
        matrices.stack {
            root.translateAndRotate(matrices)
            body.render(matrices, vertices, light, overlay, color)
        }
    }
}
