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

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.cloud.ai.application.annotation.UserIp;
import com.alibaba.cloud.ai.application.entity.mcp.McpServer;
import com.alibaba.cloud.ai.application.entity.result.Result;
import com.alibaba.cloud.ai.application.entity.tools.ToolCallResp;
import com.alibaba.cloud.ai.application.exception.SAAAppException;
import com.alibaba.cloud.ai.application.mcp.McpServerContainer;
import com.alibaba.cloud.ai.application.service.SAAMcpService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * SAA MCP（模型上下文协议）控制器，负责处理MCP相关的API请求
 * 
 * @author brianxiadong - 项目作者
 */

@RestController
@Tag(name = "MCP APIs")
@RequestMapping("/api/v1")
public class SAAMcpController {

	private final SAAMcpService mcpService;

	/**
	 * 构造函数，通过依赖注入初始化MCP服务实例
	 * 
	 * @param webSearch MCP服务实例（参数名为webSearch但实际是mcpService）
	 */
	public SAAMcpController(SAAMcpService webSearch) {
		this.mcpService = webSearch;
	}

	/**
	 * 内部MCP聊天接口
	 * 注意：内部接口不应该直接被 web 请求！
	 * 
	 * @param prompt 用户输入的提示内容
	 * @return 包含工具调用响应的统一返回结果
	 */
	@UserIp
	@GetMapping("/inner/mcp")
	@Operation(summary = "DashScope MCP Chat")
	public Result<ToolCallResp> chat(
			@Validated @RequestParam("prompt") String prompt
	) {

		return Result.success(mcpService.chat(prompt));
	}

	/**
	 * 获取MCP服务器列表接口
	 * 
	 * @return 包含MCP服务器列表的统一返回结果
	 */
	@UserIp
	@GetMapping("/mcp-list")
	@Operation(summary = "MCP List")
	public Result<List<McpServer>> mcpList() {

		return Result.success(McpServerContainer.getAllServers());
	}

	/**
	 * 运行MCP服务接口
	 * 
	 * @param id MCP服务器ID
	 * @param prompt 用户输入的提示内容
	 * @param envs 环境变量字符串（可选，格式：key1=value1,key2=value2）
	 * @return 包含工具调用响应的统一返回结果
	 */
	@UserIp
	@PostMapping("/mcp-run")
	@Operation(summary = "MCP Run")
	public Result<ToolCallResp> mcpRun(
			@Validated @RequestParam("id") String id,
			@Validated @RequestParam("prompt") String prompt,
			@RequestParam(value = "envs", required = false) String envs
	) {

		Map<String, String> env = new HashMap<>();
		if (StringUtils.hasText(envs)) {
			for (String entry : envs.split(",")) {
				String[] keyValue = entry.split("=");
				if (keyValue.length == 2) {
					env.put(keyValue[0], keyValue[1]);
				}
			}
		}

		try {
			return Result.success(mcpService.run(id, env, prompt));
		}
		catch (IOException e) {
			throw new SAAAppException(e.getMessage());
		}
	}
}

