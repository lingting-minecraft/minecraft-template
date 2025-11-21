package live.lingting.minecraft.version

data class Version(
    val major: Int,
    val minor: Int,
    val patch: Int,
) {
    companion object {
        @JvmStatic
        fun from(source: String): Version {
            val split = source.split(".")
            var major = 0
            var minor = 0
            var patch = 0

            if (split.isNotEmpty()) {
                major = split[0].toInt()
            }
            if (split.size > 1) {
                minor = split[1].toInt()
            }
            if (split.size > 2) {
                patch = split[2].toInt()
            }

            return Version(major, minor, patch)
        }
    }

    fun `is`(major: Int, minor: Int? = null, patch: Int? = null): Boolean {
        if (this.major != major) return false
        if (minor != null && this.minor != minor) return false
        if (patch != null && this.patch != patch) return false
        return true
    }

    /**
     * 当前版本是否大于指定版本
     */
    fun isGe(major: Int, minor: Int? = null, patch: Int? = null): Boolean {
        if (this.major <= major) return false
        if (minor != null && this.minor <= minor) return false
        if (patch != null && this.patch <= patch) return false
        return true
    }

}