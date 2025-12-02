package live.lingting.minecraft.listener

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.Camera
import net.minecraft.client.DeltaTracker
import net.minecraft.client.renderer.LevelRenderer
import net.minecraft.client.renderer.culling.Frustum
import org.joml.Matrix4f

/**
 * @author lingting 2025/12/2 10:45
 */
interface ClientLevelListener : BasicListener {

    fun onClientPreTick() {}

    fun onClientPostTick() {}

    data class RenderLevelData(
        val renderer: LevelRenderer,
        val pose: PoseStack,
        val modelViewMatrix: Matrix4f,
        val projectionMatrix: Matrix4f,
        val renderTick: Int,
        val partialTick: DeltaTracker,
        val camera: Camera,
        val frustum: Frustum,
    )

    fun onRenderLevelAfterTranslucentBlocks(data: RenderLevelData) {

    }

}