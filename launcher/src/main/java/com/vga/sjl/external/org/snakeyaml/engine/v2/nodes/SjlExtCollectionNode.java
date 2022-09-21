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
 * Base class for the two collection types {@link SjlExtMappingNode mapping} and {@link SjlExtSequenceNode
 * collection}.
 */
public abstract class SjlExtCollectionNode<T> extends SjlExtNode {

  private SjlExtFlowStyle flowStyle;

  public SjlExtCollectionNode(SjlExtTag tag, SjlExtFlowStyle flowStyle, Optional<SjlExtMark> startMark,
                              Optional<SjlExtMark> endMark) {
    super(tag, startMark, endMark);
    setFlowStyle(flowStyle);
  }

  /**
   * Returns the elements in this sequence.
   *
   * @return Nodes in the specified order.
   */
  public abstract List<T> getValue();

  /**
   * Serialization style of this collection.
   *
   * @return <code>true</code> for flow style, <code>false</code> for block
   * style.
   */
  public SjlExtFlowStyle getFlowStyle() {
    return flowStyle;
  }

  public void setFlowStyle(SjlExtFlowStyle flowStyle) {
    Objects.requireNonNull(flowStyle, "Flow style must be provided.");
    this.flowStyle = flowStyle;
  }

  public void setEndMark(Optional<SjlExtMark> endMark) {
    this.endMark = endMark;
  }
}
