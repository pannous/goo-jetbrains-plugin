package com.pannous.goo.settings

import com.intellij.openapi.components.*
import com.intellij.openapi.project.Project

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
            // Try bundled/common locations first
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
    
    companion object {
        fun getInstance(project: Project): GooSettings {
            return project.getService(GooSettings::class.java)
        }
    }
}