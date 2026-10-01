# Nagad Agent (Uddokta) 逆向分析报告

- 包名：`com.konasl.nagad.agent`
- 版本：`1.0.120.01`（versionCode `1120`），flavor `prodAgent` / `agent` / `prod`
- minSdk 21 / targetSdk 35 / compileSdk 35
- APK 路径：`/data/app/~~_y651RhodMroaqmkYyaDFw==/com.konasl.nagad.agent-JeMKVddxMfG-F6x3XbRiaQ==/base.apk`
- 单 APK（无 split），55,890,451 字节
- 应用类：`com.konasl.dfs.DfsApplication`
- 启动 Activity：`com.konasl.dfs.ui.splash.SplashActivity`（`MAIN`/`LAUNCHER`，exported=true）

## 1. 反编译概况

- 引擎：jadx 1.5.6（`--show-bad-code`），11105 个 Java 文件，37 个报错类。
- **DEX：2 个，未加密、未加壳**，为标准 DEX v035。
  - `classes.dex` 8,933,300 B（10317 类 / 56389 字符串 / 64943 方法）
  - `classes2.dex` 5,993,032 B
- 混淆：R8 符号混淆（包名如 `CCBP` 内为 obfuscated 单字母类名），但业务包 `com.konasl.*`、`konashield.*` 保留原名，**Retrofit 注解与字符串未混淆**。
- 框架：原生 Java/Kotlin（非 Flutter/RN/Cordova），Dagger(android) 注入 + Retrofit/OkHttp + ButterKnife + ActiveAndroid(ORM) + Kotlin Coroutines。

主要第三方 SDK：Firebase（analytics/messaging/crashlytics/remoteconfig/ml-vision）、Play Services（auth/api-phone/maps/wallet/vision）、Facebook SDK、card.io、zxing、OpenCV、TensorFlow Lite（`model.tflite` 10MB，用于证件/人脸 OCR）、sslcommerz（`com.sslwireless.*`）、Visa、Mastercard。

## 2. 目录结构

反编译产物 / 工具输入（工作目录 `_Temp/`）：

```
_Temp/
  nagad_base.apk                 # 原始 APK
  nagad-decompiled/
    sources/                     # jadx 反编译 Java 源码
    resources/                   # 资源（含 AndroidManifest.xml、assets、lib、res）
  raw/
    classes.dex, classes2.dex    # 解包出的 dex（未加密）
    lib/<abi>/*.so               # 全部原生库
    assets/**                    # 原始 assets（OCR 模型、词表等）
```

结论文档（工作目录根）：

```
NagadAgent-report.md             # 本报告
NagadAgent-endpoints.md          # 207 个接口清单（Tier 1）
NagadAgent-endpoints.tsv         # 同上的 TSV
```

## 3. BuildConfig 中的加密配置（3DES-ECB 解出）

`com/konasl/dfs/BuildConfig.java` 里的网关等常量为密文，运行时用 `decryptConfigProperty(encryptedValue, key)` 解密：

```java
// CCBP.AbstractC0513Iu / CCBP.AbstractC0546Jq
DESedeKeySpec spec = new DESedeKeySpec(Base64.decode(key, 4));
Cipher c = Cipher.getInstance("DESede");      // 即 DESede/ECB/PKCS5Padding
c.init(DECRYPT_MODE, SecretKeyFactory.getInstance("DESede").generateSecret(spec));
new String(c.doFinal(Base64.decode(encryptedValue, 4)), UTF_8);
```

`LOCK_KEY = "o64l8sYbJu4+sW54Jq+TdFZ8Rjyv3rgu"`（Base64 24 字节 3DES key）。用该 key 解密得到：

| 常量 | 明文 |
|---|---|
| `API_GATEWAY_URL` | `https://app2.mynagad.com:20002` |
| `MQTT_PUSH_URL` | `tcp://app2.mynagad.com:1883` |
| `ASP_ID` | `100012345612345` |
| `APPLICATION_SIGNATURE` | `PkStBOGlMG/2bT8hf/OKgWEXoMg0CLRtaKgt3CXz4qI=` |
| `LOCK_KEY` | `o64l8sYbJu4+sW54Jq+TdFZ8Rjyv3rgu` |
| `APK_BUILD_TIMESTAMP` | `1787895337992` |

`DfsApplication.b()` 用前三个初始化 KonaPayment SDK（`C2970ou.init`），并在 URL 为 https 时加载固定证书 `res/raw/nagad_server_certificate.cer`。

## 4. 网络与加密

### 4.1 传输层
- `usesCleartextTraffic=false`，`network_security_config` 禁止明文、仅信任系统根。
- **证书固定**：`res/raw/` 内置 `nagad_server_certificate.cer` 及多个历史证书（`_old`/`_old2`/`_old3`）、`map_certificate.cer`、`current_map_cert.cer`、`new_cert.cer`、`sign_cert.der`，自定义 TrustManager 校验。抓包需绕过固定。
- **MQTT**：`tcp://app2.mynagad.com:1883` 用于推送/长连接。

### 4.2 应用层（KonaPayment SDK）
- 请求头常量 `HeaderConstant`：`X-KM-AUTH-TOKEN`、`X-KM-REFRESH-TOKEN`、`X-KM-AUTH-SESSION`、`X-KM-DEVICE-FGP`、`X-KM-USER-ACTION`、`X-KM-USER-ACT-FGP`、`X-KM-Encryption-Algorithm`(值 `PKCS7`)、`X-KM-USING-SECURE-KEYBOARD`、`X-MPA-Id` 等。
- **安全通信/密钥会话**：`MobilePlatformDaoSecureCommunicationAdapterImpl` 包裹真实 `MobilePlatformDao`，通过 `manager.a` 的 `isSecureSessionAvailable()` / `startSecureSessionWithTriggerSession()` 走“安全会话”。配套端点：`/api/wallet/session/start`、`/api/wallet/trigger-session`、`/api/replenishKeys/getKeys`、`/api/rm/wallet/session/close`，相关模型 `SecureCommunicationRequest/Response`、`SecureMessagingModel`、`MobileKeysDetails`、`TransactionKeyModel`。部分接口前缀为 `/secure-filter/**`。
- **白盒密码**：`com.konasl.konawhiteboxcryptography.KonaWbc`（`libkonaWbc.so` + `c++_shared`）做白盒加解密，密钥隐藏于二进制，用于保护本地密钥。
- **本地数据加密 (LDE)**：`com.konasl.sdk.storage.lde`（`LdeEncryptionException` / `WhiteBoxDecryptionException` 等），结合白盒库加密本地存储。
- **安全键盘**：`com.konasl.secure.keyboard.SecureKeyboardView` + `cipher` 包，配合 `X-KM-USING-SECURE-KEYBOARD` 头，PIN 输入不走系统键盘。
- **原生密码库**：`libkonacipher.so`（`CCBP.SN.loadLibrary("konacipher")`），Java 侧门面在 `com.konasl.security.service.*`，覆盖 **AES / DES / DES3 / RSA / ECC / SEED(国密/KISA) / SHA / 随机数**（各自 `*CipherJni`，底层 context 指针 + JNI）。
- **支付相关原生库**：`libpayment.so`（konaprepay）、`libvisa.so`（VISA 交易）、`libnative-lib-payment-sdk.so`（sslcommerz）。

## 5. KonaShield 安全模块（重点）

包 `konashield.security.konasl.com.konashield.security`，是一个集成式安全 SDK。

| 能力 | 实现 | 说明 |
|---|---|---|
| Root 检测 | `ShieldNativeLibraryImpl` → `libshield.so` (`rDk()`/`fDr()`/`uDs()`) | 原生检测 rooted device / rooted files / superuser |
| Hook 检测 | `HookCheckerImpl.isApplicationHooked()` | 抛异常取栈，扫描 `XposedBridge`、`com.saurik.substrate`、`frida`、`riru`、`lsposed`、`edxp`、`zygisk` |
| 已装 Hook/root 工具 | `HookCheckerImpl.isHookedApplicationInstalled()` | 按包名匹配 xposed/rootcloak/frida-server/SuperSU/Magisk 等 10 个 |
| 篡改检测 | `TamperCheckerImpl` + `CryptoUtil.getHash` (SHA-256) | 对签名证书做 SHA-256 与 `KonaShieldManager.getAppSignature()` 比对（`APPLICATION_SIGNATURE`） |
| 调试/模拟器/USB 调试 | `DeviceCompatibilityCheckerImpl` | `isAppOnDebugMode` / `isAppRunningOnEmulator` / `isUsbDebugOn` |
| 恶意软件扫描 | `MalwareScannerImpl` | — |
| 周期巡检 | `KonaShieldService`（Manifest `:1135` 声明） | 后台 `Timer`，默认每 **5000ms** 跑一遍全部检查 |

关键结论（对 root 环境友好）：
- `KonaShieldManager` 里 `isCrashEnabledOnRootDetected/HookDetected/Debug/Emulator` **默认全部 false**，即检出后**默认不崩溃、不退出**。
- `SplashActivity.O()` 只调用 `runRootingCheck()`，回调 `onRootDetected()` **仅上报埋点** `EV_DEVICE_DEVICE_IS_ROOTED`（含 `device_id`），**不阻断**进入。
- `HookCheckerImpl.isApplicationHooked()` 即使命中（含 Zygisk/LSPosed/Frida 字样），因为 `isCrashEnabledOnHookDetected=false`，也只是 `return false`（注意末尾始终 `return false`，检出信息不生效于布尔返回）。→ 在带 Zygisk/LSPosed 的 rooted 设备上，本 App 的该层检测实际不拦截。

## 6. API 网关与接口清单

- Base URL：`https://app2.mynagad.com:20002`（HTTPS，端口 20002）
- 接口定义：`com.konasl.konapayment.sdk.map.client.dao.MobilePlatformDao`（207 个注解端点）
- 动态转发：`CCBP.InterfaceC1575cC0`（`@POST("{url}")` / `@GET("{url}")`）
- 完整清单见工作目录 **`NagadAgent-endpoints.md`**（207 条）与 `NagadAgent-endpoints.tsv`。

典型分组：`/api/user/*`、`/api/wallet/*`、`/api/dfs/*`（B2B/现金进出/P2P/缴费/DPS/DMO/DPO）、`/api/external/kyc/*`（eKYC/dKYC 注册与人脸证件）、`/api/external/mno/*`（运营商）、`/api/rm/*`（远程管理/会话）、`/secure-filter/*`（安全过滤通道）、`/api/replenishKeys/getKeys`（密钥补充）。

## 7. 认证流程（Tier 2）

关键端点：

### `POST /api/login`
- 源：`MobilePlatformDao.login(LoginRequest, LoginApiCallback<LoginBaseResponse>)`（MobilePlatformDao.java:667）
- 请求体 `LoginRequest`：`username`、`password`、`aspId`(=100012345612345)、`mpaId`
- Base URL：`https://app2.mynagad.com:20002`

### 其他认证相关
| 端点 | 用途 |
|---|---|
| `POST /api/logout` | 登出 |
| `GET /api/refreshToken` | 刷新 token |
| `GET /api/user/check-otp` | 校验 OTP（`userId`,`event`,`authCode`） |
| `GET /api/user/check-ussd-otp` | USSD OTP（`msisdn`,`ussdOtp`） |
| `POST /api/user/register` `/user/register` | 注册 |
| `POST /api/wallet/login` | 钱包口令认证 |
| `POST /api/user/resendActivationData` | 重发激活数据（对应你看到的“新设备激活”流程） |
| `POST /api/dfs/user/register-and-set-pin` | 注册并设置 PIN |
| `PUT /api/dfs/user/reset-pin` | 重置 PIN |
| `POST /api/wallet/changeDevice` | 换设备 |

当前设备屏幕即“新设备激活”引导：拨 `*167#` → My Nagad → Allow New Device → Android → 输入 PIN → 记下 Token → 用 Token+PIN+OTP 重新登录（对应 `resendActivationData` / `generateSetPassToken` / `check-ussd-otp` 一类接口）。

## 8. 其他要点

- Manifest 有 **130 个 Activity**，多数 `exported=false`；`GoogleSmsReceiver`、`FirebaseInstanceIdReceiver`、`Facebook CustomTabActivity` 等 exported。
- 权限较克制：CAMERA、POST_NOTIFICATIONS、网络状态、READ_CONTACTS、NFC、WAKE_LOCK、VIBRATE、AD_ID，无定位/读写外部存储（API>28）。
- **泄露的第三方密钥**：`com.google.android.geo.API_KEY = AIzaSyDoV6YzlihWweFfcRxWW3-FGVHfuS8-RTg`（Google Maps key）；Fabric `DEVELOPER_TOKEN`。
- 数据库：ActiveAndroid，`AA_DB_NAME=dfs_channel`，`AA_DB_VERSION=2`。
- 明文 MQTT 端口 1883（`tcp://`），非 TLS，是相对薄弱的一环。

## 9. 复现命令

```powershell
# 拉取并反编译
adb pull /data/app/.../base.apk _Temp\nagad_base.apk
& "$env:USERPROFILE\.config\opencode\skills\android-reverse-engineering\scripts\decompile.ps1" `
  -o _Temp\nagad-decompiled _Temp\nagad_base.apk
```
