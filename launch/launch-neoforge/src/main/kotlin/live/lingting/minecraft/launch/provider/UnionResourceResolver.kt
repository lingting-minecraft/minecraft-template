package live.lingting.minecraft.launch.provider

import live.lingting.framework.resource.FileResourceResolver
import live.lingting.framework.resource.JarResourceResolver
import live.lingting.framework.resource.Resource
import live.lingting.framework.resource.ResourceResolver
import live.lingting.framework.resource.ResourceResolverProvider
import java.io.File
import java.net.URI
import java.net.URL

/**
 * @author lingting 2025/10/17 10:52
 */
class UnionResourceResolver : ResourceResolver {

    companion object {

        const val DELIMITER: String = "!/"

        const val PROTOCOL = "union"
        const val PROTOCOL_JAR = "jar:file"
        const val PROTOCOL_FILE = "file"

        const val PREFIX = PROTOCOL + ResourceResolverProvider.DELIMITER
        const val PREFIX_JAR = PROTOCOL_JAR + ResourceResolverProvider.DELIMITER
    }

    override fun isSupport(u: URL, protocol: String): Boolean {
        return protocol == PROTOCOL
    }

    override fun resolve(u: URL, protocol: String): List<Resource> {
        val rawUrl = u.toString()
        if (rawUrl.contains(".jar")) {
            return resolveByJar(rawUrl)
        }

        return resolveByFile(rawUrl)
    }

    override val sequence: Int = -1

    fun pick(str: String): String {
        return str.substringAfter(PREFIX).substringBeforeLast("%23").substringBeforeLast("#")
    }

    fun resolveByJar(rawUrl: String): List<Resource> {
        val url = rawUrl.split(DELIMITER).joinToString(DELIMITER) {
            if (it.startsWith(PREFIX)) {
                val last = pick(it)
                // 转为jar:file 协议
                PREFIX_JAR + last
            } else {
                it
            }
        }

        val u = URI.create(url).toURL()
        return JarResourceResolver.resolve(u, PROTOCOL_JAR)
    }

    fun resolveByFile(rawUrl: String): List<Resource> {
        val path = rawUrl.split(DELIMITER).joinToString("") {
            if (it.startsWith(PREFIX)) {
                pick(it)
            } else {
                it
            }
        }

        val file = File(path)
        return FileResourceResolver.resolve(file, PROTOCOL_FILE)
    }

}