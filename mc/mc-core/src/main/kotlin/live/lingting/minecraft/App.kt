package live.lingting.minecraft

import live.lingting.framework.resource.JarResourceResolver
import live.lingting.framework.util.ResourceUtils
import live.lingting.minecraft.component.util.TomlUtils

/**
 * @author lingting 2025/11/6 19:10
 */
object App {

    private val source = mutableMapOf<String, MutableMap<String, String>>()

    init {
        val scan = ResourceUtils.scan("app.toml")
        for (r in scan) {
            if (!r.isFile || !r.protocol.startsWith(JarResourceResolver.PROTOCOL)) {
                continue
            }
            val read = TomlUtils.read(r.stream())
            read.forEach { (k, v) ->
                val key = k.ifBlank { "mod" }
                val absent = source.computeIfAbsent(key) { mutableMapOf() }
                absent.putAll(v)
            }
            break
        }
    }

    @JvmStatic
    val modId: String
        get() = source["mod"]?.get("id") ?: ""

    @JvmStatic
    val modName: String
        get() = source["mod"]?.get("name") ?: ""

    @JvmStatic
    val resourceName
        get() = modName.lowercase()

}