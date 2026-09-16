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

## adb / su 执行规范

执行 adb 命令（尤其涉及 `su`）时，**强烈建议用 here-string + 管道，不要把命令写进 adb 的参数**。

原因：PowerShell 5.1 给 `adb.exe` 传参时会丢失内层双引号，`su -c "cmd1; cmd2"` 里的 `;`、`&&`、`|` 会被设备侧外层 shell 拆分，导致只有第一条命令经 `su` 提权、其余以 uid=2000 落在 `u:r:shell:s0`，读写 `/data/adb` 被拒；看起来像 `su` 时灵时不灵，实为传参问题。

here-string 走 stdin 原文送达设备 shell，不经过 PowerShell 引号解析，因此不受影响。

命令里含中文/emoji 等非 ASCII 字符时，必须先把管道编码改成 UTF-8，否则会被替换成 `?`。

## 环境

- 本机系统是 Windows11 ，大陆网络，有基于 Mihomo 的代理 `http://127.0.0.1:7890` 。
- 本机已经安装最新版本的 Android Studio
- 本机已经通过 USB 数据线连接手机
- 手机已经开启 USB 调试模式，且已经解锁 OEM 。
