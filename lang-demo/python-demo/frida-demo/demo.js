Java.perform(function () {
    let targetClass = "java.lang.String";
    let methodName = "toString";
    Java.use(targetClass)[methodName].implementation = function () {
        // 注意高频调用可能会导致奔溃
        let str = this.toString();
        console.log(`toString: ${str}`);
        return str;
    };
    console.log("ready to go");
});