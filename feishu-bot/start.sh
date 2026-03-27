#!/bin/bash

# 飞书 AI 机器人启动脚本

echo "====================================="
echo "  飞书 AI 机器人启动中..."
echo "====================================="

# 检查 Python 版本
python_version=$(python3 --version 2>&1 | awk '{print $2}')
echo "Python 版本: $python_version"

# 安装依赖
echo ""
echo "[1/3] 安装依赖..."
pip install -r requirements.txt

# 检查依赖
echo ""
echo "[2/3] 验证依赖..."
python3 -c "import flask; import requests; print('✓ Flask:', flask.__version__); print('✓ Requests: 已安装')"

# 启动服务
echo ""
echo "[3/3] 启动服务..."
echo ""
echo "====================================="
echo "  服务地址: http://localhost:5000"
echo "  事件回调: http://localhost:5000/feishu/event"
echo "====================================="
echo ""
echo "按 Ctrl+C 停止服务"
echo ""

python3 app.py
