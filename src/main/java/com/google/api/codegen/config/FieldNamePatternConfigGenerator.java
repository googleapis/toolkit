/* Copyright 2016 Google Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.api.codegen.config;

import com.google.api.codegen.Resources;
import com.google.api.tools.framework.aspects.http.model.HttpAttribute.FieldSegment;
import com.google.api.tools.framework.model.Interface;
import com.google.api.tools.framework.model.Method;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Config generator for field name patterns.
 */
public class FieldNamePatternConfigGenerator implements MethodConfigGenerator {

  private static final String CONFIG_KEY_FIELD_NAME_PATTERNS = "field_name_patterns";

  private Map<String, List<FieldSegment>> fieldSegmentMap;

  public FieldNamePatternConfigGenerator(Interface service) {
    fieldSegmentMap = Resources.getAllFieldSegmentsFromHttpPathsPerMethod(service.getMethods());
  }

  @Override
  public Map<String, Object> generate(Method method) {
    if (fieldSegmentMap.containsKey(method.getFullName())) {
      Map<String, Object> nameMap = new LinkedHashMap<String, Object>();
      for (FieldSegment fieldSegment : fieldSegmentMap.get(method.getFullName())) {
        nameMap.put(fieldSegment.getFieldPath(), Resources.getEntityName(fieldSegment));
      }
      Map<String, Object> result = new LinkedHashMap<String, Object>();
      result.put(CONFIG_KEY_FIELD_NAME_PATTERNS, nameMap);
      return result;
    } else {
      return null;
    }
  }
}
