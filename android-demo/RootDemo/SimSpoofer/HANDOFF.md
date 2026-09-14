# SimSpoofer 交接文档（HANDOFF）

## 目标

在 **不向目标 App 注入 Xposed** 的前提下，伪造 SIM 环境（ICCID/IMSI/号码/运营商/订阅），
让检测类 App（春秋）与银行 App（BOI `com.boi.ua.android`）不被触发反注入自毁，并尽量过检。

## 运行环境

- 设备：Pixel 7 Pro (`cheetah`)，Android 16；KernelSU-Next + SUSFS + Zygisk Next + NoMount。
- 框架：**Vector v2.2 (3080)**（LSPosed fork），管理器包 `org.matrix.vector.manager`。
- 模块包：`com.rootdemo.simspoofer`（本仓库 `SimSpoofer/` 构建）。
- 代理：`http://127.0.0.1:7890`，已写入 `SimSpoofer/gradle.properties`。

## 核心架构（关键！不要改回“注入目标 App”）

模块作用域只勾：
- `com.android.phone/0` —— SIM 数据源
- `system_server/0` —— 网络类型、USB 调试等系统设置

**绝不勾目标 App**。目标 App 仅通过 binder 拿到已被伪造的数据，进程内无 Xposed 痕迹。

### phone 进程 hook

| 类 | 作用 |
|---|---|
| `com.android.internal.telephony.PhoneSubInfoController` | ICCID/IMSI/line1/msisdn/运营商串 |
| `com.android.phone.PhoneInterfaceManager` | simState/hasIccCard/networkType |
| `com.android.internal.telephony.subscription.SubscriptionManagerService` | 订阅列表/计数/default subId |
| `com.android.internal.telephony.IccSmsInterfaceManager` | 仅诊断：记录是否真的发短信 |

### system_server hook

| 目标 | 作用 | 捕获方式 |
|---|---|---|
| `android.net.connectivity.com.android.server.ConnectivityService` | getNetworkCapabilities→CELLULAR；NetworkInfo→MOBILE/CONNECTED；isActiveNetworkMetered→true | hook `ServiceManager.addService` 捕获 |
| `com.android.providers.settings.SettingsProvider` | 对普通 App 把 `adb_enabled`/`development_settings_enabled`/`adb_wifi_enabled` 的 GET_global/GET_secure 返回 "0" | hook `ContentProvider.attachInfo` 捕获 |

守卫：`Binder.getCallingUid()` 为 0/1000/1001（root/系统/phone）一律放行，只对普通 App 伪造。

## 运行时配置（UI 可改，无需重启）

- `SettingsActivity`（App 名 “SimSpoofer”）：改字段 → 保存 → 写入模块 App 的 SharedPreferences `sim_profile`。
- `SimConfigProvider`（authority `com.rootdemo.simspoofer.config`）暴露配置。
- `ProfileStore.get(cl)` 在 phone 进程每次调用读取（3 秒缓存），回退到 `SimProfile` 默认值。
- 可改字段：line1 / iccid / imsi / sim_operator / network_operator / operator_name /
  network_operator_name / country_iso / mcc / mnc / subscription_id / sim_state / network_type / has_icc。
- 旧值需目标 App 强停重开；已在运行的 App 有缓存。

## 已实测通过

- 普通 App（自检页）读到伪造的 ICCID/IMSI/号码/订阅列表。
- 连通性伪造让 WiFi 被识别为蜂窝（UA 过了“必须蜂窝”门禁）。
- UI 改号码 → 保存 → phone 进程 ~3 秒生效（已验证 `+918910292686`）。
- `SubscriptionInfo` 构造：API 36 构造函数参数序为
  `(id, iccid, simSlotIndex, displayName, carrierName, nameSource, iconTint, number, icon,
  iconBitmap, mccString, mncString, countryIso, ...)` —— mcc/mnc 是**第 10/11 位的 String**，
  countryIso 第 12 位。字段名 `mMcc/mMnc` 是 `private final`，反射 set 会被拒，必须走构造函数。
- system_server 注入**未被 UA 检测**（UA 只看自身进程）。

## 当前进展 / 下一步

UA (`com.boi.ua.android`) 流程：
语言 → 条款 → 定位（需点系统“改精确”对话框）→ **已到过“开发者选项/USB 调试”拦截** →
已用 SettingsProvider 伪造 `adb_enabled=0`（**待解锁屏幕后重测**）。

已知硬墙（大概率无解，需真 SIM）：
- UA 的 SIM 验证/注册需要向该号码下发 OTP（SMS Retriever，收短信）或运营商校验；
  假卡收不到，会 `SIM Verification Failed`（该提示是通用兜底文案，实测**并未真的发短信**）。

## 常用命令

```powershell
# 构建（热）
cd SimSpoofer; .\gradlew.bat assembleDebug --console=plain
# 安装
adb install -r SimSpoofer\app\build\outputs\apk\debug\app-debug.apk

# 作用域
/data/adb/modules/zygisk_vector/cli scope ls com.rootdemo.simspoofer
/data/adb/modules/zygisk_vector/cli scope add com.rootdemo.simspoofer com.android.phone/0
/data/adb/modules/zygisk_vector/cli scope add com.rootdemo.simspoofer system_server/0

# 重载：只改 phone 进程代码 → 杀 phone 进程即可（无需重启）
su -c "kill -9 $(pidof com.android.phone)"
# 改了 system_server 代码 → adb reboot（禁止软重启！）
```

## 坑

- **禁止 `ksud soft-reboot`**：实测卡开机画面（Google + 进度条），只能强重启恢复。
- PowerShell 给 `adb`/`su` 传参注意引号：用 here-string 管道（见 AGENTS.md）。
- UA 用了 **FLAG_SECURE**：投屏/截图全黑，只能用 `uiautomator dump` 读界面树、用 `input tap` 驱动。
- `input text` 不弹软键盘；`input keyevent 111(ESC)` 有时等价返回会关页面，用 `4(BACK)` 收键盘。
- SettingsActivity 曾因 `getString` 读 int 字段崩溃 → 已用 `readValue` 按类型读取；按钮放顶部
  且用 `Theme.Material.Light.NoActionBar` + 顶部状态栏留白。
- 模块调试日志目前很吵（fire/skip/hid），上线前应关掉 `SimProfile.DEBUG` 或降级日志。

## 关键文件

- `SimSpoofer/app/src/main/java/com/rootdemo/simspoofer/SimSpoofer.java` —— hooks 主体
- `.../ProfileStore.java` —— 运行时配置读取（provider + 缓存）
- `.../SimConfigProvider.java` —— 配置 provider
- `.../SettingsActivity.java` —— UI
- `.../CheckActivity.java` —— 自检页
- `.../SimProfile.java` —— 默认值
- `.../AndroidManifest.xml` —— 组件注册
