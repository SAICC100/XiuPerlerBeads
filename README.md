# 秀拼豆 - Perler Beads Pixel Art Generator

一款专为拼豆爱好者设计的 Android 应用，支持将图片转换为像素画图纸。

## 功能特性

- **图片转像素画** - 导入照片，自动转换为拼豆风格的像素图案
- **多尺寸画布** - 支持 16x16 到 256x256 尺寸调节
- **像素编辑** - 画笔、橡皮擦、填充等精细编辑工具
- **专业色库** - 内置 80+ 种拼豆品牌色号，精准匹配真实豆子
- **色号统计** - 自动计算每种颜色所需豆子数量
- **颜色优化** - 合并相近色、精简颜色数量
- **高清导出** - PNG/PDF 格式图纸，带完整色号标注

## 项目结构

```
app/
├── src/main/
│   ├── java/com/example/xiuperlerbeads/
│   │   ├── XiuPerlerBeadsApp.kt           # Application 类
│   │   ├── domain/
│   │   │   ├── model/
│   │   │   │   ├── Models.kt              # 核心数据模型
│   │   │   │   └── PerlerColorPalette.kt  # 拼豆色库 (80+ 色)
│   │   │   └── usecase/
│   │   │       ├── ImageProcessingUseCases.kt  # 图片处理用例
│   │   │       └── ProjectUseCases.kt      # 绘图/统计用例
│   │   ├── data/repository/
│   │   │   └── ExportRepository.kt        # 导出功能
│   │   └── ui/
│   │       ├── MainActivity.kt            # 主入口
│   │       ├── theme/
│   │       │   ├── Color.kt               # 主题颜色
│   │       │   └── Theme.kt                # 主题定义
│   │       ├── components/
│   │       │   ├── PixelCanvas.kt         # 像素画布组件
│   │       │   ├── ColorComponents.kt     # 颜色选择器
│   │       │   └── DrawingToolbar.kt      # 工具栏
│   │       └── screens/
│   │           ├── EditorScreen.kt        # 编辑器界面
│   │           └── EditorViewModel.kt      # 编辑器状态管理
│   └── res/                                # 资源文件
├── build.gradle.kts                        # Gradle 构建配置
└── proguard-rules.pro                      # 混淆规则
```

## 技术栈

- **语言**: Kotlin
- **UI 框架**: Jetpack Compose + Material 3
- **最小 SDK**: API 24 (Android 7.0)
- **目标 SDK**: API 34 (Android 14)
- **架构**: MVVM + Clean Architecture

## 构建说明

### 环境要求

1. **JDK 17+**
2. **Android SDK** (API Level 34)
3. **Gradle 8.2+**

### 快速开始

#### 方法一：使用 Android Studio

1. 用 Android Studio 打开项目根目录
2. 等待 Gradle 同步完成
3. 点击 Run → Run 'app' 或按 Shift + F10

#### 方法二：命令行构建

```bash
# 安装 Gradle (如果未安装)
brew install gradle

# 或使用项目内的 gradlew (推荐)
chmod +x gradlew
./gradlew assembleDebug

# 发布版本
./gradlew assembleRelease
```

### 签名配置 (发布版本)

在 `~/.gradle/gradle.properties` 中配置:

```properties
 keystorePath=/path/to/your/keystore.jks
 keystorePassword=your_password
 keyAlias=your_alias
 keyPassword=your_key_password
```

## APK 输出位置

- **调试版本**: `app/build/outputs/apk/debug/app-debug.apk`
- **发布版本**: `app/build/outputs/apk/release/app-release.apk`

## 使用说明

1. **新建项目**: 点击 + 按钮创建空白画布
2. **导入图片**: 点击图片按钮导入照片自动生成图纸
3. **编辑图纸**: 使用画笔、橡皮擦、填充工具调整
4. **选择颜色**: 点击颜色按钮打开色库面板
5. **精简颜色**: 点击压缩按钮减少颜色数量
6. **导出图纸**: 选择 PNG 或 PDF 格式导出

## 色号体系

应用内置了 80 种专业拼豆色号，涵盖：
- 红色系 (10 种)
- 橙黄色系 (9 种)
- 绿色系 (11 种)
- 蓝色系 (10 种)
- 紫色系 (10 种)
- 棕色系 (8 种)
- 特殊色 (8 种)
- 莫兰迪色 (6 种)
- 自然色 (8 种)

## License

MIT License
