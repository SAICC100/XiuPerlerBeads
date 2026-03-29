# 秀拼豆 Pro - 流程图与架构图

> Mermaid 格式图表

---

## 1. App 整体架构

```mermaid
flowchart TB
    subgraph APP["秀拼豆 Pro"]
        direction TB
        
        subgraph NAV["底部导航"]
            HOME["🏠 首页"]
            CREATE["📐 创作"]
            INVENTORY["📦 库存"]
            PROJECTS["📁 项目"]
        end

        subgraph HOME_CONTENT["首页模块"]
            H1["搜索栏"]
            H2["快捷操作"]
            H3["当前项目"]
            H4["库存预警"]
            H5["最近项目"]
        end

        subgraph CREATE_CONTENT["创作模块"]
            C1["手绘画布"]
            C2["图片转像素"]
            C3["素材库"]
        end

        subgraph INVENTORY_CONTENT["库存模块"]
            I1["库存列表"]
            I2["入库管理"]
            I3["出库管理"]
            I4["数据报表"]
            I5["补货建议"]
        end

        subgraph PROJECT_CONTENT["项目模块"]
            P1["项目列表"]
            P2["AI识别"]
            P3["导出功能"]
        end
    end

    HOME --> HOME_CONTENT
    CREATE --> CREATE_CONTENT
    INVENTORY --> INVENTORY_CONTENT
    PROJECTS --> PROJECT_CONTENT

    style NAV fill:#e3f2fd,stroke:#1976d2
    style HOME_CONTENT fill:#e8f5e9,stroke:#388e3c
    style CREATE_CONTENT fill:#fff3e0,stroke:#f57c00
    style INVENTORY_CONTENT fill:#fce4ec,stroke:#c2185b
    style PROJECT_CONTENT fill:#e1f5fe,stroke:#0288d1
```

---

## 2. 用户旅程

```mermaid
journey
    title 拼豆创作用户旅程
    
    section 新手用户
      打开App: 5: 新手
      创建第一个项目: 4: 新手
      尝试画布工具: 3: 新手
      保存项目: 4: 新手

    section 日常创作
      导入图片: 5: 爱好者
      调整颜色: 4: 爱好者
      关联库存: 3: 爱好者
      检查缺货: 4: 爱好者

    section 完成创作
      导出图纸: 5: 爱好者
      查看材料清单: 4: 爱好者
      补货采购: 3: 爱好者
```

---

## 3. 创作流程

```mermaid
flowchart TB
    START([开始]) --> MODE{选择模式}
    
    MODE -->|手绘| CANVAS[打开画布]
    MODE -->|导入| IMPORT[导入图片]
    MODE -->|素材| TEMPLATE[选择模板]
    
    CANVAS --> DRAW[绘制像素]
    IMPORT --> SIZE[选择尺寸]
    SIZE --> PREVIEW[预览效果]
    PREVIEW -->|满意| SAVE1[保存项目]
    PREVIEW -->|不满意| ADJUST[调整参数]
    ADJUST --> PREVIEW
    TEMPLATE --> MODIFY[修改颜色]
    
    DRAW --> COLOR[选择颜色]
    COLOR --> DRAW
    MODIFY --> SAVE1
    
    SAVE1 --> CHECK{库存检查}
    
    CHECK -->|充足| EXPORT[选择导出格式]
    CHECK -->|不足| WARN[显示缺货警告]
    
    WARN --> SUGGEST[查看补货建议]
    SUGGEST --> ADD[添加采购清单]
    ADD --> EXPORT
    
    EXPORT -->|PNG| PNG_IMG[保存图片]
    EXPORT -->|PDF| PDF_DOC[生成图纸]
    EXPORT -->|LIST| SHOP_LIST[生成采购清单]
    
    PNG_IMG --> END([完成])
    PDF_DOC --> END
    SHOP_LIST --> END

    style START fill:#e3f2fd
    style CHECK fill:#fff3e0
    style WARN fill:#ffebee,stroke:#f44336
    style END fill:#e8f5e9
```

---

## 4. 库存管理流程

```mermaid
flowchart LR
    subgraph ACTIONS["库存操作"]
        direction TB
        
        IN[➕ 入库] --> UPDATE[更新库存]
        OUT[➖ 出库] --> UPDATE
        AI[🤖 AI识别] --> DEDUCT[自动扣减]
        
        UPDATE --> CHECK{检查阈值}
        DEDUCT --> CHECK
    end

    subgraph THRESHOLDS["库存状态"]
        direction TB
        
        ENOUGH{>50颗}
        LOW{10-50颗}
        OUT{>10颗}
        EMPTY{<10颗}
    end

    subgraph STATUS["状态显示"]
        direction TB
        
        OK[🟢 充足]
        WARN[🟡 不足]
        CRIT[🔴 缺货]
    end

    CHECK -->|>50| ENOUGH --> OK
    CHECK -->|10-50| LOW --> WARN
    CHECK -->|<10| OUT --> CRIT

    style OK fill:#e8f5e9,stroke:#388e3c
    style WARN fill:#fff8e1,stroke:#f9a825
    style CRIT fill:#ffebee,stroke:#f44336
```

---

## 5. 数据模型关系

```mermaid
erDiagram
    PROJECT ||--o{ COLOR_USAGE : "使用"
    PROJECT ||--o{ PIXEL : "包含"
    
    PROJECT {
long id PK
        string name
        int width
        int height
        long created_at
        long updated_at
    }
    
    PIXEL {
        int x
        int y
        int color_id FK
    }
    
    COLOR_USAGE {
        long id PK
        long project_id FK
        int color_id FK
        int count
    }
    
    BEAD_INVENTORY ||--o{ INVENTORY_RECORD : "记录"
    
    BEAD_INVENTORY {
        int color_id PK
        int quantity
        long updated_at
    }
    
    INVENTORY_RECORD {
        long id PK
        int color_id FK
        string type "IN/OUT"
        int quantity
        string note
        long created_at
    }
    
    PERLER_COLOR {
        int id PK
        string name
        int red
        int green
        int blue
        string color_code "P001"
    }
    
    BEAD_INVENTORY ||--|| PERLER_COLOR : "颜色"
    COLOR_USAGE ||--|| PERLER_COLOR : "颜色"
    PIXEL ||--|| PERLER_COLOR : "颜色"
```

---

## 6. 状态机

```mermaid
stateDiagram-v2
    [*] --> 首页
    
    首页 --> 创作中心: 点击创作
    首页 --> 库存管理: 点击库存
    首页 --> 项目列表: 点击项目
    首页 --> 画布编辑: 点击继续编辑
    
    创作中心 --> 手绘画布: 手绘
    创作中心 --> 图片导入: 导入
    创作中心 --> 素材库: 素材
    
    手绘画布 --> 颜色选择: 选择颜色
    手绘画布 --> 使用工具: 画/擦/填
    手绘画布 --> 保存项目: 保存
    手绘画布 --> 库存检查: 完成
    
    图片导入 --> 选择尺寸
    图片导入 --> 调整参数
    图片导入 --> 保存项目
    
    库存检查 --> 导出: 库存充足
    库存检查 --> 补货建议: 库存不足
    
    补货建议 --> 添加清单
    添加清单 --> 导出
    
    导出 --> [*]
    
    state 库存检查 {
        [*] --> 检查库存
        检查库存 --> 充足: 全部>50
        检查库存 --> 不足: 部分<50
        检查库存 --> 缺货: 任何<10
    }
```

---

## 7. 屏幕流程

```mermaid
flowchart TB
    subgraph LAUNCH["启动"]
        SPLASH["启动页"] --> PRIVACY["隐私协议"]
        PRIVACY --> HOME["首页"]
    end

    subgraph HOME_FLOW["首页流程"]
        HOME -->|快捷操作| CREATE["创作中心"]
        HOME -->|当前项目| EDITOR["画布编辑"]
        HOME -->|库存预警| RESTOCK["补货建议"]
    end

    subgraph CREATE_FLOW["创作流程"]
        CREATE -->|手绘| CANVAS["画布编辑器"]
        CREATE -->|导入| IMPORT["图片导入"]
        CREATE -->|素材| LIBRARY["素材库"]
        
        CANVAS -->|完成| CHECK["库存检查"]
        IMPORT --> CHECK
        LIBRARY -->|修改| CANVAS
    end

    subgraph INVENTORY_FLOW["库存流程"]
        HOME -->|库存Tab| INV["库存列表"]
        INV -->|入库| ADD["入库录入"]
        INV -->|出库| SUB["出库录入"]
        INV -->|报表| REPORT["数据报表"]
    end

    subgraph EXPORT_FLOW["导出流程"]
        CHECK -->|导出| EXPORT["导出选项"]
        EXPORT -->|PNG| PNG["保存图片"]
        EXPORT -->|PDF| PDF["生成图纸"]
        EXPORT -->|清单| LIST["采购清单"]
    end

    style LAUNCH fill:#e3f2fd
    style HOME_FLOW fill:#e8f5e9
    style CREATE_FLOW fill:#fff3e0
    style INVENTORY_FLOW fill:#fce4ec
    style EXPORT_FLOW fill:#e1f5fe
```

---

## 8. 模块依赖

```mermaid
flowchart TB
    subgraph LAYERS["分层架构"]
        direction TB
        
        UI["UI 层 (Compose)"]
        VM["ViewModel 层"]
        USECASE["用例 层"]
        REPO["仓库 层"]
        DATA["数据 层"]
    end

    subgraph PACKAGES["包结构"]
        direction LR
        
        UI -->|"ui/screens"| SCREENS["screens/"]
        UI -->|"ui/components"| COMPONENTS["components/"]
        UI -->|"ui/theme"| THEME["theme/"]
        
        VM -->|"screens/*.kt"| VIEWMODELS["*ViewModel.kt"]
        
        USECASE -->|"domain/usecase"| USECASES["usecase/"]
        
        REPO -->|"data/repository"| REPOS["repository/"]
        
        DATA -->|"data/local"| LOCAL["local/ (Room)"]
        DATA -->|"data/model"| MODELS["model/"]
    end

    UI --> VM
    VM --> USECASE
    USECASE --> REPO
    REPO --> DATA

    style UI fill:#e3f2fd
    style VM fill:#e8f5e9
    style USECASE fill:#fff3e0
    style REPO fill:#fce4ec
    style DATA fill:#e1f5fe
```

---

## 9. 功能优先级矩阵

```mermaid
quadrantChart
    title 功能优先级
    x-axis 低实现难度 --> 高实现难度
    y-axis 低用户价值 --> 高用户价值
    
    "手绘画布": [0.3, 0.8]
    "颜色选择": [0.2, 0.9]
    "项目保存": [0.4, 0.85]
    "图片导入": [0.5, 0.9]
    "导出PNG": [0.3, 0.75]
    "导出PDF": [0.7, 0.8]
    "库存列表": [0.4, 0.85]
    "入库管理": [0.5, 0.8]
    "补货建议": [0.6, 0.9]
    "AI识别": [0.8, 0.95]
    "素材库": [0.6, 0.7]
    "数据报表": [0.5, 0.65]
```

---

*图表将根据项目迭代持续更新*
