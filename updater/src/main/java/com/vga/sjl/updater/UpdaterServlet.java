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

import com.vga.sjl.control.SjlControlThread;
import com.vga.sjl.external.org.snakeyaml.engine.v2.api.SjlExtLoad;
import com.vga.sjl.external.org.snakeyaml.engine.v2.api.SjlExtLoadSettings;
import com.vga.sjl.external.org.snakeyaml.engine.v2.nodes.SjlExtMappingNode;
import com.vga.sjl.external.org.snakeyaml.engine.v2.nodes.SjlExtNode;
import com.vga.sjl.external.org.snakeyaml.engine.v2.nodes.SjlExtSequenceNode;
import com.vga.sjl.restart.DeleteOperation;
import com.vga.sjl.restart.MoveOperation;
import com.vga.sjl.restart.RestartOperation;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class UpdaterServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        if(pathInfo == null){
            super.doPost(req, resp);
            return;
        }
        try {
            if (pathInfo.contains("getState")) {
                doGetState(resp);
                return;
            }
            if (pathInfo.contains("getIndex")) {
                doGetIndex(resp);
                return;
            }
            if (pathInfo.contains("uploadLib")) {
                doUploadLib(req);
                return;
            }
            if (pathInfo.contains("uploadJava")) {
                doUploadJava(req);
                return;
            }
            if (pathInfo.contains("restartApp")) {
                doRestartApp(req);
            }
        } catch (Throwable t){
            throw  new ServletException(t);
        }
    }

    private void doGetState(HttpServletResponse resp) throws IOException {
        String result = SjlControlThread.isApplicationRunning(Globals.configuration.controlPort)? "RUNNING": "NOT_RUNNING";
        try(OutputStreamWriter os = new OutputStreamWriter(resp.getOutputStream(), StandardCharsets.UTF_8)){
            os.write(result);
            os.flush();
        }
    }

    private void doRestartApp(HttpServletRequest req) throws Exception {
        List<RestartOperation> operations = new ArrayList<>();
        try (InputStream is = req.getInputStream()) {
            Utils.YamlLoad<SjlExtSequenceNode> load = new Utils.YamlLoad<>();
            SjlExtSequenceNode sequenceNode = load.load(is);
            for (SjlExtNode item : sequenceNode.getValue()) {
                SjlExtMappingNode mappingNode = (SjlExtMappingNode) item;
                String operation = Utils.getStringValue(mappingNode, "operation");
                if("deleteLib".equals(operation)){
                    operations.add(new DeleteOperation(new File(Globals.configuration.libFolder, Objects.requireNonNull(Utils.getStringValue(mappingNode, "file")))));
                    continue;
                }
                if("deleteJava".equals(operation)){
                    operations.add(new DeleteOperation(Globals.configuration.javaFolder));
                    continue;
                }
                if("moveLib".equals(operation)){
                    String fileName = Utils.getStringValue(mappingNode, "file");
                    assert fileName != null;
                    operations.add(new MoveOperation(new File(Globals.configuration.updaterTempFolder, fileName),new File(Globals.configuration.libFolder, fileName)));
                    continue;
                }
                if("moveJava".equals(operation)){
                    operations.add(new MoveOperation(new File(Globals.configuration.updaterTempFolder, "java"), Globals.configuration.javaFolder));
                }
            }
        }
        Globals.applicationHandler.updateApp(operations);
    }

    private void doUploadJava(HttpServletRequest req) throws Exception {
        File javaDir = new File(Globals.configuration.updaterTempFolder, "java");
        Utils.delete(javaDir);
        if(!javaDir.mkdirs()){
            throw new IllegalStateException("unable to create directory " + javaDir.getAbsolutePath());
        }
        Utils.unzip(req.getInputStream(), javaDir);
    }

    private void doUploadLib(HttpServletRequest req) throws Exception {
        String fileName = URLDecoder.decode(req.getPathInfo().substring(req.getPathInfo().lastIndexOf("/")), "UTF-8");
        File file = new File(Globals.configuration.updaterTempFolder, fileName);
        Utils.delete(file);
        try(OutputStream os = new FileOutputStream(file)){
            Utils.copy(req.getInputStream(), os);
        }
    }

    private void doGetIndex(HttpServletResponse resp) throws Exception {
        String index = Globals.repository.getIndex();
        try(OutputStreamWriter os = new OutputStreamWriter(resp.getOutputStream(), StandardCharsets.UTF_8)){
            os.write(index);
            os.flush();
        }
    }


}
