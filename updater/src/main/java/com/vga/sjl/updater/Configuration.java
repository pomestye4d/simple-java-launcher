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

package com.vga.sjl.updater;

import com.vga.sjl.config.AppConfiguration;
import com.vga.sjl.config.model.ConfigurationNode;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

public class Configuration {

    public final File libFolder;

    public final File javaFolder;

    public final File updaterTempFolder;

    public final File executable;

    public final int controlPort;

    public final File tempFolder;

    public final Map<String,String> environment = new LinkedHashMap<>();

    public Configuration(AppConfiguration appConfig)  {
        updaterTempFolder = new File(appConfig.computeValue("sjl.tempDirectory", "temp"));
        if(!updaterTempFolder.exists() && !updaterTempFolder.mkdirs()){
            throw new IllegalArgumentException("unable to create temp directory " + updaterTempFolder.getAbsolutePath());
        }
        String exec = appConfig.computeValue("app.executable", null);
        if(exec == null){
            throw new IllegalArgumentException("executable is not defined");
        }
        executable = new File(exec);
        if(!executable.exists()){
            throw new IllegalArgumentException(String.format("executable %s does not exist", executable.getAbsolutePath()));
        }
        String libFolderStr = appConfig.computeValue("app.libFolder", null);
        if(libFolderStr == null){
            throw new IllegalArgumentException("lib folder is not defined");
        }
        libFolder = new File(libFolderStr);
        if(!libFolder.exists()){
            throw new IllegalArgumentException(String.format("lib folder %s does not exist", libFolder.getAbsolutePath()));
        }
        String tempFolderStr = appConfig.computeValue("app.tempFolder", null);
        if(tempFolderStr == null){
            throw new IllegalArgumentException("temp folder is not defined");
        }
        tempFolder = new File(tempFolderStr);
        if(!tempFolder.exists() && !tempFolder.mkdirs()){
            throw new IllegalArgumentException(String.format("unable to create temp folder %s", tempFolder.getAbsolutePath()));
        }
        String javaFolderStr = appConfig.computeValue("app.javaFolder", null);
        if(javaFolderStr != null){
            javaFolder = new File(javaFolderStr);
            if(!javaFolder.exists()){
                throw new IllegalArgumentException(String.format("java folder %s does not exist", javaFolder.getAbsolutePath()));
            }
        } else {
            javaFolder = null;
        }
        controlPort = Integer.parseInt(appConfig.computeValue("app.controlPort", "21566"));
        ConfigurationNode env = appConfig.getSubConfiguration("app.environment");
        if(env != null){
            for(String propertyName: env.getPropertyNames()){
                environment.put(propertyName, env.getValue(propertyName));
            }
        }
    }
}
