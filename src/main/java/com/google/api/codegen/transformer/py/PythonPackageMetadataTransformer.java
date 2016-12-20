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
package com.google.api.codegen.transformer.py;

import com.google.api.codegen.SnippetSetRunner;
import com.google.api.codegen.TargetLanguage;
import com.google.api.codegen.config.ApiConfig;
import com.google.api.codegen.config.PackageMetadataConfig;
import com.google.api.codegen.transformer.ModelToViewTransformer;
import com.google.api.codegen.viewmodel.SimpleViewModel;
import com.google.api.codegen.viewmodel.ViewModel;
import com.google.api.codegen.viewmodel.metadata.PackageMetadataView;
import com.google.api.tools.framework.model.Model;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/** Responsible for producing package metadata related views for Python */
public class PythonPackageMetadataTransformer implements ModelToViewTransformer {

  PackageMetadataConfig packageConfig;

  public PythonPackageMetadataTransformer(PackageMetadataConfig packageConfig) {
    this.packageConfig = packageConfig;
  }

  @Override
  public List<ViewModel> transform(final Model model, final ApiConfig apiConfig) {
    String version = packageConfig.apiVersion();
    List<ViewModel> metadata =
        computeInitFiles(computePackages(apiConfig.getPackageName()), version);
    for (String templateFileName : getTopLevelTemplateFileNames()) {
      metadata.add(generateMetadataView(model, apiConfig, templateFileName));
    }
    return metadata;
  }

  @Override
  public List<String> getTemplateFileNames() {
    List<String> templates = new ArrayList<>();
    templates.addAll(getTopLevelTemplateFileNames());
    templates.addAll(getInitTemplateFileNames());
    return templates;
  }

  public List<String> getTopLevelTemplateFileNames() {
    return Lists.newArrayList(
        "LICENSE.snip",
        "py/MANIFEST.in.snip",
        "py/PUBLISHING.rst.snip",
        "py/setup.py.snip",
        "py/requirements.txt.snip",
        "py/README.rst.snip",
        "py/tox.ini.snip");
  }

  public List<String> getInitTemplateFileNames() {
    return Lists.newArrayList("py/__init__.py.snip", "py/namespace__init__.py.snip");
  }

  private ViewModel generateMetadataView(Model model, ApiConfig apiConfig, String template) {
    int extensionIndex = template.lastIndexOf(".");
    String outputPath = template.substring(0, extensionIndex);

    return PackageMetadataView.newBuilder()
        .templateFileName(template)
        .outputPath(outputPath)
        .identifier(apiConfig.getDomainLayerLocation())
        .packageVersionBound(packageConfig.packageVersionBound(TargetLanguage.PYTHON))
        .protoPath(packageConfig.protoPath())
        .shortName(packageConfig.shortName())
        .gaxVersionBound(packageConfig.gaxVersionBound(TargetLanguage.PYTHON))
        .protoVersionBound(packageConfig.protoVersionBound(TargetLanguage.PYTHON))
        .commonProtosVersionBound(packageConfig.commonProtosVersionBound(TargetLanguage.PYTHON))
        .packageName(packageConfig.packageName(TargetLanguage.PYTHON))
        .majorVersion(packageConfig.apiVersion())
        .author(packageConfig.author())
        .email(packageConfig.email())
        .homepage(packageConfig.homepage())
        .licenseName(packageConfig.licenseName())
        .fullName(model.getServiceConfig().getTitle())
        .serviceName(apiConfig.getPackageName())
        .namespacePackages(
            computeNamespacePackages(apiConfig.getPackageName(), packageConfig.apiVersion()))
        .hasMultipleServices(false)
        .build();
  }

  /**
   * Computes all Python packages present under the given package name. For example, for input
   * "foo.bar.baz", returns ["foo", "foo.bar", "foo.bar.baz"].
   */
  private List<String> computePackages(String packageName) {
    ArrayList<String> packages = new ArrayList<>();
    StringBuilder current = new StringBuilder();
    boolean first = true;
    for (String pkg : Splitter.on(".").split(packageName)) {
      if (!first) {
        current.append("." + pkg);
      } else {
        current.append(pkg);
        first = false;
      }
      packages.add(current.toString());
    }
    return packages;
  }

  private List<String> computeNamespacePackages(String packageName, final String apiVersion) {
    List<String> namespacePackages = new ArrayList<>();
    for (String subPackage : computePackages(packageName)) {
      if (isNamespacePackage(subPackage, apiVersion)) {
        namespacePackages.add(subPackage);
      }
    }
    return namespacePackages;
  }

  /** Set all packages to be namespace packages except for the version package (if present) */
  private boolean isNamespacePackage(String packageName, String apiVersion) {
    int lastDot = packageName.lastIndexOf(".");
    return lastDot < 0 || !packageName.substring(lastDot + 1).equals(apiVersion);
  }

  /**
   * Determines which __init__.py files to generate given a list of Python packages. Each Python
   * package corresponds to exactly one __init__.py file, although the contents of that file depend
   * on whether the package is a namespace package.
   */
  private List<ViewModel> computeInitFiles(List<String> packages, final String apiVersion) {
    List<ViewModel> initFiles = new ArrayList<>();
    for (String packageName : packages) {
      final String template;
      if (isNamespacePackage(packageName, apiVersion)) {
        template = "py/namespace__init__.py.snip";
      } else {
        template = "py/__init__.py.snip";
      }
      String outputPath =
          Paths.get(packageName.replace(".", File.separator)).resolve("__init__.py").toString();
      initFiles.add(
          SimpleViewModel.create(SnippetSetRunner.SNIPPET_RESOURCE_ROOT, template, outputPath));
    }
    return initFiles;
  }
}
