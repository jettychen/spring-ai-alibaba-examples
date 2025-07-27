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
 * 处理提示词
 * 值对象，表示用户输入的处理指令或提示
 */
public class ProcessingPrompt extends ValueObject {

    private final String content;
    private final String language;

    private ProcessingPrompt(String content, String language) {
        this.content = Objects.requireNonNull(content, "Prompt content cannot be null");
        this.language = language != null ? language : "zh-CN";
        validate();
    }

    /**
     * 创建处理提示词
     *
     * @param content 提示内容
     * @return 处理提示词
     */
    public static ProcessingPrompt of(String content) {
        return new ProcessingPrompt(content, null);
    }

    /**
     * 创建带语言的处理提示词
     *
     * @param content 提示内容
     * @param language 语言代码
     * @return 处理提示词
     */
    public static ProcessingPrompt of(String content, String language) {
        return new ProcessingPrompt(content, language);
    }

    /**
     * 创建默认提示词
     *
     * @param modalityType 模态类型
     * @return 默认提示词
     */
    public static ProcessingPrompt defaultFor(ModalityType modalityType) {
        String defaultContent = switch (modalityType.getCode()) {
            case "IMAGE" -> "请总结图片内容";
            case "AUDIO" -> "请转录音频内容";
            case "VIDEO" -> "请总结这个视频的主要内容";
            case "DOCUMENT" -> "请总结文档内容";
            default -> "请处理输入内容";
        };
        return new ProcessingPrompt(defaultContent, "zh-CN");
    }

    public String getContent() {
        return content;
    }

    public String getLanguage() {
        return language;
    }

    /**
     * 获取提示词长度
     *
     * @return 字符长度
     */
    public int length() {
        return content.length();
    }

    /**
     * 检查是否为空提示词
     *
     * @return 是否为空
     */
    public boolean isEmpty() {
        return content.trim().isEmpty();
    }

    /**
     * 检查是否为默认提示词
     *
     * @return 是否为默认提示词
     */
    public boolean isDefault() {
        return content.startsWith("请");
    }

    /**
     * 创建优化后的提示词
     *
     * @param optimization 优化后缀
     * @return 优化后的提示词
     */
    public ProcessingPrompt withOptimization(String optimization) {
        String optimizedContent = content + " " + optimization;
        return new ProcessingPrompt(optimizedContent, language);
    }

    @Override
    protected void validate() {
        if (content.trim().isEmpty()) {
            throw new IllegalArgumentException("Prompt content cannot be empty");
        }
        if (content.length() > 10000) {
            throw new IllegalArgumentException("Prompt content too long (max 10000 characters)");
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ProcessingPrompt that = (ProcessingPrompt) obj;
        return Objects.equals(content, that.content) && 
               Objects.equals(language, that.language);
    }

    @Override
    public int hashCode() {
        return Objects.hash(content, language);
    }

    @Override
    public String toString() {
        return String.format("ProcessingPrompt{content='%s', language='%s'}", 
                           content.length() > 50 ? content.substring(0, 50) + "..." : content, 
                           language);
    }
}