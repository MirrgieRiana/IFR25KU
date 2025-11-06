package miragefairy2024.client.mod

import com.mojang.blaze3d.vertex.DefaultVertexFormat
import com.mojang.blaze3d.vertex.VertexFormat
import miragefairy2024.ModContext
import miragefairy2024.mod.CommonRenderingEvents
import miragefairy2024.mod.RenderBlockPosesOutlineContext
import mirrg.kotlin.helium.max
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.LevelRenderer
import net.minecraft.client.renderer.RenderStateShard
import net.minecraft.client.renderer.RenderType
import net.minecraft.core.BlockPos
import java.util.OptionalDouble
import kotlin.math.roundToInt
import kotlin.math.sin

private val LINES_NO_DEPTH by lazy {
    RenderType.create(
        "miragefairy2024:lines_no_depth",
        DefaultVertexFormat.POSITION_COLOR_NORMAL,
        VertexFormat.Mode.LINES,
        1536,
        RenderType.CompositeState.builder()
            .setShaderState(RenderType.RENDERTYPE_LINES_SHADER)
            .setLineState(RenderStateShard.LineStateShard(OptionalDouble.empty()))
            .setLayeringState(RenderType.VIEW_OFFSET_Z_LAYERING)
            .setTransparencyState(RenderType.TRANSLUCENT_TRANSPARENCY)
            .setOutputState(RenderType.MAIN_TARGET)
            .setDepthTestState(RenderType.NO_DEPTH_TEST)
            .setWriteMaskState(RenderType.COLOR_WRITE)
            .setCullState(RenderType.NO_CULL)
            .createCompositeState(false)
    )
}

context(ModContext)
fun initCommonClientModule() {
    CommonRenderingEvents.onRenderBlockPosesOutline.subscribe { listener ->
        WorldRenderEvents.LAST.register { context ->
            val minecraft = Minecraft.getInstance() ?: return@register
            val level = minecraft.level ?: return@register

            val (baseBlockPos, blockPoses) = listener.getBlockPoses(object : RenderBlockPosesOutlineContext {
                override val level get() = level
                override val player get() = minecraft.player
                override val hitResult get() = minecraft.hitResult
            }) ?: return@register
            if (blockPoses.isEmpty()) return@register

            val poseStack = context.matrixStack()!!
            poseStack.pushPose()
            try {
                val camera = context.camera()
                poseStack.translate(-camera.position.x, -camera.position.y, -camera.position.z)

                val vertexConsumer = context.consumers()!!.getBuffer(LINES_NO_DEPTH)
                val pose = poseStack.last().pose()
                val packedLight = LevelRenderer.getLightColor(level, baseBlockPos)
                val skyLightLevel = (packedLight ushr 20) and 0xF
                val blockLightLevel = (packedLight ushr 4) and 0xF
                val skyFactor = 0.15 + 0.85 * (skyLightLevel.toDouble() / 15.0)
                val blockFactor = 0.15 + 0.85 * (blockLightLevel.toDouble() / 15.0)
                val lightFactor = skyFactor max blockFactor
                val brightness = (255.0 * lightFactor).roundToInt().coerceIn(0, 255)
                val theta = (System.nanoTime() % 2_000_000_000L).toDouble() / 1_000_000_000.0 * 2.0 * Math.PI
                val alpha = (255.0 * (0.5 + 0.25 * sin(theta))).roundToInt()
                collectEdges(blockPoses) { x0, y0, z0, x1, y1, z1 ->
                    vertexConsumer
                        .addVertex(pose, x0.toFloat(), y0.toFloat(), z0.toFloat())
                        .setColor(brightness, brightness, brightness, alpha)
                        .setNormal(x1.toFloat() - x0.toFloat(), y1.toFloat() - y0.toFloat(), z1.toFloat() - z0.toFloat())
                    vertexConsumer
                        .addVertex(pose, x1.toFloat(), y1.toFloat(), z1.toFloat())
                        .setColor(brightness, brightness, brightness, alpha)
                        .setNormal(x1.toFloat() - x0.toFloat(), y1.toFloat() - y0.toFloat(), z1.toFloat() - z0.toFloat())
                }
            } finally {
                poseStack.popPose()
            }
        }
    }
}

/**
 * @param addEdge 必ず長さが1
 */
private fun collectEdges(existingBlockPosSet: Set<BlockPos>, addEdge: (Int, Int, Int, Int, Int, Int) -> Unit) {

    // 採掘範囲と、辺を共有する負の位置のブロック座標も含めたリスト
    // 辺のチェックを正の方向の3辺しかしないため、残り9辺をこれでカバーする
    val checkingBlockPosSet = mutableSetOf<BlockPos>()
    existingBlockPosSet.forEach { blockPos ->
        checkingBlockPosSet.add(blockPos)
        checkingBlockPosSet.add(blockPos.offset(0, 0, -1)) // 2辺を共有する
        checkingBlockPosSet.add(blockPos.offset(0, -1, 0)) // 2辺を共有する
        checkingBlockPosSet.add(blockPos.offset(0, -1, -1)) // 1辺を共有する
        checkingBlockPosSet.add(blockPos.offset(-1, 0, 0)) // 2辺を共有する
        checkingBlockPosSet.add(blockPos.offset(-1, 0, -1)) // 1辺を共有する
        checkingBlockPosSet.add(blockPos.offset(-1, -1, 0)) // 1辺を共有する
    }

    // すべてのチェック対象ブロック座標について、12辺のうち正の方向の3辺をチェック
    checkingBlockPosSet.forEach { blockPos ->
        run {
            val e1 = blockPos.offset(0, 0, 0) in existingBlockPosSet
            val e2 = blockPos.offset(0, 0, 1) in existingBlockPosSet
            val e3 = blockPos.offset(0, 1, 0) in existingBlockPosSet
            val e4 = blockPos.offset(0, 1, 1) in existingBlockPosSet
            if (!(e1 == e2 && e3 == e4 || e1 == e3 && e2 == e4)) { // 平坦な辺を除外
                addEdge(
                    blockPos.x + 0, blockPos.y + 1, blockPos.z + 1,
                    blockPos.x + 1, blockPos.y + 1, blockPos.z + 1,
                )
            }
        }
        run {
            val e1 = blockPos.offset(0, 0, 0) in existingBlockPosSet
            val e2 = blockPos.offset(0, 0, 1) in existingBlockPosSet
            val e3 = blockPos.offset(1, 0, 0) in existingBlockPosSet
            val e4 = blockPos.offset(1, 0, 1) in existingBlockPosSet
            if (!(e1 == e2 && e3 == e4 || e1 == e3 && e2 == e4)) { // 平坦な辺を除外
                addEdge(
                    blockPos.x + 1, blockPos.y + 0, blockPos.z + 1,
                    blockPos.x + 1, blockPos.y + 1, blockPos.z + 1,
                )
            }
        }
        run {
            val e1 = blockPos.offset(0, 0, 0) in existingBlockPosSet
            val e2 = blockPos.offset(0, 1, 0) in existingBlockPosSet
            val e3 = blockPos.offset(1, 0, 0) in existingBlockPosSet
            val e4 = blockPos.offset(1, 1, 0) in existingBlockPosSet
            if (!(e1 == e2 && e3 == e4 || e1 == e3 && e2 == e4)) { // 平坦な辺を除外
                addEdge(
                    blockPos.x + 1, blockPos.y + 1, blockPos.z + 0,
                    blockPos.x + 1, blockPos.y + 1, blockPos.z + 1,
                )
            }
        }
    }

}
