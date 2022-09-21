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
import com.vga.sjl.external.org.snakeyaml.engine.v2.common.SjlExtFlowStyle;
import com.vga.sjl.external.org.snakeyaml.engine.v2.exceptions.SjlExtMark;

import java.util.Objects;
import java.util.Optional;

/**
 * Base class for the start events of the collection nodes.
 */
public abstract class SjlExtCollectionStartEvent extends SjlExtNodeEvent {

  private final Optional<String> tag;
  // The implicit flag of a collection start event indicates if the tag may be
  // omitted when the collection is emitted
  private final boolean implicit;
  // flag indicates if a collection is block or flow
  private final SjlExtFlowStyle flowStyle;

  public SjlExtCollectionStartEvent(Optional<SjlExtAnchor> anchor, Optional<String> tag, boolean implicit,
                                    SjlExtFlowStyle flowStyle, Optional<SjlExtMark> startMark, Optional<SjlExtMark> endMark) {
    super(anchor, startMark, endMark);
    Objects.requireNonNull(tag);
    this.tag = tag;
    this.implicit = implicit;
    Objects.requireNonNull(flowStyle);
    this.flowStyle = flowStyle;
  }

  /**
   * Tag of this collection.
   *
   * @return The tag of this collection, or <code>empty</code> if no explicit tag is available.
   */
  public Optional<String> getTag() {
    return this.tag;
  }

  /**
   * <code>true</code> if the tag can be omitted while this collection is
   * emitted.
   *
   * @return True if the tag can be omitted while this collection is emitted.
   */
  public boolean isImplicit() {
    return this.implicit;
  }

  /**
   * <code>true</code> if this collection is in flow style, <code>false</code>
   * for block style.
   *
   * @return If this collection is in flow style.
   */
  public SjlExtFlowStyle getFlowStyle() {
    return this.flowStyle;
  }

  public boolean isFlow() {
    return SjlExtFlowStyle.FLOW == flowStyle;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    getAnchor().ifPresent(a -> builder.append(" &" + a));
    if (!implicit) {
      getTag().ifPresent(theTag -> builder.append(" <" + theTag + ">"));
    }
    return builder.toString();
  }
}
