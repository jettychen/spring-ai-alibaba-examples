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

/**
 * 值对象基类
 * 值对象的特征：
 * 1. 不可变性 - 创建后不能修改
 * 2. 相等性 - 基于值比较而非标识
 * 3. 可替换性 - 可以用相同值的对象替换
 */
public abstract class ValueObject {

    /**
     * 值对象相等性比较
     * 子类应重写此方法，基于所有属性值进行比较
     */
    @Override
    public abstract boolean equals(Object obj);

    /**
     * 哈希码计算
     * 子类应重写此方法，基于所有参与equals比较的属性
     */
    @Override
    public abstract int hashCode();

    /**
     * 字符串表示
     * 子类应重写此方法，提供有意义的字符串表示
     */
    @Override
    public abstract String toString();

    /**
     * 验证值对象的有效性
     * 子类可重写此方法进行业务规则验证
     * 
     * @throws IllegalArgumentException 当值对象无效时抛出
     */
    protected void validate() {
        // 默认无验证，子类可重写
    }
}