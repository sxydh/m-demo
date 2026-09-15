# BOI Mobile 接口文档

- App：`com.boi.ua.android` v3.9.0
- 全流程：**① SIM 绑定 / 设备登记（登录前置）→ ② 登录（OAuth2 + PKCE + 登录挑战）**
- SIM 绑定采用 **UPI 版**端点（`ciamregsrvc/upi/*`）；非 UPI 孪生版（`registration/saveSimBindingInfo`、`registration/checkBindingKeyStatus`、`registration/verifyEmailAndMobileOtp`）payload 同构，本文档不展开

---

## 第一部分 · SIM 绑定（注册链，登录前置）

### API-001 · VMN 号池查询

- 描述：获取 SIM 绑定的收信号池（VMN 虚拟号）与短信关键词
- 方法：`GET`
- 地址：`https://uacymconnect.bankofindia.bank.in/ciamregsrvc/vmn/getMobileNumberForMsp`
- 鉴权：无（匿名可访问）
- 加密：无（响应明文）
- 关键代码：`getMobileNumberForMspUsingGET`

#### 请求头

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| locLatitude | string | 否 | 纬度（实测可省略） |
| locLongitude | string | 否 | 经度（实测可省略） |

#### 请求示例

```bash
curl.exe -sS "https://uacymconnect.bankofindia.bank.in/ciamregsrvc/vmn/getMobileNumberForMsp" \
  -H "locLatitude: 28.61" -H "locLongitude: 77.20"
```

#### 响应参数

| 参数 | 类型 | 说明 |
|---|---|---|
| success | boolean | 是否成功 |
| data.vmn | string | 主号池，逗号分隔的 VMN 号码 |
| data.vmnSecondary | string | 备号池，逗号分隔的 VMN 号码 |
| data.keyword | string | 短信关键词 |
| timestamp | string | 服务端时间 |
| message | string | 提示信息，可空 |
| errorCode | string | 错误码，可空 |
| errorMessage | string | 错误信息，可空 |

#### 响应示例

```json
{"success":true,"data":{"vmn":"+917291022020, +917290865959","vmnSecondary":"+918796449647, +919217665879","keyword":"BOIUAP"},"timestamp":"14-Sep-2026 12:40:40","message":null,"errorCode":null,"errorMessage":null}
```

---

### API-002 · 提交 SIM 绑定 / 取流程 token

- 描述：把 `deviceId ↔ simNumber` 提交给注册服务登记，返回本次绑定会话的流程 token
- 方法：`POST`
- 地址：`https://uacymconnect.bankofindia.bank.in/ciamregsrvc/upi/saveSimBindingInfoUpi`
- 鉴权：无（匿名，swagger 仅要求 dto）
- 加密：无
- 关键代码：`saveSimBindingInfoUpiUsingPOST`（业务封装 `saveSimBindingInfoApiUpi`）

#### 请求头

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| Content-Type | string | 是 | 固定 `application/json` |
| locLatitude | string | 否 | 纬度 |
| locLongitude | string | 否 | 经度 |

#### 请求体（application/json）

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| deviceId | string | 是 | 设备身份串；同一标识在 checkDevice 中为 `ANDROID_ID + subscriptionId + mobileNo`（2173386-2173398），UPI 绑定链由 `performSimBinding` 作为第 4 参下传（1552572-1552579） |
| simNumber | string | 是 | Android 分支 = `simSerialNumber(ICCID) + subscriptionId`；iOS 传空串（1554089-1554107） |
| ip | string | 是 | 客户端 IP，可为空串 `""` |
| codeChallenge | string | 是 | PKCE 挑战：`base64url(SHA256(codeVerifier))`，verifier 为 32 位随机串（364442-364478） |
| challengeMethod | string | 是 | 固定 `"SHA256"` |

#### 前置步骤

1. **API-001** 取到号池（vmn/vmnSecondary/keyword）
2. 原生取 SIM 数据：`subscriptionId`、`simSerialNumber`（`NativeSimData` / `getSimDetailsService`，371463-371502）
3. 原生/本地生成 PKCE 对（`getCodeVerifierAndCodeChallange()`，364426）；Android 另生成短信用 `hashCode = sha256(deviceId + simNumber + codeChallenge)`（1551804-1551847）

#### 请求示例

```bash
curl.exe -sS -X POST "https://uacymconnect.bankofindia.bank.in/ciamregsrvc/upi/saveSimBindingInfoUpi" \
  -H "Content-Type: application/json" -H "locLatitude: 28.61" -H "locLongitude: 77.20" \
  --data '{"deviceId":"6647042540","simNumber":"9711848011","ip":"","codeChallenge":"E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM","challengeMethod":"SHA256"}'
```

（示例值取自代码内调试常量，服务端不校验其真实性）

#### 响应参数

| 参数 | 类型 | 说明 |
|---|---|---|
| success | boolean | 是否成功 |
| data.token | string | 流程 token（JWT），供 API-003/004 |
| timestamp | string | 服务端时间 |
| message | string | 提示信息，可空 |
| errorCode | string | 错误码，可空 |
| errorMessage | string | 错误信息，可空 |

#### 响应示例

```json
{"success":true,"data":{"token":"eyJhbGciOiJIUzI1NiJ9.eyJyZXN1bHQiOnRydWUsInJvbGVzIjpbIlZFUklGWV9NU1BfVVBJIl0sInRlbmFudElkIjoidWFjeW1jb25uZWN0IiwidXNlclR5cGUiOm51bGwsImV4cCI6MTc4OTM5MjU5MCwidXNlcklkIjoiNmFhN2VmMTI5OGNhODM0ZTNhOTczMTUzIiwiaWF0IjoxNzg5MzkwNjEwfQ.WH_rBzt5zRHQmaT9zfX9tUu5keOxp9Z1PN9L9EZUWiI"},"timestamp":"14-Sep-2026 12:56:50","message":null,"errorCode":null,"errorMessage":null}
```

token payload（base64url 解码，非加密）：`{"result":true,"roles":["VERIFY_MSP_UPI"],"tenantId":"uacymconnect","userType":null,"exp":1789392590,"userId":"6aa7ef1298ca834e3a973153","iat":1789390610}`（约 33 分钟有效）

#### 后续

绑定接口只登记，不校验 SMS。紧随其后客户端用原生 `sendSMSToVmn(号码, hashCode, subscriptionId)` 向 VMN 号池发静默短信（1554151-1554163），再用 API-003 轮询。

#### 错误码

| errorCode | 说明 |
|---|---|
| REGNSRVC.INVALID_ARGUMENTS | 参数缺失/不合法 |
| SAVE_SIM_BINDING_ISSUE | 绑定数据保存失败 |

---

### API-003 · 轮询绑定状态

- 描述：轮询服务端是否已收到静默短信并完成号码归属确认；成功时返回绑定状态与 `sendOtpResponseDto`
- 方法：`POST`
- 地址：`https://uacymconnect.bankofindia.bank.in/ciamregsrvc/upi/checkBindingKeyStatusUpi`
- 鉴权：`Bearer <API-002 的流程 token>`（必填）
- 加密：无
- 关键代码：`checkBindingKeyStatusUpiUsingPOST`（业务封装 `verifySmsSentToVmnServer`）
- 轮询节奏：间隔 2 秒，最多若干次（1554203-1554240）

#### 请求头

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| Authorization | string | 是 | `Bearer <API-002 返回的 token>` |
| Content-Type | string | 是 | 固定 `application/json` |
| locLatitude | string | 否 | 纬度 |
| locLongitude | string | 否 | 经度 |

#### 请求体（application/json）

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| codeVerifier | string | 是 | API-002 的 PKCE verifier，须与 codeChallenge 配对 |

#### 请求示例

```bash
curl.exe -sS -X POST "https://uacymconnect.bankofindia.bank.in/ciamregsrvc/upi/checkBindingKeyStatusUpi" \
  -H "Content-Type: application/json" -H "Authorization: Bearer <API-002 token>" \
  -H "locLatitude: 28.61" -H "locLongitude: 77.20" \
  --data '{"codeVerifier":"dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk"}'
```

#### 响应参数

| 参数 | 类型 | 说明 |
|---|---|---|
| success | boolean | 是否成功 |
| data | object | 成功时含 `simbindingstatus`、`token`（JWT，claims 含 `mobileNumber`）、`sendOtpResponseDto`（1552204-1552221；客户端读法 1544237-1544305） |
| timestamp | string | 服务端时间 |
| message | string | 提示信息，可空 |
| errorCode | string | 错误码，可空 |
| errorMessage | string | 错误信息，可空 |

#### 响应示例

```json
{"success":false,"data":null,"timestamp":"14-Sep-2026 12:56:53","message":null,"errorCode":"REGNSRVC.VERIFICATION_PENDING","errorMessage":""}
```

#### 错误码

| 返回 | 说明 |
|---|---|
| 403 `{"error":"Unable to parse token, token is malformed"}` | 未带/带错误 token |
| REGNSRVC.CODE_VERIFIER_FAILED | codeChallenge/codeVerifier 不配对 |
| REGNSRVC.VERIFICATION_PENDING | 挑战配对成功，等待绑定短信验证 |
| CHECK_KEY_BINDING_ERROR | 绑定结果查询失败 |

---

### API-004 · 校验 UPI 绑定手机 OTP

- 描述：绑定短信验证通过后，校验服务端下发的手机 OTP（`sendOtpResponseDto`）；UPI SIM 绑定流程的最后一步
- 方法：`POST`
- 地址：`https://uacymconnect.bankofindia.bank.in/ciamregsrvc/upi/verifyUpiMobileOtp`
- 鉴权：`Bearer <API-003 成功时返回的 data.token>`（App 实际用这个；实测 API-002 的流程 token 服务端同样放行）
- 加密：无
- 关键代码：`verifyUpiMobileOtpUsingPOST`（业务封装 `getMobileOTPUpi`）

#### 请求头

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| Authorization | string | 是 | `Bearer <流程 token>` |
| Content-Type | string | 是 | 固定 `application/json` |
| locLatitude | string | 否 | 纬度 |
| locLongitude | string | 否 | 经度 |

#### 请求体（application/json）

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| mobileOtp | string | 是 | 服务端下发的手机 OTP（index.decompiled.js:2188889） |

#### 前置步骤

- OTP 的下发信息来自 API-003 成功响应的 `sendOtpResponseDto`；客户端用轮询成功响应里的 `token`（JWT，claims 含 `mobileNumber`）展示脱敏号码（1544237-1544305）
- 校验通过 = UPI SIM 绑定链结束（服务端完成"号码归属 + 设备"登记）。登录链见第二部分，二者的衔接点是 API-005：设备登记过，checkDevice 才会下发设备 token

#### 请求示例

```bash
curl.exe -sS -X POST "https://uacymconnect.bankofindia.bank.in/ciamregsrvc/upi/verifyUpiMobileOtp" \
  -H "Content-Type: application/json" -H "Authorization: Bearer <流程 token>" \
  --data '{"mobileOtp":"123456"}'
```

#### 响应参数

| 参数 | 类型 | 说明 |
|---|---|---|
| success | boolean | 是否成功 |
| data | object | 校验结果 |
| timestamp | string | 服务端时间 |
| errorCode | string | 错误码，可空 |
| errorMessage | string | 错误信息，可空 |

#### 错误码

| errorCode | 说明 |
|---|---|
| `{"error":"Unable to parse token, token is malformed"}` | 未带 token |
| `{"error":"auth: The two fields do not match"}` | token 与绑定会话不匹配 |
| MFASRVC.INVALID_SMS_OTP / REGNSRVC.MOBILE_OTP_NOT_VERIFIED | OTP 错误 / 未验证 |
| MFASRVC.RESEND_COUNT_EXCEED | 重发次数超限 |

#### 重发 OTP

| 用途 | 方法+路径 |
|---|---|
| 重发手机 OTP | `POST /ciamregsrvc/registration/resendOtpMobile`（body `{mobileNo, reSend:true, attempt}`，1553381-1553402） |

---

## 第二部分 · 登录

登录链 = **设备校验（取设备 token）→ OAuth2 授权码 + PKCE + 登录挑战 → 换 accessToken**。固定常量（代码硬编码，396569-396602）：`clientId=ibmopenidclient`、`scope=openid`、`state=2nJR8aiorREJYUuePL29z0JucIo4guU7rx1qZLT4mTc%3D`、`redirectUri=http%3A%2F%2F127.0.0.1%3A3000%2Fcallback`、`grantType=authorization_code`、`codeChallengeMethod=S256`。

PKCE 对由客户端本地生成（登录链用 `getCodeVerifierAndCodeChallange()`，2188206；verifier = `getRandomStrings(32)`，challenge = `base64url(SHA256(verifier))`，364442-364478），`codeVerifier` 本地保留供 API-009 使用。

---

### API-005 · 设备校验 / 取设备 token

- 描述：用设备身份串查询后端是否认识本机；认识则发设备 token（JWT），不认识返回 `token:null`（App 随即跳注册流程）
- 方法：`POST`
- 地址：`https://uacymconnect.bankofindia.bank.in/ciamregsrvc/registration/checkDevice`
- 鉴权：无（swagger 仅要求 dto）
- 加密：无
- 关键代码：`checkDeviceUsingPOST`（业务 `checkDevice`/`checkDeviceAPICall`）

#### 请求头

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| Content-Type | string | 是 | 固定 `application/json` |
| locLatitude | string | 否 | 纬度 |
| locLongitude | string | 否 | 经度 |

#### 请求体（application/json）

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| applicationId | string | 是 | 包名，`getBundleId()` → `com.boi.ua.android` |
| deviceId | string | 是 | 同 API-002：`ANDROID_ID + subscriptionId + mobileNo` |

#### 请求示例

```bash
curl.exe -sS -X POST "https://uacymconnect.bankofindia.bank.in/ciamregsrvc/registration/checkDevice" \
  -H "Content-Type: application/json" \
  --data '{"applicationId":"com.boi.ua.android","deviceId":"<16位hex>1<手机号>"}'
```

#### 响应参数

| 参数 | 类型 | 说明 |
|---|---|---|
| success | boolean | 是否成功 |
| data.token | string | 设备 token（JWT）；**未登记设备为 null** |
| timestamp | string | 服务端时间 |
| errorCode | string | 错误码，可空 |

#### 响应示例

```json
{"success":true,"data":{"token":null},"timestamp":"15-Sep-2026 06:10:01","message":null,"errorCode":null,"errorMessage":null}
```

#### 说明

- 设备 token 客户端存入 redux `loginRegister.checkDeviceToken`（仅 3 处赋值，全来自本接口：2173931、2204828、2224122）
- 它同时用于：登录 API-007 的 `Authorization: Bearer`、pre-login 类 UPI 接口的 `MFA:` 头（357563/357581）、JWT 解码后喂 `getUPITransactionLimits` 等（572604-572617）
- `token:null` ⇒ 该 deviceId 未被后端登记，需先完成第一部分（SIM 绑定）注册流程

---

### API-006 · 用户状态检查

- 描述：输入用户 ID 后校验用户是否存在/锁定，成功返回校验 token。**注意：此步属注册链（代码事件 `LogRegistrationETB`），登录本身从 API-007 开始**，保留此处仅供注册/开户复刻参考
- 方法：`POST`
- 地址：`https://uacymconnect.bankofindia.bank.in/ciamauthsrvc/oauth2/user/validate`
- 鉴权：无（客户端级）
- 加密：无
- 关键代码：`validateUser` → `validateUserUsingPOST`

#### 请求头

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| Content-Type | string | 是 | 固定 `application/json` |
| locLatitude | string | 否 | 纬度 |
| locLongitude | string | 否 | 经度 |

#### 请求体（application/json）

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| login | string | 是 | 用户 ID（customerId），index.decompiled.js:2185415 |

#### 请求示例

```bash
curl.exe -sS -X POST "https://uacymconnect.bankofindia.bank.in/ciamauthsrvc/oauth2/user/validate" \
  -H "Content-Type: application/json" \
  --data '{"login":"<customerId>"}'
```

#### 响应参数

| 参数 | 类型 | 说明 |
|---|---|---|
| success | boolean | 是否成功 |
| data.token | string | 校验通过 token，客户端在 2185533 取出后传给下一步编排 |
| timestamp | string | 服务端时间 |
| errorCode | string | 错误码，可空 |
| errorMessage | string | 错误信息，可空 |

#### 响应示例

（待实测补充）

#### 错误码

| errorCode | 说明 |
|---|---|
| CIAMAUTHSRVC_USER_LOCKED | 用户锁定 |
| USRSRVC_USER_NOT_FOUND | 用户不存在（提示 Kindly Enter Valid Customer Id） |

---

### API-007 · OAuth 授权 / 取登录挑战

- 描述：PKCE 授权第一步，服务端返回本次登录的 `loginChallenge` 与 `authToken`
- 方法：`POST`
- 地址：`https://uacymconnect.bankofindia.bank.in/ciamauthsrvc/oauth2/authorize`
- 鉴权：`Bearer <API-005 的设备 token>`
- 加密：无（body 明文）
- 关键代码：`authorizeUsingPOST`

#### 请求头

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| Authorization | string | 是 | `Bearer <API-005 返回的设备 token>` |
| Content-Type | string | 是 | 固定 `application/json` |
| locLatitude | string | 否 | 纬度 |
| locLongitude | string | 否 | 经度 |

#### 请求体（application/json）

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| clientId | string | 是 | 固定 `ibmopenidclient` |
| scope | string | 是 | 固定 `openid` |
| state | string | 是 | 固定值（硬编码，URL 编码态） |
| codeChallenge | string | 是 | `base64url(SHA256(codeVerifier))` |
| codeChallengeMethod | string | 是 | 固定 `S256` |

#### 请求示例

```bash
curl.exe -sS -X POST "https://uacymconnect.bankofindia.bank.in/ciamauthsrvc/oauth2/authorize" \
  -H "Content-Type: application/json" -H "Authorization: Bearer <API-005 返回的设备 token>" \
  --data '{"clientId":"ibmopenidclient","scope":"openid","state":"2nJR8aiorREJYUuePL29z0JucIo4guU7rx1qZLT4mTc%3D","codeChallenge":"<base64url sha256>","codeChallengeMethod":"S256"}'
```

#### 响应参数

| 参数 | 类型 | 说明 |
|---|---|---|
| success | boolean | 是否成功 |
| data.loginChallenge | string | 登录挑战值，供 API-008 |
| data.authToken | string | 挑战会话 token，供 API-008 |
| timestamp | string | 服务端时间 |
| errorCode | string | 错误码，可空 |
| errorMessage | string | 错误信息，可空 |

#### 响应示例

（待实测补充；字段名依据客户端读取代码 2188456-2188457）

#### 错误码

| 返回 | 说明 |
|---|---|
| 403 `{"error":"Unable to parse token, token is malformed"}` | 未带/带错误设备 token（实测） |
| 502 | 空 `Bearer ` 等异常 token 导致后端 502（实测） |

---

### API-008 · 提交凭据过登录挑战

- 描述：把用户 MPIN（或网银密码等）应答给服务端，通过后返回授权码 `code`
- 方法：`POST`
- 地址（MPIN，默认方式）：`https://uacymconnect.bankofindia.bank.in/ciamauthsrvc/oauth2/acceptMpinChallenge`
- 地址（网银密码）：`https://uacymconnect.bankofindia.bank.in/ciamauthsrvc/oauth2/acceptLoginChallenge`
- 鉴权：`Bearer <API-005 的设备 token>`
- 加密：无（mpin/password 明文置于 body，依赖 HTTPS；客户端未发现本地加密）
- 关键代码：`acceptMpinChallenge` / `acceptLoginChallenge`

#### 请求头

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| Authorization | string | 是 | `Bearer <API-005 返回的设备 token>` |
| Content-Type | string | 是 | 固定 `application/json` |
| locLatitude | string | 否 | 纬度 |
| locLongitude | string | 否 | 经度 |
| eventId | string | 否 | 事件ID（`getEventId()`，280060） |

#### 请求体（application/json，MPIN 版）

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| login | string | 是 | 登录 ID（customerId） |
| mpin | string | 是 | 用户 MPIN |
| loginChallenge | string | 是 | API-007 返回值 |
| authToken | string | 是 | API-007 返回值 |

密码版将 `mpin` 换成 `password`（网银密码）。

#### 前置步骤

1. `login`：redux `customerInfo.customerId`（2187690-2187691）
2. `loginChallenge`/`authToken`：API-007 响应，客户端缓存于 redux `authAPIData.response`，有缓存则跳过重新 authorize（2188082-2188115）
3. 用户选择验证方式：`MPIN` → acceptMpinChallenge；`IB` → acceptLoginChallenge（2188463-2188471）

#### 请求示例

```bash
curl.exe -sS -X POST "https://uacymconnect.bankofindia.bank.in/ciamauthsrvc/oauth2/acceptMpinChallenge" \
  -H "Content-Type: application/json" -H "Authorization: Bearer <API-005 返回的设备 token>" \
  --data '{"login":"<customerId>","mpin":"123456","loginChallenge":"<API-007 返回>","authToken":"<API-007 返回>"}'
```

#### 响应参数

| 参数 | 类型 | 说明 |
|---|---|---|
| success | boolean | 是否成功 |
| data.code | string | 授权码，供 API-009 |
| data.mfaInitiateResponseTemplate | object | 非空 = 触发 MFA，需再走 mfa/initiate + mfa/verify 后重取 |
| timestamp | string | 服务端时间 |
| errorCode | string | 错误码，可空 |
| errorMessage | string | 错误信息，可空 |

#### 响应示例

（待实测补充；code 字段依据客户端消费代码，MFA 分支见 2188835）

#### 错误码

| errorCode | 说明 |
|---|---|
| CIAMAUTHSRVC_INVALID_LOGIN_CHALLENGE | 挑战失效/不匹配 |
| CIAMAUTHSRVC_USER_LOCKED | 用户锁定 |

#### MFA 变体（同为挑战应答，路径不同，触发条件由服务端策略 `GET auth/mpin/mfa/list` 决定）

| 验证方式 | 路径 |
|---|---|
| 借记卡 | `/ciamauthsrvc/oauth2/debitCard/verify` |
| 信用卡 | `/ciamauthsrvc/oauth2/creditCard/verify` |
| KYC | `/ciamauthsrvc/oauth2/user/kyc/verify/initiate` + `.../finish` |
| 生物识别 | `/ciamauthsrvc/oauth2/acceptBiometricChallenge` |

---

### API-009 · 授权码换 Token

- 描述：`code + codeVerifier` 换取最终令牌，成功后即登录完成（App 存 `Bearer + access_token` 为会话凭据）
- 方法：`POST`
- 地址：`https://uacymconnect.bankofindia.bank.in/ciamauthsrvc/oauth2/token`
- 鉴权：`Bearer <API-005 的设备 token>`
- 加密：无
- 关键代码：`getAccessTokenUsingPOST`（wrapper：`getAccessToken`）

#### 请求头

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| Authorization | string | 是 | `Bearer <API-005 返回的设备 token>` |
| Content-Type | string | 是 | 固定 `application/json` |
| locLatitude | string | 否 | 纬度 |
| locLongitude | string | 否 | 经度 |

#### 请求体（application/json）

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| code | string | 是 | API-008 返回的授权码 |
| codeVerifier | string | 是 | 本地 PKCE verifier（与 API-007 的 codeChallenge 配对） |
| clientId | string | 是 | 固定 `ibmopenidclient` |
| scope | string | 是 | 固定 `openid` |
| state | string | 是 | 固定值 |
| redirectUri | string | 是 | 固定 `http%3A%2F%2F127.0.0.1%3A3000%2Fcallback` |
| grantType | string | 是 | 固定 `authorization_code` |

#### 请求示例

```bash
curl.exe -sS -X POST "https://uacymconnect.bankofindia.bank.in/ciamauthsrvc/oauth2/token" \
  -H "Content-Type: application/json" -H "Authorization: Bearer <API-005 返回的设备 token>" \
  --data '{"code":"<API-008 code>","codeVerifier":"<本地>","clientId":"ibmopenidclient","scope":"openid","state":"2nJR8aiorREJYUuePL29z0JucIo4guU7rx1qZLT4mTc%3D","redirectUri":"http%3A%2F%2F127.0.0.1%3A3000%2Fcallback","grantType":"authorization_code"}'
```

#### 响应参数

`data` 内为标准 OAuth2 字段（snake_case），外层为通用信封：

| 参数 | 类型 | 说明 |
|---|---|---|
| data.access_token | string | 会话令牌，后续业务接口 `Bearer <access_token>` |
| data.expires_in | number | 有效期（秒） |
| data.id_token | string | OIDC id token（JWT） |
| data.refresh_token | string | 续期用，走 `/ciamauthsrvc/oauth2/token/refresh` |
| timestamp | string | 服务端时间 |
| errorCode | string | 错误码，可空 |
| errorMessage | string | 错误信息，可空 |

#### 响应示例

（待实测补充；字段名依据客户端读取代码 2189665-2189668）

---

### 配套端点

| 用途 | 方法+路径 |
|---|---|
| 会话续期 | `POST /ciamauthsrvc/oauth2/token/refresh` |
| 登出/吊销 | `POST /ciamauthsrvc/oauth2/revoke` |
| 拉用户信息（登录后） | `POST /ciamauthsrvc/oauth2/getUserInfo` |
| UPI 结果查询（登录后） | `POST /ciamregsrvc/upi/upiStatusCheck` |
| 注销设备 | `POST /ciamregsrvc/registration/deregisterDevice` |
