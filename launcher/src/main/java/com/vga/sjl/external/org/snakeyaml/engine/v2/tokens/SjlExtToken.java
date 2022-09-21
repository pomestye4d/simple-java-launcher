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

import java.util.Objects;
import java.util.Optional;

/**
 * A unit of YAML data
 */
public abstract class SjlExtToken {

  public enum ID {
    Alias("<alias>"), //NOSONAR
    Anchor("<anchor>"), //NOSONAR
    BlockEnd("<block end>"), //NOSONAR
    BlockEntry("-"), //NOSONAR
    BlockMappingStart("<block mapping start>"), //NOSONAR
    BlockSequenceStart("<block sequence start>"), //NOSONAR
    Directive("<directive>"), //NOSONAR
    DocumentEnd("<document end>"), //NOSONAR
    DocumentStart("<document start>"), //NOSONAR
    FlowEntry(","), //NOSONAR
    FlowMappingEnd("}"), //NOSONAR
    FlowMappingStart("{"), //NOSONAR
    FlowSequenceEnd("]"), //NOSONAR
    FlowSequenceStart("["), //NOSONAR
    Key("?"), //NOSONAR
    Scalar("<scalar>"), //NOSONAR
    StreamEnd("<stream end>"), //NOSONAR
    StreamStart("<stream start>"), //NOSONAR
    Tag("<tag>"), //NOSONAR
    Comment("#"),
    Value(":"); //NOSONAR

    private final String description;

    ID(String s) {
      description = s;
    }

    @Override
    public String toString() {
      return description;
    }
  }

  private final Optional<SjlExtMark> startMark;
  private final Optional<SjlExtMark> endMark;

  public SjlExtToken(Optional<SjlExtMark> startMark, Optional<SjlExtMark> endMark) {
    Objects.requireNonNull(startMark);
    Objects.requireNonNull(endMark);
    this.startMark = startMark;
    this.endMark = endMark;
  }

  public Optional<SjlExtMark> getStartMark() {
    return startMark;
  }

  public Optional<SjlExtMark> getEndMark() {
    return endMark;
  }

  /**
   * For error reporting.
   *
   * @return ID of this token
   */
  public abstract ID getTokenId();

  @Override
  public String toString() {
    return getTokenId().toString();
  }
}
