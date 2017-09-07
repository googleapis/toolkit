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
package com.google.api.codegen.transformer.ruby;

import com.google.api.codegen.InterfaceView;
import com.google.api.codegen.ProtoFileView;
import com.google.api.codegen.config.GapicInterfaceConfig;
import com.google.api.codegen.config.GapicProductConfig;
import com.google.api.codegen.config.PackageMetadataConfig;
import com.google.api.codegen.gapic.GapicCodePathMapper;
import com.google.api.codegen.transformer.FileHeaderTransformer;
import com.google.api.codegen.transformer.GrpcElementDocTransformer;
import com.google.api.codegen.transformer.ModelToViewTransformer;
import com.google.api.codegen.transformer.ModelTypeTable;
import com.google.api.codegen.transformer.SurfaceNamer;
import com.google.api.codegen.util.ruby.RubyTypeTable;
import com.google.api.codegen.viewmodel.GrpcDocView;
import com.google.api.codegen.viewmodel.ImportSectionView;
import com.google.api.codegen.viewmodel.ViewModel;
import com.google.api.codegen.viewmodel.metadata.ModuleView;
import com.google.api.codegen.viewmodel.metadata.SimpleModuleView;
import com.google.api.codegen.viewmodel.metadata.TocContentView;
import com.google.api.codegen.viewmodel.metadata.TocModuleView;
import com.google.api.tools.framework.model.Interface;
import com.google.api.tools.framework.model.Model;
import com.google.api.tools.framework.model.ProtoFile;
import com.google.common.collect.ImmutableList;
import java.util.List;

public class RubyGapicSurfaceDocTransformer implements ModelToViewTransformer {
  private static final int VERSION_MODULE_RINDEX = 1;
  private static final String DOC_TEMPLATE_FILENAME = "ruby/message.snip";

  private final GapicCodePathMapper pathMapper;
  private final PackageMetadataConfig packageConfig;
  private final FileHeaderTransformer fileHeaderTransformer = new FileHeaderTransformer(null);
  private final GrpcElementDocTransformer elementDocTransformer = new GrpcElementDocTransformer();

  public RubyGapicSurfaceDocTransformer(
      GapicCodePathMapper pathMapper, PackageMetadataConfig packageConfig) {
    this.pathMapper = pathMapper;
    this.packageConfig = packageConfig;
  }

  @Override
  public List<String> getTemplateFileNames() {
    return ImmutableList.of(DOC_TEMPLATE_FILENAME);
  }

  @Override
  public List<ViewModel> transform(Model model, GapicProductConfig productConfig) {
    ImmutableList.Builder<ViewModel> surfaceDocs = ImmutableList.builder();
    for (ProtoFile file : new ProtoFileView().getElementIterable(model)) {
      surfaceDocs.add(generateDoc(model, file, productConfig));
    }
    return surfaceDocs.build();
  }

  private ViewModel generateDoc(Model model, ProtoFile file, GapicProductConfig productConfig) {
    ModelTypeTable typeTable =
        new ModelTypeTable(
            new RubyTypeTable(productConfig.getPackageName()),
            new RubyModelTypeNameConverter(productConfig.getPackageName()));
    // Use file path for package name to get file-specific package instead of package for the API.
    SurfaceNamer namer = new RubySurfaceNamer(typeTable.getFullNameFor(file));
    String subPath = pathMapper.getOutputPath(file, productConfig);
    String baseFilename = namer.getProtoFileName(file);
    GrpcDocView.Builder doc = GrpcDocView.newBuilder();
    doc.templateFileName(DOC_TEMPLATE_FILENAME);
    doc.outputPath(subPath + "/doc/" + baseFilename);
    doc.fileHeader(
        fileHeaderTransformer.generateFileHeader(
            productConfig, ImportSectionView.newBuilder().build(), namer));
    doc.elementDocs(elementDocTransformer.generateElementDocs(typeTable, namer, file));
    doc.modules(generateModuleViews(model, productConfig, namer, file.isSource()));
    return doc.build();
  }

  private List<ModuleView> generateModuleViews(
      Model model, GapicProductConfig productConfig, SurfaceNamer namer, boolean hasToc) {
    List<String> apiModules = namer.getApiModules();
    int moduleCount = apiModules.size();
    ImmutableList.Builder<ModuleView> moduleViews = ImmutableList.builder();
    for (int i = 0; i < moduleCount; ++i) {
      String moduleName = apiModules.get(i);
      if (hasToc && i == moduleCount - VERSION_MODULE_RINDEX) {
        moduleViews.add(generateTocModuleView(model, productConfig, namer, moduleName));
      } else {
        moduleViews.add(SimpleModuleView.newBuilder().moduleName(moduleName).build());
      }
    }
    return moduleViews.build();
  }

  private TocModuleView generateTocModuleView(
      Model model, GapicProductConfig productConfig, SurfaceNamer namer, String moduleName) {
    RubyPackageMetadataTransformer metadataTransformer =
        new RubyPackageMetadataTransformer(packageConfig);
    RubyPackageMetadataNamer packageNamer =
        new RubyPackageMetadataNamer(productConfig.getPackageName());
    String version = packageConfig.apiVersion();
    ImmutableList.Builder<TocContentView> tocContents = ImmutableList.builder();
    for (Interface apiInterface : new InterfaceView().getElementIterable(model)) {
      GapicInterfaceConfig interfaceConfig = productConfig.getInterfaceConfig(apiInterface);
      tocContents.add(
          metadataTransformer.generateTocContent(
              model, packageNamer, version, namer.getApiWrapperClassName(interfaceConfig)));
    }

    tocContents.add(
        metadataTransformer.generateDataTypeTocContent(
            productConfig.getPackageName(), packageNamer, version));

    return TocModuleView.newBuilder()
        .moduleName(moduleName)
        .fullName(model.getServiceConfig().getTitle())
        .contents(tocContents.build())
        .build();
  }
}
