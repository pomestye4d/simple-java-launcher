/*
 * Copyright (c) 2018, SnakeYAML
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.vga.sjl.external.org.snakeyaml.engine.v2.api.lowlevel;

import com.vga.sjl.external.org.snakeyaml.engine.v2.api.SjlExtLoadSettings;
import com.vga.sjl.external.org.snakeyaml.engine.v2.api.SjlExtYamlUnicodeReader;
import com.vga.sjl.external.org.snakeyaml.engine.v2.events.SjlExtEvent;
import com.vga.sjl.external.org.snakeyaml.engine.v2.parser.SjlExtParserImpl;
import com.vga.sjl.external.org.snakeyaml.engine.v2.scanner.SjlExtStreamReader;

import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.Iterator;
import java.util.Objects;

/**
 * Read the input stream and parse the content into events (opposite for Present or Emit)
 */
public class SjlExtParse {

  private final SjlExtLoadSettings settings;

  /**
   * Create instance with provided {@link SjlExtLoadSettings}
   *
   * @param settings - configuration
   */
  public SjlExtParse(SjlExtLoadSettings settings) {
    Objects.requireNonNull(settings, "LoadSettings cannot be null");
    this.settings = settings;
  }

  /**
   * Parse a YAML stream and produce parsing events.
   *
   * @param yaml - YAML document(s). Default encoding is UTF-8. The BOM must be present if the
   *             encoding is UTF-16 or UTF-32
   * @return parsed events
   * @see <a href="http://www.yaml.org/spec/1.2/spec.html#id2762107">Processing Overview</a>
   */
  public Iterable<SjlExtEvent> parseInputStream(InputStream yaml) {
    Objects.requireNonNull(yaml, "InputStream cannot be null");
    return () -> new SjlExtParserImpl(settings, new SjlExtStreamReader(settings, new SjlExtYamlUnicodeReader(yaml)));
  }

  /**
   * Parse a YAML stream and produce parsing events. Since the encoding is already known the BOM
   * must not be present (it will be parsed as content)
   *
   * @param yaml - YAML document(s).
   * @return parsed events
   * @see <a href="http://www.yaml.org/spec/1.2/spec.html#id2762107">Processing Overview</a>
   */
  public Iterable<SjlExtEvent> parseReader(Reader yaml) {
    Objects.requireNonNull(yaml, "Reader cannot be null");
    return () -> new SjlExtParserImpl(settings, new SjlExtStreamReader(settings, yaml));
  }

  /**
   * Parse a YAML stream and produce parsing events.
   *
   * @param yaml - YAML document(s). The BOM must not be present (it will be parsed as content)
   * @return parsed events
   * @see <a href="http://www.yaml.org/spec/1.2/spec.html#id2762107">Processing Overview</a>
   */
  public Iterable<SjlExtEvent> parseString(String yaml) {
    Objects.requireNonNull(yaml, "String cannot be null");
    //do not use lambda to keep Iterable and Iterator visible
    return new Iterable() {
      public Iterator<SjlExtEvent> iterator() {
        return new SjlExtParserImpl(settings, new SjlExtStreamReader(settings, new StringReader(yaml)));
      }
    };
  }
}

