/*
 * MIT License
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:

 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.

 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.vga.sjl.gradle.dist

import org.gradle.api.DefaultTask
import org.gradle.api.plugins.internal.JavaPluginHelper
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.lang.IllegalStateException
import javax.inject.Inject

abstract class BaseLinux64DistTask<T:Linux64DirectoryDistributionConfig>(): DefaultTask(){
    private lateinit var taskConfig:T
    private lateinit var commonConfig:SjlDistCommonConfig

    constructor(taskConfig:T, commonConfig:SjlDistCommonConfig) : this() {
        this.taskConfig = taskConfig
        this.commonConfig = commonConfig;
        group = "dist"
        dependsOn(commonConfig.dependsOnTasks.toArray())
    }

    @TaskAction
    open fun doAction(){
        ensureDirectoryExists(project, "../dist/${name}", true)
        val dir = ensureDirectoryExists(project, "../dist/${name}/${commonConfig.appName}", true)
        downloadJre(File(dir, commonConfig.jreRelativePath), Platform.LINUX64, commonConfig, project)
        val libDir = ensureDirectoryExists(File(dir, commonConfig.libRelativePath), true)
        val component = JavaPluginHelper.getJavaComponent(project)
        val files = component.mainJarTask.get().outputs.files.plus(component.runtimeClasspathConfiguration)
        files.forEach { it.copyTo(File(libDir, it.name))}
        commonConfig.assetsList.forEach {pair ->
            project.copy {
                it.with(pair.second)
                it.into(File(dir, pair.first))
            }
        }
        taskConfig.configLocation?.let {
            project.file(it.first).copyTo(File(dir, it.second))
        }
    }
}
open class Linux64DirectoryDistTask: BaseLinux64DistTask<Linux64DirectoryDistributionConfig>{
    @Inject
    constructor(taskConfig:Linux64DirectoryDistributionConfig, commonConfig:SjlDistCommonConfig) : super(taskConfig, commonConfig)
}

open class Linux64ArchiveDistTask: BaseLinux64DistTask<Linux64ArchiveDistributionConfig>{
    private val taskConfig: Linux64ArchiveDistributionConfig
    private val commonConfig: SjlDistCommonConfig
    @Inject
    constructor(taskConfig:Linux64ArchiveDistributionConfig, commonConfig:SjlDistCommonConfig) : super(taskConfig, commonConfig){
        this.taskConfig = taskConfig
        this.commonConfig = commonConfig
    }
    @TaskAction
    override fun doAction(){
        super.doAction()
        val dir = toFile(project, "../dist/${name}/${commonConfig.appName}")
        when(taskConfig.archiveType){
            DistributionArchiveType.TARGZ -> {
                val tarFile = File(dir.parent, "${commonConfig.appName}.tar")
                val tarGzFile = File(dir.parent, "${commonConfig.appName}.tar.gz")
                ant.invokeMethod("tar", mapOf("destfile" to tarFile.absolutePath,
                "baseDir" to dir.parentFile.absolutePath))
                ant.invokeMethod("gzip", mapOf("destfile" to tarGzFile.absolutePath,
                    "src" to tarFile.absolutePath))
                dir.deleteRecursively()
                tarFile.delete()
            }
            DistributionArchiveType.ZIP -> {
                val zipFile = File(dir.parent, "${commonConfig.appName}.zip")
                ant.invokeMethod("zip", mapOf("destfile" to zipFile.absolutePath,
                    "baseDir" to dir.absolutePath))
                dir.deleteRecursively()
            }
        }
    }
}
