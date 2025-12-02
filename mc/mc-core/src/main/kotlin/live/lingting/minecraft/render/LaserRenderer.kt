package live.lingting.minecraft.render

import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.BufferUploader
import com.mojang.blaze3d.vertex.DefaultVertexFormat
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.Tesselator
import com.mojang.blaze3d.vertex.VertexFormat
import com.mojang.math.Axis
import live.lingting.framework.util.DurationUtils.seconds
import live.lingting.minecraft.data.WeaponsData
import live.lingting.minecraft.listener.ClientLevelListener
import live.lingting.minecraft.ray.RayHit
import live.lingting.minecraft.util.RayUtils
import net.minecraft.client.renderer.GameRenderer
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.Vec3
import org.joml.Matrix4f
import java.awt.Color
import java.time.Duration
import kotlin.math.atan2
import kotlin.math.sqrt

/**
 * @author lingting 2025/12/2 10:36
 */
class LaserRenderer : ClientLevelListener {

    companion object {

        /**
         * argb
         */
        const val DEFAULT_COLOR = 0xcc40a9ff

        @JvmField
        val DEFAULT_DURATION: Duration = 1.5.seconds

        private val queue = mutableListOf<Data>()

        @JvmStatic
        fun push(data: Data) {
            queue.add(data)
        }

        @JvmStatic
        @JvmOverloads
        fun push(
            player: Player,
            data: WeaponsData,
            hits: List<RayHit>,
            color: Long = DEFAULT_COLOR,
            duration: Duration = DEFAULT_DURATION
        ) {
            val pos = RayUtils.pos(player, data.distance)
            val start = pos.first
            val end = hits.lastOrNull()?.pos ?: pos.second
            push(Data(start, end, data.range, color, duration))
        }
    }

    fun render(pose: PoseStack, cameraPos: Vec3) {
        if (queue.isEmpty()) return

        val tesselator = Tesselator.getInstance()
        val bufferBuilder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR)

        // 启用混合 (透明度)
        RenderSystem.enableBlend()
        RenderSystem.defaultBlendFunc()
        // 设置着色器 (POSITION_COLOR)
        RenderSystem.setShader(GameRenderer::getPositionColorShader)
        // 禁用面剔除，确保所有面都绘制（如果法线方向错误，这很重要）
        RenderSystem.disableCull()
        // 启用深度测试，防止穿透实体
        RenderSystem.enableDepthTest()
        // 强制使用线性深度
        RenderSystem.depthMask(false)

        val iterator = queue.iterator()
        while (iterator.hasNext()) {
            val data = iterator.next()

            // 计算生命周期透明度衰减
            val alphaRatio = 1 - data.tick.toDouble() / data.maxTick
            val c = Color(data.color.toInt(), true)
            val r = c.red
            val g = c.green
            val b = c.blue
            val a = (c.alpha * alphaRatio).toInt()

            renderSingleLaser(pose, bufferBuilder, data, cameraPos, r, g, b, a)
        }

        val mesh = bufferBuilder.build()
        if (mesh != null) {
            BufferUploader.drawWithShader(mesh)
        }

        // 重置 GL 状态，保持环境干净
        RenderSystem.depthMask(true)
        RenderSystem.enableCull()
        RenderSystem.enableDepthTest()
        RenderSystem.disableBlend()
    }

    private fun renderSingleLaser(
        pose: PoseStack,
        consumer: com.mojang.blaze3d.vertex.VertexConsumer,
        laser: Data,
        cameraPos: Vec3,
        r: Int, g: Int, b: Int, a: Int
    ) {
        val start = laser.start
        val end = laser.end
        val width = laser.radius.toFloat()

        val diff = end.subtract(start)
        val length = diff.length()

        val camX = cameraPos.x.toFloat()
        val camY = cameraPos.y.toFloat()
        val camZ = cameraPos.z.toFloat()

        // 计算旋转角度 (将 Z 轴对齐到 激光方向)
        val hDist = sqrt(diff.x * diff.x + diff.z * diff.z)
        // Minecraft 的 Yaw 算法
        val yaw = (atan2(diff.x, diff.z) * 180.0 / Math.PI).toFloat()
        val pitch = (atan2(diff.y, hDist) * 180.0 / Math.PI).toFloat()

        pose.pushPose()

        // 移动到起始点 (相对于相机)
        pose.translate(start.x - camX, start.y - camY, start.z - camZ)

        // 旋转
        pose.mulPose(Axis.YP.rotationDegrees(yaw))
        pose.mulPose(Axis.XP.rotationDegrees(-pitch))

        // 确保最小宽度 0.01m
        val safeWidth = if (width < 0.01f) 0.01f else width

        val last = pose.last().pose()

        // 画四个面组成一个长条
        drawBox(consumer, last, safeWidth, length.toFloat(), r, g, b, a)

        pose.popPose()
    }

    private fun drawBox(
        consumer: com.mojang.blaze3d.vertex.VertexConsumer,
        matrix: Matrix4f,
        w: Float,
        l: Float,
        r: Int, g: Int, b: Int, a: Int
    ) {
        // 简单的长方体顶点构建
        // Top face
        vertex(consumer, matrix, -w, w, l, r, g, b, a)
        vertex(consumer, matrix, w, w, l, r, g, b, a)
        vertex(consumer, matrix, w, w, 0f, r, g, b, a)
        vertex(consumer, matrix, -w, w, 0f, r, g, b, a)

        // Bottom face
        vertex(consumer, matrix, -w, -w, 0f, r, g, b, a)
        vertex(consumer, matrix, w, -w, 0f, r, g, b, a)
        vertex(consumer, matrix, w, -w, l, r, g, b, a)
        vertex(consumer, matrix, -w, -w, l, r, g, b, a)

        // Left face
        vertex(consumer, matrix, -w, -w, l, r, g, b, a)
        vertex(consumer, matrix, -w, w, l, r, g, b, a)
        vertex(consumer, matrix, -w, w, 0f, r, g, b, a)
        vertex(consumer, matrix, -w, -w, 0f, r, g, b, a)

        // Right face
        vertex(consumer, matrix, w, -w, 0f, r, g, b, a)
        vertex(consumer, matrix, w, w, 0f, r, g, b, a)
        vertex(consumer, matrix, w, w, l, r, g, b, a)
        vertex(consumer, matrix, w, -w, l, r, g, b, a)
    }

    private fun vertex(
        consumer: com.mojang.blaze3d.vertex.VertexConsumer,
        matrix: Matrix4f,
        x: Float,
        y: Float,
        z: Float,
        r: Int,
        g: Int,
        b: Int,
        a: Int
    ) {
        consumer.addVertex(matrix, x, y, z).setColor(r, g, b, a)
    }

    override fun onRenderLevelAfterTranslucentBlocks(data: ClientLevelListener.RenderLevelData) {
        render(data.pose, data.camera.position)
    }

    override fun onClientPreTick() {
        queue.removeIf {
            it.tick++
            it.tick >= it.maxTick
        }
    }

    data class Data(
        val start: Vec3,
        val end: Vec3,
        val radius: Double,
        // ARGB
        val color: Long,
        val duration: Duration
    ) {
        /**
         * 当前存活tick数
         */
        var tick: Long = 0

        /**
         * 最大存活tick数, 使用double规避小数位忽略导致的精度丢失
         */
        val maxTick = ((duration.toMillis() / 1000.0) * 20).toLong()

    }

}