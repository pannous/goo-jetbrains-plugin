// Recommended: Conditional auto-versioning
// Only auto-increment for development builds, manual for releases

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.2.0"
    id("org.jetbrains.intellij.platform") version "2.6.0"
}

group = "com.pannous"

// Smart versioning strategy
fun determineVersion(): String {
    val baseVersion = "1.2.1" // Release version
    
    // Check if this is a development build
    val isDevelopmentBuild = project.hasProperty("dev") || 
                           System.getenv("CI") != null ||
                           !providers.exec {
                               commandLine("git", "status", "--porcelain")
                           }.standardOutput.asText.get().trim().isEmpty()
    
    return if (isDevelopmentBuild) {
        // Auto-increment for development
        val gitCommitCount = providers.exec {
            commandLine("git", "rev-list", "--count", "HEAD")
        }.standardOutput.asText.get().trim()
        
        "$baseVersion-dev.$gitCommitCount"
    } else {
        // Use base version for releases
        baseVersion
    }
}

version = determineVersion()

// Usage:
// gradle build           -> version = "1.2.1" (release)
// gradle build -Pdev     -> version = "1.2.1-dev.123" (development)
// CI builds              -> version = "1.2.1-dev.123" (auto-detected)

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

// Rest of build script...