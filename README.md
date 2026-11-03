# 本地大字看板 v1.2

一个纯本地的 Android 大字看板应用，可选择滚动或静止。Kotlin + Jetpack Compose 编辑界面，Android 文本排版与逐帧绘制看板。

## 功能

- 输入中文、英文、emoji 或混合文字，最多 2000 个 UTF-16 字符；滚动模式将换行和制表符显示为空格，静止模式保留换行。
- 一键进入横屏沉浸式展示。**滚动**：从右到左连续循环；短文字重复填充，长文字按实际字宽滚动。
- **静止**：文字在屏幕中央显示，自动换行；优先使用设定字号，放不下时自动缩小，避免裁切。极长文字会很小，建议缩短内容或切回滚动。静止模式隐藏速度设置且不启动逐帧动画。
- 调整字号（48–280 dp）、速度（30–300 dp/秒）、文字颜色和背景颜色。
- 6 款真正的中文字体：经典黑体（原系统粗体）、马善政毛笔、站酷小薇、站酷快乐、站酷庆科黄油、龙藏草书。展开字体列表查看真实字形预览，选中后自动收起；滚动和静止模式均使用所选字体。
- 自动记住输入、展示方式、字体与设置；数据只写入应用私有 SharedPreferences，卸载后删除。
- 展示时保持屏幕常亮；返回编辑、进入后台时恢复正常休眠并停止动画。
- 展示后 4 秒隐藏退出按钮；轻触屏幕重新显示，或使用系统返回手势/返回键退出。
- 没有账号、广告、服务端、统计 SDK，不申请联网（INTERNET）、存储等设备权限，无运行时授权弹窗；关闭系统备份。

## 直接安装随附优化 APK

把 `本地大字看板-v1.2.apk` 传到 Android 手机后打开，按系统提示为该安装来源临时允许安装应用即可；不需要关闭 Play Protect 或其他系统安全保护。首次启动无需注册或授权。该 APK 已用 R8 和资源裁剪缩小应用代码，5 个字体保持完整不变；仍使用原开发测试签名，不是商店正式发布签名。

本次 v1.2 使用与 v1.0 / v1.1 相同的开发签名、相同包名和更高版本号，可覆盖安装并保留文字、字号、颜色和速度；未选过字体的旧版升级保留原经典黑体，展示模式和其他设置不变。安装更新前无需卸载。

如果你随后在自己的电脑重新构建，开发签名可能不同，无法直接覆盖安装；可卸载旧版后安装（卸载会清除旧文字与设置）。

## 用 Android Studio 打开

1. 解压源码 ZIP，打开其中的 `android-scrolling-sign` 文件夹（包含 `settings.gradle.kts`）。
2. 使用支持 AGP 8.8 的 Android Studio，例如 Ladybug Feature Drop 2024.2.2 或更新版本。
3. Gradle JDK 使用 **JDK 17 或 21**。通过 SDK Manager 安装 **Android SDK Platform 35**、**Build-Tools 35.0.0**、Platform-Tools。
4. 等待 Gradle Sync。首次构建需要联网下载开发依赖，**安装后的 App 不需要联网**。
5. 连接 Android 6.0 / API 23 或更新版本的手机并开启 USB 调试，点击 Run ▶。

项目附带标准 Gradle Wrapper，不需要全局安装 Gradle。版本锁定为 Gradle 8.10.2、AGP 8.8.2、Kotlin 2.0.21、Compose BOM 2024.12.01。

## 命令行构建和测试

设置 `ANDROID_HOME`，或在项目根目录创建仅用于本机的 `local.properties`：

```properties
sdk.dir=/你的/Android/Sdk/路径
```

macOS / Linux：

```bash
chmod +x gradlew
./gradlew assembleRelease testReleaseUnitTest lintRelease
adb install -r app/build/outputs/apk/release/app-release.apk
```

Windows：

```bat
gradlew.bat assembleRelease testReleaseUnitTest lintRelease
adb install -r app\build\outputs\apk\release\app-release.apk
```

优化 APK 位置：`app/build/outputs/apk/release/app-release.apk`。release 构建配置启用 R8/资源裁剪，并使用 debug 开发签名，便于个人安装、覆盖更新。源码不含任何私钥；自己电脑首次构建会生成本机开发签名。开发调试也可使用 `./gradlew assembleDebug`，该包未瘦身，体积更大。

R8 映射文件在 `app/build/outputs/mapping/release/mapping.txt`，请在自行发布或诊断该次构建崩溃时保留对应文件。

## 设计说明与边界

- 编辑界面跟随设备方向；独立展示 Activity 请求 `sensorLandscape`，允许左右两个横屏方向。部分平板、多窗口或厂商系统可能不遵守方向锁定；仍会按实际窗口尺寸展示。
- 字号为逻辑像素 dp，不随系统字体大小变化；在很矮的窗口中自动缩小，防止上下裁切。
- `StaticLayout` 按系统字体排版，支持中文、组合 emoji 与混合脚本；emoji 外观取决于手机系统版本，旧系统可能缺少新 emoji。
- `Choreographer` 用帧时间驱动，按测量出的文字宽度加间距循环，不为长文字创建大位图；后台暂停，恢复不追赶已错过的时间。
- 不主动读取剪贴板、文件、通讯录或网络。不提供数据导出或云同步。编辑框仍支持系统提供的复制、粘贴操作。
- 禁用 AndroidX 的自动下载 emoji 字体初始化，仅使用系统现有字体。AndroidX 合并的应用内签名权限仅用于保护动态接收器，不是联网或设备数据授权。

## 文件结构

- `MainActivity.kt`：文字、字号、速度、颜色与预览。
- `DisplayActivity.kt`：横屏、沉浸模式、常亮、返回编辑与生命周期。
- `MarqueeView.kt`：文本测量、静止居中自动适配与无缝循环动画。
- `SettingsStore.kt`：本机偏好存储。
- `SignFonts.kt`、`res/font/`：稳定字体 ID、离线字体资源及缓存。
- `assets/font_licenses/`：原始 OFL 授权、作者、来源链接、固定提交与文件校验值；应用内“字体许可与来源”可离线阅读。
- `SignSettings.kt`：默认值、输入规范化与纯计算函数。
- `app/src/test/`：输入、滚动数学与静止适配单元测试。
- `VALIDATION.md`：本次实际验证结果及尚待真机检查项。

## 真机验收建议

输入 `你好，世界！👋 Hello 123` → 调整颜色/速度 → 开始 → 横屏滚动；轻触后返回。
再检查空白输入、接近 2000 字长文、连续进入退出、左右翻转手机、Home 后恢复、锁屏后恢复、重启应用保留设置；观察只有展示页常亮。飞行模式下重复一遍，确认完全离线可用。


### v1.1 新增验收

选择“静止”→ 输入“欢迎光临”→ 开始：文字应居中且不移动。返回后改成多行中文/emoji 或长文，确认换行及自动缩小完整显示。切回滚动确认原速度保留。退出重开确认模式被记住。若已安装 v1.0，直接覆盖安装 v1.1 后确认原输入/设置仍在。


## v1.2 字体与许可

新增的 5 个完整、未修改 TTF 字体来自官方 `google/fonts` 仓库固定提交 `6c1dbe27dcf7d817d4b62cec033ba77fa8e2a5ec`，合计 27,177,904 字节，均附带 SIL Open Font License 1.1。每款均覆盖 GB2312 的全部 6,763 个汉字；没有做只保留示例短语的子集裁剪。系统黑体不额外打包。

生僻字、繁体扩展字或 emoji 如不在某款字体里，会使用 Android 系统回退字体；不同手机的 emoji 外观和罕见字符支持可能不同。“6 种字体”不表示每个 Unicode 字符都有 6 套字形。草书/艺术字体优先风格，可读性不如黑体。

验收：展开全部字体，依次选中并检查预览；分别在滚动、静止模式展示中文/英文/emoji，确认风格变化、滚动边界和居中适配正常；重启后检查字体选择保留。查看“字体许可与来源”，确认可离线滚动阅读及关闭。未执行真机渲染验收。
