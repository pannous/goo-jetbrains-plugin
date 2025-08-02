// Example: Auto-increment version in Gradle
// This shows different approaches we could implement

// Option 1: Git-based versioning
val gitCommitCount = providers.exec {
    commandLine("git", "rev-list", "--count", "HEAD")
}.standardOutput.asText.get().trim().toInt()

val gitHash = providers.exec {
    commandLine("git", "rev-parse", "--short", "HEAD")
}.standardOutput.asText.get().trim()

// Option 2: Date-based versioning
val buildDate = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"))

// Option 3: Auto-increment patch version
fun getNextVersion(): String {
    val currentVersion = "1.2.1" // Could read from file
    val parts = currentVersion.split(".")
    val major = parts[0].toInt()
    val minor = parts[1].toInt() 
    val patch = parts[2].toInt()
    
    // Auto-increment patch on each build
    return "$major.$minor.${patch + 1}"
}

// Usage examples:
// version = "1.2.$gitCommitCount"              // Git commit-based
// version = "1.2.1-$gitHash"                   // With git hash
// version = "1.2.1-$buildDate"                 // Date snapshot
// version = getNextVersion()                   // Auto-increment patch
// version = "1.2.$gitCommitCount-SNAPSHOT"     // Development builds