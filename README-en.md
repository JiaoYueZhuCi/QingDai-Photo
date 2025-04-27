# QingDai Photography

[English Documentation](README-en.md)     [中文文档](README.md)

## Project Introduction

- QingDai is a Web application based on Vue 3 + TypeScript + Spring Boot + MySQL + Redis, mainly used for displaying and managing a photography portfolio
- Now online: [qingdai.art](https://qingdai.art)

## Features
- Display
  - Show personal activity information, contact details, equipment, etc.
  - Display images and photo sets in a waterfall layout
    - Optimize performance based on photo quality
  - Display timeline information including footprints
  - Display albums of sunrise/sunset/dawn/dusk through photo sets
  - Display personal photography data (camera/drone, camera/lens model, weather photo type, photo grading, focal length/ISO/shutter/aperture distribution, monthly/yearly shooting quantity and changes)
  - Display personal footprint data (provinces photographed)
- Sharing
  - Select photos for sharing
- Management
  - Add, delete, and modify photos/photo sets, automatic image compression
  - Add, delete, and modify timeline
  - Add, delete, and modify sharing information
  - Scan for anomalies between database and corresponding files
  - One-click display of all data missing EXIF information
  - Camera information in phone/drone EXIF data may look like 'FC9113', can be replaced with actual device/focal length names
- Users
  - Administrator login
  - Viewer login (can visit management pages but has no operation rights)
- Day/Night Switch
  - Toggle between day and night themes

## Usage Guide
- Operation Guide
  - Click the avatar to enter the management page
  - The header on mobile devices can be swiped left and right
- Notes
  - Management page is not optimized for mobile devices
  - Please do not manually change photo filenames and database filename fields
  - When uploading images, the shooting time is first read from metadata, if unavailable it's read from the filename, which should follow the naming example: 20250318-095601-DSC_2046.jpg
  - In the frontend directory, views and components use a hierarchical directory structure based on reference structure,
  components with multiple references are placed in components, components mounted on routes/referenced once are placed in views

## Project Structure 

This project consists of two main modules:

### QingDai-Vue (Frontend)

- **Tech Stack**:
  - Vue 3 + TypeScript
  - Pinia state management
  - Vue Router
  - Element Plus UI component library
  - Three.js 3D rendering
  - GSAP animation library
  - Axios network requests

- **Directory Structure**:
  ```  
  ├── pubic/
        ├── img/                           
            ├── home     
                ├── avatar.jpg             # Avatar
                └── background.jpg         # Background image
            └── introduce                                     
        └── favicon.ico                    # Icon        
  ├── src/
        ├── api/                           # API encapsulation
        ├── assets/                        # Static resources
            └── css/    
                ├── el/                    # Custom styles for ElementPlus components
                ├── jicui.css              # Alternative theme color (Jicui)
                ├── main.css               # Global CSS
                └── qingdai.css            # Main theme color (Qingdai)
        ├── components/                    # Common components
        ├── data/                          # Static data  
        ├── router/                        # Router configuration
        ├── stores/                        # Pinia state management
        ├── types/                         # TS type definitions
        ├── utils/                         # Utility functions
        ├── views/                         # Page components
        ├── main.ts                        # Entry file
        └── shims-vue.d.ts                 # Vue type declarations
  ├── App.vue
  ├── index.html
  ├── package.json
  ├── tsconfig.json
  └── vite.config.ts
  ```

### QingDai-SP (Backend)

- **Tech Stack**:
  - Java 17
  - Spring Boot 3.4
  - Spring Security
  - MyBatis-Plus
  - MySQL Database
  - Redis Cache
  - JWT Authentication
  - Swagger/OpenAPI Documentation

- **Directory Structure**:
  ```
   src
    ├── main
        ├── java/come/qingdai
            ├── config/                   # Configuration classes
            ├── controller/               # REST API
            ├── entity/                   # Database entities
            │   └── dto/                  # DTOs 
            ├── filter/                   # Filters   
            ├── mapper/                   # MyBatis interfaces
            ├── service/                  # Business logic
            │   └── impl/                 # Service implementations
            ├── utils/                    # Utility classes
            └── QingDaiSpApplication.java # Start-up class
        └── resources
            ├── mapper/                   # MyBatis mapping files   
            ├── static/                   # Static resources 
            ├── application-dev.yml       # Development environment configuration
            ├── application-prod.yml      # Production environment configuration
            ├── application.yaml          # Default configuration
            └── application.yml           # Default configuration    
    └── test                              # Test classes
  ```

## System Requirements

- **Frontend**: Node.js 16+, npm 8+
- **Backend**: Java 17+, Maven 3.6+, MySQL 8+, Redis 6+

## Configuration Instructions

### Configuration Files (Involves actual configuration that needs to be added manually)
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

# Image file paths
qingdai:
  url: ?
  fullSizeUrl: ?
  thumbnail1000KUrl: ?
  thumbnail100KUrl: ?
  thumbnailSizeUrl: ?
  pendingUrl: ?
```
- QingDai-Photo/QingDai-SP/src/main/resources/application.yml

```properties
spring:
  profiles:
    active: dev/prod

jwt:
  # Secret key
  secret: QingDai
```

- QingDai-Photo/QingDai-Vue/src/data/beian.ts
```ts
// ICP Filing information
export const beianInfo = {
  number: '',
  link: 'https://beian.miit.gov.cn/'
};
```
### Recommended Image Directory Structure
```
  ├── QingDaiPhotos/
        ├── FullSize/                           
        ├── Pending/                           
        ├── Thumbnail/                           
        ├── Thumbnail-100K/                           
        └── Thumbnail-1000K/                           
```

## Database

### MySQL

#### Table Structure
```sql
create table photo
(
    id           char(18)                           not null comment 'id'
        primary key,
    title        varchar(255)                       null comment 'title',
    file_name    varchar(255)                       null comment 'original image path',
    author       varchar(255)                       null comment 'author',
    width        int                                null comment 'original width',
    height       int                                null comment 'original height',
    time         varchar(255)                       null comment 'shooting time',
    aperture     varchar(255)                       null comment 'aperture',
    shutter      varchar(255)                       null comment 'shutter',
    ISO          varchar(255)                       null comment 'iso',
    camera       varchar(255)                       null comment 'camera',
    lens         varchar(255)                       null comment 'lens',
    introduce    varchar(255)                       null comment 'photo description',
    start        int                                null comment 'star rating',
    created_time datetime default CURRENT_TIMESTAMP null comment 'created time',
    updated_time datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment 'updated time',
    focal_length varchar(255)                       null comment 'focal length'
);

create table group_photo
(
    id           char(18)                           not null comment 'id'
        primary key,
    title        varchar(255)                       null comment 'title',
    introduce    varchar(255)                       null comment 'description',
    coverPhotoId char(18)                           null comment 'cover photo id',
    created_time datetime default CURRENT_TIMESTAMP null comment 'created time',
    updated_time datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment 'updated time',
    constraint group_photo_photo_id_fk
        foreign key (coverPhotoId) references photo (id)
);

create table group_photo_photo
(
    groupPhotoId char(18)                           null comment 'photo set id',
    photoId      char(18)                           null comment 'photo id',
    created_time datetime default CURRENT_TIMESTAMP null comment 'created time',
    updated_time datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment 'updated time',
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
    created_time datetime default CURRENT_TIMESTAMP null comment 'created time',
    updated_time datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment 'updated time',
    constraint code
        unique (code)
);

create table sys_role
(
    id           char(18)                           not null
        primary key,
    name         varchar(50)                        not null,
    description  varchar(200)                       null,
    created_time datetime default CURRENT_TIMESTAMP null comment 'created time',
    updated_time datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment 'updated time',
    constraint name
        unique (name)
);

create table sys_role_permission
(
    role_id       char(18)                           not null,
    permission_id char(18)                           not null,
    created_time  datetime default CURRENT_TIMESTAMP null comment 'created time',
    updated_time  datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment 'updated time',
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
    status       tinyint  default 1                 null comment '0-disabled, 1-enabled',
    created_time datetime default CURRENT_TIMESTAMP null comment 'created time',
    updated_time datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment 'updated time',
    constraint username
        unique (username)
);

create table sys_user_role
(
    user_id      char(18)                           not null,
    role_id      char(18)                           not null,
    created_time datetime default CURRENT_TIMESTAMP null comment 'created time',
    updated_time datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment 'updated time',
    constraint sys_user_role_sys_role_id_fk
        foreign key (role_id) references sys_role (id),
    constraint sys_user_role_sys_user_id_fk
        foreign key (user_id) references sys_user (id)
);

create table timeline
(
    id           char(18)                           not null comment 'id'
        primary key,
    time         varchar(255)                       null comment 'time',
    title        varchar(255)                       null comment 'title',
    text         varchar(255)                       null comment 'content',
    created_time datetime default CURRENT_TIMESTAMP null comment 'created time',
    updated_time datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment 'updated time'
);

create index timeline_time_index
    on timeline (time desc);
```
#### User Data
1. Roles and Permissions
   - ADMIN (id=1) has permission "Photo Management"
   - VIEWER (id=2) has permission "Original Image Viewing"
   - Connected through sys_role_permission

2. Users and Roles
   - root user (id=1) has both ADMIN and VIEWER roles
   - viewer user (id=2) has VIEWER role
   - Connected through sys_user_role

3. Password Note:
   - All user passwords are the BCrypt encryption result of the plaintext "123456": $2a$10$jndgC.sZFv0volqxQeXdk.NGN.K7Smrko/5UtP33pPVUcQdP0604a (Based on configuration file jwt.secret:QingDai)

```sql
# Permissions
INSERT INTO `qingdai-photo`.sys_permission (id, code, name, description, created_time, updated_time) VALUES ('1', '1', 'Photo Management', 'Photo Management', NOW(), NOW());
INSERT INTO `qingdai-photo`.sys_permission (id, code, name, description, created_time, updated_time) VALUES ('2', '2', 'Original Image Viewing', 'Original Image Viewing', NOW(), NOW());

# Roles
INSERT INTO `qingdai-photo`.sys_role (id, name, description, created_time, updated_time) VALUES ('1', 'ADMIN', 'Administrator', NOW(), NOW());
INSERT INTO `qingdai-photo`.sys_role (id, name, description, created_time, updated_time) VALUES ('2', 'VIEWER', 'Visitor', NOW(), NOW());

# Role-Permission
INSERT INTO `qingdai-photo`.sys_role_permission (role_id, permission_id) VALUES ('1', '1');
INSERT INTO `qingdai-photo`.sys_role_permission (role_id, permission_id) VALUES ('2', '2');

# Users
INSERT INTO `qingdai-photo`.sys_user (id, username, password, status, created_time, updated_time) VALUES ('1', 'root', '$2a$10$jndgC.sZFv0volqxQeXdk.NGN.K7Smrko/5UtP33pPVUcQdP0604a', 1, NOW(), NOW());
INSERT INTO `qingdai-photo`.sys_user (id, username, password, status, created_time, updated_time) VALUES ('2', 'viewer', '$2a$10$jndgC.sZFv0volqxQeXdk.NGN.K7Smrko/5UtP33pPVUcQdP0604a', 1, NOW(), NOW());

# User-Role
INSERT INTO `qingdai-photo`.sys_user_role (user_id, role_id) VALUES ('1', '1');
INSERT INTO `qingdai-photo`.sys_user_role (user_id, role_id) VALUES ('2', '2');
``` 