import React, { useState, useEffect } from "react";
import {
  Card,
  Select,
  Input,
  Button,
  Upload,
  Space,
  Typography,
  Alert,
  Spin,
  Progress,
  Tag,
  Descriptions,
  Image,
  message,
  Row,
  Col,
  Divider,
} from "antd";
import {
  UploadOutlined,
  SendOutlined,
  ClearOutlined,
  InfoCircleOutlined,
  PlayCircleOutlined,
  FileImageOutlined,
  SoundOutlined,
  VideoCameraOutlined,
  FileTextOutlined,
} from "@ant-design/icons";
import type { UploadFile } from "antd";
import {
  processMultiModal,
  processMultiModalBinary,
  getSupportedModalities,
  ModalityTypes,
  ModalityCombinations,
  type MultiModalRequest,
  type MultiModalResponse,
  type SupportedModalities,
} from "../../api/multimodal";

const { TextArea } = Input;
const { Title, Text, Paragraph } = Typography;
const { Option } = Select;

const MultiModalPage: React.FC = () => {
  const [prompt, setPrompt] = useState<string>("");
  const [inputModality, setInputModality] = useState<string>(ModalityTypes.TEXT);
  const [outputModality, setOutputModality] = useState<string>(ModalityTypes.TEXT);
  const [files, setFiles] = useState<UploadFile[]>([]);
  const [parameters, setParameters] = useState<Record<string, string>>({});
  const [loading, setLoading] = useState<boolean>(false);
  const [result, setResult] = useState<MultiModalResponse | null>(null);
  const [binaryResult, setBinaryResult] = useState<string | null>(null);
  const [supportedModalities, setSupportedModalities] = useState<SupportedModalities | null>(null);
  
  // 加载支持的模态类型
  useEffect(() => {
    const loadModalities = async () => {
      try {
        const modalities = await getSupportedModalities();
        setSupportedModalities(modalities);
      } catch (error) {
        message.error("加载模态类型失败");
        console.error("Failed to load modalities:", error);
      }
    };
    loadModalities();
  }, []);

  // 获取模态类型图标
  const getModalityIcon = (modality: string) => {
    switch (modality) {
      case ModalityTypes.IMAGE:
        return <FileImageOutlined />;
      case ModalityTypes.AUDIO:
        return <SoundOutlined />;
      case ModalityTypes.VIDEO:
        return <VideoCameraOutlined />;
      case ModalityTypes.DOCUMENT:
        return <FileTextOutlined />;
      default:
        return <FileTextOutlined />;
    }
  };

  // 处理文件上传
  const handleFileChange = (fileList: UploadFile[]) => {
    setFiles(fileList);
  };

  // 处理参数变化
  const handleParameterChange = (key: string, value: string) => {
    setParameters(prev => ({
      ...prev,
      [key]: value
    }));
  };

  // 清除结果
  const clearResult = () => {
    setResult(null);
    setBinaryResult(null);
  };

  // 处理多模态请求
  const handleProcess = async () => {
    if (!prompt.trim()) {
      message.warning("请输入提示内容");
      return;
    }

    if (inputModality !== ModalityTypes.TEXT && files.length === 0) {
      message.warning("请上传相应的文件");
      return;
    }

    setLoading(true);
    clearResult();

    try {
      const requestData: MultiModalRequest = {
        prompt,
        inputModality,
        outputModality,
        files: files.map(file => file.originFileObj!),
        parameters,
        streaming: false,
      };

      // 根据输出类型选择处理方式
      if (outputModality === ModalityTypes.IMAGE || outputModality === ModalityTypes.AUDIO) {
        // 二进制输出
        const blob = await processMultiModalBinary(requestData);
        const url = URL.createObjectURL(blob);
        setBinaryResult(url);
      } else {
        // 文本输出
        const response = await processMultiModal(requestData);
        setResult(response);
      }

      message.success("处理完成");
    } catch (error) {
      message.error("处理失败: " + (error as Error).message);
      console.error("Processing error:", error);
    } finally {
      setLoading(false);
    }
  };

  // 选择预设组合
  const selectCombination = (combination: typeof ModalityCombinations[0]) => {
    setInputModality(combination.input);
    setOutputModality(combination.output);
    setPrompt("");
    setFiles([]);
    setParameters({});
    clearResult();
  };

  // 获取当前组合的参数输入
  const renderParameterInputs = () => {
    const inputs: React.ReactNode[] = [];

    if (outputModality === ModalityTypes.IMAGE) {
      inputs.push(
        <Input
          key="style"
          placeholder="图像风格 (如: 摄影写实, 动漫, 油画)"
          value={parameters.style || ""}
          onChange={(e) => handleParameterChange("style", e.target.value)}
        />
      );
      inputs.push(
        <Input
          key="resolution"
          placeholder="图像分辨率 (如: 1080*1080, 1920*1080)"
          value={parameters.resolution || ""}
          onChange={(e) => handleParameterChange("resolution", e.target.value)}
        />
      );
    }

    if (outputModality === ModalityTypes.AUDIO) {
      inputs.push(
        <Input
          key="voice"
          placeholder="语音类型 (如: default, female, male)"
          value={parameters.voice || ""}
          onChange={(e) => handleParameterChange("voice", e.target.value)}
        />
      );
      inputs.push(
        <Input
          key="language"
          placeholder="语言 (如: zh-CN, en-US)"
          value={parameters.language || ""}
          onChange={(e) => handleParameterChange("language", e.target.value)}
        />
      );
    }

    return inputs;
  };

  // 渲染结果
  const renderResult = () => {
    if (binaryResult) {
      if (outputModality === ModalityTypes.IMAGE) {
        return (
          <Card title="生成的图像" size="small">
            <Image
              src={binaryResult}
              alt="Generated image"
              style={{ maxWidth: "100%", maxHeight: "400px" }}
            />
          </Card>
        );
      } else if (outputModality === ModalityTypes.AUDIO) {
        return (
          <Card title="生成的音频" size="small">
            <audio controls style={{ width: "100%" }}>
              <source src={binaryResult} type="audio/wav" />
              您的浏览器不支持音频播放
            </audio>
          </Card>
        );
      }
    }

    if (result) {
      return (
        <Card title="处理结果" size="small">
          <Descriptions column={1} size="small">
            <Descriptions.Item label="状态">
              <Tag color={result.status === "SUCCESS" ? "green" : "red"}>
                {result.status}
              </Tag>
            </Descriptions.Item>
            {result.processingTimeMs && (
              <Descriptions.Item label="处理时间">
                {result.processingTimeMs}ms
              </Descriptions.Item>
            )}
            {result.confidence && (
              <Descriptions.Item label="置信度">
                <Progress
                  percent={Math.round(result.confidence * 100)}
                  size="small"
                  showInfo={false}
                />
                {Math.round(result.confidence * 100)}%
              </Descriptions.Item>
            )}
          </Descriptions>
          
          {result.content && (
            <div style={{ marginTop: 16 }}>
              <Text strong>内容:</Text>
              <Paragraph
                style={{
                  background: "#f5f5f5",
                  padding: "12px",
                  borderRadius: "6px",
                  marginTop: "8px",
                  whiteSpace: "pre-wrap",
                }}
              >
                {result.content}
              </Paragraph>
            </div>
          )}
          
          {result.metadata && Object.keys(result.metadata).length > 0 && (
            <div style={{ marginTop: 16 }}>
              <Text strong>元数据:</Text>
              <pre style={{ fontSize: "12px", marginTop: "8px" }}>
                {JSON.stringify(result.metadata, null, 2)}
              </pre>
            </div>
          )}
        </Card>
      );
    }

    return null;
  };

  return (
    <div style={{ padding: "24px", maxWidth: "1200px", margin: "0 auto" }}>
      <Title level={2}>统一多模态处理</Title>
      <Paragraph type="secondary">
        支持图像、音频、视频、文本等多种模态间的智能转换处理
      </Paragraph>

      <Row gutter={[24, 24]}>
        {/* 左侧：输入配置 */}
        <Col xs={24} lg={12}>
          <Card title="输入配置" size="small">
            <Space direction="vertical" style={{ width: "100%" }} size="middle">
              {/* 预设组合 */}
              <div>
                <Text strong>快速选择:</Text>
                <div style={{ marginTop: 8 }}>
                  <Space wrap>
                    {ModalityCombinations.map((combo, index) => (
                      <Button
                        key={index}
                        size="small"
                        onClick={() => selectCombination(combo)}
                        title={combo.description}
                      >
                        {getModalityIcon(combo.input)} → {getModalityIcon(combo.output)} {combo.label}
                      </Button>
                    ))}
                  </Space>
                </div>
              </div>

              <Divider />

              {/* 模态类型选择 */}
              <Row gutter={16}>
                <Col span={12}>
                  <div>
                    <Text strong>输入模态:</Text>
                    <Select
                      value={inputModality}
                      onChange={setInputModality}
                      style={{ width: "100%", marginTop: 8 }}
                    >
                      {Object.values(ModalityTypes).map(type => (
                        <Option key={type} value={type}>
                          {getModalityIcon(type)} {type.toUpperCase()}
                        </Option>
                      ))}
                    </Select>
                  </div>
                </Col>
                <Col span={12}>
                  <div>
                    <Text strong>输出模态:</Text>
                    <Select
                      value={outputModality}
                      onChange={setOutputModality}
                      style={{ width: "100%", marginTop: 8 }}
                    >
                      {Object.values(ModalityTypes).map(type => (
                        <Option key={type} value={type}>
                          {getModalityIcon(type)} {type.toUpperCase()}
                        </Option>
                      ))}
                    </Select>
                  </div>
                </Col>
              </Row>

              {/* 提示输入 */}
              <div>
                <Text strong>提示内容:</Text>
                <TextArea
                  value={prompt}
                  onChange={(e) => setPrompt(e.target.value)}
                  placeholder="请输入您的提示内容..."
                  rows={4}
                  style={{ marginTop: 8 }}
                />
              </div>

              {/* 文件上传 */}
              {inputModality !== ModalityTypes.TEXT && (
                <div>
                  <Text strong>上传文件:</Text>
                  <Upload
                    fileList={files}
                    onChange={({ fileList }) => handleFileChange(fileList)}
                    beforeUpload={() => false}
                    style={{ marginTop: 8 }}
                  >
                    <Button icon={<UploadOutlined />}>
                      选择{inputModality === ModalityTypes.IMAGE ? "图像" : 
                             inputModality === ModalityTypes.AUDIO ? "音频" : 
                             inputModality === ModalityTypes.VIDEO ? "视频" : "文件"}
                    </Button>
                  </Upload>
                </div>
              )}

              {/* 参数输入 */}
              {renderParameterInputs().length > 0 && (
                <div>
                  <Text strong>参数设置:</Text>
                  <Space direction="vertical" style={{ width: "100%", marginTop: 8 }}>
                    {renderParameterInputs()}
                  </Space>
                </div>
              )}

              {/* 操作按钮 */}
              <div>
                <Space>
                  <Button
                    type="primary"
                    icon={<SendOutlined />}
                    onClick={handleProcess}
                    loading={loading}
                  >
                    开始处理
                  </Button>
                  <Button
                    icon={<ClearOutlined />}
                    onClick={clearResult}
                  >
                    清除结果
                  </Button>
                </Space>
              </div>
            </Space>
          </Card>
        </Col>

        {/* 右侧：结果展示 */}
        <Col xs={24} lg={12}>
          <Card title="处理结果" size="small">
            {loading && (
              <div style={{ textAlign: "center", padding: "40px" }}>
                <Spin size="large" />
                <div style={{ marginTop: 16 }}>
                  <Text>正在处理中...</Text>
                </div>
              </div>
            )}

            {!loading && !result && !binaryResult && (
              <div style={{ textAlign: "center", padding: "40px", color: "#999" }}>
                <InfoCircleOutlined style={{ fontSize: "48px", marginBottom: "16px" }} />
                <div>配置参数后点击"开始处理"查看结果</div>
              </div>
            )}

            {renderResult()}
          </Card>

          {/* 系统信息 */}
          {supportedModalities && (
            <Card title="系统信息" size="small" style={{ marginTop: 16 }}>
              <Descriptions column={1} size="small">
                <Descriptions.Item label="支持的处理器">
                  {supportedModalities.processors.length}个
                </Descriptions.Item>
                <Descriptions.Item label="输入模态">
                  {supportedModalities.inputModalities.map(modality => (
                    <Tag key={modality} color="blue" style={{ margin: 2 }}>
                      {modality}
                    </Tag>
                  ))}
                </Descriptions.Item>
                <Descriptions.Item label="输出模态">
                  {supportedModalities.outputModalities.map(modality => (
                    <Tag key={modality} color="green" style={{ margin: 2 }}>
                      {modality}
                    </Tag>
                  ))}
                </Descriptions.Item>
              </Descriptions>
            </Card>
          )}
        </Col>
      </Row>
    </div>
  );
};

export default MultiModalPage;
