# BOI Mobile 接口文档

- App：`com.boi.ua.android` v3.9.0

---

## API-001 · VMN 号池查询

- 描述：获取 SIM 绑定的收信号池（VMN 虚拟号）与短信关键词
- 方法：`GET`
- 地址：`https://uacymconnect.bankofindia.bank.in/ciamregsrvc/vmn/getMobileNumberForMsp`
- 鉴权：无（匿名可访问）
- 加密：无（响应明文）
- 关键代码：`getMobileNumberForMspUsingGET`

### 请求头

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| locLatitude | string | 否 | 纬度（实测可省略） |
| locLongitude | string | 否 | 经度（实测可省略） |

### 请求示例

```bash
curl.exe -sS "https://uacymconnect.bankofindia.bank.in/ciamregsrvc/vmn/getMobileNumberForMsp" \
  -H "locLatitude: 28.61" -H "locLongitude: 77.20"
```

### 响应参数

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

### 响应示例

```json
{"success":true,"data":{"vmn":"+917291022020, +917290865959","vmnSecondary":"+918796449647, +919217665879","keyword":"BOIUAP"},"timestamp":"14-Sep-2026 12:40:40","message":null,"errorCode":null,"errorMessage":null}
```

---

## API-002 · 提交 SIM 绑定 / 取流程 token

- 描述：提交绑定信息，服务端返回本次绑定会话的流程 token（供 API-003 用）
- 方法：`POST`
- 地址：`https://uacymconnect.bankofindia.bank.in/ciamregsrvc/upi/saveSimBindingInfoUpi`
- 鉴权：无（匿名可访问；任意 deviceId/simNumber 均发放 token，不校验设备）
- 加密：无
- 关键代码：`saveSimBindingInfoUpiUsingPOST`

### 请求头

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| Content-Type | string | 是 | 固定 `application/json` |
| locLatitude | string | 否 | 纬度 |
| locLongitude | string | 否 | 经度 |

### 请求体（application/json）

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| deviceId | string | 是 | 设备ID（任意值均可） |
| simNumber | string | 是 | SIM 号码 |
| ip | string | 是 | 客户端IP，传空串 `""` |
| codeChallenge | string | 是 | PKCE 挑战值，须为 `base64url(SHA256(codeVerifier))`（服务端本步不校验） |
| challengeMethod | string | 是 | 固定 `"SHA256"` |

### 说明

`codeVerifier` 由客户端本地随机生成（`getRandomStrings(32)`，字符集 `A-Za-z0-9`），`codeChallenge = base64url(SHA256(codeVerifier))`；App 本地保存 `codeVerifier` 供 API-003 使用。属标准 PKCE，非服务端下发。

### 请求示例

```bash
curl.exe -sS -X POST "https://uacymconnect.bankofindia.bank.in/ciamregsrvc/upi/saveSimBindingInfoUpi" \
  -H "Content-Type: application/json" -H "locLatitude: 28.61" -H "locLongitude: 77.20" \
  --data '{"deviceId":"6647042540","simNumber":"9711848011","ip":"","codeChallenge":"E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM","challengeMethod":"SHA256"}'
```

### 响应参数

| 参数 | 类型 | 说明 |
|---|---|---|
| success | boolean | 是否成功 |
| data.token | string | 流程 token（JWT，HS256） |
| timestamp | string | 服务端时间 |
| message | string | 提示信息，可空 |
| errorCode | string | 错误码，可空 |
| errorMessage | string | 错误信息，可空 |

### 响应示例

```json
{"success":true,"data":{"token":"eyJhbGciOiJIUzI1NiJ9.eyJyZXN1bHQiOnRydWUsInJvbGVzIjpbIlZFUklGWV9NU1BfVVBJIl0sInRlbmFudElkIjoidWFjeW1jb25uZWN0IiwidXNlclR5cGUiOm51bGwsImV4cCI6MTc4OTM5MjU5MCwidXNlcklkIjoiNmFhN2VmMTI5OGNhODM0ZTNhOTczMTUzIiwiaWF0IjoxNzg5MzkwNjEwfQ.WH_rBzt5zRHQmaT9zfX9tUu5keOxp9Z1PN9L9EZUWiI"},"timestamp":"14-Sep-2026 12:56:50","message":null,"errorCode":null,"errorMessage":null}
```

token payload（base64url 解码，非加密）：`{"result":true,"roles":["VERIFY_MSP_UPI"],"tenantId":"uacymconnect","userType":null,"exp":1789392590,"userId":"6aa7ef1298ca834e3a973153","iat":1789390610}`（约 33 分钟有效）

### 错误码

| errorCode | 说明 |
|---|---|
| REGNSRVC.INVALID_ARGUMENTS | 参数缺失/不合法 |

---

## API-003 · 轮询绑定状态

- 描述：查询 SIM 绑定结果；成功时返回绑定状态、新 `token`（JWT）与 `sendOtpResponseDto`（OTP 下发信息）
- 方法：`POST`
- 地址：`https://uacymconnect.bankofindia.bank.in/ciamregsrvc/upi/checkBindingKeyStatusUpi`
- 鉴权：`Bearer <API-002 的 token>`
- 加密：无
- 关键代码：`checkBindingKeyStatusUpiUsingPOST`

### 请求头

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| Authorization | string | 是 | `Bearer <API-002 返回的 token>` |
| Content-Type | string | 是 | 固定 `application/json` |
| locLatitude | string | 否 | 纬度 |
| locLongitude | string | 否 | 经度 |

### 请求体（application/json）

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| codeVerifier | string | 是 | PKCE verifier，须满足 `base64url(SHA256(codeVerifier)) == API-002 的 codeChallenge` |

### 请求示例

```bash
curl.exe -sS -X POST "https://uacymconnect.bankofindia.bank.in/ciamregsrvc/upi/checkBindingKeyStatusUpi" \
  -H "Content-Type: application/json" -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJyZXN1bHQiOnRydWUsInJvbGVzIjpbIlZFUklGWV9NU1BfVVBJIl0sInRlbmFudElkIjoidWFjeW1jb25uZWN0IiwidXNlclR5cGUiOm51bGwsImV4cCI6MTc4OTM5MjU5MCwidXNlcklkIjoiNmFhN2VmMTI5OGNhODM0ZTNhOTczMTUzIiwiaWF0IjoxNzg5MzkwNjEwfQ.WH_rBzt5zRHQmaT9zfX9tUu5keOxp9Z1PN9L9EZUWiI" \
  -H "locLatitude: 28.61" -H "locLongitude: 77.20" \
  --data '{"codeVerifier":"dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk"}'
```

### 响应参数

| 参数 | 类型 | 说明 |
|---|---|---|
| success | boolean | 是否成功 |
| data | object | 绑定状态；成功时含 `simbindingstatus`、`token`、`sendOtpResponseDto` |
| timestamp | string | 服务端时间 |
| message | string | 提示信息，可空 |
| errorCode | string | 错误码，可空 |
| errorMessage | string | 错误信息，可空 |

### 响应示例

```json
{"success":false,"data":null,"timestamp":"14-Sep-2026 12:56:53","message":null,"errorCode":"REGNSRVC.VERIFICATION_PENDING","errorMessage":""}
```

### 错误码

| 返回 | 说明 |
|---|---|
| 403 `{"error":"Unable to parse token, token is malformed"}` | 未带/带错误 token |
| REGNSRVC.CODE_VERIFIER_FAILED | codeChallenge/codeVerifier 不配对 |
| REGNSRVC.VERIFICATION_PENDING | 挑战配对成功，等待绑定短信验证 |

---

## API-004 · 校验 UPI 绑定 OTP

- 描述：绑定短信验证通过后，校验服务端下发的手机 OTP（`sendOtpResponseDto`）；UPI SIM 绑定流程的最后一步
- 方法：`POST`
- 地址：`https://uacymconnect.bankofindia.bank.in/ciamregsrvc/upi/verifyUpiMobileOtp`
- 鉴权：`Bearer <API-002 的 token>`
- 加密：无
- 关键代码：`verifyUpiMobileOtpUsingPOST`

### 请求头

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| Authorization | string | 是 | `Bearer <API-002 返回的 token>` |
| Content-Type | string | 是 | 固定 `application/json` |
| locLatitude | string | 否 | 纬度 |
| locLongitude | string | 否 | 经度 |

### 请求体（application/json）

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| mobileOtp | string | 是 | 服务端下发的手机 OTP |

### 请求示例

```bash
curl.exe -sS -X POST "https://uacymconnect.bankofindia.bank.in/ciamregsrvc/upi/verifyUpiMobileOtp" \
  -H "Content-Type: application/json" -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJyZXN1bHQiOnRydWUsInJvbGVzIjpbIlZFUklGWV9NU1BfVVBJIl0sInRlbmFudElkIjoidWFjeW1jb25uZWN0IiwidXNlclR5cGUiOm51bGwsImV4cCI6MTc4OTM5MjU5MCwidXNlcklkIjoiNmFhN2VmMTI5OGNhODM0ZTNhOTczMTUzIiwiaWF0IjoxNzg5MzkwNjEwfQ.WH_rBzt5zRHQmaT9zfX9tUu5keOxp9Z1PN9L9EZUWiI" \
  -H "locLatitude: 28.61" -H "locLongitude: 77.20" \
  --data '{"mobileOtp":"123456"}'
```

### 响应参数

| 参数 | 类型 | 说明 |
|---|---|---|
| success | boolean | 是否成功 |
| data | object | 校验结果 |
| timestamp | string | 服务端时间 |
| message | string | 提示信息，可空 |
| errorCode | string | 错误码，可空 |
| errorMessage | string | 错误信息，可空 |

### 响应示例

```json
{"error":"auth: The two fields do not match"}
```

### 错误码

| 返回 | 说明 |
|---|---|
| `{"error":"Unable to parse token, token is malformed"}` | 未带 token |
| `{"error":"auth: The two fields do not match"}` | token 与绑定会话不匹配 |
| MFASRVC.INVALID_SMS_OTP / REGNSRVC.MOBILE_OTP_NOT_VERIFIED | OTP 错误 / 未验证 |
