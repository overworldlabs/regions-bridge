import groovy.json.JsonOutput
import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform
import org.gradle.api.tasks.compile.JavaCompile

plugins {
    id("java")
}

group = "com.overworldlabs.regions"
version = "1.0.0"

val javaLanguageVersion = 25
val earlyBridgeBytecodeVersion = 21

val hytaleHome: String by extra {
    if (project.hasProperty("hytale_home")) {
        project.findProperty("hytale_home") as String
    } else {
        val os = DefaultNativePlatform.getCurrentOperatingSystem()
        when {
            os.isWindows -> "${System.getProperty("user.home")}/AppData/Roaming/Hytale"
            os.isMacOsX -> "${System.getProperty("user.home")}/Library/Application Support/Hytale"
            os.isLinux -> "${System.getProperty("user.home")}/.local/share/Hytale"
            else -> throw GradleException("Could not detect Hytale install. Set -Phytale_home=/path/to/Hytale.")
        }
    }
}

val patchline = (findProperty("patchline") as? String) ?: "release"
val hytaleServerJar = "$hytaleHome/install/$patchline/package/game/latest/Server/HytaleServer.jar"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(javaLanguageVersion))
    }
}

tasks.withType<JavaCompile> {
    options.release.set(earlyBridgeBytecodeVersion)
    options.compilerArgs.add("-Xlint:deprecation")
}

repositories {
    mavenCentral()
    maven(url = "https://repo.spongepowered.org/repository/maven-public/")
    maven(url = "https://maven.fabricmc.net/")
}

dependencies {
    compileOnly(files(hytaleServerJar))
    implementation("org.ow2.asm:asm:9.7")
    implementation("org.ow2.asm:asm-analysis:9.7")
    implementation("org.ow2.asm:asm-commons:9.7")
    implementation("org.ow2.asm:asm-tree:9.7")
    implementation("org.ow2.asm:asm-util:9.7")
    implementation("net.fabricmc:sponge-mixin:0.16.5+mixin.0.8.7")
    implementation("org.ow2.sat4j:org.ow2.sat4j.core:2.3.6")
    implementation("org.ow2.sat4j:org.ow2.sat4j.pb:2.3.6")
    implementation("com.google.guava:guava:33.2.1-jre")
    implementation("com.google.code.gson:gson:2.10.1")
}

tasks.processResources {
    inputs.property("version", project.version)
    filesMatching("manifest.json") {
        expand("version" to project.version)
    }
}

tasks.jar {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    archiveBaseName.set("Regions-Bridge")
    archiveVersion.set(project.version.toString())

    manifest {
        attributes(
            "Implementation-Title" to "Regions-Bridge",
            "Implementation-Version" to project.version
        )
    }

    from({
        configurations.runtimeClasspath.get()
            .filter { it.name.endsWith(".jar") }
            .map { zipTree(it) }
    })
    exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA")
}
