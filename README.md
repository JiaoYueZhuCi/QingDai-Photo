# QingDai青黛影像

[English Documentation](README-en.md)   |  [中文文档](README.md)

仓库：[Github](https://github.com/JiaoYueZhuCi/QingDai-Photo) | [Gitee](https://gitee.com/JiaoYueZhuCi/QingDai-Photo)
## 项目简介

- QingDai青黛影像是一个基于Vue 3 + TypeScript + Spring Boot + MySQL + Redis的Web应用，主要用于展示和管理照片的摄影个人主页
- 现已上线: [qingdai.art](https://qingdai.art)

## 功能
- 展示
  - 展示个人活跃信息、联系方式、设备等
  - 展示图片及组图使用瀑布流
    - 分照片质量展示以优化性能
  - 展示时间轴信息包括足迹
  - 通过组图展示记录过的朝霞/晚霞/日出/日落的专辑
  - 展示个人拍摄数据(相机/无人机、相机/镜头型号、气象照片类型、照片分级、焦距/ISO/快门/光圈分布、月度/年度拍摄数量及变化)
  - 展示个人足迹数据(已拍摄省份)
- 分享
  - 选取照片进行分享  
- 管理
  - 增删改图片/组图,自动图片压缩
  - 增删改时间轴
  - 增删改分享信息
  - 对数据库与文件对应异常的进行扫描
  - 一键显示所有丢失了exif的数据
  - 手机/无人机的exif的相机信息会类似'FC9113',可替换实际的设备/焦距名称
- 用户
  - 管理员登陆
  - 浏览者登陆(可对管理页进行参观但无操作权限)  
- 日夜切换
  - 切换日夜主题


## 使用指南
- 操作指南
  - 点击头像进入管理页
  - 移动端的页头可以左右滑动
- 注意事项
  - 管理页没有适配移动端
  - 请勿随意手动更改照片文件名与数据库文件名字段
  - 上传图片首先从元数据读取拍摄时间,读取不到时从文件名读取,要求文件名遵循命名示范：20250318-095601-DSC_2046.jpg
  - 前端目录views ​​components根据引用结构使用层级目录结构，
  组件非单次引用放在​​components，组件路由挂载/单次引用时放在​views

## 项目结构 

本项目由两个主要模块组成：

### QingDai-Vue (前端)

- **技术栈**：
  - Vue 3 + TypeScript
  - Pinia 状态管理
  - Vue Router
  - Element Plus UI组件库
  - Three.js 3D渲染
  - GSAP 动画库
  - Axios 网络请求
  - vue-verification-code   

- **目录结构**：
  ```  
  ├── pubic/
        ├── img/                           
            ├── home     
                ├── avatar.jpg             # 头像
                └── background.jpg         # 背景图
            └── introduce                                     
        └── favicon.ico                    # 图标        
  ├── src/
        ├── api/                           # 接口封装
        ├── assets/                        # 静态资源
            └── css/    
                ├── el/                    # 自定义的ElementPlus的组件样式
                ├── jicui.css              # 备选主题色(吉翠)
                ├── main.css               # 全局css
                └── qingdai.css            # 主题色(青黛)
        ├── components/                    # 通用组件
        ├── data/                          # 静态数据  
        ├── router/                        # 路由配置
        ├── stores/                        # Pinia状态管理
        ├── types/                         # TS类型定义
        ├── utils/                         # 工具函数
        ├── views/                         # 页面组件
        ├── main.ts                        # 入口文件
        └── shims-vue.d.ts                 # vue类型声明
  ├── App.vue
  ├── index.html
  ├── package.json
  ├── tsconfig.json
  └── vite.config.ts
  ```


### QingDai-SP (后端)

- **技术栈**：
  - Java 17
  - Spring Boot 3.4
  - Spring Security
  - MyBatis-Plus
  - MySQL 数据库
  - Redis 缓存
  - RocketMQ 消息队列
  - JWT 认证
  - Swagger/OpenAPI 文档
  - kaptcha

- **目录结构**：
  ```
   src
    ├── main
        ├── java/come/qingdai
            ├── config/                   # 配置类
            ├── controller/               # REST API
            ├── entity/                   # 数据库实体
            │   └── dto/                  # DTO 
            ├── filter/                   # 过滤器   
            ├── mapper/                   # MyBatis接口
            ├── mq/                       # RocketMQ相关
            │   ├── consumer/             # 消息消费者
            │   └── producer/             # 消息生产者
            ├── service/                  # 业务逻辑
            │   └── impl/                 # 服务实现
            ├── utils/                    # 工具类
            └── QingDaiSpApplication.java # 启动类
        └── resources
            ├── mapper/                   # MyBatis映射文件   
            ├── static/                   # 静态资源 
            ├── application-dev.yml       # 开发环境配置
            ├── application-prod.yml      # 生产环境配置
            ├── application.yaml          # 默认配置
            └── application.yml           # 默认配置    
    └── test                              # 测试类
  ```

## 系统要求

- **前端**：Node.js 16+, npm 8+
- **后端**：Java 17+, Maven 3.6+, MySQL 8+, Redis 6+, RocketMQ 4.8+

## 配置说明

### 配置文件(涉及真实配置 需手动添加文件)
- QingDai-Photo/QingDai-SP/src/main/resources/application-prod.yml

```properties
spring:
  # mysql
  datasource:
    url: ?
    username: ?
    password: ?
  # redis  
  data:
    redis:
      host: ?
      port: ?
      password: ?
      database: 0

# 图片文件路径
qingdai:
  url: ?
  fullSizeUrl: ?
  thumbnail1000KUrl: ?
  thumbnail100KUrl: ?
  thumbnailSizeUrl: ?
  pendingUrl: ?

#RocketMQ配置
rocketmq:
  name-server: ?
  producer:
    access-key: ?
    secret-key: ?
  consumer:
    access-key: ?
    secret-key: ?
```
- QingDai-Photo/QingDai-SP/src/main/resources/application.yml

```properties
spring:
  profiles:
    active: prod

jwt:
  # 密钥
  secret: QingDai
```

- QingDai-Photo/QingDai-Vue/src/data/beian.ts
```ts
// 备案信息
export const beianInfo = {
  number: '',
  link: 'https://beian.miit.gov.cn/'
};
```
### 推荐图片目录
```
  ├── QingDaiPhotos/
        ├── FullSize/                           
        ├── Pending/                           
        ├── Thumbnail/                           
        ├── Thumbnail-100K/                           
        └── Thumbnail-1000K/                           
```

## 数据库

### Mysql

#### 表结构
请查看 [database/init.sql](database/init.sql) 文件，其中包含完整的数据库结构和初始化数据。

#### 用户数据
1. 角色与权限
   - ADMIN（id=1）拥有权限「照片管理」
   - VIEWER（id=2）拥有权限「原图浏览」
   - 通过 sys_role_permission 关联

2. 用户与角色
   - root用户（id=1）同时拥有 ADMIN 和 VIEWER 角色
   - viewer用户（id=2）拥有  VIEWER 角色
   - 通过 sys_user_role 关联

3. 密码说明：
   - 所有用户密码均为明文 "123456" 的BCrypt加密结果:$2a$10$jndgC.sZFv0volqxQeXdk.NGN.K7Smrko/5UtP33pPVUcQdP0604a（根据配置文件jwt.secret:QingDai）

#### 特殊字段
1. photo.start_rating
  - -1 隐藏
  - 0 普通
  - 1 精选
  - 2 气象

2. group_photo.id
 - 1 朝霞
 - 2 晚霞
 - 3 日出
 - 4 日落
