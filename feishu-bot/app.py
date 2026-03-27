"""
飞书 AI 机器人 - 主应用
使用 Flask + MiniMax API 实现飞书智能对话机器人
"""

import json
import logging
from threading import Thread
from flask import Flask, request, jsonify, Response
from config import config
from feishu_api import feishu_api
from minimax_api import minimax_api, DEFAULT_SYSTEM_PROMPT

# 配置日志
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

# 创建 Flask 应用
app = Flask(__name__)


class ConversationHistory:
    """对话历史管理"""

    def __init__(self, max_history: int = 10):
        self.history: dict = {}  # {open_id: [(role, content), ...]}
        self.max_history = max_history

    def add(self, open_id: str, role: str, content: str):
        """添加对话记录"""
        if open_id not in self.history:
            self.history[open_id] = []
        self.history[open_id].append((role, content))

        # 保持历史记录数量
        if len(self.history[open_id]) > self.max_history:
            self.history[open_id] = self.history[open_id][-self.max_history:]

    def get_messages(self, open_id: str) -> list:
        """获取格式化后的消息列表"""
        if open_id not in self.history:
            return [{"role": "system", "content": DEFAULT_SYSTEM_PROMPT}]

        messages = [{"role": "system", "content": DEFAULT_SYSTEM_PROMPT}]
        for role, content in self.history[open_id]:
            messages.append({"role": role, "content": content})
        return messages

    def clear(self, open_id: str):
        """清除用户对话历史"""
        if open_id in self.history:
            del self.history[open_id]


# 全局对话历史管理
conversation_history = ConversationHistory()


@app.route("/", methods=["GET"])
def index():
    """首页"""
    return jsonify({
        "status": "ok",
        "service": "飞书 AI 机器人",
        "version": "1.0.0",
        "endpoints": {
            "/": "本页面",
            "/health": "健康检查",
            "/feishu/event": "飞书事件订阅",
            "/feishu/callback": "飞书消息回调"
        }
    })


@app.route("/health", methods=["GET"])
def health():
    """健康检查"""
    return jsonify({"status": "healthy"})


@app.route("/feishu/event", methods=["GET"])
def feishu_event_get():
    """
    飞书事件订阅验证 (GET)
    用于验证事件订阅 URL 的有效性
    """
    params = request.args

    # 飞书会发送这些参数进行验证
    challenge = params.get("challenge", "")
    token = params.get("token", "")

    if challenge:
        return jsonify({"challenge": challenge})

    return jsonify({"error": "missing challenge parameter"})


@app.route("/feishu/event", methods=["POST"])
def feishu_event_post():
    """
    飞书事件订阅回调 (POST)
    处理飞书推送的事件和消息
    """
    try:
        # 解析请求体
        body = request.get_json()
        logger.info(f"收到飞书事件: {json.dumps(body, ensure_ascii=False)}")

        # 验证请求类型
        event_type = body.get("type", "")

        # 处理 URL 验证
        if event_type == "url_verification":
            challenge = body.get("challenge", "")
            return jsonify({"challenge": challenge})

        # 处理事件回调
        if event_type == "event_callback":
            event = body.get("event", {})
            event_schema = body.get("schema", "")

            # 处理消息事件
            if event_schema == "im.message.receive_v1":
                handle_message_event(event)

        return jsonify({"code": 0, "msg": "success"})

    except Exception as e:
        logger.error(f"处理飞书事件失败: {str(e)}")
        return jsonify({"code": 1, "msg": str(e)})


@app.route("/feishu/callback", methods=["POST"])
def feishu_callback():
    """
    另一种消息回调方式
    直接接收消息内容
    """
    try:
        body = request.get_json()
        logger.info(f"收到回调: {json.dumps(body, ensure_ascii=False)}")

        # 处理消息
        event = body.get("event", {})
        handle_message_event(event)

        return jsonify({"code": 0, "msg": "success"})

    except Exception as e:
        logger.error(f"处理回调失败: {str(e)}")
        return jsonify({"code": 1, "msg": str(e)})


def handle_message_event(event: dict):
    """处理接收到的消息事件"""
    try:
        # 提取消息信息
        message_type = event.get("message", {}).get("msg_type", "")
        message_id = event.get("message", {}).get("message_id", "")
        content_str = event.get("message", {}).get("content", "{}")

        # 解析消息内容
        content = json.loads(content_str)

        # 获取发送者信息
        sender = event.get("sender", {})
        sender_id = sender.get("sender_id", {}).get("open_id", "")
        sender_type = sender.get("sender_type", "")

        # 只处理用户发送的消息
        if sender_type != "user":
            logger.info(f"忽略非用户消息: {sender_type}")
            return

        # 处理不同类型的消息
        if message_type == "text":
            text = content.get("text", "").strip()
            if text:
                # 处理文本消息
                process_text_message(message_id, sender_id, text)

        elif message_type == "image":
            # 处理图片消息
            image_key = content.get("image_key", "")
            process_image_message(message_id, sender_id, image_key)

        else:
            logger.info(f"暂不支持的消息类型: {message_type}")

    except Exception as e:
        logger.error(f"处理消息事件失败: {str(e)}")


def process_text_message(message_id: str, open_id: str, text: str):
    """处理文本消息"""
    logger.info(f"处理用户消息: open_id={open_id}, text={text}")

    # 特殊命令处理
    if text.lower() in ["帮助", "help", "/help"]:
        help_text = """🤖 MiniMax AI 助手

欢迎使用飞书 AI 助手！

📌 使用方法：
直接发送您的问题，AI 会尽快回复。

✨ 支持功能：
• 问答对话
• 文本创作
• 代码编写
• 问题解答
• ...

🛠 管理命令：
• "清除历史" - 清除对话历史
• "帮助" - 显示此帮助信息

有问题请随时提问！"""

        feishu_api.reply_text_message(message_id, help_text)
        return

    if text.lower() in ["清除历史", "clear", "/clear"]:
        conversation_history.clear(open_id)
        feishu_api.reply_text_message(message_id, "✅ 对话历史已清除")
        return

    # 异步处理 AI 回复（避免阻塞）
    thread = Thread(target=generate_ai_response, args=(message_id, open_id, text))
    thread.start()


def generate_ai_response(message_id: str, open_id: str, user_message: str):
    """生成 AI 回复（异步执行）"""
    try:
        # 添加用户消息到历史
        conversation_history.add(open_id, "user", user_message)

        # 获取对话历史
        messages = conversation_history.get_messages(open_id)

        # 调用 MiniMax API
        result = minimax_api.chat_completion(messages)

        if result["success"]:
            reply = result["reply"]
            # 添加 AI 回复到历史
            conversation_history.add(open_id, "assistant", reply)

            # 发送回复
            feishu_api.reply_text_message(message_id, reply)
        else:
            error_msg = f"抱歉，AI 处理失败: {result.get('error', 'Unknown error')}"
            feishu_api.reply_text_message(message_id, error_msg)

    except Exception as e:
        logger.error(f"生成 AI 回复失败: {str(e)}")
        feishu_api.reply_text_message(message_id, "抱歉，处理您的请求时出现错误，请稍后重试。")


def process_image_message(message_id: str, open_id: str, image_key: str):
    """处理图片消息"""
    feishu_api.reply_text_message(
        message_id,
        "📷 图片已收到！\n\n抱歉，我目前还不支持图片理解功能。\n请继续发送文字消息，我会尽力帮助您！"
    )


def create_app():
    """创建 Flask 应用（用于 gunicorn 等 WSGI 服务器）"""
    return app


if __name__ == "__main__":
    logger.info("=" * 50)
    logger.info("飞书 AI 机器人启动中...")
    logger.info(f"监听地址: {config.HOST}:{config.PORT}")
    logger.info("=" * 50)

    # 启动 Flask 应用
    app.run(
        host=config.HOST,
        port=config.PORT,
        debug=True,
        threaded=True
    )
