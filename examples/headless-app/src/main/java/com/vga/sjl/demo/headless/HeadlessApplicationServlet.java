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

package com.vga.sjl.demo.headless;

import com.vga.sjl.restart.MoveOperation;
import com.vga.sjl.restart.RestartOperation;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class HeadlessApplicationServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = req.getPathInfo();
        File versionFile = new File("temp/version.txt");
        if(path.contains("version")){
            if(!versionFile.exists()){
                Files.write(versionFile.toPath(), "1".getBytes(StandardCharsets.UTF_8));
            }
            write(resp, new String(Files.readAllBytes(versionFile.toPath())));
        }
        if(path.contains("update")){
            new Thread(()->{
                try {
                    Thread.sleep(3000L);
                    System.out.println("updating");
                    List<RestartOperation> operations = new ArrayList<>();
                    int version = Integer.parseInt(new String(Files.readAllBytes(versionFile.toPath())));
                    version++;
                    File versionFile2 = new File("temp/version2.txt");
                    Files.write(versionFile.toPath(), String.valueOf(version).getBytes(StandardCharsets.UTF_8));
                    operations.add(new MoveOperation(versionFile2, versionFile));
                    HeadlessApplication.applicationCallback.restart(operations);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    private void write(HttpServletResponse resp, String value) throws IOException {
        try(OutputStreamWriter sw = new OutputStreamWriter(resp.getOutputStream())){
            sw.write(value);
            sw.flush();
        }
    }
}
