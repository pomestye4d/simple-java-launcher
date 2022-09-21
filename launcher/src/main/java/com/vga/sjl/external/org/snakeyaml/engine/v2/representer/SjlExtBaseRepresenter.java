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
package com.vga.sjl.external.org.snakeyaml.engine.v2.representer;

import com.vga.sjl.external.org.snakeyaml.engine.v2.api.SjlExtRepresentToNode;
import com.vga.sjl.external.org.snakeyaml.engine.v2.common.SjlExtFlowStyle;
import com.vga.sjl.external.org.snakeyaml.engine.v2.common.SjlExtScalarStyle;
import com.vga.sjl.external.org.snakeyaml.engine.v2.exceptions.SjlExtYamlEngineException;
import com.vga.sjl.external.org.snakeyaml.engine.v2.nodes.*;

import java.util.*;

/**
 * Represent basic YAML structures: scalar, sequence, mapping
 */
public abstract class SjlExtBaseRepresenter {

  /**
   * Keep representers which must match the class exactly
   */
  protected final Map<Class<?>, SjlExtRepresentToNode> representers = new HashMap();
  /**
   * in Java 'null' is not a type. So we have to keep the null representer separately otherwise it
   * will coincide with the default representer which is stored with the key null.
   */
  protected SjlExtRepresentToNode nullRepresenter;
  // the order is important (map can be also a sequence of key-values)
  /**
   * Keep representers which match a parent of the class to be represented
   */
  protected final Map<Class<?>, SjlExtRepresentToNode> parentClassRepresenters = new LinkedHashMap();
  protected SjlExtScalarStyle defaultScalarStyle = SjlExtScalarStyle.PLAIN;
  protected SjlExtFlowStyle defaultFlowStyle = SjlExtFlowStyle.AUTO;
  protected final Map<Object, SjlExtNode> representedObjects = new IdentityHashMap<Object, SjlExtNode>() {
    @Override
    public SjlExtNode put(Object key, SjlExtNode value) {
      return super.put(key, new SjlExtAnchorNode(value));
    }
  };

  protected Object objectToRepresent;

  /**
   * Represent the provided Java instance to a Node
   *
   * @param data - Java instance to be represented
   * @return The Node to be serialized
   */
  public SjlExtNode represent(Object data) {
    SjlExtNode node = representData(data);
    representedObjects.clear();
    objectToRepresent = null;
    return node;
  }

  /**
   * Find the representer which is suitable to represent the internal structure of the provided
   * instance to a Node
   *
   * @param data - the data to be serialized
   * @return RepresentToNode to call to create a Node
   */
  protected Optional<SjlExtRepresentToNode> findRepresenterFor(Object data) {
    Class<?> clazz = data.getClass();
    // check the same class
    if (representers.containsKey(clazz)) {
      return Optional.of(representers.get(clazz));
    } else {
      // check the parents
      for (Map.Entry<Class<?>, SjlExtRepresentToNode> parentRepresenterEntry : parentClassRepresenters.entrySet()) {
        if (parentRepresenterEntry.getKey().isInstance(data)) {
          return Optional.of(parentRepresenterEntry.getValue());
        }
      }
      return Optional.empty();
    }
  }

  protected final SjlExtNode representData(Object data) {
    objectToRepresent = data;
    // check for identity
    if (representedObjects.containsKey(objectToRepresent)) {
      return representedObjects.get(objectToRepresent);
    }
    // check for null first
    if (data == null) {
      return nullRepresenter.representData(null);
    }
    SjlExtRepresentToNode representer = findRepresenterFor(data)
        .orElseThrow(
            () -> new SjlExtYamlEngineException("Representer is not defined for " + data.getClass()));
    return representer.representData(data);
  }

  protected SjlExtNode representScalar(SjlExtTag tag, String value, SjlExtScalarStyle style) {
    if (style == SjlExtScalarStyle.PLAIN) {
      style = this.defaultScalarStyle;
    }
    return new SjlExtScalarNode(tag, value, style);
  }

  protected SjlExtNode representScalar(SjlExtTag tag, String value) {
    return representScalar(tag, value, SjlExtScalarStyle.PLAIN);
  }

  protected SjlExtNode representSequence(SjlExtTag tag, Iterable<?> sequence, SjlExtFlowStyle flowStyle) {
    int size = 10;// default for ArrayList
    if (sequence instanceof List<?>) {
      size = ((List<?>) sequence).size();
    }
    List<SjlExtNode> value = new ArrayList<>(size);
    SjlExtSequenceNode node = new SjlExtSequenceNode(tag, value, flowStyle);
    representedObjects.put(objectToRepresent, node);
    SjlExtFlowStyle bestStyle = SjlExtFlowStyle.FLOW;
    for (Object item : sequence) {
      SjlExtNode nodeItem = representData(item);
      if (!(nodeItem instanceof SjlExtScalarNode && ((SjlExtScalarNode) nodeItem).isPlain())) {
        bestStyle = SjlExtFlowStyle.BLOCK;
      }
      value.add(nodeItem);
    }
    if (flowStyle == SjlExtFlowStyle.AUTO) {
      if (defaultFlowStyle != SjlExtFlowStyle.AUTO) {
        node.setFlowStyle(defaultFlowStyle);
      } else {
        node.setFlowStyle(bestStyle);
      }
    }
    return node;
  }

  protected SjlExtNodeTuple representMappingEntry(Map.Entry<?, ?> entry) {
    return new SjlExtNodeTuple(representData(entry.getKey()), representData(entry.getValue()));
  }

  protected SjlExtNode representMapping(SjlExtTag tag, Map<?, ?> mapping, SjlExtFlowStyle flowStyle) {
    List<SjlExtNodeTuple> value = new ArrayList<>(mapping.size());
    SjlExtMappingNode node = new SjlExtMappingNode(tag, value, flowStyle);
    representedObjects.put(objectToRepresent, node);
    SjlExtFlowStyle bestStyle = SjlExtFlowStyle.FLOW;
    for (Map.Entry<?, ?> entry : mapping.entrySet()) {
      SjlExtNodeTuple tuple = representMappingEntry(entry);
      if (!(tuple.getKeyNode() instanceof SjlExtScalarNode
          && ((SjlExtScalarNode) tuple.getKeyNode()).isPlain())) {
        bestStyle = SjlExtFlowStyle.BLOCK;
      }
      if (!(tuple.getValueNode() instanceof SjlExtScalarNode
          && ((SjlExtScalarNode) tuple.getValueNode()).isPlain())) {
        bestStyle = SjlExtFlowStyle.BLOCK;
      }
      value.add(tuple);
    }
    if (flowStyle == SjlExtFlowStyle.AUTO) {
      if (defaultFlowStyle != SjlExtFlowStyle.AUTO) {
        node.setFlowStyle(defaultFlowStyle);
      } else {
        node.setFlowStyle(bestStyle);
      }
    }
    return node;
  }
}
