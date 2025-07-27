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
import java.util.UUID;

/**
 * 处理任务标识符
 * 值对象，唯一标识一个多模态处理任务
 */
public class ProcessingTaskId extends ValueObject {

    private final String value;

    private ProcessingTaskId(String value) {
        this.value = Objects.requireNonNull(value, "ProcessingTaskId value cannot be null");
        validate();
    }

    /**
     * 生成新的任务标识
     *
     * @return 新的任务标识
     */
    public static ProcessingTaskId generate() {
        return new ProcessingTaskId(UUID.randomUUID().toString());
    }

    /**
     * 从字符串创建任务标识
     *
     * @param value 标识值
     * @return 任务标识
     */
    public static ProcessingTaskId of(String value) {
        return new ProcessingTaskId(value);
    }

    /**
     * 获取标识值
     *
     * @return 标识值
     */
    public String getValue() {
        return value;
    }

    @Override
    protected void validate() {
        if (value.trim().isEmpty()) {
            throw new IllegalArgumentException("ProcessingTaskId value cannot be empty");
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ProcessingTaskId that = (ProcessingTaskId) obj;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}