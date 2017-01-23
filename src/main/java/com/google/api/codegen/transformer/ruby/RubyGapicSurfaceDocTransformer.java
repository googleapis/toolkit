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

import com.google.api.codegen.ProtoFileView;
import com.google.api.codegen.config.ApiConfig;
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
import com.google.api.tools.framework.model.Model;
import com.google.api.tools.framework.model.ProtoFile;
import com.google.common.collect.ImmutableList;
import java.util.List;

public class RubyGapicSurfaceDocTransformer implements ModelToViewTransformer {
  private static final String XAPI_TEMPLATE_FILENAME = "ruby/message.snip";

  private final GapicCodePathMapper pathMapper;
  private final FileHeaderTransformer fileHeaderTransformer = new FileHeaderTransformer(null);
  private final GrpcElementDocTransformer elementDocTransformer = new GrpcElementDocTransformer();

  public RubyGapicSurfaceDocTransformer(GapicCodePathMapper pathMapper) {
    this.pathMapper = pathMapper;
  }

  @Override
  public List<String> getTemplateFileNames() {
    return ImmutableList.of(XAPI_TEMPLATE_FILENAME);
  }

  @Override
  public List<ViewModel> transform(Model model, ApiConfig apiConfig) {
    ImmutableList.Builder<ViewModel> surfaceDocs = ImmutableList.builder();
    for (ProtoFile file : new ProtoFileView().getElementIterable(model)) {
      surfaceDocs.add(createView(file, apiConfig));
    }
    return surfaceDocs.build();
  }

  private ViewModel createView(ProtoFile file, ApiConfig apiConfig) {
    String subPath = pathMapper.getOutputPath(file, apiConfig);
    String baseFilename = file.getSimpleName().replace(".proto", "");
    ModelTypeTable typeTable =
        new ModelTypeTable(
            new RubyTypeTable(apiConfig.getPackageName()),
            new RubyModelTypeNameConverter(apiConfig.getPackageName()));
    SurfaceNamer namer = new RubySurfaceNamer(typeTable.getFullNameFor(file));
    GrpcDocView.Builder doc = GrpcDocView.newBuilder();
    doc.templateFileName(XAPI_TEMPLATE_FILENAME);
    doc.outputPath(subPath + "/doc/" + baseFilename + ".rb");
    doc.fileHeader(
        fileHeaderTransformer.generateFileHeader(
            apiConfig, ImportSectionView.newBuilder().build(), namer));
    doc.children(elementDocTransformer.generateElementDocs(typeTable, namer, file));
    return doc.build();
  }
}
