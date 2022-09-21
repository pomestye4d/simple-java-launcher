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

import com.vga.sjl.external.org.snakeyaml.engine.v2.api.SjlExtDumpSettings;
import com.vga.sjl.external.org.snakeyaml.engine.v2.api.SjlExtRepresentToNode;
import com.vga.sjl.external.org.snakeyaml.engine.v2.common.SjlExtFlowStyle;
import com.vga.sjl.external.org.snakeyaml.engine.v2.common.SjlExtNonPrintableStyle;
import com.vga.sjl.external.org.snakeyaml.engine.v2.common.SjlExtScalarStyle;
import com.vga.sjl.external.org.snakeyaml.engine.v2.exceptions.SjlExtYamlEngineException;
import com.vga.sjl.external.org.snakeyaml.engine.v2.nodes.SjlExtNode;
import com.vga.sjl.external.org.snakeyaml.engine.v2.nodes.SjlExtTag;
import com.vga.sjl.external.org.snakeyaml.engine.v2.scanner.SjlExtStreamReader;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Represent standard Java classes
 */
public class SjlExtStandardRepresenter extends SjlExtBaseRepresenter {

  protected Map<Class<? extends Object>, SjlExtTag> classTags;
  protected SjlExtDumpSettings settings;

  public SjlExtStandardRepresenter(SjlExtDumpSettings settings) {
    this.defaultFlowStyle = settings.getDefaultFlowStyle();
    this.defaultScalarStyle = settings.getDefaultScalarStyle();

    this.nullRepresenter = new RepresentNull();
    this.representers.put(String.class, new RepresentString());
    this.representers.put(Boolean.class, new RepresentBoolean());
    this.representers.put(Character.class, new RepresentString());
    this.representers.put(UUID.class, new RepresentUuid());
    this.representers.put(Optional.class, new RepresentOptional());
    this.representers.put(byte[].class, new RepresentByteArray());

    SjlExtRepresentToNode primitiveArray = new RepresentPrimitiveArray();
    representers.put(short[].class, primitiveArray);
    representers.put(int[].class, primitiveArray);
    representers.put(long[].class, primitiveArray);
    representers.put(float[].class, primitiveArray);
    representers.put(double[].class, primitiveArray);
    representers.put(char[].class, primitiveArray);
    representers.put(boolean[].class, primitiveArray);

    this.parentClassRepresenters.put(Number.class, new RepresentNumber());
    this.parentClassRepresenters.put(List.class, new RepresentList());
    this.parentClassRepresenters.put(Map.class, new RepresentMap());
    this.parentClassRepresenters.put(Set.class, new RepresentSet());
    this.parentClassRepresenters.put(Iterator.class, new RepresentIterator());
    this.parentClassRepresenters.put(new Object[0].getClass(), new RepresentArray());
    this.parentClassRepresenters.put(Enum.class, new RepresentEnum());
    classTags = new HashMap();
    this.settings = settings;
  }

  protected SjlExtTag getTag(Class<?> clazz, SjlExtTag defaultTag) {
    if (classTags.containsKey(clazz)) {
      return classTags.get(clazz);
    } else {
      return defaultTag;
    }
  }

  /**
   * Define a tag for the <code>Class</code> to serialize.
   *
   * @param clazz <code>Class</code> which tag is changed
   * @param tag   new tag to be used for every instance of the specified
   *              <code>Class</code>
   * @return the previous tag associated with the <code>Class</code>
   */
  public SjlExtTag addClassTag(Class<? extends Object> clazz, SjlExtTag tag) {
    if (tag == null) {
      throw new NullPointerException("Tag must be provided.");
    }
    return classTags.put(clazz, tag);
  }

  //remove and move to BaseRe
  protected class RepresentNull implements SjlExtRepresentToNode {

    public SjlExtNode representData(Object data) {
      return representScalar(SjlExtTag.NULL, "null");
    }
  }

  public static final Pattern MULTILINE_PATTERN = Pattern.compile("\n|\u0085|\u2028|\u2029");

  protected class RepresentString implements SjlExtRepresentToNode {

    public SjlExtNode representData(Object data) {
      SjlExtTag tag = SjlExtTag.STR;
      SjlExtScalarStyle style = SjlExtScalarStyle.PLAIN;
      String value = data.toString();
      if (settings.getNonPrintableStyle() == SjlExtNonPrintableStyle.BINARY && !SjlExtStreamReader.isPrintable(
          value)) {
        tag = SjlExtTag.BINARY;
        final byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        // sometimes above will just silently fail - it will return incomplete data
        // it happens when String has invalid code points
        // (for example half surrogate character without other half)
        final String checkValue = new String(bytes, StandardCharsets.UTF_8);
        if (!checkValue.equals(value)) {
          throw new SjlExtYamlEngineException("invalid string value has occurred");
        }
        value = Base64.getEncoder().encodeToString(bytes);
        style = SjlExtScalarStyle.LITERAL;
      }
      // if no other scalar style is explicitly set, use literal style for
      // multiline scalars
      if (defaultScalarStyle == SjlExtScalarStyle.PLAIN && MULTILINE_PATTERN.matcher(value).find()) {
        style = SjlExtScalarStyle.LITERAL;
      }
      return representScalar(tag, value, style);
    }
  }

  protected class RepresentBoolean implements SjlExtRepresentToNode {

    public SjlExtNode representData(Object data) {
      String value;
      if (Boolean.TRUE.equals(data)) {
        value = "true";
      } else {
        value = "false";
      }
      return representScalar(SjlExtTag.BOOL, value);
    }
  }

  protected class RepresentNumber implements SjlExtRepresentToNode {

    public SjlExtNode representData(Object data) {
      SjlExtTag tag;
      String value;
      if (data instanceof Byte || data instanceof Short || data instanceof Integer
          || data instanceof Long || data instanceof BigInteger) {
        tag = SjlExtTag.INT;
        value = data.toString();
      } else {
        Number number = (Number) data;
        tag = SjlExtTag.FLOAT;
        if (number.equals(Double.NaN)) {
          value = ".NaN";
        } else if (number.equals(Double.POSITIVE_INFINITY)) {
          value = ".inf";
        } else if (number.equals(Double.NEGATIVE_INFINITY)) {
          value = "-.inf";
        } else {
          value = number.toString();
        }
      }
      return representScalar(getTag(data.getClass(), tag), value);
    }
  }

  protected class RepresentList implements SjlExtRepresentToNode {

    @SuppressWarnings("unchecked")
    public SjlExtNode representData(Object data) {
      return representSequence(getTag(data.getClass(), SjlExtTag.SEQ), (List<Object>) data,
          SjlExtFlowStyle.AUTO);
    }
  }

  protected class RepresentIterator implements SjlExtRepresentToNode {

    @SuppressWarnings("unchecked")
    public SjlExtNode representData(Object data) {
      Iterator<Object> iter = (Iterator<Object>) data;
      return representSequence(getTag(data.getClass(), SjlExtTag.SEQ), new IteratorWrapper(iter),
          SjlExtFlowStyle.AUTO);
    }
  }

  private static class IteratorWrapper implements Iterable<Object> {

    private final Iterator<Object> iter;

    public IteratorWrapper(Iterator<Object> iter) {
      this.iter = iter;
    }

    public Iterator<Object> iterator() {
      return iter;
    }
  }

  protected class RepresentArray implements SjlExtRepresentToNode {

    public SjlExtNode representData(Object data) {
      Object[] array = (Object[]) data;
      List<Object> list = Arrays.asList(array);
      return representSequence(SjlExtTag.SEQ, list, SjlExtFlowStyle.AUTO);
    }
  }

  /**
   * Represents primitive arrays, such as short[] and float[], by converting them into equivalent
   * {@link List} using the appropriate autoboxing type.
   */
  protected class RepresentPrimitiveArray implements SjlExtRepresentToNode {

    public SjlExtNode representData(Object data) {
      Class<?> type = data.getClass().getComponentType();

      if (byte.class == type) {
        return representSequence(SjlExtTag.SEQ, asByteList(data), SjlExtFlowStyle.AUTO);
      } else if (short.class == type) {
        return representSequence(SjlExtTag.SEQ, asShortList(data), SjlExtFlowStyle.AUTO);
      } else if (int.class == type) {
        return representSequence(SjlExtTag.SEQ, asIntList(data), SjlExtFlowStyle.AUTO);
      } else if (long.class == type) {
        return representSequence(SjlExtTag.SEQ, asLongList(data), SjlExtFlowStyle.AUTO);
      } else if (float.class == type) {
        return representSequence(SjlExtTag.SEQ, asFloatList(data), SjlExtFlowStyle.AUTO);
      } else if (double.class == type) {
        return representSequence(SjlExtTag.SEQ, asDoubleList(data), SjlExtFlowStyle.AUTO);
      } else if (char.class == type) {
        return representSequence(SjlExtTag.SEQ, asCharList(data), SjlExtFlowStyle.AUTO);
      } else if (boolean.class == type) {
        return representSequence(SjlExtTag.SEQ, asBooleanList(data), SjlExtFlowStyle.AUTO);
      }

      throw new SjlExtYamlEngineException("Unexpected primitive '" + type.getCanonicalName() + "'");
    }

    private List<Byte> asByteList(Object in) {
      byte[] array = (byte[]) in;
      List<Byte> list = new ArrayList<>(array.length);
      for (int i = 0; i < array.length; ++i) {
        list.add(array[i]);
      }
      return list;
    }

    private List<Short> asShortList(Object in) {
      short[] array = (short[]) in;
      List<Short> list = new ArrayList<>(array.length);
      for (int i = 0; i < array.length; ++i) {
        list.add(array[i]);
      }
      return list;
    }

    private List<Integer> asIntList(Object in) {
      int[] array = (int[]) in;
      List<Integer> list = new ArrayList<>(array.length);
      for (int i = 0; i < array.length; ++i) {
        list.add(array[i]);
      }
      return list;
    }

    private List<Long> asLongList(Object in) {
      long[] array = (long[]) in;
      List<Long> list = new ArrayList<>(array.length);
      for (int i = 0; i < array.length; ++i) {
        list.add(array[i]);
      }
      return list;
    }

    private List<Float> asFloatList(Object in) {
      float[] array = (float[]) in;
      List<Float> list = new ArrayList<>(array.length);
      for (int i = 0; i < array.length; ++i) {
        list.add(array[i]);
      }
      return list;
    }

    private List<Double> asDoubleList(Object in) {
      double[] array = (double[]) in;
      List<Double> list = new ArrayList<>(array.length);
      for (int i = 0; i < array.length; ++i) {
        list.add(array[i]);
      }
      return list;
    }

    private List<Character> asCharList(Object in) {
      char[] array = (char[]) in;
      List<Character> list = new ArrayList<>(array.length);
      for (int i = 0; i < array.length; ++i) {
        list.add(array[i]);
      }
      return list;
    }

    private List<Boolean> asBooleanList(Object in) {
      boolean[] array = (boolean[]) in;
      List<Boolean> list = new ArrayList<>(array.length);
      for (int i = 0; i < array.length; ++i) {
        list.add(array[i]);
      }
      return list;
    }
  }

  protected class RepresentMap implements SjlExtRepresentToNode {

    @SuppressWarnings("unchecked")
    public SjlExtNode representData(Object data) {
      return representMapping(getTag(data.getClass(), SjlExtTag.MAP), (Map<Object, Object>) data,
          SjlExtFlowStyle.AUTO);
    }
  }

  protected class RepresentSet implements SjlExtRepresentToNode {

    @SuppressWarnings("unchecked")
    public SjlExtNode representData(Object data) {
      Map<Object, Object> value = new LinkedHashMap<>();
      Set<Object> set = (Set<Object>) data;
      for (Object key : set) {
        value.put(key, null);
      }
      return representMapping(getTag(data.getClass(), SjlExtTag.SET), value, SjlExtFlowStyle.AUTO);
    }
  }


  protected class RepresentEnum implements SjlExtRepresentToNode {

    public SjlExtNode representData(Object data) {
      SjlExtTag tag = new SjlExtTag(data.getClass());
      return representScalar(getTag(data.getClass(), tag), ((Enum<?>) data).name());
    }
  }

  protected class RepresentByteArray implements SjlExtRepresentToNode {

    public SjlExtNode representData(Object data) {
      return representScalar(SjlExtTag.BINARY, Base64.getEncoder().encodeToString((byte[]) data),
          SjlExtScalarStyle.LITERAL);
    }
  }

  protected class RepresentUuid implements SjlExtRepresentToNode {

    public SjlExtNode representData(Object data) {
      return representScalar(getTag(data.getClass(), new SjlExtTag(UUID.class)), data.toString());
    }
  }

  protected class RepresentOptional implements SjlExtRepresentToNode {

    public SjlExtNode representData(Object data) {
      Optional<?> opt = (Optional<?>) data;
      if (opt.isPresent()) {
        SjlExtNode node = represent(opt.get());
        node.setTag(new SjlExtTag(Optional.class));
        return node;
      } else {
        return representScalar(SjlExtTag.NULL, "null");
      }
    }
  }
}
