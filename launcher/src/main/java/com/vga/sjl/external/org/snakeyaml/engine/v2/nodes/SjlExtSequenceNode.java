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

import com.vga.sjl.external.org.snakeyaml.engine.v2.common.SjlExtFlowStyle;
import com.vga.sjl.external.org.snakeyaml.engine.v2.exceptions.SjlExtMark;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Represents a sequence.
 * <p>
 * A sequence is a ordered collection of nodes.
 * </p>
 */
public class SjlExtSequenceNode extends SjlExtCollectionNode<SjlExtNode> {

  private final List<SjlExtNode> value;

  public SjlExtSequenceNode(SjlExtTag tag, boolean resolved, List<SjlExtNode> value,
                            SjlExtFlowStyle flowStyle, Optional<SjlExtMark> startMark, Optional<SjlExtMark> endMark) {
    super(tag, flowStyle, startMark, endMark);
    Objects.requireNonNull(value, "value in a Node is required.");
    this.value = value;
    this.resolved = resolved;
  }

  public SjlExtSequenceNode(SjlExtTag tag, List<SjlExtNode> value, SjlExtFlowStyle flowStyle) {
    this(tag, true, value, flowStyle, Optional.empty(), Optional.empty());
  }

  @Override
  public SjlExtNodeType getNodeType() {
    return SjlExtNodeType.SEQUENCE;
  }

  /**
   * Returns the elements in this sequence.
   *
   * @return Nodes in the specified order.
   */
  public List<SjlExtNode> getValue() {
    return value;
  }

  public String toString() {
    return "<" + this.getClass().getName() + " (tag=" + getTag() + ", value=" + getValue()
        + ")>";
  }
}
