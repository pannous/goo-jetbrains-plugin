plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.25"
    id("org.jetbrains.intellij") version "1.17.4"
}

group = "com.pannous"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

intellij {
    // version.set("2024.1.5")
    // type.set("GO")

    // Alternatively, use your local IDE installation
    localPath.set("/Applications/GoLand.app/Contents")

    plugins.set(listOf(/* Plugin Dependencies */))
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
}

tasks {
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }

    patchPluginXml {
        sinceBuild.set("232")
        untilBuild.set("242.*")
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }
}