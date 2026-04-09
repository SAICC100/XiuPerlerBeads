package com.example.xiuperlerbeads.data.ai

import android.content.Context
import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import com.example.xiuperlerbeads.domain.model.BeadColor
import com.example.xiuperlerbeads.domain.model.BeadColorManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

/**
 * AI API 提供商
 */
enum class AIProvider(val displayName: String, val endpoint: String) {
    OPENAI("OpenAI", "https://api.openai.com/v1/chat/completions"),
    KIMI("Kimi", "https://api.moonshot.cn/v1/chat/completions"),
    QWEN("通义千问", "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions"),
    GEMINI("Gemini", ""), // Gemini uses a different API format
    ANTHROPIC("Claude", "https://api.anthropic.com/v1/messages")
}

/**
 * AI 配置
 */
data class AIConfig(
    val provider: AIProvider = AIProvider.OPENAI,
    val apiKey: String = "",
    val model: String = "gpt-4o",
    val maxTokens: Int = 4096,
    val temperature: Float = 0.1f
) {
    fun isConfigured(): Boolean = apiKey.isNotBlank()
    
    fun getModelForProvider(): String {
        return when (provider) {
            AIProvider.OPENAI -> "gpt-4o"
            AIProvider.KIMI -> "moonshot-v1-128k"
            AIProvider.QWEN -> "qwen-vl-max"
            AIProvider.GEMINI -> "gemini-1.5-flash"
            AIProvider.ANTHROPIC -> "claude-3-sonnet-20240229"
        }
    }
}

/**
 * 识别结果
 */
data class RecognitionResult(
    val success: Boolean,
    val colors: List<RecognizedColor> = emptyList(),
    val errorMessage: String? = null,
    val rawResponse: String? = null
)

/**
 * 识别的颜色
 */
data class RecognizedColor(
    val colorHex: String,
    val colorName: String?,
    val count: Int,
    val percentage: Float
)

/**
 * AI 识别请求
 */
data class RecognitionRequest(
    val imageBase64: String,
    val gridSize: Int = 32,
    val provider: AIProvider = AIProvider.OPENAI
)

/**
 * AI 管理器
 * 处理所有 AI API 调用
 */
class AIManager(private val context: Context) {
    
    companion object {
        private const val TAG = "AIManager"
        private const val PREFS_NAME = "ai_prefs"
        private const val KEY_PROVIDER = "provider"
        private const val KEY_API_KEY = "api_key"
        private const val KEY_MODEL = "model"
        private const val KEY_MAX_TOKENS = "max_tokens"
        private const val KEY_TEMPERATURE = "temperature"
        
        // 颜色识别提示词
        private val SYSTEM_PROMPT = """
你是一个拼豆图案分析专家。请分析用户上传的图片，识别出图片中的主要颜色，并用MARD色号体系中对应的颜色进行匹配。

请按以下JSON格式返回结果：
{
  "colors": [
    {
      "hex": "#FF5733",
      "mardCode": "M001",
      "colorName": "红色",
      "count": 150,
      "description": "描述这种颜色在图片中的使用情况"
    }
  ],
  "totalPixels": 1024,
  "dominantColor": "#FF5733",
  "pattern": "简单图案"
}

请确保：
1. 只返回真实存在于图片中的颜色
2. 每个颜色尽量匹配到最接近的MARD色号
3. 颜色数量控制在5-20种之间
4. count表示该颜色在简化像素图中的数量
""".trimIndent()
    }
    
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    /**
     * 获取当前配置
     */
    fun getConfig(): AIConfig {
        val providerName = prefs.getString(KEY_PROVIDER, AIProvider.OPENAI.name) ?: AIProvider.OPENAI.name
        return AIConfig(
            provider = AIProvider.entries.find { it.name == providerName } ?: AIProvider.OPENAI,
            apiKey = prefs.getString(KEY_API_KEY, "") ?: "",
            model = prefs.getString(KEY_MODEL, "gpt-4o") ?: "gpt-4o",
            maxTokens = prefs.getInt(KEY_MAX_TOKENS, 4096),
            temperature = prefs.getFloat(KEY_TEMPERATURE, 0.1f)
        )
    }
    
    /**
     * 保存配置
     */
    fun saveConfig(config: AIConfig) {
        prefs.edit().apply {
            putString(KEY_PROVIDER, config.provider.name)
            putString(KEY_API_KEY, config.apiKey)
            putString(KEY_MODEL, config.model)
            putInt(KEY_MAX_TOKENS, config.maxTokens)
            putFloat(KEY_TEMPERATURE, config.temperature)
            apply()
        }
    }
    
    /**
     * 识别图片中的颜色
     */
    suspend fun recognizeColors(bitmap: Bitmap, gridSize: Int = 32): RecognitionResult {
        return withContext(Dispatchers.IO) {
            try {
                val config = getConfig()
                if (!config.isConfigured()) {
                    return@withContext RecognitionResult(
                        success = false,
                        errorMessage = "请先配置 AI API"
                    )
                }
                
                // 缩放图片到指定尺寸
                val scaledBitmap = Bitmap.createScaledBitmap(bitmap, gridSize, gridSize, true)
                val imageBase64 = bitmapToBase64(scaledBitmap)
                
                // 根据不同提供商调用 API
                val result = when (config.provider) {
                    AIProvider.OPENAI -> callOpenAI(imageBase64, config)
                    AIProvider.KIMI -> callKimi(imageBase64, config)
                    AIProvider.QWEN -> callQwen(imageBase64, config)
                    AIProvider.GEMINI -> callGemini(imageBase64, config)
                    AIProvider.ANTHROPIC -> callAnthropic(imageBase64, config)
                }
                
                scaledBitmap.recycle()
                result
            } catch (e: Exception) {
                Log.e(TAG, "识别失败", e)
                RecognitionResult(
                    success = false,
                    errorMessage = "识别失败: ${e.message}"
                )
            }
        }
    }
    
    /**
     * 调用 OpenAI API
     */
    private suspend fun callOpenAI(imageBase64: String, config: AIConfig): RecognitionResult {
        return try {
            val url = URL(config.provider.endpoint)
            val connection = url.openConnection() as HttpURLConnection
            
            connection.apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Authorization", "Bearer ${config.apiKey}")
                doOutput = true
                connectTimeout = 60000
                readTimeout = 120000
            }
            
            val requestBody = JSONObject().apply {
                put("model", config.getModelForProvider())
                put("max_tokens", config.maxTokens)
                put("temperature", config.temperature)
                put("messages", JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "system")
                        put("content", SYSTEM_PROMPT)
                    })
                    put(JSONObject().apply {
                        put("role", "user")
                        put("content", JSONArray().apply {
                            put(JSONObject().apply {
                                put("type", "text")
                                put("text", "请分析这张图片，识别出主要颜色并匹配到MARD色号体系中。图片已简化到32x32像素。")
                            })
                            put(JSONObject().apply {
                                put("type", "image_url")
                                put("image_url", JSONObject().apply {
                                    put("url", "data:image/png;base64,$imageBase64")
                                })
                            })
                        })
                    })
                })
            }
            
            OutputStreamWriter(connection.outputStream).use { writer ->
                writer.write(requestBody.toString())
                writer.flush()
            }
            
            val responseCode = connection.responseCode
            val response = connection.inputStream.bufferedReader().readText()
            
            if (responseCode == 200) {
                parseAIResponse(response)
            } else {
                RecognitionResult(
                    success = false,
                    errorMessage = "API 调用失败: $responseCode - $response",
                    rawResponse = response
                )
            }
        } catch (e: Exception) {
            RecognitionResult(
                success = false,
                errorMessage = "OpenAI API 调用失败: ${e.message}"
            )
        }
    }
    
    /**
     * 调用 Kimi API
     */
    private suspend fun callKimi(imageBase64: String, config: AIConfig): RecognitionResult {
        return try {
            val url = URL(config.provider.endpoint)
            val connection = url.openConnection() as HttpURLConnection
            
            connection.apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Authorization", "Bearer ${config.apiKey}")
                doOutput = true
                connectTimeout = 60000
                readTimeout = 120000
            }
            
            val requestBody = JSONObject().apply {
                put("model", config.getModelForProvider())
                put("max_tokens", config.maxTokens)
                put("temperature", config.temperature)
                put("messages", JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "system")
                        put("content", SYSTEM_PROMPT)
                    })
                    put(JSONObject().apply {
                        put("role", "user")
                        put("content", JSONArray().apply {
                            put(JSONObject().apply {
                                put("type", "text")
                                put("text", "请分析这张图片，识别出主要颜色并匹配到MARD色号体系中。")
                            })
                            put(JSONObject().apply {
                                put("type", "image_url")
                                put("image_url", JSONObject().apply {
                                    put("url", "data:image/png;base64,$imageBase64")
                                })
                            })
                        })
                    })
                })
            }
            
            OutputStreamWriter(connection.outputStream).use { writer ->
                writer.write(requestBody.toString())
                writer.flush()
            }
            
            val responseCode = connection.responseCode
            val response = connection.inputStream.bufferedReader().readText()
            
            if (responseCode == 200) {
                parseAIResponse(response)
            } else {
                RecognitionResult(
                    success = false,
                    errorMessage = "Kimi API 调用失败: $responseCode",
                    rawResponse = response
                )
            }
        } catch (e: Exception) {
            RecognitionResult(
                success = false,
                errorMessage = "Kimi API 调用失败: ${e.message}"
            )
        }
    }
    
    /**
     * 调用通义千问 API
     */
    private suspend fun callQwen(imageBase64: String, config: AIConfig): RecognitionResult {
        return try {
            val url = URL(config.provider.endpoint)
            val connection = url.openConnection() as HttpURLConnection

            connection.apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json")
                // API Key 应通过 Authorization Header 传递，不能暴露在 URL 中
                setRequestProperty("Authorization", "Bearer ${config.apiKey}")
                doOutput = true
                connectTimeout = 60000
                readTimeout = 120000
            }
            
            val requestBody = JSONObject().apply {
                put("model", "qwen-vl-max")
                put("max_tokens", config.maxTokens)
                put("temperature", config.temperature)
                put("messages", JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "system")
                        put("content", SYSTEM_PROMPT)
                    })
                    put(JSONObject().apply {
                        put("role", "user")
                        put("content", JSONArray().apply {
                            put(JSONObject().apply {
                                put("type", "text")
                                put("text", "请分析这张图片，识别出主要颜色并匹配到MARD色号体系中。")
                            })
                            put(JSONObject().apply {
                                put("type", "image_url")
                                put("image_url", mapOf("url" to "data:image/png;base64,$imageBase64"))
                            })
                        })
                    })
                })
            }
            
            OutputStreamWriter(connection.outputStream).use { writer ->
                writer.write(requestBody.toString())
                writer.flush()
            }
            
            val responseCode = connection.responseCode
            val response = connection.inputStream.bufferedReader().readText()
            
            if (responseCode == 200) {
                parseAIResponse(response)
            } else {
                RecognitionResult(
                    success = false,
                    errorMessage = "Qwen API 调用失败: $responseCode",
                    rawResponse = response
                )
            }
        } catch (e: Exception) {
            RecognitionResult(
                success = false,
                errorMessage = "Qwen API 调用失败: ${e.message}"
            )
        }
    }
    
    /**
     * 调用 Gemini API
     */
    private suspend fun callGemini(imageBase64: String, config: AIConfig): RecognitionResult {
        return try {
            val url = URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=${config.apiKey}")
            val connection = url.openConnection() as HttpURLConnection
            
            connection.apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json")
                doOutput = true
                connectTimeout = 60000
                readTimeout = 120000
            }
            
            val requestBody = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", "请分析这张图片，识别出主要颜色并匹配到MARD色号体系中。请按JSON格式返回结果，包含colors数组。")
                            })
                            put(JSONObject().apply {
                                put("inlineData", JSONObject().apply {
                                    put("mimeType", "image/png")
                                    put("data", imageBase64)
                                })
                            })
                        })
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("temperature", config.temperature)
                    put("maxOutputTokens", config.maxTokens)
                })
            }
            
            OutputStreamWriter(connection.outputStream).use { writer ->
                writer.write(requestBody.toString())
                writer.flush()
            }
            
            val responseCode = connection.responseCode
            val response = connection.inputStream.bufferedReader().readText()
            
            if (responseCode == 200) {
                parseGeminiResponse(response)
            } else {
                RecognitionResult(
                    success = false,
                    errorMessage = "Gemini API 调用失败: $responseCode",
                    rawResponse = response
                )
            }
        } catch (e: Exception) {
            RecognitionResult(
                success = false,
                errorMessage = "Gemini API 调用失败: ${e.message}"
            )
        }
    }
    
    /**
     * 调用 Claude API
     */
    private suspend fun callAnthropic(imageBase64: String, config: AIConfig): RecognitionResult {
        return try {
            val url = URL(config.provider.endpoint)
            val connection = url.openConnection() as HttpURLConnection
            
            connection.apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("x-api-key", config.apiKey)
                setRequestProperty("anthropic-version", "2023-06-01")
                setRequestProperty("anthropic-dangerous-direct-browser-access", "true")
                doOutput = true
                connectTimeout = 60000
                readTimeout = 120000
            }
            
            val requestBody = JSONObject().apply {
                put("model", config.model)
                put("max_tokens", config.maxTokens)
                put("temperature", config.temperature)
                put("messages", JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "user")
                        put("content", JSONArray().apply {
                            put(JSONObject().apply {
                                put("type", "text")
                                put("text", "$SYSTEM_PROMPT\n\n请分析这张图片。")
                            })
                            put(JSONObject().apply {
                                put("type", "image")
                                put("source", JSONObject().apply {
                                    put("type", "base64")
                                    put("media_type", "image/png")
                                    put("data", imageBase64)
                                })
                            })
                        })
                    })
                })
            }
            
            OutputStreamWriter(connection.outputStream).use { writer ->
                writer.write(requestBody.toString())
                writer.flush()
            }
            
            val responseCode = connection.responseCode
            val response = connection.inputStream.bufferedReader().readText()
            
            if (responseCode == 200) {
                parseClaudeResponse(response)
            } else {
                RecognitionResult(
                    success = false,
                    errorMessage = "Claude API 调用失败: $responseCode",
                    rawResponse = response
                )
            }
        } catch (e: Exception) {
            RecognitionResult(
                success = false,
                errorMessage = "Claude API 调用失败: ${e.message}"
            )
        }
    }
    
    /**
     * 解析 OpenAI/Kimi/Qwen 响应
     */
    private fun parseAIResponse(response: String): RecognitionResult {
        return try {
            val json = JSONObject(response)
            val content = json.getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content")
            
            // 提取 JSON 部分
            val jsonMatch = Regex("""\{[\s\S]*\}""").find(content)
            val jsonStr = jsonMatch?.value ?: content
            
            parseColorJson(JSONObject(jsonStr))
        } catch (e: Exception) {
            RecognitionResult(
                success = false,
                errorMessage = "解析响应失败: ${e.message}",
                rawResponse = response
            )
        }
    }
    
    /**
     * 解析 Gemini 响应
     */
    private fun parseGeminiResponse(response: String): RecognitionResult {
        return try {
            val json = JSONObject(response)
            val content = json.getJSONArray("candidates")
                .getJSONObject(0)
                .getJSONObject("content")
                .getJSONArray("parts")
                .getJSONObject(0)
                .getString("text")
            
            val jsonMatch = Regex("""\{[\s\S]*\}""").find(content)
            val jsonStr = jsonMatch?.value ?: content
            
            parseColorJson(JSONObject(jsonStr))
        } catch (e: Exception) {
            RecognitionResult(
                success = false,
                errorMessage = "解析 Gemini 响应失败: ${e.message}",
                rawResponse = response
            )
        }
    }
    
    /**
     * 解析 Claude 响应
     */
    private fun parseClaudeResponse(response: String): RecognitionResult {
        return try {
            val json = JSONObject(response)
            val content = json.getJSONArray("content")
                .getJSONObject(0)
                .getString("text")
            
            val jsonMatch = Regex("""\{[\s\S]*\}""").find(content)
            val jsonStr = jsonMatch?.value ?: content
            
            parseColorJson(JSONObject(jsonStr))
        } catch (e: Exception) {
            RecognitionResult(
                success = false,
                errorMessage = "解析 Claude 响应失败: ${e.message}",
                rawResponse = response
            )
        }
    }
    
    /**
     * 解析颜色 JSON
     */
    private fun parseColorJson(json: JSONObject): RecognitionResult {
        return try {
            val colorsArray = json.optJSONArray("colors") ?: JSONArray()
            val colors = mutableListOf<RecognizedColor>()
            var totalCount = 0
            
            for (i in 0 until colorsArray.length()) {
                val colorObj = colorsArray.getJSONObject(i)
                val hex = colorObj.getString("hex")
                val mardCode = colorObj.optString("mardCode", "")
                val count = colorObj.optInt("count", 0)
                
                // 查找对应的颜色信息
                val beadColor = if (mardCode.isNotEmpty()) {
                    BeadColorManager.findByMardCode(mardCode)
                } else {
                    BeadColorManager.findClosestColor(hex)
                }
                
                colors.add(RecognizedColor(
                    colorHex = hex,
                    colorName = beadColor?.colorName ?: colorObj.optString("colorName", "未知"),
                    count = count,
                    percentage = 0f
                ))
                
                totalCount += count
            }
            
            // 计算百分比
            val finalColors = if (totalCount > 0) {
                colors.map { it.copy(percentage = it.count.toFloat() / totalCount * 100) }
            } else emptyList()
            
            RecognitionResult(
                success = true,
                colors = finalColors,
                rawResponse = json.toString()
            )
        } catch (e: Exception) {
            RecognitionResult(
                success = false,
                errorMessage = "解析颜色数据失败: ${e.message}"
            )
        }
    }
    
    /**
     * Bitmap 转 Base64
     */
    private fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }
    
    /**
     * 本地颜色识别（不使用 AI）
     * 使用 K-means 聚类算法识别主要颜色
     */
    fun localColorRecognition(bitmap: Bitmap, gridSize: Int = 32, maxColors: Int = 15): RecognitionResult {
        return try {
            // 缩放图片
            val scaledBitmap = Bitmap.createScaledBitmap(bitmap, gridSize, gridSize, true)
            
            // 提取所有像素颜色
            val pixels = mutableListOf<Int>()
            for (y in 0 until gridSize) {
                for (x in 0 until gridSize) {
                    pixels.add(scaledBitmap.getPixel(x, y))
                }
            }
            
            // 简单的颜色聚类
            val colorCounts = mutableMapOf<Int, Int>()
            for (pixel in pixels) {
                // 简化颜色（减少细微差异）
                val r = (pixel shr 16) and 0xFF
                val g = (pixel shr 8) and 0xFF
                val b = pixel and 0xFF
                val simplified = ((r / 16) * 16 shl 16) or ((g / 16) * 16 shl 8) or ((b / 16) * 16)
                
                colorCounts[simplified] = (colorCounts[simplified] ?: 0) + 1
            }
            
            // 排序并取前 N 个颜色
            val sortedColors = colorCounts.entries
                .sortedByDescending { it.value }
                .take(maxColors)
            
            val totalPixels = pixels.size
            val recognizedColors = sortedColors.map { (color, count) ->
                val r = (color shr 16) and 0xFF
                val g = (color shr 8) and 0xFF
                val b = color and 0xFF
                val hex = String.format("#%02X%02X%02X", r, g, b)
                
                // 匹配到最近的 MARD 颜色
                val beadColor = BeadColorManager.findClosestColor(hex)
                
                RecognizedColor(
                    colorHex = hex,
                    colorName = beadColor?.colorName ?: "未知",
                    count = count,
                    percentage = count.toFloat() / totalPixels * 100
                )
            }
            
            scaledBitmap.recycle()
            
            RecognitionResult(
                success = true,
                colors = recognizedColors
            )
        } catch (e: Exception) {
            RecognitionResult(
                success = false,
                errorMessage = "本地颜色识别失败: ${e.message}"
            )
        }
    }
}
