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

package com.alibaba.cloud.ai.ddd.multimodal.domain.event;

import com.alibaba.cloud.ai.ddd.shared.domain.DomainEvent;
import com.alibaba.cloud.ai.ddd.multimodal.domain.model.ProcessingTaskId;
import com.alibaba.cloud.ai.ddd.multimodal.domain.model.ModalityType;

/**
 * 处理任务创建事件
 * 当新的处理任务被创建时发布
 */
public class ProcessingTaskCreated extends DomainEvent {

    private final ProcessingTaskId taskId;
    private final ModalityType inputModality;
    private final ModalityType outputModality;

    public ProcessingTaskCreated(ProcessingTaskId taskId, ModalityType inputModality, ModalityType outputModality) {
        super();
        this.taskId = taskId;
        this.inputModality = inputModality;
        this.outputModality = outputModality;
    }

    public ProcessingTaskId getTaskId() {
        return taskId;
    }

    public ModalityType getInputModality() {
        return inputModality;
    }

    public ModalityType getOutputModality() {
        return outputModality;
    }

    @Override
    public String toString() {
        return String.format("ProcessingTaskCreated{taskId=%s, inputModality=%s, outputModality=%s, occurredOn=%s}", 
                           taskId, inputModality.getCode(), outputModality.getCode(), getOccurredOn());
    }
}