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
package com.vga.sjl.external.org.snakeyaml.engine.v2.events;

import com.vga.sjl.external.org.snakeyaml.engine.v2.exceptions.SjlExtMark;

import java.util.Optional;

/**
 * Basic unit of output from a {@link com.vga.sjl.external.org.snakeyaml.engine.v2.parser.SjlExtParser} or input of a {@link
 * com.vga.sjl.external.org.snakeyaml.engine.v2.emitter.SjlExtEmitter}.
 */
public abstract class SjlExtEvent {

  /**
   * ID of a non-abstract Event
   */
  public enum ID {
    Alias, Comment, DocumentEnd, DocumentStart, MappingEnd, MappingStart,
    Scalar, SequenceEnd, SequenceStart, StreamEnd, StreamStart //NOSONAR
  }

  private final Optional<SjlExtMark> startMark;
  private final Optional<SjlExtMark> endMark;

  public SjlExtEvent(Optional<SjlExtMark> startMark, Optional<SjlExtMark> endMark) {
    if ((startMark.isPresent() && !endMark.isPresent()) || (!startMark.isPresent()
        && endMark.isPresent())) {
      throw new NullPointerException("Both marks must be either present or absent.");
    }
    this.startMark = startMark;
    this.endMark = endMark;
  }

  /*
   * Create Node for emitter
   */
  public SjlExtEvent() {
    this(Optional.empty(), Optional.empty());
  }

  public Optional<SjlExtMark> getStartMark() {
    return startMark;
  }

  public Optional<SjlExtMark> getEndMark() {
    return endMark;
  }

  /**
   * Get the type (kind) if this Event
   *
   * @return the ID of this Event
   */
  public abstract ID getEventId();
}
