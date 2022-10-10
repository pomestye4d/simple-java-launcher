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

import com.vga.sjl.external.org.snakeyaml.engine.v2.api.SjlExtLoad;
import com.vga.sjl.external.org.snakeyaml.engine.v2.api.SjlExtLoadSettings;
import com.vga.sjl.external.org.snakeyaml.engine.v2.composer.SjlExtComposer;
import com.vga.sjl.external.org.snakeyaml.engine.v2.nodes.SjlExtMappingNode;
import com.vga.sjl.external.org.snakeyaml.engine.v2.nodes.SjlExtNode;
import com.vga.sjl.external.org.snakeyaml.engine.v2.nodes.SjlExtNodeTuple;
import com.vga.sjl.external.org.snakeyaml.engine.v2.nodes.SjlExtScalarNode;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Utils {
    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    private static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static String calculateCheckSum(File file) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        if (file.getName().endsWith(".zip") || file.getName().endsWith(".jar") || file.getName().endsWith(".war")) {
            try (FileInputStream fis = new FileInputStream(file)) {
                try (ZipInputStream zipStrm = new ZipInputStream(fis)) {
                    updateZipDigest(md, zipStrm);
                }
            }
        } else {
            try (FileInputStream fis = new FileInputStream(file)) {
                updateDigest(md, fis);
            }
        }
        byte[] digest = md.digest();
        return bytesToHex(digest);
    }

    private static void updateZipDigest(final MessageDigest md,
                                        final ZipInputStream strm) throws IOException {
        ZipEntry entry;
        while ((entry = strm.getNextEntry()) != null) {
            String entryName = entry.getName();
            md.update(entryName.getBytes(StandardCharsets.UTF_8));
            if (entryName.endsWith("/")) {
                continue;
            }
            entryName = entryName.toLowerCase();
            if (entryName.endsWith(".zip") || entryName.endsWith(".jar") || entryName.endsWith(".war")) {
                updateZipDigest(md, new ZipInputStream(strm));
            } else {
                updateDigest(md, strm);
            }
        }
    }

    private static void updateDigest(final MessageDigest md,
                                     final InputStream strm) throws IOException {
        byte[] buf = new byte[256];
        int len;
        while ((len = strm.read(buf)) != -1) {
            md.update(buf, 0, len);
        }
    }

    public static void delete(File file) throws IOException {
        if(!file.exists()){
            return;
        }
        if(file.isDirectory()){
            File[] allContents = file.listFiles();
            if (allContents != null) {
                for (File f2 : allContents) {
                    delete(f2);
                }
            }
        }
        if (!file.delete()) {
            throw new IOException("unable to delete file " + file.getAbsolutePath());
        }
    }
    public static void copy(InputStream is, OutputStream os) throws IOException {
        byte[] buf = new byte[256];
        int len;
        while ((len = is.read(buf)) != -1) {
            os.write(buf, 0, len);
        }
        os.flush();
    }

    private static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());

        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }

    public  static void unzip(InputStream is, File destDir) throws IOException {
        try (ZipInputStream zipStrm = new ZipInputStream(is)) {
            ZipEntry zipEntry = zipStrm.getNextEntry();
            while (zipEntry != null) {
                File newFile = newFile(destDir, zipEntry);
                if (zipEntry.isDirectory()) {
                    if (!newFile.isDirectory() && !newFile.mkdirs()) {
                        throw new IOException("Failed to create directory " + newFile);
                    }
                } else {
                    // fix for Windows-created archives
                    File parent = newFile.getParentFile();
                    if (!parent.isDirectory() && !parent.mkdirs()) {
                        throw new IOException("Failed to create directory " + parent);
                    }

                    FileOutputStream fos = new FileOutputStream(newFile);
                    copy(zipStrm, fos);
                }
                zipEntry = zipStrm.getNextEntry();
            }
        }
    }

    public static String getStringValue(SjlExtNode keyNode) {
        return ((SjlExtScalarNode) keyNode).getValue();
    }

    public static String getStringValue(SjlExtMappingNode mappingNode, String key) {
        SjlExtNodeTuple tuple = mappingNode.getValue().stream().filter(it -> key.equals(getStringValue(it.getKeyNode()))).findFirst().orElse(null);
        return tuple == null? null: getStringValue(tuple.getValueNode());
    }

    public static class YamlLoad<T> extends SjlExtLoad {

        public YamlLoad() {
            super(createSettings());
        }

        private static SjlExtLoadSettings createSettings() {
            return SjlExtLoadSettings.builder().setParseComments(true).build();
        }

        public T load(InputStream is){
            Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8);
            SjlExtComposer composer = createComposer(reader);
            return (T) composer.getSingleNode().orElse(null);
        }
    }
}
