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
package com.vga.sjl.external.org.snakeyaml.engine.v2.common;

import com.vga.sjl.external.org.snakeyaml.engine.v2.exceptions.SjlExtEmitterException;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Value inside Anchor and Alias
 */
public class SjlExtAnchor {

  private static final Set<Character> INVALID_ANCHOR = new HashSet();
  private static final Pattern SPACES_PATTERN = Pattern.compile("\\s");

  static {
    INVALID_ANCHOR.add('[');
    INVALID_ANCHOR.add(']');
    INVALID_ANCHOR.add('{');
    INVALID_ANCHOR.add('}');
    INVALID_ANCHOR.add(',');
    INVALID_ANCHOR.add('*');
    INVALID_ANCHOR.add('&');
  }

  private final String value;

  public SjlExtAnchor(String value) {
    Objects.requireNonNull(value);
    if (value.isEmpty()) {
      throw new IllegalArgumentException("Empty anchor.");
    }
    for (int i = 0; i < value.length(); i++) {
      char ch = value.charAt(i);
      if (INVALID_ANCHOR.contains(ch)) {
        throw new SjlExtEmitterException("Invalid character '" + ch + "' in the anchor: " + value);
      }
    }
    Matcher matcher = SPACES_PATTERN.matcher(value);
    if (matcher.find()) {
      throw new SjlExtEmitterException("Anchor may not contain spaces: " + value);
    }
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  @Override
  public String toString() {
    return value;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SjlExtAnchor anchor1 = (SjlExtAnchor) o;
    return Objects.equals(value, anchor1.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(value);
  }
}
