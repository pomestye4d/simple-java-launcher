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

package com.vga.sjl.config.impl;

import com.vga.sjl.config.model.ConfigurationNode;
import com.vga.sjl.config.model.ConfigurationProvider;
import com.vga.sjl.external.org.snakeyaml.engine.v2.api.*;
import com.vga.sjl.external.org.snakeyaml.engine.v2.composer.SjlExtComposer;
import com.vga.sjl.external.org.snakeyaml.engine.v2.nodes.*;
import com.vga.sjl.external.org.snakeyaml.engine.v2.representer.SjlExtStandardRepresenter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SjlYamlConfigurationProvider implements ConfigurationProvider {

    private volatile SjlConfigurationNodeImpl configRootNode;

    private volatile SjlExtMappingNode yamlRootNode;

    private final SjlExtDumpSettings settings = SjlExtDumpSettings.builder().setDumpComments(true).build();

    private final SjlExtStandardRepresenter representer = new SjlExtStandardRepresenter(settings);

    public SjlYamlConfigurationProvider(InputStream is) {
        init(is);
    }

    public SjlYamlConfigurationProvider(File file) throws IOException {
        try(InputStream is = new FileInputStream(file)){
            init(is);
        }
    }

    private synchronized void init(InputStream is){
        SjlYamlLoad load = new SjlYamlLoad();
        SjlExtMappingNode root = load.load(is);
        if(root == null){
            root = (SjlExtMappingNode) representer.represent(new HashMap<String, Object>());
        }
        yamlRootNode = root;
        updateConfigRootNode();
    }

    private void updateConfigRootNode() {
        configRootNode = (SjlConfigurationNodeImpl) getConfigNode(yamlRootNode);
    }

    private Object getConfigNode(SjlExtNode node) {
        if(node instanceof SjlExtAnchorNode){
            return null;
        }
        if(node instanceof SjlExtScalarNode){
            return ((SjlExtScalarNode) node).getValue();
        }
        if(node instanceof SjlExtMappingNode){
            SjlConfigurationNodeImpl result = new SjlConfigurationNodeImpl();
            SjlExtMappingNode mn = (SjlExtMappingNode) node;
            for(SjlExtNodeTuple tuple: mn.getValue()){
                SjlExtScalarNode keyNode = (SjlExtScalarNode) tuple.getKeyNode();
                String key = keyNode.getValue();
                result.map.put(key, getConfigNode(tuple.getValueNode()));
            }
            return  result;
        }
        if(node instanceof SjlExtCollectionNode){
            List<Object> result = new ArrayList<>();
            SjlExtCollectionNode<Object> collectionNode = (SjlExtCollectionNode<Object>) node;
            for(Object item: collectionNode.getValue()){
                result.add(getConfigNode((SjlExtNode) item));
            }
            return result;
        }
        return null;
    }

    @Override
    public ConfigurationNode getConfiguration() {
        return configRootNode;
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
        String[] parts = normalizedPropertyName.split("\\.");
        SjlExtMappingNode node = yamlRootNode;
        for(int n =0; n< parts.length-1; n++){
            String part = parts[n];
            SjlExtNodeTuple tuple = node.getValue().stream().filter(it -> part.equals(((SjlExtScalarNode) it.getKeyNode()).getValue()))
                    .findFirst().orElse(null);
            if(tuple != null){
                if(tuple.getValueNode() instanceof SjlExtMappingNode){
                    node = (SjlExtMappingNode) tuple.getValueNode();
                    continue;
                }
                node.getValue().remove(tuple);
            }
            tuple = new SjlExtNodeTuple(representer.represent(part), representer.represent(new HashMap<>()));
            node.getValue().add(tuple);
            node = (SjlExtMappingNode) tuple.getValueNode();
        }
        String lastPart = parts[parts.length -1];
        SjlExtNodeTuple tuple = node.getValue().stream().filter(it -> lastPart.equals(((SjlExtScalarNode) it.getKeyNode()).getValue()))
                .findFirst().orElse(null);
        int idx = node.getValue().size();
        if(tuple != null){
            idx = node.getValue().indexOf(tuple);
            node.getValue().remove(tuple);
        }
        node.getValue().add(idx, new SjlExtNodeTuple(representer.represent(lastPart), representer.represent(normalizedPropertyValue)));
        updateConfigRootNode();
    }

    @Override
    public synchronized void save(OutputStream os) throws IOException {

        SjlExtYamlOutputStreamWriter writer = new SjlExtYamlOutputStreamWriter(os, StandardCharsets.UTF_8) {
            @Override
            public void processIOException(IOException e) {
                throw new RuntimeException(e);
            }
        };
        new SjlExtDump(settings).dumpNode(yamlRootNode, writer);
    }

    static class SjlYamlLoad extends SjlExtLoad{

        SjlYamlLoad() {
            super(createSettings());
        }

        private static SjlExtLoadSettings createSettings() {
            return SjlExtLoadSettings.builder().setParseComments(true).build();
        }

        SjlExtMappingNode load(InputStream is){
            Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8);
            SjlExtComposer composer = createComposer(reader);
            return (SjlExtMappingNode) composer.getSingleNode().orElse(null);
        }
    }
}
