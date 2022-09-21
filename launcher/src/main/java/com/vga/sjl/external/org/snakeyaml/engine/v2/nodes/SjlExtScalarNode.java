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
package com.vga.sjl.external.org.snakeyaml.engine.v2.nodes;

import com.vga.sjl.external.org.snakeyaml.engine.v2.common.SjlExtScalarStyle;
import com.vga.sjl.external.org.snakeyaml.engine.v2.exceptions.SjlExtMark;

import java.util.Objects;
import java.util.Optional;

/**
 * Represents a scalar node.
 * <p>
 * Scalar nodes form the leaves in the node graph.
 * </p>
 */
public class SjlExtScalarNode extends SjlExtNode {

  private final SjlExtScalarStyle style;
  private final String value;

  public SjlExtScalarNode(SjlExtTag tag, boolean resolved, String value, SjlExtScalarStyle style,
                          Optional<SjlExtMark> startMark, Optional<SjlExtMark> endMark) {
    super(tag, startMark, endMark);
    Objects.requireNonNull(value, "value in a Node is required.");
    this.value = value;
    Objects.requireNonNull(style, "Scalar style must be provided.");
    this.style = style;
    this.resolved = resolved;
  }

  public SjlExtScalarNode(SjlExtTag tag, String value, SjlExtScalarStyle style) {
    this(tag, true, value, style, Optional.empty(), Optional.empty());
  }

  /**
   * Get scalar style of this node.
   *
   * @return style of this scalar node
   * @see com.vga.sjl.external.org.snakeyaml.engine.v2.events.SjlExtScalarEvent Flow  styles - https://yaml.org/spec/1.2/spec.html#id2786942
   * Block styles - https://yaml.org/spec/1.2/spec.html#id2793652
   */
  public SjlExtScalarStyle getScalarStyle() {
    return style;
  }

  @Override
  public SjlExtNodeType getNodeType() {
    return SjlExtNodeType.SCALAR;
  }

  /**
   * Value of this scalar.
   *
   * @return Scalar's value.
   */
  public String getValue() {
    return value;
  }

  public String toString() {
    return "<" + this.getClass().getName() + " (tag=" + getTag() + ", value=" + getValue()
        + ")>";
  }

  public boolean isPlain() {
    return style == SjlExtScalarStyle.PLAIN;
  }
}
