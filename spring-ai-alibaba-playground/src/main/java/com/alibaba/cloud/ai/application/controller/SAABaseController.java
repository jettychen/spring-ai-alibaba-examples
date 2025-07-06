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

package com.alibaba.cloud.ai.application.controller; // 定义控制器包路径

import java.util.Map; // 引入Java映射工具类
import java.util.Set; // 引入Java集合工具类

import com.alibaba.cloud.ai.application.entity.result.Result; // 引入统一返回结果类
import com.alibaba.cloud.ai.application.service.SAABaseService; // 引入基础服务接口
import io.swagger.v3.oas.annotations.tags.Tag; // 引入Swagger标签注解

import org.springframework.web.bind.annotation.GetMapping; // 引入Spring GET映射注解
import org.springframework.web.bind.annotation.RequestMapping; // 引入Spring请求映射注解
import org.springframework.web.bind.annotation.RestController; // 引入Spring REST控制器注解

/**
 * SAA基础控制器，负责处理系统基础信息相关的API请求
 * 
 * @author yuluo - 项目作者
 * @author <a href="mailto:yuluo08290126@gmail.com">yuluo</a> - 作者联系邮箱
 */

@RestController // 标识该类为REST控制器，返回JSON格式数据
@Tag(name = "Base APIs") // Swagger标签，用于API文档分组
@RequestMapping("/api/v1") // 设置控制器的基础路径为/api/v1
public class SAABaseController {

	private final SAABaseService baseService; // 基础服务实例，用于获取系统基础信息

	/**
	 * 构造函数，通过依赖注入初始化基础服务实例
	 * 
	 * @param baseService 基础服务实例
	 */
	public SAABaseController(SAABaseService baseService) {
		this.baseService = baseService; // 初始化基础服务
	}

	/**
	 * 获取DashScope可用模型列表的API接口
	 * 
	 * @return 包含模型信息的统一返回结果
	 */
	@GetMapping("/dashscope/getModels") // 映射GET请求到/dashscope/getModels端点
	public Result<Set<Map<String, String>>> getDashScopeModels() {

		Set<Map<String, String>> dashScope = baseService.getDashScope(); // 调用基础服务获取DashScope模型集合

		if (dashScope.isEmpty()) { // 如果模型集合为空
			return Result.failed("No DashScope models found"); // 返回失败结果
		}

		return Result.success(dashScope); // 返回成功结果，包含模型集合
	}

	/**
	 * 健康检查API接口，用于检查应用程序是否正常运行
	 * 
	 * @return 包含健康状态信息的统一返回结果
	 */
	@GetMapping("/health") // 映射GET请求到/health端点
	public Result<String> health() {

		return Result.success("Spring AI Alibaba Playground is running......"); // 返回应用程序运行状态信息
	}

}
