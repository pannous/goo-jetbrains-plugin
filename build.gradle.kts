plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.1.0"
    id("org.jetbrains.intellij.platform") version "2.1.0"
}

group = "com.pannous"
version = "1.1.1"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

intellijPlatform {
    pluginConfiguration {
        ideaVersion {
            sinceBuild = "251"
            untilBuild = "251.*"
        }
    }
    
    sandboxContainer = file("${rootProject.projectDir}/.sandbox")
}

dependencies {
    intellijPlatform {
        goland("2025.1.3")
        instrumentationTools()
        pluginVerifier()
        zipSigner()
        testFramework(org.jetbrains.intellij.platform.gradle.TestFrameworkType.Platform)
    }
}

tasks {
    withType<JavaCompile> {
        sourceCompatibility = "21"
        targetCompatibility = "21"
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "21"
    }

    patchPluginXml {
        sinceBuild.set("251")
        untilBuild.set("251.*")
    }
    
    buildSearchableOptions {
        enabled = false
    }
    
    runIde {
        jvmArgs = listOf("-Xmx2048m", "-XX:+UseG1GC")
        systemProperty("idea.auto.reload.plugins", "true")
        systemProperty("idea.plugin.in.sandbox.mode", "true")
    }
}