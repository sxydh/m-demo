# BOI Mobile 接口文档

`com.boi.ua.android` v3.9.0

## 001 · VMN 号池查询
匿名可访问，明文返回。

关键代码：`VmnApi.getMobileNumberForMspUsingGET`

```bash
curl.exe -sS "https://uacymconnect.bankofindia.bank.in/ciamregsrvc/vmn/getMobileNumberForMsp?locLatitude=28.61&locLongitude=77.20"
```

```json
{"success":true,"data":{"vmn":"+917291022020, +917290865959","vmnSecondary":"+918796449647, +919217665879","keyword":"BOIUAP"},"timestamp":"14-Sep-2026 09:40:13","message":null,"errorCode":null,"errorMessage":null}
```
