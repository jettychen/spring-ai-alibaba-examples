// Global variables
let selectedInputModality = 'text';
let selectedOutputModality = 'text';
let uploadedFiles = [];
let currentTaskId = null;

// Initialize page
document.addEventListener('DOMContentLoaded', function() {
    initializeEventListeners();
    loadSystemStatus();
    
    // Auto-refresh system status every 30 seconds
    setInterval(loadSystemStatus, 30000);
});

// Event listeners
function initializeEventListeners() {
    // Modality selection
    document.querySelectorAll('.modality-card').forEach(card => {
        card.addEventListener('click', handleModalitySelection);
    });

    // File upload
    const fileUploadArea = document.getElementById('fileUploadArea');
    const fileInput = document.getElementById('fileInput');
    
    fileUploadArea.addEventListener('click', () => fileInput.click());
    fileUploadArea.addEventListener('dragover', handleDragOver);
    fileUploadArea.addEventListener('drop', handleFileDrop);
    fileInput.addEventListener('change', handleFileSelect);

    // Priority slider
    document.getElementById('priorityInput').addEventListener('input', function() {
        document.getElementById('priorityValue').textContent = this.value;
    });

    // Buttons
    document.getElementById('processBtn').addEventListener('click', handleProcess);
    document.getElementById('clearBtn').addEventListener('click', handleClear);
    document.getElementById('statusBtn').addEventListener('click', loadSystemStatus);
}

// Handle modality selection
function handleModalitySelection(event) {
    const card = event.currentTarget;
    const type = card.dataset.type;
    const modality = card.dataset.modality;
    
    // Remove selection from same type
    document.querySelectorAll(`.modality-card[data-type="${type}"]`).forEach(c => {
        c.classList.remove('selected');
    });
    
    // Add selection to clicked card
    card.classList.add('selected');
    
    if (type === 'input') {
        selectedInputModality = modality;
    } else {
        selectedOutputModality = modality;
    }
    
    console.log(`Selected ${type} modality: ${modality}`);
}

// File handling
function handleDragOver(event) {
    event.preventDefault();
    event.currentTarget.classList.add('dragover');
}

function handleFileDrop(event) {
    event.preventDefault();
    event.currentTarget.classList.remove('dragover');
    const files = Array.from(event.dataTransfer.files);
    addFiles(files);
}

function handleFileSelect(event) {
    const files = Array.from(event.target.files);
    addFiles(files);
}

function addFiles(files) {
    uploadedFiles = uploadedFiles.concat(files);
    updateFileList();
}

function updateFileList() {
    const fileList = document.getElementById('fileList');
    if (uploadedFiles.length === 0) {
        fileList.innerHTML = '';
        return;
    }
    
    fileList.innerHTML = '<div class="mt-2"><strong>已选择文件:</strong></div>';
    uploadedFiles.forEach((file, index) => {
        const fileItem = document.createElement('div');
        fileItem.className = 'alert alert-info alert-dismissible fade show py-1 px-2 mt-1';
        fileItem.innerHTML = `
            <small>${file.name} (${formatFileSize(file.size)})</small>
            <button type="button" class="btn-close btn-close-sm" data-file-index="${index}"></button>
        `;
        
        // Add event listener to the close button
        const closeBtn = fileItem.querySelector('.btn-close');
        closeBtn.addEventListener('click', function() {
            removeFile(parseInt(this.dataset.fileIndex));
        });
        
        fileList.appendChild(fileItem);
    });
}

function removeFile(index) {
    uploadedFiles.splice(index, 1);
    updateFileList();
}

function formatFileSize(bytes) {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
}

// Load system status
async function loadSystemStatus() {
    try {
        const response = await fetch('/api/v1/ddd/multimodal/system/status');
        const result = await response.json();
        
        if (result.code === 10000) {
            const data = result.data;
            document.getElementById('systemHealthy').textContent = data.systemHealthy ? '✅ 健康' : '❌ 异常';
            document.getElementById('availableEngines').textContent = data.availableEngines;
            document.getElementById('processingTasks').textContent = data.processingTasks;
            document.getElementById('completedTasks').textContent = data.completedTasks;
        } else {
            throw new Error(result.message || '获取系统状态失败');
        }
    } catch (error) {
        console.error('Failed to load system status:', error);
        document.getElementById('systemHealthy').textContent = '❌ 连接失败';
    }
}

// Handle process
async function handleProcess() {
    const prompt = document.getElementById('promptInput').value.trim();
    if (!prompt) {
        alert('请输入处理指令！');
        return;
    }

    const processBtn = document.getElementById('processBtn');
    const loadingSpinner = document.getElementById('loadingSpinner');
    
    // Disable button and show spinner
    processBtn.disabled = true;
    loadingSpinner.style.display = 'block';
    
    // Clear previous results
    document.getElementById('resultContainer').innerHTML = '<div class="text-muted">处理中...</div>';
    
    try {
        const formData = new FormData();
        formData.append('inputModality', selectedInputModality);
        formData.append('outputModality', selectedOutputModality);
        formData.append('prompt', prompt);
        formData.append('userId', document.getElementById('userIdInput').value || 'test-user');
        formData.append('priority', document.getElementById('priorityInput').value);
        formData.append('streaming', document.getElementById('streamingCheck').checked);
        
        // Add files
        uploadedFiles.forEach(file => {
            formData.append('files', file);
        });

        const isStreaming = document.getElementById('streamingCheck').checked;
        const endpoint = isStreaming ? '/api/v1/ddd/multimodal/process-stream' : '/api/v1/ddd/multimodal/process';
        
        if (isStreaming) {
            await handleStreamingProcess(endpoint, formData);
        } else {
            await handleNormalProcess(endpoint, formData);
        }
        
    } catch (error) {
        console.error('Process failed:', error);
        document.getElementById('resultContainer').innerHTML = `
            <div class="alert alert-danger">
                <span class="icon-exclamation-triangle"></span> 处理失败: ${error.message}
            </div>
        `;
    } finally {
        processBtn.disabled = false;
        loadingSpinner.style.display = 'none';
    }
}

// Handle normal process
async function handleNormalProcess(endpoint, formData) {
    const response = await fetch(endpoint, {
        method: 'POST',
        body: formData
    });

    if (!response.ok) {
        throw new Error(`HTTP ${response.status}: ${response.statusText}`);
    }

    const result = await response.json();
    displayResult(result);
}

// Handle streaming process
async function handleStreamingProcess(endpoint, formData) {
    const response = await fetch(endpoint, {
        method: 'POST',
        body: formData
    });

    if (!response.ok) {
        throw new Error(`HTTP ${response.status}: ${response.statusText}`);
    }

    // Show stream container
    document.getElementById('streamContainer').style.display = 'block';
    const streamOutput = document.getElementById('streamOutput');
    streamOutput.innerHTML = '';

    const reader = response.body.getReader();
    const decoder = new TextDecoder();

    while (true) {
        const { done, value } = await reader.read();
        if (done) break;

        const chunk = decoder.decode(value, { stream: true });
        const div = document.createElement('div');
        div.textContent = chunk;
        streamOutput.appendChild(div);
        streamOutput.scrollTop = streamOutput.scrollHeight;
    }
}

// Display result
function displayResult(result) {
    const container = document.getElementById('resultContainer');
    
    if (result.code === 10000 && result.data) {
        const data = result.data;
        
        // Show current task info
        if (data.taskId) {
            currentTaskId = data.taskId;
            document.getElementById('currentTask').style.display = 'block';
            document.getElementById('currentTaskId').textContent = data.taskId;
            document.getElementById('currentTaskStatus').textContent = data.status || 'completed';
        }

        // Display content
        let contentHtml = '';
        
        if (data.content) {
            contentHtml += `
                <div class="alert alert-success">
                    <h6><span class="icon-check-circle"></span> 文本结果</h6>
                    <pre class="mb-0">${escapeHtml(data.content)}</pre>
                </div>
            `;
        }

        if (data.binaryContent) {
            contentHtml += `
                <div class="alert alert-info">
                    <h6><span class="icon-file-earmark-binary"></span> 二进制结果</h6>
                    <p>内容类型: ${data.contentType || '未知'}</p>
                    <p>数据长度: ${data.binaryContent.length} bytes</p>
                </div>
            `;
        }

        if (data.confidence !== undefined) {
            contentHtml += `
                <div class="alert alert-light">
                    <small><span class="icon-speedometer2"></span> 置信度: ${(data.confidence * 100).toFixed(2)}%</small>
                </div>
            `;
        }

        container.innerHTML = contentHtml || '<div class="text-success">处理完成，但无返回内容</div>';
    } else {
        container.innerHTML = `
            <div class="alert alert-warning">
                <span class="icon-exclamation-triangle"></span> ${result.message || '处理失败'}
            </div>
        `;
    }
}

// Clear form
function handleClear() {
    document.getElementById('promptInput').value = '';
    document.getElementById('userIdInput').value = 'test-user';
    document.getElementById('priorityInput').value = 5;
    document.getElementById('priorityValue').textContent = '5';
    document.getElementById('streamingCheck').checked = false;
    uploadedFiles = [];
    updateFileList();
    document.getElementById('resultContainer').innerHTML = `
        <div class="text-muted text-center">
            <span class="icon-chat-square-dots fs-1"></span>
            <p class="mt-2">处理结果将显示在这里</p>
        </div>
    `;
    document.getElementById('streamContainer').style.display = 'none';
    document.getElementById('currentTask').style.display = 'none';
    currentTaskId = null;
}

// Utility functions
function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}