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
package com.vga.sjl.external.org.snakeyaml.engine.v2.comments;

import com.vga.sjl.external.org.snakeyaml.engine.v2.events.SjlExtCommentEvent;
import com.vga.sjl.external.org.snakeyaml.engine.v2.exceptions.SjlExtMark;

import java.util.Objects;
import java.util.Optional;

/**
 * A comment line. May be a block comment, blank line, or inline comment.
 */
public class SjlExtCommentLine {

  private final Optional<SjlExtMark> startMark;
  private final Optional<SjlExtMark> endMark;
  private final String value;
  private final SjlExtCommentType commentType;

  public SjlExtCommentLine(SjlExtCommentEvent event) {
    this(event.getStartMark(), event.getEndMark(), event.getValue(), event.getCommentType());
  }

  public SjlExtCommentLine(Optional<SjlExtMark> startMark, Optional<SjlExtMark> endMark, String value,
                           SjlExtCommentType commentType) {
    Objects.requireNonNull(startMark);
    this.startMark = startMark;
    Objects.requireNonNull(endMark);
    this.endMark = endMark;
    Objects.requireNonNull(value);
    this.value = value;
    Objects.requireNonNull(commentType);
    this.commentType = commentType;
  }

  public Optional<SjlExtMark> getEndMark() {
    return endMark;
  }

  public Optional<SjlExtMark> getStartMark() {
    return startMark;
  }

  public SjlExtCommentType getCommentType() {
    return commentType;
  }

  /**
   * Value of this comment.
   *
   * @return comment's value.
   */
  public String getValue() {
    return value;
  }

  public String toString() {
    return "<" + this.getClass().getName() + " (type=" + getCommentType() + ", value=" + getValue()
        + ")>";
  }
}
