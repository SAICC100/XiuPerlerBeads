"""
MiniMax API 工具类
调用 MiniMax 大模型实现智能对话
"""

import json
from typing import List, Dict, Any, Optional

import requests
from config import config


class MiniMaxAPI:
    """MiniMax API 封装"""

    def __init__(self):
        self.api_key = config.MINIMAX_API_KEY
        self.base_url = config.MINIMAX_BASE_URL
        self.model = config.MINIMAX_MODEL

    def chat_completion(
        self,
        messages: List[Dict[str, str]],
        stream: bool = False,
        temperature: float = 0.7,
        max_tokens: int = 2048
    ) -> Dict[str, Any]:
        """
        调用 MiniMax 文本生成 API

        Args:
            messages: 消息列表 [{"role": "user", "content": "..."}]
            stream: 是否流式返回
            temperature: 温度参数 (0-1)
            max_tokens: 最大生成长度

        Returns:
            API 响应结果
        """
        url = f"{self.base_url}/v1/text/chatcompletion_pro"

        headers = {
            "Authorization": f"Bearer {self.api_key}",
            "Content-Type": "application/json"
        }

        data = {
            "model": self.model,
            "messages": messages,
            "stream": stream,
            "temperature": temperature,
            "max_tokens": max_tokens
        }

        try:
            response = requests.post(url, headers=headers, json=data, timeout=60)
            result = response.json()

            if "error" in result:
                return {
                    "success": False,
                    "error": result["error"].get("message", "Unknown error")
                }

            # 提取回复内容
            choices = result.get("choices", [])
            if choices:
                reply = choices[0].get("message", {}).get("content", "")
                return {
                    "success": True,
                    "reply": reply,
                    "usage": result.get("usage", {}),
                    "id": result.get("id", "")
                }
            else:
                return {
                    "success": False,
                    "error": "No response from model"
                }

        except requests.exceptions.Timeout:
            return {
                "success": False,
                "error": "请求超时，请稍后重试"
            }
        except Exception as e:
            return {
                "success": False,
                "error": f"API 调用失败: {str(e)}"
            }

    def simple_chat(self, user_message: str, system_prompt: str = "") -> Dict[str, Any]:
        """
        简单的单轮对话

        Args:
            user_message: 用户消息
            system_prompt: 系统提示词

        Returns:
            AI 回复文本
        """
        messages = []

        if system_prompt:
            messages.append({
                "role": "system",
                "content": system_prompt
            })

        messages.append({
            "role": "user",
            "content": user_message
        })

        result = self.chat_completion(messages)

        if result["success"]:
            return {
                "success": True,
                "reply": result["reply"]
            }
        else:
            return {
                "success": False,
                "error": result.get("error", "Unknown error")
            }


# 全局 API 实例
minimax_api = MiniMaxAPI()


# 默认系统提示词
DEFAULT_SYSTEM_PROMPT = """你是一个智能助手，名字叫 MiniMax AI助手，由 MiniMax 公司开发。

你的特点：
1. 专业、友善、有耐心
2. 善于解答各类问题
3. 可以帮助用户完成各种任务
4. 支持中英文对话

请用简洁、有帮助的方式回复用户。"""
