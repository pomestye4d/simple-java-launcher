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

package com.vga.sjl.config.test;

import com.vga.sjl.config.AppConfiguration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

public class YamlTest {

    @Test
    public void testYaml() throws IOException {
        AppConfiguration config;
        try(InputStream is = getClass().getClassLoader().getResourceAsStream("simple.yml")){
            config = AppConfiguration.fromYaml(is);
        }
        Assertions.assertEquals("123" , config.getValue("simpleProperty"));
        Assertions.assertEquals("value" , config.getValue("systemProperties.[com.vga.sjl.key]"));
        List<String> simpleCollection = config.getValues("simpleCollection");
        Assertions.assertEquals("item 1" , simpleCollection.get(0));
        Assertions.assertEquals("item 2" , simpleCollection.get(1));
        Assertions.assertEquals("simple value" , config.getSubConfiguration("subconfig").getValue("value"));
        config.setProperty("subconfig.value","simple value 2");
        File output = new File("temp/simple.yml");
        //noinspection ResultOfMethodCallIgnored
        output.getParentFile().mkdirs();
        try(OutputStream os = new FileOutputStream(output)) {
            config.save(os);
        }
        String content = new String(Files.readAllBytes(output.toPath()), StandardCharsets.UTF_8);
        Assertions.assertTrue(content.contains("value: simple value 2"));
    }
}
