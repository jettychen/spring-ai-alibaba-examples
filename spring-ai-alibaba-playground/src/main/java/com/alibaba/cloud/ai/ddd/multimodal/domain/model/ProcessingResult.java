/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.cloud.ai.ddd.multimodal.domain.model;

import com.alibaba.cloud.ai.ddd.shared.domain.ValueObject;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 处理结果
 * 值对象，表示多模态处理的输出结果
 */
public class ProcessingResult extends ValueObject {

    private final String content;
    private final byte[] binaryContent;
    private final String contentType;
    private final double confidence;
    private final Map<String, Object> metadata;
    private final LocalDateTime generatedAt;

    private ProcessingResult(String content, byte[] binaryContent, String contentType, 
                           double confidence, Map<String, Object> metadata) {
        this.content = content;
        this.binaryContent = binaryContent != null ? binaryContent.clone() : null;
        this.contentType = contentType;
        this.confidence = confidence;
        this.metadata = Map.copyOf(metadata != null ? metadata : Map.of());
        this.generatedAt = LocalDateTime.now();
        validate();
    }

    /**
     * 创建文本结果
     *
     * @param content 文本内容
     * @param confidence 置信度
     * @return 处理结果
     */
    public static ProcessingResult textResult(String content, double confidence) {
        return new ProcessingResult(content, null, "text/plain", confidence, null);
    }

    /**
     * 创建二进制结果
     *
     * @param binaryContent 二进制内容
     * @param contentType 内容类型
     * @param confidence 置信度
     * @return 处理结果
     */
    public static ProcessingResult binaryResult(byte[] binaryContent, String contentType, double confidence) {
        return new ProcessingResult(null, binaryContent, contentType, confidence, null);
    }

    /**
     * 创建带元数据的文本结果
     *
     * @param content 文本内容
     * @param confidence 置信度
     * @param metadata 元数据
     * @return 处理结果
     */
    public static ProcessingResult textResultWithMetadata(String content, double confidence, Map<String, Object> metadata) {
        return new ProcessingResult(content, null, "text/plain", confidence, metadata);
    }

    /**
     * 创建带元数据的二进制结果
     *
     * @param binaryContent 二进制内容
     * @param contentType 内容类型
     * @param confidence 置信度
     * @param metadata 元数据
     * @return 处理结果
     */
    public static ProcessingResult binaryResultWithMetadata(byte[] binaryContent, String contentType, 
                                                           double confidence, Map<String, Object> metadata) {
        return new ProcessingResult(null, binaryContent, contentType, confidence, metadata);
    }

    public String getContent() {
        return content;
    }

    public byte[] getBinaryContent() {
        return binaryContent != null ? binaryContent.clone() : null;
    }

    public String getContentType() {
        return contentType;
    }

    public double getConfidence() {
        return confidence;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    /**
     * 检查是否有文本内容
     *
     * @return 是否有文本内容
     */
    public boolean hasTextContent() {
        return content != null && !content.trim().isEmpty();
    }

    /**
     * 检查是否有二进制内容
     *
     * @return 是否有二进制内容
     */
    public boolean hasBinaryContent() {
        return binaryContent != null && binaryContent.length > 0;
    }

    /**
     * 检查置信度是否高
     *
     * @return 置信度是否大于0.8
     */
    public boolean hasHighConfidence() {
        return confidence > 0.8;
    }

    /**
     * 获取内容大小
     *
     * @return 内容大小（字符数或字节数）
     */
    public int getContentSize() {
        if (hasTextContent()) {
            return content.length();
        }
        if (hasBinaryContent()) {
            return binaryContent.length;
        }
        return 0;
    }

    /**
     * 添加元数据并创建新的结果对象
     *
     * @param key 元数据键
     * @param value 元数据值
     * @return 新的处理结果
     */
    public ProcessingResult withMetadata(String key, Object value) {
        Map<String, Object> newMetadata = new HashMap<>(metadata);
        newMetadata.put(key, value);
        return new ProcessingResult(content, binaryContent, contentType, confidence, newMetadata);
    }

    @Override
    protected void validate() {
        if (!hasTextContent() && !hasBinaryContent()) {
            throw new IllegalArgumentException("ProcessingResult must have either text or binary content");
        }
        if (confidence < 0.0 || confidence > 1.0) {
            throw new IllegalArgumentException("Confidence must be between 0.0 and 1.0");
        }
        if (contentType == null || contentType.trim().isEmpty()) {
            throw new IllegalArgumentException("Content type cannot be null or empty");
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ProcessingResult that = (ProcessingResult) obj;
        return Double.compare(that.confidence, confidence) == 0 &&
               Objects.equals(content, that.content) &&
               Objects.deepEquals(binaryContent, that.binaryContent) &&
               Objects.equals(contentType, that.contentType) &&
               Objects.equals(metadata, that.metadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(content, Objects.hashCode(binaryContent), contentType, confidence, metadata);
    }

    @Override
    public String toString() {
        return String.format("ProcessingResult{contentType='%s', confidence=%.2f, contentSize=%d, generatedAt=%s}", 
                           contentType, confidence, getContentSize(), generatedAt);
    }
}