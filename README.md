# QingDai青黛摄影主页

## 项目简介

- QingDai青黛是一个基于Vue 3 + TypeScript + Spring Boot + MySQL + Redis的Web应用，主要用于展示和管理照片的个人主页
- 现已上线: [qingdai.art](https://qingdai.art)

## 使用指南
- 操作指南
  - 点击头像进入管理页
- 注意事项
  - 请勿随意更改照片文件名与数据库文件名字段
  - 上传图片首先从元数据读取拍摄时间,读取不到时从文件名读取,要求文件名遵循命名示范：20250318-095601-DSC_2046.jpg

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
        ├── components/                    # 通用组件(根据类型使用扁平目录结构)
        ├── data/                          # 静态数据  
        ├── router/                        # 路由配置
        ├── stores/                        # Pinia状态管理
        ├── types/                         # TS类型定义
        ├── utils/                         # 工具函数
        ├── views/                         # 页面组件(根据引用结构使用层级目录结构)
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
  - JWT 认证
  - Swagger/OpenAPI 文档

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
- **后端**：Java 17+, Maven 3.6+, MySQL 8+, Redis 6+

## 配置说明

### 配置文件
- application-dev.yml (已写出)
- application-prod.yml(涉及真实配置 需手动添加)

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
```

- application.yaml    (已写出)
- application.yml (涉及真实配置 需手动添加)

```properties
spring:
  profiles:
    active: dev/prod

jwt:
  # 密钥
  secret: ?
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
```sql
create table photo
(
    id           char(18)                           not null comment 'id'
        primary key,
    title        varchar(255)                       null comment '标题',
    file_name    varchar(255)                       null comment '原图地址',
    author       varchar(255)                       null comment '作者',
    width        int                                null comment '原图宽度',
    height       int                                null comment '原图高度',
    time         varchar(255)                       null comment '拍摄时间',
    aperture     varchar(255)                       null comment '光圈',
    shutter      varchar(255)                       null comment '快门',
    ISO          varchar(255)                       null comment 'iso',
    camera       varchar(255)                       null comment '相机',
    lens         varchar(255)                       null comment '镜头',
    introduce    varchar(255)                       null comment '照片介绍',
    start        int                                null comment '星标',
    created_time datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updated_time datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间'
);

create table group_photo
(
    id           char(18)                           not null comment 'id'
        primary key,
    title        varchar(255)                       null comment '标题',
    introduce    varchar(255)                       null comment '介绍',
    coverPhotoId char(18)                           null comment '封面图id',
    created_time datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updated_time datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    constraint group_photo_photo_id_fk
        foreign key (coverPhotoId) references photo (id)
);

create table group_photo_photo
(
    groupPhotoId char(18)                           null comment '组图id',
    photoId      char(18)                           null comment '照片id',
    created_time datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updated_time datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    constraint group_photo_photo_group_photo_id_fk
        foreign key (groupPhotoId) references group_photo (id),
    constraint group_photo_photo_photo_id_fk
        foreign key (photoId) references photo (id)
);

create index photo_time_index
    on photo (time desc);

create table sys_permission
(
    id           char(18)                           not null
        primary key,
    code         varchar(100)                       not null,
    name         varchar(50)                        not null,
    description  varchar(200)                       null,
    created_time datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updated_time datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    constraint code
        unique (code)
);

create table sys_role
(
    id           char(18)                           not null
        primary key,
    name         varchar(50)                        not null,
    description  varchar(200)                       null,
    created_time datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updated_time datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    constraint name
        unique (name)
);

create table sys_role_permission
(
    role_id       char(18)                           not null,
    permission_id char(18)                           not null,
    created_time  datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updated_time  datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    constraint sys_role_permission_sys_permission_id_fk
        foreign key (permission_id) references sys_permission (id),
    constraint sys_role_permission_sys_role_id_fk
        foreign key (role_id) references sys_role (id)
);

create table sys_user
(
    id           char(18)                           not null
        primary key,
    username     varchar(50)                        not null,
    password     varchar(100)                       not null,
    status       tinyint  default 1                 null comment '0-禁用, 1-启用',
    created_time datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updated_time datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    constraint username
        unique (username)
);

create table sys_user_role
(
    user_id      char(18)                           not null,
    role_id      char(18)                           not null,
    created_time datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updated_time datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    constraint sys_user_role_sys_role_id_fk
        foreign key (role_id) references sys_role (id),
    constraint sys_user_role_sys_user_id_fk
        foreign key (user_id) references sys_user (id)
);

create table timeline
(
    id           char(18)                           not null comment 'id'
        primary key,
    time         varchar(255)                       null comment '时间',
    title        varchar(255)                       null comment '标题',
    text         varchar(255)                       null comment '正文',
    created_time datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updated_time datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间'
);

create index timeline_time_index
    on timeline (time desc);
```
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

```sql
# 权限
INSERT INTO `qingdai-photo`.sys_permission (id, code, name, description, created_time, updated_time) VALUES ('1', '1', '照片管理', '照片管理', NOW(), NOW());
INSERT INTO `qingdai-photo`.sys_permission (id, code, name, description, created_time, updated_time) VALUES ('2', '2', '原图浏览', '原图浏览', NOW(), NOW());

# 角色
INSERT INTO `qingdai-photo`.sys_role (id, name, description, created_time, updated_time) VALUES ('1', 'ADMIN', '管理员', NOW(), NOW());
INSERT INTO `qingdai-photo`.sys_role (id, name, description, created_time, updated_time) VALUES ('2', 'VIEWER', '访客', NOW(), NOW());

# 角色-权限
INSERT INTO `qingdai-photo`.sys_role_permission (role_id, permission_id) VALUES ('1', '1');
INSERT INTO `qingdai-photo`.sys_role_permission (role_id, permission_id) VALUES ('2', '2');

# 用户
INSERT INTO `qingdai-photo`.sys_user (id, username, password, status, created_time, updated_time) VALUES ('1', 'root', '$2a$10$jndgC.sZFv0volqxQeXdk.NGN.K7Smrko/5UtP33pPVUcQdP0604a', 1, NOW(), NOW());
INSERT INTO `qingdai-photo`.sys_user (id, username, password, status, created_time, updated_time) VALUES ('2', 'viewer', '$2a$10$jndgC.sZFv0volqxQeXdk.NGN.K7Smrko/5UtP33pPVUcQdP0604a', 1, NOW(), NOW());

# 用户-角色
INSERT INTO `qingdai-photo`.sys_user_role (user_id, role_id) VALUES ('1', '1');
INSERT INTO `qingdai-photo`.sys_user_role (user_id, role_id) VALUES ('2', '2');
```
