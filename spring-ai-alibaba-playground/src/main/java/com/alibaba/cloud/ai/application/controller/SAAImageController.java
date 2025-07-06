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

import com.alibaba.cloud.ai.application.annotation.UserIp;
import com.alibaba.cloud.ai.application.entity.result.Result;
import com.alibaba.cloud.ai.application.service.SAAImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import reactor.core.publisher.Flux;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * SAA图像控制器，负责处理图像相关的API请求
 * 
 * @author yuluo - 项目作者
 * @author <a href="mailto:yuluo08290126@gmail.com">yuluo</a> - 作者联系邮箱
 */

@RestController
@Tag(name = "Image APIs")
@RequestMapping("/api/v1")
public class SAAImageController {

	private static final String DEFAULT_IMAGE_STYLE = "摄影写实";

	private final SAAImageService imageService;

	/**
	 * 构造函数，通过依赖注入初始化图像服务实例
	 * 
	 * @param imageService 图像服务实例
	 */
	public SAAImageController(SAAImageService imageService) {
		this.imageService = imageService;
	}

	/**
	 * 图像识别接口，将图像转换为文本描述
	 * 注意：prompt 参数可以为空
	 * 
	 * @param prompt 用户输入的提示内容（可选，有默认值）
	 * @param image 上传的图像文件
	 * @return 响应式字符串流，包含图像识别结果
	 */
	@UserIp
	@PostMapping("/image2text")
	@Operation(summary = "DashScope Image Recognition")
	public Flux<String> image2text(
			@Validated @RequestParam(value = "prompt", required = false, defaultValue = "请总结图片内容") String prompt,
			@Validated @RequestParam("image") MultipartFile image
	) {

		if (image.isEmpty()) {
			return Flux.just("No image file provided");
		}

		Flux<String> res;
		try {
			 res = imageService.image2Text(prompt, image);
		} catch (Exception e) {
			return Flux.just(e.getMessage());
		}

		return res;
	}

	/**
	 * 文本到图像生成接口，根据文本提示生成图像
	 * 
	 * @param response HTTP响应对象
	 * @param prompt 用户输入的文本提示
	 * @param style 图像生成风格（可选，有默认值）
	 * @param resolution 图像分辨率（可选，有默认值）
	 * @return 统一返回结果，表示操作成功
	 */
	@UserIp
	@GetMapping("/text2image")
	@Operation(summary = "DashScope Image Generation")
	public Result<Void> text2Image(
			HttpServletResponse response,
			@Validated @RequestParam("prompt") String prompt,
			@RequestParam(value = "style", required = false, defaultValue = DEFAULT_IMAGE_STYLE) String style,
			@RequestParam(value = "resolution", required = false, defaultValue = "1080*1080") String resolution
	) {

		imageService.text2Image(prompt, resolution, style, response);
		return Result.success();
	}

}
