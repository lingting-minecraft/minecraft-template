package live.lingting.minecraft.launch.basic

import live.lingting.minecraft.version.Version
import net.neoforged.fml.loading.FMLLoader

/**
 * @author lingting 2025/10/17 22:52
 */
object Versions {


    object NeoForge {
        val version = Version.from(FMLLoader.versionInfo().neoForgeVersion())

        val is21_4 = version.`is`(21, 4)
        val isGe21_4 = version.isGe(21, 4)

        val is21_1 = version.`is`(21, 1)

    }

}