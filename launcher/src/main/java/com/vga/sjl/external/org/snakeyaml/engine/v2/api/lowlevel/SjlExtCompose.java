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

/*
 * This is a renamed copy of file from snakeyaml-engine project version
 * Original file can be found at https://bitbucket.org/snakeyaml/snakeyaml-engine
 * at master branch
 * Copy was made at 2022-09-19
 */

package com.vga.sjl.external.org.snakeyaml.engine.v2.api.lowlevel;

import com.vga.sjl.external.org.snakeyaml.engine.v2.api.SjlExtLoadSettings;
import com.vga.sjl.external.org.snakeyaml.engine.v2.api.SjlExtYamlUnicodeReader;
import com.vga.sjl.external.org.snakeyaml.engine.v2.composer.SjlExtComposer;
import com.vga.sjl.external.org.snakeyaml.engine.v2.nodes.SjlExtNode;
import com.vga.sjl.external.org.snakeyaml.engine.v2.parser.SjlExtParserImpl;
import com.vga.sjl.external.org.snakeyaml.engine.v2.scanner.SjlExtStreamReader;

import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;

public class SjlExtCompose {

  private final SjlExtLoadSettings settings;

  /**
   * Create instance with provided {@link SjlExtLoadSettings}
   *
   * @param settings - configuration
   */
  public SjlExtCompose(SjlExtLoadSettings settings) {
    Objects.requireNonNull(settings, "LoadSettings cannot be null");
    this.settings = settings;
  }

  /**
   * Parse a YAML stream and produce {@link SjlExtNode}
   *
   * @param yaml - YAML document(s). Since the encoding is already known the BOM must not be present
   *             (it will be parsed as content)
   * @return parsed {@link SjlExtNode} if available
   * @see <a href="http://www.yaml.org/spec/1.2/spec.html#id2762107">Processing Overview</a>
   */
  public Optional<SjlExtNode> composeReader(Reader yaml) {
    Objects.requireNonNull(yaml, "Reader cannot be null");
    return new SjlExtComposer(settings, new SjlExtParserImpl(settings, new SjlExtStreamReader(settings, yaml))
    ).getSingleNode();
  }

  /**
   * Parse a YAML stream and produce {@link SjlExtNode}
   *
   * @param yaml - YAML document(s). Default encoding is UTF-8. The BOM must be present if the
   *             encoding is UTF-16 or UTF-32
   * @return parsed {@link SjlExtNode} if available
   * @see <a href="http://www.yaml.org/spec/1.2/spec.html#id2762107">Processing Overview</a>
   */
  public Optional<SjlExtNode> composeInputStream(InputStream yaml) {
    Objects.requireNonNull(yaml, "InputStream cannot be null");
    return new SjlExtComposer(settings,
        new SjlExtParserImpl(settings, new SjlExtStreamReader(settings, new SjlExtYamlUnicodeReader(yaml)))
    ).getSingleNode();
  }

  /**
   * Parse a YAML stream and produce {@link SjlExtNode}
   *
   * @param yaml - YAML document(s).
   * @return parsed {@link SjlExtNode} if available
   * @see <a href="http://www.yaml.org/spec/1.2/spec.html#id2762107">Processing Overview</a>
   */
  public Optional<SjlExtNode> composeString(String yaml) {
    Objects.requireNonNull(yaml, "String cannot be null");
    return new SjlExtComposer(settings,
        new SjlExtParserImpl(settings, new SjlExtStreamReader(settings, new StringReader(yaml)))
    ).getSingleNode();
  }

  // Compose all documents

  /**
   * Parse all YAML documents in a stream and produce corresponding representation trees.
   *
   * @param yaml stream of YAML documents
   * @return parsed root Nodes for all the specified YAML documents
   * @see <a href="http://www.yaml.org/spec/1.2/spec.html#id2762107">Processing Overview</a>
   */
  public Iterable<SjlExtNode> composeAllFromReader(Reader yaml) {
    Objects.requireNonNull(yaml, "Reader cannot be null");
    return () -> new SjlExtComposer(settings, new SjlExtParserImpl(settings, new SjlExtStreamReader(settings, yaml)));
  }

  /**
   * Parse all YAML documents in a stream and produce corresponding representation trees.
   *
   * @param yaml - YAML document(s). Default encoding is UTF-8. The BOM must be present if the
   *             encoding is UTF-16 or UTF-32
   * @return parsed root Nodes for all the specified YAML documents
   * @see <a href="http://www.yaml.org/spec/1.2/spec.html#id2762107">Processing Overview</a>
   */
  public Iterable<SjlExtNode> composeAllFromInputStream(InputStream yaml) {
    Objects.requireNonNull(yaml, "InputStream cannot be null");
    return () -> new SjlExtComposer(settings,
        new SjlExtParserImpl(settings, new SjlExtStreamReader(settings, new SjlExtYamlUnicodeReader(yaml))));
  }

  /**
   * Parse all YAML documents in a stream and produce corresponding representation trees.
   *
   * @param yaml - YAML document(s).
   * @return parsed root Nodes for all the specified YAML documents
   * @see <a href="http://www.yaml.org/spec/1.2/spec.html#id2762107">Processing Overview</a>
   */
  public Iterable<SjlExtNode> composeAllFromString(String yaml) {
    Objects.requireNonNull(yaml, "String cannot be null");
    //do not use lambda to keep Iterable and Iterator visible
    return new Iterable() {
      public Iterator<SjlExtNode> iterator() {
        return new SjlExtComposer(settings, new SjlExtParserImpl(
            settings, new SjlExtStreamReader(settings, new StringReader(yaml))));
      }
    };
  }
}




