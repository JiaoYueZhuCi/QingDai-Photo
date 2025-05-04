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
  - RocketMQ Message Queue
  - JWT Authentication
  - Swagger/OpenAPI Documentation
  - kaptcha

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
            ├── mq/                       # RocketMQ related
            │   ├── consumer/             # Message consumers
            │   └── producer/             # Message producers
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
- **Backend**: Java 17+, Maven 3.6+, MySQL 8+, Redis 6+, RocketMQ 4.8+

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

rocketmq:
  name-server: ?
  producer:
    group: qingdai-photo-group
    send-message-timeout: 3000
    retry-times-when-send-failed: 2
    access-key: ?
    secret-key: ?
  consumer:
    group: qingdai-photo-consumer
    access-key: ?
    secret-key: ?
  topic:
    photo: photo-topic
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
Please check the [database/init.sql](database/init.sql) file, which contains the complete database structure and initialization data.

#### User Data
1. Roles and Permissions
   - ADMIN (id=1) has permission for "Photo Management"
   - VIEWER (id=2) has permission for "Original Photo Viewing"
   - Associated through sys_role_permission

2. Users and Roles
   - root user (id=1) has both ADMIN and VIEWER roles
   - viewer user (id=2) has only VIEWER role
   - Associated through sys_user_role

3. Password Notes:
   - All user passwords are the BCrypt encryption result of plain text "123456": $2a$10$jndgC.sZFv0volqxQeXdk.NGN.K7Smrko/5UtP33pPVUcQdP0604a (based on configuration file jwt.secret:QingDai)

## RocketMQ

### Feature Description
RocketMQ is primarily used in this project for handling asynchronous image processing workflows, including:
- Compression of uploaded images
- Metadata extraction and processing
- Thumbnail generation
