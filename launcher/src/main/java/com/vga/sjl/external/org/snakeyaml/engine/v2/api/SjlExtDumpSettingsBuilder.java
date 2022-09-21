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

import com.vga.sjl.external.org.snakeyaml.engine.v2.common.SjlExtFlowStyle;
import com.vga.sjl.external.org.snakeyaml.engine.v2.common.SjlExtNonPrintableStyle;
import com.vga.sjl.external.org.snakeyaml.engine.v2.common.SjlExtScalarStyle;
import com.vga.sjl.external.org.snakeyaml.engine.v2.common.SjlExtSpecVersion;
import com.vga.sjl.external.org.snakeyaml.engine.v2.emitter.SjlExtEmitter;
import com.vga.sjl.external.org.snakeyaml.engine.v2.exceptions.SjlExtEmitterException;
import com.vga.sjl.external.org.snakeyaml.engine.v2.exceptions.SjlExtYamlEngineException;
import com.vga.sjl.external.org.snakeyaml.engine.v2.nodes.SjlExtTag;
import com.vga.sjl.external.org.snakeyaml.engine.v2.resolver.SjlExtJsonScalarResolver;
import com.vga.sjl.external.org.snakeyaml.engine.v2.resolver.SjlExtScalarResolver;
import com.vga.sjl.external.org.snakeyaml.engine.v2.serializer.SjlExtAnchorGenerator;
import com.vga.sjl.external.org.snakeyaml.engine.v2.serializer.SjlExtNumberAnchorGenerator;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Builder pattern implementation for DumpSettings
 */
public final class SjlExtDumpSettingsBuilder {

  private boolean explicitStart;
  private boolean explicitEnd;
  private SjlExtNonPrintableStyle nonPrintableStyle;
  private Optional<SjlExtTag> explicitRootTag;
  private SjlExtAnchorGenerator anchorGenerator;
  private Optional<SjlExtSpecVersion> yamlDirective;
  private Map<String, String> tagDirective;
  private SjlExtScalarResolver scalarResolver;
  private SjlExtFlowStyle defaultFlowStyle;
  private SjlExtScalarStyle defaultScalarStyle;

  //emitter
  private boolean canonical;
  private boolean multiLineFlow;
  private boolean useUnicodeEncoding;
  private int indent;
  private int indicatorIndent;
  private int width;
  private String bestLineBreak;
  private boolean splitLines;
  private int maxSimpleKeyLength;
  private boolean indentWithIndicator;
  private boolean dumpComments;

  //general
  Map<SjlExtSettingKey, Object> customProperties = new HashMap();

  /**
   * Create builder
   */
  SjlExtDumpSettingsBuilder() {
    this.explicitRootTag = Optional.empty();
    this.tagDirective = new HashMap<>();
    this.scalarResolver = new SjlExtJsonScalarResolver();
    this.anchorGenerator = new SjlExtNumberAnchorGenerator(0);
    this.bestLineBreak = "\n";
    this.canonical = false;
    this.useUnicodeEncoding = true;
    this.indent = 2;
    this.indicatorIndent = 0;
    this.width = 80;
    this.splitLines = true;
    this.explicitStart = false;
    this.explicitEnd = false;
    this.yamlDirective = Optional.empty();
    this.defaultFlowStyle = SjlExtFlowStyle.AUTO;
    this.defaultScalarStyle = SjlExtScalarStyle.PLAIN;
    this.maxSimpleKeyLength = 128;
    this.indentWithIndicator = false;
    this.dumpComments = false;
  }

  /**
   * Define flow style
   *
   * @param defaultFlowStyle - specify the style
   * @return the builder with the provided value
   */
  public SjlExtDumpSettingsBuilder setDefaultFlowStyle(SjlExtFlowStyle defaultFlowStyle) {
    this.defaultFlowStyle = defaultFlowStyle;
    return this;
  }

  /**
   * Define default scalar style
   *
   * @param defaultScalarStyle - specify the scalar style
   * @return the builder with the provided value
   */
  public SjlExtDumpSettingsBuilder setDefaultScalarStyle(SjlExtScalarStyle defaultScalarStyle) {
    this.defaultScalarStyle = defaultScalarStyle;
    return this;
  }

  /**
   * Add '---' in the beginning of the document
   *
   * @param explicitStart - true if the document start must be explicitly indicated
   * @return the builder with the provided value
   */
  public SjlExtDumpSettingsBuilder setExplicitStart(boolean explicitStart) {
    this.explicitStart = explicitStart;
    return this;
  }

  /**
   * Define anchor name generator (by default 'id' + number)
   *
   * @param anchorGenerator - specified function to create anchor names
   * @return the builder with the provided value
   */
  public SjlExtDumpSettingsBuilder setAnchorGenerator(SjlExtAnchorGenerator anchorGenerator) {
    Objects.requireNonNull(anchorGenerator, "anchorGenerator cannot be null");
    this.anchorGenerator = anchorGenerator;
    return this;
  }

  /**
   * Define {@link SjlExtScalarResolver} or use JSON resolver by default. Do we need this method ?
   *
   * @param scalarResolver - specify the scalar resolver
   * @return the builder with the provided value
   */
  public SjlExtDumpSettingsBuilder setScalarResolver(SjlExtScalarResolver scalarResolver) {
    Objects.requireNonNull(scalarResolver, "scalarResolver cannot be null");
    this.scalarResolver = scalarResolver;
    return this;
  }

  /**
   * Define root {@link SjlExtTag} or let the tag to be detected automatically
   *
   * @param explicitRootTag - specify the root tag
   * @return the builder with the provided value
   */
  public SjlExtDumpSettingsBuilder setExplicitRootTag(Optional<SjlExtTag> explicitRootTag) {
    Objects.requireNonNull(explicitRootTag, "explicitRootTag cannot be null");
    this.explicitRootTag = explicitRootTag;
    return this;
  }

  /**
   * Add '...' in the end of the document
   *
   * @param explicitEnd - true if the document end must be explicitly indicated
   * @return the builder with the provided value
   */
  public SjlExtDumpSettingsBuilder setExplicitEnd(boolean explicitEnd) {
    this.explicitEnd = explicitEnd;
    return this;
  }

  /**
   * Add YAML directive (http://yaml.org/spec/1.2/spec.html#id2782090)
   *
   * @param yamlDirective - the version to be used in the directive
   * @return the builder with the provided value
   */
  public SjlExtDumpSettingsBuilder setYamlDirective(Optional<SjlExtSpecVersion> yamlDirective) {
    Objects.requireNonNull(yamlDirective, "yamlDirective cannot be null");
    this.yamlDirective = yamlDirective;
    return this;
  }

  /**
   * Add TAG directive (http://yaml.org/spec/1.2/spec.html#id2782090)
   *
   * @param tagDirective - the data to create TAG directive
   * @return the builder with the provided value
   */
  public SjlExtDumpSettingsBuilder setTagDirective(Map<String, String> tagDirective) {
    Objects.requireNonNull(tagDirective, "tagDirective cannot be null");
    this.tagDirective = tagDirective;
    return this;
  }

  /**
   * Enforce canonical representation
   *
   * @param canonical - specify if the canonical representation must be used
   * @return the builder with the provided value
   */
  public SjlExtDumpSettingsBuilder setCanonical(boolean canonical) {
    this.canonical = canonical;
    return this;
  }

  /**
   * Use pretty flow style when every value in the flow context gets a separate line.
   *
   * @param multiLineFlow - set false to output all values in a single line.
   * @return the builder with the provided value
   */
  public SjlExtDumpSettingsBuilder setMultiLineFlow(boolean multiLineFlow) {
    this.multiLineFlow = multiLineFlow;
    return this;
  }

  /**
   * Specify whether to emit non-ASCII printable Unicode characters (emit Unicode char or escape
   * sequence starting with '\\u') The default value is true. When set to false then printable
   * non-ASCII characters (Cyrillic, Chinese etc) will be not printed but escaped (to support ASCII
   * terminals)
   *
   * @param useUnicodeEncoding - true to use Unicode for "Я", false to use "\u0427" for the same
   *                           char (if useUnicodeEncoding is false then all non-ASCII characters
   *                           are escaped)
   * @return the builder with the provided value
   */
  public SjlExtDumpSettingsBuilder setUseUnicodeEncoding(boolean useUnicodeEncoding) {
    this.useUnicodeEncoding = useUnicodeEncoding;
    return this;
  }

  /**
   * Define the amount of the spaces for the indent in the block flow style. Default is 2.
   *
   * @param indent - the number of spaces. Must be within the range org.snakeyaml.engine.v2.emitter.Emitter.MIN_INDENT
   *               and org.snakeyaml.engine.v2.emitter.Emitter.MAX_INDENT
   * @return the builder with the provided value
   */
  public SjlExtDumpSettingsBuilder setIndent(int indent) {
    if (indent < SjlExtEmitter.MIN_INDENT) {
      throw new SjlExtEmitterException("Indent must be at least " + SjlExtEmitter.MIN_INDENT);
    }
    if (indent > SjlExtEmitter.MAX_INDENT) {
      throw new SjlExtEmitterException("Indent must be at most " + SjlExtEmitter.MAX_INDENT);
    }
    this.indent = indent;
    return this;
  }

  /**
   * It adds the specified indent for sequence indicator in the block flow. Default is 0. For better
   * visual results it should be by 2 less than the indent (which is 2 by default) It is 2 chars
   * less because the first char is '-' and the second char is the space after it.
   *
   * @param indicatorIndent - must be non-negative and less than org.snakeyaml.engine.v2.emitter.Emitter.MAX_INDENT
   *                        - 1
   * @return the builder with the provided value
   */
  public SjlExtDumpSettingsBuilder setIndicatorIndent(int indicatorIndent) {
    if (indicatorIndent < 0) {
      throw new SjlExtEmitterException("Indicator indent must be non-negative");
    }
    if (indicatorIndent > SjlExtEmitter.MAX_INDENT - 1) {
      throw new SjlExtEmitterException(
          "Indicator indent must be at most Emitter.MAX_INDENT-1: " + (SjlExtEmitter.MAX_INDENT - 1));
    }
    this.indicatorIndent = indicatorIndent;
    return this;
  }

  /**
   * Set max width for literal scalars. When the scalar representation takes more then the preferred
   * with the scalar will be split into a few lines. The default is 80.
   *
   * @param width - the width
   * @return the builder with the provided value
   */
  public SjlExtDumpSettingsBuilder setWidth(int width) {
    this.width = width;
    return this;
  }

  /**
   * If the YAML is created for another platform (for instance on Windows to be consumed under
   * Linux) than this setting is used to define the line ending. The platform line end is used by
   * default.
   *
   * @param bestLineBreak -  "\r\n" or "\n"
   * @return the builder with the provided value
   */
  public SjlExtDumpSettingsBuilder setBestLineBreak(String bestLineBreak) {
    Objects.requireNonNull(bestLineBreak, "bestLineBreak cannot be null");
    this.bestLineBreak = bestLineBreak;
    return this;
  }

  /**
   * Define whether to split long lines
   *
   * @param splitLines - true to split long lines
   * @return the builder with the provided value
   */
  public SjlExtDumpSettingsBuilder setSplitLines(boolean splitLines) {
    this.splitLines = splitLines;
    return this;
  }

  /**
   * Define max key length to use simple key (without '?') More info https://yaml.org/spec/1.2/spec.html#id2798057
   *
   * @param maxSimpleKeyLength - the limit after which the key gets explicit key indicator '?'
   * @return the builder with the provided value
   */
  public SjlExtDumpSettingsBuilder setMaxSimpleKeyLength(int maxSimpleKeyLength) {
    if (maxSimpleKeyLength > 1024) {
      throw new SjlExtYamlEngineException(
          "The simple key must not span more than 1024 stream characters. See https://yaml.org/spec/1.2/spec.html#id2798057");
    }
    this.maxSimpleKeyLength = maxSimpleKeyLength;
    return this;
  }

  /**
   * When String object contains non-printable characters, they are escaped with \\u or \\x
   * notation. Sometimes it is better to transform this data to binary (with the !!binary tag).
   * String objects with printable data are non affected by this setting.
   *
   * @param nonPrintableStyle - set this to BINARY to force non-printable String to represented as
   *                          binary (byte array)
   * @return the builder with the provided value
   */
  public SjlExtDumpSettingsBuilder setNonPrintableStyle(SjlExtNonPrintableStyle nonPrintableStyle) {
    this.nonPrintableStyle = nonPrintableStyle;
    return this;
  }

  public SjlExtDumpSettingsBuilder setCustomProperty(SjlExtSettingKey key, Object value) {
    customProperties.put(key, value);
    return this;
  }

  /**
   * Set to true to add the indent for sequences to the general indent
   *
   * @param indentWithIndicator - true when indent for sequences is added to general
   */
  public SjlExtDumpSettingsBuilder setIndentWithIndicator(boolean indentWithIndicator) {
    this.indentWithIndicator = indentWithIndicator;
    return this;
  }

  /**
   * Set to true to add comments from Nodes to
   *
   * @param dumpComments - true when comments should be dumped (serialised)
   */
  public SjlExtDumpSettingsBuilder setDumpComments(boolean dumpComments) {
    this.dumpComments = dumpComments;
    return this;
  }

  /**
   * Create immutable DumpSettings
   *
   * @return DumpSettings with the provided values
   */
  public SjlExtDumpSettings build() {
    return new SjlExtDumpSettings(explicitStart, explicitEnd, explicitRootTag,
        anchorGenerator, yamlDirective, tagDirective,
        scalarResolver, defaultFlowStyle, defaultScalarStyle, nonPrintableStyle,
        //emitter
        canonical, multiLineFlow, useUnicodeEncoding,
        indent, indicatorIndent, width, bestLineBreak, splitLines, maxSimpleKeyLength,
        customProperties, indentWithIndicator, dumpComments);
  }
}

