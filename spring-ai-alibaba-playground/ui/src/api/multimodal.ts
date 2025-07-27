import { request } from './request';

export interface MultiModalRequest {
  prompt: string;
  inputModality: string;
  outputModality: string;
  files?: File[];
  parameters?: Record<string, string>;
  streaming?: boolean;
}

export interface MultiModalResponse {
  responseId?: string;
  requestId?: string;
  outputModality?: string;
  content?: string;
  binaryContent?: string;
  contentType?: string;
  metadata?: Record<string, any>;
  status?: string;
  errorMessage?: string;
  timestamp?: string;
  processingTimeMs?: number;
  confidence?: number;
}

export interface ProcessorInfo {
  name: string;
  supportedModality: string;
  priority: number;
}

export interface SupportedModalities {
  inputModalities: string[];
  outputModalities: string[];
  processors: ProcessorInfo[];
}

/**
 * 统一多模态处理
 */
export const processMultiModal = async (data: MultiModalRequest): Promise<MultiModalResponse> => {
  const formData = new FormData();
  formData.append('prompt', data.prompt);
  formData.append('inputModality', data.inputModality);
  formData.append('outputModality', data.outputModality);
  
  if (data.files && data.files.length > 0) {
    data.files.forEach(file => {
      formData.append('files', file);
    });
  }
  
  if (data.parameters) {
    Object.entries(data.parameters).forEach(([key, value]) => {
      formData.append(`parameters[${key}]`, value);
    });
  }
  
  if (data.streaming) {
    formData.append('streaming', 'true');
  }

  return request.post('/multimodal/process', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  });
};

/**
 * 流式多模态处理
 */
export const processMultiModalStream = (data: MultiModalRequest): EventSource => {
  const formData = new FormData();
  formData.append('prompt', data.prompt);
  formData.append('inputModality', data.inputModality);
  formData.append('outputModality', data.outputModality);
  
  if (data.files && data.files.length > 0) {
    data.files.forEach(file => {
      formData.append('files', file);
    });
  }
  
  if (data.parameters) {
    Object.entries(data.parameters).forEach(([key, value]) => {
      formData.append(`parameters[${key}]`, value);
    });
  }
  
  // 对于流式处理，我们使用 fetch API 创建流式连接
  // 注意：这里需要根据后端实际实现调整
  const url = new URL('/api/v1/multimodal/process-stream', window.location.origin);
  
  // 将FormData转换为URLSearchParams用于GET请求
  const params = new URLSearchParams();
  params.append('prompt', data.prompt);
  params.append('inputModality', data.inputModality);
  params.append('outputModality', data.outputModality);
  
  if (data.parameters) {
    Object.entries(data.parameters).forEach(([key, value]) => {
      params.append(`parameters[${key}]`, value);
    });
  }
  
  url.search = params.toString();
  
  return new EventSource(url.toString());
};

/**
 * 处理二进制输出（图像、音频等）
 */
export const processMultiModalBinary = async (data: MultiModalRequest): Promise<Blob> => {
  const formData = new FormData();
  formData.append('prompt', data.prompt);
  formData.append('inputModality', data.inputModality);
  formData.append('outputModality', data.outputModality);
  
  if (data.files && data.files.length > 0) {
    data.files.forEach(file => {
      formData.append('files', file);
    });
  }
  
  if (data.parameters) {
    Object.entries(data.parameters).forEach(([key, value]) => {
      formData.append(`parameters[${key}]`, value);
    });
  }

  const response = await fetch('/api/v1/multimodal/process-binary', {
    method: 'POST',
    body: formData,
  });
  
  if (!response.ok) {
    throw new Error(`HTTP error! status: ${response.status}`);
  }
  
  return response.blob();
};

/**
 * 获取支持的模态类型
 */
export const getSupportedModalities = async (): Promise<SupportedModalities> => {
  return request.get('/multimodal/modalities');
};

/**
 * 健康检查
 */
export const checkMultiModalHealth = async (): Promise<any> => {
  return request.get('/multimodal/health');
};

/**
 * 批量处理
 */
export const processMultiModalBatch = async (requests: MultiModalRequest[]): Promise<MultiModalResponse[]> => {
  const batchData = requests.map(req => ({
    prompt: req.prompt,
    inputModality: req.inputModality,
    outputModality: req.outputModality,
    parameters: req.parameters || {}
  }));
  
  return request.post('/multimodal/process-batch', batchData);
};

// 模态类型常量
export const ModalityTypes = {
  TEXT: 'text',
  IMAGE: 'image',
  AUDIO: 'audio',
  VIDEO: 'video',
  DOCUMENT: 'document',
  MULTIMODAL: 'multimodal'
} as const;

// 预设的模态转换组合
export const ModalityCombinations = [
  { input: ModalityTypes.IMAGE, output: ModalityTypes.TEXT, label: '图像识别', description: '将图像转换为文本描述' },
  { input: ModalityTypes.TEXT, output: ModalityTypes.IMAGE, label: '文本生图', description: '根据文本描述生成图像' },
  { input: ModalityTypes.AUDIO, output: ModalityTypes.TEXT, label: '语音识别', description: '将音频转换为文本' },
  { input: ModalityTypes.TEXT, output: ModalityTypes.AUDIO, label: '语音合成', description: '将文本转换为语音' },
  { input: ModalityTypes.VIDEO, output: ModalityTypes.TEXT, label: '视频分析', description: '分析视频内容并生成描述' },
  { input: ModalityTypes.DOCUMENT, output: ModalityTypes.TEXT, label: '文档解析', description: '解析文档内容' },
] as const;