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
package com.google.api.codegen.transformer.ruby;

import com.google.api.codegen.InterfaceView;
import com.google.api.codegen.TargetLanguage;
import com.google.api.codegen.config.FlatteningConfig;
import com.google.api.codegen.config.GapicProductConfig;
import com.google.api.codegen.config.PackageMetadataConfig;
import com.google.api.codegen.transformer.DynamicLangApiMethodTransformer;
import com.google.api.codegen.transformer.FileHeaderTransformer;
import com.google.api.codegen.transformer.GapicInterfaceContext;
import com.google.api.codegen.transformer.GapicMethodContext;
import com.google.api.codegen.transformer.InitCodeTransformer;
import com.google.api.codegen.transformer.ModelToViewTransformer;
import com.google.api.codegen.transformer.ModelTypeTable;
import com.google.api.codegen.transformer.PackageMetadataTransformer;
import com.google.api.codegen.transformer.TestCaseTransformer;
import com.google.api.codegen.util.ruby.RubyTypeTable;
import com.google.api.codegen.util.testing.StandardValueProducer;
import com.google.api.codegen.util.testing.ValueProducer;
import com.google.api.codegen.viewmodel.ApiMethodView;
import com.google.api.codegen.viewmodel.ImportSectionView;
import com.google.api.codegen.viewmodel.InitCodeView;
import com.google.api.codegen.viewmodel.OptionalArrayMethodView;
import com.google.api.codegen.viewmodel.ViewModel;
import com.google.api.tools.framework.model.Interface;
import com.google.api.tools.framework.model.Method;
import com.google.api.tools.framework.model.Model;
import com.google.common.collect.ImmutableList;
import java.util.List;

/** Responsible for producing package metadata related views for Ruby */
public class RubyPackageMetadataTransformer implements ModelToViewTransformer {
  private static final String GEMSPEC_FILE = "ruby/gemspec.snip";
  private static final String README_FILE = "ruby/README.md.snip";
  private static final String README_OUTPUT_FILE = "README.md";
  private static final List<String> TOP_LEVEL_FILES =
      ImmutableList.of("ruby/Gemfile.snip", "ruby/Rakefile.snip", "LICENSE.snip");
  private final FileHeaderTransformer fileHeaderTransformer =
      new FileHeaderTransformer(new RubyImportSectionTransformer());
  private final PackageMetadataConfig packageConfig;
  private final PackageMetadataTransformer metadataTransformer = new PackageMetadataTransformer();
  private final ValueProducer valueProducer = new StandardValueProducer();
  private final TestCaseTransformer testCaseTransformer = new TestCaseTransformer(valueProducer);

  private static final String RUBY_PREFIX = "ruby/";

  public RubyPackageMetadataTransformer(PackageMetadataConfig packageConfig) {
    this.packageConfig = packageConfig;
  }

  @Override
  public List<String> getTemplateFileNames() {
    return ImmutableList.<String>builder()
        .add(GEMSPEC_FILE)
        .add(README_FILE)
        .addAll(TOP_LEVEL_FILES)
        .build();
  }

  @Override
  public List<ViewModel> transform(Model model, GapicProductConfig productConfig) {
    RubyPackageMetadataNamer namer = new RubyPackageMetadataNamer(productConfig.getPackageName());
    return ImmutableList.<ViewModel>builder()
        .add(generateGemspecView(model, namer))
        .add(generateReadmeView(model, productConfig, namer))
        .addAll(generateMetadataViews(model, productConfig, namer))
        .build();
  }

  private ViewModel generateGemspecView(Model model, RubyPackageMetadataNamer namer) {
    return metadataTransformer
        .generateMetadataView(
            packageConfig, model, GEMSPEC_FILE, namer.getOutputFileName(), TargetLanguage.RUBY)
        .identifier(namer.getMetadataIdentifier())
        .build();
  }

  private ViewModel generateReadmeView(
      Model model, GapicProductConfig productConfig, RubyPackageMetadataNamer namer) {
    List<ApiMethodView> exampleMethods = generateExampleMethods(model, productConfig);
    return metadataTransformer
        .generateMetadataView(
            packageConfig, model, README_FILE, README_OUTPUT_FILE, TargetLanguage.RUBY)
        .identifier(namer.getMetadataIdentifier())
        .fileHeader(
            fileHeaderTransformer.generateFileHeader(
                productConfig,
                ImportSectionView.newBuilder().build(),
                new RubySurfaceNamer(productConfig.getPackageName())))
        .developmentStatus(
            namer.getReleaseAnnotation(packageConfig.releaseLevel(TargetLanguage.RUBY)))
        .exampleMethods(exampleMethods)
        .targetLanguage("Ruby")
        .metapackageIdentifier("google-cloud-ruby")
        .build();
  }

  // Generates methods used as examples for the README.md file.
  // Note: This is based on sample gen method calls. In the future, the example
  // methods may be configured separately.
  private List<ApiMethodView> generateExampleMethods(
      Model model, GapicProductConfig productConfig) {
    ImmutableList.Builder<ApiMethodView> exampleMethods = ImmutableList.builder();
    for (Interface apiInterface : new InterfaceView().getElementIterable(model)) {
      GapicInterfaceContext context = createContext(apiInterface, productConfig);
      if (context.getInterfaceConfig().getSmokeTestConfig() != null) {
        Method method = context.getInterfaceConfig().getSmokeTestConfig().getMethod();
        FlatteningConfig flatteningGroup =
            testCaseTransformer.getSmokeTestFlatteningGroup(
                context.getMethodConfig(method), context.getInterfaceConfig().getSmokeTestConfig());
        GapicMethodContext flattenedMethodContext =
            context.asFlattenedMethodContext(method, flatteningGroup);
        exampleMethods.add(createExampleApiMethodView(flattenedMethodContext));
      }
    }
    return exampleMethods.build();
  }

  private OptionalArrayMethodView createExampleApiMethodView(GapicMethodContext context) {
    OptionalArrayMethodView initialApiMethodView =
        new DynamicLangApiMethodTransformer(new RubyApiMethodParamTransformer())
            .generateMethod(context);

    OptionalArrayMethodView.Builder apiMethodView = initialApiMethodView.toBuilder();

    InitCodeTransformer initCodeTransformer = new InitCodeTransformer();
    InitCodeView initCodeView =
        initCodeTransformer.generateInitCode(
            context, testCaseTransformer.createSmokeTestInitContext(context));
    apiMethodView.initCode(initCodeView);

    return apiMethodView.build();
  }

  private List<ViewModel> generateMetadataViews(
      Model model, GapicProductConfig productConfig, RubyPackageMetadataNamer namer) {
    ImmutableList.Builder<ViewModel> views = ImmutableList.builder();
    for (String template : TOP_LEVEL_FILES) {
      views.add(generateMetadataView(model, productConfig, template, namer));
    }
    return views.build();
  }

  private ViewModel generateMetadataView(
      Model model,
      GapicProductConfig productConfig,
      String template,
      RubyPackageMetadataNamer namer) {
    String noLeadingRubyDir =
        template.startsWith(RUBY_PREFIX) ? template.substring(RUBY_PREFIX.length()) : template;
    int extensionIndex = noLeadingRubyDir.lastIndexOf(".");
    String outputPath = noLeadingRubyDir.substring(0, extensionIndex);

    boolean hasSmokeTests = false;
    for (Interface apiInterface : new InterfaceView().getElementIterable(model)) {
      GapicInterfaceContext context = createContext(apiInterface, productConfig);
      if (context.getInterfaceConfig().getSmokeTestConfig() != null) {
        hasSmokeTests = true;
        break;
      }
    }

    return metadataTransformer
        .generateMetadataView(packageConfig, model, template, outputPath, TargetLanguage.RUBY)
        .identifier(namer.getMetadataIdentifier())
        .fileHeader(
            fileHeaderTransformer.generateFileHeader(
                productConfig,
                ImportSectionView.newBuilder().build(),
                new RubySurfaceNamer(productConfig.getPackageName())))
        .hasSmokeTests(hasSmokeTests)
        .build();
  }

  private GapicInterfaceContext createContext(
      Interface apiInterface, GapicProductConfig productConfig) {
    return GapicInterfaceContext.create(
        apiInterface,
        productConfig,
        new ModelTypeTable(
            new RubyTypeTable(productConfig.getPackageName()),
            new RubyModelTypeNameConverter(productConfig.getPackageName())),
        new RubySurfaceNamer(productConfig.getPackageName()),
        new RubyFeatureConfig());
  }
}
