import com.google.gson.Gson
import com.google.gson.JsonObject
import org.gradle.kotlin.dsl.support.uppercaseFirstChar

description = "mod实现中, 非特化部分. 抽象出来后面可以单独项目然后发布作为公用依赖"

dependencies {
    compileOnly(project(":mc:mc-api"))
    api(project(":component:component-core"))
}

val basicPackage = "live.lingting.minecraft"

val generatedDir = File(project.layout.buildDirectory.dir("generated").get().asFile, "sources")
val i18nPackage = "$basicPackage.i18n"
val i18nObjectName = "I18n"
val i18nOutput = File(generatedDir, "i18n")

fun generateI18nByDir(dir: File, name: String): String {
    val builder = StringBuilder()
    val dirs = mutableListOf<File>()
    val files = mutableListOf<File>()
    dir.listFiles()?.forEach {
        if (it.isDirectory) {
            dirs.add(it)
        } else {
            files.add(it)
        }
    }

    val className = name.uppercaseFirstChar()
    val field = name.uppercase()
    var key = ""
    if (name != i18nObjectName) {
        key = field.lowercase()
        builder.appendLine("@JvmField")
            .append("val ").append(field).append(" = ").appendLine(className)
    }
    builder.appendLine("object $className : I18nFinder {")
    generateI18nBody(key, files, builder)
    dirs.forEach {
        val str = generateI18nByDir(it, it.name)
        builder.appendLine(str)
    }
    builder.appendLine("}")
    return builder.toString()
}

fun generateI18nBody(key: String, files: Collection<File>, builder: StringBuilder) {
    val gson = Gson()
    // {key: { locale: value}}
    val map = mutableMapOf<String, MutableMap<String, String>>()
    files.forEach {
        val locale = it.name.substringBeforeLast(".")
        val obj = gson.fromJson(it.readText(), JsonObject::class.java)
        obj.entrySet().forEach { (k, v) ->
            val absent = map.computeIfAbsent(k) { mutableMapOf() }
            if (absent.containsKey(locale)) {
                println("[WARN] [$key] 当前key在语言[$locale]存在重复配置!")
            }
            absent[locale] = v.asString
        }
    }

    map.forEach { (k, m) ->
        val field = k.replace(".", "_").uppercase()
        builder.appendLine("@JvmField")
            .append("val ").append(field).append(" = ")
            .append("I18nLocale(\"").append(key)
        if (key.isNotBlank()) {
            builder.append(".")
        }
        builder.append(k).append("\", ")
            .appendLine("buildMap {")

        m.forEach { (l, v) ->
            builder.append("put(")
                .append("\"").append(l).append("\",")
                .append("\"").append(v).append("\"")
                .appendLine(")")
        }

        builder.appendLine("})")

    }
}

tasks.register("processI18n") {
    val rootDir = File(project.rootDir, "assets/i18n")
    val writeDir = File(i18nOutput, i18nPackage.replace(".", "/"))
    // 声明输入依赖源, 且相对路径敏感. 源文件变化和路径变化触发
    inputs.dir(rootDir).withPropertyName("rootDir").withPathSensitivity(PathSensitivity.RELATIVE)
    // 声明输出依赖
    outputs.dir(writeDir).withPropertyName("writeDir")

    doLast {
        val str = generateI18nByDir(rootDir, i18nObjectName)
        writeDir.mkdirs()
        val file = File(writeDir, "$i18nObjectName.kt")
        file.writeText("package $i18nPackage\n")
        file.appendText(str)
    }
}

sourceSets.main.get().kotlin.srcDir(i18nOutput)

val texturesPackage = "$basicPackage.textures"
val texturesObjectName = "Textures"
val texturesOutput = File(generatedDir, "textures")

fun generateTextures(root: File): String {
    val builder = StringBuilder()
    val dirs = mutableListOf<File>()
    root.listFiles()?.forEach {
        if (it.isDirectory) {
            dirs.add(it)
        }
    }

    builder.appendLine("object $texturesObjectName {")

    dirs.forEach { dir ->
        val key = dir.name
        dir.listFiles().forEach { file ->
            if (file.isFile) {
                val name = file.name.substringBeforeLast(".")
                val field = "${key}_${name.replace(".", "_")}".uppercase()
                builder.appendLine("@JvmField")
                    .appendLine("val $field = TexturesItem(\"$key\",\"$name\")")
            }
        }
    }

    builder.appendLine("}")
    return builder.toString()
}

tasks.register("processTextures") {
    val rootDir = File(project.rootDir, "assets/textures")
    val writeDir = File(texturesOutput, texturesPackage.replace(".", "/"))
    // 声明输入依赖源, 且相对路径敏感. 源文件变化和路径变化触发
    inputs.dir(rootDir).withPropertyName("rootDir").withPathSensitivity(PathSensitivity.RELATIVE)
    // 声明输出依赖
    outputs.dir(writeDir).withPropertyName("writeDir")

    doLast {
        val str = generateTextures(rootDir)
        writeDir.mkdirs()
        val file = File(writeDir, "$texturesObjectName.kt")
        file.writeText("package $texturesPackage\n")
        file.appendText(str)
    }
}

sourceSets.main.get().kotlin.srcDir(texturesOutput)

tasks.named("compileKotlin") {
    dependsOn("processI18n")
    dependsOn("processTextures")
}
