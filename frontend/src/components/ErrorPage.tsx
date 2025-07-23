import React, { useState, useEffect } from 'react';
import './ErrorPage.css';

interface ErrorInfo {
  timestamp: string;
  status: number;
  message: string;
  path: string;
  description: string;
  availableEndpoints: string[];
  errorHandling: string[];
}

const ErrorPage: React.FC = () => {
  const [errorInfo, setErrorInfo] = useState<ErrorInfo | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string>('');

  useEffect(() => {
    fetchErrorInfo();
  }, []);

  const fetchErrorInfo = async () => {
    try {
      setLoading(true);
      const response = await fetch('http://localhost:8080/api/error');
      
      if (response.ok) {
        const data = await response.json();
        setErrorInfo(data);
      } else {
        setError(`HTTP ${response.status}: ${response.statusText}`);
      }
    } catch (err) {
      setError('无法连接到后端服务器');
    } finally {
      setLoading(false);
    }
  };

  const testEndpoint = async (endpoint: string) => {
    try {
      const response = await fetch(`http://localhost:8080${endpoint}`);
      const data = await response.text();
      
      alert(`测试端点: ${endpoint}\n状态码: ${response.status}\n响应: ${data.substring(0, 200)}...`);
    } catch (err) {
      alert(`测试端点失败: ${endpoint}\n错误: ${err}`);
    }
  };

  if (loading) {
    return (
      <div className="error-page">
        <div className="loading">
          <div className="spinner"></div>
          <p>加载中...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="error-page">
        <div className="error-container">
          <h1>🚫 连接错误</h1>
          <p className="error-message">{error}</p>
          <button onClick={fetchErrorInfo} className="retry-button">
            重试
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="error-page">
      <div className="error-container">
        <header className="error-header">
          <h1>🛠️ API 错误处理页面</h1>
          <p className="subtitle">Spring Boot 全局错误处理演示</p>
        </header>

        {errorInfo && (
          <>
            <section className="error-info">
              <h2>📋 错误处理信息</h2>
              <div className="info-grid">
                <div className="info-item">
                  <label>时间戳:</label>
                  <span>{new Date(errorInfo.timestamp).toLocaleString()}</span>
                </div>
                <div className="info-item">
                  <label>状态码:</label>
                  <span className="status-code">{errorInfo.status}</span>
                </div>
                <div className="info-item">
                  <label>路径:</label>
                  <span>{errorInfo.path}</span>
                </div>
                <div className="info-item">
                  <label>消息:</label>
                  <span>{errorInfo.message}</span>
                </div>
              </div>
              <div className="description">
                <p>{errorInfo.description}</p>
              </div>
            </section>

            <section className="endpoints-section">
              <h2>🔗 可用的 API 端点</h2>
              <div className="endpoints-list">
                {errorInfo.availableEndpoints.map((endpoint, index) => {
                  const [path, description] = endpoint.split(' - ');
                  return (
                    <div key={index} className="endpoint-item">
                      <div className="endpoint-info">
                        <code className="endpoint-path">{path}</code>
                        <span className="endpoint-desc">{description}</span>
                      </div>
                      <button 
                        onClick={() => testEndpoint(path)}
                        className="test-button"
                      >
                        测试
                      </button>
                    </div>
                  );
                })}
              </div>
            </section>

            <section className="error-handling-section">
              <h2>⚠️ 错误处理类型</h2>
              <div className="error-types">
                {errorInfo.errorHandling.map((errorType, index) => {
                  const [code, description] = errorType.split(' - ');
                  return (
                    <div key={index} className="error-type">
                      <span className="error-code">{code}</span>
                      <span className="error-desc">{description}</span>
                    </div>
                  );
                })}
              </div>
            </section>

            <section className="demo-section">
              <h2>🧪 错误演示</h2>
              <div className="demo-buttons">
                <button 
                  onClick={() => testEndpoint('/api/nonexistent')}
                  className="demo-button error-404"
                >
                  测试 404 错误
                </button>
                <button 
                  onClick={() => testEndpoint('/api/test/authenticated')}
                  className="demo-button error-401"
                >
                  测试 401 错误
                </button>
                <button 
                  onClick={() => testEndpoint('/api/test/admin')}
                  className="demo-button error-403"
                >
                  测试 403 错误
                </button>
              </div>
            </section>
          </>
        )}

        <footer className="error-footer">
          <p>这个页面展示了 Spring Boot 应用程序的全局错误处理机制</p>
          <button onClick={fetchErrorInfo} className="refresh-button">
            🔄 刷新信息
          </button>
        </footer>
      </div>
    </div>
  );
};

export default ErrorPage;