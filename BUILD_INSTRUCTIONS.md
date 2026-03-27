# 秀拼豆 - Android 项目构建指南

## 项目完成状态

所有源代码已完成，包含以下模块：

### 已完成的源代码文件

| 文件 | 功能 |
|------|------|
| `domain/model/Models.kt` | 核心数据模型（像素、项目、工具） |
| `domain/model/PerlerColorPalette.kt` | 80+种专业拼豆色库 |
| `domain/usecase/ImageProcessingUseCases.kt` | 图片转像素画算法 |
| `domain/usecase/ProjectUseCases.kt` | 绘图、填充、统计用例 |
| `data/repository/ExportRepository.kt` | PNG/PDF 导出功能 |
| `ui/components/PixelCanvas.kt` | 像素编辑画布组件 |
| `ui/components/ColorComponents.kt` | 颜色选择器组件 |
| `ui/components/DrawingToolbar.kt` | 绘图工具栏组件 |
| `ui/screens/EditorScreen.kt` | 编辑器主界面 |
| `ui/screens/EditorViewModel.kt` | 编辑器状态管理 |
| `ui/MainActivity.kt` | 应用入口 |
| `ui/theme/` | Material 3 主题 |

---

## 在 Android Studio 中构建

### 方法一：打开项目

1. 打开 **Android Studio**
2. 选择 **File → Open**
3. 选择项目根目录 `/Users/saicc/pyProject/1251`
4. 等待 Gradle 同步完成（首次可能需要下载依赖）
5. 点击 **Run → Run 'app'** 或按 **Shift + F10**

### 方法二：命令行构建

需要先安装 **Gradle Wrapper**：

```bash
# 进入项目目录
cd /Users/saicc/pyProject/1251

# 使用 Gradle 8.4 创建 wrapper（需要网络）
gradle wrapper --gradle-version 8.4

# 或直接使用系统 Gradle
gradle assembleDebug
```

---

## 环境要求

### 必须安装

1. **JDK 17+**
   ```bash
   # macOS
   brew install openjdk@17
   
   # 设置 JAVA_HOME
   export JAVA_HOME=$(/usr/libexec/java_home -v 17)
   ```

2. **Android SDK** (API 34)
   - 下载 Android Studio 时会自动安装
   - 或单独下载：https://developer.android.com/studio

3. **Gradle 8.4+**
   ```bash
   brew install gradle
   ```

---

## 构建输出

- **调试 APK**: `app/build/outputs/apk/debug/app-debug.apk`
- **发布 APK**: `app/build/outputs/apk/release/app-release.apk`

---

## 常见问题

### 1. Gradle 同步失败

检查网络连接，或配置国内镜像：

```properties
# ~/.gradle/init.gradle
allprojects {
    repositories {
        maven { url 'https://maven.aliyun.com/repository/google' }
        maven { url 'https://maven.aliyun.com/repository/public' }
        google()
        mavenCentral()
    }
}
```

### 2. SDK 未找到

确保设置 `ANDROID_HOME`：
```bash
export ANDROID_HOME=~/Library/Android/sdk
```

### 3. Java 版本不兼容

确保使用 JDK 17：
```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
```

---

## 下一步

1. 在 Android Studio 中打开项目
2. 等待依赖下载完成
3. 连接 Android 设备或启动模拟器
4. 运行应用进行测试

如有构建问题，请提供完整的错误信息。
