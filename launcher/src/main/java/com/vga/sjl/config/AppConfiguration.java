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

package com.vga.sjl.config;

import com.vga.sjl.config.impl.SjlPropertiesConfigurationProvider;
import com.vga.sjl.config.impl.SjlYamlConfigurationProvider;
import com.vga.sjl.config.model.ConfigurationNode;
import com.vga.sjl.config.model.ConfigurationProvider;

import java.io.*;
import java.util.List;

public class AppConfiguration implements ConfigurationNode {

    private final ConfigurationProvider provider;

    public static AppConfiguration fromProperties(InputStream is) throws IOException {
        return new AppConfiguration(new SjlPropertiesConfigurationProvider(is));
    }

    public static AppConfiguration fromProperties(File file) throws IOException {
        return new AppConfiguration(new SjlPropertiesConfigurationProvider(file));
    }

    public static AppConfiguration fromYaml(InputStream is) throws IOException {
        return new AppConfiguration(new SjlYamlConfigurationProvider(is));
    }

    public static AppConfiguration fromYaml(File file) throws IOException {
        return new AppConfiguration(new SjlYamlConfigurationProvider(file));
    }

    private AppConfiguration(ConfigurationProvider provider){
        this.provider = provider;
    }

    @Override
    public List<String> getValues(String propertyName) {
        return provider.getConfiguration().getValues(propertyName);
    }

    @Override
    public String getValue(String propertyName) {
        return provider.getConfiguration().getValue(propertyName);
    }

    @Override
    public ConfigurationNode getSubConfiguration(String propertyName) {
        return provider.getConfiguration().getSubConfiguration(propertyName);
    }

    @Override
    public List<ConfigurationNode> getSubConfigurations(String propertyName) {
        return provider.getConfiguration().getSubConfigurations(propertyName);
    }

    public void setProperty(String propertyName, String value){
        provider.setProperty(propertyName, value);
    }

    public void save(OutputStream os) throws IOException {
        provider.save(os);
    }

    public void save(File file) throws IOException {
        try(OutputStream fos = new FileOutputStream(file)){
            save(fos);
        }
    }
}
