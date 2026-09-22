// dump_dex.js
// 参考 tig3rhu《他趣APP脱壳》的 dump_dex.js（原版按 Android 9 的 DexFile 布局写死偏移），
// 此处适配 Android 16 / arm64：
//   - hook libart.so 的 ClassLinker::DefineClass（符号含 ClassLinker/DefineClass/Thread/DexFile）
//   - 不写死 DexFile 布局，改为在对象头若干指针里自动定位指向 "dex\n" 的 begin_
//   - 大小优先取 dex 头的 file_size（偏移 0x20），回退取 DexFile 内的 size_ 字段
//   - 用 libc open/write 分块流式写盘，避免大 dex（百 MB 级）在 JS 侧爆内存
(function () {
    var dumped = {};
    var count = 0;
    var outDir = null;

    // Frida 17：Module 静态查找方法已移除
    function libcExport(name) {
        return Process.getModuleByName("libc.so").getExportByName(name);
    }

    function log(msg) {
        console.log(msg);
        try { send({ jsname: "dump_dex", type: "log", data: "" + msg }); } catch (e) {}
    }

    function get_self_process_name() {
        try {
            var openPtr = libcExport("open");
            var open = new NativeFunction(openPtr, "int", ["pointer", "int", "int"]);
            var readPtr = libcExport("read");
            var read = new NativeFunction(readPtr, "int", ["int", "pointer", "int"]);
            var closePtr = libcExport("close");
            var close = new NativeFunction(closePtr, "int", ["int"]);
            var fd = open(Memory.allocUtf8String("/proc/self/cmdline"), 0, 0);
            if (fd == -1) return null;
            var buf = Memory.alloc(0x1000);
            var n = read(fd, buf, 0x1000);
            close(fd);
            if (n <= 0) return null;
            return buf.readCString();
        } catch (e) {
            log("[!] cmdline read failed: " + e);
            return null;
        }
    }

    function ensureDir(path) {
        var opendir = new NativeFunction(libcExport("opendir"), "pointer", ["pointer"]);
        var closedir = new NativeFunction(libcExport("closedir"), "int", ["pointer"]);
        var mkdir = new NativeFunction(libcExport("mkdir"), "int", ["pointer", "int"]);
        var p = Memory.allocUtf8String(path);
        var d = opendir(p);
        if (!d.isNull()) { closedir(d); return; }
        mkdir(p, 0x1FF); // 0777
    }

    function writeAll(path, base, size) {
        var open = new NativeFunction(libcExport("open"), "int", ["pointer", "int", "int"]);
        var write = new NativeFunction(libcExport("write"), "int", ["int", "pointer", "int"]);
        var close = new NativeFunction(libcExport("close"), "int", ["int"]);
        var O_WRONLY = 1, O_CREAT = 64, O_TRUNC = 512;
        var fd = open(Memory.allocUtf8String(path), O_WRONLY | O_CREAT | O_TRUNC, 0x1A4); // 0644
        if (fd < 0) { log("[!] open failed: " + path); return false; }
        var chunk = 1 << 20;
        var off = 0;
        while (off < size) {
            var n = Math.min(chunk, size - off);
            var w = write(fd, base.add(off), n);
            if (w <= 0) break;
            off += w;
        }
        close(fd);
        return off === size;
    }

    // 在 DexFile 对象头里找 begin_：一个指向 "dex\n" 的指针
    function locateDex(objPtr) {
        for (var off = 0; off < 8 * Process.pointerSize; off += Process.pointerSize) {
            var p;
            try { p = objPtr.add(off).readPointer(); } catch (e) { continue; }
            if (p.isNull()) continue;
            try { if (p.readCString(4) !== "dex\n") continue; } catch (e) { continue; }
            var hdrSize = 0, objSize = 0;
            try { hdrSize = p.add(0x20).readU32(); } catch (e) {}
            try { objSize = objPtr.add(off + Process.pointerSize).readU64().toNumber(); } catch (e) {}
            var size = (hdrSize > 0 ? hdrSize : objSize);
            if (size <= 0 || size > 0x20000000) continue;
            return { base: p, size: size, off: off, hdrSize: hdrSize, objSize: objSize };
        }
        return null;
    }

    function main() {
        var pkg = get_self_process_name();
        if (!pkg) { log("[!] cannot determine process name"); return; }
        outDir = "/sdcard/Android/data/" + pkg + "/files/tq_dump";
        ensureDir(outDir);

        var libart = Process.findModuleByName("libart.so");
        if (libart === null) { log("[!] libart.so not found"); return; }

        var addr = null, symName = null;
        var syms = libart.enumerateSymbols();
        for (var i = 0; i < syms.length; i++) {
            var nm = syms[i].name;
            if (nm.indexOf("ClassLinker") >= 0 && nm.indexOf("DefineClass") >= 0 &&
                nm.indexOf("Thread") >= 0 && nm.indexOf("DexFile") >= 0) {
                addr = syms[i].address;
                symName = nm;
            }
        }
        if (addr === null) { log("[!] DefineClass symbol not found"); return; }
        log("[DefineClass] " + symName);
        log("[DefineClass] addr=" + addr + " outDir=" + outDir);

        Interceptor.attach(addr, {
            onEnter: function (args) {
                var found = null;
                for (var a = 3; a <= 6 && found === null; a++) {
                    if (args[a].isNull()) continue;
                    try { found = locateDex(args[a]); } catch (e) {}
                }
                if (found === null) return;
                var key = found.base.toString();
                if (dumped[key]) return;
                dumped[key] = true;
                count++;
                var name = (count < 10 ? "0" : "") + count + "_" + key + ".dex";
                var path = outDir + "/" + name;
                var ok = writeAll(path, found.base, found.size);
                log("[dump] #" + count + " base=" + key + " size=" + found.size +
                    " hdr=" + found.hdrSize + " obj=" + found.objSize +
                    " -> " + name + (ok ? " OK" : " PARTIAL"));
            },
            onLeave: function (retval) {}
        });
        log("[+] dump_dex hook installed");
    }

    setImmediate(main);
})();
