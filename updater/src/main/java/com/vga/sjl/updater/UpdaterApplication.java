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

import com.vga.sjl.Application;
import com.vga.sjl.ApplicationCallback;
import com.vga.sjl.config.AppConfiguration;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.Tomcat;
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.io.File;
import java.net.URLDecoder;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UpdaterApplication implements Application {

    private volatile Tomcat tomcat;


    @Override
    public void start(AppConfiguration config, ApplicationCallback callback) throws Exception {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
        Logger.getLogger("").setLevel(Level.FINEST);
        final Configuration configuration = new Configuration(config);
        final Repository repository = new Repository(configuration);
        Globals.configuration = configuration;
        Globals.repository = repository;
        tomcat = new Tomcat();
        File webappDir = new File(configuration.updaterTempFolder, String.format("tomcat-workdir%swebapps", File.separator));
        if(!webappDir.exists()){
            webappDir.mkdirs();
        }
        tomcat.setBaseDir(webappDir.getParentFile().getAbsolutePath());
        tomcat.setPort(Integer.parseInt(config.computeValue("tomcat.port", "8081")));
        tomcat.setHostname("localhost");
        tomcat.getConnector();
        tomcat.getServer().setParentClassLoader(getClass().getClassLoader());
        File war = new File(String.format("lib%swebapp.war",File.separator));
        File dir = new File("webapp");
        StandardContext ctx;
        if(dir.exists()) {
            ctx = (StandardContext) tomcat.addWebapp("", dir.getAbsolutePath());
        } else {
            ctx = (StandardContext) tomcat.addWebapp("",new File(URLDecoder.decode(war.toURI().toURL().getFile(), "UTF-8")).getAbsolutePath());
        }
        ctx.setDelegate(true);
        ctx.setTldValidation(false);
        ctx.setXmlValidation(false);
        ctx.getJarScanner().setJarScanFilter((jarScanType, jarName) -> false);
        tomcat.init();
        tomcat.start();
    }

    @Override
    public void stop() throws Exception {
        tomcat.stop();
        tomcat.destroy();
    }
}
