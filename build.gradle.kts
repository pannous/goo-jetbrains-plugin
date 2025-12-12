plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.2.0"
    id("org.jetbrains.intellij.platform") version "2.9.0"
}

group = "com.pannous"

// Auto-increment patch version for development builds
val baseVersion = "1.2"
val buildNumber = System.getenv("BUILD_NUMBER") ?: run {
    // For local development, use git commit count as build number
    try {
        val gitCount = Runtime.getRuntime()
            .exec("git rev-list --count HEAD")
            .inputStream.bufferedReader().readText().trim()
        gitCount.toIntOrNull()?.toString() ?: "0"
    } catch (e: Exception) {
        "0"
    }
}

version = "$baseVersion.$buildNumber"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

// Suppress build warnings and verbose output
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions {
        suppressWarnings.set(true)
        freeCompilerArgs.add("-Xsuppress-version-warnings")
    }
}

intellijPlatform {
    pluginConfiguration {
        ideaVersion {
            sinceBuild = "253"
            untilBuild = "253.*"
        }
    }
}

dependencies {
    intellijPlatform {
        goland("2025.3")
    }
}
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}
tasks {
//    patchPluginXml {
//        sinceBuild.set("251")
//        untilBuild.set("251.*")
//    }

    runIde {
        systemProperty("idea.auto.reload.plugins", "true")
//        systemProperty("idea.plugin.in.sandbox.mode", "false")
    }
}
