# 📱 Android WebView Template 应用

<div align="center">
  <img src="app/src/main/res/drawable/round_icon.png" width="120" height="120" alt="App Icon"/>
  
  [![Android](https://img.shields.io/badge/Android-8.0%2B-green?logo=android)](https://developer.android.com)
  [![Kotlin](https://img.shields.io/badge/Kotlin-2.2.0-blue?logo=kotlin)](https://kotlinlang.org)
  [![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE.md)
</div>

## 📋 简介

一个简单实用的Android WebView 模板应用，可以把任何网站变成手机APP。支持自动登录、全屏浏览等功能。

## ✨ 主要功能

- 🔐 **自动登录** - 支持基于HTTP Basic Auth 需要用户名密码的网站，登录一次自动记住，不需要重复输入
- 🌐 **完整网页体验** - 支持JavaScript、Cookie、本地存储
- 🎨 **沉浸式界面** - 支持全屏浏览，支持刘海屏

## 🚀 使用方法

### 1. 下载安装
```bash
git clone https://github.com/Shixiaoyanger/WebViewTemplate.git
cd WebViewTemplate
```

### 2. 配置网址
在 `app/build.gradle` 文件中修改要访问的网站：
```gradle
buildConfigField "String", "WEBVIEW_URL", "\"https://your-website.com\""
```

### 3. 自定义APP名称
在 `app/src/main/res/values/strings.xml` 中修改：
```xml
<string name="app_name">你的APP名称</string>
```

### 4. 编译安装
```bash
./gradlew assembleDebug
./gradlew installDebug
```

## 📱 如何使用

1. **打开APP** - 自动加载设置的网站
2. **登录网站** - 如果需要登录，会弹出登录框，输入用户名密码
3. **正常浏览** - 像使用浏览器一样浏览网站
4. **返回导航** - 按返回键可以回到上一页

## 🔧 技术说明

- **开发语言**: Kotlin
- **最低系统**: Android 8.0 (API 26)
- **核心功能**: WebView + HTTP认证 + Cookie管理
- **界面特色**: 沉浸式状态栏 + 自适应布局

## 📁 项目结构

```
WebViewTemplate/
├── app/src/main/java/com/yanger/suwayomi/
│   └── WebViewActivity.kt          # 主要功能代码
├── app/src/main/res/
│   ├── layout/
│   │   ├── activity_web_view.xml   # 主界面
│   │   └── dialog_auth.xml         # 登录对话框
│   └── values/strings.xml          # 文本配置
└── app/build.gradle                # 网址配置
```

## 🐛 常见问题

**Q: 如何换成其他网站？**  
A: 修改 `app/build.gradle` 中的 `WEBVIEW_URL` 值

**Q: 登录信息保存在哪里？**  
A: 安全保存在手机本地，卸载APP后会清除

**Q: 支持哪些网站？**  
A: 支持所有标准网站，包括需要登录的网站

**Q: 如何更换APP图标？**  
A: 替换 `app/src/main/res/drawable/` 目录下的图标文件

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE.md](LICENSE.md) 文件了解详情。

---

<div align="center">
  <p>Made with ❤️ by <a href="https://github.com/Shixiaoyanger">Shixiaoyanger</a></p>
  <p>⭐ 如果这个项目对您有帮助，请给个星星支持一下！</p>
</div>