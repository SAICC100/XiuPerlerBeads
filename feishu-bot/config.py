"""
飞书 AI 机器人配置
使用 MiniMax API 实现智能对话
"""

import os
from dataclasses import dataclass


@dataclass
class Config:
    """飞书机器人配置"""

    # 飞书配置
    FEISHU_APP_ID: str = "cli_a914dfc2ad385cd5"
    FEISHU_APP_SECRET: str = "cRhXLAiSdTp8tXpavgp0IGLR1hK17wfn"

    # MiniMax API 配置
    MINIMAX_API_KEY: str = "sk-api-PSzlxDSeqxk2FPtAcpZnJ3gVh2AqZcV1-X6qOfD8FjjKud1cNBb82ovDccMcApLS2RLfJJdt5iueJ7Eg77mo-8AWdGWEMpNaFLhb4ayyTy1AiFzozm_K_bk"
    MINIMAX_BASE_URL: str = "https://api.minimax.chat"

    # 服务器配置
    HOST: str = "0.0.0.0"
    PORT: int = 5000

    # 飞书 API 地址
    FEISHU_BASE_URL: str = "https://open.feishu.cn/open-apis"

    # MiniMax 模型
    MINIMAX_MODEL: str = "MiniMax-Text-01"
    MINIMAX_GROUP_ID: str = ""  # 可选：设置群组ID限制


# 全局配置实例
config = Config()
