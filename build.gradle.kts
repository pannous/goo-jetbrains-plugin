plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.2.0"
    id("org.jetbrains.intellij.platform") version "2.6.0"
}

group = "com.pannous"
version = "1.2.1"

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
}

dependencies {
    intellijPlatform {
        local("/Applications/GoLand.app/Contents")
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
