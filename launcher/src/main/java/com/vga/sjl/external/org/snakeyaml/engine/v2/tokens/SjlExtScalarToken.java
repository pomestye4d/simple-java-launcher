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

import com.vga.sjl.external.org.snakeyaml.engine.v2.common.SjlExtScalarStyle;
import com.vga.sjl.external.org.snakeyaml.engine.v2.exceptions.SjlExtMark;

import java.util.Objects;
import java.util.Optional;

public final class SjlExtScalarToken extends SjlExtToken {

  private final String value;
  private final boolean plain;
  private final SjlExtScalarStyle style;

  public SjlExtScalarToken(String value, boolean plain, Optional<SjlExtMark> startMark,
                           Optional<SjlExtMark> endMark) {
    this(value, plain, SjlExtScalarStyle.PLAIN, startMark, endMark);
  }

  public SjlExtScalarToken(String value, boolean plain, SjlExtScalarStyle style, Optional<SjlExtMark> startMark,
                           Optional<SjlExtMark> endMark) {
    super(startMark, endMark);
    Objects.requireNonNull(value);
    this.value = value;
    this.plain = plain;
    Objects.requireNonNull(style);
    this.style = style;
  }

  public boolean isPlain() {
    return this.plain;
  }

  public String getValue() {
    return this.value;
  }

  public SjlExtScalarStyle getStyle() {
    return this.style;
  }

  @Override
  public ID getTokenId() {
    return ID.Scalar;
  }

  @Override
  public String toString() {
    return getTokenId().toString() + " plain=" + plain + " style=" + style + " value=" + value;
  }
}
