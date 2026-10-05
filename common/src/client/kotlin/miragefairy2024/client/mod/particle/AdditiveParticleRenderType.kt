package miragefairy2024.client.mod.particle

import com.mojang.blaze3d.platform.GlStateManager
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.BufferBuilder
import com.mojang.blaze3d.vertex.DefaultVertexFormat
import com.mojang.blaze3d.vertex.Tesselator
import com.mojang.blaze3d.vertex.VertexFormat
import miragefairy2024.MirageFairy2024
import net.minecraft.client.particle.ParticleRenderType
import net.minecraft.client.renderer.texture.TextureAtlas
import net.minecraft.client.renderer.texture.TextureManager

/**
 * 加算合成でパーティクルを描画するのだ～🌱
 * バニラの [net.minecraft.client.particle.ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT] から、合成関数だけを加算へ替えたものなのだ～🌱
 *
 * [net.minecraft.client.particle.ParticleEngine.render] は、この種別ごとに [begin] を 1 回だけ呼んで、そのキューの全頂点をまとめて描画するのだ～🌱
 * だから、専用の種別を持つことで、他の MOD のパーティクルを巻き込まずに合成関数を選べるのだ～🌱
 */
object AdditiveParticleRenderType : ParticleRenderType {
    override fun begin(tesselator: Tesselator, textureManager: TextureManager): BufferBuilder {
        RenderSystem.depthMask(true)
        RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_PARTICLES)
        RenderSystem.enableBlend()
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE)
        return tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE)
    }

    // バニラの種別と同じく、クラッシュレポートに出る名前を与えるのだ～🌱
    override fun toString() = "${MirageFairy2024.MOD_ID}:additive"
}
