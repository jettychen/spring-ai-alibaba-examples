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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 实体基类
 * 提供实体的基本特征：唯一标识、领域事件管理
 *
 * @param <ID> 实体标识类型
 */
public abstract class Entity<ID> {

    private final List<DomainEvent> domainEvents = new ArrayList<>();

    /**
     * 获取实体唯一标识
     *
     * @return 实体标识
     */
    public abstract ID getId();

    /**
     * 添加领域事件
     *
     * @param event 领域事件
     */
    protected void addDomainEvent(DomainEvent event) {
        this.domainEvents.add(event);
    }

    /**
     * 获取所有领域事件
     *
     * @return 领域事件列表
     */
    public List<DomainEvent> getDomainEvents() {
        return List.copyOf(domainEvents);
    }

    /**
     * 清空领域事件
     */
    public void clearDomainEvents() {
        this.domainEvents.clear();
    }

    /**
     * 实体相等性比较基于唯一标识
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        
        Entity<?> entity = (Entity<?>) obj;
        return Objects.equals(getId(), entity.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

    @Override
    public String toString() {
        return String.format("%s{id=%s}", getClass().getSimpleName(), getId());
    }
}