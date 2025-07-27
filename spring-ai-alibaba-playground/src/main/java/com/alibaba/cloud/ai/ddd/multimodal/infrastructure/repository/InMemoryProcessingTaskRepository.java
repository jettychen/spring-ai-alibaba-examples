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

package com.alibaba.cloud.ai.ddd.multimodal.infrastructure.repository;

import com.alibaba.cloud.ai.ddd.multimodal.domain.model.ProcessingTask;
import com.alibaba.cloud.ai.ddd.multimodal.domain.model.ProcessingTaskId;
import com.alibaba.cloud.ai.ddd.multimodal.domain.model.ProcessingStatus;
import com.alibaba.cloud.ai.ddd.multimodal.domain.model.ModalityType;
import com.alibaba.cloud.ai.ddd.multimodal.domain.repository.ProcessingTaskRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 内存中的处理任务仓储实现
 * 用于演示和测试，生产环境应使用持久化存储
 */
@Repository
public class InMemoryProcessingTaskRepository implements ProcessingTaskRepository {

    private final Map<ProcessingTaskId, ProcessingTask> tasks = new ConcurrentHashMap<>();

    @Override
    public Optional<ProcessingTask> findById(ProcessingTaskId id) {
        return Optional.ofNullable(tasks.get(id));
    }

    @Override
    public List<ProcessingTask> findAll() {
        return List.copyOf(tasks.values());
    }

    @Override
    public ProcessingTask save(ProcessingTask entity) {
        tasks.put(entity.getId(), entity);
        return entity;
    }

    @Override
    public void delete(ProcessingTask entity) {
        tasks.remove(entity.getId());
    }

    @Override
    public void deleteById(ProcessingTaskId id) {
        tasks.remove(id);
    }

    @Override
    public boolean existsById(ProcessingTaskId id) {
        return tasks.containsKey(id);
    }

    @Override
    public long count() {
        return tasks.size();
    }

    @Override
    public List<ProcessingTask> findByUserId(String userId) {
        return tasks.values().stream()
                .filter(task -> userId.equals(task.getUserId()))
                .sorted(Comparator.comparing(ProcessingTask::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public List<ProcessingTask> findByStatus(ProcessingStatus status) {
        return tasks.values().stream()
                .filter(task -> task.getStatus() == status)
                .sorted(Comparator.comparing(ProcessingTask::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public List<ProcessingTask> findByModalityTypes(ModalityType inputModality, ModalityType outputModality) {
        return tasks.values().stream()
                .filter(task -> task.getInputModality().equals(inputModality) && 
                               task.getOutputModality().equals(outputModality))
                .sorted(Comparator.comparing(ProcessingTask::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public List<ProcessingTask> findByCreatedAtBetween(LocalDateTime startTime, LocalDateTime endTime) {
        return tasks.values().stream()
                .filter(task -> !task.getCreatedAt().isBefore(startTime) && 
                               !task.getCreatedAt().isAfter(endTime))
                .sorted(Comparator.comparing(ProcessingTask::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public List<ProcessingTask> findPendingTasksByPriority(int maxPriority, int limit) {
        return tasks.values().stream()
                .filter(task -> task.getStatus() == ProcessingStatus.PENDING)
                .filter(task -> task.getPriority() <= maxPriority)
                .sorted(Comparator.comparingInt(ProcessingTask::getPriority)
                        .thenComparing(ProcessingTask::getCreatedAt))
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProcessingTask> findTimeoutProcessingTasks(int timeoutMinutes) {
        LocalDateTime timeoutThreshold = LocalDateTime.now().minusMinutes(timeoutMinutes);
        return tasks.values().stream()
                .filter(task -> task.getStatus() == ProcessingStatus.PROCESSING)
                .filter(task -> task.getCreatedAt().isBefore(timeoutThreshold))
                .collect(Collectors.toList());
    }

    @Override
    public List<ProcessingTask> findByUserIdAndStatus(String userId, ProcessingStatus status) {
        return tasks.values().stream()
                .filter(task -> userId.equals(task.getUserId()))
                .filter(task -> task.getStatus() == status)
                .sorted(Comparator.comparing(ProcessingTask::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public long countByStatus(ProcessingStatus status) {
        return tasks.values().stream()
                .filter(task -> task.getStatus() == status)
                .count();
    }

    @Override
    public long countByUserId(String userId) {
        return tasks.values().stream()
                .filter(task -> userId.equals(task.getUserId()))
                .count();
    }

    @Override
    public long deleteCompletedTasksBefore(LocalDateTime beforeTime) {
        List<ProcessingTaskId> toDelete = tasks.values().stream()
                .filter(task -> task.getStatus() == ProcessingStatus.COMPLETED)
                .filter(task -> task.getCompletedAt() != null && 
                               task.getCompletedAt().isBefore(beforeTime))
                .map(ProcessingTask::getId)
                .collect(Collectors.toList());
        
        toDelete.forEach(tasks::remove);
        return toDelete.size();
    }
}