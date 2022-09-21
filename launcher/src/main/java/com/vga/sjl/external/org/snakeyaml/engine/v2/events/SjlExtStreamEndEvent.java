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

import com.vga.sjl.external.org.snakeyaml.engine.v2.exceptions.SjlExtMark;

import java.util.Optional;

/**
 * Marks the end of a stream that might have contained multiple documents.
 * <p>
 * This event is the last event that a parser emits. Together with {@link SjlExtStreamStartEvent} (which
 * is the first event a parser emits) they mark the beginning and the end of a stream of documents.
 * </p>
 * <p>
 * See {@link SjlExtEvent} for an exemplary output.
 * </p>
 */
public final class SjlExtStreamEndEvent extends SjlExtEvent {

  public SjlExtStreamEndEvent(Optional<SjlExtMark> startMark, Optional<SjlExtMark> endMark) {
    super(startMark, endMark);
  }

  public SjlExtStreamEndEvent() {
    super();
  }

  @Override
  public ID getEventId() {
    return ID.StreamEnd;
  }

  @Override
  public String toString() {
    return "-STR";
  }
}
