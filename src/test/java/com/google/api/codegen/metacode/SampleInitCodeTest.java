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
package com.google.api.codegen.metacode;

import com.google.api.codegen.util.Name;
import com.google.api.codegen.util.SymbolTable;
import com.google.api.tools.framework.model.Interface;
import com.google.api.tools.framework.model.Method;
import com.google.api.tools.framework.model.Model;
import com.google.api.tools.framework.model.stages.Merged;
import com.google.api.tools.framework.model.testing.TestConfig;
import com.google.api.tools.framework.model.testing.TestDataLocator;
import com.google.api.tools.framework.setup.StandardSetup;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.truth.Truth;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SampleInitCodeTest {

  @Rule public TemporaryFolder tempDir = new TemporaryFolder();

  private TestDataLocator testDataLocator;
  private TestConfig testConfig;
  private Model model;
  private Interface interfaze;
  private Method method;

  @Before
  public void setupClass() {
    List<String> protoFiles = Lists.newArrayList("myproto.proto");
    List<String> yamlFiles = Lists.newArrayList("myproto.yaml");
    testDataLocator = TestDataLocator.create(SampleInitCodeTest.class);
    testConfig = new TestConfig(testDataLocator, tempDir.getRoot().getPath(), protoFiles);
    model = testConfig.createModel(yamlFiles);
    StandardSetup.registerStandardProcessors(model);
    StandardSetup.registerStandardConfigAspects(model);
    model.establishStage(Merged.KEY);
    interfaze = model.getSymbolTable().getInterfaces().asList().get(0);
    method = interfaze.getMethods().get(0);
  }

  private InitTreeParserContext.Builder getContextBuilder() {
    return InitTreeParserContext.newBuilder()
        .table(new SymbolTable())
        .rootObjectType(method.getInputType())
        .initValueConfigMap(ImmutableMap.<String, InitValueConfig>of())
        .suggestedName(Name.from("request"));
  }

  @Test
  public void testRegex() throws Exception {
    Pattern fieldPattern = FieldStructureParser.getFieldStructurePattern();
    Pattern listPattern = FieldStructureParser.getFieldListPattern();
    Pattern mapPattern = FieldStructureParser.getFieldMapPattern();

    Matcher matcher = listPattern.matcher("mylist[0][0]");
    Truth.assertThat(matcher.matches()).isTrue();
    Truth.assertThat(matcher.group(1)).isEqualTo("mylist[0]");
    Truth.assertThat(matcher.group(2)).isEqualTo("0");

    String dualMatch = "mymap[0]{key}";
    matcher = listPattern.matcher(dualMatch);
    Truth.assertThat(matcher.matches()).isFalse();
    matcher = mapPattern.matcher(dualMatch);
    Truth.assertThat(matcher.matches()).isTrue();
    Truth.assertThat(matcher.group(1)).isEqualTo("mymap[0]");
    Truth.assertThat(matcher.group(2)).isEqualTo("key");

    Matcher fieldMatcher = fieldPattern.matcher("myfield.mynextfield");
    Truth.assertThat(fieldMatcher.matches()).isTrue();
    Truth.assertThat(fieldMatcher.group(1)).isEqualTo("myfield");
    Truth.assertThat(fieldMatcher.group(2)).isEqualTo("mynextfield");

    Truth.assertThat(fieldPattern.matcher("singlefield").matches()).isFalse();
    Truth.assertThat(fieldPattern.matcher("myfield.mylist[0]").matches()).isFalse();
    Truth.assertThat(fieldPattern.matcher("myfield.mymap{key}").matches()).isFalse();
  }

  @Test
  public void testSimpleField() throws Exception {
    String fieldSpec = "myfield";

    InitCodeNode expectedStructure = InitCodeNode.create("myfield");

    InitCodeNode actualStructure = FieldStructureParser.parse(fieldSpec);
    Truth.assertThat(checkEquals(actualStructure, expectedStructure));
  }

  @Test
  public void testEmbeddedField() throws Exception {
    String fieldSpec = "myobj.myfield";

    InitCodeNode innerStructure = InitCodeNode.create("myfield");
    InitCodeNode expectedStructure =
        InitCodeNode.createWithChildren(
            "myobj", InitCodeLineType.StructureInitLine, innerStructure);

    InitCodeNode actualStructure = FieldStructureParser.parse(fieldSpec);
    Truth.assertThat(checkEquals(actualStructure, expectedStructure));
  }

  @Test
  public void testListField() throws Exception {
    String fieldSpec = "mylist[0]";

    InitCodeNode innerStructure = InitCodeNode.create("0");
    InitCodeNode expectedStructure =
        InitCodeNode.createWithChildren("mylist", InitCodeLineType.ListInitLine, innerStructure);

    InitCodeNode actualStructure = FieldStructureParser.parse(fieldSpec);
    Truth.assertThat(checkEquals(actualStructure, expectedStructure));
  }

  @Test
  public void testMapField() throws Exception {
    String fieldSpec = "mymap{key}";

    InitCodeNode innerStructure = InitCodeNode.create("key");
    InitCodeNode expectedStructure =
        InitCodeNode.createWithChildren("mymap", InitCodeLineType.MapInitLine, innerStructure);

    InitCodeNode actualStructure = FieldStructureParser.parse(fieldSpec);
    Truth.assertThat(checkEquals(actualStructure, expectedStructure));
  }

  @Test
  public void testNestedListField() throws Exception {
    String fieldSpec = "mylist[0][0]";

    InitCodeNode innerList = InitCodeNode.create("0");
    InitCodeNode outerList =
        InitCodeNode.createWithChildren("0", InitCodeLineType.ListInitLine, innerList);
    InitCodeNode expectedStructure =
        InitCodeNode.createWithChildren("mylist", InitCodeLineType.ListInitLine, outerList);

    InitCodeNode actualStructure = FieldStructureParser.parse(fieldSpec);
    Truth.assertThat(checkEquals(actualStructure, expectedStructure));
  }

  @Test
  public void testNestedMixedField() throws Exception {
    String fieldSpec = "mylist[0]{key}";

    InitCodeNode innerMap = InitCodeNode.create("key");
    InitCodeNode innerList =
        InitCodeNode.createWithChildren("0", InitCodeLineType.ListInitLine, innerMap);
    InitCodeNode expectedStructure =
        InitCodeNode.createWithChildren("mylist", InitCodeLineType.ListInitLine, innerList);

    InitCodeNode actualStructure = FieldStructureParser.parse(fieldSpec);
    Truth.assertThat(checkEquals(actualStructure, expectedStructure));
  }

  @Test
  public void testAssignment() throws Exception {
    String fieldSpec = "myfield=\"default\"";

    InitCodeNode expectedStructure =
        InitCodeNode.createWithValue("myfield", InitValueConfig.createWithValue("\"default\""));

    InitCodeNode actualStructure = FieldStructureParser.parse(fieldSpec);
    Truth.assertThat(checkEquals(actualStructure, expectedStructure));
  }

  @Test
  public void testListEmbeddedField() throws Exception {
    String fieldSpec = "mylist[0].myfield";

    InitCodeNode innerStructure = InitCodeNode.create("myfield");
    InitCodeNode innerList =
        InitCodeNode.createWithChildren("0", InitCodeLineType.StructureInitLine, innerStructure);
    InitCodeNode expectedStructure =
        InitCodeNode.createWithChildren("mylist", InitCodeLineType.ListInitLine, innerList);

    InitCodeNode actualStructure = FieldStructureParser.parse(fieldSpec);
    Truth.assertThat(checkEquals(actualStructure, expectedStructure));
  }

  @Test
  public void testEmbeddedFieldList() throws Exception {
    String fieldSpec = "myfield.mylist[0]";

    InitCodeNode innerList = InitCodeNode.create("0");
    InitCodeNode innerStructure =
        InitCodeNode.createWithChildren("mylist", InitCodeLineType.ListInitLine, innerList);
    InitCodeNode expectedStructure =
        InitCodeNode.createWithChildren(
            "myfield", InitCodeLineType.StructureInitLine, innerStructure);

    InitCodeNode actualStructure = FieldStructureParser.parse(fieldSpec);
    Truth.assertThat(checkEquals(actualStructure, expectedStructure));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testListFieldBadIndex() throws Exception {
    List<String> fieldSpecs = Arrays.asList("mylist[1]");
    InitCodeNode.createTree(getContextBuilder().dottedPathStrings(fieldSpecs).build());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testListFieldIndexGap() throws Exception {
    List<String> fieldSpecs = Arrays.asList("mylist[0]", "mylist[2]");
    InitCodeNode.createTree(getContextBuilder().dottedPathStrings(fieldSpecs).build());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testListFieldMismatchedListThenField() throws Exception {
    List<String> fieldSpecs = Arrays.asList("myfield[0]", "myfield.subfield");
    InitCodeNode.createTree(getContextBuilder().dottedPathStrings(fieldSpecs).build());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testListFieldMismatchedFieldThenList() throws Exception {
    List<String> fieldSpecs = Arrays.asList("myfield.subfield", "myfield[0]");
    InitCodeNode.createTree(getContextBuilder().dottedPathStrings(fieldSpecs).build());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBadField() throws Exception {
    List<String> fieldSpecs = Arrays.asList("notafield");
    InitCodeNode.createTree(getContextBuilder().dottedPathStrings(fieldSpecs).build());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBadSubField() throws Exception {
    List<String> fieldSpecs = Arrays.asList("myfield.notafield");
    InitCodeNode.createTree(getContextBuilder().dottedPathStrings(fieldSpecs).build());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMapFieldBadStringIndex() throws Exception {
    List<String> fieldSpecs = Arrays.asList("stringmap{0}");
    InitCodeNode.createTree(getContextBuilder().dottedPathStrings(fieldSpecs).build());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMapFieldBadStringIndexNoQuotes() throws Exception {
    List<String> fieldSpecs = Arrays.asList("stringmap{key}");
    InitCodeNode.createTree(getContextBuilder().dottedPathStrings(fieldSpecs).build());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMapFieldBadIntIndex() throws Exception {
    List<String> fieldSpecs = Arrays.asList("intmap{\"key\"}");
    InitCodeNode.createTree(getContextBuilder().dottedPathStrings(fieldSpecs).build());
  }

  @Test
  public void testMultipleFields() throws Exception {
    List<String> fieldSpecs =
        Arrays.asList("mylist", "myfield", "secondfield", "stringmap", "intmap");

    List<String> expectedKeyList =
        Lists.newArrayList("mylist", "myfield", "secondfield", "stringmap", "intmap", "root");

    InitCodeNode rootNode =
        InitCodeNode.createTree(getContextBuilder().dottedPathStrings(fieldSpecs).build());
    List<String> actualKeyList = new ArrayList<>();
    for (InitCodeNode node : rootNode.listInInitializationOrder()) {
      actualKeyList.add(node.getKey());
    }
    Truth.assertThat(actualKeyList.equals(expectedKeyList));
  }

  @Test
  public void testMultipleListEntries() throws Exception {
    List<String> fieldSpecs = Arrays.asList("mylist[0]", "mylist[1]");

    List<String> expectedKeyList = Arrays.asList("0", "1", "mylist", "root");

    InitCodeNode rootNode =
        InitCodeNode.createTree(getContextBuilder().dottedPathStrings(fieldSpecs).build());
    List<String> actualKeyList = new ArrayList<>();
    for (InitCodeNode node : rootNode.listInInitializationOrder()) {
      actualKeyList.add(node.getKey());
    }
    Truth.assertThat(actualKeyList.equals(expectedKeyList));
  }

  @Test
  public void testMultipleMapEntries() throws Exception {
    List<String> fieldSpecs =
        Arrays.asList("stringmap{\"key1\"}", "stringmap{\"key2\"}", "intmap{123}", "intmap{456}");

    List<String> expectedKeyList =
        Arrays.asList("key1", "key2", "stringmap", "123", "456", "intmap", "root");

    InitCodeNode rootNode =
        InitCodeNode.createTree(getContextBuilder().dottedPathStrings(fieldSpecs).build());
    List<String> actualKeyList = new ArrayList<>();
    for (InitCodeNode node : rootNode.listInInitializationOrder()) {
      actualKeyList.add(node.getKey());
    }
    Truth.assertThat(actualKeyList.equals(expectedKeyList));
  }

  @Test
  public void testListEmbeddedMultipleFields() throws Exception {
    List<String> fieldSpecs = Arrays.asList("mylist[0].subfield", "mylist[0].subsecondfield");

    List<String> expectedKeyList =
        Arrays.asList("subfield", "subsecondfield", "0", "mylist", "root");

    InitCodeNode rootNode =
        InitCodeNode.createTree(getContextBuilder().dottedPathStrings(fieldSpecs).build());
    List<String> actualKeyList = new ArrayList<>();
    for (InitCodeNode node : rootNode.listInInitializationOrder()) {
      actualKeyList.add(node.getKey());
    }
    Truth.assertThat(actualKeyList.equals(expectedKeyList));
  }

  @Test
  public void testCompoundingStructure() throws Exception {
    List<String> fieldSpecs = Arrays.asList("myfield", "myfield.subfield");

    List<String> expectedKeyList = Arrays.asList("subfield", "myfield", "root");

    InitCodeNode rootNode =
        InitCodeNode.createTree(getContextBuilder().dottedPathStrings(fieldSpecs).build());
    List<String> actualKeyList = new ArrayList<>();
    for (InitCodeNode node : rootNode.listInInitializationOrder()) {
      actualKeyList.add(node.getKey());
    }
    Truth.assertThat(actualKeyList.equals(expectedKeyList));
  }

  @Test
  public void testCompoundingStructureList() throws Exception {
    List<String> fieldSpecs = Arrays.asList("mylist", "mylist[0]", "mylist[0].subfield");

    List<String> expectedKeyList = Arrays.asList("subfield", "0", "mylist", "root");

    InitCodeNode rootNode =
        InitCodeNode.createTree(getContextBuilder().dottedPathStrings(fieldSpecs).build());
    List<String> actualKeyList = new ArrayList<>();
    for (InitCodeNode node : rootNode.listInInitializationOrder()) {
      actualKeyList.add(node.getKey());
    }
    Truth.assertThat(actualKeyList.equals(expectedKeyList));
  }

  private static boolean checkEquals(InitCodeNode first, InitCodeNode second) {
    if (first == second) {
      return true;
    }
    return first.getKey().equals(second.getKey())
        && first.getLineType().equals(second.getLineType())
        && first.getInitValueConfig().equals(second.getInitValueConfig())
        && first.getChildren().equals(second.getChildren())
        && (first.getType() == null
            ? second.getType() == null
            : first.getType().equals(second.getType()))
        && (first.getIdentifier() == null
            ? second.getIdentifier() == null
            : first.getIdentifier().equals(second.getIdentifier()))
        && (first.getInitCodeLine() == null
            ? second.getInitCodeLine() == null
            : first.getInitCodeLine().equals(second.getInitCodeLine()));
  }
}
