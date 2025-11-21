description = "用于嫁接mc的API, 让其他模块引用此模块后可以拥有mc的代码提示"

val minecraftVersion = project.properties["minecraft.version"] as String
val mappingsVersion = project.properties["minecraft.mappings.version"] as String

dependencies {
    val dir = project.layout.projectDirectory.dir("libs")
    dir.asFileTree.forEach {
        if (it.name.endsWith(".jar")) {
            api(files(it))
        }
    }
}