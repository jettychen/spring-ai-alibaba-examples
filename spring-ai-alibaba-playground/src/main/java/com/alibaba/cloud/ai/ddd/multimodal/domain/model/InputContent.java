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

import java.util.Objects;

/**
 * 输入内容
 * 值对象，表示处理任务的输入数据
 */
public class InputContent extends ValueObject {

    private final String fileName;
    private final byte[] content;
    private final String contentType;
    private final long size;
    private final ModalityType modalityType;

    private InputContent(String fileName, byte[] content, String contentType) {
        this.fileName = Objects.requireNonNull(fileName, "File name cannot be null");
        this.content = Objects.requireNonNull(content, "Content cannot be null").clone();
        this.contentType = Objects.requireNonNull(contentType, "Content type cannot be null");
        this.size = content.length;
        this.modalityType = inferModalityType(fileName, contentType);
        validate();
    }

    /**
     * 创建输入内容
     *
     * @param fileName 文件名
     * @param content 内容数据
     * @param contentType 内容类型
     * @return 输入内容
     */
    public static InputContent of(String fileName, byte[] content, String contentType) {
        return new InputContent(fileName, content, contentType);
    }

    /**
     * 创建文本输入内容
     *
     * @param text 文本内容
     * @return 输入内容
     */
    public static InputContent text(String text) {
        return new InputContent("input.txt", text.getBytes(), "text/plain");
    }

    public String getFileName() {
        return fileName;
    }

    public byte[] getContent() {
        return content.clone();
    }

    public String getContentType() {
        return contentType;
    }

    public long getSize() {
        return size;
    }

    public ModalityType getModalityType() {
        return modalityType;
    }

    /**
     * 获取文件扩展名
     *
     * @return 文件扩展名
     */
    public String getFileExtension() {
        int lastDot = fileName.lastIndexOf('.');
        return lastDot > 0 ? fileName.substring(lastDot + 1).toLowerCase() : "";
    }

    /**
     * 检查是否为空内容
     *
     * @return 是否为空
     */
    public boolean isEmpty() {
        return content.length == 0;
    }

    /**
     * 检查是否为大文件
     *
     * @return 是否超过10MB
     */
    public boolean isLargeFile() {
        return size > 10 * 1024 * 1024; // 10MB
    }

    /**
     * 获取格式化的文件大小
     *
     * @return 格式化的大小字符串
     */
    public String getFormattedSize() {
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return String.format("%.2f KB", size / 1024.0);
        } else if (size < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", size / (1024.0 * 1024.0));
        } else {
            return String.format("%.2f GB", size / (1024.0 * 1024.0 * 1024.0));
        }
    }

    /**
     * 根据文件名和内容类型推断模态类型
     */
    private ModalityType inferModalityType(String fileName, String contentType) {
        // 优先根据内容类型判断
        if (contentType.startsWith("image/")) {
            return ModalityType.IMAGE;
        } else if (contentType.startsWith("audio/")) {
            return ModalityType.AUDIO;
        } else if (contentType.startsWith("video/")) {
            return ModalityType.VIDEO;
        } else if (contentType.startsWith("text/")) {
            return ModalityType.TEXT;
        }
        
        // 根据文件扩展名判断
        return ModalityType.inferFromExtension(getFileExtension());
    }

    @Override
    protected void validate() {
        if (fileName.trim().isEmpty()) {
            throw new IllegalArgumentException("File name cannot be empty");
        }
        if (content.length == 0) {
            throw new IllegalArgumentException("Content cannot be empty");
        }
        if (contentType.trim().isEmpty()) {
            throw new IllegalArgumentException("Content type cannot be empty");
        }
        if (size > 100 * 1024 * 1024) { // 100MB限制
            throw new IllegalArgumentException("File size exceeds maximum limit (100MB)");
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        InputContent that = (InputContent) obj;
        return size == that.size &&
               Objects.equals(fileName, that.fileName) &&
               Objects.deepEquals(content, that.content) &&
               Objects.equals(contentType, that.contentType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileName, Objects.hashCode(content), contentType, size);
    }

    @Override
    public String toString() {
        return String.format("InputContent{fileName='%s', contentType='%s', size=%s, modalityType=%s}", 
                           fileName, contentType, getFormattedSize(), modalityType.getCode());
    }
}