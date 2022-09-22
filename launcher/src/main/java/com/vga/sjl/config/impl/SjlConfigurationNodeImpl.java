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

import java.util.*;

public class SjlConfigurationNodeImpl implements ConfigurationNode {

    final Map<String, Object> map = new HashMap<>();

    @Override
    public List<String> getValues(String propertyName) {
        return getValuesInternal(propertyName, List.class);
    }

    static List<String> split(String normalizedPropertyName) {
        List<String> result = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean insideComplexProperty = false;
        for(int n=0; n < normalizedPropertyName.length(); n++){
            char c = normalizedPropertyName.charAt(n);
            if(c == '.' && !insideComplexProperty){
                if(sb.length() > 0) {
                    result.add(sb.toString());
                }
                sb.setLength(0);
                continue;
            }
            if(c == '['){
                insideComplexProperty = true;
                continue;
            }
            if(c == ']'){
                insideComplexProperty = false;
                continue;
            }
            sb.append(c);
        }
        if(sb.length() > 0){
            result.add(sb.toString());
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private<T> T getValuesInternal(String propertyName, Class<?> cls) {
        if(propertyName == null || propertyName.length() == 0){
            throw new IllegalArgumentException("property name must be a non empty string");
        }
        List<String> parts =  split(propertyName);
        Map<String, Object> conf = map;
        for(int n = 0; n < parts.size(); n++){
            Object result = conf.get(parts.get(n));
            if(n == parts.size()-1){
                if(result == null){
                    return (T) (List.class.isAssignableFrom(cls) ? Collections.emptyList(): null);
                }
                if(! (cls.isAssignableFrom(result.getClass()))){
                    throw new IllegalArgumentException(String.format("value with name %s is not a %s", propertyName, cls.getSimpleName()));
                }
                return (T) result;
            }
            if(result == null){
                return (T) (List.class.isAssignableFrom(cls) ? Collections.emptyList(): null);
            }
            if(!(result instanceof SjlConfigurationNodeImpl)){
                StringBuilder sb = new StringBuilder();
                for(int i =0; i <= n; i++){
                    if(sb.length() > 0){
                        sb.append(".");
                    }
                    sb.append(parts.get(i));
                }
                throw new IllegalArgumentException(String.format("value with property name %s is string instead of subconfiguration", sb));
            }
            conf = ((SjlConfigurationNodeImpl) result).map;
        }
        return (T) (List.class.isAssignableFrom(cls) ? Collections.emptyList(): null);
    }


    @Override
    public String getValue(String propertyName) {
        return getValuesInternal(propertyName, String.class);
    }

    @Override
    public ConfigurationNode getSubConfiguration(String propertyName) {
        return getValuesInternal(propertyName, ConfigurationNode.class);
    }

    @Override
    public List<ConfigurationNode> getSubConfigurations(String propertyName) {
        return getValuesInternal(propertyName, List.class);
    }

}
