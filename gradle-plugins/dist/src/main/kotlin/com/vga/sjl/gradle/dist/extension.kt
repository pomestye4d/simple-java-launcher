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

import org.gradle.api.Project
import org.gradle.api.file.CopySpec
import java.io.File
import java.net.URL
import javax.inject.Inject

annotation class SjlDistConfigMaker

enum class Platform {
    LINUX64,
    WINDOWS64,
    MACOS64
}

enum class JreArchiveType{
    TARGZ{
        override fun toString(): String {
            return "tar.gz"
        }
    },
    ZIP{
        override fun toString(): String {
            return "zip"
        }
    },
    NONE{
        override fun toString(): String {
            return "none"
        }
    }


}

enum class DistributionArchiveType{
    TARGZ{
        override fun toString(): String {
            return "tar.gz"
        }
    },
    ZIP{
        override fun toString(): String {
            return "zip"
        }
    }

}

enum class DistributionType {
    LINUX64_DIRECTORY,
    LINUX64_ARCHIVE,
}

interface JreDownloadHandler {
    fun getJreDownloadUrl(platform:Platform) : Pair<URL,JreArchiveType>
    fun getCheckSumDownloadUrl(platform: Platform):URL?
    fun pickFiles(tempDir: File, copySpec: CopySpec) {
        copySpec.from(tempDir.absolutePath)
    }
}

@SjlDistConfigMaker
open class SjlDistExtension @Inject constructor(private  val project: Project) {
    internal val commonConfig:SjlDistCommonConfig
    internal val distributions = arrayListOf<BaseDistributionConfig>()

    init {
        this.commonConfig= SjlDistCommonConfig(project)
    }

    fun common(configure: SjlDistCommonConfig.()->Unit){
        this.commonConfig.configure()
    }

    fun linux64Directory(name:String, configure:Linux64DirectoryDistributionConfig.()->Unit){
        val dist = Linux64DirectoryDistributionConfig(project, name)
        dist.configure()
        distributions.add(dist)
    }

    fun linux64Archive(name:String, configure:Linux64ArchiveDistributionConfig.()->Unit){
        val dist = Linux64ArchiveDistributionConfig(project, name)
        dist.configure()
        distributions.add(dist)
    }
}

@SjlDistConfigMaker
open class SjlDistCommonConfig(private val project: Project) {
    lateinit var appName:String
    var jreRelativePath: String = "jre"
    var libRelativePath: String = "lib"
    var dependsOnTasks = arrayListOf("jar")
    internal var assetsList = arrayListOf<Pair<String, CopySpec>>()
    internal var jreDownloadUrlResolver: JreDownloadHandler? = null
    fun useCustomJreDownloadUrlResolver(resolver: JreDownloadHandler) {
        jreDownloadUrlResolver = resolver
    }
    fun useAmazonJreDownloadUrlResolver(javaVersion:Int) {
        jreDownloadUrlResolver = AmazonCorrettoJreDownloadUrlResolver(javaVersion)
    }
    fun assets(destDir:String, configure:CopySpec.()->Unit){
        val spec = project.copySpec()
        spec.configure()
        assetsList.add(Pair(destDir, spec))
    }
}

abstract class BaseDistributionConfig(internal val project: Project, val name:String) {
    internal var configLocation:Pair<String,String>? = null
    internal var assetsList = arrayListOf<Pair<String, CopySpec>>()
    internal var executableConfig:ExecutableConfig? = null
    fun assets(destDir:String, configure:CopySpec.()->Unit){
        val spec = project.copySpec()
        spec.configure()
        assetsList.add(Pair(destDir, spec))
    }
    fun config(sourceRelativePath:String, targetRelativePath: String) {
        configLocation = Pair(sourceRelativePath, targetRelativePath)
    }
    fun generateExecutable(configure:ExecutableConfig.()->Unit = {}){
        executableConfig = ExecutableConfig();
        executableConfig!!.configure()
    }
    abstract fun getType():DistributionType
}

@SjlDistConfigMaker
class ExecutableConfig {
    var location: String? = null
}

@SjlDistConfigMaker
open class Linux64DirectoryDistributionConfig (project: Project, name:String): BaseDistributionConfig(project, name) {
    override fun getType() = DistributionType.LINUX64_DIRECTORY
}

@SjlDistConfigMaker
open class Linux64ArchiveDistributionConfig (project: Project, name:String): Linux64DirectoryDistributionConfig(project, name) {
    override fun getType() = DistributionType.LINUX64_ARCHIVE
    var archiveType = DistributionArchiveType.TARGZ
}

