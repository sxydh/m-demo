# APK 分析思路（静态识别 + 内存提取）

> 目的：拿到一个正在运行的 App，快速回答四个问题——
> 1. **有没有加壳？**
> 2. **业务代码在哪（Java 侧 还是 RN/JS 侧）？**
> 3. **它用什么框架？**
> 4. **业务代码被加密了，怎么拿到明文？（内存 dump）**
>
> 以 `com.boi.ua.android` v3.9.0（Bank of India，RN 应用）为实例。
>
> ⚠️ 本文档同时记录了自己**踩过的坑和错误结论**，见第 10 节，务必先看。

---

## 0. 总体流程

```
定位前台应用
   └─ 拉取 base.apk + 各 split apk
        ├─ 看包结构（文件大小/命名）        → 判断框架
        ├─ 看 AndroidManifest（Application 入口）→ 判断是否壳
        ├─ 看 DEX（大小 + 是否含真实业务类）  → 判断是否壳
        ├─ 看 native 库 + bundle            → 判断业务在哪 & 是否加密
        └─ 业务被加密时：dump 进程内存 → 提取解密后的 Hermes 字节码 → 反编译
```

核心思想：**每一项都拿"正常应用应有的样子"去对照，找反常点**。
单个反常可能是巧合，多个反常叠加才能下结论。

---

## 1. 定位前台应用

```powershell
adb shell dumpsys activity activities | Select-String "topResumedActivity|mResumedActivity"
```

得到包名，例如 `com.boi.ua.android/com.boi.ua.MainActivity`。
然后用 `adb shell dumpsys package <包名>` 看 `versionName` / `versionCode` / `codePath`。

---

## 2. 拉包

```powershell
adb shell pm path com.boi.ua.android          # 列出 base.apk 和各 split apk
adb pull "<上面列出的路径>" "<本地临时目录>\base.apk"
```

> 路径里带 `~~XXX==` 时用双引号包住。建议放在
> `C:\Users\ADMINI~1\AppData\Local\Temp\opencode\<项目名>\`。

---

## 3. 看包结构（不解压，直接读 zip）

```powershell
Add-Type -AssemblyName System.IO.Compression.FileSystem
$z=[System.IO.Compression.ZipFile]::OpenRead("$tmp\base.apk")
$z.Entries | Sort-Object Length -Descending | Select-Object -First 60 FullName,Length,CompressedLength | Format-Table -AutoSize
$z.Dispose()
```

**看什么：**

| 现象 | 含义 |
|---|---|
| `assets/index.android.bundle` | → React Native（见第 6 节） |
| `assets/*.js` / `www/` | → Cordova / Ionic / WebView 套壳 |
| `libflutter.so` / `kernel_blob.bin` | → Flutter |
| `*.dex` 只有 1 个且很小（几百 KB）+ 大量加密资源 | → 疑似加壳 |
| `res/raw/` 下一堆无意义短名（`BX.wav`、`ky`、`lF`） | 资源名混淆（R8/AndResGuard），属正常加固手段 |
| 某大文件的 `Length == CompressedLength` | 该文件在 zip 里是 **store（未压缩）**，说明它本身已压缩/加密（如 Hermes 字节码、加密 bundle） |

---

## 4. 判断有没有加壳

**壳的本质**：真正的 DEX 被加密藏起来，App 里只放一个"壳 stub"，启动时由壳解密并动态加载。

### 4.1 看 AndroidManifest 的 Application 入口

```powershell
$aapt="C:\Users\Administrator\AppData\Local\Android\Sdk\build-tools\36.1.0\aapt2.exe"
& $aapt dump xmltree --file AndroidManifest.xml "$tmp\base.apk" > "$tmp\manifest.txt"
# 在 manifest.txt 里找 "E: application"，看其 android:name
```

| Application 类名 | 判断 |
|---|---|
| `com.xxx.MainApplication`（和包名一致） | 正常 |
| `com.stub.StubApp` | 360 加固 |
| `com.secneo.apkwrapper.*` | 梆梆加固 |
| `com.tencent.StubShell.TxAppEntry` | 腾讯乐固 |
| 其他陌生包名 | 高度可疑 |

### 4.2 看 DEX 的大小与内容

**壳的 DEX 特征：小、稀疏、没有业务类。**

```powershell
# 解出 dex
Add-Type -AssemblyName System.IO.Compression.FileSystem
$z=[System.IO.Compression.ZipFile]::OpenRead("$tmp\base.apk")
foreach($n in @("classes.dex","classes2.dex","classes3.dex","classes4.dex")){
  $e=$z.GetEntry($n); if(-not $e){continue}
  $fs=[IO.File]::Create("$tmp\$n"); $e.Open().CopyTo($fs); $fs.Close()
}
$z.Dispose()
```

统计是否含**真实业务类**（用 Latin1 把二进制当文本读，避免编码问题）：

```powershell
foreach($f in Get-ChildItem "$tmp\classes*.dex"){
  $txt=[Text.Encoding]::GetEncoding(28591).GetString([IO.File]::ReadAllBytes($f.FullName))
  $c=([regex]::Matches($txt,"Lcom/boi/ua/[^;]*;")).Count
  Write-Output "$($f.Name): $c 个业务类"
}
```

### 4.3 搜已知壳的指纹串

```powershell
$pats="libjiagu","secneo","apkwrapper","StubApp","com.qihoo.util","libmobisec",
      "DexHelper","protectClass","libnesec","DexProtector","libkwscmm","libsecexe",
      "libnqshield","baiduprotect","libtup","libshella"
foreach($f in Get-ChildItem "$tmp\classes*.dex"){
  $t=[Text.Encoding]::GetEncoding(28591).GetString([IO.File]::ReadAllBytes($f.FullName))
  foreach($p in $pats){ if($t.Contains($p)){ "$($f.Name): 命中 $p" } }
}
```

### 4.4 结论判定

| 条件 | 结论 |
|---|---|
| DEX 大且直接含真实业务类，Application 是自家类，无壳指纹 | **未加壳** |
| DEX 小/无业务类，Application 是陌生 stub，命中指纹 | **已加壳** |

> **注意区分"加壳"和"加固手段"**：
> - 加壳 = 代码被加密隐藏（要有脱壳）。
> - 加固/混淆 = 代码还是明文，只是做了 **R8/ProGuard 混淆、资源名混淆、native 库改名、root 检测、SSL Pinning**。
> 后者不等于加壳，仍可直接反编译。

---

## 5. 判断业务代码在哪：Java 侧 vs RN 侧

思路：**Java 侧数业务类，RN 侧看 bundle，谁体量大、谁包含流程，业务就在哪。**

### 5.1 Java 侧

列出该应用的自家类（过滤掉 R8 合成类）：

```powershell
$txt=[Text.Encoding]::GetEncoding(28591).GetString([IO.File]::ReadAllBytes("$tmp\classes.dex"))
[regex]::Matches($txt,"Lcom/boi/ua/[^;]*;") |
  ForEach-Object { $_.Value } | Sort-Object -Unique |
  Where-Object { $_ -notmatch '\$\$|ExternalSynthetic|Binding|Databinding' }
```

**判断**：如果列出来的清一色是
`MainApplication` / `MainActivity` / `Native*Module` / `*Spec` / 各种 `helper`，
→ Java 侧只是**宿主 + 桥接 + 安全能力**，不是业务主体。

（RN 桥接模块的典型命名：`NativeXxxModule` + `NativeXxxSpec`，`Spec` 是 RN 自动生成的接口。）

### 5.2 RN 侧

看 bundle 的**大小**和**熵**：

```powershell
$b=[IO.File]::ReadAllBytes("$tmp\index.android.bundle")
Write-Output "size=$($b.Length)"
$h=New-Object 'int[]' 256; foreach($x in $b){$h[$x]++}
$ent=0.0; foreach($c in $h){ if($c){$p=$c/$b.Length; $ent+=-$p*[Math]::Log($p,2)} }
Write-Output ("entropy={0:N4} bits/byte" -f $ent)
```

| 熵 | 含义 |
|---|---|
| 明显 < 8（常见 6~7.x） | 明文/普通压缩，能看到 `function`、字符串 |
| **= 8.0000（满熵）** | **已加密**（或高压缩），内容不可读 |

再搜明文验证：

```powershell
$s=[Text.Encoding]::ASCII.GetString($b)
Write-Output ($s.Contains("function"))   # 明文 JS 应为 True
# 密文应搜不到任何明文串（function / 业务串都应为 False）
# 注意：Hermes 字节码本身也不一定有 "Hermes" 字面串，别拿它当判据
```

**结论**：
- bundle 大 + 满熵 + 无明文 → 业务在 RN 侧，且 **bundle 被整体加密**，直接反编译 dex 看不到业务。
- 此时要分析业务，重点转向 **从内存取解密后的字节码**（见 5.3）。

> 加密 bundle 的表现：zip 里该文件 `Length == CompressedLength`（store），
> 且熵 = 8.0000。Hermes 字节码虽有二进制也不会有这种满熵+全无字符串的表现。

### 5.3 从内存提取解密后的 Hermes 字节码（实操）

`assets/index.android.bundle` 只是**密文**；App 启动时会把它解密成 Hermes 字节码并**常驻内存**。
因为没有落盘文件、也没走常规文件 mmap，**直接从进程内存 dump 是拿到业务代码最快、最稳的办法**
（纯 root 读 `/proc/<pid>/mem`，不注入、不 hook、不改进程，风险极低）。

> **前提**：App 必须正在运行、且已加载 JS（页面能显示）。
> ⚠️ **新地址/新 PID 每次都要重取**：重启后 PID 变、内存地址还会因 ASLR 变，**不要照抄本文档里的地址**。

**1）拿 PID**

```powershell
adb shell pidof com.boi.ua.android
```

**2）看内存映射，按大小列出可疑的匿名可读写区**

```powershell
@'
su -c "cat /proc/<pid>/maps"
'@ | adb shell > "$tmp\maps.txt"

# 把所有 [anon:...] 的 rw 区按大小从大到小排，挑最大的那几个
$o=@()
foreach($l in Get-Content "$tmp\maps.txt"){
  if($l -match '^([0-9a-f]+)-([0-9a-f]+)\s+rw-p\s+.*\[anon:(.+?)\]'){
    $s=[Convert]::ToInt64($matches[1],16); $e=[Convert]::ToInt64($matches[2],16)
    $o += [pscustomobject]@{Start=$matches[1]; KB=[math]::Round(($e-$s)/1KB); Anon=$matches[3]}
  }
}
$o | Sort-Object KB -Descending | Select-Object -First 10 | Format-Table -AutoSize
```

本例解密字节码在 `[anon:scudo:secondary]` 的 **~41.9MB** 那块。
这是**启发式**——别的 App 可能在别的匿名区（`hermes-*`、`libc_malloc`、dalvik 堆…）；实在找不到就全内存搜魔数（做法见第 5 步）。

**3）精确算出该区域的大小 / dd 的 skip 与 count（一定要算，别估）**

```powershell
$start='70c4861000'                                # ← 换成上一步选中的区域起始地址
$l=(Get-Content "$tmp\maps.txt" | Where-Object { $_ -match "^$start-" })
if($l -match '^([0-9a-f]+)-([0-9a-f]+)'){
  $s=[Convert]::ToInt64($matches[1],16); $e=[Convert]::ToInt64($matches[2],16); $sz=$e-$s
  "size=$sz  blocks=$([math]::Ceiling($sz/4096))  skip=$([int64]($s/4096))"
}
```

**4）dump 整块内存**（⚠️ 二进制必须用 `cmd /c` 重定向，**PS 的 `>` 会破坏二进制**；skip/count 用十进制字面量，别放 `$(( ))`）

```powershell
cmd /c 'adb exec-out su -c "dd if=/proc/<pid>/mem bs=4096 skip=<skip> count=<blocks> 2>/dev/null" > C:\...\bundle.mem'
```

**5）在 dump 里找 Hermes 魔数，定位字节码起点**
⚠️ **魔数别凭记忆**：严格常量是 4 字节 `0xC61FBC03`，但本文件开头 8 字节是 **`c6 1f bc 03 c1 03 19 1f`**（`hbc-file-parser` 把它显示为 Magic），也不是 `03 bc 1f c6`。所以按 8 字节搜最稳：

```powershell
$b=[IO.File]::ReadAllBytes("$tmp\bundle.mem")
$pat=[byte[]](0xC6,0x1F,0xBC,0x03,0xC1,0x03,0x19,0x1F)   # 前 8 字节
$pos=@()
for($i=0;$i -le $b.Length-$pat.Length;$i++){
  $ok=$true
  for($j=0;$j -lt $pat.Length;$j++){ if($b[$i+$j]-ne $pat[$j]){ $ok=$false; break } }
  if($ok){ $pos+=$i }
}
$pos | ForEach-Object { "0x{0:X}" -f $_ }
```

**6）按魔数偏移切出字节码，再用 `hbc-file-parser` 校验头**
（本例：字节码起点 = 魔数偏移 `0x320`；`FileLength` 字段在字节码起点 +32。这个 +32 是本文件/版本的实际布局，换个 App 要以解析器输出为准。）

```powershell
$b=[IO.File]::ReadAllBytes("$tmp\bundle.mem")
$off=0x320                                        # ← 第 5 步找到的魔数偏移
$fileLen=[BitConverter]::ToUInt32($b,$off+32)     # 本例 FileLength 在 +32
$hbc=New-Object byte[] $fileLen
[Array]::Copy($b,$off,$hbc,0,$fileLen)
[IO.File]::WriteAllBytes("$tmp\index.android.hbc",$hbc)

$env:PYTHONIOENCODING="utf-8"                     # 否则 Windows 控制台 GBK 会崩
hbc-file-parser "$tmp\index.android.hbc"          # 校验 Magic / Version / FileLength
```

**7）反编译成 JS**

```powershell
$env:PYTHONIOENCODING="utf-8"
pip install hermes-dec
hbc-decompiler "$tmp\index.android.hbc" "$tmp\decompiled\index.js"
```

本例产出：

| 产物 | 大小 | 说明 |
|---|---|---|
| `bundle.mem` | 41,922,560 | 内存区域原样 |
| `index.android.hbc` | 41,921,760 | 解密后 Hermes 字节码（version 96，函数 104,651） |
| `decompiled/index.js` | 204,507,285 | 反编译 JS（业务代码已恢复） |

---

## 6. 判断框架：为什么认定是 React Native

用"专属指纹链"，**任一单条可伪造，多条同时出现即定论**：

| # | 证据 | 指向 |
|---|---|---|
| 1 | `assets/index.android.bundle`（RN 固定产物名） | RN |
| 2 | DEX 里海量 `Lcom/facebook/react/...` | RN 引擎 Java 层 |
| 3 | 宿主类 `MainApplication$reactNativeHost$1`、`ReactNativeHost` | RN 独有宿主抽象 |
| 4 | `ReactInstanceManager` / `JSBundleLoader` / `ReactPackageTurboModuleManagerDelegate` | RN 独有 |
| 5 | 桥接模块 `NativeXxxSpec` 命名 | RN codegen 自动生成 |
| 6 | `libhermes.so`（默认 JS 引擎）、`libfbjni.so`（Facebook JNI） | RN |
| 7 | 改名的 so 里残留 `folly / yoga / fabric / jsireact / TurboModule / RCT` | RN 技术栈 |
| 8 | `libgesturehandler.so`、`react-native-screens`、`com.swmansion.*` | RN 生态库 |

其他框架的对应指纹：

| 框架 | 指纹 |
|---|---|
| React Native | `assets/index.android.bundle` + `com.facebook.react.*` + `libhermes.so/libfbjni.so` |
| Flutter | `libflutter.so` + `assets/flutter_assets/kernel_blob.bin` + `io.flutter.*` |
| Cordova/Ionic | `assets/www/index.html` + `cordova.js` + `org.apache.cordova.*` |
| 原生 Android | 只有 `com.<包名>.*` + `androidx.*`，无上述引擎包 |

---

## 7. native 库被改名时怎么识别

有些加固会把 `libXXX.so` 改成随机名（本例：`libb64.so`、`libf6f022.so`、`libod8558.so`…）。
**改名不等于加密**，可以看内部字符串反推它是什么：

```powershell
foreach($f in Get-ChildItem "$tmp\lib\*.so"){
  $t=[Text.Encoding]::GetEncoding(28591).GetString([IO.File]::ReadAllBytes($f.FullName))
  $m=[regex]::Matches($t,"Java_[A-Za-z0-9_]+") | ForEach-Object {$_.Value} | Sort-Object -Unique
  if($m){ "== $($f.Name)"; $m | Select-Object -First 6 }
}
```

- 有 `Java_com_xxx_...` → 这是某模块的 JNI 库，类名直接暴露身份。
- 再搜 `hermes / folly / jsc / reactnative / yoga / fabric / TurboModule / RCT` 等特征串归类。

本例结论：这些随机名 so 全是 App 自带的正经库，只是文件名被混淆。

---

## 8. 实例结论（com.boi.ua.android v3.9.0）

| 问题 | 结论 | 关键证据 |
|---|---|---|
| 加壳？ | **否** | 4 个明文 dex（~46MB）含真实类 `Lcom/boi/ua/MainApplication;`；Application 是 `com.boi.ua.MainApplication`；无任何壳指纹 |
| 业务在哪？ | **RN 侧** | `index.android.bundle` 41MB，熵 = 8.0000（**已加密**）；Java 侧 139 个类全是 `Native*Module`/`*Spec`/helper |
| 业务怎么拿到？ | **内存 dump + Hermes 反编译** | `[anon:scudo:secondary]` ~41.9MB 区域 → `index.android.hbc`(41,921,760) → `index.js`(204,507,285 B ≈ 195 MiB)，抽查命中 `getMobileNumberForMsp`×42、`saveSimBindingInfoUpi`×15 |
| 框架？ | **React Native** | bundle 名 + `com.facebook.react.*`(3000+ 处) + `ReactNativeHost` + `*Spec` + `libhermes.so`/`libfbjni.so` |
| 额外加固 | 有（但非加壳） | native 库随机改名；RootBeer root 检测；TrustKit SSL Pinning；AES/HMAC payload 加密 |

Java 侧职责（桥接 + 安全）：
- 宿主：`MainApplication` / `MainActivity`
- 支付/业务桥接：`NativeUpiModule`(UPI/NPCI)、`NativePayloadEncModule`、`NativeSendSMSModule`、`NativeSimDataModule`、`NativePhotoPickerModule`、`NativeBrightnessControllerModule`、`NativeCustomKeyboardModule`、`NativeKeyChainStorage`、`NativeUmangModule`、`NativeUtilModule`
- 安全：`TrustKitHelper`、RootBeer、`payloadenc/AesCbcEncryptionUtils`、`HmacUtils`

**已解决**：业务在加密 bundle 里，最终通过**内存 dump** 取到解密后的 Hermes 字节码并反编译成 JS（见 5.3）。
Java 侧未见明显解密类，说明解密发生在 native（改名 so）；本次没走静态逆 so，直接用内存 dump 绕过，更快。

**后续可选**：按模块拆分 JS + 生成 `functions.idx`；或逆向改名的 native so 还原离线解密算法（可复现、免 dump）。

---

## 9. 命令速查 / 环境

| 项 | 值 |
|---|---|
| SDK aapt2 | `C:\Users\Administrator\AppData\Local\Android\Sdk\build-tools\36.1.0\aapt2.exe` |
| 临时目录 | `C:\Users\ADMINI~1\AppData\Local\Temp\opencode\<项目名>\` |
| 二进制当文本读 | `[Text.Encoding]::GetEncoding(28591).GetString([IO.File]::ReadAllBytes($f))` |
| 二进制 dump | `cmd /c 'adb exec-out su -c "dd ..." > file'`（**不要**用 PowerShell 的 `>`） |
| Python 工具 | 先 `$env:PYTHONIOENCODING="utf-8"`，否则 GBK 报 `UnicodeEncodeError` |
| Hermes 工具 | `pip install hermes-dec` → `hbc-file-parser` / `hbc-decompiler` / `hbc-disassembler` |
| Hermes 魔数 | 常量 4 字节 `0xC61FBC03`；本例开头 8 字节 `c6 1f bc 03 c1 03 19 1f`。**别只搜 4 字节、别写反字节序**，以 `hbc-file-parser` 输出为准 |

> 设备侧 `su` 命令务必用 here-string + 管道，不要写进 adb 参数（见 AGENTS.md）。

---

## 10. 踩坑记录（血泪，务必先看）

> 这些是我本次**实际踩过、并且一度得出错误结论**的坑。

### 10.1 魔数长度/字节序搞错 → 错误结论"全内存没有"
- 我按 4 字节 `03 bc 1f c6` 全内存扫，没命中，就**断言"解密字节码不在内存、是逐段解密"**——**错的**。
- 实际字节码开头 8 字节是 `c6 1f bc 03 c1 03 19 1f`（`hbc-file-parser` 把这 8 字节显示为 Magic）；前 4 字节也不是我假设的 `03 bc 1f c6`。
  - 严格说 Hermes 魔数常量是 4 字节 `0xC61FBC03`；**长度和字节序都不要靠背**。
- **教训**：搜二进制 magic 前先确认它的**完整长度和字节序**，别凭记忆；最稳的是拿一个已知样本喂给 `hbc-file-parser` 看它打印的 Magic 字段。**搜不到时，先怀疑 pattern 错了，再怀疑"真没有"。**

### 10.2 dd 的 count 算少 → 字节码被截断
- 区域大小用错，`count` 少算 5 块（20,480 字节），切出的 hbc 头部字段全是天文数字（version 变 52 亿）。
- **教训**：skip/count 一律用 PowerShell 从 maps 行**精确计算**，禁止口算/估。

### 10.3 PowerShell `>` 破坏二进制
- `adb exec-out ... > file` 在 PS 5.1 里按文本转写，1MB 变 1.85MB、内容全废。
- **教训**：二进制一律 `cmd /c '... > file'` 或 `[IO.File]::WriteAllBytes`。

### 10.4 `$(( ))` 放进 `cmd /c '...'` 被 cmd 括号解析搞挂
- dd 静默失败、输出 0 字节（看着像"没权限/没内容"）。
- **教训**：给 `cmd /c` 的命令里别放 `$(())`，先算成**十进制字面量**。

### 10.5 Windows 控制台 GBK 让 Python 工具崩
- `hbc-file-parser` 打印非 GBK 字符直接 `UnicodeEncodeError`，容易被误读成"文件坏了"。
- **教训**：先 `$env:PYTHONIOENCODING="utf-8"`。且它常是**打印到一半才崩**，崩之前已经打印的头部信息是有效的。

### 10.6 全内存 `dd|grep` 又慢又像卡死
- dalvik 堆单区 512MB，`dd|grep` 扫全量 80+ 秒、CPU 拉满、无输出，主观上就是"卡死了"。
- **教训**：先看 maps **定位可疑大区**再定点 dump，别一上来全量扫；真要全量，先跟用户说明耗时。

### 10.7 把间接证据当结论（假命中）
- 在 dalvik 堆里搜到 `uacymconnect.bankofindia.bank.in`，一度以为找到 bundle；其实是 **TrustKit 的 SSL Pinning 配置**（附近还有 `Invalid expiration date in pin-set`、`SHA-256`、`digest`）。
- **教训**：单条字符串命中 ≠ 定位到目标；要看**上下文**（邻近常量、函数名、所在映射区类型）再判定。真正的 bundle 在 `[anon:scudo:secondary]`。

### 10.8 PowerShell 小坑
- `Select-String` 命中多行时 `$matches` 可能来自别的行导致解析失败 → 用 `Where-Object { $_ -match '起始地址' }` 精确定位。
- `foreach(...){...} | Sort-Object` 会报"不能使用空管道元素" → 先收集进数组再排序。
- `Get-Content | Select-String` 返回的是 MatchInfo，`.Line` 取内容；多命中要自己挑。

### 10.9 方法论层面的教训
- **不要对没验证过的结论下断言**（尤其"全内存没有"这种强否定）。先自证方法有效（造一个已知样本验证 grep/pattern 能命中），再下结论。
- 慢操作要**主动分段、报进度**，别让用户以为死机。
- 一条链路上多个自动判断，任何一步的 pattern/偏移错了都会导向错误结论——**关键常量（magic、字段偏移、区域边界）必须用工具校验，不靠记忆。**
