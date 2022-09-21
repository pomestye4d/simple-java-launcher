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

import com.vga.sjl.external.org.snakeyaml.engine.v2.common.SjlExtSpecVersion;
import com.vga.sjl.external.org.snakeyaml.engine.v2.env.SjlExtEnvConfig;
import com.vga.sjl.external.org.snakeyaml.engine.v2.nodes.SjlExtTag;
import com.vga.sjl.external.org.snakeyaml.engine.v2.resolver.SjlExtScalarResolver;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.UnaryOperator;

/**
 * Fine-tuning parsing/loading Description for all the fields can be found in the builder
 */
public final class SjlExtLoadSettings {

  private final String label;
  private final Map<SjlExtTag, SjlExtConstructNode> tagConstructors;
  private final SjlExtScalarResolver scalarResolver;
  private final IntFunction<List> defaultList;
  private final IntFunction<Set> defaultSet;
  private final IntFunction<Map> defaultMap;
  private final UnaryOperator<SjlExtSpecVersion> versionFunction;
  private final Integer bufferSize;
  private final boolean allowDuplicateKeys;
  private final boolean allowRecursiveKeys;
  private final boolean parseComments;
  private final int maxAliasesForCollections;
  private final boolean useMarks;
  private final Optional<SjlExtEnvConfig> envConfig;

  //general
  private final Map<SjlExtSettingKey, Object> customProperties;

  SjlExtLoadSettings(String label, Map<SjlExtTag, SjlExtConstructNode> tagConstructors,
                     SjlExtScalarResolver scalarResolver, IntFunction<List> defaultList,
                     IntFunction<Set> defaultSet, IntFunction<Map> defaultMap,
                     UnaryOperator<SjlExtSpecVersion> versionFunction, Integer bufferSize,
                     boolean allowDuplicateKeys, boolean allowRecursiveKeys, int maxAliasesForCollections,
                     boolean useMarks, Map<SjlExtSettingKey, Object> customProperties, Optional<SjlExtEnvConfig> envConfig,
                     boolean parseComments) {
    this.label = label;
    this.tagConstructors = tagConstructors;
    this.scalarResolver = scalarResolver;
    this.defaultList = defaultList;
    this.defaultSet = defaultSet;
    this.defaultMap = defaultMap;
    this.versionFunction = versionFunction;
    this.bufferSize = bufferSize;
    this.allowDuplicateKeys = allowDuplicateKeys;
    this.allowRecursiveKeys = allowRecursiveKeys;
    this.parseComments = parseComments;
    this.maxAliasesForCollections = maxAliasesForCollections;
    this.useMarks = useMarks;
    this.customProperties = customProperties;
    this.envConfig = envConfig;
  }

  public static final SjlExtLoadSettingsBuilder builder() {
    return new SjlExtLoadSettingsBuilder();
  }

  public String getLabel() {
    return label;
  }

  public Map<SjlExtTag, SjlExtConstructNode> getTagConstructors() {
    return tagConstructors;
  }

  public SjlExtScalarResolver getScalarResolver() {
    return scalarResolver;
  }

  public IntFunction<List> getDefaultList() {
    return defaultList;
  }

  public IntFunction<Set> getDefaultSet() {
    return defaultSet;
  }

  public IntFunction<Map> getDefaultMap() {
    return defaultMap;
  }

  public Integer getBufferSize() {
    return bufferSize;
  }

  public boolean getAllowDuplicateKeys() {
    return allowDuplicateKeys;
  }

  public boolean getAllowRecursiveKeys() {
    return allowRecursiveKeys;
  }

  public boolean getUseMarks() {
    return useMarks;
  }

  public Function<SjlExtSpecVersion, SjlExtSpecVersion> getVersionFunction() {
    return versionFunction;
  }

  public Object getCustomProperty(SjlExtSettingKey key) {
    return customProperties.get(key);
  }

  public int getMaxAliasesForCollections() {
    return maxAliasesForCollections;
  }

  public Optional<SjlExtEnvConfig> getEnvConfig() {
    return envConfig;
  }

  public boolean getParseComments() {
    return parseComments;
  }
}

