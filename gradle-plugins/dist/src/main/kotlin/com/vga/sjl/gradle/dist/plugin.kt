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

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware

open class SjlDistPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.extensions.create(
            "sjl-dist",
            SjlDistExtension::class.java, target
        )
    }
}

fun Project.dist(configure: SjlDistExtension.() -> Unit) {
    (this as ExtensionAware).extensions.configure("sjl-dist", configure)
    val extension = this.extensions.getByType(SjlDistExtension::class.java)
    extension.distributions.forEach {
        when(it.getType()){
            DistributionType.LINUX64_DIRECTORY -> this.tasks.create("dist-${it.name}", Linux64DirectoryDistTask::class.java, it, extension.commonConfig)
            DistributionType.LINUX64_ARCHIVE -> this.tasks.create("dist-${it.name}", Linux64ArchiveDistTask::class.java, it, extension.commonConfig)
        }
    }
}