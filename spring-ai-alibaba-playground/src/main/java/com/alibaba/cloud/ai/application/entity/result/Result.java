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

package com.alibaba.cloud.ai.application.entity.result; // 定义结果实体包路径

/**
 * 统一返回结果类，用于封装API响应数据
 * 
 * @author yuluo - 项目作者
 * @author <a href="mailto:yuluo08290126@gmail.com">yuluo</a> - 作者联系邮箱
 */

public class Result<T> { // 泛型类，T表示数据类型

	private int code; // 响应状态码

	private String message; // 响应消息

	private T data; // 响应数据，使用泛型支持任意数据类型

	/**
	 * 构造函数，根据结果码创建结果对象
	 * 
	 * @param resultCode 结果码枚举
	 */
	public Result(ResultCode resultCode) {
		this.code = resultCode.getCode(); // 设置状态码
		this.message = resultCode.getMessage(); // 设置消息
	}

	/**
	 * 构造函数，根据结果码和数据创建结果对象
	 * 
	 * @param resultCode 结果码枚举
	 * @param data 响应数据
	 */
	public Result(ResultCode resultCode, T data) {
		this.code = resultCode.getCode(); // 设置状态码
		this.message = resultCode.getMessage(); // 设置消息
		this.data = data; // 设置数据
	}

	/**
	 * 获取响应状态码
	 * 
	 * @return 状态码
	 */
	public int getCode() {
		return code; // 返回状态码
	}

	/**
	 * 设置响应状态码
	 * 
	 * @param code 状态码
	 */
	public void setCode(int code) {
		this.code = code; // 设置状态码
	}

	/**
	 * 获取响应消息
	 * 
	 * @return 响应消息
	 */
	public String getMessage() {
		return message; // 返回消息
	}

	/**
	 * 设置响应消息
	 * 
	 * @param message 响应消息
	 */
	public void setMessage(String message) {
		this.message = message; // 设置消息
	}

	/**
	 * 获取响应数据
	 * 
	 * @return 响应数据
	 */
	public T getData() {
		return data; // 返回数据
	}

	/**
	 * 设置响应数据
	 * 
	 * @param data 响应数据
	 */
	public void setData(T data) {
		this.data = data; // 设置数据
	}

	/**
	 * 创建成功结果（无数据）
	 * 
	 * @return 成功结果对象
	 */
	public static <T> Result<T> success() {
		return new Result<>(ResultCode.SUCCESS); // 返回成功结果
	}

	/**
	 * 创建成功结果（带数据）
	 * 
	 * @param data 响应数据
	 * @return 成功结果对象
	 */
	public static <T> Result<T> success(T data) {
		return new Result<>(ResultCode.SUCCESS, data); // 返回带数据的成功结果
	}

	/**
	 * 创建失败结果（无数据）
	 * 
	 * @return 失败结果对象
	 */
	public static <T> Result<T> failed() {
		return new Result<>(ResultCode.FAILED); // 返回失败结果
	}

	/**
	 * 创建失败结果（自定义消息）
	 * 
	 * @param customMessage 自定义错误消息
	 * @return 失败结果对象
	 */
	public static <T> Result<T> failed(String customMessage) {
		Result<T> result = new Result<>(ResultCode.FAILED); // 创建失败结果
		result.setMessage(customMessage); // 设置自定义消息
		return result; // 返回失败结果
	}

}
