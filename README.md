# Weex Analyzer

---

[ ![Download](https://api.bintray.com/packages/chuyi/maven/weex_analyzer/images/download.svg) ](https://bintray.com/chuyi/maven/weex_analyzer/_latestVersion)[![GitHub release](https://img.shields.io/badge/release-v0.0.3.0-brightgreen.svg)](https://github.com/weexteam/weex-analyzer-android/releases/latest) [![GitHub release](https://img.shields.io/badge/license-%20Apache--2.0-yellowgreen.svg)](https://github.com/weexteam/weex-analyzer-android/blob/master/LICENSE)

`Weex Analyzer`是一款运行在手机客户端上辅助开发者进行weex开发的小工具。
接入此工具后，开发者可以在debug包中通过摇一摇打开功能选项。目前实现的功能有:

1. log日志查看悬浮窗，支持日志分级
2. weex性能分析(查看当前weex页面性能指标/以图表方式展示历史性能指标变化趋势)
3. weex storage查看、删除
4. 实时帧率、丢帧数、内存占用悬浮框
5. cpu/fps 折线图
6. 页面3d视图
7. js报错时弹框提醒


### 接入

#### 添加依赖

```
debugCompile 'com.taobao.android:weex_analyzer:0.0.1.2'
```

#### 代码集成

目前集成过程有点繁琐，需要复写`WeexActivity`所有生命周期函数. 具体请参考`commons`module下`WXAnalyzerDelegate`、`AbstractWeexActivity`.





