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
package com.vga.sjl.external.org.snakeyaml.engine.v2.serializer;

import com.vga.sjl.external.org.snakeyaml.engine.v2.api.SjlExtDumpSettings;
import com.vga.sjl.external.org.snakeyaml.engine.v2.comments.SjlExtCommentLine;
import com.vga.sjl.external.org.snakeyaml.engine.v2.common.SjlExtAnchor;
import com.vga.sjl.external.org.snakeyaml.engine.v2.emitter.SjlExtEmitable;
import com.vga.sjl.external.org.snakeyaml.engine.v2.events.*;
import com.vga.sjl.external.org.snakeyaml.engine.v2.nodes.*;

import java.util.*;

/**
 * Transform a Node Graph to Event stream and allow provided {@link SjlExtEmitable} to present the {@link
 * SjlExtEvent}s into the output stream
 */
public class SjlExtSerializer {

  private final SjlExtDumpSettings settings;
  private final SjlExtEmitable emitable;
  private final Set<SjlExtNode> serializedNodes;
  private final Map<SjlExtNode, SjlExtAnchor> anchors;

  /**
   * Create Serializer
   *
   * @param settings - dump configuration
   * @param emitable - destination for the event stream
   */
  public SjlExtSerializer(SjlExtDumpSettings settings, SjlExtEmitable emitable) {
    this.settings = settings;
    this.emitable = emitable;
    this.serializedNodes = new HashSet();
    this.anchors = new HashMap();
  }

  /**
   * Serialize document
   *
   * @param node - the document root
   */
  public void serializeDocument(SjlExtNode node) {
    this.emitable.emit(
        new SjlExtDocumentStartEvent(settings.isExplicitStart(), settings.getYamlDirective(),
            settings.getTagDirective()));
    anchorNode(node);
    settings.getExplicitRootTag().ifPresent(node::setTag);
    serializeNode(node);
    this.emitable.emit(new SjlExtDocumentEndEvent(settings.isExplicitEnd()));
    this.serializedNodes.clear();
    this.anchors.clear();
  }

  /**
   * Emit {@link SjlExtStreamStartEvent}
   */
  public void emitStreamStart() {
    this.emitable.emit(new SjlExtStreamStartEvent());
  }

  /**
   * Emit {@link SjlExtStreamEndEvent}
   */
  public void emitStreamEnd() {
    this.emitable.emit(new SjlExtStreamEndEvent());
  }

  private void anchorNode(SjlExtNode node) {
    final SjlExtNode realNode;
    if (node.getNodeType() == SjlExtNodeType.ANCHOR) {
      realNode = ((SjlExtAnchorNode) node).getRealNode();
    } else {
      realNode = node;
    }
    if (this.anchors.containsKey(realNode)) {
      //it looks weird, anchor does contain the key node, but we call computeIfAbsent()
      // this is because the value is null (HashMap permits values to be null)
      this.anchors.computeIfAbsent(realNode,
          a -> settings.getAnchorGenerator().nextAnchor(realNode));
    } else {
      this.anchors.put(realNode,
          realNode.getAnchor().isPresent() ? settings.getAnchorGenerator().nextAnchor(realNode)
              : null);
      switch (realNode.getNodeType()) {
        case SEQUENCE:
          SjlExtSequenceNode seqNode = (SjlExtSequenceNode) realNode;
          List<SjlExtNode> list = seqNode.getValue();
          for (SjlExtNode item : list) {
            anchorNode(item);
          }
          break;
        case MAPPING:
          SjlExtMappingNode mappingNode = (SjlExtMappingNode) realNode;
          List<SjlExtNodeTuple> map = mappingNode.getValue();
          for (SjlExtNodeTuple object : map) {
            SjlExtNode key = object.getKeyNode();
            SjlExtNode value = object.getValueNode();
            anchorNode(key);
            anchorNode(value);
          }
          break;
        default: // no further action required for non-collections
      }
    }
  }

  /**
   * Recursive serialization of a {@link SjlExtNode}
   *
   * @param node - content
   */
  private void serializeNode(SjlExtNode node) {
    if (node.getNodeType() == SjlExtNodeType.ANCHOR) {
      node = ((SjlExtAnchorNode) node).getRealNode();
    }
    Optional<SjlExtAnchor> tAlias = Optional.ofNullable(this.anchors.get(node));
    if (this.serializedNodes.contains(node)) {
      this.emitable.emit(new SjlExtAliasEvent(tAlias));
    } else {
      this.serializedNodes.add(node);
      switch (node.getNodeType()) {
        case SCALAR:
          SjlExtScalarNode scalarNode = (SjlExtScalarNode) node;
          serializeComments(node.getBlockComments());
          SjlExtTag detectedTag = settings.getScalarResolver().resolve(scalarNode.getValue(), true);
          SjlExtTag defaultTag = settings.getScalarResolver().resolve(scalarNode.getValue(), false);
          SjlExtImplicitTuple tuple = new SjlExtImplicitTuple(node.getTag().equals(detectedTag), node
              .getTag().equals(defaultTag));
          SjlExtScalarEvent event = new SjlExtScalarEvent(tAlias, Optional.of(node.getTag().getValue()), tuple,
              scalarNode.getValue(), scalarNode.getScalarStyle());
          this.emitable.emit(event);
          serializeComments(node.getInLineComments());
          serializeComments(node.getEndComments());
          break;
        case SEQUENCE:
          SjlExtSequenceNode seqNode = (SjlExtSequenceNode) node;
          serializeComments(node.getBlockComments());
          boolean implicitS = node.getTag().equals(SjlExtTag.SEQ);
          this.emitable.emit(new SjlExtSequenceStartEvent(tAlias, Optional.of(node.getTag().getValue()),
              implicitS, seqNode.getFlowStyle()));
          List<SjlExtNode> list = seqNode.getValue();
          for (SjlExtNode item : list) {
            serializeNode(item);
          }
          this.emitable.emit(new SjlExtSequenceEndEvent());
          serializeComments(node.getInLineComments());
          serializeComments(node.getEndComments());
          break;
        default:// instance of MappingNode
          serializeComments(node.getBlockComments());
          boolean implicitM = node.getTag().equals(SjlExtTag.MAP);
          SjlExtMappingNode mappingNode = (SjlExtMappingNode) node;
          List<SjlExtNodeTuple> map = mappingNode.getValue();
          if (mappingNode.getTag() != SjlExtTag.COMMENT) {
            this.emitable.emit(
                new SjlExtMappingStartEvent(tAlias, Optional.of(mappingNode.getTag().getValue()),
                    implicitM, mappingNode.getFlowStyle(), Optional.empty(), Optional.empty()));
            for (SjlExtNodeTuple entry : map) {
              SjlExtNode key = entry.getKeyNode();
              SjlExtNode value = entry.getValueNode();
              serializeNode(key);
              serializeNode(value);
            }
            this.emitable.emit(new SjlExtMappingEndEvent());
            serializeComments(node.getInLineComments());
            serializeComments(node.getEndComments());
          }
      }
    }
  }

  private void serializeComments(List<SjlExtCommentLine> comments) {
    if (comments == null) {
      return;
    }
    for (SjlExtCommentLine line : comments) {
      SjlExtCommentEvent commentEvent = new SjlExtCommentEvent(line.getCommentType(), line.getValue(),
          line.getStartMark(),
          line.getEndMark());
      this.emitable.emit(commentEvent);
    }
  }
}
