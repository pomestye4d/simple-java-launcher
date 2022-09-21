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

public class PropertiesFileTest {

    @Test
    public void testPropertiesFile() throws IOException {
        AppConfiguration config;
        try(InputStream is = getClass().getClassLoader().getResourceAsStream("simple.properties")){
            config = AppConfiguration.fromProperties(is);
        }
        Assertions.assertEquals("simple" , config.getValue("com.vga.sjl.simpleProperty"));
        Assertions.assertEquals("simple value" , config.getValue("com.vga.sjl.subconfig.value"));
        Assertions.assertEquals("simple value" , config.getSubConfiguration("com.vga.sjl.subconfig").getValue("value"));
        config.setProperty("com.vga.sjl.subconfig.value","simple value 2");
        File output = new File("temp/simple.properties");
        output.getParentFile().mkdirs();
        try(OutputStream os = new FileOutputStream(output)) {
            config.save(os);
        }
        String content = new String(Files.readAllBytes(output.toPath()), StandardCharsets.UTF_8);
        Assertions.assertTrue(content.contains("com.vga.sjl.subconfig.value=simple value 2"));
    }
}
