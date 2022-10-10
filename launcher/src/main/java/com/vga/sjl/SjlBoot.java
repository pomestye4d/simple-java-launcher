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

package com.vga.sjl;

import com.vga.sjl.config.AppConfiguration;
import com.vga.sjl.control.SjlControlThread;
import com.vga.sjl.external.org.snakeyaml.engine.v2.api.SjlExtDump;
import com.vga.sjl.external.org.snakeyaml.engine.v2.api.SjlExtDumpSettings;
import com.vga.sjl.external.org.snakeyaml.engine.v2.api.SjlExtYamlOutputStreamWriter;
import com.vga.sjl.external.org.snakeyaml.engine.v2.representer.SjlExtStandardRepresenter;
import com.vga.sjl.restart.DeleteOperation;
import com.vga.sjl.restart.MoveOperation;
import com.vga.sjl.restart.RestartOperation;
import com.vga.sjl.restart.SleepOperation;

import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.channels.FileLock;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

public class SjlBoot {
    private static final Object lock = new Object();

    public static void main(String[] args) throws Exception {
        String configFileName = System.getenv("sjl.configFile");
        if (configFileName == null) {
            configFileName = System.getProperty("sjl.configFile");
        }
        if (configFileName == null && new File(String.format("config%sconfig.yaml",File.pathSeparator)).exists()) {
            configFileName = String.format("config%sconfig.yaml",File.pathSeparator);
        }
        if (configFileName == null && new File(String.format("config%sconfig.yml",File.pathSeparator)).exists()) {
            configFileName = String.format("config%sconfig.yml",File.pathSeparator);
        }
        if (configFileName == null && new File("config.yaml").exists()) {
            configFileName = "config.yaml";
        }
        if (configFileName == null && new File("config.yml").exists()) {
            configFileName = "config.yml";
        }
        if (configFileName == null && new File(String.format("config%sconfig.properties",File.pathSeparator)).exists()) {
            configFileName = String.format("config%sconfig.properties",File.pathSeparator);
        }
        if (configFileName == null && new File("config.properties").exists()) {
            configFileName = "config.properties";
        }
        if (configFileName == null) {
            throw new IllegalArgumentException("Config file is not found");
        }
        if (!configFileName.endsWith(".yaml") && !configFileName.endsWith(".yml") && !configFileName.endsWith(".properties")) {
            throw new IllegalArgumentException(String.format("Config file %s has wrong extension", configFileName));
        }
        Logger logger = Logger.getLogger(SjlBoot.class.getName());
        File configFile = new File(configFileName);
        if (!configFile.exists()) {
            throw new IllegalArgumentException(String.format("Config file %s does not exist", configFile.getAbsolutePath()));
        }
        AppConfiguration config;
        if (configFileName.endsWith(".properties")) {
            config = AppConfiguration.fromProperties(configFile);
        } else {
            config = AppConfiguration.fromYaml(configFile);
        }
        List<String> argsList = Arrays.asList(args);
        int port = Integer.parseInt(config.computeValue("sjl.controlPort", "21566"));
        if(argsList.contains("stop")){
            if(SjlControlThread.isApplicationRunning(port)){
                if(!SjlControlThread.stopRunningApplication(port)){
                    throw new Exception("unable to stop application");
                }
            }
            return;
        }
        if(argsList.contains("status")){
            if(SjlControlThread.isApplicationRunning(port)){
                System.exit(0);
            }
            System.exit(1);
        }
        File libFolder = new File(config.computeValue("sjl.libFolder", "lib"));
        String applicationClass = config.computeValue("sjl.applicationClass", null);
        if (applicationClass == null) {
            throw new IllegalArgumentException("application class is not defined");
        }
        File tempDirectory = new File(config.computeValue("sjl.tempDirectory", "temp"));
        if(!tempDirectory.exists() && !tempDirectory.mkdirs()){
            throw new IllegalArgumentException("unable to create temp directory " + tempDirectory.getAbsolutePath());
        }
        File tempFile = new File(tempDirectory, "lock.tmp");
        FileLock fileLock = acquireLock(tempFile);
        String externalsFileName = config.computeValue("sjl.externalsFileName", "lib/externals.txt");
        List<URL> urls = new ArrayList<>();
        if(libFolder.exists()) {
            File externalsFile = new File(libFolder, externalsFileName);
            if (externalsFile.exists()) {
                try (InputStream is = new FileInputStream(externalsFile)) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
                    String line = reader.readLine();
                    while (line != null) {
                        urls.add(new File(line).toURI().toURL());
                        line = reader.readLine();
                    }

                }
            }
            File[] files = libFolder.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && file.getName().endsWith(".jar")) {
                        urls.add(file.toURI().toURL());
                    }
                }
            }
        }
        ClassLoader cl = new URLClassLoader(urls.toArray(new URL[0]), SjlBoot.class.getClassLoader());
        Application app = (Application) cl.loadClass(applicationClass).getConstructor().newInstance();

        AtomicReference<Boolean> stopped = new AtomicReference<>(false);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> stopApplication(app, stopped)));
        try {
            app.start(config, new ApplicationCallback() {
                @Override
                public void stop() {
                    stopApplication(app, stopped);
                    System.exit(0);
                }

                @Override
                public void restart(List<RestartOperation> operations) {
                    if (operations.size() > 0) {
                        try {
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
                            try (OutputStream os = new FileOutputStream(new File(tempDirectory, "restart.dat"))) {
                                SjlExtYamlOutputStreamWriter writer = new SjlExtYamlOutputStreamWriter(os, StandardCharsets.UTF_8) {
                                    @Override
                                    public void processIOException(IOException e) {
                                        throw new RuntimeException(e);
                                    }
                                };
                                new SjlExtDump(SjlExtDumpSettings.builder().build()).dumpNode(representer.represent(nodes), writer);
                            }
                        } catch (Throwable t) {
                            logger.severe("unable to write restart instructions");
                        }
                    }
                    stopApplication(app, stopped);
                    System.exit(2);
                }
            });
        } catch (Exception e) {
            stopApplication(app, stopped);
            try {
                fileLock.release();
                fileLock.channel().close();
                if (tempFile.exists() && !tempFile.delete()) {
                    throw new Exception("unable to delete temp file " + tempFile);
                }
            } catch (Exception e2) {
                logger.severe("unable to release lock");
            }
            throw e;
        }
        if(argsList.contains("-background")){
            new SjlControlThread(port, ()->{
                stopApplication(app, stopped);
                return null;
            }, fileLock, new File(tempDirectory, "lock.tmp")).start();
            logger.info("application started in background mode");
            return;
        }
        System.out.println("Press 'q' key to exit.");
        int c;
        do {
            try {
                c = System.in.read();
            } catch (IOException e) {
                break;
            }
        } while ('q' != (char) c && 'Q' != (char) c);
        try {
            stopApplication(app,stopped);
        } finally {
            SjlControlThread.releaseLock(fileLock, tempFile);
        }
    }

    private static FileLock acquireLock(File tempFile) throws Exception {
        File tempDir = tempFile.getParentFile();
        if (!tempDir.exists() && !tempDir.mkdirs()) {
            throw new Exception("unable to create dir " + tempDir.getAbsolutePath());
        }
        FileLock result;
        try {
            if (!tempFile.exists() && !tempFile.createNewFile()) {
                throw new Exception("unable to create temp file");
            }
            result = new RandomAccessFile(tempFile, "rwd").getChannel().tryLock();
            if(result != null){
                tempFile.deleteOnExit();
            }
        } catch (Exception e) {
            throw new Exception(
                    "Another instance of the application is running. Please terminate and try again.", e);
        }
        if (result == null) {
            throw new Exception(
                    "Another instance of the application is running. Please terminate and try again.");
        }
        return result;
    }

    private static void stopApplication(Application app, AtomicReference<Boolean> stopped) {
        Logger logger = Logger.getLogger(SjlBoot.class.getName());
        synchronized (lock) {
            if (!stopped.get()) {
                logger.info("stopping application");
                stopped.set(true);
                try {
                    app.stop();
                    logger.info("application is stopped");
                } catch (Throwable e) {
                    logger.warning(SjlControlThread.prepareLog("unable to stop application", e));
                }
            }
            lock.notifyAll();
        }
    }
}
