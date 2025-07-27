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

import java.util.List;
import java.util.Optional;

/**
 * 仓储接口基类
 * 提供聚合根的基本持久化操作
 *
 * @param <T> 聚合根类型
 * @param <ID> 聚合根标识类型
 */
public interface Repository<T extends Entity<ID>, ID> {

    /**
     * 根据ID查找聚合根
     *
     * @param id 聚合根标识
     * @return 聚合根实例，如果不存在则返回空
     */
    Optional<T> findById(ID id);

    /**
     * 查找所有聚合根
     *
     * @return 所有聚合根列表
     */
    List<T> findAll();

    /**
     * 保存聚合根
     *
     * @param entity 要保存的聚合根
     * @return 保存后的聚合根
     */
    T save(T entity);

    /**
     * 删除聚合根
     *
     * @param entity 要删除的聚合根
     */
    void delete(T entity);

    /**
     * 根据ID删除聚合根
     *
     * @param id 聚合根标识
     */
    void deleteById(ID id);

    /**
     * 检查聚合根是否存在
     *
     * @param id 聚合根标识
     * @return 如果存在返回true，否则返回false
     */
    boolean existsById(ID id);

    /**
     * 获取聚合根总数
     *
     * @return 总数
     */
    long count();
}