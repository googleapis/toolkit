/* Copyright 2017 Google Inc
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
package com.google.api.codegen;

import com.google.api.codegen.configgen.DiscoConfigGeneratorApi;
import com.google.api.codegen.configgen.DiscoConfigGeneratorApi;
import com.google.api.tools.framework.model.testing.ConfigBaselineTestCase;
import com.google.api.tools.framework.model.testing.DiscoConfigBaselineTestCase;
import com.google.api.tools.framework.snippet.Doc;
import com.google.api.tools.framework.tools.ToolOptions;
import com.google.common.collect.Lists;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;

public class DiscoConfigGenerationTest extends DiscoConfigBaselineTestCase {

  @Override
  protected String baselineFileName() {
    return "simplecompute.v1.json" + "_config.baseline";
  }

  @Override
  protected boolean suppressDiagnosis() {
    // Suppress linter warnings
    return true;
  }

  @Override
  public Map<String, String> run() {
    String outFile = tempDir.getRoot().getPath() + File.separator + baselineFileName();
    String discoveryFile = tempDir.getRoot().getPath() + File.separator + baselineFileName();
    ToolOptions options = ToolOptions.create();
    options.set(DiscoConfigGeneratorApi.OUTPUT_FILE, outFile);
    options.set(DiscoConfigGeneratorApi.DISCOVERY_DOC, discoveryFile);
    new DiscoConfigGeneratorApi(options).run();

    String outputContent;
    try {
      outputContent = new String(Files.readAllBytes(Paths.get(outFile)), StandardCharsets.UTF_8);
    } catch (IOException e) {
      return null;
    }

    Map<String, String> output = new HashMap<>();
    output.put(outFile, outputContent);
    return output;
  }

  @Before
  public void setup() {
    getTestDataLocator().addTestDataSource(getClass(), "testsrc");
  }

  @Test
  public void simpleCompute() throws Exception {
    test(Lists.<String>newArrayList("simplecompute"));
  }
}
