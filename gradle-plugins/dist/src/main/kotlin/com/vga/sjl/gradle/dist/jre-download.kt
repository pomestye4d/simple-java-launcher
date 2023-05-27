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
import java.lang.IllegalArgumentException
import java.lang.IllegalStateException
import java.net.URL
import kotlin.io.path.toPath

class AmazonCorrettoJreDownloadUrlResolver(private val javaVersion:Int) : JreDownloadHandler {

    private fun getArch(platform: Platform):String {
        return when(platform){
            Platform.LINUX64 -> "x64-linux"
            Platform.WINDOWS64 -> "x64-windows"
            Platform.MACOS64 -> "x64-macos"
        }
    }

    private fun getArchiveType(platform: Platform): JreArchiveType {
        return when(platform){
            Platform.WINDOWS64 -> JreArchiveType.ZIP
            else -> JreArchiveType.TARGZ
        }
    }

    override fun getJreDownloadUrl(platform: Platform): Pair<URL, JreArchiveType> {
        return Pair(
            URL("https://corretto.aws/downloads/latest/amazon-corretto-${javaVersion}-${getArch(platform)}-jdk.${getArchiveType(platform)}"),
            getArchiveType(platform))
    }

    override fun getCheckSumDownloadUrl(platform: Platform): URL? {
        return URL("https://corretto.aws/downloads/latest_checksum/amazon-corretto-${javaVersion}-${getArch(platform)}-jdk.${getArchiveType(platform)}")
    }

    override fun pickFiles(tempDir: File, copySpec: CopySpec) {
        copySpec.from(tempDir.absolutePath)
        copySpec.include("**/jre/**")
        copySpec.includeEmptyDirs=false
        copySpec.eachFile {
            val segs = it.relativePath.segments.toList()
            val corrected = segs.subList(2, segs.size)
            it.path = corrected.joinToString(File.separator)
        }
    }
}

private fun updateJreCache(platform: Platform, config: SjlDistCommonConfig, project: Project):File{
    val resolver = config.jreDownloadUrlResolver?:throw IllegalStateException("jre download resolver is not defined")
    val downloadData = resolver.getJreDownloadUrl(platform);
    if(downloadData.second == JreArchiveType.NONE){
        if(downloadData.first.protocol != "file"){
            throw IllegalArgumentException("jre without compression can be only a local folder")
        }
        return downloadData.first.toURI().toPath().toFile()
    }
    val checkSumUrl = resolver.getCheckSumDownloadUrl(platform)
    val jreCache = ensureDirectoryExists(project, "jre-cache/${platform.name}", false)
    val checkSumFile = toFile(project, "jre-cache/${platform.name}-checksum.dat")
    if(checkSumUrl !== null){
        if(jreCache.exists() && checkSumFile.exists() && checkSumFile.readText() == checkSumUrl.readText()){
            return jreCache
        }
        jreCache.listFiles().forEach { it.deleteRecursively() }
    }
    checkSumFile.delete()
    val archiveFile = toFile(project, "jre-cache/archive.${downloadData.second}")
    if(archiveFile.exists()){
        archiveFile.delete()
    }
    val jreTempDir = ensureDirectoryExists(project, "jre-cache/temp-dir", true)
    downloadData.first.openStream().use {ins ->
        archiveFile.outputStream().use {os ->
            ins.copyTo(os)
            os.flush()
        }
    }
    when(downloadData.second){
        JreArchiveType.TARGZ ->{
            project.copy {
                it.from (project.tarTree(project.resources.gzip(archiveFile)))
                it.into(jreTempDir)
            }
        }
        JreArchiveType.ZIP -> {
            project.copy {
                it.from (project.zipTree(archiveFile))
                it.into(jreTempDir)
            }
        }
        JreArchiveType.NONE -> throw IllegalArgumentException();
    }
    if(checkSumUrl != null) {
        checkSumFile.writeText(checkSumUrl.readText())
    }
    project.copy {
        resolver.pickFiles(jreTempDir, it)
        it.into(jreCache)
    }
    return jreCache
}

fun downloadJre(targetDirectory:File, platform: Platform, config: SjlDistCommonConfig, project: Project) {
    val jre = updateJreCache(platform,config, project)
    ensureDirectoryExists(targetDirectory, true)
    jre.copyRecursively(targetDirectory)
}