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
import com.vga.sjl.external.org.snakeyaml.engine.v2.exceptions.SjlExtMark;

import java.util.Objects;
import java.util.Optional;

/**
 * Base class for all events that mark the beginning of a node.
 */
public abstract class SjlExtNodeEvent extends SjlExtEvent {

  private final Optional<SjlExtAnchor> anchor;

  public SjlExtNodeEvent(Optional<SjlExtAnchor> anchor, Optional<SjlExtMark> startMark, Optional<SjlExtMark> endMark) {
    super(startMark, endMark);
    Objects.requireNonNull(anchor);
    this.anchor = anchor;
  }

  /**
   * Node anchor by which this node might later be referenced by a {@link SjlExtAliasEvent}.
   * <p>
   * Note that {@link SjlExtAliasEvent}s are by it self <code>NodeEvent</code>s and use this property to
   * indicate the referenced anchor.
   *
   * @return Anchor of this node or <code>null</code> if no anchor is defined.
   */
  public Optional<SjlExtAnchor> getAnchor() {
    return this.anchor;
  }
}
