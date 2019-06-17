/* Copyright 2016 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.api.codegen.packagegen;

import com.google.api.codegen.common.CodeGenerator;
import com.google.api.codegen.common.GeneratedResult;
import com.google.api.codegen.common.TargetLanguage;
import com.google.api.codegen.config.ApiDefaultsConfig;
import com.google.api.codegen.config.DependenciesConfig;
import com.google.api.codegen.config.PackageMetadataConfig;
import com.google.api.codegen.config.PackagingConfig;
import com.google.api.tools.framework.model.Diag;
import com.google.api.tools.framework.model.Model;
import com.google.api.tools.framework.model.stages.Merged;
import com.google.api.tools.framework.snippet.Doc;
import com.google.api.tools.framework.tools.ToolDriverBase;
import com.google.api.tools.framework.tools.ToolOptions;
import com.google.api.tools.framework.tools.ToolOptions.Option;
import com.google.api.tools.framework.tools.ToolUtil;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.io.IOException;
import java.net.URL;
import java.util.Map;

/** ToolDriver for gRPC meta-data generation. */
public class PackageGeneratorApp extends ToolDriverBase {
  public static final Option<String> LANGUAGE =
      ToolOptions.createOption(String.class, "language", "The target language.", "");
  public static final Option<String> OUTPUT_DIR =
      ToolOptions.createOption(
          String.class, "output_file", "The name of the output folder to put generated code.", "");
  public static final Option<String> INPUT_DIR =
      ToolOptions.createOption(
          String.class,
          "input_file",
          "The name of the folder containing the gRPC package to generate metadata for.",
          "");
  public static final Option<String> PACKAGE_CONFIG2_FILE =
      ToolOptions.createOption(
          String.class,
          "package_config2",
          "The packaging configuration. This is required if --proto_package "
              + "option is not given.",
          "");
  public static final Option<String> PROTO_PACKAGE =
      ToolOptions.createOption(
          String.class,
          "proto_package",
          "The proto package designating the files actually intended for output.\n"
              + "This option is required if the package_yaml2 file is not given.",
          "");
  public static final Option<PackagingArtifactType> ARTIFACT_TYPE =
      ToolOptions.createOption(
          PackagingArtifactType.class,
          "artifact_type",
          "The artifacts to be generated by the metadata generator.",
          null);

  private URL dependenciesYamlUrl;

  public PackageGeneratorApp(ToolOptions options) {
    super(options);
  }

  PackageGeneratorApp(ToolOptions options, URL dependenciesYamlUrl) {
    super(options);
    this.dependenciesYamlUrl = dependenciesYamlUrl;
  }

  @Override
  protected void process() throws Exception {
    model.establishStage(Merged.KEY);

    if (model.getDiagReporter().getDiagCollector().getErrorCount() > 0) {
      for (Diag diag : model.getDiagReporter().getDiagCollector().getDiags()) {
        System.err.println(diag.toString());
      }
      return;
    }
    Map<String, GeneratedResult<Doc>> results = generate(model);
    ToolUtil.writeFiles(GeneratedResult.extractBodies(results), options.get(OUTPUT_DIR));
  }

  protected Map<String, GeneratedResult<Doc>> generate(Model model) throws IOException {
    TargetLanguage language = TargetLanguage.fromString(options.get(LANGUAGE));

    PackageMetadataConfig config = null;

    ApiDefaultsConfig apiDefaultsConfig = ApiDefaultsConfig.load();
    DependenciesConfig dependenciesConfig;
    if (dependenciesYamlUrl != null) {
      dependenciesConfig = DependenciesConfig.loadFromURL(dependenciesYamlUrl);
    } else {
      dependenciesConfig = DependenciesConfig.load();
    }

    PackagingConfig packagingConfig = null;
    if (!Strings.isNullOrEmpty(options.get(PACKAGE_CONFIG2_FILE))) {
      packagingConfig = PackagingConfig.load(options.get(PACKAGE_CONFIG2_FILE));
    } else {
      // TODO(andrealin): Get PackageMetadataConfig from proto annotations.
    }

    config =
        PackageMetadataConfig.createFromPackaging(
            apiDefaultsConfig, dependenciesConfig, packagingConfig);

    Preconditions.checkNotNull(config);

    PackagingArtifactType artifactType = options.get(PackageGeneratorApp.ARTIFACT_TYPE);
    if (artifactType == null) {
      artifactType = config.artifactType();
    }
    CodeGenerator<Doc> generator =
        PackageGeneratorFactory.create(language, artifactType, options, model, config);

    return generator.generate();
  }
}
