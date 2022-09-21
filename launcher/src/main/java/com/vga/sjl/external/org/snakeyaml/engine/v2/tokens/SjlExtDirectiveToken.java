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
package com.vga.sjl.external.org.snakeyaml.engine.v2.tokens;

import com.vga.sjl.external.org.snakeyaml.engine.v2.exceptions.SjlExtMark;
import com.vga.sjl.external.org.snakeyaml.engine.v2.exceptions.SjlExtYamlEngineException;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class SjlExtDirectiveToken<T> extends SjlExtToken {

  private final String name;
  private final Optional<List<T>> value;

  public static final String YAML_DIRECTIVE = "YAML";
  public static final String TAG_DIRECTIVE = "TAG";

  public SjlExtDirectiveToken(String name, Optional<List<T>> value, Optional<SjlExtMark> startMark,
                              Optional<SjlExtMark> endMark) {
    super(startMark, endMark);
    Objects.requireNonNull(name);
    this.name = name;
    Objects.requireNonNull(value);
    if (value.isPresent() && value.get().size() != 2) {
      throw new SjlExtYamlEngineException(
          "Two strings/integers must be provided instead of " + value.get().size());
    }
    this.value = value;
  }

  public String getName() {
    return this.name;
  }

  public Optional<List<T>> getValue() {
    return this.value;
  }

  @Override
  public ID getTokenId() {
    return ID.Directive;
  }
}
