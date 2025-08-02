package com.pannous.goo.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.pannous.goo.compiler.GooCompiler
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

/**
 * Service that manages Goo compiler integration for the project.
 * Provides caching, background compilation, and efficient resource management.
 */
@Service(Service.Level.PROJECT)
class GooCompilerService(private val project: Project) {
    
    private val compiler = GooCompiler(project)
    private val executor = Executors.newCachedThreadPool { runnable ->
        Thread(runnable, "Goo-Compiler-Thread").apply {
            isDaemon = true
        }
    }
    
    // Cache for compiler results to avoid repeated compilations
    private val cache = ConcurrentHashMap<String, CachedResult>()
    private val cacheTimeout = TimeUnit.MINUTES.toMillis(2) // 2 minutes cache
    
    data class CachedResult(
        val result: GooCompiler.CompilerResult,
        val timestamp: Long,
        val fileModificationStamp: Long
    )
    
    /**
     * Get compiler diagnostics for a file, using cache if available
     */
    fun getDiagnosticsAsync(file: VirtualFile): Future<GooCompiler.CompilerResult> {
        return executor.submit<GooCompiler.CompilerResult> {
            getDiagnostics(file)
        }
    }
    
    /**
     * Get compiler diagnostics for a file synchronously
     */
    fun getDiagnostics(file: VirtualFile): GooCompiler.CompilerResult {
        val cacheKey = file.path
        val modificationStamp = file.modificationStamp
        val currentTime = System.currentTimeMillis()
        
        // Check cache first
        val cached = cache[cacheKey]
        if (cached != null && 
            cached.fileModificationStamp == modificationStamp &&
            (currentTime - cached.timestamp) < cacheTimeout) {
            return cached.result
        }
        
        // Compile and cache result
        val result = compiler.getDiagnostics(file)
        cache[cacheKey] = CachedResult(result, currentTime, modificationStamp)
        
        return result
    }
    
    /**
     * Check syntax of a file
     */
    fun checkSyntaxAsync(file: VirtualFile): Future<GooCompiler.CompilerResult> {
        return executor.submit<GooCompiler.CompilerResult> {
            compiler.checkSyntax(file)
        }
    }
    
    /**
     * Parse file structure
     */
    fun parseFileAsync(file: VirtualFile): Future<GooCompiler.CompilerResult> {
        return executor.submit<GooCompiler.CompilerResult> {
            compiler.parseFile(file)
        }
    }
    
    /**
     * Check if compiler is available
     */
    fun isCompilerAvailable(): Boolean {
        return compiler.isCompilerAvailable()
    }
    
    /**
     * Clear cache for a specific file
     */
    fun invalidateCache(file: VirtualFile) {
        cache.remove(file.path)
    }
    
    /**
     * Clear all cached results
     */
    fun clearCache() {
        cache.clear()
    }
    
    /**
     * Clean up expired cache entries
     */
    fun cleanupCache() {
        val currentTime = System.currentTimeMillis()
        val expiredKeys = cache.entries
            .filter { (currentTime - it.value.timestamp) > cacheTimeout }
            .map { it.key }
        
        expiredKeys.forEach { cache.remove(it) }
    }
    
    /**
     * Dispose service and clean up resources
     */
    fun dispose() {
        executor.shutdown()
        cache.clear()
    }
    
    companion object {
        fun getInstance(project: Project): GooCompilerService {
            return project.getService(GooCompilerService::class.java)
        }
    }
}