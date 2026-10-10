package miragefairy2024.client.mod.entity

import com.mojang.blaze3d.vertex.DefaultVertexFormat
import com.mojang.blaze3d.vertex.VertexFormat
import miragefairy2024.MirageFairy2024
import miragefairy2024.mod.entity.MirageMissileEntity
import miragefairy2024.mod.entity.MirageMissileItem
import miragefairy2024.mod.entity.searchMirageMissileTarget
import miragefairy2024.util.isValid
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.LightTexture
import net.minecraft.client.renderer.RenderStateShard
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.entity.ArrowRenderer
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.Mth
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.BowItem
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import org.joml.Quaternionf
import org.joml.Vector3f
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

class MirageMissileEntityRenderer(context: EntityRendererProvider.Context) : ArrowRenderer<MirageMissileEntity>(context) {
    companion object {
        val TEXTURE: ResourceLocation = MirageFairy2024.identifier("textures/entity/projectiles/mirage_missile.png")
    }

    override fun getTextureLocation(entity: MirageMissileEntity) = TEXTURE
}

/** 事前表示の目印として使う、バニラの矢のテクスチャなのだ～🌱 */
private val ARROW_TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/projectiles/arrow.png")

/**
 * 矢のテクスチャのうち、軸と鏃が横並びに描かれている帯の範囲なのだ～🌱
 * [net.minecraft.client.renderer.entity.ArrowRenderer.render] が軸の側面に張っているのと、同じ範囲なのだ～🌱
 */
private const val ARROW_U0 = 0.0F
private const val ARROW_U1 = 0.5F
private const val ARROW_V0 = 0.0F
private const val ARROW_V1 = 0.15625F

/** 目印の矢の、長辺の長さなのだ～🌱 */
private const val MARKER_LENGTH = 0.8

private const val MARKER_ASPECT = (ARROW_V1 - ARROW_V0) / (ARROW_U1 - ARROW_U0)

/** 相手の当たり判定の上端から、目印の矢の中心までの高さなのだ～🌱 */
private const val MARKER_OFFSET = 0.5

/** 跳ねる放物線の高さと、1 往復にかける時間なのだ～🌱 */
private const val BOUNCE_HEIGHT = 0.3
private const val BOUNCE_PERIOD_NANOS = 1_000_000_000L

/** 目印の矢を、周りの明るさに左右されない見た目で出すための、最大の明るさなのだ～🌱 */
private const val MARKER_LIGHT = LightTexture.FULL_BRIGHT

/** 不透明部分へ重ねる加算合成の色なのだ～🌱 */
private const val HIGHLIGHT_RGB = 0x00FF00

private fun createMarkerRenderType(name: String, transparency: RenderStateShard.TransparencyStateShard) = RenderType.create(
    "miragefairy2024:$name",
    DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP,
    VertexFormat.Mode.QUADS,
    256,
    RenderType.CompositeState.builder()
        .setShaderState(RenderType.POSITION_COLOR_TEX_LIGHTMAP_SHADER)
        .setTextureState(RenderStateShard.TextureStateShard(ARROW_TEXTURE, false, false))
        .setTransparencyState(transparency)
        .setLightmapState(RenderType.LIGHTMAP)
        // 壁や地形に隠れていても狙っている相手が分かるように、深度テストを切るのだ～🌱
        .setDepthTestState(RenderType.NO_DEPTH_TEST)
        .setWriteMaskState(RenderType.COLOR_WRITE)
        .setCullState(RenderType.NO_CULL)
        .createCompositeState(false)
)

private val MARKER_BASE by lazy { createMarkerRenderType("mirage_missile_target_base", RenderType.TRANSLUCENT_TRANSPARENCY) }

// SRC_ALPHA と ONE の組み合わせだから、テクスチャの透明な部分には色が足されないのだ～🌱
private val MARKER_HIGHLIGHT by lazy { createMarkerRenderType("mirage_missile_target_highlight", RenderType.LIGHTNING_TRANSPARENCY) }

/**
 * ミラージュミサイルを撃つ前に、今この瞬間に撃ったらどの相手を狙うかを、その相手の頭の上の矢で知らせるのだ～🌱
 * 狙う相手の選び方は、実際に射出された矢が使うのと同じ [miragefairy2024.mod.entity.searchMirageMissileTarget] なのだ～🌱
 */
fun registerMirageMissileTargetPreview() {
    WorldRenderEvents.LAST.register { context ->
        val minecraft = Minecraft.getInstance() ?: return@register
        val level = minecraft.level ?: return@register
        val player = minecraft.player ?: return@register
        if (!player.isValid) return@register

        val target = searchPreviewTarget(level, player) ?: return@register

        val poseStack = context.matrixStack()!!
        val cameraPosition = context.camera().position
        val corners = buildMarkerCorners(context.camera().rotation())

        // 放物線で跳ねさせることで、狙われていることをアピールするのだ～🌱
        val phase = (System.nanoTime() % BOUNCE_PERIOD_NANOS).toDouble() / BOUNCE_PERIOD_NANOS.toDouble()
        val bounce = 4.0 * phase * (1.0 - phase) * BOUNCE_HEIGHT

        // ワールド座標のままFloatにすると、原点から遠い場所では刻みが粗くなって目印が震えてしまうから、Doubleでカメラからの相対位置にしてからFloatにするのだ～🌱
        val centerX = target.x - cameraPosition.x
        val centerY = target.boundingBox.maxY + MARKER_OFFSET + bounce - cameraPosition.y
        val centerZ = target.z - cameraPosition.z

        val pose = poseStack.last().pose()
        val consumers = context.consumers()!!

        fun putQuad(renderType: RenderType, rgb: Int) {
            val red = (rgb shr 16) and 0xFF
            val green = (rgb shr 8) and 0xFF
            val blue = rgb and 0xFF
            val vertexConsumer = consumers.getBuffer(renderType)
            val us = floatArrayOf(ARROW_U0, ARROW_U0, ARROW_U1, ARROW_U1)
            val vs = floatArrayOf(ARROW_V0, ARROW_V1, ARROW_V1, ARROW_V0)
            corners.forEachIndexed { index, corner ->
                vertexConsumer
                    .addVertex(pose, (centerX + corner.x).toFloat(), (centerY + corner.y).toFloat(), (centerZ + corner.z).toFloat())
                    .setColor(red, green, blue, 255)
                    .setUv(us[index], vs[index])
                    .setLight(MARKER_LIGHT)
            }
        }

        putQuad(MARKER_BASE, 0xFFFFFF)
        putQuad(MARKER_HIGHLIGHT, HIGHLIGHT_RGB)
    }
}

/**
 * 目印の矢の 4 隅を、その中心からの相対位置で返すのだ～🌱
 * 並びは、テクスチャの左上、左下、右下、右上なのだ～🌱
 *
 * パーティクルと同じようにカメラへ正面を向けたうえで、テクスチャの右上の角が画面の真下へ来るように、画面の中で回すのだ～🌱
 * そうすると、横長の帯に描かれた鏃が、狙われている相手の方を指すのだ～🌱
 */
private fun buildMarkerCorners(cameraRotation: Quaternionf): List<Vector3f> {
    val halfLength = (MARKER_LENGTH / 2.0).toFloat()
    val halfWidth = (MARKER_LENGTH * MARKER_ASPECT / 2.0).toFloat()
    val angle = -Mth.HALF_PI - atan2(halfWidth, halfLength)
    val cosAngle = cos(angle)
    val sinAngle = sin(angle)

    fun corner(x: Float, y: Float) = Vector3f(x * cosAngle - y * sinAngle, x * sinAngle + y * cosAngle, 0.0F).rotate(cameraRotation)

    return listOf(
        corner(-halfLength, halfWidth),
        corner(-halfLength, -halfWidth),
        corner(halfLength, -halfWidth),
        corner(halfLength, halfWidth),
    )
}

/**
 * 今この瞬間に矢を放ったら狙われる相手を返すのだ～🌱
 * 弓を引いていて、かつ実際に放たれる矢がミラージュミサイルのときだけ、相手を探すのだ～🌱
 */
private fun searchPreviewTarget(level: Level, player: Player): LivingEntity? {
    if (!player.isUsingItem) return null
    val weapon = player.useItem

    // 引いた長さが初速を決めるのは弓だけで、クロスボウは装填してから撃つから初速が別の決まり方をするのだ～🌱
    if (weapon.item !is BowItem) return null
    if (player.getProjectile(weapon).item !is MirageMissileItem) return null

    // 初速と初期位置は、[net.minecraft.world.item.BowItem.releaseUsing] と [net.minecraft.world.entity.projectile.AbstractArrow] が実際に使うのと同じ式で求めるのだ～🌱
    val power = BowItem.getPowerForTime(player.ticksUsingItem)
    if (power < 0.1F) return null
    val speed = (power * 3.0F).toDouble()
    val direction = Vec3(
        (-Mth.sin(player.yRot * Mth.DEG_TO_RAD) * Mth.cos(player.xRot * Mth.DEG_TO_RAD)).toDouble(),
        (-Mth.sin(player.xRot * Mth.DEG_TO_RAD)).toDouble(),
        (Mth.cos(player.yRot * Mth.DEG_TO_RAD) * Mth.cos(player.xRot * Mth.DEG_TO_RAD)).toDouble(),
    )
    val knownMovement = player.knownMovement
    val initialVelocity = direction.normalize().scale(speed).add(knownMovement.x, if (player.onGround()) 0.0 else knownMovement.y, knownMovement.z)
    val origin = Vec3(player.x, player.eyeY - 0.1, player.z)

    return searchMirageMissileTarget(level, origin, initialVelocity, player)
}
