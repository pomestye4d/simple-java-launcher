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

import com.vga.sjl.external.org.snakeyaml.engine.v2.comments.SjlExtCommentType;
import com.vga.sjl.external.org.snakeyaml.engine.v2.exceptions.SjlExtMark;

import java.util.Objects;
import java.util.Optional;

/**
 * Marks a comment block value.
 */
public final class SjlExtCommentEvent extends SjlExtEvent {

  private final SjlExtCommentType type;
  private final String value;

  public SjlExtCommentEvent(SjlExtCommentType type, String value, Optional<SjlExtMark> startMark,
                            Optional<SjlExtMark> endMark) {
    super(startMark, endMark);
    Objects.requireNonNull(type);
    this.type = type;
    Objects.requireNonNull(value);
    this.value = value;
  }

  /**
   * String representation of the value.
   * <p>
   * Without quotes and escaping.
   * </p>
   *
   * @return Value a comment line string without the leading '#' or a blank line.
   */
  public String getValue() {
    return this.value;
  }

  /**
   * The comment type.
   *
   * @return the commentType.
   */
  public SjlExtCommentType getCommentType() {
    return this.type;
  }

  @Override
  public ID getEventId() {
    return ID.Comment;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder("=COM ");
    builder.append(type);
    builder.append(" ");
    builder.append(value);
    return builder.toString();
  }
}
