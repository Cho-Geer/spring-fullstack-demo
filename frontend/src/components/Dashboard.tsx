import React from 'react';
import { useAuth } from '../contexts/AuthContext';
import './Dashboard.css';

const Dashboard: React.FC = () => {
  const { user, logout } = useAuth();

  const handleLogout = async () => {
    try {
      await logout();
    } catch (error) {
      console.error('登出失败:', error);
    }
  };

  return (
    <div className="dashboard-container">
      <header className="dashboard-header">
        <div className="header-content">
          <h1>用户仪表板</h1>
          <div className="user-info">
            <span>欢迎, {user?.username}</span>
            <button onClick={handleLogout} className="logout-button">
              登出
            </button>
          </div>
        </div>
      </header>

      <main className="dashboard-main">
        <div className="dashboard-content">
          <div className="welcome-section">
            <h2>欢迎使用系统</h2>
            <p>您已成功登录到系统中。</p>
          </div>

          <div className="user-details-section">
            <h3>用户信息</h3>
            <div className="user-details">
              <div className="detail-item">
                <label>用户名:</label>
                <span>{user?.username}</span>
              </div>
              <div className="detail-item">
                <label>邮箱:</label>
                <span>{user?.email || '未设置'}</span>
              </div>
              <div className="detail-item">
                <label>权限:</label>
                <span>{user?.authorities?.join(', ') || '无'}</span>
              </div>
            </div>
          </div>

          <div className="features-section">
            <h3>系统功能</h3>
            <div className="features-grid">
              <div className="feature-card">
                <h4>用户管理</h4>
                <p>管理系统用户信息</p>
              </div>
              <div className="feature-card">
                <h4>权限控制</h4>
                <p>基于JWT的权限管理</p>
              </div>
              <div className="feature-card">
                <h4>安全认证</h4>
                <p>Spring Security集成</p>
              </div>
              <div className="feature-card">
                <h4>数据持久化</h4>
                <p>JPA + MySQL数据存储</p>
              </div>
            </div>
          </div>
        </div>
      </main>
    </div>
  );
};

export default Dashboard;