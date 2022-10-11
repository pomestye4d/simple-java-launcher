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
import com.vga.sjl.external.org.snakeyaml.engine.v2.api.SjlExtDump;
import com.vga.sjl.external.org.snakeyaml.engine.v2.api.SjlExtDumpSettings;
import com.vga.sjl.external.org.snakeyaml.engine.v2.api.SjlExtYamlOutputStreamWriter;
import com.vga.sjl.external.org.snakeyaml.engine.v2.representer.SjlExtStandardRepresenter;
import com.vga.sjl.restart.DeleteOperation;
import com.vga.sjl.restart.MoveOperation;
import com.vga.sjl.restart.RestartOperation;
import com.vga.sjl.restart.SleepOperation;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApplicationHandler {
    public void updateApp(List<RestartOperation> operations) throws Exception {
        if(!SjlControlThread.isApplicationRunning(Globals.configuration.controlPort)){
            for(RestartOperation operation: operations){
                if(operation instanceof DeleteOperation){
                    DeleteOperation delOp = (DeleteOperation) operation;
                    if(delOp.file.exists()){
                        Utils.delete(delOp.file);
                    }
                    continue;
                }
                if(operation instanceof MoveOperation){
                    MoveOperation moveOp = (MoveOperation) operation;
                    Files.move(moveOp.from.toPath(), moveOp.to.toPath());
                }
            }
            new Thread( ()->{
               try{
                   String[] envs = Globals.configuration.environment.entrySet().stream()
                           .map(it-> String.format("%s=%s", it.getKey(), it.getValue())).toArray(String[]::new);
                   Process process = Runtime.getRuntime().exec(String.format("./%s -background", Globals.configuration.executable.getName()), envs, Globals.configuration.executable.getParentFile());
                   process.waitFor();
               } catch (Throwable t){
                   //noops
               }
            }).start();

            return;
        }

        List<Map<String, String>> nodes = new ArrayList<>();
        operations.forEach(op -> {
            if (op instanceof DeleteOperation) {
                DeleteOperation dop = (DeleteOperation) op;
                Map<String, String> map = new HashMap<>();
                map.put("operation", "delete");
                map.put("file", dop.file.getAbsolutePath());
                nodes.add(map);
            }
            if (op instanceof MoveOperation) {
                MoveOperation mop = (MoveOperation) op;
                Map<String, String> map = new HashMap<>();
                map.put("operation", "move");
                map.put("from", mop.from.getAbsolutePath());
                map.put("to", mop.to.getAbsolutePath());
                nodes.add(map);
            }
            if (op instanceof SleepOperation) {
                SleepOperation sop = (SleepOperation) op;
                Map<String, String> map = new HashMap<>();
                map.put("operation", "sleep");
                map.put("duration", String.valueOf(sop.duration));
                nodes.add(map);
            }
        });
        SjlExtStandardRepresenter representer = new SjlExtStandardRepresenter(SjlExtDumpSettings.builder().build());
        try (OutputStream os = new FileOutputStream(new File(Globals.configuration.tempFolder, "restart.dat"))) {
            SjlExtYamlOutputStreamWriter writer = new SjlExtYamlOutputStreamWriter(os, StandardCharsets.UTF_8) {
                @Override
                public void processIOException(IOException e) {
                    throw new RuntimeException(e);
                }
            };
            new SjlExtDump(SjlExtDumpSettings.builder().build()).dumpNode(representer.represent(nodes), writer);
        }
        boolean restated = SjlControlThread.makeRequest(Globals.configuration.controlPort, new SjlControlThread.RequestHandler() {
            @Override
            public byte[] getRequest() {
                return "RESTART".getBytes();
            }

            @Override
            public boolean handleResponse(String response) {
                return response.startsWith("RESTART");
            }
        });
        if(!restated){
            throw new IllegalStateException("unable to restart application");
        }
    }
}
