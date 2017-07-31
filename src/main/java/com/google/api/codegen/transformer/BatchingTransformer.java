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
package com.google.api.codegen.transformer;

import com.google.api.codegen.config.BatchingConfig;
import com.google.api.codegen.config.FieldType;
import com.google.api.codegen.config.MethodConfig;
import com.google.api.codegen.util.Name;
import com.google.api.codegen.viewmodel.BatchingConfigView;
import com.google.api.codegen.viewmodel.BatchingDescriptorClassView;
import com.google.api.codegen.viewmodel.BatchingDescriptorView;
import com.google.api.codegen.viewmodel.BatchingPartitionKeyView;
import com.google.api.codegen.viewmodel.FieldCopyView;
import com.google.api.tools.framework.model.FieldSelector;
import com.google.api.tools.framework.model.Method;
import com.google.api.tools.framework.model.TypeRef;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;

public class BatchingTransformer {

  public List<BatchingDescriptorView> generateDescriptors(GapicInterfaceContext context) {
    SurfaceNamer namer = context.getNamer();
    ImmutableList.Builder<BatchingDescriptorView> descriptors = ImmutableList.builder();
    for (Method method : context.getBatchingMethods()) {
      BatchingConfig batching = context.getMethodConfig(method).getBatching();
      BatchingDescriptorView.Builder descriptor = BatchingDescriptorView.newBuilder();
      descriptor.methodName(namer.getMethodKey(method));
      descriptor.batchedFieldName(namer.getFieldName(batching.getBatchedField()));
      descriptor.discriminatorFieldNames(generateDiscriminatorFieldNames(batching));

      if (batching.hasSubresponseField()) {
        descriptor.subresponseFieldName(namer.getFieldName(batching.getSubresponseField()));
      }

      descriptor.byteLengthFunctionName(
          namer.getByteLengthFunctionName(batching.getBatchedField().getProtoTypeRef()));
      descriptors.add(descriptor.build());
    }
    return descriptors.build();
  }

  public List<BatchingDescriptorClassView> generateDescriptorClasses(
      GapicInterfaceContext context) {
    List<BatchingDescriptorClassView> descriptors = new ArrayList<>();

    for (Method method : context.getInterface().getMethods()) {
      MethodConfig methodConfig = context.getMethodConfig(method);
      if (!methodConfig.isBatching()) {
        continue;
      }
      descriptors.add(generateDescriptorClass(context.asRequestMethodContext(method)));
    }

    return descriptors;
  }

  public List<BatchingDescriptorClassView> generateDescriptorClasses(
      DiscoGapicInterfaceContext context) {
    List<BatchingDescriptorClassView> descriptors = new ArrayList<>();

    for (com.google.api.codegen.discovery.Method method : context.getMethods()) {
      MethodConfig methodConfig = context.getMethodConfig(method);
      if (!methodConfig.isBatching()) {
        continue;
      }
      descriptors.add(generateDescriptorClass(context.asRequestMethodContext(method)));
    }

    return descriptors;
  }

  public BatchingConfigView generateBatchingConfig(MethodContext context) {
    BatchingConfig batchingConfig = context.getMethodConfig().getBatching();
    BatchingConfigView.Builder batchingConfigView = BatchingConfigView.newBuilder();

    batchingConfigView.elementCountThreshold(batchingConfig.getElementCountThreshold());
    batchingConfigView.elementCountLimit(batchingConfig.getElementCountLimit());
    batchingConfigView.requestByteThreshold(batchingConfig.getRequestByteThreshold());
    batchingConfigView.requestByteLimit(batchingConfig.getRequestByteLimit());
    batchingConfigView.delayThresholdMillis(batchingConfig.getDelayThresholdMillis());
    batchingConfigView.flowControlElementLimit(batchingConfig.getFlowControlElementLimit());
    batchingConfigView.flowControlByteLimit(batchingConfig.getFlowControlByteLimit());
    batchingConfigView.flowControlLimitExceededBehavior(
        batchingConfig.getFlowControlLimitConfig().toString());

    return batchingConfigView.build();
  }

  private List<String> generateDiscriminatorFieldNames(BatchingConfig batching) {
    ImmutableList.Builder<String> fieldNames = ImmutableList.builder();
    for (FieldSelector fieldSelector : batching.getDiscriminatorFields()) {
      fieldNames.add(fieldSelector.getParamName());
    }
    return fieldNames.build();
  }

  private BatchingDescriptorClassView generateDescriptorClass(GapicMethodContext context) {
    SurfaceNamer namer = context.getNamer();
    ModelTypeTable typeTable = context.getTypeTable();
    Method method = context.getMethod();
    BatchingConfig batching = context.getMethodConfig().getBatching();

    FieldType batchedField = batching.getBatchedField();

    FieldType subresponseField = batching.getSubresponseField();

    BatchingDescriptorClassView.Builder desc = BatchingDescriptorClassView.newBuilder();

    desc.name(namer.getBatchingDescriptorConstName(method));
    desc.requestTypeName(typeTable.getAndSaveNicknameFor(method.getInputType()));
    desc.responseTypeName(typeTable.getAndSaveNicknameFor(method.getOutputType()));
    desc.batchedFieldTypeName(typeTable.getAndSaveNicknameFor(batchedField));

    desc.partitionKeys(generatePartitionKeys(context));
    desc.discriminatorFieldCopies(generateDiscriminatorFieldCopies(context));

    desc.batchedFieldGetFunction(namer.getFieldGetFunctionName(batchedField));
    desc.batchedFieldSetFunction(namer.getFieldSetFunctionName(batchedField));
    desc.batchedFieldCountGetFunction(namer.getFieldCountGetFunctionName(batchedField));

    if (subresponseField != null) {
      desc.subresponseTypeName(typeTable.getAndSaveNicknameFor(subresponseField));
      desc.subresponseByIndexGetFunction(namer.getByIndexGetFunctionName(subresponseField));
      desc.subresponseSetFunction(namer.getFieldSetFunctionName(subresponseField));
    }

    return desc.build();
  }

  private BatchingDescriptorClassView generateDescriptorClass(DiscoGapicMethodContext context) {
    SurfaceNamer namer = context.getNamer();
    SchemaTypeTable typeTable = context.getTypeTable();
    com.google.api.codegen.discovery.Method method = context.getMethod();
    BatchingConfig batching = context.getMethodConfig().getBatching();

    FieldType batchedField = batching.getBatchedField();
    FieldType batchedType = batchedField;

    FieldType subresponseField = batching.getSubresponseField();

    BatchingDescriptorClassView.Builder desc = BatchingDescriptorClassView.newBuilder();

    desc.name(namer.getBatchingDescriptorConstName(method));
    desc.requestTypeName(typeTable.getAndSaveNicknameFor(method.request()));
    desc.responseTypeName(typeTable.getAndSaveNicknameFor(method.response()));
    desc.batchedFieldTypeName(typeTable.getAndSaveNicknameFor(batchedType));

    desc.partitionKeys(generatePartitionKeys(context));
    desc.discriminatorFieldCopies(generateDiscriminatorFieldCopies(context));

    desc.batchedFieldGetFunction(namer.getFieldGetFunctionName(batchedType));
    desc.batchedFieldSetFunction(namer.getFieldSetFunctionName(batchedType));
    desc.batchedFieldCountGetFunction(namer.getFieldCountGetFunctionName(batchedField));

    if (subresponseField != null) {
      FieldType subresponseType = subresponseField;
      desc.subresponseTypeName(typeTable.getAndSaveNicknameFor(subresponseType));
      desc.subresponseByIndexGetFunction(namer.getByIndexGetFunctionName(subresponseField));
      desc.subresponseSetFunction(namer.getFieldSetFunctionName(subresponseType));
    }

    return desc.build();
  }

  private List<BatchingPartitionKeyView> generatePartitionKeys(GapicMethodContext context) {
    List<BatchingPartitionKeyView> keys = new ArrayList<>();
    BatchingConfig batching = context.getMethodConfig().getBatching();
    for (FieldSelector fieldSelector : batching.getDiscriminatorFields()) {
      TypeRef selectedType = fieldSelector.getLastField().getType();
      Name selectedTypeName = Name.from(fieldSelector.getLastField().getSimpleName());
      BatchingPartitionKeyView key =
          BatchingPartitionKeyView.newBuilder()
              .fieldGetFunction(
                  context.getNamer().getFieldGetFunctionName(selectedType, selectedTypeName))
              .build();
      keys.add(key);
    }
    return keys;
  }

  private List<BatchingPartitionKeyView> generatePartitionKeys(DiscoGapicMethodContext context) {
    List<BatchingPartitionKeyView> keys = new ArrayList<>();
    BatchingConfig batching = context.getMethodConfig().getBatching();
    for (FieldSelector fieldSelector : batching.getDiscriminatorFields()) {
      TypeRef selectedType = fieldSelector.getLastField().getType();
      Name selectedTypeName = Name.from(fieldSelector.getLastField().getSimpleName());
      BatchingPartitionKeyView key =
          BatchingPartitionKeyView.newBuilder()
              .fieldGetFunction(
                  context.getNamer().getFieldGetFunctionName(selectedType, selectedTypeName))
              .build();
      keys.add(key);
    }
    return keys;
  }

  private List<FieldCopyView> generateDiscriminatorFieldCopies(GapicMethodContext context) {
    List<FieldCopyView> fieldCopies = new ArrayList<>();
    BatchingConfig batching = context.getMethodConfig().getBatching();
    for (FieldSelector fieldSelector : batching.getDiscriminatorFields()) {
      TypeRef selectedType = fieldSelector.getLastField().getType();
      Name selectedTypeName = Name.from(fieldSelector.getLastField().getSimpleName());
      FieldCopyView fieldCopy =
          FieldCopyView.newBuilder()
              .fieldGetFunction(
                  context.getNamer().getFieldGetFunctionName(selectedType, selectedTypeName))
              .fieldSetFunction(
                  context.getNamer().getFieldSetFunctionName(selectedType, selectedTypeName))
              .build();
      fieldCopies.add(fieldCopy);
    }
    return fieldCopies;
  }

  private List<FieldCopyView> generateDiscriminatorFieldCopies(DiscoGapicMethodContext context) {
    List<FieldCopyView> fieldCopies = new ArrayList<>();
    BatchingConfig batching = context.getMethodConfig().getBatching();
    for (FieldSelector fieldSelector : batching.getDiscriminatorFields()) {
      TypeRef selectedType = fieldSelector.getLastField().getType();
      Name selectedTypeName = Name.from(fieldSelector.getLastField().getSimpleName());
      FieldCopyView fieldCopy =
          FieldCopyView.newBuilder()
              .fieldGetFunction(
                  context.getNamer().getFieldGetFunctionName(selectedType, selectedTypeName))
              .fieldSetFunction(
                  context.getNamer().getFieldSetFunctionName(selectedType, selectedTypeName))
              .build();
      fieldCopies.add(fieldCopy);
    }
    return fieldCopies;
  }
}
