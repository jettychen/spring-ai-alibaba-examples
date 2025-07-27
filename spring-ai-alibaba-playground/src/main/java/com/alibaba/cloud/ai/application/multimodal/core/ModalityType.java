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

package com.alibaba.cloud.ai.application.multimodal.core;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 多模态类型枚举
 * 定义系统支持的所有模态类型及其特征
 */
public enum ModalityType {
    
    TEXT("text", "文本", Set.of("txt", "md"), "text/plain", true, true),
    IMAGE("image", "图像", Set.of("jpg", "jpeg", "png", "gif", "bmp", "webp"), "image/*", true, true),
    AUDIO("audio", "音频", Set.of("mp3", "wav", "m4a", "aac", "flac"), "audio/*", true, true),
    VIDEO("video", "视频", Set.of("mp4", "avi", "mov", "wmv", "flv", "mkv"), "video/*", true, false),
    DOCUMENT("document", "文档", Set.of("pdf", "doc", "docx", "ppt", "pptx", "xls", "xlsx"), "application/*", true, false),
    MULTIMODAL("multimodal", "多模态组合", Set.of(), "*/*", true, true);

    private final String code;
    private final String displayName;
    private final Set<String> supportedExtensions;
    private final String mimeType;
    private final boolean inputSupported;
    private final boolean outputSupported;

    ModalityType(String code, String displayName, Set<String> supportedExtensions, 
                String mimeType, boolean inputSupported, boolean outputSupported) {
        this.code = code;
        this.displayName = displayName;
        this.supportedExtensions = supportedExtensions;
        this.mimeType = mimeType;
        this.inputSupported = inputSupported;
        this.outputSupported = outputSupported;
    }

    public String getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Set<String> getSupportedExtensions() {
        return supportedExtensions;
    }

    public String getMimeType() {
        return mimeType;
    }

    public boolean isInputSupported() {
        return inputSupported;
    }

    public boolean isOutputSupported() {
        return outputSupported;
    }

    /**
     * 根据文件扩展名判断模态类型
     * @param extension 文件扩展名
     * @return 模态类型
     */
    public static ModalityType fromExtension(String extension) {
        if (extension == null || extension.trim().isEmpty()) {
            return TEXT;
        }
        
        String lowerExt = extension.toLowerCase().replaceFirst("^\\.", "");
        return Arrays.stream(values())
                .filter(type -> type.getSupportedExtensions().contains(lowerExt))
                .findFirst()
                .orElse(TEXT);
    }

    /**
     * 根据MIME类型判断模态类型
     * @param mimeType MIME类型
     * @return 模态类型
     */
    public static ModalityType fromMimeType(String mimeType) {
        if (mimeType == null || mimeType.trim().isEmpty()) {
            return TEXT;
        }
        
        String lowerMime = mimeType.toLowerCase();
        return Arrays.stream(values())
                .filter(type -> lowerMime.startsWith(type.getMimeType().replace("*", "")))
                .findFirst()
                .orElse(TEXT);
    }

    /**
     * 获取所有支持输入的模态类型
     * @return 支持输入的模态类型集合
     */
    public static Set<ModalityType> getInputSupportedTypes() {
        return Arrays.stream(values())
                .filter(ModalityType::isInputSupported)
                .collect(Collectors.toSet());
    }

    /**
     * 获取所有支持输出的模态类型
     * @return 支持输出的模态类型集合
     */
    public static Set<ModalityType> getOutputSupportedTypes() {
        return Arrays.stream(values())
                .filter(ModalityType::isOutputSupported)
                .collect(Collectors.toSet());
    }
}