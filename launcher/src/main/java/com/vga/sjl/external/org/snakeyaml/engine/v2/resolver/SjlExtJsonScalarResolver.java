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
package com.vga.sjl.external.org.snakeyaml.engine.v2.resolver;

import com.vga.sjl.external.org.snakeyaml.engine.v2.nodes.SjlExtTag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * ScalarResolver for JSON Schema The schema is NOT the same as in YAML 1.2 but identical to JSON,
 *
 * @see <a href="http://www.yaml.org/spec/1.2/spec.html#id2803231">Chapter 10.2. JSON Schema</a>
 */
public class SjlExtJsonScalarResolver implements SjlExtScalarResolver {

  public static final Pattern BOOL = Pattern.compile("^(?:true|false)$");
  public static final Pattern FLOAT = Pattern.compile(
      "^(-?(0?\\.[0-9]+|[1-9][0-9]*(\\.[0-9]*)?)(e[-+]?[0-9]+)?)|-?\\.(?:inf)|\\.(?:nan)$"); //NOSONAR
  public static final Pattern INT = Pattern.compile("^(?:-?(?:0|[1-9][0-9]*))$");
  public static final Pattern NULL = Pattern.compile("^(?:null)$");
  public static final Pattern EMPTY = Pattern.compile("^$");

  /** group 1: name, group 2: separator, group 3: value */
  @SuppressWarnings("squid:S4784")
  public static final Pattern ENV_FORMAT = Pattern.compile(
      "^\\$\\{\\s*(?:(\\w+)(?:(:?[-?])(\\w+)?)?)\\s*\\}$");

  protected Map<Character, List<SjlExtResolverTuple>> yamlImplicitResolvers = new HashMap();

  public void addImplicitResolver(SjlExtTag tag, Pattern regexp, String first) {
    if (first == null) {
      List<SjlExtResolverTuple> curr = yamlImplicitResolvers.computeIfAbsent(null, c -> new ArrayList());
      curr.add(new SjlExtResolverTuple(tag, regexp));
    } else {
      char[] chrs = first.toCharArray();
      for (int i = 0, j = chrs.length; i < j; i++) {
        Character theC = Character.valueOf(chrs[i]);
        if (theC == 0) {
          // special case: for null
          theC = null;
        }
        List<SjlExtResolverTuple> curr = yamlImplicitResolvers.get(theC);
        if (curr == null) {
          curr = new ArrayList();
          yamlImplicitResolvers.put(theC, curr);
        }
        curr.add(new SjlExtResolverTuple(tag, regexp));
      }
    }
  }

  protected void addImplicitResolvers() {
    addImplicitResolver(SjlExtTag.NULL, EMPTY, null);
    addImplicitResolver(SjlExtTag.BOOL, BOOL, "tf");
    /*
     * INT must be before FLOAT because the regular expression for FLOAT
     * matches INT (see issue 130)
     * http://code.google.com/p/snakeyaml/issues/detail?id=130
     */
    addImplicitResolver(SjlExtTag.INT, INT, "-0123456789");
    addImplicitResolver(SjlExtTag.FLOAT, FLOAT, "-0123456789.");
    addImplicitResolver(SjlExtTag.NULL, NULL, "n\u0000");
    addImplicitResolver(SjlExtTag.ENV_TAG, ENV_FORMAT, "$");
  }

  public SjlExtJsonScalarResolver() {
    addImplicitResolvers();
  }

  @Override
  public SjlExtTag resolve(String value, Boolean implicit) {
    if (!implicit) {
      return SjlExtTag.STR;
    }
    final List<SjlExtResolverTuple> resolvers;
    if (value.length() == 0) {
      resolvers = yamlImplicitResolvers.get('\0');
    } else {
      resolvers = yamlImplicitResolvers.get(value.charAt(0));
    }
    if (resolvers != null) {
      for (SjlExtResolverTuple v : resolvers) {
        SjlExtTag tag = v.getTag();
        Pattern regexp = v.getRegexp();
        if (regexp.matcher(value).matches()) {
          return tag;
        }
      }
    }
    if (yamlImplicitResolvers.containsKey(null)) {
      for (SjlExtResolverTuple v : yamlImplicitResolvers.get(null)) {
        SjlExtTag tag = v.getTag();
        Pattern regexp = v.getRegexp();
        if (regexp.matcher(value).matches()) {
          return tag;
        }
      }
    }
    return SjlExtTag.STR;
  }
}
