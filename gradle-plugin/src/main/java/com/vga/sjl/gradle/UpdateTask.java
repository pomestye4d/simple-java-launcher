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

package com.vga.sjl.gradle;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskAction;
import org.snakeyaml.engine.v2.api.Dump;
import org.snakeyaml.engine.v2.api.DumpSettings;
import org.snakeyaml.engine.v2.api.YamlOutputStreamWriter;
import org.snakeyaml.engine.v2.nodes.MappingNode;
import org.snakeyaml.engine.v2.nodes.Node;
import org.snakeyaml.engine.v2.nodes.NodeTuple;
import org.snakeyaml.engine.v2.nodes.SequenceNode;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class UpdateTask extends DefaultTask {
    @Internal
    private File localLibsDirectory;

    @Internal
    private File embeddedJava;

    @Internal
    private String remoteHost;

    @Internal
    private int remotePort;

    @Internal
    private int startWaitTime = 30;

    public void setLocalLibsDirectory(File localLibsDirectory) {
        this.localLibsDirectory = localLibsDirectory;
    }

    public String getRemoteHost() {
        return remoteHost;
    }

    public void setRemoteHost(String remoteHost) {
        this.remoteHost = remoteHost;
    }

    public int getRemotePort() {
        return remotePort;
    }

    public void setRemotePort(int remotePort) {
        this.remotePort = remotePort;
    }

    public File getLocalLibsDirectory() {
        return localLibsDirectory;
    }

    public int getStartWaitTime() {
        return startWaitTime;
    }

    public void setStartWaitTime(int startWaitTime) {
        this.startWaitTime = startWaitTime;
    }

    public void setEmbeddedJava(File embeddedJava) {
        this.embeddedJava = embeddedJava;
    }

    public File getEmbeddedJava() {
        return embeddedJava;
    }

    @TaskAction
    public void doUpdate() throws Exception{
        if(localLibsDirectory == null){
            throw new IllegalArgumentException("local libs directory is not defined");
        }
        if(!localLibsDirectory.exists()){
            throw new IllegalArgumentException(String.format("local lib directory %s does not exist", localLibsDirectory.getAbsolutePath()));
        }
        Utils.println("indexing local repository");
        Map<String,Object> localIndex = loadIndex();
        Utils.println("getting index of remote repository");
        String remoteIndexStr = Utils.requestString(String.format("http://%s:%s/getIndex",remoteHost, remotePort));
        Map<String,Object> remoteIndex = new LinkedHashMap<>();
        readData(remoteIndex, new ByteArrayInputStream(remoteIndexStr.getBytes(StandardCharsets.UTF_8)));
        boolean uploadJava = false;
        List<File> libsUpload = new ArrayList<>();
        List<Map<String, String>> remoteOperations = new ArrayList<>();
        List<Map<String,String>> localLibs = (List<Map<String, String>>) localIndex.get("libs");
        List<Map<String,String>> remoteLibs = (List<Map<String, String>>) remoteIndex.get("libs");
        for(Map<String, String> localLib: localLibs){
            Map<String,String> remoteLib = remoteLibs.stream().filter(it -> localLib.get("name").equals(it.get("name"))).findFirst().orElse(null);
            if(remoteLib != null && remoteLib.get("size").equals(localLib.get("size")) && remoteLib.get("checkSum").equals(localLib.get("checkSum"))){
                remoteLibs.remove(remoteLib);
                continue;
            }
            if(remoteLib != null){
                remoteLibs.remove(remoteLib);
                Map<String, String> operation = new LinkedHashMap<>();
                operation.put("operation", "deleteLib");
                operation.put("file", localLib.get("name"));
                remoteOperations.add(operation);
            }
            libsUpload.add(new File(localLibsDirectory, localLib.get("name")));
            Map<String, String> operation = new LinkedHashMap<>();
            operation.put("operation", "moveLib");
            operation.put("file", localLib.get("name"));
            remoteOperations.add(operation);
        }
        for(Map<String, String> remoteLib: remoteLibs){
            Map<String, String> operation = new LinkedHashMap<>();
            operation.put("operation", "deleteLib");
            operation.put("file", remoteLib.get("name"));
            remoteOperations.add(operation);
        }
        {
            String java = (String) localIndex.get("java");
            if(java != null && !"undefined".equals(java) && !java.equals(remoteIndex.get("java"))){
                uploadJava = true;
                {
                    Map<String,String> operation = new HashMap<>();
                    operation.put("operation", "deleteJava");
                    remoteOperations.add(operation);
                }
                {
                    Map<String,String> operation = new HashMap<>();
                    operation.put("operation", "moveJava");
                    remoteOperations.add(operation);
                }
            }
        }
        if(remoteOperations.isEmpty()){
            Utils.println("nothing to update");
            return;
        }
        if(uploadJava){
            Utils.println("uploading java");
            Utils.uploadFile(String.format("http://%s:%s/uploadJava",remoteHost, remotePort), embeddedJava);
        }
        for(File lib: libsUpload){
            Utils.println("uploading lib file " + lib.getName());
            Utils.uploadFile(String.format("http://%s:%s/uploadLib/%s",remoteHost, remotePort, URLEncoder.encode(lib.getName(), "UTF-8")), lib);
        }
        long started = System.currentTimeMillis();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        YamlOutputStreamWriter writer = new YamlOutputStreamWriter(os, StandardCharsets.UTF_8) {
                @Override
                public void processIOException(IOException e) {
                    throw new RuntimeException(e);
                }
            };
        DumpSettings settings = DumpSettings.builder().build();
        new Dump(settings).dump(remoteOperations, writer);
        Utils.println("restarting application");
        Utils.uploadString(String.format("http://%s:%s/restartApp",remoteHost, remotePort), new String(os.toByteArray(), StandardCharsets.UTF_8));
        while (System.currentTimeMillis() - started < TimeUnit.SECONDS.toMillis(startWaitTime)){
            try{
                String content = Utils.requestString(String.format("http://%s:%s/getState",remoteHost, remotePort));
                if("RUNNING".equals(content)){
                    Utils.println("application is successfully updated");
                    return;
                }
            } catch (Exception e){
                //noops
            }
            Utils.println("waiting for application to start");
            Thread.sleep(5000L);
        }
        throw  new IllegalStateException("unable to start application");
    }

    @SuppressWarnings("unchecked")
    public Map<String,Object> loadIndex() throws Exception {
        File tempFolder = new File(getProject().getProjectDir(), "temp");
        if(!tempFolder.exists() && !tempFolder.mkdirs()){
            throw new IOException("unable to create temp directory " + tempFolder.getAbsolutePath());

        }
        File indexFile = new File(tempFolder, "index.yml");
        Map<String, Object> existingData = new LinkedHashMap<>();
        if (indexFile.exists()) {
            try(FileInputStream fis = new FileInputStream(indexFile)){
                readData(existingData, fis);
            }
        }
        Map<String, Object> newData = new LinkedHashMap<>();
        List<Map<String, String>> libsData = new ArrayList<>();
        newData.put("libs", libsData);
        for (File file : Objects.requireNonNull(localLibsDirectory.listFiles())) {
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
        if(embeddedJava != null){
            File sjlFile = new File(embeddedJava, "sjl.txt");
            newData.put("java", sjlFile.exists()? new String(Files.readAllBytes(sjlFile.toPath())): "undefined");
        }
        if (differs(existingData, newData)) {
            writeData(newData, indexFile);
        }
        return newData;
    }

    private void readData(Map<String, Object> existingData, InputStream is) throws Exception {
            Utils.YamlLoad<MappingNode> load = new Utils.YamlLoad<>();
            MappingNode mappingNode = load.load(is);
            for (NodeTuple entry : mappingNode.getValue()) {
                String key = Utils.getStringValue(entry.getKeyNode());
                if ("java".equals(key)) {
                    existingData.put(key, Utils.getStringValue(entry.getValueNode()));
                    continue;
                }
                if ("libs".equals(key)) {
                    List<Map<String, String>> libsData = new ArrayList<>();
                    existingData.put("libs", libsData);
                    SequenceNode libs = (SequenceNode) entry.getValueNode();
                    for (Node entry2 : libs.getValue()) {
                        Map<String, String> nodeData = new HashMap<>();
                        libsData.add(nodeData);
                        MappingNode lib2 = (MappingNode) entry2;
                        for (NodeTuple tuple : lib2.getValue()) {
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
    @SuppressWarnings("unchecked")
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
            YamlOutputStreamWriter writer = new YamlOutputStreamWriter(os, StandardCharsets.UTF_8) {
                @Override
                public void processIOException(IOException e) {
                    throw new RuntimeException(e);
                }
            };
            DumpSettings settings = DumpSettings.builder().build();
            new Dump(settings).dump(data, writer);
        }
    }
}
