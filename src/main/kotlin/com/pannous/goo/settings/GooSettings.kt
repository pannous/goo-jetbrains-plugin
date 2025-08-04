package com.pannous.goo.settings

import com.intellij.openapi.components.*
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.SystemInfo
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.attribute.PosixFilePermission

@Service(Service.Level.PROJECT)
@State(
    name = "GooSettings",
    storages = [Storage("goo.xml")]
)
class GooSettings : PersistentStateComponent<GooSettings.State> {
    
    data class State(
        var compilerPath: String = "",
        var goRoot: String = "",
        var enableCompilerIntegration: Boolean = true,
        var useBundledCompiler: Boolean = true
    )
    
    private var state = State()
    
    override fun getState(): State = state
    
    override fun loadState(state: State) {
        this.state = state
    }
    
    fun getCompilerPath(): String {
        if (state.compilerPath.isNotEmpty()) {
            return state.compilerPath
        }
        
        if (state.useBundledCompiler) {
            // Try bundled compiler from plugin resources first
            getBundledCompilerPath()?.let { path ->
                state.compilerPath = path
                return path
            }
            
            // Fallback to common locations
            val bundledPaths = listOf(
                "/opt/other/go/bin/go",
                "/usr/local/go/bin/go", 
                "/usr/local/goo/bin/go"
            )
            
            for (path in bundledPaths) {
                if (java.io.File(path).exists()) {
                    state.compilerPath = path
                    return path
                }
            }
        }
        
        // Fallback to PATH or user locations
        val userPaths = listOf(
            System.getProperty("user.home") + "/go/bin/go",
            System.getProperty("user.home") + "/goo/bin/go"
        )
        
        for (path in userPaths) {
            if (java.io.File(path).exists()) {
                state.compilerPath = path
                return path
            }
        }
        
        return "go" // Final fallback to PATH
    }
    
    fun getGoRoot(): String {
        if (state.goRoot.isNotEmpty()) {
            return state.goRoot
        }
        
        // Try to derive from compiler path
        val compilerPath = getCompilerPath()
        if (compilerPath.endsWith("/bin/go")) {
            val goRoot = compilerPath.removeSuffix("/bin/go")
            if (java.io.File(goRoot).exists()) {
                state.goRoot = goRoot
                return goRoot
            }
        }
        
        // Try common locations
        val possibleRoots = listOf(
            "/opt/other/go",
            "/usr/local/go",
            "/usr/local/goo",
            System.getProperty("user.home") + "/go",
            System.getProperty("user.home") + "/goo"
        )
        
        for (root in possibleRoots) {
            if (java.io.File(root).exists()) {
                state.goRoot = root
                return root
            }
        }
        
        return "" // Let compiler use default
    }
    
    fun isCompilerIntegrationEnabled(): Boolean = state.enableCompilerIntegration
    
    fun setCompilerPath(path: String) {
        state.compilerPath = path
    }
    
    fun setGoRoot(root: String) {
        state.goRoot = root
    }
    
    fun setCompilerIntegrationEnabled(enabled: Boolean) {
        state.enableCompilerIntegration = enabled
    }
    
    fun isUsingBundledCompiler(): Boolean = state.useBundledCompiler
    
    fun setUseBundledCompiler(useBundled: Boolean) {
        state.useBundledCompiler = useBundled
        // Clear cached compiler path when switching modes
        state.compilerPath = ""
    }
    
    private fun getBundledCompilerPath(): String? {
        try {
            // Determine the architecture and OS
            val osArch = when {
                SystemInfo.isMac && (System.getProperty("os.arch") == "aarch64" || System.getProperty("os.arch") == "arm64") -> "darwin-arm64"
                SystemInfo.isMac && (System.getProperty("os.arch") == "x86_64" || System.getProperty("os.arch") == "amd64") -> "darwin-amd64"
                SystemInfo.isLinux && (System.getProperty("os.arch") == "aarch64" || System.getProperty("os.arch") == "arm64") -> "linux-arm64"
                SystemInfo.isLinux && (System.getProperty("os.arch") == "amd64" || System.getProperty("os.arch") == "x86_64") -> "linux-amd64"
                SystemInfo.isWindows -> "windows-amd64"
                else -> return null
            }
            
            // Resource path in the JAR
            val resourcePath = "/compiler/$osArch/go"
            val inputStream = this.javaClass.getResourceAsStream(resourcePath) ?: return null
            
            // Create temporary directory for extracted compiler
            val tempDir = Files.createTempDirectory("goo-compiler").toFile()
            tempDir.deleteOnExit()
            
            val compilerFile = File(tempDir, "go").apply { deleteOnExit() }
            
            // Extract the compiler from JAR to temp file
            FileOutputStream(compilerFile).use { output ->
                inputStream.copyTo(output)
            }
            
            // Make executable on Unix systems
            if (!SystemInfo.isWindows) {
                val permissions = mutableSetOf<PosixFilePermission>()
                permissions.add(PosixFilePermission.OWNER_READ)
                permissions.add(PosixFilePermission.OWNER_WRITE)
                permissions.add(PosixFilePermission.OWNER_EXECUTE)
                permissions.add(PosixFilePermission.GROUP_READ)
                permissions.add(PosixFilePermission.GROUP_EXECUTE)
                permissions.add(PosixFilePermission.OTHERS_READ)
                permissions.add(PosixFilePermission.OTHERS_EXECUTE)
                Files.setPosixFilePermissions(compilerFile.toPath(), permissions)
            }
            
            return compilerFile.absolutePath
        } catch (e: Exception) {
            // Return null if extraction fails - will fall back to other methods
            return null
        }
    }
    
    companion object {
        fun getInstance(project: Project): GooSettings {
            return project.getService(GooSettings::class.java)
        }
    }
}