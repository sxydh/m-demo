# NoFlagSecure 模块说明

清除窗口 `FLAG_SECURE`，让 **adb 镜像 / 截图 / `uiautomator`** 能看到设置了该标志的
窗口（银行类 App 常用它禁止捕获）。

## 1. 模块信息

| 项 | 值 |
|---|---|
| 包名 | `com.rootdemo.noflagsecure` |
| 类型 | Xposed 模块（`IXposedHookLoadPackage` + `assets/xposed_init`） |
| 作用域 | `system/0`（system_server） |
| 依赖 | KernelSU-Next + Zygisk Next(`zygisksu`) + Vector(`zygisk_vector`, LSPosed fork) |

**只碰 system_server，不注入任何目标 App。**

## 2. 原理

`FLAG_SECURE = 0x00002000`（`android.view.WindowManager.LayoutParams`）。
在 system_server 的 `WindowManagerService` 里，窗口**添加/重布局**时把该位清掉：

| 类 | 方法 |
|---|---|
| `com.android.server.wm.WindowManagerService` | `addWindow`、`relayoutWindow` |

只影响“捕获”，不改变 App 自身渲染。system_server 侧改动对全局生效。

## 3. 构建 / 安装 / 作用域

```powershell
# 构建（本机有代理 http://127.0.0.1:7890，见 gradle.properties）
cd NoFlagSecure; .\gradlew.bat assembleDebug --console=plain

# 安装
adb install -r NoFlagSecure\app\build\outputs\apk\debug\app-debug.apk
```

作用域（Vector CLI，`system/0` 即 system_server）：

```powershell
su -c "/data/adb/modules/zygisk_vector/cli scope set com.rootdemo.noflagsecure system/0"
su -c "/data/adb/modules/zygisk_vector/cli modules ls"
```

生效需要 `adb reboot`（system_server 侧改动无法热重载，**禁止 `ksud soft-reboot`**）。

## 4. 验证

- system_server 日志（Vector）：
  ```
  [NoFlagSecure] loading in android
  [NoFlagSecure] hooks installed (2 on WindowManagerService)
  [NoFlagSecure] stripped FLAG_SECURE (relayoutWindow)
  ```
- 检查某个 App 的窗口 flags 不再含 `SECURE`：
  ```powershell
  adb shell "dumpsys window windows | grep -A2 '<pkg>/<Activity>}' | grep fl="
  ```

## 5. 日志

直接 `XposedBridge.log`，前缀 `[NoFlagSecure]`。默认只打印 加载 / 安装 hook / **首次**
清除（各一次），不刷屏；逐次清除日志由源码里的 `DEBUG` 常量控制（排查时置 `true`）。

## 6. 注意 / 边界

- 作用域是 **system_server**，不是目标 App；不会给目标 App 进程增加 Xposed 痕迹。
- 与“应用黑名单”类检测无关（那是“模块 App 被列举”的问题，需 HMA-OSS）。
- 仅去掉捕获限制，不改变 App 的逻辑与安全性判断。

## 7. 关键文件

- `app/src/main/java/com/rootdemo/noflagsecure/NoFlagSecure.java` — hooks 主体
- `app/src/main/assets/xposed_init` — 入口类
- `app/src/main/AndroidManifest.xml` — `xposedmodule` 元数据

## 8. adb / su 规范

PowerShell 给 `adb`/`su` 传参注意引号：用 here-string 管道（见 `RootDemo/AGENTS.md`）。
