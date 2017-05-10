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
import com.google.api.codegen.config.GapicProductConfig;
import com.google.api.codegen.config.PackageMetadataConfig;
import com.google.api.codegen.transformer.FileHeaderTransformer;
import com.google.api.codegen.transformer.GapicInterfaceContext;
import com.google.api.codegen.transformer.ModelToViewTransformer;
import com.google.api.codegen.transformer.ModelTypeTable;
import com.google.api.codegen.transformer.PackageMetadataTransformer;
import com.google.api.codegen.transformer.SurfaceNamer;
import com.google.api.codegen.util.ruby.RubyTypeTable;
import com.google.api.codegen.viewmodel.ImportSectionView;
import com.google.api.codegen.viewmodel.ViewModel;
import com.google.api.tools.framework.model.Interface;
import com.google.api.tools.framework.model.Model;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import java.util.List;

/** Responsible for producing package metadata related views for Ruby */
public class RubyPackageMetadataTransformer implements ModelToViewTransformer {
  private static final String GEMSPEC_FILE = "ruby/gemspec.snip";
  private static final List<String> TOP_LEVEL_FILES =
      ImmutableList.of(
          "ruby/Gemfile.snip", "ruby/Rakefile.snip", "ruby/README.md.snip", "LICENSE.snip");
  private static final List<String> TOP_LEVEL_DOT_FILES =
      ImmutableList.of("ruby/gitignore.snip", "ruby/rubocop.yml.snip", "ruby/yardopts.snip");
  private final FileHeaderTransformer fileHeaderTransformer =
      new FileHeaderTransformer(new RubyImportSectionTransformer());
  private final PackageMetadataConfig packageConfig;
  private final PackageMetadataTransformer metadataTransformer = new PackageMetadataTransformer();

  private static final String RUBY_PREFIX = "ruby/";

  public RubyPackageMetadataTransformer(PackageMetadataConfig packageConfig) {
    this.packageConfig = packageConfig;
  }

  @Override
  public List<String> getTemplateFileNames() {
    return ImmutableList.<String>builder()
        .add(GEMSPEC_FILE)
        .addAll(TOP_LEVEL_FILES)
        .addAll(TOP_LEVEL_DOT_FILES)
        .build();
  }

  @Override
  public List<ViewModel> transform(Model model, GapicProductConfig productConfig) {
    RubyPackageMetadataNamer namer = new RubyPackageMetadataNamer(productConfig.getPackageName());
    return ImmutableList.<ViewModel>builder()
        .add(generateGemspecView(model, namer))
        .addAll(generateMetadataViews(model, productConfig, namer, TOP_LEVEL_FILES))
        .addAll(generateMetadataViews(model, productConfig, namer, TOP_LEVEL_DOT_FILES, "."))
        .build();
  }

  private ViewModel generateGemspecView(Model model, RubyPackageMetadataNamer namer) {
    return metadataTransformer
        .generateMetadataView(
            packageConfig, model, GEMSPEC_FILE, namer.getOutputFileName(), TargetLanguage.RUBY)
        .identifier(namer.getMetadataIdentifier())
        .build();
  }

  private List<ViewModel> generateMetadataViews(
      Model model,
      GapicProductConfig productConfig,
      RubyPackageMetadataNamer namer,
      List<String> snippets) {
    return generateMetadataViews(model, productConfig, namer, snippets, null);
  }

  private List<ViewModel> generateMetadataViews(
      Model model,
      GapicProductConfig productConfig,
      RubyPackageMetadataNamer namer,
      List<String> snippets,
      String filePrefix) {
    ImmutableList.Builder<ViewModel> views = ImmutableList.builder();
    for (String template : snippets) {
      views.add(generateMetadataView(model, productConfig, template, namer, filePrefix));
    }
    return views.build();
  }

  private ViewModel generateMetadataView(
      Model model,
      GapicProductConfig productConfig,
      String template,
      RubyPackageMetadataNamer namer,
      String filePrefix) {
    String noLeadingRubyDir =
        template.startsWith(RUBY_PREFIX) ? template.substring(RUBY_PREFIX.length()) : template;
    if (!Strings.isNullOrEmpty(filePrefix)) {
      noLeadingRubyDir = filePrefix + noLeadingRubyDir;
    }
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

    SurfaceNamer surfaceNamer = new RubySurfaceNamer(productConfig.getPackageName());

    return metadataTransformer
        .generateMetadataView(packageConfig, model, template, outputPath, TargetLanguage.RUBY)
        .identifier(namer.getMetadataIdentifier())
        .fileHeader(
            fileHeaderTransformer.generateFileHeader(
                productConfig, ImportSectionView.newBuilder().build(), surfaceNamer))
        .hasSmokeTests(hasSmokeTests)
        .versionPath(surfaceNamer.getVersionIndexFileImportName())
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
