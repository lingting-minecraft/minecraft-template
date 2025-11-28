import org.slf4j.event.Level

val modId = project.properties["mod.id"] as String
val modName = project.properties["mod.name"] as String

plugins {
    alias(libs.plugins.neoforged.modedev)
    alias(libs.plugins.shadow)
}

idea {
    module {
        isDownloadSources = true
        isDownloadJavadoc = true
    }
}

neoForge {
    version = project.properties["minecraft.neoforged.version"] as String
}

val shadowImplementation: Configuration by configurations.creating {
    isTransitive = true
    isCanBeResolved = true
    isCanBeConsumed = false
    extendsFrom()
}

dependencies {
    "additionalRuntimeClasspath"(api(project(":component:component-libs")) {
        exclude("org.slf4j")
    })
    shadowImplementation("api"(project(":launch:launch-core")) {
        exclude("org.slf4j")
    })
}

tasks.named { "compileKotlin" == it || "processLaunchResources" == it }.forEach {
    it.dependsOn("processAssets")
}

val buildDirectory = project.layout.buildDirectory
val generatedDir = File(buildDirectory.dir("generated").get().asFile, "sources")
val dataOutput = File(generatedDir, "data")

sourceSets.main.get().resources.srcDir(dataOutput)

neoForge {
    parchment {
        mappingsVersion = project.properties["minecraft.mappings.version"] as String
        minecraftVersion = project.properties["minecraft.version"] as String
    }

    runs {
        create("client") {
            client()
            logLevel = Level.DEBUG
            systemProperty("neoforge.enabledGameTestNamespaces", modId)
        }

        create("server") {
            server()
            logLevel = Level.DEBUG
            programArguments.addAll("--nogui")
            systemProperty("neoforge.enabledGameTestNamespaces", modId)
        }

        create("data") {
            data()
            logLevel = Level.DEBUG

            val sourceDir = tasks.getByName<ProcessResources>("processAssets").outputs.files.last()
            programArguments.addAll(
                "--mod", modId, "--all", "--output", dataOutput.absolutePath, "--existing", sourceDir.absolutePath
            )
            systemProperty("neoforge.enabledGameTestNamespaces", modId)
        }

        mods {
            create(modId) {

                sourceSet(sourceSets.main.get())

                project.rootProject.allprojects.forEach { p ->
                    if (p == project) {
                        return@forEach
                    }
                    val main = p.sourceSets.main.get()
                    sourceSet(main)
                }

            }
        }

    }

    ideSyncTask(tasks.named("processLaunchResources"))

}

tasks.shadowJar {
    archiveClassifier = ""
    archiveBaseName = project.name.replace("launch", modName)
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    configurations = listOf()
    exclude("")

    doFirst {
        from(
            shadowImplementation
                .filter { it.name.endsWith(".jar") && it.exists() }
                .map { zipTree(it) }
        )
        exclude("META-INF/*.SF")
        exclude("META-INF/*.DSA")
        exclude("META-INF/*.RSA")
        exclude("META-INF/*.md")
        exclude("META-INF/*LICENSE*")
        exclude("META-INF/*NOTICE*")
        exclude("module-info.class")
        exclude("package-info.class")
        exclude("META-INF/versions/**")
    }

    relocate("live.lingting", "$modId.live.lingting")
    mergeServiceFiles()
}

tasks.jar {
    enabled = false
}