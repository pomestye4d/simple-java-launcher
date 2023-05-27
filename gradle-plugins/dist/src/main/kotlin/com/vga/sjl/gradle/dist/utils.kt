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
import java.io.File

fun ensureDirectoryExists(dir:File,  clean: Boolean):File {

    if(dir.exists() && clean){
        dir.deleteRecursively()
    }
    if(!dir.exists() && !dir.mkdirs()){
        throw Exception("unable to create ${dir.absolutePath}")
    }
    return dir
}

fun toFile(project: Project, relativePath:String):File{
    return File(project.buildDir, "sjl-workdir/${relativePath}")
}


fun ensureDirectoryExists(project: Project, relativePath:String, clean: Boolean):File {
    val file = toFile(project, relativePath)
    ensureDirectoryExists(file, clean)
    return file
}
