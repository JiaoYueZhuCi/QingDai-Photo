# QingDai 图片管理系统

## 项目概述
前后端分离的图片管理系统，包含：
- 后端服务(QingDai-SP)：基于Spring Boot 3.4.3构建
- 前端项目(QingDai-Vue)：基于Vue3 + Element Plus构建

## 技术栈
### 后端技术
- Java 17
- Spring Boot 3.4.3
- MyBatis-Plus
- Maven

### 前端技术
- Vue 3.5.13
- TypeScript 5.7.3
- Element Plus 2.9.5
- Vite 6.1.0
- Pinia状态管理

## 项目结构
```
qingdai-photo/
├── QingDai-SP/                               # 后端项目
│   ├── src/                                  # 源代码
│   │   ├── main/                             # 主目录
│   │   │   ├── java.com.qingdai/             # 包名
│   │   │   │   ├── config/                   # 配置类
│   │   │   │   ├── controller/               # 控制器
│   │   │   │   ├── dto/                      # 数据传输对象
│   │   │   │   ├── entity/                   # 实体类
│   │   │   │   ├── filter/                   # 过滤器
│   │   │   │   ├── mapper/                   # 数据访问层     
│   │   │   │   ├── service/                  # 业务逻辑层     
│   │   │   │   ├── utils/                    # 工具类
│   │   │   │   ├── QingDaiSpApplication.java # 启动类  
│   │   │   └── resources/                    # 资源文件
│   │   │       ├── mapper/                   # MyBatis映射文件   
│   │   │       ├── static/                   # 静态资源
│   │   │       ├── templates/                # 模板文件
│   │   │       ├── application.yml           # 通用配置
│   │   │       ├── application.yaml          # 通用配置
│   │   │       ├── applicationi-dev.yml      # 开发配置
│   │   │       └── application-prod.yml      # 生产配置
│   │   └── test/                             # 测试目录
│   └── pom.xml                               # Maven配置
└── QingDai-Vue/                              # 前端项目
    ├── public/                               # 公共资源
    │   ├── favicon.ico                       # 网站图标
    │   └── img/                              # 静态图片资源
    ├── src/                                  # 源代码
    │   ├── api/                              # 接口模块
    │   ├── assets/                           # 静态资源
    │   ├── components/                       # 公共组件
    │   ├── map-data/                         # 地图数据
    │   ├── router/                           # 路由配置
    │   ├── stores/                           # 状态管理
    │   ├── types/                            # TS类型定义
    │   └── views/                            # 页面组件
    ├── App.vue                               # 根组件
    ├── index.html                            # 入口HTML文件
    ├── package.json                          # 依赖配置
    ├── tsconfig.json                         # TypeScript配置
    └── vite.config.ts                        # Vite配置
```

## 环境要求
- JDK 17+
- Node.js 18+
- MySQL 8.0+

