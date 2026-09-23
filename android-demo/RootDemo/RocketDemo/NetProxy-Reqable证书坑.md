# NetProxy + Reqable 抓包：Go 程序不信任系统证书的坑

> 记录时间：2026-09-23
> 机型：Pixel 7 Pro（cheetah / gs201），Android 16，KernelSU-Next + SUSFS

## 1. 现象

- NetProxy 出站模式 `global`，当前节点 `default/reqable`（你 PC 上的 Reqable，`192.168.0.187:9000`）。
- 节点测速/延迟测试**超时/失败**（clash API 返回 `503 An error occurred in the delay test`）。
- 具体表现：
  - 普通 HTTP 能抓；HTTPS 抓不了。
  - 国内 App（如他趣 `com.xingjiabi.shengsheng`，域名 `*.jiaoliuqu.com`）**能**被 Reqable 解密抓包。
  - 境外 App（如 Rocket / DBBL，`com.dbbl.mbs.apps.main`，域名 `*.dutchbanglabank.com`）**一条记录都没有**。
- 时好时坏：这台机器上“昨天还能抓，今天突然不行”。

## 2. 根因

三个事实叠加：

1. **Reqable 开了全量 MITM**：SSL Proxying = `include-all`（规则 `*`），所有 HTTPS 都被解密，包括 NetProxy 内核 sing-box 自己发出的 DNS-over-HTTPS。
2. **NetProxy 的代理内核 sing-box 是 Go 原生程序**，它的系统信任库读取的是 **`/system/etc/security/cacerts`**（可用 `grep -a "security/cacerts" sing-box` 验证），而不是 Android 14+ 之后 App 用的 conscrypt APEX 目录。
3. **MoveCertificate 模块是 `compatible` 模式**，在 Android 14+ 上只把用户证书挂进 `/apex/com.android.conscrypt/cacerts`（给 Java/Android App 用），**从不写 `/system/etc/security/cacerts`**。

于是：

- Reqable 解密 DoH（`dns.google` / `cloudflare-dns.com`），用自己签的证书；
- sing-box(Go) 在 `/system/etc/security/cacerts` 里找不到 Reqable 的 CA → TLS 校验失败；
- sing-box 的 DoH 全挂 → **境外域名无法解析** → 境外 App 连连接都建不起来 → Reqable 里自然没有记录；
- 国内域名走的是直连 DNS（dns-direct），不受影响 → 国内 App 照抓。

一句话：**不是“能抓但解不开”，而是境外流量根本没到 Reqable。**

## 3. 关键证据

```text
# sing-box 日志：包进了内核，但没有到 reqable 的出站
router: found package name: com.dbbl.mbs.apps.main

# Reqable 里搜域名：国内有，境外没有
keyword "jiaoliuqu"        -> 29 条（他趣）
keyword "dutchbanglabank"  -> 0 条（Rocket）

# Reqable 里一波来自手机(192.168.0.226)的 DoH CONNECT 全都是“无响应”状态
CONNECT 8.8.8.8:443   (Go-http-client) -> 无 response
CONNECT 1.1.1.1:443   (Go-http-client) -> 无 response

# 证书确实只在 APEX，不在 /system
ls /apex/com.android.conscrypt/cacerts | grep 6ebe5489   -> 有
ls /system/etc/security/cacerts          | grep 6ebe5489   -> 无
```

修复后同一批测试通过：

```text
https://www.gstatic.com/generate_204              -> {"delay":632}
https://www.google.com/generate_204               -> {"delay":626}
https://rocket-ekycservice.dutchbanglabank.com/   -> {"delay":1025}
```

## 4. 修复方案

把用户证书（含 Reqable 的 CA）也挂进 **`/system/etc/security/cacerts`**，让 Go 程序能读到。

新增开机脚本：`/data/adb/post-fs-data.d/zz-syscacerts.sh`

```sh
#!/system/bin/sh
# Expose user certificates to /system/etc/security/cacerts for native/Go clients (sing-box etc.)
D=/mnt/syscacerts
mkdir -p "$D"
mount -t tmpfs tmpfs "$D" 2>/dev/null
cp -f /system/etc/security/cacerts/* "$D"/ 2>/dev/null
cp -f /data/misc/user/0/cacerts-added/*.0 "$D"/ 2>/dev/null
chown root:root "$D"/* 2>/dev/null
chmod 644 "$D"/* 2>/dev/null
chcon u:object_r:system_security_cacerts_file:s0 "$D"/* 2>/dev/null
mount -o bind "$D" /system/etc/security/cacerts 2>/dev/null
```

```sh
chmod 755 /data/adb/post-fs-data.d/zz-syscacerts.sh
```

> 可随时手动执行一遍生效（无需重启）：
> `su -c 'sh /data/adb/post-fs-data.d/zz-syscacerts.sh'`，然后
> `su -c '/data/adb/modules/netproxy/bin/netproxyctl service restart'`

## 5. 验证

```sh
# 1) 证书是否可见
su -c 'ls /system/etc/security/cacerts/ | grep -E "6ebe5489|269953fb"'

# 2) sing-box 命名空间内也可见
su -c 'SP=$(pidof sing-box); nsenter --mount=/proc/$SP/ns/mnt -- ls /system/etc/security/cacerts/'

# 3) 用 clash API 触发节点延迟测试（注意代理名要 URL 编码，含中文与斜杠）
#    本地配置/reqable = %E6%9C%AC%E5%9C%B0%E9%85%8D%E7%BD%AE%2Freqable
#    GET /proxies/<name>/delay?timeout=6000&url=https%3A%2F%2Fwww.google.com%2Fgenerate_204
```

期望：HTTPS 测速返回 `{"delay":...}`（不再是 503），且境外域名可达。

## 6. 为什么“昨天可以，今天不行”

根因是个**潜伏 bug**，只在特定状态组合下才暴露（Reqable MITM + Go 信任库缺证书）。之前能抓多半是下面两种偶发状态之一撑着：

1. **临时挂载/残留**：这台机器有不少手工折腾痕迹（`/data/local/tmp/reqable-root.crt`、MoveCertificate 相关脚本等）。如果之前有人手工把证书挂到过 `/system`，那次挂载是 tmpfs，**一重启就没了**；而 MoveCertificate 的 compatible 模式只挂 APEX、不会重建 `/system`，所以重启后 bug 暴露。
2. **DNS 缓存**：sing-box 的 DNS 是 `optimistic` + `cache.db`。境外域名一旦被缓存过，DoH 坏了也能靠缓存顶一阵；缓存重建（本次是重启后 22:20）后必须现解析，就立刻失败。

本方案把它变成开机固定挂载，不再依赖偶发状态。

## 7. 备注

- `MoveCertificate` 的 `mode.conf` 保持 `compatible` 即可（App 侧走 APEX）；本次曾试过改 `builtin`，在 KSU-Next + SUSFS 环境下模块的 `system/`、`apex/` 目录**根本没被挂载**，反而把 App 的证书也弄丢，已回滚（备份 `mode.conf.bak.20260923`）。
- 另有一个替代修法：在 Reqable 的 SSL Proxying 里把 DoH 域名/IP（`dns.google`、`cloudflare-dns.com`、`doh.pub`、`dns.alidns.com`、`8.8.8.8`、`1.1.1.1`）设为 bypass（不 MITM），sing-box 就能拿到真实证书。也可两者结合。
- 注意 `type: http` 的出站 `udp: false`，QUIC/UDP 流量无法经它转发；若某 App 走 HTTP/3 且不回落 TCP，仍可能抓不到。
