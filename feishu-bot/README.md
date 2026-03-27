# 飞书 AI 机器人部署指南

基于 MiniMax API 的飞书智能对话机器人

---

## 📋 项目概述

本项目是一个飞书 AI 机器人，使用 MiniMax API 提供智能对话功能。

### 功能特点
- 🤖 基于 MiniMax 大模型
- 💬 支持多轮对话
- 🔄 自动管理对话历史
- 🛠 支持特殊命令（帮助、清除历史）

---

## 🚀 快速开始

### 1. 安装依赖

```bash
cd /Users/saicc/pyProject/1251/feishu-bot
pip install -r requirements.txt
```

### 2. 配置

配置文件 `config.py` 已包含您的凭证：

| 配置项 | 值 |
|--------|-----|
| 飞书 App ID | `cli_a914dfc2ad385cd5` |
| MiniMax API | 已配置 ✅ |

### 3. 本地测试

```bash
python app.py
```

服务将在 `http://localhost:5000` 启动

### 4. 验证服务

```bash
curl http://localhost:5000/health
```

---

## 🌐 部署到服务器

### 方式一：使用 Gunicorn（生产环境推荐）

```bash
# 安装 gunicorn
pip install gunicorn

# 启动服务
gunicorn -w 4 -b 0.0.0.0:5000 app:app
```

### 方式二：使用 Docker

创建 `Dockerfile`：
```dockerfile
FROM python:3.10-slim
WORKDIR /app
COPY requirements.txt .
RUN pip install -r requirements.txt
COPY . .
EXPOSE 5000
CMD ["gunicorn", "-w", "4", "-b", "0.0.0.0:5000", "app:app"]
```

构建运行：
```bash
docker build -t feishu-bot .
docker run -d -p 5000:5000 --name feishu-bot feishu-bot
```

---

## ⚙️ 飞书开发者后台配置

### 1. 配置事件订阅

1. 登录 [飞书开发者后台](https://open.feishu.cn/app)
2. 选择您的应用 `cli_a914dfc2ad385cd5`
3. 进入 **事件订阅** 页面
4. 配置请求地址：
   ```
   https://your-domain.com/feishu/event
   ```
   > 注意：必须使用 HTTPS 地址

5. 订阅事件：
   - `im.message.receive_v1` (接收消息)

### 2. 配置权限

在 **权限管理** 页面添加以下权限：

| 权限名称 | 权限标识 |
|----------|----------|
| 获取与发送单聊消息 | `im:message` |
| 接收消息 | `im:message:receive` |

### 3. 机器人能力

在 **应用能力** 页面：
1. 添加 **机器人** 能力
2. 配置机器人名称和描述

### 4. 发布应用

1. 进入 **版本管理与发布** 页面
2. 创建新版本
3. 申请发布（企业内部应用无需审核）

---

## 🌐 公网访问配置

机器人需要公网可访问的 HTTPS 地址。推荐方案：

### 方案 1：内网穿透

使用 ngrok：
```bash
ngrok http 5000
```
会返回类似 `https://abc123.ngrok.io` 的地址

### 方案 2：云服务器

部署到阿里云、腾讯云等服务器：
1. 安装 Python 3.8+
2. 克隆代码
3. 安装依赖
4. 使用 gunicorn 运行
5. 配置 Nginx 反向代理 + SSL

### 方案 3：云函数

部署到阿里云函数计算、腾讯云 SCF 等：
1. 打包代码
2. 配置环境变量
3. 设置触发器

---

## 📝 API 接口说明

| 接口 | 方法 | 说明 |
|------|------|------|
| `/` | GET | 服务信息 |
| `/health` | GET | 健康检查 |
| `/feishu/event` | GET/POST | 飞书事件订阅 |
| `/feishu/callback` | POST | 消息回调 |

---

## 🛠 故障排查

### 1. 服务无法启动

```bash
# 检查端口占用
lsof -i :5000

# 查看错误日志
python app.py
```

### 2. 飞书消息未收到

1. 确认公网地址可访问
2. 检查飞书事件订阅配置
3. 确认权限已申请

### 3. MiniMax API 调用失败

1. 检查 API Key 是否正确
2. 确认 API 调用额度

### 4. 查看日志

```bash
# 实时查看日志
tail -f app.log

# 查看最近 100 行
tail -100 app.log
```

---

## 📞 支持

如有问题，请检查：
1. 飞书开发者后台日志
2. 服务器日志
3. MiniMax API 调用记录

---

## 📄 许可证

MIT License
