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

package com.alibaba.cloud.ai.ddd.multimodal.interfaces.web;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * DDD多模态测试页面控制器
 * 提供专门的测试界面访问
 */
@RestController
public class DddTestPageController {

    /**
     * DDD多模态测试页面
     */
    @GetMapping(value = "/ddd-test", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public String dddTestPage() throws IOException {
        Resource resource = new ClassPathResource("static/ddd-multimodal-test.html");
        return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    }

    /**
     * DDD多模态测试页面（带.html后缀）
     */
    @GetMapping(value = "/ddd-multimodal-test.html", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public String dddTestPageWithHtml() throws IOException {
        return dddTestPage();
    }
}