/*
 * Copyright (c) 1995, 2020, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package com.vga.sjl.config.impl;

import com.vga.sjl.config.model.ConfigurationNode;
import com.vga.sjl.config.model.ConfigurationProvider;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class SjlPropertiesConfigurationProvider implements ConfigurationProvider {
    private volatile SjlConfigurationNodeImpl rootNode;

    private final List<Object> allLines = new ArrayList<>();

    private static final int SOFT_MAX_ARRAY_LENGTH = Integer.MAX_VALUE - 8;

    public SjlPropertiesConfigurationProvider(File file) throws IOException {
        try(InputStream is = new FileInputStream(file)){
            init(is);
        }
    }

    public SjlPropertiesConfigurationProvider(InputStream is) throws IOException {
        init(is);
    }

    private synchronized void init(InputStream is) throws IOException {
        List<Object> lines = load0(new LineReader(new InputStreamReader(is, StandardCharsets.UTF_8)));
        allLines.clear();
        allLines.addAll(lines);
        updateRootNode();
    }

    private void updateRootNode() {
        SjlConfigurationNodeImpl result = new SjlConfigurationNodeImpl();
        for(Object line: allLines){
            if(line instanceof String){
                continue;
            }
            PropertyLine propertyLine = (PropertyLine) line;
            String propertyName = propertyLine.propertyName;
            String[] parts = propertyName.split("\\.");
            SjlConfigurationNodeImpl node = result;
            for(int n=0; n < parts.length-1; n++){
                Object content = node.map.get(parts[n]);
                if(content instanceof SjlConfigurationNodeImpl){
                    node = (SjlConfigurationNodeImpl) content;
                    continue;
                }
                content = new SjlConfigurationNodeImpl();
                node.map.put(parts[n], content);
                node = (SjlConfigurationNodeImpl) content;
            }
            String lastPart = parts[parts.length-1];
            node.map.put(lastPart, propertyLine.propertyValue);
        }
        rootNode =result;
    }

    @Override
    public ConfigurationNode getConfiguration() {
        return rootNode;
    }

    @Override
    public synchronized void setProperty(String propertyName, String value) {
        String normalizedPropertyName = propertyName;
        if(propertyName != null){
            normalizedPropertyName = propertyName.trim();
            if(normalizedPropertyName.length() == 0){
                normalizedPropertyName = null;
            }
        }
        if(normalizedPropertyName == null){
            throw new IllegalArgumentException("property name must be non empty string");
        }
        String normalizedPropertyValue = value;
        if(normalizedPropertyValue != null){
            normalizedPropertyValue = value.trim();
            if(normalizedPropertyValue.length() == 0){
                normalizedPropertyValue = null;
            }
        }
        boolean found = false;
        for (Object obj : allLines) {
            if (obj instanceof String) {
                continue;
            }

            if (!found && (obj instanceof PropertyLine)) {
                PropertyLine existingLine = (PropertyLine) obj;
                if(normalizedPropertyName.equals(existingLine.propertyName)) {
                    existingLine.propertyValue = normalizedPropertyValue;
                    found = true;
                }
            }
        }
        if(!found){
            allLines.add(new PropertyLine(normalizedPropertyName, normalizedPropertyValue));
        }
        updateRootNode();
    }

    @Override
    public synchronized void save(OutputStream os) throws IOException {
        Writer writer = new OutputStreamWriter(os, StandardCharsets.UTF_8);
        for(Object line: allLines){
            if(line instanceof String){
                writer.write((String) line);
            } else {
                PropertyLine propertyLine = (PropertyLine) line;
                writer.write(String.format("%s=%s", propertyLine.propertyName, propertyLine.propertyValue != null? propertyLine.propertyValue: ""));
            }
            writer.write("\n");
        }
        writer.flush();
    }

    private List<Object> load0(LineReader lr) throws IOException {
        List<Object> result = new ArrayList<>();
        StringBuilder outBuffer = new StringBuilder();
        int limit;
        int keyLen;
        int valueStart;
        boolean hasSep;
        boolean precedingBackslash;

        while ((limit = lr.readLine()) >= 0) {
            char[] buff = lr.lineBuf;
            if(buff[0] == '!' || buff[0] == '#'){
                result.add(new String(buff, 0, limit).trim());
                continue;
            }
            keyLen = 0;
            valueStart = limit;
            hasSep = false;

            //System.out.println("line=<" + new String(lineBuf, 0, limit) + ">");
            precedingBackslash = false;
            while (keyLen < limit) {
                char c = lr.lineBuf[keyLen];
                //need check if escaped.
                if ((c == '=' ||  c == ':') && !precedingBackslash) {
                    valueStart = keyLen + 1;
                    hasSep = true;
                    break;
                } else if ((c == ' ' || c == '\t' ||  c == '\f') && !precedingBackslash) {
                    valueStart = keyLen + 1;
                    break;
                }
                if (c == '\\') {
                    precedingBackslash = !precedingBackslash;
                } else {
                    precedingBackslash = false;
                }
                keyLen++;
            }
            while (valueStart < limit) {
                char c = lr.lineBuf[valueStart];
                if (c != ' ' && c != '\t' &&  c != '\f') {
                    if (!hasSep && (c == '=' ||  c == ':')) {
                        hasSep = true;
                    } else {
                        break;
                    }
                }
                valueStart++;
            }
            String key = loadConvert(lr.lineBuf, 0, keyLen, outBuffer);
            String value = loadConvert(lr.lineBuf, valueStart, limit - valueStart, outBuffer);
            result.add(new PropertyLine(key, value));
        }
        return result;
    }

    /* Read in a "logical line" from an InputStream/Reader, skip all comment
     * and blank lines and filter out those leading whitespace characters
     * (\u0020, \u0009 and \u000c) from the beginning of a "natural line".
     * Method returns the char length of the "logical line" and stores
     * the line in "lineBuf".
     */
    private static class LineReader {

        LineReader(Reader reader) {
            this.reader = reader;
            inCharBuf = new char[8192];
        }

        char[] lineBuf = new char[1024];
        private final char[] inCharBuf;
        private int inLimit = 0;
        private int inOff = 0;
        private final Reader reader;

        int readLine() throws IOException {
            // use locals to optimize for interpreted performance
            int len = 0;
            int off = inOff;
            int limit = inLimit;

            boolean skipWhiteSpace = true;
            boolean appendedLineBegin = false;
            boolean precedingBackslash = false;
            char[] charBuf = inCharBuf;
            char[] lineBuf = this.lineBuf;
            char c;

            while (true) {
                if (off >= limit) {
                    inLimit = limit = reader.read(charBuf);
                    if (limit <= 0) {
                        if (len == 0) {
                            return -1;
                        }
                        return precedingBackslash ? len - 1 : len;
                    }
                    off = 0;
                }

                // (char)(byte & 0xFF) is equivalent to calling a ISO8859-1 decoder.
                c = charBuf[off++];

                if (skipWhiteSpace) {
                    if (c == ' ' || c == '\t' || c == '\f') {
                        continue;
                    }
                    if (!appendedLineBegin && (c == '\r' || c == '\n')) {
                        continue;
                    }
                    skipWhiteSpace = false;
                    appendedLineBegin = false;

                }
                if (len == 0) { // Still on a new logical line
                    if (c == '#' || c == '!') {
                        // Comment, quickly consume the rest of the line

                        // When checking for new line characters a range check,
                        // starting with the higher bound ('\r') means one less
                        // branch in the common case.
                        // Modification: read line characters and return comment length
                        int startIdx = off-1;
                        while (true) {
                                while (off < limit) {
                                    c = charBuf[off++];
                                    //noinspection ConditionCoveredByFurtherCondition
                                    if (c <= '\r' && (c == '\r' || c == '\n')) {
                                        lineBuf = new char[off-startIdx-1];
                                        System.arraycopy(charBuf, startIdx, lineBuf, 0, off-startIdx-1);
                                        inOff = off;
                                        this.lineBuf = lineBuf;
                                        return off-startIdx-1;
                                    }
                                }
                                if (off == limit) {
                                    lineBuf = new char[off-startIdx];
                                    System.arraycopy(charBuf, startIdx, lineBuf, 0, off-startIdx);
                                    inOff = off;
                                    this.lineBuf = lineBuf;
                                    return off-startIdx;
                                }
                        }
                    }
                }

                if (c != '\n' && c != '\r') {
                    lineBuf[len++] = c;
                    if (len == lineBuf.length) {
                        lineBuf = new char[newLength(len, 1, len)];
                        System.arraycopy(this.lineBuf, 0, lineBuf, 0, len);
                        this.lineBuf = lineBuf;
                    }
                    // flip the preceding backslash flag
                    //noinspection SimplifiableConditionalExpression
                    precedingBackslash = (c == '\\') ? !precedingBackslash : false;
                } else {
                    // reached EOL
                    if (len == 0) {
                        skipWhiteSpace = true;
                        continue;
                    }
                    if (off >= limit) {
                        inLimit = limit =  reader.read(charBuf);
                        off = 0;
                        if (limit <= 0) { // EOF
                            return precedingBackslash ? len - 1 : len;
                        }
                    }
                    if (precedingBackslash) {
                        // backslash at EOL is not part of the line
                        len -= 1;
                        // skip leading whitespace characters in the following line
                        skipWhiteSpace = true;
                        appendedLineBegin = true;
                        precedingBackslash = false;
                        // take care not to include any subsequent \n
                        if (c == '\r') {
                            if (charBuf[off] == '\n') {
                                off++;
                            }
                        }
                    } else {
                        inOff = off;
                        return len;
                    }
                }
            }
        }
    }

    @SuppressWarnings("SameParameterValue")
    private static int newLength(int oldLength, int minGrowth, int prefGrowth) {
        // preconditions not checked because of inlining
        // assert oldLength >= 0
        // assert minGrowth > 0

        int prefLength = oldLength + Math.max(minGrowth, prefGrowth); // might overflow
        if (0 < prefLength && prefLength <= SOFT_MAX_ARRAY_LENGTH) {
            return prefLength;
        } else {
            // put code cold in a separate method
            return hugeLength(oldLength, minGrowth);
        }
    }

    @SuppressWarnings("ManualMinMaxCalculation")
    private static int hugeLength(int oldLength, int minGrowth) {
        int minLength = oldLength + minGrowth;
        if (minLength < 0) { // overflow
            throw new OutOfMemoryError(
                    "Required array length " + oldLength + " + " + minGrowth + " is too large");
        } else if (minLength <= SOFT_MAX_ARRAY_LENGTH) {
            return SOFT_MAX_ARRAY_LENGTH;
        } else {
            return minLength;
        }
    }

    private String loadConvert(char[] in, int off, int len, StringBuilder out) {
        char aChar;
        int end = off + len;
        int start = off;
        while (off < end) {
            aChar = in[off++];
            if (aChar == '\\') {
                break;
            }
        }
        if (off == end) { // No backslash
            return new String(in, start, len);
        }

        // backslash found at off - 1, reset the shared buffer, rewind offset
        out.setLength(0);
        off--;
        out.append(in, start, off - start);

        while (off < end) {
            aChar = in[off++];
            if (aChar == '\\') {
                // No need to bounds check since LineReader::readLine excludes
                // unescaped \s at the end of the line
                aChar = in[off++];
                if(aChar == 'u') {
                    // Read the xxxx
                    if (off > end - 4)
                        throw new IllegalArgumentException(
                                "Malformed \\uxxxx encoding.");
                    int value = 0;
                    for (int i = 0; i < 4; i++) {
                        aChar = in[off++];
                        switch (aChar) {
                            case '0':
                            case '1':
                            case '2':
                            case '3':
                            case '4': case '5': case  '6': case  '7': case  '8':case '9':{
                                value = (value << 4) + aChar - '0';
                                break;
                            }
                            case 'a': case 'b': case 'c': case 'd': case 'e': case 'f': {
                                value = (value << 4) + 10 + aChar - 'a';
                                break;
                            }
                            case 'A': case 'B': case 'C': case 'D': case 'E': case 'F' : {
                                value = (value << 4) + 10 + aChar - 'A';
                                break;
                            }
                            default:
                                throw new IllegalArgumentException("Malformed \\uxxxx encoding.");
                        }
                    }
                    out.append((char)value);
                } else {
                    if (aChar == 't') aChar = '\t';
                    else if (aChar == 'r') aChar = '\r';
                    else if (aChar == 'n') aChar = '\n';
                    else if (aChar == 'f') aChar = '\f';
                    out.append(aChar);
                }
            } else {
                out.append(aChar);
            }
        }
        return out.toString();
    }

    static class PropertyLine{
        String propertyName;
        volatile String propertyValue;

        PropertyLine(String propertyName, String propertyValue) {
            this.propertyName = propertyName;
            this.propertyValue = propertyValue;
        }
    }

}
