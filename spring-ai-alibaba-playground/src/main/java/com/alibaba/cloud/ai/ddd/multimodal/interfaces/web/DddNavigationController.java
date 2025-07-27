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

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * DDD导航页面控制器
 * 提供DDD功能的导航入口
 */
@RestController
public class DddNavigationController {

    /**
     * DDD功能导航页面
     */
    @GetMapping(value = "/ddd", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public String dddNavigationPage() {
        return """
            <!DOCTYPE html>
            <html lang="zh-CN">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>DDD多模态处理系统</title>
                <style>
                    * { margin: 0; padding: 0; box-sizing: border-box; }
                    body {
                        font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
                        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                        min-height: 100vh;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                    }
                    .navigation-card {
                        background: white;
                        border-radius: 20px;
                        box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
                        padding: 40px;
                        text-align: center;
                        max-width: 600px;
                        width: 100%;
                        margin: 20px;
                    }
                    .feature-card {
                        background: #f8f9fa;
                        border: 2px solid #e9ecef;
                        border-radius: 15px;
                        padding: 20px;
                        margin: 15px 0;
                        transition: all 0.3s ease;
                        text-decoration: none;
                        display: block;
                        color: inherit;
                    }
                    .feature-card:hover {
                        border-color: #667eea;
                        transform: translateY(-3px);
                        box-shadow: 0 8px 25px rgba(102, 126, 234, 0.2);
                        color: inherit;
                        text-decoration: none;
                    }
                    .feature-icon {
                        font-size: 3rem;
                        margin-bottom: 15px;
                    }
                    .btn {
                        display: inline-block;
                        padding: 12px 24px;
                        margin: 5px;
                        text-decoration: none;
                        border-radius: 25px;
                        font-weight: 600;
                        transition: all 0.3s ease;
                        border: 2px solid transparent;
                    }
                    .btn-primary {
                        background: linear-gradient(45deg, #667eea 0%, #764ba2 100%);
                        color: white;
                    }
                    .btn-primary:hover {
                        background: linear-gradient(45deg, #5a6fd8 0%, #6a4190 100%);
                        transform: translateY(-2px);
                        color: white;
                        text-decoration: none;
                    }
                    .btn-outline-info {
                        color: #0dcaf0;
                        border-color: #0dcaf0;
                        background: transparent;
                    }
                    .btn-outline-info:hover {
                        background: #0dcaf0;
                        color: white;
                        text-decoration: none;
                    }
                    .btn-outline-secondary {
                        color: #6c757d;
                        border-color: #6c757d;
                        background: transparent;
                    }
                    .btn-outline-secondary:hover {
                        background: #6c757d;
                        color: white;
                        text-decoration: none;
                    }
                    .text-primary { color: #667eea !important; }
                    .text-success { color: #28a745 !important; }
                    .text-muted { color: #6c757d !important; }
                    .text-center { text-align: center; }
                    .mb-4 { margin-bottom: 1.5rem; }
                    .mt-3 { margin-top: 1rem; }
                    .mt-4 { margin-top: 1.5rem; }
                    .mt-5 { margin-top: 3rem; }
                    .mb-0 { margin-bottom: 0; }
                    .pt-4 { padding-top: 1.5rem; }
                    .border-top { border-top: 1px solid #dee2e6; }
                    .d-flex { display: flex; }
                    .gap-2 { gap: 0.5rem; }
                    .gap-4 { gap: 1.5rem; }
                    .justify-content-center { justify-content: center; }
                    .flex-wrap { flex-wrap: wrap; }
                    .row { display: flex; flex-wrap: wrap; margin: -15px; }
                    .col-md-6 { flex: 0 0 50%; max-width: 50%; padding: 15px; }
                    @media (max-width: 768px) {
                        .col-md-6 { flex: 0 0 100%; max-width: 100%; }
                    }
                    .badge {
                        display: inline-block;
                        padding: 0.5em 0.75em;
                        font-size: 0.875em;
                        font-weight: 700;
                        line-height: 1;
                        text-align: center;
                        white-space: nowrap;
                        vertical-align: baseline;
                        border-radius: 0.375rem;
                    }
                    .bg-primary { background-color: #667eea; color: white; }
                    .bg-success { background-color: #28a745; color: white; }
                    .bg-warning { background-color: #ffc107; color: #212529; }
                    .bg-danger { background-color: #dc3545; color: white; }
                    .fs-6 { font-size: 1rem; }
                    h1 { font-size: 2.5rem; margin-bottom: 0.5rem; }
                    h5 { font-size: 1.25rem; margin-bottom: 0.75rem; }
                    h6 { font-size: 1rem; margin-bottom: 0.5rem; }
                    small { font-size: 0.875em; }
                    .icon-robot::before { content: "🤖"; }
                    .icon-play-circle::before { content: "▶️"; }
                    .icon-book::before { content: "📚"; }
                    .icon-activity::before { content: "📊"; }
                    .icon-house::before { content: "🏠"; }
                    .icon-gear::before { content: "⚙️"; }
                    .icon-lightning::before { content: "⚡"; }
                    .icon-shield-check::before { content: "🛡️"; }
                    .icon-chat-text::before { content: "💬"; }
                    .icon-image::before { content: "🖼️"; }
                    .icon-mic::before { content: "🎤"; }
                    .icon-camera-video::before { content: "📹"; }
                </style>
            </head>
            <body>
                <div class="navigation-card">
                    <div class="text-center mb-4">
                        <span class="icon-robot text-primary" style="font-size: 4rem;"></span>
                        <h1 class="mt-3">DDD多模态处理系统</h1>
                        <p class="text-muted">基于领域驱动设计的统一多模态AI处理平台</p>
                    </div>
                    
                    <div class="row">
                        <div class="col-md-6">
                            <a href="/ddd-test" class="feature-card">
                                <span class="icon-play-circle text-primary feature-icon"></span>
                                <h5>🎯 测试界面</h5>
                                <p class="text-muted mb-0">交互式多模态处理测试平台</p>
                            </a>
                        </div>
                        <div class="col-md-6">
                            <a href="/doc.html" class="feature-card">
                                <span class="icon-book text-success feature-icon"></span>
                                <h5>📚 API文档</h5>
                                <p class="text-muted mb-0">完整的接口文档和在线测试</p>
                            </a>
                        </div>
                    </div>
                    
                    <div class="mt-4">
                        <h6 class="text-muted">🚀 快速访问</h6>
                        <div class="d-flex gap-2 justify-content-center flex-wrap">
                            <a href="/ddd-test" class="btn btn-primary">
                                <span class="icon-play-circle"></span> 开始测试
                            </a>
                            <a href="/api/v1/ddd/multimodal/system/status" class="btn btn-outline-info" target="_blank">
                                <span class="icon-activity"></span> 系统状态
                            </a>
                            <a href="/" class="btn btn-outline-secondary">
                                <span class="icon-house"></span> 主页
                            </a>
                        </div>
                    </div>
                    
                    <div class="mt-5 pt-4 border-top">
                        <h6 class="text-muted">🎨 支持的模态</h6>
                        <div class="d-flex justify-content-center gap-4 flex-wrap">
                            <span class="badge bg-primary fs-6"><span class="icon-chat-text"></span> 文本</span>
                            <span class="badge bg-success fs-6"><span class="icon-image"></span> 图像</span>
                            <span class="badge bg-warning fs-6"><span class="icon-mic"></span> 音频</span>
                            <span class="badge bg-danger fs-6"><span class="icon-camera-video"></span> 视频</span>
                        </div>
                    </div>
                    
                    <div class="mt-3">
                        <small class="text-muted">
                            <span class="icon-gear"></span> DDD架构 | 
                            <span class="icon-lightning"></span> 反应式 | 
                            <span class="icon-shield-check"></span> 企业级
                        </small>
                    </div>
                </div>
            </body>
            </html>
            """;
    }
}