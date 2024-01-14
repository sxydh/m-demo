package cn.net.bhe.jdkdemo;

import cn.net.bhe.mutil.As;
import cn.net.bhe.mutil.CpiUtils;
import cn.net.bhe.mutil.FlUtils;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

public class URLClassLoaderDemo {

    public static void main(String[] args) throws Exception {
        String root = FlUtils.getRootTmp() + File.separatorChar + "URLClassLoaderDemo";
        String outputDir = root + File.separatorChar + "output" + File.separator;
        String packageName = "compiledemo";
        String inputDir = root + File.separatorChar + "input" + File.separator + packageName;
        As.isTrue(FlUtils.mkdir(inputDir));
        FlUtils.write(String.format("package %s; public class Hello {}", packageName), inputDir + File.separatorChar + "Hello.java", false);
        FlUtils.write(String.format("package %s; public class World {}", packageName), inputDir + File.separatorChar + "World.java", false);
        boolean compile = CpiUtils.compile(inputDir, outputDir);
        As.isTrue(compile);

        // URL 构造函数 URL(spec) 中的参数 spec 分隔符必须是 /
        URL[] urls = new URL[]{new File(outputDir).toURI().toURL()};
        try (URLClassLoader urlClassLoader = new URLClassLoader(urls)) {
            Class<?> clazz = urlClassLoader.loadClass(String.format("%s.Hello", packageName));
            System.out.println(clazz);
        }
    }

}
