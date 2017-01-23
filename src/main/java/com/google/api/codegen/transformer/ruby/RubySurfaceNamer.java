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

import com.google.api.codegen.ServiceMessages;
import com.google.api.codegen.config.MethodConfig;
import com.google.api.codegen.config.SingleResourceNameConfig;
import com.google.api.codegen.transformer.ModelTypeFormatterImpl;
import com.google.api.codegen.transformer.ModelTypeTable;
import com.google.api.codegen.transformer.SurfaceNamer;
import com.google.api.codegen.transformer.SurfaceTransformerContext;
import com.google.api.codegen.transformer.Synchronicity;
import com.google.api.codegen.util.Name;
import com.google.api.codegen.util.NamePath;
import com.google.api.codegen.util.TypeName;
import com.google.api.codegen.util.ruby.RubyCommentFixer;
import com.google.api.codegen.util.ruby.RubyNameFormatter;
import com.google.api.codegen.util.ruby.RubyTypeTable;
import com.google.api.tools.framework.model.Interface;
import com.google.api.tools.framework.model.Method;
import com.google.api.tools.framework.model.TypeRef;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;

/** The SurfaceNamer for Ruby. */
public class RubySurfaceNamer extends SurfaceNamer {
  public RubySurfaceNamer(String packageName) {
    super(
        new RubyNameFormatter(),
        new ModelTypeFormatterImpl(new RubyModelTypeNameConverter(packageName)),
        new RubyTypeTable(packageName),
        new RubyCommentFixer(),
        packageName);
  }

  @Override
  /** The name of the class that implements snippets for a particular proto interface. */
  public String getApiSnippetsClassName(Interface interfaze) {
    return publicClassName(Name.upperCamel(interfaze.getSimpleName(), "ClientSnippets"));
  }

  /** The function name to set a field having the given type and name. */
  @Override
  public String getFieldSetFunctionName(TypeRef type, Name identifier) {
    return publicMethodName(identifier);
  }

  @Override
  public String getPathTemplateName(
      Interface service, SingleResourceNameConfig resourceNameConfig) {
    return Name.from(resourceNameConfig.getEntityName(), "path", "template").toUpperUnderscore();
  }

  /** The function name to format the entity for the given collection. */
  @Override
  public String getFormatFunctionName(
      Interface service, SingleResourceNameConfig resourceNameConfig) {
    return staticFunctionName(Name.from(resourceNameConfig.getEntityName(), "path"));
  }

  @Override
  public String getParseFunctionName(String var, SingleResourceNameConfig resourceNameConfig) {
    return staticFunctionName(
        Name.from("match", var, "from", resourceNameConfig.getEntityName(), "name"));
  }

  @Override
  public String getClientConfigPath(Interface service) {
    return Name.upperCamel(service.getSimpleName()).join("client_config").toLowerUnderscore()
        + ".json";
  }

  /**
   * The type name of the Grpc client class. This needs to match what Grpc generates for the
   * particular language.
   */
  @Override
  public String getGrpcClientTypeName(Interface service) {
    return getModelTypeFormatter().getFullNameFor(service);
  }

  @Override
  public List<String> getThrowsDocLines() {
    return ImmutableList.of("@raise [Google::Gax::GaxError] if the RPC is aborted.");
  }

  @Override
  public List<String> getReturnDocLines(
      SurfaceTransformerContext context, MethodConfig methodConfig, Synchronicity synchronicity) {
    Method method = methodConfig.getMethod();
    if (method.getResponseStreaming()) {
      String classInfo = getModelTypeFormatter().getFullNameForElementType(method.getOutputType());
      return ImmutableList.of("An enumerable of " + classInfo + " instances.", "");
    }

    if (methodConfig.isPageStreaming()) {
      TypeRef resourceType = methodConfig.getPageStreaming().getResourcesField().getType();
      String resourceTypeName = getModelTypeFormatter().getFullNameForElementType(resourceType);
      return ImmutableList.of(
          "An enumerable of " + resourceTypeName + " instances.",
          "See Google::Gax::PagedEnumerable documentation for other",
          "operations such as per-page iteration or access to the response",
          "object.");
    }

    return ImmutableList.<String>of();
  }

  @Override
  public String getRequestTypeName(ModelTypeTable typeTable, TypeRef type) {
    return typeTable.getFullNameFor(type);
  }

  @Override
  public String getParamTypeName(ModelTypeTable typeTable, TypeRef type) {
    if (type.isMap()) {
      String keyTypeName = typeTable.getFullNameForElementType(type.getMapKeyField().getType());
      String valueTypeName = typeTable.getFullNameForElementType(type.getMapValueField().getType());
      return new TypeName(
              typeTable.getFullNameFor(type),
              typeTable.getNicknameFor(type),
              "%s{%i => %i}",
              new TypeName(keyTypeName),
              new TypeName(valueTypeName))
          .getFullName();
    }

    if (type.isRepeated()) {
      String elementTypeName = typeTable.getFullNameForElementType(type);
      return new TypeName(
              typeTable.getFullNameFor(type),
              typeTable.getNicknameFor(type),
              "%s<%i>",
              new TypeName(elementTypeName))
          .getFullName();
    }

    return typeTable.getFullNameForElementType(type);
  }

  @Override
  public String getDynamicLangReturnTypeName(Method method, MethodConfig methodConfig) {
    if (new ServiceMessages().isEmptyType(method.getOutputType())) {
      return "";
    }

    String classInfo = getModelTypeFormatter().getFullNameForElementType(method.getOutputType());
    if (method.getResponseStreaming()) {
      return "Enumerable<" + classInfo + ">";
    }

    if (methodConfig.isPageStreaming()) {
      TypeRef resourceType = methodConfig.getPageStreaming().getResourcesField().getType();
      String resourceTypeName = getModelTypeFormatter().getFullNameForElementType(resourceType);
      return "Google::Gax::PagedEnumerable<" + resourceTypeName + ">";
    }

    if (methodConfig.isLongRunningOperation()) {
      return "Google::Gax::Operation";
    }

    return classInfo;
  }

  @Override
  public String getLongRunningOperationTypeName(ModelTypeTable typeTable, TypeRef type) {
    return typeTable.getFullNameFor(type);
  }

  @Override
  public String getFullyQualifiedStubType(Interface service) {
    NamePath namePath =
        getTypeNameConverter().getNamePath(getModelTypeFormatter().getFullNameFor(service));
    return qualifiedName(namePath.append("Stub"));
  }

  /** The file name for an API service. */
  @Override
  public String getServiceFileName(Interface service) {
    String[] names = getPackageName().split("::");
    List<String> newNames = new ArrayList<>();
    for (String name : names) {
      newNames.add(packageFilePathPiece(Name.upperCamel(name)));
    }
    newNames.add(classFileNameBase(Name.upperCamel(getApiWrapperClassName(service))));
    return Joiner.on("/").join(newNames.toArray());
  }

  @Override
  public String getFullyQualifiedApiWrapperClassName(Interface service) {
    return getPackageName() + "::" + getApiWrapperClassName(service);
  }

  @Override
  public String getServiceFileImportFromService(Interface service) {
    return service.getFile().getSimpleName().replace(".proto", "_services_pb");
  }

  @Override
  public String getProtoFileImportFromService(Interface service) {
    return service.getFile().getSimpleName().replace(".proto", "_pb");
  }
}
