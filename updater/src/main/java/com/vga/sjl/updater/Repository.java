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

import com.vga.sjl.config.impl.SjlYamlConfigurationProvider;
import com.vga.sjl.external.org.snakeyaml.engine.v2.api.*;
import com.vga.sjl.external.org.snakeyaml.engine.v2.nodes.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

@SuppressWarnings("unchecked")
public class Repository {

    private final Configuration config;

    private final Logger logger = LoggerFactory.getLogger(getClass());


    public Repository(Configuration config) {
        this.config = config;
        new Thread(()->{
            try{
                index();
            } catch (Throwable t){
                logger.error("unable to index repository", t);
            }
        }).start();
    }



    public String getIndex() throws Exception {
        index();
        return new String(Files.readAllBytes(new File(config.updaterTempFolder, "index.yml").toPath()), StandardCharsets.UTF_8);
    }

    public synchronized void index() throws Exception {
        File indexFile = new File(config.updaterTempFolder, "index.yml");
        Map<String, Object> existingData = new LinkedHashMap<>();
        if (indexFile.exists()) {
            readData(existingData, indexFile);
        }
        Map<String, Object> newData = new LinkedHashMap<>();
        List<Map<String, String>> libsData = new ArrayList<>();
        newData.put("libs", libsData);
        for (File file : Objects.requireNonNull(config.libFolder.listFiles())) {
            if (file.isDirectory()) {
                continue;
            }
            Map<String, String> item = new HashMap<>();
            libsData.add(item);
            String name = file.getName();
            String size = String.valueOf(file.length());
            String modified = String.valueOf(file.lastModified());
            item.put("name", name);
            item.put("size", size);
            item.put("modified", modified);
            List<Map<String, String>> existingLibsData = (List<Map<String, String>>) existingData.get("libs");
            String checkSum = null;
            if (existingLibsData != null) {
                Map<String, String> eld = existingLibsData.stream().filter(it -> name.equals(it.get("name"))).findFirst().orElse(null);
                if (eld != null && size.equals(eld.get("size")) && modified.equals(eld.get("modified"))) {
                    checkSum = eld.get("checkSum");
                }
            }
            if (checkSum == null) {
                checkSum = Utils.calculateCheckSum(file);
            }
            item.put("checkSum", checkSum);
        }
        if(config.javaFolder != null){
            File sjlFile = new File(config.javaFolder, "sjl.txt");
            newData.put("java", sjlFile.exists()? new String(Files.readAllBytes(sjlFile.toPath())): "undefined");
        }
        if (differs(existingData, newData)) {
            writeData(newData, indexFile);
        }
    }

    private boolean differs(Map<String, Object> existingData, Map<String, Object> newData) {
        if (existingData.size() != newData.size()) {
            return true;
        }
        for (Map.Entry<String, Object> entry : existingData.entrySet()) {
            Object newValue = newData.get(entry.getKey());
            Object eValue = entry.getValue();
            if (newValue == null && eValue == null) {
                continue;
            }
            if ((newValue == null) || (eValue == null)) {
                return true;
            }
            if (!newValue.getClass().equals(eValue.getClass())) {
                return true;
            }
            if (newValue instanceof String) {
                if (!Objects.equals(eValue, newValue)) {
                    return true;
                }
                continue;
            }
            if (newValue instanceof List) {
                if (differs((List<Object>) newValue, (List<Object>) eValue)) {
                    return true;
                }
                continue;
            }
            if (differs((Map<String, Object>) newValue, (Map<String, Object>) eValue)) {
                return true;
            }
        }
        return false;
    }

    private boolean differs(List<Object> newValue, List<Object> eValue) {
        if (newValue.size() != eValue.size()) {
            return true;
        }
        for (int n = 0; n < newValue.size(); n++) {
            Object nv = newValue.get(n);
            Object ev = eValue.get(n);
            if (nv == null && ev == null) {
                continue;
            }
            if ((nv == null) || (ev == null)) {
                return true;
            }
            if (!newValue.getClass().equals(eValue.getClass())) {
                return true;
            }
            if (nv instanceof String) {
                if (!Objects.equals(eValue, newValue)) {
                    return true;
                }
                continue;
            }
            if (nv instanceof List) {
                if (differs((List<Object>) nv, (List<Object>) ev)) {
                    return true;
                }
                continue;
            }
            if (differs((Map<String, Object>) nv, (Map<String, Object>) ev)) {
                return true;
            }
        }
        return false;
    }

    private void writeData(Map<String, Object> data, File indexFile) throws Exception {
        try (OutputStream os = new FileOutputStream(indexFile)) {
            SjlExtYamlOutputStreamWriter writer = new SjlExtYamlOutputStreamWriter(os, StandardCharsets.UTF_8) {
                @Override
                public void processIOException(IOException e) {
                    throw new RuntimeException(e);
                }
            };
            SjlExtDumpSettings settings = SjlExtDumpSettings.builder().build();
            new SjlExtDump(settings).dump(data, writer);
        }
    }

    private void readData(Map<String, Object> existingData, File indexFile) throws Exception {
        try (InputStream is = new FileInputStream(indexFile)) {
            SjlYamlConfigurationProvider.SjlYamlLoad load = new SjlYamlConfigurationProvider.SjlYamlLoad();
            SjlExtMappingNode mappingNode = load.load(is);
            for (SjlExtNodeTuple entry : mappingNode.getValue()) {
                String key = Utils.getStringValue(entry.getKeyNode());
                if ("javaDir".equals(key)) {
                    existingData.put(key, Utils.getStringValue(entry.getValueNode()));
                    continue;
                }
                if ("libs".equals(key)) {
                    List<Map<String, String>> libsData = new ArrayList<>();
                    existingData.put("libs", libsData);
                    SjlExtSequenceNode libs = (SjlExtSequenceNode) entry.getValueNode();
                    for (SjlExtNode entry2 : libs.getValue()) {
                        Map<String, String> nodeData = new HashMap<>();
                        libsData.add(nodeData);
                        SjlExtMappingNode lib2 = (SjlExtMappingNode) entry2;
                        for (SjlExtNodeTuple tuple : lib2.getValue()) {
                            String key2 = Utils.getStringValue(tuple.getKeyNode());
                            if ("name".equals(key2)) {
                                nodeData.put("name", Utils.getStringValue(tuple.getValueNode()));
                                continue;
                            }
                            if ("size".equals(key2)) {
                                nodeData.put("size", Utils.getStringValue(tuple.getValueNode()));
                                continue;
                            }
                            if ("modified".equals(key2)) {
                                nodeData.put("modified", Utils.getStringValue(tuple.getValueNode()));
                                continue;
                            }
                            if ("checkSum".equals(key2)) {
                                nodeData.put("checkSum", Utils.getStringValue(tuple.getValueNode()));
                            }
                        }
                    }
                }
            }
        }
    }




}
