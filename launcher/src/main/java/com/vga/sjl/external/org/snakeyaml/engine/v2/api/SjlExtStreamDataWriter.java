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
package com.vga.sjl.external.org.snakeyaml.engine.v2.api;

/**
 * Writer with the same methods as in {@link java.io.Writer} but without throwing IOExceptions The
 * purpose of this class is to avoid checked exceptions in every method signature. Implementations
 * must define their own way to react on IOExceptions {@link SjlExtYamlOutputStreamWriter}
 */
public interface SjlExtStreamDataWriter {

  /**
   * Flushes this stream by writing any buffered output to the underlying stream.
   */
  default void flush() {
  }

  void write(String str);

  void write(String str, int off, int len);
}
