# 青黛天气 Android 应用

这是一个Android应用，用于展示青黛天气数据，包括北京时间、鲜艳度和气溶胶信息。

## 功能

- 从sunsetbot.top API获取天气数据
- 展示城市信息
- 展示北京时间信息
- 展示鲜艳度数据
- 展示气溶胶数据
- 显示相关天气图像

## 技术栈

- Android Java
- Retrofit2用于网络请求
- Glide用于图像加载
- Material Design组件

## 构建和运行

1. 使用Android Studio打开项目
2. 构建项目：`Build > Make Project`
3. 运行项目：`Run > Run 'app'`
4. 在模拟器或实际设备上测试应用

## 依赖项

- Android SDK 33
- AndroidX库
- Retrofit2和Gson转换器
- Glide图像加载库
- Jsoup HTML解析库

## 项目结构

- `app/src/main/java/com/qingdai/photo/` - Java源代码
  - `MainActivity.java` - 主界面活动
  - `ApiService.java` - Retrofit API接口
  - `WeatherData.java` - 数据模型
- `app/src/main/res/` - 资源文件
  - `layout/activity_main.xml` - 主界面布局
  - `values/` - 字符串、颜色和样式
- `app/` - Gradle构建配置 