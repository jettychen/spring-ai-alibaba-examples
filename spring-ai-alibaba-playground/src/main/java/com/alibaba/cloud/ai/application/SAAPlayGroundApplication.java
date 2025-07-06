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

package com.alibaba.cloud.ai.application; // 定义包名，遵循阿里云AI应用的包结构

import org.slf4j.Logger; // 引入SLF4J日志记录器接口
import org.slf4j.LoggerFactory; // 引入SLF4J日志工厂类，用于创建日志记录器

import org.springframework.boot.SpringApplication; // 引入Spring Boot应用启动类
import org.springframework.boot.autoconfigure.SpringBootApplication; // 引入Spring Boot自动配置注解

/**
 * Spring AI Alibaba Playground应用的主启动类
 * 
 * @author yuluo - 项目作者
 * @author <a href="mailto:yuluo08290126@gmail.com">yuluo</a> - 作者联系邮箱
 */

@SpringBootApplication // Spring Boot应用启动注解，包含@Configuration、@EnableAutoConfiguration和@ComponentScan
public class SAAPlayGroundApplication {

	// 创建当前类的日志记录器，用于记录应用启动信息
	private static final Logger logger = LoggerFactory.getLogger(SAAPlayGroundApplication.class);

	/**
	 * 应用程序主入口方法
	 * 
	 * @param args 命令行参数数组
	 */
	public static void main(String[] args) {

		// 启动Spring Boot应用，传入主类和命令行参数
		SpringApplication.run(SAAPlayGroundApplication.class, args);
		
		// 记录应用启动成功信息，包含端口号和访问地址
		logger.info(
				"SAAPlayGroundApplication started successfully. port: {}, swagger: {}, frontend: {}",
				8080, // 应用默认端口号
				"http://localhost:8080/doc.html", // Swagger API文档访问地址
				"http://localhost:8080" // 前端页面访问地址
		);
	}

}
