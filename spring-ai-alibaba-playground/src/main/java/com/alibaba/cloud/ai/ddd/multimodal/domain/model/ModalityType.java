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

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 模态类型
 * 值对象，表示多模态处理中的不同数据模态
 */
public class ModalityType extends ValueObject {

    // 预定义的模态类型
    public static final ModalityType TEXT = new ModalityType("TEXT", "文本", Set.of("txt", "md"), true, true);
    public static final ModalityType IMAGE = new ModalityType("IMAGE", "图像", Set.of("jpg", "jpeg", "png", "gif", "bmp", "webp"), true, true);
    public static final ModalityType AUDIO = new ModalityType("AUDIO", "音频", Set.of("mp3", "wav", "m4a", "aac", "flac"), true, true);
    public static final ModalityType VIDEO = new ModalityType("VIDEO", "视频", Set.of("mp4", "avi", "mov", "wmv", "flv", "mkv"), true, false);
    public static final ModalityType DOCUMENT = new ModalityType("DOCUMENT", "文档", Set.of("pdf", "doc", "docx", "ppt", "pptx", "xls", "xlsx"), true, false);

    private final String code;
    private final String displayName;
    private final Set<String> supportedExtensions;
    private final boolean inputSupported;
    private final boolean outputSupported;

    private ModalityType(String code, String displayName, Set<String> supportedExtensions, 
                        boolean inputSupported, boolean outputSupported) {
        this.code = Objects.requireNonNull(code, "Modality code cannot be null");
        this.displayName = Objects.requireNonNull(displayName, "Display name cannot be null");
        this.supportedExtensions = Set.copyOf(supportedExtensions);
        this.inputSupported = inputSupported;
        this.outputSupported = outputSupported;
        validate();
    }

    /**
     * 创建自定义模态类型
     *
     * @param code 模态代码
     * @param displayName 显示名称
     * @param supportedExtensions 支持的文件扩展名
     * @param inputSupported 是否支持输入
     * @param outputSupported 是否支持输出
     * @return 模态类型
     */
    public static ModalityType custom(String code, String displayName, Set<String> supportedExtensions,
                                     boolean inputSupported, boolean outputSupported) {
        return new ModalityType(code, displayName, supportedExtensions, inputSupported, outputSupported);
    }

    /**
     * 根据代码获取预定义的模态类型
     *
     * @param code 模态代码
     * @return 模态类型
     */
    public static ModalityType fromCode(String code) {
        return Arrays.stream(getPredefinedTypes())
                .filter(type -> type.getCode().equalsIgnoreCase(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown modality type: " + code));
    }

    /**
     * 根据文件扩展名推断模态类型
     *
     * @param extension 文件扩展名
     * @return 模态类型
     */
    public static ModalityType inferFromExtension(String extension) {
        if (extension == null || extension.trim().isEmpty()) {
            return TEXT;
        }
        
        String normalizedExt = extension.toLowerCase().replaceFirst("^\\.", "");
        return Arrays.stream(getPredefinedTypes())
                .filter(type -> type.getSupportedExtensions().contains(normalizedExt))
                .findFirst()
                .orElse(TEXT);
    }

    /**
     * 获取所有预定义的模态类型
     *
     * @return 预定义模态类型数组
     */
    public static ModalityType[] getPredefinedTypes() {
        return new ModalityType[]{TEXT, IMAGE, AUDIO, VIDEO, DOCUMENT};
    }

    /**
     * 获取支持输入的模态类型
     *
     * @return 支持输入的模态类型集合
     */
    public static Set<ModalityType> getInputSupportedTypes() {
        return Arrays.stream(getPredefinedTypes())
                .filter(ModalityType::isInputSupported)
                .collect(Collectors.toSet());
    }

    /**
     * 获取支持输出的模态类型
     *
     * @return 支持输出的模态类型集合
     */
    public static Set<ModalityType> getOutputSupportedTypes() {
        return Arrays.stream(getPredefinedTypes())
                .filter(ModalityType::isOutputSupported)
                .collect(Collectors.toSet());
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

    public boolean isInputSupported() {
        return inputSupported;
    }

    public boolean isOutputSupported() {
        return outputSupported;
    }

    /**
     * 检查是否支持指定的文件扩展名
     *
     * @param extension 文件扩展名
     * @return 是否支持
     */
    public boolean supportsExtension(String extension) {
        if (extension == null) return false;
        String normalizedExt = extension.toLowerCase().replaceFirst("^\\.", "");
        return supportedExtensions.contains(normalizedExt);
    }

    @Override
    protected void validate() {
        if (code.trim().isEmpty()) {
            throw new IllegalArgumentException("Modality code cannot be empty");
        }
        if (displayName.trim().isEmpty()) {
            throw new IllegalArgumentException("Display name cannot be empty");
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ModalityType that = (ModalityType) obj;
        return Objects.equals(code, that.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }

    @Override
    public String toString() {
        return code;
    }
}