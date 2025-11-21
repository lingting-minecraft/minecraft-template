package live.lingting.minecraft.component.util

import live.lingting.framework.util.StreamUtils
import java.io.InputStream
import java.nio.charset.StandardCharsets.UTF_8

/**
 * @author lingting 2025/11/17 14:03
 */
object TomlUtils {

    fun read(i: InputStream): Map<String, Map<String, String>> {
        val source = mutableMapOf<String, MutableMap<String, String>>()
        var key = ""
        StreamUtils.readLine(i, UTF_8) { l, _ ->
            val line = l.trim()
            if (line.isBlank()) {
                return@readLine
            }
            if (line.startsWith("[")) {
                key = line.substring(1, line.length - 1)
                return@readLine
            }
            val (name, value) = convert(line)
            val map = source.computeIfAbsent(key) { mutableMapOf() }
            map[name] = value
        }
        return source
    }

    private fun convert(line: String): Pair<String, String> {
        val (n, v) = line.split("=".toRegex(), 2)
        val name = n.trim()
        var value = v.trim()
        if (value.startsWith("\"") && value.endsWith("\"")) {
            value = value.substring(1, value.length - 1)
        }
        return name to value
    }

}