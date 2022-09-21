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
package com.vga.sjl.external.org.snakeyaml.engine.v2.api.lowlevel;

import com.vga.sjl.external.org.snakeyaml.engine.v2.api.SjlExtDumpSettings;
import com.vga.sjl.external.org.snakeyaml.engine.v2.api.SjlExtStreamDataWriter;
import com.vga.sjl.external.org.snakeyaml.engine.v2.emitter.SjlExtEmitter;
import com.vga.sjl.external.org.snakeyaml.engine.v2.events.SjlExtEvent;

import java.io.StringWriter;
import java.util.Iterator;
import java.util.Objects;

/**
 * Emit the events into a data stream (opposite for Parse)
 */
public class SjlExtPresent {

  private final SjlExtDumpSettings settings;

  /**
   * Create Present (emitter)
   *
   * @param settings - configuration
   */
  public SjlExtPresent(SjlExtDumpSettings settings) {
    Objects.requireNonNull(settings, "DumpSettings cannot be null");
    this.settings = settings;
  }

  public String emitToString(Iterator<SjlExtEvent> events) {
    Objects.requireNonNull(events, "events cannot be null");
    SjlExtStreamToStringWriter writer = new SjlExtStreamToStringWriter();
    final SjlExtEmitter emitter = new SjlExtEmitter(settings, writer);
    events.forEachRemaining(emitter::emit);
    return writer.toString();
  }

}

/**
 * Internal helper class to support emitting to String
 */
class SjlExtStreamToStringWriter extends StringWriter implements SjlExtStreamDataWriter {

}

