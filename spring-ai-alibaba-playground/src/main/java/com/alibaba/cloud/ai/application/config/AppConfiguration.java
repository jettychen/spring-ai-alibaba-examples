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

package com.alibaba.cloud.ai.application.config; // 定义配置包路径

//import com.alibaba.cloud.ai.memory.jdbc.SQLiteChatMemory; // 注释掉的SQLite聊天记忆导入

import com.alibaba.cloud.ai.memory.jdbc.SQLiteChatMemoryRepository; // 引入SQLite聊天记忆仓库
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor; // 引入消息聊天记忆顾问
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor; // 引入简单日志顾问
import org.springframework.ai.chat.memory.ChatMemory; // 引入聊天记忆接口
import org.springframework.ai.chat.memory.MessageWindowChatMemory; // 引入消息窗口聊天记忆
import org.springframework.ai.model.tool.ToolCallingManager; // 引入工具调用管理器
import org.springframework.context.annotation.Bean; // 引入Spring Bean注解
import org.springframework.context.annotation.Configuration; // 引入Spring配置注解
import org.springframework.jdbc.core.JdbcTemplate; // 引入JDBC模板

/**
 * 应用全局配置类，负责配置Chat Memory Bean和SimpleLoggerAdvisor等核心组件
 * 
 * @author yuluo - 项目作者
 * @author <a href="mailto:yuluo08290126@gmail.com">yuluo</a> - 作者联系邮箱
 *
 * 全局统一管理 ChatMemory Bean 和 SimpleLoggerAdvisor
 */

@Configuration // 标识该类为Spring配置类
public class AppConfiguration {

	//TODO SQLiteChatMemory待验证
	/**
	 * 配置SQLite聊天记忆Bean
	 * 
	 * @param jdbcTemplate JDBC模板，用于数据库操作
	 * @return ChatMemory 聊天记忆实例
	 */
	@Bean // 标识该方法返回一个Spring Bean
	public ChatMemory SQLiteChatMemory(JdbcTemplate jdbcTemplate) { // 参数通过Spring依赖注入
		return MessageWindowChatMemory.builder() // 使用消息窗口聊天记忆构建器
				.chatMemoryRepository(SQLiteChatMemoryRepository.sqliteBuilder() // 设置SQLite聊天记忆仓库
						.jdbcTemplate(jdbcTemplate) // 设置JDBC模板
						.build()) // 构建聊天记忆仓库
				.build(); // 构建聊天记忆实例
	}

//	/**
//	 * 备用的SQLite聊天记忆配置（被注释掉）
//	 * 
//	 * @return ChatMemory 聊天记忆实例
//	 */
//	@Bean
//	public ChatMemory SQLiteChatMemory() {
//
//		return new SQLiteChatMemory(
//				null,
//				null,
//				"jdbc:sqlite:src/main/resources/db/saa.db"
//		);
//	}

	/**
	 * 配置简单日志顾问Bean
	 * 
	 * @return SimpleLoggerAdvisor 简单日志顾问实例
	 */
	@Bean // 标识该方法返回一个Spring Bean
	public SimpleLoggerAdvisor simpleLoggerAdvisor() {

		return new SimpleLoggerAdvisor(100); // 创建简单日志顾问，参数100表示日志级别或限制
	}

	/**
	 * 配置消息聊天记忆顾问Bean
	 * 
	 * @param sqLiteChatMemory SQLite聊天记忆实例
	 * @return MessageChatMemoryAdvisor 消息聊天记忆顾问实例
	 */
	@Bean // 标识该方法返回一个Spring Bean
	public MessageChatMemoryAdvisor messageChatMemoryAdvisor(
			ChatMemory sqLiteChatMemory // 通过Spring依赖注入获取聊天记忆实例
	) {
		return MessageChatMemoryAdvisor.builder(sqLiteChatMemory).build(); // 使用聊天记忆构建消息聊天记忆顾问
	}

	/**
	 * 配置工具调用管理器Bean
	 * 
	 * @return ToolCallingManager 工具调用管理器实例
	 */
	@Bean // 标识该方法返回一个Spring Bean
	public ToolCallingManager toolCallingManager() {

		return ToolCallingManager.builder().build(); // 构建工具调用管理器
	}

}
