import org.gradle.kotlin.dsl.support.uppercaseFirstChar
import org.gradle.plugins.ide.idea.model.IdeaLanguageLevel
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import java.util.regex.Pattern

val modId = project.properties["mod.id"] as String

val projectGroup = "live.lingting.minecraft.$modId"
val projectVersion = "2025.08.09-beta1"

// 用于子模块获取包管理信息
val catalogLibs = libs
// java 项目
val javaProjects = subprojects
val launchProjects = javaProjects.filter { it.name.startsWith("launch-") }
val mcProjects = subprojects.filter { it.name.startsWith("mc-") }
// 使用的java版本
val javaVersion = JavaVersion.VERSION_21
// 字符集
val encoding = "UTF-8"
val ideaLanguageLevel = IdeaLanguageLevel(javaVersion)

plugins {
    id("idea")
    alias(libs.plugins.kotlin.jvm)
}

idea {
    project {
        languageLevel = ideaLanguageLevel
        targetBytecodeVersion = javaVersion
    }
}

allprojects {
    group = projectGroup
    version = projectVersion

    apply {
        plugin("idea")
    }

    idea {
        module {
            languageLevel = ideaLanguageLevel
            targetBytecodeVersion = javaVersion
        }
    }
}

configure(javaProjects) {

    apply {
        plugin(catalogLibs.plugins.kotlin.jvm.get().pluginId)
    }

    dependencies {
        catalogLibs.bundles.dependencies.get().forEach {
            implementation(platform(it))
        }
        implementation(catalogLibs.bundles.implementation)

        annotationProcessor(catalogLibs.bundles.annotation)
        compileOnly(catalogLibs.bundles.compile)
        testImplementation(catalogLibs.bundles.test)
    }

    // 这样子Java代码直接写在kotlin里面就可以被访问了
    sourceSets {
        main { java { srcDir("src/main/kotlin") } }
        test { java { srcDir("src/test/kotlin") } }
    }

    configure<KotlinJvmProjectExtension> {
        jvmToolchain(javaVersion.majorVersion.toInt())
        compilerOptions {
            freeCompilerArgs.addAll(
                "-Xjvm-default=all",
                "-Xjsr305=strict",
            )
        }
    }

    configure<JavaPluginExtension> {
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
    }

    tasks.withType<Test> {
        enabled = gradle.startParameter.taskNames.contains("test")
        useJUnitPlatform()
        testLogging {
            showStandardStreams = true
        }
    }

    tasks.withType<JavaCompile> {
        options.encoding = encoding
        options.compilerArgs.add("-parameters")
        options.setIncremental(true)
    }

    tasks.withType<Javadoc> {
        isFailOnError = false
        options.encoding(encoding)
    }
}

fun invoke(key: String, obj: Any?): Any? {
    if (obj == null) {
        return null
    }

    if (obj is Map<*, *>) {
        return obj[key]
    }

    if (obj is List<*>) {
        return obj[key.toInt()]
    }

    val java = obj::class.java
    val fields = java.fields.filter { field -> field.name == key }

    if (fields.isNotEmpty()) {
        return fields.first().get(obj)
    }
    val methods = java.methods.filter { it.name == "get${key.uppercaseFirstChar()}" && it.parameterCount == 0 }
    if (methods.isNotEmpty()) {
        return methods.first().invoke(obj)
    }
    return null
}

fun findByInvoke(keys: String, obj: Any?): Any? {
    var tv: Any? = obj
    keys.split(".").forEach { key ->
        tv = invoke(key, tv)
    }
    return tv
}

val convertPattern: Pattern = Pattern.compile("@[\\w|.]+@")
fun convertByInvoke(source: String, map: MutableMap<String, Any?>): String {
    if (!source.contains("@")) {
        return source
    }

    val matcher = convertPattern.matcher(source)
    var string = source

    while (matcher.find()) {
        val text = matcher.group()
        val keys = text.substring(1, text.length - 1)
        val value = if (map.containsKey(keys)) {
            // 优先从缓存获取
            map[keys]
        } else {
            val tv = findByInvoke(keys, map)
            map[keys] = tv
            tv
        }
        string = string.replace(text, value?.toString() ?: "")
    }

    return string
}

configure(launchProjects + mcProjects) {

    tasks.register("processLaunchResources", ProcessResources::class) {
        dependsOn("processResources")
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        val map = project.properties.toMutableMap()
        map.putIfAbsent("version", project.version)

        from(sourceSets.main.get().resources.srcDirs)
        into(tasks.processResources.get().destinationDir)
        include("app.toml", "META-INF/neoforge.mods.toml")
        eachFile {
            filter {
                convertByInvoke(it, map)
            }
        }
    }

    tasks.named { "classes" == it }.forEach {
        it.dependsOn("processLaunchResources")
    }

}

configure(launchProjects) {

    val generatedDir = File(project.layout.buildDirectory.dir("generated").get().asFile, "sources")
    val processAssetsOutput = File(generatedDir, "processAssets")

    tasks.register("processAssets", ProcessResources::class) {
        dependsOn("processResources")
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        val map = project.properties.toMutableMap()
        map.putIfAbsent("version", project.version)

        from(File(rootDir, "assets"))
        into(File(processAssetsOutput, "assets/${modId}"))
        exclude("i18n/**")
        eachFile {
            if (name.endsWith(".json")) {
                filter {
                    convertByInvoke(it, map)
                }
            }
        }
        outputs.dir(processAssetsOutput).withPropertyName("root")
    }

    sourceSets.main.get().resources.srcDir(processAssetsOutput)

}