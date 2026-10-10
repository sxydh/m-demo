# 概述

Android root demo

## 手机当前状态

- **设备**：Google Pixel 7 Pro（`cheetah`，SoC `gs201`），Android 16，构建 `BP2A.250605.031.A2`，活动槽 `_b`，真实安全补丁 `2025-06-05`。
- **解锁/root**：BL 已解锁；KernelSU-Next 内置（GKI，非 LKM），`su -c id` → `uid=0`。
- **内核**：WildKernels 自编译 GKI `6.1.124-android14-2025-02-KernelSU-Next`（SUSFS v2.3.0）；`uname` 已由 SUSFS 伪装回原厂串。
- **启动状态伪装**：`ro.boot.verifiedbootstate=green`、`ro.boot.flash.locked=1`、`ro.boot.vbmeta.device_state=locked`。
- **管理器**：spoofed 随机包名 `wccygg.njrtqd.fbvelz`（KernelSU-Next v3.3.0 / ksud 3.3.0，uapi: 4）。
- **元模块**：NoMount v2.0.0（`/data/adb/modules/nomount`）。
- **已装模块**：`susfs4ksu` v2.3.0-R27、`zygisksu`（Zygisk Next）1.3.4 (746)、`playintegrityfix` v4.7-1-inject-s、`tricky_store`（TEESimulator-RS）v6.0.1-307、`TA_enhanced` v5.27.0、`hma_oss_zygisk` oss-164。
- **KSU 功能位**：`su_compat` / `kernel_umount` / `selinux_hide` / `avc_spoof` 均 `enabled`（值 1）。
- **伪装配置**：`/data/adb/susfs4ksu/config.sh` → `spoof_uname=2`、`hide_sus_mnts_for_all_or_non_su_procs=1`、`hide_loops=0`，kernel_version/build 为原厂串。
- **补丁日期**：`/data/adb/tricky_store/security_patch.txt` → `system=202506` / `boot=2025-06-05` / `vendor=2025-06-05`；TA_enhanced `auto_update=false`。
- **TEESimulator 作用目标**：`/data/adb/tricky_store/target.txt` 含 `com.google.android.gms` / `com.google.android.gsf` / `com.android.vending` 等。
- **其他**：`/data/local/tmp` 属主 `shell:shell`、权限 `771`；`adb_enabled=1`（USB 调试保留开启）。

## 注意

- 高危操作前，必须告知用户风险，必须经过用户授权后，才能进行下一步操作。
- 任何可能将手机变砖的操作，都必须有恢复方案，否则严禁执行。
- 每次重启手机后，必须在用户手动解锁屏幕之后，才能继续下一步操作。

## Frida 与 Zygisk/Vector 注入冲突（重要）

> 完整对照实验（A/B/C/D）与排他性验证见 `ShopeeDemo/FridaServer与Zygisk注入冲突.md`。本节为可直接照做的精简版。

- **秒崩条件**：隐藏版 frida 的 server（`.sysrender`）**正在运行** **且** 目标 app 处于 Vector/Zygisk 模块作用域（会被注入），两者同时成立 → 冷启约 1 秒内 SIGSEGV。缺任一条件都不崩。
- **反直觉点（必读，否则会误判）**：**"server 在跑"就足够，不需要你手动 attach**。`.sysrender` 带 **zygote symbiote（`zygmbiote`）**，server 一起就注入 zygote，之后每个新 fork 的 app 都被自动塞进 frida。所以"server 在跑" ≈ "frida 已进该 app"。别套用原版 `frida-server`"空闲时不碰其它进程"的直觉——隐藏版不是那样。
- **机理**：frida 与 Vector 两套 inline-hook 引擎在同一进程争用系统库代码页 → 往只读代码页写 → `SEGV_ACCERR`。**不是 app 反作弊/自毁**。
- **崩溃签名（已实测，与具体 app 无关）**：`Process uptime: 1s`、`signal 11 (SIGSEGV) code 2 (SEGV_ACCERR)`、**`1 total frames`**、`#00 ... /system/lib64/libmedia.so`（或 `libstagefright.so`）。实测 `com.shopee.ph` 与**自建、无任何反检测**的 `com.example.omnichat` 崩在**同一偏移 `libmedia.so+0x8f1bc`、同 BuildId**（同一 boot 内 fault 地址固定，跨 boot 随 ASLR 变）→ 这就是"与 app 无关"的硬证据。
- **确认是否真被注入**：logcat 有 `Loading Vector/Xposed for <pkg>`，紧跟（实测 ~37ms）即 SIGSEGV。
- 看到「冷启 1 秒内 SIGSEGV + 单帧落系统库」，先 `pidof .sysrender` 确认 server 是否在跑。

### 作用域由 `lspd/cli` 管理——**严禁手改 db**

作用域存在 `/data/adb/lspd/config/modules_config.db`，但它**由 LSPosed/Vector 框架托管、改动时会自动备份**（见旁边的 `*.bak_pre_*`）。**不要直接编辑这个 db**：会被守护进程覆盖、状态不一致、甚至搞坏 Vector。一律用 CLI（增删即时生效，对目标 app **冷启**后注入状态才更新）：

```sh
/data/adb/lspd/cli scope ls  hk.kirk.trustme                      # 列当前作用域
/data/adb/lspd/cli scope add hk.kirk.trustme com.example.app/0    # 加入（要 MITM 抓包时挂回）
/data/adb/lspd/cli scope rm  hk.kirk.trustme com.example.app/0    # 移除（要 frida spawn 时摘掉）
```

### 规避 / 共存

- **只抓包（挂 TrustMe、不开 Frida）**：完全安全，可随意冷启。
- **要 attach 已启动的 app**：固定顺序 杀 server → 启动 app → 起 server（等 `netstat` 真 LISTEN）→ attach。（先启动 app 时 server 是死的，启动期不会被 zygmbiote 注入；symbiote 只在 fork 时注入、不回灌已起来的进程，故晚 attach 安全。）
- **要 frida spawn / hook 启动早期**：先 `scope rm` 把目标**移出 Vector 作用域**（消掉第二套引擎）再 spawn；或改用 Zygisk 注入的 frida-gadget（如 ZygiskFrida，不占端口、不 ptrace、不依赖 server）。

## Frida：版本 + Java 桥（每次必踩，动手前先读）

- 只能用**隐藏版 frida `17.16.4`**（phantom-frida 的 `sysrender-server-17.16.4-android-arm64`，伪装进程名 `.sysrender`，监听 `127.0.0.1:31337`）。**不能用官方 `frida-server`**：不隐藏会被检测到。
- **PC 端版本（易混）**：`frida`（python 核心包）= **17.16.4**，必须与设备端严格一致（`frida --version` 显示 `17.16.4`）；`frida-tools` 是**独立版本线** = **14.10.4**（没有 frida-tools 17.x，别去找"17.16.4 的 frida-tools"）。

### Java 桥 —— Frida 17 的头号坑

Frida 17 把 `Java` / `ObjC` / `Swift` 桥**从运行时内核里移除了**，改成独立包（`frida-java-bridge` 等）。所以"有没有 Java 桥"取决于**你怎么注入脚本**：

- **frida / frida-trace CLI（含 `-l script.js`）**：桥**自动加载**，照常 `Java.perform(function(){ ... })`、`Java.use("...")`，与 frida 16 用法**完全一样**。← 交互/脚本 hook 走这条，不用管桥。
- **Python `session.create_script(裸JS)`**：`Java` 是 **`undefined`**！核心 API（`Interceptor` / `Module` / `Memory`）仍在，但 Java 层用不了。要用 Java 桥得：① 走 `frida-compile` 把 `frida-java-bridge` 打包进 agent，或 ② 改用会自动加桥的 frida-tools（CLI / `-l`）。
- **读 `static final` 常量 / 调静态方法前**：目标类可能还没跑 `<clinit>`，此时读字段拿到 `null`。**先调一次该类任意静态方法触发初始化，再读字段**。
- **连接与 attach（实测可用）**：
  ```sh
  # 设备端起隐藏版（见上方冲突一节的时序约束）
  su -c '/data/local/tmp/.sysrender -l 127.0.0.1:31337 -D'
  # PC 端
  adb forward tcp:31337 tcp:31337
  frida -H 127.0.0.1:31337 -p <pid> -l hook.js     # ← 按 PID 最稳
  ```
  注意 `-n 包名` 常**匹配不到**（frida 显示名是 App 的 label，如 `OmniChat` 而非 `com.example.omnichat`），用 `adb shell pidof <pkg>` 取 PID、`-p <pid>` attach。

## 重装作用域内 App 后抓包 "No Connection"（重启恢复）

- 卸载重装被 TrustMe 作用域覆盖的 App（如 `com.shopee.ph`）后，走 MITM 显示 **No Connection**，Reqable 只有 `CONNECT`、无解密 HTTPS。
- 原因：新进程**没被 Vector/TrustMe 注入**。判据：`su -c 'grep -a TrustMe /data/adb/lspd/log/verbose_*.log | tail'`，最后一条 `[TrustMe] 处理 App` 停在重装前。
- **不是** Zygisk denylist（新旧进程都在 `zygisk-core64 [..] is on the denylist`，但旧进程照样被处理）；也不是证书挂载。
- **修复：重启手机**（重启后需手动解锁）。凡「重装作用域内 App」后网络/抓包异常，先重启。

## adb 无线连接（免数据线）

- 已开启无线 adb，端口固定 `5555`。
- 连接：`adb connect 192.168.0.226:5555`（IP 以手机实际 Wi-Fi IP 为准）。
- 开机自启脚本：`/data/adb/service.d/adb_tcpip.sh`。
- 关闭：`su -c 'rm /data/adb/service.d/adb_tcpip.sh'`，再 `adb usb`。

## adb / su 执行规范

执行 adb 命令（尤其涉及 `su`）时，**强烈建议用 here-string + 管道，不要把命令写进 adb 的参数**。

原因：PowerShell 5.1 给 `adb.exe` 传参时会丢失内层双引号，`su -c "cmd1; cmd2"` 里的 `;`、`&&`、`|` 会被设备侧外层 shell 拆分，导致只有第一条命令经 `su` 提权、其余以 uid=2000 落在 `u:r:shell:s0`，读写 `/data/adb` 被拒；看起来像 `su` 时灵时不灵，实为传参问题。

here-string 走 stdin 原文送达设备 shell，不经过 PowerShell 引号解析，因此不受影响。

命令里含中文/emoji 等非 ASCII 字符时，必须先把管道编码改成 UTF-8，否则会被替换成 `?`。

## 环境

- 本机系统是 Windows11 ，大陆网络，有基于 Mihomo 的代理 `http://127.0.0.1:7890` 。
- 本机已经安装最新版本的 Android Studio
- 手机已通过**无线 adb**（固定 `192.168.0.226:5555`）连接，无需数据线（详见「adb 无线连接」）
- 手机已经开启 USB 调试模式，且已经解锁 OEM 。
