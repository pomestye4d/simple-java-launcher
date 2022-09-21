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

import com.vga.sjl.external.org.snakeyaml.engine.v2.common.SjlExtUriEncoder;

import java.util.Objects;

public final class SjlExtTag {

  public static final String PREFIX = "tag:yaml.org,2002:";
  public static final SjlExtTag SET = new SjlExtTag(PREFIX + "set");
  public static final SjlExtTag BINARY = new SjlExtTag(PREFIX + "binary");
  public static final SjlExtTag INT = new SjlExtTag(PREFIX + "int");
  public static final SjlExtTag FLOAT = new SjlExtTag(PREFIX + "float");
  public static final SjlExtTag BOOL = new SjlExtTag(PREFIX + "bool");
  public static final SjlExtTag NULL = new SjlExtTag(PREFIX + "null");
  public static final SjlExtTag STR = new SjlExtTag(PREFIX + "str");
  public static final SjlExtTag SEQ = new SjlExtTag(PREFIX + "seq");
  public static final SjlExtTag MAP = new SjlExtTag(PREFIX + "map");
  // For use to indicate a DUMMY node that contains comments, when there is no other (empty document)
  public static final SjlExtTag COMMENT = new SjlExtTag(PREFIX + "comment");

  public static final SjlExtTag ENV_TAG = new SjlExtTag("!ENV_VARIABLE");

  private final String value;

  public SjlExtTag(String tag) {
    Objects.requireNonNull(tag, "Tag must be provided.");
    if (tag.isEmpty()) {
      throw new IllegalArgumentException("Tag must not be empty.");
    } else if (tag.trim().length() != tag.length()) {
      throw new IllegalArgumentException("Tag must not contain leading or trailing spaces.");
    }
    this.value = SjlExtUriEncoder.encode(tag);
  }

  /**
   * Create a global tag to dump the fully qualified class name
   *
   * @param clazz - the class to use the name
   */
  public SjlExtTag(Class<? extends Object> clazz) {
    Objects.requireNonNull(clazz, "Class for tag must be provided.");
    this.value = SjlExtTag.PREFIX + SjlExtUriEncoder.encode(clazz.getName());
  }

  public String getValue() {
    return value;
  }

  @Override
  public String toString() {
    return value;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof SjlExtTag) {
      return value.equals(((SjlExtTag) obj).getValue());
    } else {
      return false;
    }
  }

  @Override
  public int hashCode() {
    return value.hashCode();
  }
}

