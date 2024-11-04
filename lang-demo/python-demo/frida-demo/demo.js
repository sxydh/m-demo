setImmediate(function () {
    Java.perform(function () {
        var targetClass = "java.lang.String";
        var methodName = "toString";
        Java.use(targetClass)[methodName].implementation = function () {
            // 注意高频调用可能会导致奔溃
            console.log(`toString: ${this.toString()}`);
            return this.toString();
        };
        console.log("ready to go");
    });
});