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


import com.vga.sjl.external.org.snakeyaml.engine.v2.nodes.SjlExtNode;

/**
 * Create a Node Graph out of the provided Native Data Structure (Java instance). (this is opposite
 * to ConstructNode)
 *
 * @see <a href="http://www.yaml.org/spec/1.2/spec.html#id2762107">Processing Overview</a>
 */
public interface SjlExtRepresentToNode {

  /**
   * Create a Node
   *
   * @param data the instance to represent
   * @return Node to dump
   */
  SjlExtNode representData(Object data);
}
