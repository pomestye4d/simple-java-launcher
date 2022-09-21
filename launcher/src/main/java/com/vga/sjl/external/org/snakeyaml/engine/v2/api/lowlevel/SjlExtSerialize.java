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
import com.vga.sjl.external.org.snakeyaml.engine.v2.emitter.SjlExtEmitable;
import com.vga.sjl.external.org.snakeyaml.engine.v2.events.SjlExtEvent;
import com.vga.sjl.external.org.snakeyaml.engine.v2.nodes.SjlExtNode;
import com.vga.sjl.external.org.snakeyaml.engine.v2.serializer.SjlExtSerializer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class SjlExtSerialize {

  private final SjlExtDumpSettings settings;

  /**
   * Create instance with provided {@link SjlExtDumpSettings}
   *
   * @param settings - configuration
   */
  public SjlExtSerialize(SjlExtDumpSettings settings) {
    Objects.requireNonNull(settings, "DumpSettings cannot be null");
    this.settings = settings;
  }

  /**
   * Serialize a {@link SjlExtNode} and produce events.
   *
   * @param node - {@link SjlExtNode} to serialize
   * @return serialized events
   * @see <a href="http://www.yaml.org/spec/1.2/spec.html#id2762107">Processing Overview</a>
   */
  public List<SjlExtEvent> serializeOne(SjlExtNode node) {
    Objects.requireNonNull(node, "Node cannot be null");
    return serializeAll(Collections.singletonList(node));
  }

  /**
   * Serialize {@link SjlExtNode}s and produce events.
   *
   * @param nodes - {@link SjlExtNode}s to serialize
   * @return serialized events
   * @see <a href="http://www.yaml.org/spec/1.2/spec.html#id2762107">Processing Overview</a>
   */
  public List<SjlExtEvent> serializeAll(List<SjlExtNode> nodes) {
    Objects.requireNonNull(nodes, "Nodes cannot be null");
    SjlExtEmitableEvents emitableEvents = new SjlExtEmitableEvents();
    SjlExtSerializer serializer = new SjlExtSerializer(settings, emitableEvents);
    serializer.emitStreamStart();
    for (SjlExtNode node : nodes) {
      serializer.serializeDocument(node);
    }
    serializer.emitStreamEnd();
    return emitableEvents.getEvents();
  }
}

class SjlExtEmitableEvents implements SjlExtEmitable {

  private final List<SjlExtEvent> events = new ArrayList<>();

  @Override
  public void emit(SjlExtEvent event) {
    events.add(event);
  }

  public List<SjlExtEvent> getEvents() {
    return events;
  }
}

