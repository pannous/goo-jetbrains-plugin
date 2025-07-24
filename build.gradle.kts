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

configurations {
    all {
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib-jdk8")
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib-jdk7")
    }
}

tasks {
    withType<JavaCompile> {
        sourceCompatibility = "21"
        targetCompatibility = "21"
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
        }
    }

    patchPluginXml {
        sinceBuild.set("251")
        untilBuild.set("251.*")
    }
    
    buildSearchableOptions {
        enabled = false
    }
    
    jarSearchableOptions {
        enabled = false
    }
    
    // Copy goo binary to resources before processing resources
    processResources {
        doFirst {
            val goBinary = file("bin/go")
            if (!goBinary.exists()) {
                throw GradleException("Goo binary not found at bin/go")
            }
            if (!goBinary.canExecute()) {
                throw GradleException("Goo binary at bin/go is not executable")
            }
            
            // Test if binary responds to version command
            try {
                val stdOut = ByteArrayOutputStream()
                val stdErr = ByteArrayOutputStream()
                val result = exec {
                    commandLine(goBinary.absolutePath, "version")
                    isIgnoreExitValue = true
                    standardOutput = stdOut
                    errorOutput = stdErr
                }
                if (result.exitValue != 0) {
                    val output = stdOut.toString() + stdErr.toString()
                    println("Warning: Goo binary at bin/go failed version check (exit code: ${result.exitValue})")
                    println("Output: $output")
                    println("This may indicate the binary is in development or broken state")
                } else {
                    val output = stdOut.toString()
                    println("Goo binary validation successful: $output")
                }
            } catch (e: Exception) {
                println("Warning: Could not validate goo binary: ${e.message}")
                println("Proceeding with copy, but binary may not work correctly")
            }
        }
        
        from("bin/go") {
            into("bin")
            rename("go", "goo")
        }
    }
    
    runIde {
        jvmArgs = listOf("-Xmx2048m", "-XX:+UseG1GC")
        systemProperty("idea.auto.reload.plugins", "true")
        systemProperty("idea.plugin.in.sandbox.mode", "true")
    }
}