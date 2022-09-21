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
package com.vga.sjl.external.org.snakeyaml.engine.v2.composer;

import com.vga.sjl.external.org.snakeyaml.engine.v2.api.SjlExtLoadSettings;
import com.vga.sjl.external.org.snakeyaml.engine.v2.comments.SjlExtCommentEventsCollector;
import com.vga.sjl.external.org.snakeyaml.engine.v2.comments.SjlExtCommentLine;
import com.vga.sjl.external.org.snakeyaml.engine.v2.comments.SjlExtCommentType;
import com.vga.sjl.external.org.snakeyaml.engine.v2.common.SjlExtAnchor;
import com.vga.sjl.external.org.snakeyaml.engine.v2.common.SjlExtFlowStyle;
import com.vga.sjl.external.org.snakeyaml.engine.v2.events.*;
import com.vga.sjl.external.org.snakeyaml.engine.v2.exceptions.SjlExtComposerException;
import com.vga.sjl.external.org.snakeyaml.engine.v2.exceptions.SjlExtMark;
import com.vga.sjl.external.org.snakeyaml.engine.v2.exceptions.SjlExtYamlEngineException;
import com.vga.sjl.external.org.snakeyaml.engine.v2.nodes.*;
import com.vga.sjl.external.org.snakeyaml.engine.v2.parser.SjlExtParser;
import com.vga.sjl.external.org.snakeyaml.engine.v2.resolver.SjlExtScalarResolver;

import java.util.*;

/**
 * Creates a node graph from parser events.
 * <p>
 * Corresponds to the 'Composer' step as described in chapter 3.1.2 of the
 * <a href="http://www.yaml.org/spec/1.2/spec.html#id2762107">YAML Specification</a>.
 * </p>
 * It implements {@link Iterator< SjlExtNode >} to get the stream of {@link SjlExtNode}s from the input.
 */
public class SjlExtComposer implements Iterator<SjlExtNode> {

  protected final SjlExtParser parser;
  private final SjlExtScalarResolver scalarResolver;
  private final Map<SjlExtAnchor, SjlExtNode> anchors;
  private final Set<SjlExtNode> recursiveNodes;
  private int nonScalarAliasesCount = 0;
  private final SjlExtLoadSettings settings;
  private final SjlExtCommentEventsCollector blockCommentsCollector;
  private final SjlExtCommentEventsCollector inlineCommentsCollector;

  /**
   * @deprecated use the other constructor with LoadSettings first
   */
  public SjlExtComposer(SjlExtParser parser, SjlExtLoadSettings settings) {
    this(settings, parser);
  }

  public SjlExtComposer(SjlExtLoadSettings settings, SjlExtParser parser) {
    this.parser = parser;
    this.scalarResolver = settings.getScalarResolver();
    this.settings = settings;
    this.anchors = new HashMap();
    this.recursiveNodes = new HashSet();
    this.blockCommentsCollector = new SjlExtCommentEventsCollector(parser,
        SjlExtCommentType.BLANK_LINE, SjlExtCommentType.BLOCK);
    this.inlineCommentsCollector = new SjlExtCommentEventsCollector(parser,
        SjlExtCommentType.IN_LINE);
  }

  /**
   * Checks if further documents are available.
   *
   * @return <code>true</code> if there is at least one more document.
   */
  public boolean hasNext() {
    // Drop the STREAM-START event.
    if (parser.checkEvent(SjlExtEvent.ID.StreamStart)) {
      parser.next();
    }
    // If there are more documents available?
    return !parser.checkEvent(SjlExtEvent.ID.StreamEnd);
  }

  /**
   * Reads a document from a source that contains only one document.
   * <p>
   * If the stream contains more than one document an exception is thrown.
   * </p>
   *
   * @return The root node of the document or <code>Optional.empty()</code> if no document is
   * available.
   */
  public Optional<SjlExtNode> getSingleNode() {
    // Drop the STREAM-START event.
    parser.next();
    // Compose a document if the stream is not empty.
    Optional<SjlExtNode> document = Optional.empty();
    if (!parser.checkEvent(SjlExtEvent.ID.StreamEnd)) {
      document = Optional.of(next());
    }
    // Ensure that the stream contains no more documents.
    if (!parser.checkEvent(SjlExtEvent.ID.StreamEnd)) {
      SjlExtEvent event = parser.next();
      Optional<SjlExtMark> previousDocMark = document.flatMap(SjlExtNode::getStartMark);
      throw new SjlExtComposerException("expected a single document in the stream", previousDocMark,
          "but found another document", event.getStartMark());
    }
    // Drop the STREAM-END event.
    parser.next();
    return document;
  }

  /**
   * Reads and composes the next document.
   *
   * @return The root node of the document or <code>null</code> if no more documents are available.
   */
  public SjlExtNode next() {
    // Collect inter-document start comments
    blockCommentsCollector.collectEvents();
    if (parser.checkEvent(SjlExtEvent.ID.StreamEnd)) {
      List<SjlExtCommentLine> commentLines = blockCommentsCollector.consume();
      Optional<SjlExtMark> startMark = commentLines.get(0).getStartMark();
      List<SjlExtNodeTuple> children = Collections.emptyList();
      SjlExtNode node = new SjlExtMappingNode(SjlExtTag.COMMENT, false, children, SjlExtFlowStyle.BLOCK, startMark,
          Optional.empty());
      node.setBlockComments(commentLines);
      return node;
    }
    // Drop the DOCUMENT-START event.
    parser.next();
    // Compose the root node.
    SjlExtNode node = composeNode(Optional.empty());
    // Drop the DOCUMENT-END event.
    blockCommentsCollector.collectEvents();
    if (!blockCommentsCollector.isEmpty()) {
      node.setEndComments(blockCommentsCollector.consume());
    }
    parser.next();
    this.anchors.clear();
    this.recursiveNodes.clear();
    this.nonScalarAliasesCount = 0;
    return node;
  }


  private SjlExtNode composeNode(Optional<SjlExtNode> parent) {
    blockCommentsCollector.collectEvents();
    parent.ifPresent(recursiveNodes::add);//TODO add unit test for this line
    final SjlExtNode node;
    if (parser.checkEvent(SjlExtEvent.ID.Alias)) {
      SjlExtAliasEvent event = (SjlExtAliasEvent) parser.next();
      SjlExtAnchor anchor = event.getAlias();
      if (!anchors.containsKey(anchor)) {
        throw new SjlExtComposerException("found undefined alias " + anchor, event.getStartMark());
      }
      node = anchors.get(anchor);
      if (node.getNodeType() != SjlExtNodeType.SCALAR) {
        this.nonScalarAliasesCount++;
        if (this.nonScalarAliasesCount > settings.getMaxAliasesForCollections()) {
          throw new SjlExtYamlEngineException(
              "Number of aliases for non-scalar nodes exceeds the specified max="
                  + settings.getMaxAliasesForCollections());
        }
      }
      if (recursiveNodes.remove(node)) {
        node.setRecursive(true);
      }
      // drop comments, they can not be supported here
      blockCommentsCollector.consume();
      inlineCommentsCollector.collectEvents().consume();
    } else {
      SjlExtNodeEvent event = (SjlExtNodeEvent) parser.peekEvent();
      Optional<SjlExtAnchor> anchor = event.getAnchor();
      // the check for duplicate anchors has been removed (issue 174)
      if (parser.checkEvent(SjlExtEvent.ID.Scalar)) {
        node = composeScalarNode(anchor, blockCommentsCollector.consume());
      } else if (parser.checkEvent(SjlExtEvent.ID.SequenceStart)) {
        node = composeSequenceNode(anchor);
      } else {
        node = composeMappingNode(anchor);
      }
    }
    parent.ifPresent(recursiveNodes::remove);//TODO add unit test for this line
    return node;
  }

  private void registerAnchor(SjlExtAnchor anchor, SjlExtNode node) {
    anchors.put(anchor, node);
    node.setAnchor(Optional.of(anchor));
  }

  protected SjlExtNode composeScalarNode(Optional<SjlExtAnchor> anchor, List<SjlExtCommentLine> blockComments) {
    SjlExtScalarEvent ev = (SjlExtScalarEvent) parser.next();
    Optional<String> tag = ev.getTag();
    boolean resolved = false;
    SjlExtTag nodeTag;
    if (!tag.isPresent() || tag.get().equals("!")) {
      nodeTag = scalarResolver.resolve(ev.getValue(), ev.getImplicit().canOmitTagInPlainScalar());
      resolved = true;
    } else {
      nodeTag = new SjlExtTag(tag.get());
    }
    SjlExtNode node = new SjlExtScalarNode(nodeTag, resolved, ev.getValue(), ev.getScalarStyle(),
        ev.getStartMark(), ev.getEndMark());
    anchor.ifPresent(a -> registerAnchor(a, node));
    node.setBlockComments(blockComments);
    node.setInLineComments(inlineCommentsCollector.collectEvents().consume());
    return node;
  }

  protected SjlExtNode composeSequenceNode(Optional<SjlExtAnchor> anchor) {
    SjlExtSequenceStartEvent startEvent = (SjlExtSequenceStartEvent) parser.next();
    Optional<String> tag = startEvent.getTag();
    SjlExtTag nodeTag;
    boolean resolved = false;
    if (!tag.isPresent() || tag.get().equals("!")) {
      nodeTag = SjlExtTag.SEQ;
      resolved = true;
    } else {
      nodeTag = new SjlExtTag(tag.get());
    }
    final ArrayList<SjlExtNode> children = new ArrayList();
    SjlExtSequenceNode node = new SjlExtSequenceNode(nodeTag, resolved, children, startEvent.getFlowStyle(),
        startEvent.getStartMark(),
        Optional.empty());
    if (startEvent.isFlow()) {
      node.setBlockComments(blockCommentsCollector.consume());
    }
    anchor.ifPresent(a -> registerAnchor(a, node));
    while (!parser.checkEvent(SjlExtEvent.ID.SequenceEnd)) {
      blockCommentsCollector.collectEvents();
      if (parser.checkEvent(SjlExtEvent.ID.SequenceEnd)) {
        break;
      }
      children.add(composeNode(Optional.of(node)));
    }
    if (startEvent.isFlow()) {
      node.setInLineComments(inlineCommentsCollector.collectEvents().consume());
    }
    SjlExtEvent endEvent = parser.next();
    node.setEndMark(endEvent.getEndMark());
    inlineCommentsCollector.collectEvents();
    if (!inlineCommentsCollector.isEmpty()) {
      node.setInLineComments(inlineCommentsCollector.consume());
    }
    return node;
  }

  protected SjlExtNode composeMappingNode(Optional<SjlExtAnchor> anchor) {
    SjlExtMappingStartEvent startEvent = (SjlExtMappingStartEvent) parser.next();
    Optional<String> tag = startEvent.getTag();
    SjlExtTag nodeTag;
    boolean resolved = false;
    if (!tag.isPresent() || tag.get().equals("!")) {
      nodeTag = SjlExtTag.MAP;
      resolved = true;
    } else {
      nodeTag = new SjlExtTag(tag.get());
    }

    final List<SjlExtNodeTuple> children = new ArrayList<>();
    SjlExtMappingNode node = new SjlExtMappingNode(nodeTag, resolved, children, startEvent.getFlowStyle(),
        startEvent.getStartMark(), Optional.empty());
    if (startEvent.isFlow()) {
      node.setBlockComments(blockCommentsCollector.consume());
    }
    anchor.ifPresent(a -> registerAnchor(a, node));
    while (!parser.checkEvent(SjlExtEvent.ID.MappingEnd)) {
      blockCommentsCollector.collectEvents();
      if (parser.checkEvent(SjlExtEvent.ID.MappingEnd)) {
        break;
      }
      composeMappingChildren(children, node);
    }
    if (startEvent.isFlow()) {
      node.setInLineComments(inlineCommentsCollector.collectEvents().consume());
    }
    SjlExtEvent endEvent = parser.next();
    node.setEndMark(endEvent.getEndMark());
    inlineCommentsCollector.collectEvents();
    if (!inlineCommentsCollector.isEmpty()) {
      node.setInLineComments(inlineCommentsCollector.consume());
    }
    return node;
  }

  protected void composeMappingChildren(List<SjlExtNodeTuple> children, SjlExtMappingNode node) {
    SjlExtNode itemKey = composeKeyNode(node);
    SjlExtNode itemValue = composeValueNode(node);
    children.add(new SjlExtNodeTuple(itemKey, itemValue));
  }

  protected SjlExtNode composeKeyNode(SjlExtMappingNode node) {
    return composeNode(Optional.of(node));
  }

  protected SjlExtNode composeValueNode(SjlExtMappingNode node) {
    return composeNode(Optional.of(node));
  }
}
