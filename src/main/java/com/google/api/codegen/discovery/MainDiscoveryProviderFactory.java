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
package com.google.api.codegen.discovery;

import org.apache.commons.lang3.NotImplementedException;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.api.Service;
import com.google.api.codegen.ApiaryConfig;
import com.google.api.codegen.SnippetSetRunner;
import com.google.api.codegen.csharp.CSharpDiscoveryContext;
import com.google.api.codegen.csharp.CSharpSnippetSetRunner;
import com.google.api.codegen.discovery.config.java.JavaTypeNameGenerator;
import com.google.api.codegen.discovery.transformer.java.JavaSampleMethodToViewTransformer;
import com.google.api.codegen.go.GoDiscoveryContext;
import com.google.api.codegen.go.GoSnippetSetRunner;
import com.google.api.codegen.nodejs.NodeJSDiscoveryContext;
import com.google.api.codegen.nodejs.NodeJSSnippetSetRunner;
import com.google.api.codegen.php.PhpDiscoveryContext;
import com.google.api.codegen.php.PhpSnippetSetRunner;
import com.google.api.codegen.py.PythonDiscoveryContext;
import com.google.api.codegen.py.PythonDiscoveryInitializer;
import com.google.api.codegen.py.PythonSnippetSetRunner;
import com.google.api.codegen.rendering.CommonSnippetSetRunner;
import com.google.api.codegen.ruby.RubyDiscoveryContext;
import com.google.api.codegen.ruby.RubySnippetSetRunner;
import com.google.api.codegen.util.CommonRenderingUtil;
import com.google.protobuf.Method;

/*
 * Creates DiscoveryProvider instances based on an ID.
 */
public class MainDiscoveryProviderFactory implements DiscoveryProviderFactory {

  public static final String CSHARP = "csharp";
  public static final String GO = "go";
  public static final String JAVA = "java";
  public static final String NODEJS = "nodejs";
  public static final String PHP = "php";
  public static final String PYTHON = "python";
  public static final String RUBY = "ruby";

  private static final String DEFAULT_SNIPPET_FILE = "discovery_fragment.snip";

  public static DiscoveryProvider defaultCreate(
      Service service, ApiaryConfig apiaryConfig, JsonNode sampleConfigOverrides, String id) {
    // If the JSON object has a language field at root that matches the current
    // language, use that node instead. Conversely, if there is no language
    // field, don't adjust the node.
    if (sampleConfigOverrides != null && sampleConfigOverrides.has(id)) {
      sampleConfigOverrides = sampleConfigOverrides.get(id);
    }
    if (id.equals(CSHARP)) {
      return CommonDiscoveryProvider.newBuilder()
          .setContext(new CSharpDiscoveryContext(service, apiaryConfig))
          .setSnippetSetRunner(
              new CSharpSnippetSetRunner<Method>(SnippetSetRunner.SNIPPET_RESOURCE_ROOT))
          .setSnippetFileName(id + "/" + DEFAULT_SNIPPET_FILE)
          .build();

    } else if (id.equals(GO)) {
      return CommonDiscoveryProvider.newBuilder()
          .setContext(new GoDiscoveryContext(service, apiaryConfig))
          .setSnippetSetRunner(
              new GoSnippetSetRunner<Method>(SnippetSetRunner.SNIPPET_RESOURCE_ROOT))
          .setSnippetFileName(id + "/" + DEFAULT_SNIPPET_FILE)
          .build();

    } else if (id.equals(JAVA)) {
      return ViewModelProvider.newBuilder()
          .setApiaryConfig(apiaryConfig)
          .setSnippetSetRunner(new CommonSnippetSetRunner(new CommonRenderingUtil()))
          .setMethodToViewTransformer(new JavaSampleMethodToViewTransformer())
          .setOverrides(sampleConfigOverrides)
          .setTypeNameGenerator(new JavaTypeNameGenerator())
          .setOutputRoot(
              "autogenerated/"
                  + apiaryConfig.getApiName()
                  + "/"
                  + apiaryConfig.getApiVersion()
                  + "/"
                  + service.getDocumentation().getOverview())
          .build();
    } else if (id.equals(NODEJS)) {
      return CommonDiscoveryProvider.newBuilder()
          .setContext(new NodeJSDiscoveryContext(service, apiaryConfig))
          .setSnippetSetRunner(
              new NodeJSSnippetSetRunner<Method>(SnippetSetRunner.SNIPPET_RESOURCE_ROOT))
          .setSnippetFileName(id + "/" + DEFAULT_SNIPPET_FILE)
          .build();

    } else if (id.equals(PHP)) {
      return CommonDiscoveryProvider.newBuilder()
          .setContext(new PhpDiscoveryContext(service, apiaryConfig))
          .setSnippetSetRunner(
              new PhpSnippetSetRunner<Method>(SnippetSetRunner.SNIPPET_RESOURCE_ROOT))
          .setSnippetFileName(id + "/" + DEFAULT_SNIPPET_FILE)
          .build();

    } else if (id.equals(PYTHON)) {
      return CommonDiscoveryProvider.newBuilder()
          .setContext(new PythonDiscoveryContext(service, apiaryConfig))
          .setSnippetSetRunner(
              new PythonSnippetSetRunner<Method>(
                  new PythonDiscoveryInitializer(), SnippetSetRunner.SNIPPET_RESOURCE_ROOT))
          .setSnippetFileName("py/" + DEFAULT_SNIPPET_FILE)
          .build();

    } else if (id.equals(RUBY)) {
      return CommonDiscoveryProvider.newBuilder()
          .setContext(new RubyDiscoveryContext(service, apiaryConfig))
          .setSnippetSetRunner(
              new RubySnippetSetRunner<Method>(SnippetSetRunner.SNIPPET_RESOURCE_ROOT))
          .setSnippetFileName(id + "/" + DEFAULT_SNIPPET_FILE)
          .build();

    } else {
      throw new NotImplementedException("MainDiscoveryProviderFactory: invalid id \"" + id + "\"");
    }
  }

  @Override
  public DiscoveryProvider create(
      Service service, ApiaryConfig apiaryConfig, JsonNode sampleConfigOverrides, String id) {
    return defaultCreate(service, apiaryConfig, sampleConfigOverrides, id);
  }
}
