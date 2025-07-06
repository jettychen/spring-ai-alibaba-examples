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

package com.alibaba.cloud.ai.application.controller;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.alibaba.cloud.ai.application.annotation.UserIp;
import com.alibaba.cloud.ai.application.service.SAABaseService;
import com.alibaba.cloud.ai.application.service.SAAChatService;
import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import reactor.core.publisher.Flux;

import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * SAA聊天控制器，负责处理聊天相关的API请求
 * 
 * @author yuluo - 项目作者
 * @author <a href="mailto:yuluo08290126@gmail.com">yuluo</a> - 作者联系邮箱
 */

@RestController // 标识该类为REST控制器，返回JSON格式数据
@Tag(name = "Chat APIs") // Swagger标签，用于API文档分组
@RequestMapping("/api/v1") // 设置控制器的基础路径为/api/v1
public class SAAChatController {

	private final SAAChatService chatService; // 聊天服务实例，用于处理聊天业务逻辑

	private final SAABaseService baseService; // 基础服务实例，用于获取系统基础信息

	/**
	 * 构造函数，通过依赖注入初始化服务实例
	 * 
	 * @param chatService 聊天服务实例
	 * @param baseService 基础服务实例
	 */
	public SAAChatController(SAAChatService chatService, SAABaseService baseService) {
		this.chatService = chatService; // 初始化聊天服务
		this.baseService = baseService; // 初始化基础服务
	}

	/**
	 * 发送指定参数以获取模型响应的聊天接口
	 * 处理逻辑：
	 * 1. 当发送的提示为空时，返回错误消息
	 * 2. 当发送模型时，允许为空，当参数有值且在模型配置列表中时，调用相应模型。如果没有则返回错误。
	 *    如果模型参数为空，则设置默认模型 qwen-plus
	 * 3. chatId聊天记忆，由前端传递，类型为Object，不能重复
	 */
	@UserIp // 自定义注解，用于记录用户IP地址
	@PostMapping("/chat") // 映射POST请求到/chat端点
	@Operation(summary = "DashScope Flux Chat") // Swagger操作描述
	public Flux<String> chat(
			HttpServletResponse response, // HTTP响应对象
			@Validated @RequestBody String prompt, // 验证并获取请求体中的提示内容
			@RequestHeader(value = "model", required = false) String model, // 从请求头获取模型名称（可选）
			@RequestHeader(value = "chatId", required = false, defaultValue = "spring-ai-alibaba-playground-chat") String chatId // 从请求头获取聊天ID（可选，有默认值）
	) {

		// 获取可用的DashScope模型集合
		Set<Map<String, String>> dashScope = baseService.getDashScope();
		
		// 提取所有模型名称到列表中
		List<String> modelName = dashScope.stream()
				.flatMap(map -> map.keySet().stream().map(map::get)) // 将嵌套Map转换为扁平化流
				.distinct() // 去重
				.toList(); // 转换为列表

		// 验证模型参数
		if (StringUtils.hasText(model)) { // 如果模型参数不为空
			if (!modelName.contains(model)) { // 如果模型名称不在支持的列表中
				return Flux.just("Input model not support."); // 返回错误信息
			}
		}
		else {
			model = DashScopeApi.ChatModel.QWEN_PLUS.getModel(); // 设置默认模型为qwen-plus
		}

		response.setCharacterEncoding("UTF-8"); // 设置响应编码为UTF-8
		return chatService.chat(chatId, model, prompt); // 调用聊天服务并返回响应式流
	}

	/**
	 * 深度思考聊天接口，用于处理需要深度思考的聊天请求
	 * 
	 * @param response HTTP响应对象
	 * @param prompt 用户输入的提示内容
	 * @param model 模型名称（可选）
	 * @param chatId 聊天会话ID（可选，有默认值）
	 * @return 响应式字符串流
	 */
	@PostMapping("/deep-thinking/chat") // 映射POST请求到/deep-thinking/chat端点
	public Flux<String> deepThinkingChat(
			HttpServletResponse response, // HTTP响应对象
			@Validated @RequestBody String prompt, // 验证并获取请求体中的提示内容
			@RequestHeader(value = "model", required = false) String model, // 从请求头获取模型名称（可选）
			@RequestHeader(value = "chatId", required = false, defaultValue = "spring-ai-alibaba-playground-deepthink-chat") String chatId // 从请求头获取聊天ID（可选，有默认值）
	) {

		// 获取可用的DashScope模型集合
		Set<Map<String, String>> dashScope = baseService.getDashScope();
		
		// 提取所有模型名称到列表中
		List<String> modelName = dashScope.stream()
				.flatMap(map -> map.keySet().stream().map(map::get)) // 将嵌套Map转换为扁平化流
				.distinct() // 去重
				.toList(); // 转换为列表

		// 验证模型参数
		if (StringUtils.hasText(model)) { // 如果模型参数不为空
			if (!modelName.contains(model)) { // 如果模型名称不在支持的列表中
				return Flux.just("Input model not support."); // 返回错误信息
			}
		}
		else {
			model = DashScopeApi.ChatModel.QWEN_PLUS.getModel(); // 设置默认模型为qwen-plus
		}

		response.setCharacterEncoding("UTF-8"); // 设置响应编码为UTF-8
		return chatService.deepThinkingChat(chatId, model, prompt); // 调用深度思考聊天服务并返回响应式流
	}

}
