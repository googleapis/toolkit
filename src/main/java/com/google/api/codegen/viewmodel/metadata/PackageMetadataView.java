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
package com.google.api.codegen.viewmodel.metadata;

import com.google.api.codegen.SnippetSetRunner;
import com.google.api.codegen.config.VersionBound;
import com.google.api.codegen.grpcmetadatagen.GenerationLayer;
import com.google.api.codegen.grpcmetadatagen.PackageType;
import com.google.api.codegen.viewmodel.ApiMethodView;
import com.google.api.codegen.viewmodel.FileHeaderView;
import com.google.api.codegen.viewmodel.ViewModel;
import com.google.auto.value.AutoValue;
import java.util.List;
import javax.annotation.Nullable;

@AutoValue
public abstract class PackageMetadataView implements ViewModel {

  @Override
  public String resourceRoot() {
    return SnippetSetRunner.SNIPPET_RESOURCE_ROOT;
  }

  @Override
  public abstract String templateFileName();

  @Override
  public abstract String outputPath();

  @Nullable
  public abstract PackageType packageType();

  @Nullable
  public abstract GenerationLayer generationLayer();

  @Nullable
  public abstract String gapicConfigName();

  @Nullable
  public abstract String identifier();

  @Nullable
  public abstract String apiSummary();

  @Nullable
  public abstract VersionBound packageVersionBound();

  @Nullable
  public abstract VersionBound gaxVersionBound();

  @Nullable
  public abstract VersionBound grpcVersionBound();

  @Nullable
  public abstract VersionBound protoVersionBound();

  @Nullable
  public abstract VersionBound apiCommonVersionBound();

  @Nullable
  public abstract List<PackageDependencyView> protoPackageDependencies();

  @Nullable
  public abstract List<PackageDependencyView> protoPackageTestDependencies();

  @Nullable
  public abstract VersionBound authVersionBound();

  @Nullable
  public abstract String serviceName();

  public abstract String fullName();

  public abstract String shortName();

  public abstract String discoveryApiName();

  public abstract String protoPackageName();

  @Nullable
  public abstract String grpcPackageName();

  public abstract String gapicPackageName();

  public abstract String majorVersion();

  public abstract String protoPath();

  @Nullable
  public abstract String versionPath();

  public abstract String author();

  public abstract String email();

  public abstract String homepage();

  public abstract String licenseName();

  @Nullable
  public abstract String developmentStatus();

  @Nullable
  public abstract String developmentStatusTitle();

  public abstract boolean hasMultipleServices();

  public abstract boolean hasSmokeTests();

  @Nullable
  public abstract List<ApiMethodView> exampleMethods();

  // TODO(landrito) Currently only Ruby supports using fileHeaderView. Switch all metadata gen to
  // use this field.
  @Nullable
  public abstract FileHeaderView fileHeader();

  // Python-specific configuration
  @Nullable
  public abstract List<String> namespacePackages();

  @Nullable
  public abstract List<String> apiModules();

  @Nullable
  public abstract List<String> typeModules();

  @Nullable
  public abstract String targetLanguage();

  @Nullable
  public abstract String mainReadmeLink();

  @Nullable
  public abstract String authDocumentationLink();

  @Nullable
  public abstract String libraryDocumentationLink();

  @Nullable
  public abstract String versioningDocumentationLink();

  public static Builder newBuilder() {
    return new AutoValue_PackageMetadataView.Builder().hasSmokeTests(false);
  }

  @AutoValue.Builder
  public abstract static class Builder {
    public abstract Builder outputPath(String val);

    public abstract Builder templateFileName(String val);

    public abstract Builder identifier(String val);

    public abstract Builder gapicConfigName(String val);

    public abstract Builder packageType(PackageType val);

    public abstract Builder generationLayer(GenerationLayer val);

    public abstract Builder packageVersionBound(VersionBound val);

    public abstract Builder gaxVersionBound(VersionBound val);

    public abstract Builder grpcVersionBound(VersionBound val);

    public abstract Builder protoVersionBound(VersionBound val);

    public abstract Builder apiCommonVersionBound(VersionBound val);

    public abstract Builder protoPackageDependencies(List<PackageDependencyView> val);

    @Nullable
    public abstract Builder protoPackageTestDependencies(List<PackageDependencyView> val);

    public abstract Builder authVersionBound(VersionBound val);

    public abstract Builder serviceName(String val);

    /** The descriptive summary of the api. */
    public abstract Builder apiSummary(String val);

    /** The full name of the API, including branding. E.g., "Stackdriver Logging". */
    public abstract Builder fullName(String val);

    /** A single-word short name of the API. E.g., "logging". */
    public abstract Builder shortName(String val);

    /** The API name used for generating the URL for the Discovery doc and APIs Explorer link. */
    public abstract Builder discoveryApiName(String val);

    /** The base name of the proto client library package. E.g., "proto-google-cloud-logging-v1". */
    public abstract Builder protoPackageName(String val);

    /** The base name of the gRPC client library package. E.g., "grpc-google-cloud-logging-v1". */
    public abstract Builder grpcPackageName(String val);

    /** The base name of the GAPIC client library package. E.g., "gapic-google-cloud-logging-v1". */
    public abstract Builder gapicPackageName(String val);

    /** The major version of the API, as used in the package name. E.g., "v1". */
    public abstract Builder majorVersion(String val);

    /** The path to the API protos in the googleapis repo. */
    public abstract Builder protoPath(String val);

    public abstract Builder versionPath(String val);

    /** The author of the package. */
    public abstract Builder author(String val);

    /** The email of the author of the package. */
    public abstract Builder email(String val);

    /** The URL of the homepage of the author of the package. */
    public abstract Builder homepage(String val);

    /** The name of the license that the package is licensed under. */
    public abstract Builder licenseName(String val);

    /** The developement status of the package. E.g., "alpha". */
    public abstract Builder developmentStatus(String val);

    /** The developement status of the package used in titles. E.g., "Alpha". */
    public abstract Builder developmentStatusTitle(String s);

    /** Whether the package contains multiple service objects */
    public abstract Builder hasMultipleServices(boolean val);

    /** The packages that should be declared as (Python) namespace packages. */
    public abstract Builder namespacePackages(List<String> val);

    /** The names of the GAPIC modules defining service objects. */
    public abstract Builder apiModules(List<String> val);

    /** The names of the GAPIC modules defining service types. */
    public abstract Builder typeModules(List<String> val);

    /** Whether a smoketest was generated for the package. */
    public abstract Builder hasSmokeTests(boolean val);

    /** File header information such as copyright lines and license lines */
    public abstract Builder fileHeader(FileHeaderView val);

    /** Methods to show smoke test examples for in the readme * */
    public abstract Builder exampleMethods(List<ApiMethodView> vals);

    /**
     * The language that is being generated; primarily used in titles. First letter is uppercase.
     */
    public abstract Builder targetLanguage(String val);

    /** Link to the main README of the metapackage. */
    public abstract Builder mainReadmeLink(String s);

    /** Link to authentication instructions on github.io. */
    public abstract Builder authDocumentationLink(String s);

    /** Link to the client library documentation on github.io. */
    public abstract Builder libraryDocumentationLink(String s);

    /** Link to the semantic versioning information of the metapackage. */
    public abstract Builder versioningDocumentationLink(String s);

    public abstract PackageMetadataView build();
  }
}
