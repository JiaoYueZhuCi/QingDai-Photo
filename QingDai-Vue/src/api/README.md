# API 接口文档

本文档描述了项目中的所有API接口及其使用方法。

## API模块重构说明

API模块已进行重构，以提高代码质量和可维护性：

1. **集中管理**：所有API请求从组件中提取出来，集中在`api`目录下管理
2. **模块化**：按功能分为`photo.ts`、`timeline.ts`、`user.ts`等模块
3. **拦截器**：统一的请求/响应拦截器处理，包括：
   - Token自动添加
   - 错误统一处理
   - 响应数据预处理
4. **类型安全**：使用TypeScript接口定义请求参数和响应数据结构

## 使用方法

### 导入API模块

```typescript
// 导入单个API
import { getPhotosByPage } from '@/api';

// 或者导入整个模块
import * as photoApi from '@/api/photo';
```

### 调用API方法

```typescript
// 示例：获取照片列表
const getPhotos = async () => {
  try {
    const response = await getPhotosByPage({
      page: 1,
      pageSize: 10
    });
    
    // 使用响应数据
    console.log(response.records);
  } catch (error) {
    console.error('获取照片失败:', error);
  }
};
```

## 照片模块 (photo.ts)

### 获取照片分页数据

```typescript
getPhotosByPage(params: PhotoQueryParams)
```

参数:
- `page`: 页码，默认1
- `pageSize`: 每页大小，默认10

### 更新照片星标状态

```typescript
updatePhotoStartStatus(data: PhotoStatusUpdateParams)
```

参数:
- `id`: 照片ID
- `start`: 星标状态 (1: 精选, 0: 普通, -1: 隐藏)

### 上传照片

```typescript
processPhotosFromFrontend(formData: FormData)
```

参数:
- `formData`: 包含文件的表单数据

## 时间轴模块 (timeline.ts)

### 获取所有时间轴数据

```typescript
getAllTimelines()
```

### 添加时间轴

```typescript
addTimeline(data: TimelineDTO)
```

参数:
- `time`: 时间
- `title`: 标题
- `text`: 内容

### 更新时间轴

```typescript
updateTimeline(data: TimelineItem)
```

参数:
- `id`: 时间轴ID
- `time`: 时间
- `title`: 标题
- `text`: 内容

### 删除时间轴

```typescript
deleteTimeline(id: string | number)
```

参数:
- `id`: 时间轴ID

## 用户模块 (user.ts)

### 用户登录

```typescript
login(data: LoginParams)
```

参数:
- `username`: 用户名
- `password`: 密码

## 响应处理

所有请求都已配置统一的响应拦截器，会自动处理常见的错误情况。如果需要自定义错误处理，可以在调用API时使用try/catch。 