description = "用于嫁接mc的API, 让其他模块引用此模块后可以拥有mc的代码提示"

val minecraftVersion = project.properties["minecraft.version"] as String
val mappingsVersion = project.properties["minecraft.mappings.version"] as String

val brigadier: String? = "1.3.10"
val datafixerupper: String? = "8.0.16"

dependencies {
    val dir = project.layout.projectDirectory.dir("libs")
    dir.asFileTree.forEach {
        if (it.name.endsWith(".jar")) {
            api(files(it))
        }
    }
    if (!brigadier.isNullOrBlank()) {
        api("com.mojang:brigadier:$brigadier")
    }
    if (!datafixerupper.isNullOrBlank()) {
        api("com.mojang:datafixerupper:$datafixerupper")
    }
}