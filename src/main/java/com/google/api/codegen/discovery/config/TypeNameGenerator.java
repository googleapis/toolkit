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
package com.google.api.codegen.discovery.config;

import com.google.api.codegen.DiscoveryImporter;
import java.util.LinkedList;
import java.util.List;

/** Generates language specific names for types and package paths. */
public class TypeNameGenerator {

  /**
   * Returns the language-formatted method name components.
   *
   * <p>For example: "myapi.foo.get" to ["Foo", "Get"]
   */
  public List<String> getMethodNameComponents(List<String> nameComponents) {
    LinkedList<String> copy = new LinkedList<>(nameComponents);
    // Don't edit the original object.
    copy.removeFirst();
    return copy;
  }

  /**
   * Sets the apiName and apiVersion used for name lookups.
   *
   * <p>Only used in Ruby to filter method and type names from apiary_names.yaml
   */
  public void setApiNameAndVersion(String apiName, String apiVersion) {}

  /**
   * Returns the version of the API.
   *
   * <p>Provided in case there is some common transformation on the API's version. For example:
   * "alpha" to "v0.alpha"
   */
  public String getApiVersion(String apiVersion) {
    return apiVersion;
  }

  /** Returns the package prefix for the API. */
  public String getPackagePrefix(String apiName, String apiVersion) {
    return "";
  }

  /**
   * Returns the API type name.
   *
   * <p>Not fully qualified.
   */
  public String getApiTypeName(String apiName) {
    return apiName;
  }

  /**
   * Returns the request's type name.
   *
   * <p>Not fully qualified.
   */
  public String getRequestTypeName(List<String> methodNameComponents) {
    return "";
  }

  /**
   * Returns the responseTypeUrl or an empty string if the response is empty.
   *
   * <p>Provided in case there is some common transformation on the responseTypeUrl.
   */
  public String getResponseTypeUrl(String responseTypeUrl) {
    if (responseTypeUrl.equals(DiscoveryImporter.EMPTY_TYPE_NAME)
        || responseTypeUrl.equals(DiscoveryImporter.EMPTY_TYPE_URL)) {
      return "";
    }
    return responseTypeUrl;
  }

  /**
   * Returns the message's type name.
   *
   * <p>Not fully qualified.
   */
  public String getMessageTypeName(String messageTypeName) {
    return messageTypeName;
  }

  /** Returns a message's subpackage depending on whether or not it's a request type. */
  public String getSubpackage(boolean isRequest) {
    return "";
  }

  /**
   * Returns an example demonstrating the given string format or an empty string if format is
   * unrecognized.
   *
   * <p>If not the empty string, the returned value will be enclosed within the correct
   * language-specific quotes.
   */
  public String getStringFormatExample(String format) {
    return "";
  }

  /**
   * Returns an example demonstrating the given field pattern or an empty string if pattern is
   * invalid.
   *
   * <p>If not the empty string, the returned value will be enclosed within the correct
   * language-specific quotes.
   */
  public String getFieldPatternExample(String pattern) {
    return "";
  }
}
