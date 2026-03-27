"""
飞书 API 工具类
处理消息接收、发送和 API 调用
"""

import json
import time
import hashlib
import base64
from typing import Optional, Dict, Any

import requests
from config import config


class FeishuAPI:
    """飞书 API 封装"""

    def __init__(self):
        self.app_id = config.FEISHU_APP_ID
        self.app_secret = config.FEISHU_APP_SECRET
        self.base_url = config.FEISHU_BASE_URL
        self.access_token = None
        self.token_expires_at = 0

    def get_access_token(self) -> str:
        """获取 tenant_access_token"""
        # 如果 token 还未过期，直接返回
        if self.access_token and time.time() < self.token_expires_at:
            return self.access_token

        url = f"{self.base_url}/auth/v3/tenant_access_token/internal"
        headers = {"Content-Type": "application/json"}
        data = {
            "app_id": self.app_id,
            "app_secret": self.app_secret
        }

        response = requests.post(url, headers=headers, json=data)
        result = response.json()

        if result.get("code") == 0:
            self.access_token = result.get("tenant_access_token")
            # 提前5分钟过期
            self.token_expires_at = time.time() + result.get("expire") - 300
            return self.access_token
        else:
            raise Exception(f"获取 access_token 失败: {result}")

    def send_message(self, receive_id: str, msg_type: str, content: dict) -> dict:
        """发送消息给用户或群"""
        url = f"{self.base_url}/im/v1/messages"
        token = self.get_access_token()

        headers = {
            "Authorization": f"Bearer {token}",
            "Content-Type": "application/json"
        }

        # 构建消息内容
        message_content = json.dumps(content) if isinstance(content, dict) else content

        data = {
            "receive_id": receive_id,
            "msg_type": msg_type,
            "content": message_content
        }

        params = {"receive_id_type": "open_id"}

        response = requests.post(url, headers=headers, json=data, params=params)
        result = response.json()

        if result.get("code") == 0:
            return {"success": True, "message_id": result.get("data", {}).get("message_id")}
        else:
            return {"success": False, "error": result}

    def send_text_message(self, receive_id: str, text: str) -> dict:
        """发送文本消息"""
        return self.send_message(
            receive_id=receive_id,
            msg_type="text",
            content={"text": text}
        )

    def reply_message(self, message_id: str, msg_type: str, content: dict) -> dict:
        """回复消息"""
        url = f"{self.base_url}/im/v1/messages/{message_id}/reply"
        token = self.get_access_token()

        headers = {
            "Authorization": f"Bearer {token}",
            "Content-Type": "application/json"
        }

        message_content = json.dumps(content) if isinstance(content, dict) else content

        data = {
            "msg_type": msg_type,
            "content": message_content
        }

        response = requests.post(url, headers=headers, json=data)
        result = response.json()

        if result.get("code") == 0:
            return {"success": True, "message_id": result.get("data", {}).get("message_id")}
        else:
            return {"success": False, "error": result}

    def reply_text_message(self, message_id: str, text: str) -> dict:
        """回复文本消息"""
        return self.reply_message(
            message_id=message_id,
            msg_type="text",
            content={"text": text}
        )

    def upload_image(self, image_path: str) -> Optional[str]:
        """上传图片并返回 image_key"""
        url = f"{self.base_url}/im/v1/images"
        token = self.get_access_token()

        headers = {
            "Authorization": f"Bearer {token}"
        }

        with open(image_path, "rb") as f:
            files = {"image": f}
            data = {"image_type": "message"}
            response = requests.post(url, headers=headers, files=files, data=data)

        result = response.json()
        if result.get("code") == 0:
            return result.get("data", {}).get("image_key")
        return None

    def get_message_content(self, message_id: str) -> Optional[Dict[str, Any]]:
        """获取消息内容"""
        url = f"{self.base_url}/im/v1/messages/{message_id}"
        token = self.get_access_token()

        headers = {"Authorization": f"Bearer {token}"}

        response = requests.get(url, headers=headers)
        result = response.json()

        if result.get("code") == 0:
            return result.get("data", {}).get("items", [{}])[0]
        return None

    @staticmethod
    def verify_signature(signature: str, timestamp: str, content: str, secret: str) -> bool:
        """验证飞书事件签名"""
        string_to_sign = f"{timestamp}{secret}{content}"
        hash_code = hashlib.sha256(string_to_sign.encode('utf-8')).hexdigest()
        return hash_code == signature


# 全局 API 实例
feishu_api = FeishuAPI()
