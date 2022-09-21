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
 * Represents a map.
 * <p>
 * A map is a collection of unsorted key-value pairs.
 * </p>
 */
public class SjlExtMappingNode extends SjlExtCollectionNode<SjlExtNodeTuple> {

  private List<SjlExtNodeTuple> value;

  public SjlExtMappingNode(SjlExtTag tag, boolean resolved, List<SjlExtNodeTuple> value, SjlExtFlowStyle flowStyle,
                           Optional<SjlExtMark> startMark, Optional<SjlExtMark> endMark) {
    super(tag, flowStyle, startMark, endMark);
    Objects.requireNonNull(value);
    this.value = value;
    this.resolved = resolved;
  }

  public SjlExtMappingNode(SjlExtTag tag, List<SjlExtNodeTuple> value, SjlExtFlowStyle flowStyle) {
    this(tag, true, value, flowStyle, Optional.empty(), Optional.empty());
  }

  @Override
  public SjlExtNodeType getNodeType() {
    return SjlExtNodeType.MAPPING;
  }

  /**
   * Returns the entries of this map.
   *
   * @return List of entries.
   */
  public List<SjlExtNodeTuple> getValue() {
    return value;
  }

  public void setValue(List<SjlExtNodeTuple> mergedValue) {
    value = mergedValue;
  }

  @Override
  public String toString() {
    String values;
    StringBuilder buf = new StringBuilder();
    for (SjlExtNodeTuple node : getValue()) {
      buf.append("{ key=");
      buf.append(node.getKeyNode());
      buf.append("; value=");
      if (node.getValueNode() instanceof SjlExtCollectionNode) {
        // to avoid overflow in case of recursive structures
        buf.append(System.identityHashCode(node.getValueNode()));
      } else {
        buf.append(node);
      }
      buf.append(" }");
    }
    values = buf.toString();
    return "<" + this.getClass().getName() + " (tag=" + getTag() + ", values=" + values + ")>";
  }
}
