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

import com.vga.sjl.external.org.snakeyaml.engine.v2.comments.SjlExtCommentType;
import com.vga.sjl.external.org.snakeyaml.engine.v2.exceptions.SjlExtMark;

import java.util.Objects;
import java.util.Optional;

public final class SjlExtCommentToken extends SjlExtToken {

  private final SjlExtCommentType type;
  private final String value;

  public SjlExtCommentToken(SjlExtCommentType type, String value, Optional<SjlExtMark> startMark,
                            Optional<SjlExtMark> endMark) {
    super(startMark, endMark);
    Objects.requireNonNull(type);
    this.type = type;
    Objects.requireNonNull(value);
    this.value = value;
  }

  public SjlExtCommentType getCommentType() {
    return this.type;
  }

  public String getValue() {
    return this.value;
  }

  @Override
  public ID getTokenId() {
    return ID.Comment;
  }
}
