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

package com.alibaba.cloud.ai.ddd.multimodal.application.command;

import java.util.Objects;

/**
 * 处理任务命令
 * 触发处理任务的执行
 */
public class ProcessTaskCommand {

    private final String taskId;
    private final boolean streaming;

    public ProcessTaskCommand(String taskId, boolean streaming) {
        this.taskId = Objects.requireNonNull(taskId, "Task ID cannot be null");
        this.streaming = streaming;
    }

    public String getTaskId() {
        return taskId;
    }

    public boolean isStreaming() {
        return streaming;
    }

    @Override
    public String toString() {
        return String.format("ProcessTaskCommand{taskId='%s', streaming=%s}", taskId, streaming);
    }
}