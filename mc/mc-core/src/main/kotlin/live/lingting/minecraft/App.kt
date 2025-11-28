package live.lingting.minecraft

import live.lingting.framework.resource.JarResourceResolver
import live.lingting.framework.util.ResourceUtils
import live.lingting.framework.value.WaitValue
import live.lingting.minecraft.component.util.TomlUtils
import live.lingting.minecraft.data.RegisterData

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

    private val registerDataValue = WaitValue.of<RegisterData>()

    var registerData: RegisterData
        get() = registerDataValue.notNull()
        set(value) = registerDataValue.update(value)

    fun findItem(id: String?) = registerData.findItem(id)

    fun getItem(id: String) = registerData.getItem(id)

    fun findBlock(id: String?) = registerData.findBlock(id)

    fun getBlock(id: String) = registerData.getBlock(id)

}