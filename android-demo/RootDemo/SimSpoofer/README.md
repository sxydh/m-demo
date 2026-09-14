# SimSpoofer 模块说明

不注入目标 App 的前提下，**伪装 SIM/网络环境**的 Xposed 模块，用于调试/研究类银行
App 的启动流程。

> 结论先说：本模块能过“**读取环境**”类门禁（SIM 字段、蜂窝网络、USB 调试开关），
> 但**过不了“真实 SIM 发短信/收 OTP”类门禁**——那取决于网络侧身份（真实 A 号码 /
> IMSI 鉴权），不在伪造范围内。见第 6 节“能力边界”。

---

## 1. 模块信息

| 项 | 值 |
|---|---|
| 包名 | `com.rootdemo.simspoofer` |
| 类型 | Xposed 模块（`IXposedHookLoadPackage` + `assets/xposed_init`） |
| 作用域 | `com.android.phone/0`、`system/0`（system_server） |
| 依赖 | KernelSU-Next + Zygisk Next(`zygisksu`) + Vector(`zygisk_vector`, LSPosed fork) |

模块自身不依赖 KernelSU/Zygisk API；由 Vector 按作用域注入。

---

## 2. 架构（关键：不要改回“注入目标 App”）

只改“数据源”，**绝不勾目标 App**：

```
目标 App ──binder──> phone 进程(com.android.phone) ──> 本模块改返回值
目标 App ──binder──> system_server ─────────────────> 本模块改连通性/设置
```

目标 App 进程内没有 Xposed 痕迹，因此不会触发 App 的注入自毁。
（反例：AU 0101 的 Protectt SDK 会做 app blocklisting，点名“模块 App 本身”，
属应用列举检测，需用 HMA-OSS 隐藏模块 App，见第 7 节。）

---

## 3. 构建 / 安装 / 作用域

```powershell
# 构建（本机有代理 http://127.0.0.1:7890，见 gradle.properties）
cd SimSpoofer; .\gradlew.bat assembleDebug --console=plain

# 安装
adb install -r SimSpoofer\app\build\outputs\apk\debug\app-debug.apk
```

作用域（Vector CLI，`system/0` 即 system_server）：

```powershell
su -c "/data/adb/modules/zygisk_vector/cli scope set com.rootdemo.simspoofer com.android.phone/0 system/0"
su -c "/data/adb/modules/zygisk_vector/cli modules ls"
```

热重载：

- **只改 phone 进程代码** → `su -c "kill -9 $(pidof com.android.phone)"`，无需重启。
- **改了 system_server 代码（连通性/设置）** → 只能 `adb reboot`。
  **禁止 `ksud soft-reboot`**（实测卡开机）。

---

## 4. 运行时配置

设置页 `SettingsActivity`（App 名 “SimSpoofer”）改字段 → 保存 → 写入模块 App 的
`SharedPreferences("sim_profile")` → phone 进程 ≤3 秒读到（`ProfileStore`，3s 缓存）。

可改字段：`line1/iccid/imsi/sim_operator/network_operator/operator_name/
network_operator_name/country_iso/mcc/mnc/subscription_id/sim_state/network_type/
has_icc`，以及 **`debug`（调试日志开关）**。

---

## 5. Hook 覆盖（能伪造什么）

**phone 进程（`com.android.phone`）**

| 类 | 方法（示例） | 伪造 |
|---|---|---|
| `…telephony.PhoneSubInfoController` | `getIccSerialNumber*`, `getSimSerialNumber*`, `getSubscriberId*`, `getLine1Number*`, `getMsisdn*`, `getSimOperator*`, `getSimOperatorName*`, `getNetworkOperator*`, `getNetworkOperatorName*` | ICCID/IMSI/号码/运营商 |
| `…telephony.PhoneInterfaceManager` | `getSimState*`, `hasIccCard`, `getNetworkType*`, `getDataNetworkType*`, `getVoiceNetworkType*` | SIM 状态/网络制式 |
| `…subscription.SubscriptionManagerService` | `getActiveSubscriptionInfoList`, `getAccessibleSubscriptionInfoList`, `getActiveSubscriptionInfo`, `getActiveSubInfoCount*`, `getDefault*SubId`, `getSlotIndex*` | 订阅列表/计数/默认 subId |
| `…telephony.SmsController`（Android 16 的 ISms 实现） | `sendText*`/`sendMultipartText*`/`sendData*` | **仅诊断打印**，不改变行为 |

**system_server**

| 类 | 方法 | 伪造 |
|---|---|---|
| `…server.connectivity.ConnectivityService` | `getNetworkCapabilities`→CELLULAR；`isActiveNetworkMetered`→true；`getActiveNetworkInfo*`/`getNetworkInfo*`→MOBILE/CONNECTED | WiFi/以太网/VPN 被当作蜂窝 |
| `…providers.settings.SettingsProvider` | `call(GET_global/GET_secure, adb_enabled/development_settings_enabled/adb_wifi_enabled)` | 对普通 App 返回 `"0"` |

**守卫**：`Binder.getCallingUid()` 为 0/1000/1001（root/system/phone）一律放行，只对普通 App 伪造。

**SubscriptionInfo 构造**（API 36）参数序：
`(id, iccid, simSlotIndex, displayName, carrierName, nameSource, iconTint, number, icon,
iconBitmap, mccString, mncString, countryIso, …)`，mcc/mnc 为第 10/11 位 String，
countryIso 第 12 位；字段 `mMcc/mMnc` 是 private final，必须走构造函数。

---

## 6. 能力边界（重要）

**能**：让 App 通过 telephony/connectivity API 读到的值变成伪造值；隐藏 USB 调试/开发者选项。

**不能**（取决于网络侧，非本模块职责）：

- **发短信 / 收短信**：`SmsController.sendTextForSubscriber` 在发送前会走
  `TelephonyPermissions.checkSubscriptionAssociatedWithUser(subId)` 查**真实订阅库**，
  伪造的 `subId` 查不到 → 直接拒发（`Subscription[Subscription ID:1] has no records on device`）。
- **真实身份**：短信的发送方号码（A 号码）由网络按 IMSI 的 HLR 记录填入，端侧改不了；
  假卡无法驻网/鉴权。
- **网络注册状态**：`ServiceState`、小区信息等未伪造。

因此：**能过“必须蜂窝/必须非调试环境/读 SIM 字段”的门禁，过不了“向指定号码发短信、
通过运营商/服务端校验真身”的门禁**（如 BOI Mobile、AU 0101 的 SIM 绑定）。需要真实可用的 SIM。

---

## 7. 反检测

- **模块 App 本身可被列举检测**：AU 0101 用 Protectt.ai（`ai.protectt.app.security`），
  通过 `getPackageArchiveInfo` / `sourceDir` 扫描已安装 APK 并命中 `de.robv.android.xposed.XposedBridge`
  等特征，弹出 “Hooking App Detected” 并点名 `SimSpoofer`。
  缓解：用 **HMA-OSS**（已装 `hma_oss_zygisk`）把模块 App 对目标 App 隐藏
  （模板可用内置 “LSPosed/Xposed modules” 预设）。
- **system_server 注入本身**未被 UA/BOI 检测到（它们不做系统进程注入检测）。

---

## 8. 日志

`SimLog` 分级：`OFF/ERROR/INFO/DEBUG`，**默认 INFO**（无逐调用刷屏）。
设置页勾选“调试日志”→ phone 进程下次读取配置（≤3s）切到 DEBUG，取消即回 INFO。
热路径（逐调用 `fire …`）在 DEBUG；hook 安装/采集在 INFO；失败在 ERROR。

---

## 9. 已知缺口 / 可选后续

- 适配层：不同 Android 版本类名/构造器存在差异，当前按 Android 14~16 实测适配；
  可在 `buildSubscriptionInfo`/类查找处做更强的按类型匹配与降级。
- 未覆盖：`ServiceState`、`getCellLocation`、`DataConnectionState` 等（如需更完整的“蜂窝”假象）。
- 自检：`CheckActivity` 可扩展为 hook 覆盖率自检。

---

## 10. 关键文件

- `app/src/main/java/com/rootdemo/simspoofer/SimSpoofer.java` — hooks 主体
- `.../SimLog.java` — 日志分级
- `.../ProfileStore.java` — 运行时配置读取（provider + 3s 缓存）
- `.../SimConfigProvider.java` — 配置 provider（authority `com.rootdemo.simspoofer.config`）
- `.../SettingsActivity.java` — 设置页
- `.../CheckActivity.java` — 自检页
- `.../SimProfile.java` — 默认值

## 11. adb / su 规范

PowerShell 给 `adb`/`su` 传参注意引号：用 here-string 管道（见 `RootDemo/AGENTS.md`）。
