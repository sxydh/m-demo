# Frida server 与 Zygisk/Vector 注入冲突导致 app 秒崩

> - 设备：Pixel 7 Pro（cheetah，Android 16，`BP2A.250605.031.A2`）
> - 环境：KernelSU-Next + SUSFS / Zygisk Next 1.3.4 / Vector 2.2（Zygisk 版 Xposed）/ phantom-frida 17.16.4
> - 适用：在"Frida + Xposed 共存"的 root 环境下，目标 app 一启动就 SIGSEGV 的问题

---

## 0. 结论

**app 冷启后 1 秒内 SIGSEGV 的充分条件是——`Frida server 正在运行` 且 `Zygisk/Vector 向该 app 注入`，两者同时成立。**

被注入进程的内存会因两者冲突而被写坏，进程随即崩溃。这与应用自身的反作弊、完整性校验无关，**换成任意 app 结果相同**。

---

## 1. 现象与崩溃签名

- 冷启后约 1 秒内死亡，反复冷启稳定复现。
- tombstone 签名（不同 app 完全一致）：

```
signal 11 (SIGSEGV), code 2 (SEGV_ACCERR), fault addr 0x...XXXX400
1 total frames
backtrace:
    #00 pc 0x...  /system/lib64/libstagefright.so
         或        /system/lib64/libmedia.so
Process uptime: 1s
```

解读：
- **只有 1 帧**，且落在**系统媒体库的只读代码页**——说明是"往只读页写"触发的 `SEGV_ACCERR`，不是正常的调用栈崩溃；
- fault 地址在同一 boot 内固定，跨 boot 换基址（ASLR）。

- logcat 时序显示崩溃**紧跟注入之后**（约 7ms）：

```
V Vector: Loading Vector/Xposed for <pkg> (UID: ...)
V VectorLegacyBridge: Loading legacy module ...
I [TrustMe] ... Conscrypt Hook 完成
F libc: Fatal signal 11 (SIGSEGV), ... fault addr 0x...
```

> 现场形态**像**"主动自毁跳板"，实际是注入层写坏内存后失控跳转的结果。

---

## 2. 对照实验（A/B/C/D）

自变量两个：**Frida server 是否在跑** × **目标 app 是否处于 Vector/Zygisk 模块作用域**（即是否会被注入）。

| | 无注入 | 有注入 |
|---|---|---|
| **无 Frida server** | A：存活 3/3 | B：存活 3/3 |
| **有 Frida server** | C：存活 3/3 | **D：秒崩 3/3** |

- 正序（A→B→C→D）与反序（D→C→B→A）**各在独立 boot 下**执行，结果一致；累计 3 个 boot 复现同一模式。
- 每格执行前均核验状态：`pidof .sysrender`（server 是否在跑）、`cli scope ls <模块>`（是否会被注入）、logcat 的 `Loading Vector/Xposed for <pkg>`（是否真的注入成功）。

---

## 3. 排他性验证（已排除的假设）

| 假设 | 验证方式 | 结果 |
|---|---|---|
| 特定 app 的反作弊/自毁 | 用一个与目标无关的自建测试 app 同测 | **同样秒崩，fault 地址一致 → 与 app 无关** |
| TrustMe 的 SSL hook 引起 | 换成不做 SSL hook 的无关模块注入 | **同样崩 → 与 TrustMe 无关** |
| 监听端口被占用 | 用 `nc` / toybox 监听同一端口 | 不崩 |
| Frida 协议探测该端口 | 用自建 TCP 记录器监听该端口 | 目标 app **从不发起连接** |
| abstract socket 名含 `zymbiote` | 用普通程序绑定同名 `@/sysrender-zymbiote-<hex>` | 不崩 |
| abstract socket 形状 | 用普通程序绑定 `@/<name>-<32hex>` | 不崩 |
| 可执行文件在 `/data/local/tmp` | 把普通程序拷到该目录运行并监听 | 不崩 |
| Frida agent 被注入目标进程 | 检查崩溃进程的内存映射 | **无 frida/gum/agent 映射** |

结论：只有"Frida server 在跑 + 目标进程被 Zygisk 注入"这一组合成立时才崩。

---

## 4. 机制（推断）

`frida-server` 与 Zygisk 都要操作 zygote / 新进程的内存。两者同时运行时，被 Zygisk 注入（加载 Vector 框架）的进程在**启动早期**内存被写坏，向只读代码页写入触发 `SEGV_ACCERR`，于是 tombstone 呈现"单帧落在系统媒体库"的假象。

旁证：
- `lico-n/ZygiskFrida`：明确建议**不要同时运行 frida-server**（其 ptrace 方式与 Zygisk 注入冲突），推荐改用 Zygisk 注入 gadget。
- `TheQmaks/phantom-frida`：README 声明不保证覆盖"应用特定 / 行为 / 时序"类问题。

---

## 5. 规避规则

1. **Frida server 在跑时，不要冷启处于 Vector/Zygisk 模块作用域内的 app**——这是唯一的触发条件。
2. 需要 hook 时按固定顺序：`杀 server → 启动 app → 启动 server（等 netstat 真正 LISTEN）→ attach`。
3. 需要 hook 启动期：改用 **frida-gadget / ZygiskFrida**（不占端口、不 ptrace、不依赖 server）。
4. 只做抓包（挂 TrustMe、不开 Frida）：完全安全，可随意冷启。
5. 遇到"启动 1 秒内 SIGSEGV、单帧落在系统库"的现场，**先确认 frida-server 是否在跑**。

---

## 6. 可复用的排查方法

1. **先定义变量，再做对照**：把可疑因素拆成互斥的二元自变量做受控实验（如本文 A/B/C/D），每格多次、每格前核验状态。
2. **用"无关样本"做排他**：换一个与被测对象无关的 app 重现，直接判断"是否与被测对象相关"。
3. **把现场读全**：`uptime`、帧数、fault 地址低位的规律、fault 落在"文件映射还是匿名页"（tombstone 的 memory map 会标出）。
4. **对齐时序**：将 logcat（注入日志）与 tombstone（时间戳）对齐，可得到"注入后 N ms 崩溃"。
5. **不要凭现象编因果**：没有对照就下"某应用自毁/某风控触发"的结论，很容易判断错误。
6. **跨 boot 复验**：ASLR / 内核 / SELinux 状态跨 boot 会变，独立 boot 复现可排除环境残留。

---

## 7. 附录：状态核验命令

```sh
pidof .sysrender                                   # Frida server 是否在跑
/data/adb/lspd/cli scope ls hk.kirk.trustme        # 哪些 app 会被注入
adb logcat | grep -a "Loading Vector/Xposed for"   # 本次启动是否真的被注入
adb shell ls -lt /data/tombstones/ | head          # 崩溃现场
```
