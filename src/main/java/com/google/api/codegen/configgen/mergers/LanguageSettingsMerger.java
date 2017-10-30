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
package com.google.api.codegen.configgen.mergers;

import com.google.api.codegen.configgen.ListTransformer;
import com.google.api.codegen.configgen.nodes.ConfigNode;
import com.google.api.codegen.configgen.nodes.FieldConfigNode;
import com.google.api.codegen.configgen.nodes.ScalarConfigNode;
import com.google.api.codegen.configgen.nodes.metadata.DefaultComment;
import com.google.api.codegen.util.VersionMatcher;
import com.google.api.tools.framework.model.Diag;
import com.google.api.tools.framework.model.Interface;
import com.google.api.tools.framework.model.Model;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.protobuf.Api;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/** Merges the language_settings property from a Model into a ConfigNode. */
public class LanguageSettingsMerger {
  private static final String DEFAULT_PACKAGE_SEPARATOR = ".";

  private static final Map<String, LanguageFormatter> LANGUAGE_FORMATTERS;

  static {
    List<RewriteRule> javaRewriteRules =
        Arrays.asList(new RewriteRule("^google(\\.cloud)?", "com.google.cloud"));
    List<RewriteRule> commonRewriteRules =
        Arrays.asList(new RewriteRule("^google(?!\\.cloud)", "google.cloud"));
    LANGUAGE_FORMATTERS =
        ImmutableMap.<String, LanguageFormatter>builder()
            .put("java", new SimpleLanguageFormatter(".", javaRewriteRules, false))
            .put("python", new PythonLanguageFormatter(commonRewriteRules))
            .put("go", new GoLanguageFormatter())
            .put("csharp", new SimpleLanguageFormatter(".", null, true))
            .put("ruby", new SimpleLanguageFormatter("::", commonRewriteRules, true))
            .put("php", new SimpleLanguageFormatter("\\", commonRewriteRules, true))
            .put("nodejs", new NodeJSLanguageFormatter())
            .build();
  }

  public ConfigNode mergeLanguageSettings(Model model, ConfigNode configNode, ConfigNode prevNode) {
    final String packageName = getPackageName(model);
    if (packageName == null) {
      return null;
    }

    FieldConfigNode languageSettingsNode = new FieldConfigNode("language_settings");
    prevNode.insertNext(languageSettingsNode);
    ConfigNode languageSettingsValueNode =
        ListTransformer.generateList(
            LANGUAGE_FORMATTERS.entrySet(),
            languageSettingsNode,
            new ListTransformer.ElementTransformer<Map.Entry<String, LanguageFormatter>>() {
              @Override
              public ConfigNode generateElement(Map.Entry<String, LanguageFormatter> entry) {
                ConfigNode languageNode = new FieldConfigNode(entry.getKey());
                mergeLanguageSetting(languageNode, entry.getValue(), packageName);
                return languageNode;
              }
            });
    return languageSettingsNode
        .setChild(languageSettingsValueNode)
        .setComment(new DefaultComment("The settings of generated code in a specific language."));
  }

  private String getPackageName(Model model) {
    if (model.getServiceConfig().getApisCount() > 0) {
      Api api = model.getServiceConfig().getApis(0);
      Interface apiInterface = model.getSymbolTable().lookupInterface(api.getName());
      if (apiInterface != null) {
        return apiInterface.getFile().getFullName();
      }
    }

    model.getDiagCollector().addDiag(Diag.error(model.getLocation(), "No interface found"));
    return null;
  }

  private ConfigNode mergeLanguageSetting(
      ConfigNode languageNode, LanguageFormatter languageFormatter, String packageName) {
    ConfigNode packageNameNode = new FieldConfigNode("package_name");
    languageNode.setChild(packageNameNode);
    mergePackageNameValue(packageNameNode, languageFormatter, packageName);
    return packageNameNode;
  }

  private ConfigNode mergePackageNameValue(
      ConfigNode packageNameNode, LanguageFormatter languageFormatter, String packageName) {
    ConfigNode packageNameValueNode =
        new ScalarConfigNode(languageFormatter.getFormattedPackageName(packageName));
    packageNameNode.setChild(packageNameValueNode);
    return packageNameValueNode;
  }

  private interface LanguageFormatter {
    String getFormattedPackageName(String packageName);
  }

  private static class SimpleLanguageFormatter implements LanguageFormatter {

    private final String separator;
    private final List<RewriteRule> rewriteRules;
    private final boolean shouldCapitalize;

    public SimpleLanguageFormatter(
        String separator, List<RewriteRule> rewriteRules, boolean shouldCapitalize) {
      this.separator = separator;
      if (rewriteRules != null) {
        this.rewriteRules = rewriteRules;
      } else {
        this.rewriteRules = new ArrayList<>();
      }
      this.shouldCapitalize = shouldCapitalize;
    }

    @Override
    public String getFormattedPackageName(String packageName) {
      for (RewriteRule rewriteRule : rewriteRules) {
        packageName = rewriteRule.rewrite(packageName);
      }
      List<String> elements = new LinkedList<>();
      for (String component : Splitter.on(DEFAULT_PACKAGE_SEPARATOR).split(packageName)) {
        if (shouldCapitalize) {
          elements.add(capitalize(component));
        } else {
          elements.add(component);
        }
      }
      return Joiner.on(separator).join(elements);
    }

    private String capitalize(String string) {
      return Character.toUpperCase(string.charAt(0)) + string.substring(1);
    }
  }

  private static class GoLanguageFormatter implements LanguageFormatter {
    public String getFormattedPackageName(String packageName) {
      List<String> nameComponents =
          Lists.newArrayList(Splitter.on(DEFAULT_PACKAGE_SEPARATOR).splitToList(packageName));

      // If the name follows the pattern google.foo.bar.v1234,
      // we reformat it into cloud.google.com.
      // google.logging.v2 => cloud.google.com/go/logging/apiv2
      // Otherwise, fall back to backup
      if (!isApiGoogleCloud(nameComponents)) {
        nameComponents.add(0, "google.golang.org");
        return Joiner.on("/").join(nameComponents);
      }
      int size = nameComponents.size();
      return "cloud.google.com/go/"
          + Joiner.on("/").join(nameComponents.subList(1, size - 1))
          + "/api"
          + nameComponents.get(size - 1);
    }

    /** Returns true if it is a Google Cloud API. */
    private boolean isApiGoogleCloud(List<String> nameComponents) {
      int size = nameComponents.size();
      return size >= 3
          && nameComponents.get(0).equals("google")
          && nameComponents.get(size - 1).startsWith("v");
    }
  }

  private static class NodeJSLanguageFormatter implements LanguageFormatter {
    @Override
    public String getFormattedPackageName(String packageName) {
      List<String> nameComponents = Splitter.on(DEFAULT_PACKAGE_SEPARATOR).splitToList(packageName);
      return nameComponents.get(nameComponents.size() - 2)
          + "."
          + nameComponents.get(nameComponents.size() - 1);
    }
  }

  private static class RewriteRule {
    private final String pattern;
    private final String replacement;

    public RewriteRule(String pattern, String replacement) {
      this.pattern = pattern;
      this.replacement = replacement;
    }

    public String rewrite(String input) {
      if (pattern == null) {
        return input;
      }
      return input.replaceAll(pattern, replacement);
    }
  }

  private static class PythonLanguageFormatter implements LanguageFormatter {
    private List<RewriteRule> rewriteRules;

    public PythonLanguageFormatter(List<RewriteRule> rewriteRules) {
      this.rewriteRules = rewriteRules;
    }

    @Override
    public String getFormattedPackageName(String packageName) {
      for (RewriteRule rule : rewriteRules) {
        packageName = rule.rewrite(packageName);
      }
      List<String> names = Splitter.on(DEFAULT_PACKAGE_SEPARATOR).splitToList(packageName);
      String lastName = Iterables.getLast(names);
      if (!VersionMatcher.isVersion(lastName)) {
        return String.format("%s.gapic", packageName);
      }
      String unversionedPackageName = Joiner.on('.').join(names.subList(0, names.size() - 1));
      return String.format("%s_%s.gapic", unversionedPackageName, lastName);
    }
  }
}
