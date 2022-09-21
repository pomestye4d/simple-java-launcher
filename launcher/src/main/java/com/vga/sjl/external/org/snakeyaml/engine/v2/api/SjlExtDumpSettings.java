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
import com.vga.sjl.external.org.snakeyaml.engine.v2.nodes.SjlExtTag;
import com.vga.sjl.external.org.snakeyaml.engine.v2.resolver.SjlExtScalarResolver;
import com.vga.sjl.external.org.snakeyaml.engine.v2.serializer.SjlExtAnchorGenerator;

import java.util.Map;
import java.util.Optional;

/**
 * Fine-tuning serializing/dumping Description for all the fields can be found in the builder
 */
public final class SjlExtDumpSettings {

  private final boolean explicitStart;
  private final boolean explicitEnd;
  private final SjlExtNonPrintableStyle nonPrintableStyle;
  private final Optional<SjlExtTag> explicitRootTag;
  private final SjlExtAnchorGenerator anchorGenerator;
  private final Optional<SjlExtSpecVersion> yamlDirective;
  private final Map<String, String> tagDirective;
  private final SjlExtScalarResolver scalarResolver;
  private final SjlExtFlowStyle defaultFlowStyle;
  private final SjlExtScalarStyle defaultScalarStyle;

  //emitter
  private final boolean canonical;
  private final boolean multiLineFlow;
  private final boolean useUnicodeEncoding;
  private final int indent;
  private final int indicatorIndent;
  private final int width;
  private final String bestLineBreak;
  private final boolean splitLines;
  private final int maxSimpleKeyLength;
  private final boolean indentWithIndicator;
  private final boolean dumpComments;

  //general
  private final Map<SjlExtSettingKey, Object> customProperties;

  SjlExtDumpSettings(boolean explicitStart, boolean explicitEnd, Optional<SjlExtTag> explicitRootTag,
                     SjlExtAnchorGenerator anchorGenerator, Optional<SjlExtSpecVersion> yamlDirective,
                     Map<String, String> tagDirective,
                     SjlExtScalarResolver scalarResolver, SjlExtFlowStyle defaultFlowStyle, SjlExtScalarStyle defaultScalarStyle,
                     SjlExtNonPrintableStyle nonPrintableStyle,
                     //emitter
                     boolean canonical, boolean multiLineFlow, boolean useUnicodeEncoding,
                     int indent, int indicatorIndent, int width, String bestLineBreak, boolean splitLines,
                     int maxSimpleKeyLength,
                     Map<SjlExtSettingKey, Object> customProperties, boolean indentWithIndicator, boolean dumpComments
  ) {
    this.explicitStart = explicitStart;
    this.explicitEnd = explicitEnd;
    this.nonPrintableStyle = nonPrintableStyle;
    this.explicitRootTag = explicitRootTag;
    this.anchorGenerator = anchorGenerator;
    this.yamlDirective = yamlDirective;
    this.tagDirective = tagDirective;
    this.scalarResolver = scalarResolver;
    this.defaultFlowStyle = defaultFlowStyle;
    this.defaultScalarStyle = defaultScalarStyle;
    this.canonical = canonical;
    this.multiLineFlow = multiLineFlow;
    this.useUnicodeEncoding = useUnicodeEncoding;
    this.indent = indent;
    this.indicatorIndent = indicatorIndent;
    this.width = width;
    this.bestLineBreak = bestLineBreak;
    this.splitLines = splitLines;
    this.maxSimpleKeyLength = maxSimpleKeyLength;
    this.customProperties = customProperties;
    this.indentWithIndicator = indentWithIndicator;
    this.dumpComments = dumpComments;
  }

  public static SjlExtDumpSettingsBuilder builder() {
    return new SjlExtDumpSettingsBuilder();
  }

  public SjlExtFlowStyle getDefaultFlowStyle() {
    return defaultFlowStyle;
  }

  public SjlExtScalarStyle getDefaultScalarStyle() {
    return defaultScalarStyle;
  }

  public boolean isExplicitStart() {
    return explicitStart;
  }

  public SjlExtAnchorGenerator getAnchorGenerator() {
    return anchorGenerator;
  }

  public SjlExtScalarResolver getScalarResolver() {
    return scalarResolver;
  }

  public boolean isExplicitEnd() {
    return explicitEnd;
  }

  public Optional<SjlExtTag> getExplicitRootTag() {
    return explicitRootTag;
  }

  public Optional<SjlExtSpecVersion> getYamlDirective() {
    return yamlDirective;
  }

  public Map<String, String> getTagDirective() {
    return tagDirective;
  }

  public boolean isCanonical() {
    return canonical;
  }

  public boolean isMultiLineFlow() {
    return multiLineFlow;
  }

  public boolean isUseUnicodeEncoding() {
    return useUnicodeEncoding;
  }

  public int getIndent() {
    return indent;
  }

  public int getIndicatorIndent() {
    return indicatorIndent;
  }

  public int getWidth() {
    return width;
  }

  public String getBestLineBreak() {
    return bestLineBreak;
  }

  public boolean isSplitLines() {
    return splitLines;
  }

  public int getMaxSimpleKeyLength() {
    return maxSimpleKeyLength;
  }

  public SjlExtNonPrintableStyle getNonPrintableStyle() {
    return nonPrintableStyle;
  }

  public Object getCustomProperty(SjlExtSettingKey key) {
    return customProperties.get(key);
  }

  public boolean getIndentWithIndicator() {
    return indentWithIndicator;
  }

  public boolean getDumpComments() {
    return dumpComments;
  }
}

