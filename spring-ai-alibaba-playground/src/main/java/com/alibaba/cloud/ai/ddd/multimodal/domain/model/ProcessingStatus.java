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

/**
 * 处理状态枚举
 * 表示处理任务的生命周期状态
 */
public enum ProcessingStatus {
    
    /**
     * 待处理状态
     * 任务已创建，等待开始处理
     */
    PENDING("pending", "待处理"),
    
    /**
     * 处理中状态
     * 任务正在被处理器处理
     */
    PROCESSING("processing", "处理中"),
    
    /**
     * 已完成状态
     * 任务成功完成处理
     */
    COMPLETED("completed", "已完成"),
    
    /**
     * 失败状态
     * 任务处理失败
     */
    FAILED("failed", "失败"),
    
    /**
     * 已取消状态
     * 任务被用户或系统取消
     */
    CANCELLED("cancelled", "已取消");

    private final String code;
    private final String displayName;

    ProcessingStatus(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    public String getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * 根据代码获取状态
     *
     * @param code 状态代码
     * @return 处理状态
     */
    public static ProcessingStatus fromCode(String code) {
        for (ProcessingStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown processing status code: " + code);
    }

    /**
     * 检查是否为终端状态（不会再改变）
     *
     * @return 是否为终端状态
     */
    public boolean isTerminal() {
        return this == COMPLETED || this == FAILED || this == CANCELLED;
    }

    /**
     * 检查是否为活跃状态（还在处理中）
     *
     * @return 是否为活跃状态
     */
    public boolean isActive() {
        return this == PENDING || this == PROCESSING;
    }

    /**
     * 检查是否为成功状态
     *
     * @return 是否为成功状态
     */
    public boolean isSuccessful() {
        return this == COMPLETED;
    }

    @Override
    public String toString() {
        return code;
    }
}