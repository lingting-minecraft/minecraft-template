package live.lingting.minecraft.launch.provider

import live.lingting.minecraft.App
import net.minecraft.DetectedVersion
import net.minecraft.data.metadata.PackMetadataGenerator
import net.minecraft.network.chat.Component
import net.minecraft.server.packs.PackType
import net.minecraft.server.packs.metadata.pack.PackMetadataSection
import net.minecraft.util.InclusiveRange
import net.neoforged.neoforge.data.event.GatherDataEvent
import java.util.Optional

/**
 * @author lingting 2025/11/24 15:33
 */
object PackMetaProvider {

    fun register(e: GatherDataEvent) {
        val generator = PackMetadataGenerator(e.generator.packOutput)
        generator.add(
            PackMetadataSection.TYPE, PackMetadataSection(
                Component.literal("Resources for ${App.modName}"),
                DetectedVersion.BUILT_IN.getPackVersion(PackType.SERVER_DATA),
                Optional.of(InclusiveRange(0, Int.MAX_VALUE))
            )
        )

        e.generator.addProvider(true, generator)
    }

}