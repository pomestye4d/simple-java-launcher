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

import org.apache.hc.client5.http.ClientProtocolException;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.EntityBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.snakeyaml.engine.v2.api.Load;
import org.snakeyaml.engine.v2.api.LoadSettings;
import org.snakeyaml.engine.v2.composer.Composer;
import org.snakeyaml.engine.v2.nodes.MappingNode;
import org.snakeyaml.engine.v2.nodes.Node;
import org.snakeyaml.engine.v2.nodes.NodeTuple;
import org.snakeyaml.engine.v2.nodes.ScalarNode;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Utils {

    public static void println(String text){
        System.out.println(text);
    }

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
    public static String getStringValue(Node keyNode) {
        return ((ScalarNode) keyNode).getValue();
    }

    public static String getStringValue(MappingNode mappingNode, String key) {
        NodeTuple tuple = mappingNode.getValue().stream().filter(it -> key.equals(getStringValue(it.getKeyNode()))).findFirst().orElse(null);
        return tuple == null? null: getStringValue(tuple.getValueNode());
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

    public static class YamlLoad<T> extends Load {

        public YamlLoad() {
            super(createSettings());
        }

        private static LoadSettings createSettings() {
            return LoadSettings.builder().setParseComments(true).build();
        }

        public T load(InputStream is){
            Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8);
            Composer composer = createComposer(reader);
            return (T) composer.getSingleNode().orElse(null);
        }
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

    public static String requestString(String url) throws IOException {
        try(CloseableHttpClient httpclient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(url);
            final HttpClientResponseHandler<String> responseHandler = response -> {
                final int status = response.getCode();
                if (status >= HttpStatus.SC_SUCCESS && status < HttpStatus.SC_REDIRECTION) {
                    final HttpEntity entity = response.getEntity();
                    try {
                        return entity != null ? EntityUtils.toString(entity) : null;
                    } catch (final ParseException ex) {
                        throw new ClientProtocolException(ex);
                    }
                } else {
                    throw new ClientProtocolException("Unexpected response status: " + status);
                }
            };
            return  httpclient.execute(httpPost, responseHandler);
        }
    }
    public static void uploadFile(String url, File file) throws IOException {
        try(CloseableHttpClient httpclient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(url);
            httpPost.setEntity(EntityBuilder.create().setFile(file).build());
            final HttpClientResponseHandler<Void> responseHandler = response -> {
                final int status = response.getCode();
                if (status >= HttpStatus.SC_SUCCESS && status < HttpStatus.SC_REDIRECTION) {
                    return null;
                } else {
                    throw new ClientProtocolException("Unexpected response status: " + status);
                }
            };
            httpclient.execute(httpPost, responseHandler);
        }
    }
    public static void uploadString(String url, String content) throws IOException {
        try(CloseableHttpClient httpclient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(url);
            httpPost.setEntity(EntityBuilder.create().setStream(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8))).build());
            final HttpClientResponseHandler<Void> responseHandler = response -> {
                final int status = response.getCode();
                if (status >= HttpStatus.SC_SUCCESS && status < HttpStatus.SC_REDIRECTION) {
                    return null;
                } else {
                    throw new ClientProtocolException("Unexpected response status: " + status);
                }
            };
            httpclient.execute(httpPost, responseHandler);
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
}
