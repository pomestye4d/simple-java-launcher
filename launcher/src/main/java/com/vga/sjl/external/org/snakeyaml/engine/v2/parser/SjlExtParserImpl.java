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
package com.vga.sjl.external.org.snakeyaml.engine.v2.parser;

import com.vga.sjl.external.org.snakeyaml.engine.v2.api.SjlExtLoadSettings;
import com.vga.sjl.external.org.snakeyaml.engine.v2.comments.SjlExtCommentType;
import com.vga.sjl.external.org.snakeyaml.engine.v2.common.*;
import com.vga.sjl.external.org.snakeyaml.engine.v2.events.*;
import com.vga.sjl.external.org.snakeyaml.engine.v2.exceptions.SjlExtMark;
import com.vga.sjl.external.org.snakeyaml.engine.v2.exceptions.SjlExtParserException;
import com.vga.sjl.external.org.snakeyaml.engine.v2.exceptions.SjlExtYamlEngineException;
import com.vga.sjl.external.org.snakeyaml.engine.v2.nodes.SjlExtTag;
import com.vga.sjl.external.org.snakeyaml.engine.v2.scanner.SjlExtScanner;
import com.vga.sjl.external.org.snakeyaml.engine.v2.scanner.SjlExtScannerImpl;
import com.vga.sjl.external.org.snakeyaml.engine.v2.scanner.SjlExtStreamReader;
import com.vga.sjl.external.org.snakeyaml.engine.v2.tokens.*;

import java.util.*;

/**
 * <pre>
 * # The following YAML grammar is LL(1) and is parsed by a recursive descent parser.
 *
 * stream            ::= STREAM-START implicit_document? explicit_document* STREAM-END
 * implicit_document ::= block_node DOCUMENT-END*
 * explicit_document ::= DIRECTIVE* DOCUMENT-START block_node? DOCUMENT-END*
 * block_node_or_indentless_sequence ::=
 *                       ALIAS
 *                       | properties (block_content | indentless_block_sequence)?
 *                       | block_content
 *                       | indentless_block_sequence
 * block_node        ::= ALIAS
 *                       | properties block_content?
 *                       | block_content
 * flow_node         ::= ALIAS
 *                       | properties flow_content?
 *                       | flow_content
 * properties        ::= TAG ANCHOR? | ANCHOR TAG?
 * block_content     ::= block_collection | flow_collection | SCALAR
 * flow_content      ::= flow_collection | SCALAR
 * block_collection  ::= block_sequence | block_mapping
 * flow_collection   ::= flow_sequence | flow_mapping
 * block_sequence    ::= BLOCK-SEQUENCE-START (BLOCK-ENTRY block_node?)* BLOCK-END
 * indentless_sequence   ::= (BLOCK-ENTRY block_node?)+
 * block_mapping     ::= BLOCK-MAPPING_START
 *                       ((KEY block_node_or_indentless_sequence?)?
 *                       (VALUE block_node_or_indentless_sequence?)?)*
 *                       BLOCK-END
 * flow_sequence     ::= FLOW-SEQUENCE-START
 *                       (flow_sequence_entry FLOW-ENTRY)*
 *                       flow_sequence_entry?
 *                       FLOW-SEQUENCE-END
 * flow_sequence_entry   ::= flow_node | KEY flow_node? (VALUE flow_node?)?
 * flow_mapping      ::= FLOW-MAPPING-START
 *                       (flow_mapping_entry FLOW-ENTRY)*
 *                       flow_mapping_entry?
 *                       FLOW-MAPPING-END
 * flow_mapping_entry    ::= flow_node | KEY flow_node? (VALUE flow_node?)?
 * #
 * FIRST sets:
 * #
 * stream: { STREAM-START }
 * explicit_document: { DIRECTIVE DOCUMENT-START }
 * implicit_document: FIRST(block_node)
 * block_node: { ALIAS TAG ANCHOR SCALAR BLOCK-SEQUENCE-START BLOCK-MAPPING-START FLOW-SEQUENCE-START FLOW-MAPPING-START }
 * flow_node: { ALIAS ANCHOR TAG SCALAR FLOW-SEQUENCE-START FLOW-MAPPING-START }
 * block_content: { BLOCK-SEQUENCE-START BLOCK-MAPPING-START FLOW-SEQUENCE-START FLOW-MAPPING-START SCALAR }
 * flow_content: { FLOW-SEQUENCE-START FLOW-MAPPING-START SCALAR }
 * block_collection: { BLOCK-SEQUENCE-START BLOCK-MAPPING-START }
 * flow_collection: { FLOW-SEQUENCE-START FLOW-MAPPING-START }
 * block_sequence: { BLOCK-SEQUENCE-START }
 * block_mapping: { BLOCK-MAPPING-START }
 * block_node_or_indentless_sequence: { ALIAS ANCHOR TAG SCALAR BLOCK-SEQUENCE-START BLOCK-MAPPING-START FLOW-SEQUENCE-START FLOW-MAPPING-START BLOCK-ENTRY }
 * indentless_sequence: { ENTRY }
 * flow_collection: { FLOW-SEQUENCE-START FLOW-MAPPING-START }
 * flow_sequence: { FLOW-SEQUENCE-START }
 * flow_mapping: { FLOW-MAPPING-START }
 * flow_sequence_entry: { ALIAS ANCHOR TAG SCALAR FLOW-SEQUENCE-START FLOW-MAPPING-START KEY }
 * flow_mapping_entry: { ALIAS ANCHOR TAG SCALAR FLOW-SEQUENCE-START FLOW-MAPPING-START KEY }
 * </pre>
 * <p>
 * Since writing a recursive-descendant parser is a straightforward task, we do not give many
 * comments here.
 */
public class SjlExtParserImpl implements SjlExtParser {

  private static final Map<String, String> DEFAULT_TAGS = new HashMap();

  static {
    DEFAULT_TAGS.put("!", "!");
    DEFAULT_TAGS.put("!!", SjlExtTag.PREFIX);
  }

  protected final SjlExtScanner scanner;
  private final SjlExtLoadSettings settings;
  private Optional<SjlExtEvent> currentEvent; // parsed event
  private final SjlExtArrayStack<SjlExtProduction> states;
  private final SjlExtArrayStack<Optional<SjlExtMark>> marksStack;
  private Optional<SjlExtProduction> state;
  private Map<String, String> directiveTags;

  /**
   * @deprecated use the other constructor with LoadSettings first
   */
  public SjlExtParserImpl(SjlExtStreamReader reader, SjlExtLoadSettings settings) {
    this(settings, reader);
  }

  public SjlExtParserImpl(SjlExtLoadSettings settings, SjlExtStreamReader reader) {
    this(settings, new SjlExtScannerImpl(settings, reader));
  }

  /**
   * @deprecated use the other constructor with LoadSettings first
   */
  public SjlExtParserImpl(SjlExtScanner scanner, SjlExtLoadSettings settings) {
    this(settings, scanner);
  }

  public SjlExtParserImpl(SjlExtLoadSettings settings, SjlExtScanner scanner) {
    this.scanner = scanner;
    this.settings = settings;
    currentEvent = Optional.empty();
    directiveTags = new HashMap<>(DEFAULT_TAGS);
    states = new SjlExtArrayStack(100);
    marksStack = new SjlExtArrayStack(10);
    state = Optional.of(new ParseStreamStart()); // prepare the next state
  }

  /**
   * Check the ID of the next event.
   */
  public boolean checkEvent(SjlExtEvent.ID id) {
    peekEvent();
    return currentEvent.isPresent() && currentEvent.get().getEventId() == id;
  }

  /**
   * Get the next event (and keep it). Produce the event if not yet present.
   */
  public SjlExtEvent peekEvent() {
    produce();
    return currentEvent.orElseThrow(() -> new NoSuchElementException("No more Events found."));
  }

  /**
   * Consume the event (get the next event and removed it).
   */
  public SjlExtEvent next() {
    SjlExtEvent value = peekEvent();
    currentEvent = Optional.empty();
    return value;
  }

  /**
   * Produce the event if not yet present.
   *
   * @return true if there is another event
   */
  @Override
  public boolean hasNext() {
    produce();
    return currentEvent.isPresent();
  }

  private void produce() {
    if (!currentEvent.isPresent()) {
      state.ifPresent(production -> currentEvent = Optional.of(production.produce()));
    }
  }

  private SjlExtCommentEvent produceCommentEvent(SjlExtCommentToken token) {
    String value = token.getValue();
    SjlExtCommentType type = token.getCommentType();

    // state = state, that no change in state

    return new SjlExtCommentEvent(type, value, token.getStartMark(), token.getEndMark());
  }

  private class ParseStreamStart implements SjlExtProduction {

    public SjlExtEvent produce() {
      // Parse the stream start.
      SjlExtStreamStartToken token = (SjlExtStreamStartToken) scanner.next();
      SjlExtEvent event = new SjlExtStreamStartEvent(token.getStartMark(), token.getEndMark());
      // Prepare the next state.
      state = Optional.of(new ParseImplicitDocumentStart());
      return event;
    }
  }

  private class ParseImplicitDocumentStart implements SjlExtProduction {

    public SjlExtEvent produce() {
      if (scanner.checkToken(SjlExtToken.ID.Comment)) {
        state = Optional.of(new ParseImplicitDocumentStart());
        return produceCommentEvent((SjlExtCommentToken) scanner.next());
      }
      if (!scanner.checkToken(SjlExtToken.ID.Directive, SjlExtToken.ID.DocumentStart, SjlExtToken.ID.StreamEnd)) {
        // Parse an implicit document.
        SjlExtToken token = scanner.peekToken();
        Optional<SjlExtMark> startMark = token.getStartMark();
        Optional<SjlExtMark> endMark = startMark;
        SjlExtEvent event = new SjlExtDocumentStartEvent(false, Optional.empty(), Collections.emptyMap(),
            startMark, endMark);
        // Prepare the next state.
        states.push(new ParseDocumentEnd());
        state = Optional.of(new ParseBlockNode());
        return event;
      } else {
        // explicit document detected
        return new ParseDocumentStart().produce();
      }
    }
  }

  private class ParseDocumentStart implements SjlExtProduction {

    public SjlExtEvent produce() {
      if (scanner.checkToken(SjlExtToken.ID.Comment)) {
        state = Optional.of(new ParseDocumentStart());
        return produceCommentEvent((SjlExtCommentToken) scanner.next());
      }
      // Parse any extra document end indicators.
      while (scanner.checkToken(SjlExtToken.ID.DocumentEnd)) {
        scanner.next();
      }
      if (scanner.checkToken(SjlExtToken.ID.Comment)) {
        state = Optional.of(new ParseDocumentStart());
        return produceCommentEvent((SjlExtCommentToken) scanner.next());
      }
      // Parse an explicit document.
      SjlExtEvent event;
      if (!scanner.checkToken(SjlExtToken.ID.StreamEnd)) {
        SjlExtToken token = scanner.peekToken();
        Optional<SjlExtMark> startMark = token.getStartMark();
        SjlExtVersionTagsTuple tuple = processDirectives();
        while (scanner.checkToken(SjlExtToken.ID.Comment)) {
          scanner.next();
        }
        if (!scanner.checkToken(SjlExtToken.ID.StreamEnd)) {
          if (!scanner.checkToken(SjlExtToken.ID.DocumentStart)) {
            throw new SjlExtParserException("expected '<document start>', but found '"
                + scanner.peekToken().getTokenId() + "'", scanner.peekToken().getStartMark());
          }
          token = scanner.next();
          Optional<SjlExtMark> endMark = token.getEndMark();
          event = new SjlExtDocumentStartEvent(true, tuple.getSpecVersion(), tuple.getTags(), startMark,
              endMark);
          states.push(new ParseDocumentEnd());
          state = Optional.of(new ParseDocumentContent());
          return event;
        } else {
          throw new SjlExtParserException("expected '<document start>', but found '"
              + scanner.peekToken().getTokenId() + "'", scanner.peekToken().getStartMark());
        }
      }
      // Parse the end of the stream.
      SjlExtStreamEndToken token = (SjlExtStreamEndToken) scanner.next();
      event = new SjlExtStreamEndEvent(token.getStartMark(), token.getEndMark());
      if (!states.isEmpty()) {
        throw new SjlExtYamlEngineException("Unexpected end of stream. States left: " + states);
      }
      if (!markEmpty()) {
        throw new SjlExtYamlEngineException("Unexpected end of stream. Marks left: " + marksStack);
      }
      state = Optional.empty();
      return event;
    }

    private boolean markEmpty() {
      return marksStack.isEmpty();
    }
  }

  private class ParseDocumentEnd implements SjlExtProduction {

    public SjlExtEvent produce() {
      // Parse the document end.
      SjlExtToken token = scanner.peekToken();
      Optional<SjlExtMark> startMark = token.getStartMark();
      Optional<SjlExtMark> endMark = startMark;
      boolean explicit = false;
      if (scanner.checkToken(SjlExtToken.ID.DocumentEnd)) {
        token = scanner.next();
        endMark = token.getEndMark();
        explicit = true;
      } else if (scanner.checkToken(SjlExtToken.ID.Directive) )  {
        throw new SjlExtParserException("expected '<document end>' before directives, but found '"
            + scanner.peekToken().getTokenId() + "'", scanner.peekToken().getStartMark());
      }
      directiveTags.clear(); // directive tags do not survive between the documents
      SjlExtEvent event = new SjlExtDocumentEndEvent(explicit, startMark, endMark);
      // Prepare the next state.
      state = Optional.of(new ParseDocumentStart());
      return event;
    }
  }

  private class ParseDocumentContent implements SjlExtProduction {

    public SjlExtEvent produce() {
      if (scanner.checkToken(SjlExtToken.ID.Comment)) {
        state = Optional.of(new ParseDocumentContent());
        return produceCommentEvent((SjlExtCommentToken) scanner.next());
      }
      if (scanner.checkToken(SjlExtToken.ID.Directive, SjlExtToken.ID.DocumentStart,
          SjlExtToken.ID.DocumentEnd, SjlExtToken.ID.StreamEnd)) {
        SjlExtEvent event = processEmptyScalar(scanner.peekToken().getStartMark());
        state = Optional.of(states.pop());
        return event;
      } else {
        return new ParseBlockNode().produce();
      }
    }
  }

  @SuppressWarnings("unchecked")
  private SjlExtVersionTagsTuple processDirectives() {
    Optional<SjlExtSpecVersion> yamlSpecVersion = Optional.empty();
    HashMap<String, String> tagHandles = new HashMap<>();
    while (scanner.checkToken(SjlExtToken.ID.Directive)) {
      @SuppressWarnings("rawtypes")
      SjlExtDirectiveToken token = (SjlExtDirectiveToken) scanner.next();
      Optional<List<?>> dirOption = token.getValue();
      if (dirOption.isPresent()) {
        //the value must be present
        List<?> directiveValue = dirOption.get();
        if (token.getName().equals(SjlExtDirectiveToken.YAML_DIRECTIVE)) {
          if (yamlSpecVersion.isPresent()) {
            throw new SjlExtParserException("found duplicate YAML directive", token.getStartMark());
          }
          List<Integer> value = (List<Integer>) directiveValue;
          Integer major = value.get(0);
          Integer minor = value.get(1);
          yamlSpecVersion = Optional.of(
              settings.getVersionFunction().apply(new SjlExtSpecVersion(major, minor)));
        } else if (token.getName().equals(SjlExtDirectiveToken.TAG_DIRECTIVE)) {
          List<String> value = (List<String>) directiveValue;
          String handle = value.get(0);
          String prefix = value.get(1);
          if (tagHandles.containsKey(handle)) {
            throw new SjlExtParserException("duplicate tag handle " + handle,
                token.getStartMark());
          }
          tagHandles.put(handle, prefix);
        }
      }
    }
    HashMap<String, String> detectedTagHandles = new HashMap<String, String>();
    if (!tagHandles.isEmpty()) {
      // copy from tagHandles
      detectedTagHandles.putAll(tagHandles);
    }
    for (Map.Entry<String, String> entry : DEFAULT_TAGS.entrySet()) {
      // do not overwrite re-defined tags
      if (!tagHandles.containsKey(entry.getKey())) {
        tagHandles.put(entry.getKey(), entry.getValue());
      }
    }
    directiveTags = tagHandles;
    // data for the event (no default tags added)
    return new SjlExtVersionTagsTuple(yamlSpecVersion, detectedTagHandles);
  }

  /**
   * <pre>
   *  block_node_or_indentless_sequence ::= ALIAS
   *                | properties (block_content | indentless_block_sequence)?
   *                | block_content
   *                | indentless_block_sequence
   *  block_node    ::= ALIAS
   *                    | properties block_content?
   *                    | block_content
   *  flow_node     ::= ALIAS
   *                    | properties flow_content?
   *                    | flow_content
   *  properties    ::= TAG ANCHOR? | ANCHOR TAG?
   *  block_content     ::= block_collection | flow_collection | SCALAR
   *  flow_content      ::= flow_collection | SCALAR
   *  block_collection  ::= block_sequence | block_mapping
   *  flow_collection   ::= flow_sequence | flow_mapping
   * </pre>
   */

  private class ParseBlockNode implements SjlExtProduction {

    public SjlExtEvent produce() {
      return parseNode(true, false);
    }
  }

  private SjlExtEvent parseFlowNode() {
    return parseNode(false, false);
  }

  private SjlExtEvent parseBlockNodeOrIndentlessSequence() {
    return parseNode(true, true);
  }

  private SjlExtEvent parseNode(boolean block, boolean indentlessSequence) {
    SjlExtEvent event;
    Optional<SjlExtMark> startMark = Optional.empty();
    Optional<SjlExtMark> endMark = Optional.empty();
    Optional<SjlExtMark> tagMark = Optional.empty();
    if (scanner.checkToken(SjlExtToken.ID.Alias)) {
      SjlExtAliasToken token = (SjlExtAliasToken) scanner.next();
      event = new SjlExtAliasEvent(Optional.of(token.getValue()), token.getStartMark(),
          token.getEndMark());
      state = Optional.of(states.pop());
    } else {
      Optional<SjlExtAnchor> anchor = Optional.empty();
      SjlExtTagTuple tagTupleValue = null;
      if (scanner.checkToken(SjlExtToken.ID.Anchor)) {
        SjlExtAnchorToken token = (SjlExtAnchorToken) scanner.next();
        startMark = token.getStartMark();
        endMark = token.getEndMark();
        anchor = Optional.of(token.getValue());
        if (scanner.checkToken(SjlExtToken.ID.Tag)) {
          SjlExtTagToken tagToken = (SjlExtTagToken) scanner.next();
          tagMark = tagToken.getStartMark();
          endMark = tagToken.getEndMark();
          tagTupleValue = tagToken.getValue();
        }
      } else if (scanner.checkToken(SjlExtToken.ID.Tag)) {
        SjlExtTagToken tagToken = (SjlExtTagToken) scanner.next();
        startMark = tagToken.getStartMark();
        tagMark = startMark;
        endMark = tagToken.getEndMark();
        tagTupleValue = tagToken.getValue();
        if (scanner.checkToken(SjlExtToken.ID.Anchor)) {
          SjlExtAnchorToken token = (SjlExtAnchorToken) scanner.next();
          endMark = token.getEndMark();
          anchor = Optional.of(token.getValue());
        }
      }
      Optional<String> tag = Optional.empty();
      if (tagTupleValue != null) {
        Optional<String> handleOpt = tagTupleValue.getHandle();
        String suffix = tagTupleValue.getSuffix();
        if (handleOpt.isPresent()) {
          String handle = handleOpt.get();
          if (!directiveTags.containsKey(handle)) {
            throw new SjlExtParserException("while parsing a node", startMark,
                "found undefined tag handle " + handle, tagMark);
          }
          tag = Optional.of(directiveTags.get(handle) + suffix);
        } else {
          tag = Optional.of(suffix);
        }
      }
      if (!startMark.isPresent()) {
        startMark = scanner.peekToken().getStartMark();
        endMark = startMark;
      }
      boolean implicit = (!tag.isPresent() /* TODO issue 459 || tag.equals("!") */);
      if (indentlessSequence && scanner.checkToken(SjlExtToken.ID.BlockEntry)) {
        endMark = scanner.peekToken().getEndMark();
        event = new SjlExtSequenceStartEvent(anchor, tag, implicit, SjlExtFlowStyle.BLOCK, startMark, endMark);
        state = Optional.of(new ParseIndentlessSequenceEntryKey());
      } else {
        if (scanner.checkToken(SjlExtToken.ID.Scalar)) {
          SjlExtScalarToken token = (SjlExtScalarToken) scanner.next();
          endMark = token.getEndMark();
          SjlExtImplicitTuple implicitValues;
          if ((token.isPlain() && !tag.isPresent()) /* TODO issue 459 || "!".equals(tag)*/) {
            implicitValues = new SjlExtImplicitTuple(true, false);
          } else if (!tag.isPresent()) {
            implicitValues = new SjlExtImplicitTuple(false, true);
          } else {
            implicitValues = new SjlExtImplicitTuple(false, false);
          }
          event = new SjlExtScalarEvent(anchor, tag, implicitValues, token.getValue(), token.getStyle(),
              startMark, endMark);
          state = Optional.of(states.pop());
        } else if (scanner.checkToken(SjlExtToken.ID.FlowSequenceStart)) {
          endMark = scanner.peekToken().getEndMark();
          event = new SjlExtSequenceStartEvent(anchor, tag, implicit, SjlExtFlowStyle.FLOW, startMark, endMark);
          state = Optional.of(new ParseFlowSequenceFirstEntry());
        } else if (scanner.checkToken(SjlExtToken.ID.FlowMappingStart)) {
          endMark = scanner.peekToken().getEndMark();
          event = new SjlExtMappingStartEvent(anchor, tag, implicit,
              SjlExtFlowStyle.FLOW, startMark, endMark);
          state = Optional.of(new ParseFlowMappingFirstKey());
        } else if (block && scanner.checkToken(SjlExtToken.ID.BlockSequenceStart)) {
          endMark = scanner.peekToken().getStartMark();
          event = new SjlExtSequenceStartEvent(anchor, tag, implicit, SjlExtFlowStyle.BLOCK, startMark,
              endMark);
          state = Optional.of(new ParseBlockSequenceFirstEntry());
        } else if (block && scanner.checkToken(SjlExtToken.ID.BlockMappingStart)) {
          endMark = scanner.peekToken().getStartMark();
          event = new SjlExtMappingStartEvent(anchor, tag, implicit,
              SjlExtFlowStyle.BLOCK, startMark, endMark);
          state = Optional.of(new ParseBlockMappingFirstKey());
        } else if (anchor.isPresent() || tag.isPresent()) {
          // Empty scalars are allowed even if a tag or an anchor is specified.
          event = new SjlExtScalarEvent(anchor, tag, new SjlExtImplicitTuple(implicit, false), "",
              SjlExtScalarStyle.PLAIN,
              startMark, endMark);
          state = Optional.of(states.pop());
        } else {
          SjlExtToken token = scanner.peekToken();
          throw new SjlExtParserException("while parsing a " + (block ? "block" : "flow") + " node",
              startMark,
              "expected the node content, but found '" + token.getTokenId() + "'",
              token.getStartMark());
        }
      }
    }
    return event;
  }

  // block_sequence ::= BLOCK-SEQUENCE-START (BLOCK-ENTRY block_node?)*
  // BLOCK-END

  private class ParseBlockSequenceFirstEntry implements SjlExtProduction {

    public SjlExtEvent produce() {
      SjlExtToken token = scanner.next();
      markPush(token.getStartMark());
      return new ParseBlockSequenceEntryKey().produce();
    }
  }

  private class ParseBlockSequenceEntryKey implements SjlExtProduction {

    public SjlExtEvent produce() {
      if (scanner.checkToken(SjlExtToken.ID.Comment)) {
        state = Optional.of(new ParseBlockSequenceEntryKey());
        return produceCommentEvent((SjlExtCommentToken) scanner.next());
      }
      if (scanner.checkToken(SjlExtToken.ID.BlockEntry)) {
        SjlExtBlockEntryToken token = (SjlExtBlockEntryToken) scanner.next();
        return new ParseBlockSequenceEntryValue(token).produce();
      }
      if (!scanner.checkToken(SjlExtToken.ID.BlockEnd)) {
        SjlExtToken token = scanner.peekToken();
        throw new SjlExtParserException("while parsing a block collection", markPop(),
            "expected <block end>, but found '" + token.getTokenId() + "'",
            token.getStartMark());
      }
      SjlExtToken token = scanner.next();
      SjlExtEvent event = new SjlExtSequenceEndEvent(token.getStartMark(), token.getEndMark());
      state = Optional.of(states.pop());
      markPop();
      return event;
    }
  }

  private class ParseBlockSequenceEntryValue implements SjlExtProduction {

    SjlExtBlockEntryToken token;

    public ParseBlockSequenceEntryValue(final SjlExtBlockEntryToken token) {
      this.token = token;
    }

    public SjlExtEvent produce() {
      if (scanner.checkToken(SjlExtToken.ID.Comment)) {
        state = Optional.of(new ParseBlockSequenceEntryValue(token));
        return produceCommentEvent((SjlExtCommentToken) scanner.next());
      }
      if (!scanner.checkToken(SjlExtToken.ID.BlockEntry, SjlExtToken.ID.BlockEnd)) {
        states.push(new ParseBlockSequenceEntryKey());
        return new ParseBlockNode().produce();
      } else {
        state = Optional.of(new ParseBlockSequenceEntryKey());
        return processEmptyScalar(token.getEndMark());
      }
    }
  }

  // indentless_sequence ::= (BLOCK-ENTRY block_node?)+

  private class ParseIndentlessSequenceEntryKey implements SjlExtProduction {

    public SjlExtEvent produce() {
      if (scanner.checkToken(SjlExtToken.ID.Comment)) {
        state = Optional.of(new ParseIndentlessSequenceEntryKey());
        return produceCommentEvent((SjlExtCommentToken) scanner.next());
      }
      if (scanner.checkToken(SjlExtToken.ID.BlockEntry)) {
        SjlExtBlockEntryToken token = (SjlExtBlockEntryToken) scanner.next();
        return new ParseIndentlessSequenceEntryValue(token).produce();
      }
      SjlExtToken token = scanner.peekToken();
      SjlExtEvent event = new SjlExtSequenceEndEvent(token.getStartMark(), token.getEndMark());
      state = Optional.of(states.pop());
      return event;
    }
  }

  private class ParseIndentlessSequenceEntryValue implements SjlExtProduction {

    SjlExtBlockEntryToken token;

    public ParseIndentlessSequenceEntryValue(final SjlExtBlockEntryToken token) {
      this.token = token;
    }

    public SjlExtEvent produce() {
      if (scanner.checkToken(SjlExtToken.ID.Comment)) {
        state = Optional.of(new ParseIndentlessSequenceEntryValue(token));
        return produceCommentEvent((SjlExtCommentToken) scanner.next());
      }
      if (!scanner.checkToken(SjlExtToken.ID.BlockEntry, SjlExtToken.ID.Key, SjlExtToken.ID.Value,
          SjlExtToken.ID.BlockEnd)) {
        states.push(new ParseIndentlessSequenceEntryKey());
        return new ParseBlockNode().produce();
      } else {
        state = Optional.of(new ParseIndentlessSequenceEntryKey());
        return processEmptyScalar(token.getEndMark());
      }
    }
  }

  private class ParseBlockMappingFirstKey implements SjlExtProduction {

    public SjlExtEvent produce() {
      SjlExtToken token = scanner.next();
      markPush(token.getStartMark());
      return new ParseBlockMappingKey().produce();
    }
  }

  private class ParseBlockMappingKey implements SjlExtProduction {

    public SjlExtEvent produce() {
      if (scanner.checkToken(SjlExtToken.ID.Comment)) {
        state = Optional.of(new ParseBlockMappingKey());
        return produceCommentEvent((SjlExtCommentToken) scanner.next());
      }
      if (scanner.checkToken(SjlExtToken.ID.Key)) {
        SjlExtToken token = scanner.next();
        if (!scanner.checkToken(SjlExtToken.ID.Key, SjlExtToken.ID.Value, SjlExtToken.ID.BlockEnd)) {
          states.push(new ParseBlockMappingValue());
          return parseBlockNodeOrIndentlessSequence();
        } else {
          state = Optional.of(new ParseBlockMappingValue());
          return processEmptyScalar(token.getEndMark());
        }
      }
      if (!scanner.checkToken(SjlExtToken.ID.BlockEnd)) {
        SjlExtToken token = scanner.peekToken();
        throw new SjlExtParserException("while parsing a block mapping", markPop(),
            "expected <block end>, but found '" + token.getTokenId() + "'",
            token.getStartMark());
      }
      SjlExtToken token = scanner.next();
      SjlExtEvent event = new SjlExtMappingEndEvent(token.getStartMark(), token.getEndMark());
      state = Optional.of(states.pop());
      markPop();
      return event;
    }
  }

  private class ParseBlockMappingValue implements SjlExtProduction {

    public SjlExtEvent produce() {
      if (scanner.checkToken(SjlExtToken.ID.Value)) {
        SjlExtToken token = scanner.next();
        if (scanner.checkToken(SjlExtToken.ID.Comment)) {
          SjlExtProduction p = new ParseBlockMappingValueComment();
          state = Optional.of(p);
          return p.produce();
        } else if (!scanner.checkToken(SjlExtToken.ID.Key, SjlExtToken.ID.Value, SjlExtToken.ID.BlockEnd)) {
          states.push(new ParseBlockMappingKey());
          return parseBlockNodeOrIndentlessSequence();
        } else {
          state = Optional.of(new ParseBlockMappingKey());
          return processEmptyScalar(token.getEndMark());
        }
      } else if (scanner.checkToken(SjlExtToken.ID.Scalar)) {
        states.push(new ParseBlockMappingKey());
        return parseBlockNodeOrIndentlessSequence();
      }
      state = Optional.of(new ParseBlockMappingKey());
      SjlExtToken token = scanner.peekToken();
      return processEmptyScalar(token.getStartMark());
    }
  }

  private class ParseBlockMappingValueComment implements SjlExtProduction {

    List<SjlExtCommentToken> tokens = new LinkedList<>();

    public SjlExtEvent produce() {
      if (scanner.checkToken(SjlExtToken.ID.Comment)) {
        tokens.add((SjlExtCommentToken) scanner.next());
        return produce();
      } else if (!scanner.checkToken(SjlExtToken.ID.Key, SjlExtToken.ID.Value, SjlExtToken.ID.BlockEnd)) {
        if (!tokens.isEmpty()) {
          return produceCommentEvent(tokens.remove(0));
        }
        states.push(new ParseBlockMappingKey());
        return parseBlockNodeOrIndentlessSequence();
      } else {
        state = Optional.of(new ParseBlockMappingValueCommentList(tokens));
        return processEmptyScalar(scanner.peekToken().getStartMark());
      }
    }
  }

  private class ParseBlockMappingValueCommentList implements SjlExtProduction {

    List<SjlExtCommentToken> tokens;

    public ParseBlockMappingValueCommentList(final List<SjlExtCommentToken> tokens) {
      this.tokens = tokens;
    }

    public SjlExtEvent produce() {
      if (!tokens.isEmpty()) {
        return produceCommentEvent(tokens.remove(0));
      }
      return new ParseBlockMappingKey().produce();
    }
  }

  /**
   * <pre>
   * flow_sequence     ::= FLOW-SEQUENCE-START
   *                       (flow_sequence_entry FLOW-ENTRY)*
   *                       flow_sequence_entry?
   *                       FLOW-SEQUENCE-END
   * flow_sequence_entry   ::= flow_node | KEY flow_node? (VALUE flow_node?)?
   * Note that while production rules for both flow_sequence_entry and
   * flow_mapping_entry are equal, their interpretations are different.
   * For `flow_sequence_entry`, the part `KEY flow_node? (VALUE flow_node?)?`
   * generate an inline mapping (set syntax).
   * </pre>
   */
  private class ParseFlowSequenceFirstEntry implements SjlExtProduction {

    public SjlExtEvent produce() {
      SjlExtToken token = scanner.next();
      markPush(token.getStartMark());
      return new ParseFlowSequenceEntry(true).produce();
    }
  }

  private class ParseFlowSequenceEntry implements SjlExtProduction {

    private final boolean first;

    public ParseFlowSequenceEntry(boolean first) {
      this.first = first;
    }

    public SjlExtEvent produce() {
      if (scanner.checkToken(SjlExtToken.ID.Comment)) {
        state = Optional.of(new ParseFlowSequenceEntry(first));
        return produceCommentEvent((SjlExtCommentToken) scanner.next());
      }
      if (!scanner.checkToken(SjlExtToken.ID.FlowSequenceEnd)) {
        if (!first) {
          if (scanner.checkToken(SjlExtToken.ID.FlowEntry)) {
            scanner.next();
            if (scanner.checkToken(SjlExtToken.ID.Comment)) {
              state = Optional.of(new ParseFlowSequenceEntry(true));
              return produceCommentEvent((SjlExtCommentToken) scanner.next());
            }
          } else {
            SjlExtToken token = scanner.peekToken();
            throw new SjlExtParserException("while parsing a flow sequence", markPop(),
                "expected ',' or ']', but got " + token.getTokenId(),
                token.getStartMark());
          }
        }
        if (scanner.checkToken(SjlExtToken.ID.Key)) {
          SjlExtToken token = scanner.peekToken();
          SjlExtEvent event = new SjlExtMappingStartEvent(Optional.empty(), Optional.empty(), true,
              SjlExtFlowStyle.FLOW, token.getStartMark(),
              token.getEndMark());
          state = Optional.of(new ParseFlowSequenceEntryMappingKey());
          return event;
        } else if (!scanner.checkToken(SjlExtToken.ID.FlowSequenceEnd)) {
          states.push(new ParseFlowSequenceEntry(false));
          return parseFlowNode();
        }
      }
      SjlExtToken token = scanner.next();
      SjlExtEvent event = new SjlExtSequenceEndEvent(token.getStartMark(), token.getEndMark());
      if (!scanner.checkToken(SjlExtToken.ID.Comment)) {
        state = Optional.of(states.pop());
      } else {
        state = Optional.of(new ParseFlowEndComment());
      }
      markPop();
      return event;
    }
  }

  private class ParseFlowEndComment implements SjlExtProduction {

    public SjlExtEvent produce() {
      SjlExtEvent event = produceCommentEvent((SjlExtCommentToken) scanner.next());
      if (!scanner.checkToken(SjlExtToken.ID.Comment)) {
        state = Optional.of(states.pop());
      }
      return event;
    }
  }

  private class ParseFlowSequenceEntryMappingKey implements SjlExtProduction {

    public SjlExtEvent produce() {
      SjlExtToken token = scanner.next();
      if (!scanner.checkToken(SjlExtToken.ID.Value, SjlExtToken.ID.FlowEntry, SjlExtToken.ID.FlowSequenceEnd)) {
        states.push(new ParseFlowSequenceEntryMappingValue());
        return parseFlowNode();
      } else {
        state = Optional.of(new ParseFlowSequenceEntryMappingValue());
        return processEmptyScalar(token.getEndMark());
      }
    }
  }

  private class ParseFlowSequenceEntryMappingValue implements SjlExtProduction {

    public SjlExtEvent produce() {
      if (scanner.checkToken(SjlExtToken.ID.Value)) {
        SjlExtToken token = scanner.next();
        if (!scanner.checkToken(SjlExtToken.ID.FlowEntry, SjlExtToken.ID.FlowSequenceEnd)) {
          states.push(new ParseFlowSequenceEntryMappingEnd());
          return parseFlowNode();
        } else {
          state = Optional.of(new ParseFlowSequenceEntryMappingEnd());
          return processEmptyScalar(token.getEndMark());
        }
      } else {
        state = Optional.of(new ParseFlowSequenceEntryMappingEnd());
        SjlExtToken token = scanner.peekToken();
        return processEmptyScalar(token.getStartMark());
      }
    }
  }

  private class ParseFlowSequenceEntryMappingEnd implements SjlExtProduction {

    public SjlExtEvent produce() {
      state = Optional.of(new ParseFlowSequenceEntry(false));
      SjlExtToken token = scanner.peekToken();
      return new SjlExtMappingEndEvent(token.getStartMark(), token.getEndMark());
    }
  }

  /**
   * <pre>
   *   flow_mapping  ::= FLOW-MAPPING-START
   *          (flow_mapping_entry FLOW-ENTRY)*
   *          flow_mapping_entry?
   *          FLOW-MAPPING-END
   *   flow_mapping_entry    ::= flow_node | KEY flow_node? (VALUE flow_node?)?
   * </pre>
   */
  private class ParseFlowMappingFirstKey implements SjlExtProduction {

    public SjlExtEvent produce() {
      SjlExtToken token = scanner.next();
      markPush(token.getStartMark());
      return new ParseFlowMappingKey(true).produce();
    }
  }

  private class ParseFlowMappingKey implements SjlExtProduction {

    private final boolean first;

    public ParseFlowMappingKey(boolean first) {
      this.first = first;
    }

    public SjlExtEvent produce() {
      if (!scanner.checkToken(SjlExtToken.ID.FlowMappingEnd)) {
        if (!first) {
          if (scanner.checkToken(SjlExtToken.ID.FlowEntry)) {
            scanner.next();
          } else {
            SjlExtToken token = scanner.peekToken();
            throw new SjlExtParserException("while parsing a flow mapping", markPop(),
                "expected ',' or '}', but got " + token.getTokenId(),
                token.getStartMark());
          }
        }
        if (scanner.checkToken(SjlExtToken.ID.Key)) {
          SjlExtToken token = scanner.next();
          if (!scanner.checkToken(SjlExtToken.ID.Value, SjlExtToken.ID.FlowEntry,
              SjlExtToken.ID.FlowMappingEnd)) {
            states.push(new ParseFlowMappingValue());
            return parseFlowNode();
          } else {
            state = Optional.of(new ParseFlowMappingValue());
            return processEmptyScalar(token.getEndMark());
          }
        } else if (!scanner.checkToken(SjlExtToken.ID.FlowMappingEnd)) {
          states.push(new ParseFlowMappingEmptyValue());
          return parseFlowNode();
        }
      }
      SjlExtToken token = scanner.next();
      SjlExtEvent event = new SjlExtMappingEndEvent(token.getStartMark(), token.getEndMark());
      markPop();
      if (!scanner.checkToken(SjlExtToken.ID.Comment)) {
        state = Optional.of(states.pop());
      } else {
        state = Optional.of(new ParseFlowEndComment());
      }
      return event;
    }
  }

  private class ParseFlowMappingValue implements SjlExtProduction {

    public SjlExtEvent produce() {
      if (scanner.checkToken(SjlExtToken.ID.Value)) {
        SjlExtToken token = scanner.next();
        if (!scanner.checkToken(SjlExtToken.ID.FlowEntry, SjlExtToken.ID.FlowMappingEnd)) {
          states.push(new ParseFlowMappingKey(false));
          return parseFlowNode();
        } else {
          state = Optional.of(new ParseFlowMappingKey(false));
          return processEmptyScalar(token.getEndMark());
        }
      } else {
        state = Optional.of(new ParseFlowMappingKey(false));
        SjlExtToken token = scanner.peekToken();
        return processEmptyScalar(token.getStartMark());
      }
    }
  }

  private class ParseFlowMappingEmptyValue implements SjlExtProduction {

    public SjlExtEvent produce() {
      state = Optional.of(new ParseFlowMappingKey(false));
      return processEmptyScalar(scanner.peekToken().getStartMark());
    }
  }

  /**
   * <pre>
   * block_mapping     ::= BLOCK-MAPPING_START
   *           ((KEY block_node_or_indentless_sequence?)?
   *           (VALUE block_node_or_indentless_sequence?)?)*
   *           BLOCK-END
   * </pre>
   */
  private SjlExtEvent processEmptyScalar(Optional<SjlExtMark> mark) {
    return new SjlExtScalarEvent(Optional.empty(), Optional.empty(), new SjlExtImplicitTuple(true, false), "",
        SjlExtScalarStyle.PLAIN, mark, mark);
  }

  private Optional<SjlExtMark> markPop() {
    return marksStack.pop();
  }

  private void markPush(Optional<SjlExtMark> mark) {
    marksStack.push(mark);
  }
}
