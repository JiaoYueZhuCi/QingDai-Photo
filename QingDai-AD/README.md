# 青黛日出 Android 应用

这是一个简单的Android应用，用于展示来自SunsetBot API的日出日落数据。

## 功能

- 显示日出/日落事件的时间
- 显示数据来源信息
- 显示气溶胶光学厚度(AOD)信息
- 显示鲜艳度信息
- 支持手动刷新数据

## 技术栈

- Java
- Retrofit2 用于网络请求
- OkHttp3 用于HTTP客户端
- Gson 用于JSON解析

## 如何构建

1. 使用Android Studio打开项目
2. 点击"Sync Project with Gradle Files"
3. 点击"Run"按钮，选择设备或模拟器运行应用

## 接口说明

应用调用以下接口获取数据：
```
https://www.sunsetbot.top/?query_id=5754827&intend=select_city&query_city=天津市-天津&event_date=None&event=rise_1&times=None
```

## 截图

应用展示了以下信息：
- 北京时间：事件发生的时间
- 数据来源：数据的来源信息
- 气溶胶：大气中的气溶胶光学厚度值及评级
- 鲜艳度：日出/日落景观的鲜艳程度评级 