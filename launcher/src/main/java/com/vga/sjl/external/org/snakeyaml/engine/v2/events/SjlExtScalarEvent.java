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

import com.vga.sjl.external.org.snakeyaml.engine.v2.common.SjlExtAnchor;
import com.vga.sjl.external.org.snakeyaml.engine.v2.common.SjlExtCharConstants;
import com.vga.sjl.external.org.snakeyaml.engine.v2.common.SjlExtScalarStyle;
import com.vga.sjl.external.org.snakeyaml.engine.v2.exceptions.SjlExtMark;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Marks a scalar value.
 */
public final class SjlExtScalarEvent extends SjlExtNodeEvent {

  private final Optional<String> tag;
  // style flag of a scalar event indicates the style of the scalar. Possible
  // values are None, '', '\'', '"', '|', '>'
  private final SjlExtScalarStyle style;
  private final String value;
  // The implicit flag of a scalar event is a pair of boolean values that
  // indicate if the tag may be omitted when the scalar is emitted in a plain
  // and non-plain style correspondingly.
  private final SjlExtImplicitTuple implicit;

  public SjlExtScalarEvent(Optional<SjlExtAnchor> anchor, Optional<String> tag, SjlExtImplicitTuple implicit,
                           String value, SjlExtScalarStyle style,
                           Optional<SjlExtMark> startMark, Optional<SjlExtMark> endMark) {
    super(anchor, startMark, endMark);
    Objects.requireNonNull(tag);
    this.tag = tag;
    this.implicit = implicit;
    Objects.requireNonNull(value);
    this.value = value;
    Objects.requireNonNull(style);
    this.style = style;
  }

  public SjlExtScalarEvent(Optional<SjlExtAnchor> anchor, Optional<String> tag, SjlExtImplicitTuple implicit,
                           String value, SjlExtScalarStyle style) {
    this(anchor, tag, implicit, value, style, Optional.empty(), Optional.empty());
  }

  /**
   * Tag of this scalar.
   *
   * @return The tag of this scalar, or <code>null</code> if no explicit tag is available.
   */
  public Optional<String> getTag() {
    return this.tag;
  }

  /**
   * Style of the scalar.
   * <dl>
   * <dt>null</dt>
   * <dd>Flow Style - Plain</dd>
   * <dt>'\''</dt>
   * <dd>Flow Style - Single-Quoted</dd>
   * <dt>'"'</dt>
   * <dd>Flow Style - Double-Quoted</dd>
   * <dt>'|'</dt>
   * <dd>Block Style - Literal</dd>
   * <dt>'&gt;'</dt>
   * <dd>Block Style - Folded</dd>
   * </dl>
   *
   * @return Style of the scalar.
   */
  public SjlExtScalarStyle getScalarStyle() {
    return this.style;
  }

  /**
   * String representation of the value.
   * <p>
   * Without quotes and escaping.
   * </p>
   *
   * @return Value as Unicode string.
   */
  public String getValue() {
    return this.value;
  }

  public SjlExtImplicitTuple getImplicit() {
    return this.implicit;
  }

  @Override
  public ID getEventId() {
    return ID.Scalar;
  }

  public boolean isPlain() {
    return style == SjlExtScalarStyle.PLAIN;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder("=VAL");
    getAnchor().ifPresent(a -> builder.append(" &" + a));
    if (implicit.bothFalse()) {
      getTag().ifPresent(theTag -> builder.append(" <" + theTag + ">"));
    }
    builder.append(" ");
    builder.append(getScalarStyle().toString());
    builder.append(escapedValue());
    return builder.toString();
  }

  // escape
  public String escapedValue() {
    return value.codePoints()
        .filter(i -> i < Character.MAX_VALUE)
        .mapToObj(ch -> SjlExtCharConstants.escapeChar(String.valueOf(Character.toChars(ch))))
        .collect(Collectors.joining(""));
  }
}
