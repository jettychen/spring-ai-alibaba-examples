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

package com.alibaba.cloud.ai.ddd.shared.domain;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 领域事件基类
 * 领域事件表示在领域中发生的有业务意义的事情
 */
public abstract class DomainEvent {

    private final String eventId;
    private final LocalDateTime occurredOn;
    private final String eventType;

    protected DomainEvent() {
        this.eventId = UUID.randomUUID().toString();
        this.occurredOn = LocalDateTime.now();
        this.eventType = this.getClass().getSimpleName();
    }

    /**
     * 获取事件唯一标识
     *
     * @return 事件ID
     */
    public String getEventId() {
        return eventId;
    }

    /**
     * 获取事件发生时间
     *
     * @return 发生时间
     */
    public LocalDateTime getOccurredOn() {
        return occurredOn;
    }

    /**
     * 获取事件类型
     *
     * @return 事件类型
     */
    public String getEventType() {
        return eventType;
    }

    /**
     * 获取事件版本，用于事件演化
     *
     * @return 事件版本
     */
    public int getVersion() {
        return 1;
    }

    @Override
    public String toString() {
        return String.format("%s{eventId='%s', occurredOn=%s}", 
                            eventType, eventId, occurredOn);
    }
}