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
package com.vga.sjl.external.org.snakeyaml.engine.v2.api;

import com.vga.sjl.external.org.snakeyaml.engine.v2.composer.SjlExtComposer;
import com.vga.sjl.external.org.snakeyaml.engine.v2.constructor.SjlExtBaseConstructor;
import com.vga.sjl.external.org.snakeyaml.engine.v2.constructor.SjlExtStandardConstructor;
import com.vga.sjl.external.org.snakeyaml.engine.v2.nodes.SjlExtNode;
import com.vga.sjl.external.org.snakeyaml.engine.v2.parser.SjlExtParserImpl;
import com.vga.sjl.external.org.snakeyaml.engine.v2.scanner.SjlExtStreamReader;

import java.io.InputStream;
import java.io.Reader;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;

/**
 * Common way to load Java instance(s). This class is not thread-safe. Which means that all the
 * methods of the same instance can be called only by one thread. It is better to create an instance
 * for every YAML stream. The instance is stateful. Only one of the 'load' methods may be called and
 * it may be called only once.
 */
public class SjlExtLoad {

  private final SjlExtLoadSettings settings;
  private final SjlExtBaseConstructor constructor;

  /**
   * Create instance to parse the incoming YAML data and create Java instances
   *
   * @param settings - configuration
   */
  public SjlExtLoad(SjlExtLoadSettings settings) {
    this(settings, new SjlExtStandardConstructor(settings));
  }

  /**
   * Create instance to parse the incoming YAML data and create Java instances
   *
   * @param settings    - configuration
   * @param constructor - custom YAML constructor
   */
  public SjlExtLoad(SjlExtLoadSettings settings, SjlExtBaseConstructor constructor) {
    Objects.requireNonNull(settings, "LoadSettings cannot be null");
    Objects.requireNonNull(constructor, "BaseConstructor cannot be null");
    this.settings = settings;
    this.constructor = constructor;
  }

  private SjlExtComposer createComposer(SjlExtStreamReader streamReader) {
    return new SjlExtComposer(settings, new SjlExtParserImpl(settings, streamReader));
  }

  protected SjlExtComposer createComposer(InputStream yamlStream) {
    return createComposer(new SjlExtStreamReader(settings, new SjlExtYamlUnicodeReader(yamlStream)));
  }

  protected SjlExtComposer createComposer(String yaml) {
    return createComposer(new SjlExtStreamReader(settings, yaml));
  }

  protected SjlExtComposer createComposer(Reader yamlReader) {
    return createComposer(new SjlExtStreamReader(settings, yamlReader));
  }

  // Load  a single document

  protected Object loadOne(SjlExtComposer composer) {
    Optional<SjlExtNode> nodeOptional = composer.getSingleNode();
    return constructor.constructSingleDocument(nodeOptional);
  }

  /**
   * Parse the only YAML document in a stream and produce the corresponding Java object.
   *
   * @param yamlStream - data to load from (BOM is respected to detect encoding and removed from the
   *                   data)
   * @return parsed Java instance
   */
  public Object loadFromInputStream(InputStream yamlStream) {
    Objects.requireNonNull(yamlStream, "InputStream cannot be null");
    return loadOne(createComposer(yamlStream));
  }

  /**
   * Parse a YAML document and create a Java instance
   *
   * @param yamlReader - data to load from (BOM must not be present)
   * @return parsed Java instance
   */
  public Object loadFromReader(Reader yamlReader) {
    Objects.requireNonNull(yamlReader, "Reader cannot be null");
    return loadOne(createComposer(yamlReader));
  }

  /**
   * Parse a YAML document and create a Java instance
   *
   * @param yaml - YAML data to load from (BOM must not be present)
   * @return parsed Java instance
   * @throws com.vga.sjl.external.org.snakeyaml.engine.v2.exceptions.SjlExtYamlEngineException if the YAML is not valid
   */
  public Object loadFromString(String yaml) {
    Objects.requireNonNull(yaml, "String cannot be null");
    return loadOne(createComposer(yaml));
  }

  // Load all the documents

  private Iterable<Object> loadAll(SjlExtComposer composer) {
    Iterator<Object> result = new SjlExtYamlIterator(composer, constructor);
    return new YamlIterable(result);
  }

  /**
   * Parse all YAML documents in a stream and produce corresponding Java objects. The documents are
   * parsed only when the iterator is invoked.
   *
   * @param yamlStream - YAML data to load from (BOM is respected to detect encoding and removed
   *                   from the data)
   * @return an Iterable over the parsed Java objects in this stream in proper sequence
   */
  public Iterable<Object> loadAllFromInputStream(InputStream yamlStream) {
    Objects.requireNonNull(yamlStream, "InputStream cannot be null");
    SjlExtComposer composer = createComposer(
        new SjlExtStreamReader(settings, new SjlExtYamlUnicodeReader(yamlStream)));
    return loadAll(composer);
  }

  /**
   * Parse all YAML documents in a String and produce corresponding Java objects. The documents are
   * parsed only when the iterator is invoked.
   *
   * @param yamlReader - YAML data to load from (BOM must not be present)
   * @return an Iterable over the parsed Java objects in this stream in proper sequence
   */
  public Iterable<Object> loadAllFromReader(Reader yamlReader) {
    Objects.requireNonNull(yamlReader, "Reader cannot be null");
    SjlExtComposer composer = createComposer(new SjlExtStreamReader(settings, yamlReader));
    return loadAll(composer);
  }

  /**
   * Parse all YAML documents in a String and produce corresponding Java objects. (Because the
   * encoding in known BOM is not respected.) The documents are parsed only when the iterator is
   * invoked.
   *
   * @param yaml - YAML data to load from (BOM must not be present)
   * @return an Iterable over the parsed Java objects in this stream in proper sequence
   */
  public Iterable<Object> loadAllFromString(String yaml) {
    Objects.requireNonNull(yaml, "String cannot be null");
    SjlExtComposer composer = createComposer(new SjlExtStreamReader(settings, yaml));
    return loadAll(composer);
  }

  private static class YamlIterable implements Iterable<Object> {

    private final Iterator<Object> iterator;

    public YamlIterable(Iterator<Object> iterator) {
      this.iterator = iterator;
    }

    @Override
    public Iterator<Object> iterator() {
      return iterator;
    }
  }

  private static class SjlExtYamlIterator implements Iterator<Object> {

    private final SjlExtComposer composer;
    private final SjlExtBaseConstructor constructor;

    public SjlExtYamlIterator(SjlExtComposer composer, SjlExtBaseConstructor constructor) {
      this.composer = composer;
      this.constructor = constructor;
    }

    @Override
    public boolean hasNext() {
      return composer.hasNext();
    }

    @Override
    public Object next() {
      SjlExtNode node = composer.next();
      return constructor.constructSingleDocument(Optional.of(node));
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException("Removing is not supported.");
    }
  }
}




