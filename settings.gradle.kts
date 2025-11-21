plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

// 遍历rootDir, 获取符合条件的文件夹名称,  使用代码 include 所有文件夹
val ignore = setOf("gradle", "build", "logs", "version")
val isInclude = fun(file: File): Boolean {
    return file.isDirectory && !file.isHidden && !file.name.startsWith(".") && !ignore.contains(file.name)
}
rootProject.name = "minecraft-template"
rootDir.listFiles()?.filterNotNull()?.filter { isInclude(it) }?.forEach { dir ->
    dir.listFiles()?.filterNotNull()?.filter { isInclude(it) && it.name.contains("-") }?.forEach {
        include("${dir.name}:${it.name}")
    }
}

