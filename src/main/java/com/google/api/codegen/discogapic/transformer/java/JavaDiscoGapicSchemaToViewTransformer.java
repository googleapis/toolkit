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
package com.google.api.codegen.discogapic.transformer.java;

import com.google.api.codegen.config.GapicProductConfig;
import com.google.api.codegen.config.PackageMetadataConfig;
import com.google.api.codegen.discogapic.DiscoGapicInterfaceContext;
import com.google.api.codegen.discogapic.transformer.DocumentToViewTransformer;
import com.google.api.codegen.discovery.Document;
import com.google.api.codegen.discovery.Schema;
import com.google.api.codegen.gapic.GapicCodePathMapper;
import com.google.api.codegen.transformer.FileHeaderTransformer;
import com.google.api.codegen.transformer.SchemaTypeTable;
import com.google.api.codegen.transformer.StandardImportSectionTransformer;
import com.google.api.codegen.transformer.java.JavaFeatureConfig;
import com.google.api.codegen.transformer.java.JavaSchemaTypeNameConverter;
import com.google.api.codegen.transformer.java.JavaSurfaceNamer;
import com.google.api.codegen.util.Name;
import com.google.api.codegen.util.SymbolTable;
import com.google.api.codegen.util.java.JavaNameFormatter;
import com.google.api.codegen.util.java.JavaTypeTable;
import com.google.api.codegen.viewmodel.StaticLangApiMessageFileView;
import com.google.api.codegen.viewmodel.StaticLangApiMessageView;
import com.google.api.codegen.viewmodel.ViewModel;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/* Creates the ViewModel for a Discovery Doc Schema Java class. */
public class JavaDiscoGapicSchemaToViewTransformer implements DocumentToViewTransformer {
  private final GapicCodePathMapper pathMapper;
  private final PackageMetadataConfig packageConfig;
  private final StandardImportSectionTransformer importSectionTransformer =
      new StandardImportSectionTransformer();
  private final FileHeaderTransformer fileHeaderTransformer =
      new FileHeaderTransformer(importSectionTransformer);
  private final JavaNameFormatter nameFormatter = new JavaNameFormatter();

  private static final String XAPI_TEMPLATE_FILENAME = "java/main.snip";
  private static final String PACKAGE_INFO_TEMPLATE_FILENAME = "java/package-info.snip";
  private static final String SCHEMA_TEMPLATE_FILENAME = "java/message.snip";

  public JavaDiscoGapicSchemaToViewTransformer(
      GapicCodePathMapper pathMapper, PackageMetadataConfig packageMetadataConfig) {
    this.pathMapper = pathMapper;
    this.packageConfig = packageMetadataConfig;
    // TODO use packageMetadataConfig
  }

  public List<String> getTemplateFileNames() {
    return Arrays.asList(
        XAPI_TEMPLATE_FILENAME, PACKAGE_INFO_TEMPLATE_FILENAME, SCHEMA_TEMPLATE_FILENAME);
  }

  @Override
  public List<ViewModel> transform(Document document, GapicProductConfig productConfig) {
    List<ViewModel> surfaceSchemas = new ArrayList<>();
    JavaDiscoGapicNamer discoGapicNamer = new JavaDiscoGapicNamer();

    DiscoGapicInterfaceContext context =
        DiscoGapicInterfaceContext.create(
            document,
            productConfig,
            createTypeTable(productConfig.getPackageName()),
            discoGapicNamer,
            new JavaSurfaceNamer(productConfig.getPackageName()),
            JavaFeatureConfig.newBuilder().enableStringFormatFunctions(false).build());

    StaticLangApiMessageFileView apiFile;

    // TODO(andrealin): Remove this when imports are fixed.
    final List<Schema> schemas = new LinkedList<>(context.getDocument().schemas().values());
    Collections.sort(
        schemas,
        new Comparator<Schema>() {
          @Override
          public int compare(Schema o1, Schema o2) {
            return String.CASE_INSENSITIVE_ORDER.compare(o1.id(), o2.id());
          }
        });

    for (Schema schema : schemas) {
      apiFile = generateSchemaFile(context, schema);
      surfaceSchemas.add(apiFile);
    }
    return surfaceSchemas;
  }

  private SchemaTypeTable createTypeTable(String implicitPackageName) {
    return new SchemaTypeTable(
        new JavaTypeTable(implicitPackageName),
        new JavaSchemaTypeNameConverter(implicitPackageName, nameFormatter));
  }

  private StaticLangApiMessageFileView generateSchemaFile(
      DiscoGapicInterfaceContext context, Schema schema) {
    StaticLangApiMessageFileView.Builder apiFile = StaticLangApiMessageFileView.newBuilder();
    // Escape any schema's field names that are Java keywords.

    apiFile.templateFileName(SCHEMA_TEMPLATE_FILENAME);

    addApiImports(context);
    StaticLangApiMessageView messageView = generateSchemaClass(context, null, schema, null);
    apiFile.schema(messageView);

    String outputPath = pathMapper.getOutputPath(null, context.getProductConfig());
    apiFile.outputPath(outputPath + File.separator + messageView.typeName() + ".java");

    // must be done as the last step to catch all imports
    apiFile.fileHeader(fileHeaderTransformer.generateFileHeader(context));

    return apiFile.build();
  }

  private StaticLangApiMessageView generateSchemaClass(
      DiscoGapicInterfaceContext context, String key, Schema schema, String parentName) {

    // TODO(andrealin): Probably don't need to create a new SymbolTable for every method invocation.
    SymbolTable schemaSymbolTable = SymbolTable.fromSeed(JavaNameFormatter.RESERVED_IDENTIFIER_SET);

    StaticLangApiMessageView.Builder schemaView = StaticLangApiMessageView.newBuilder();

    String schemaId = schema.id().isEmpty() ? key : schema.id();
    String schemaName =
        nameFormatter.privateFieldName(Name.anyCamel(schemaSymbolTable.getNewSymbol(schemaId)));
    schemaView.name(schemaName);
    schemaView.defaultValue(schema.defaultValue());
    schemaView.description(schema.description());
    schemaView.typeName(
        context.getSchemaTypeTable().getAndSaveNicknameForElementType(key, schema, parentName));
    schemaView.innerTypeName(
        context.getSchemaTypeTable().getAndSaveNicknameForElementType(key, schema, parentName));
    schemaView.fieldGetFunction(context.getDiscoGapicNamer().getResourceGetterName(schemaName));
    schemaView.fieldSetFunction(context.getDiscoGapicNamer().getResourceSetterName(schemaName));

    // Generate a Schema view from each property.
    List<StaticLangApiMessageView> properties = new LinkedList<>();
    Map<String, Schema> schemaProperties = new HashMap<>();
    schemaProperties.putAll(schema.properties());
    if (schema.items() != null && schema.items().properties() != null) {
      schemaProperties.putAll(schema.items().properties());
    }
    for (Map.Entry<String, Schema> propertyEntry : schemaProperties.entrySet()) {
      properties.add(
          generateSchemaClass(
              context, propertyEntry.getKey(), propertyEntry.getValue(), schemaName));
    }
    Collections.sort(
        properties,
        new Comparator<StaticLangApiMessageView>() {
          @Override
          public int compare(StaticLangApiMessageView o1, StaticLangApiMessageView o2) {
            return String.CASE_INSENSITIVE_ORDER.compare(o1.name(), o2.name());
          };
        });
    schemaView.properties(properties);

    return schemaView.build();
  }

  private void addApiImports(DiscoGapicInterfaceContext context) {
    SchemaTypeTable typeTable = context.getSchemaTypeTable();
    typeTable.saveNicknameFor("com.google.api.core.BetaApi");
    typeTable.saveNicknameFor("java.io.Serializable");
    typeTable.saveNicknameFor("java.util.List");
    typeTable.saveNicknameFor("javax.annotation.Generated");
  }
}
