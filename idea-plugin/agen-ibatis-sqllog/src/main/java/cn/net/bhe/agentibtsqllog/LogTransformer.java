package cn.net.bhe.agentibtsqllog;

import javassist.*;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.List;

/**
 * @author Administrator
 */
public class LogTransformer implements ClassFileTransformer {

    /**
     * 颜色
     */
    static final String RED_BOLD = "\033[1;31m";
    static final String RESET = "\033[0m";

    @Override
    public byte[] transform(ClassLoader loader,
                            String className,
                            Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain,
                            byte[] classfileBuffer) {
        try {
            className = className.replace("/", ".");
            String preparedStatementHandler = "org.apache.ibatis.executor.statement.PreparedStatementHandler";
            String preparedStatementProxyImpl = "com.alibaba.druid.proxy.jdbc.PreparedStatementProxyImpl";
            if (className.equals(preparedStatementHandler)) {
                return preparedStatementHandler(getCtClass(className));
            } else if (className.equals(preparedStatementProxyImpl)) {
                return preparedStatementProxyImpl(getCtClass(className));
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    private CtClass getCtClass(String className) {
        ClassPool classPool = ClassPool.getDefault();
        classPool.appendClassPath(new LoaderClassPath(Thread.currentThread().getContextClassLoader()));
        return classPool.getOrNull(className);
    }

    private byte[] preparedStatementHandler(CtClass ctClass) throws Exception {
        if (ctClass != null) {
            System.out.println(RED_BOLD + "agent-ibatis-sqllog [PreparedStatementHandler] is being loaded...");
            System.out.print(RESET);
            CtField ctField = new CtField(getCtClass("java.lang.ThreadLocal"), "MS_THREAD_LOCAL", ctClass);
            ctField.setModifiers(Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL);
            ctClass.addField(ctField, CtField.Initializer.byNew(getCtClass("java.lang.ThreadLocal")));
            for (CtConstructor ctConstructor : ctClass.getConstructors()) {
                String name = ctConstructor.getName();
                String targetName = "PreparedStatementHandler";
                if (name.equals(targetName)) {
                    ctConstructor.insertAfter("" +
                            "        try {\n" +
                            "            MS_THREAD_LOCAL.set($2.getId());\n" +
                            "        } catch (Throwable e) {\n" +
                            "            e.printStackTrace();\n" +
                            "        };\n" +
                            "");
                    break;
                }
            }
            ctClass.writeFile("C:\\Users\\Administrator\\Desktop");
            System.out.println(RED_BOLD + "agent-ibatis-sqllog [PreparedStatementHandler] has been loaded.");
            System.out.print(RESET);
            return ctClass.toBytecode();
        }
        return null;
    }

    private byte[] preparedStatementProxyImpl(CtClass ctClass) throws Exception {
        if (ctClass != null) {
            System.out.println(RED_BOLD + "agent-ibatis-sqllog [PreparedStatementProxyImpl] is being loaded...");
            System.out.print(RESET);
            for (CtMethod ctMethod : ctClass.getDeclaredMethods()) {
                String ctMethodName = ctMethod.getName();
                List<String> targetMethodNames = Arrays.asList("execute", "executeQuery", "executeUpdate");
                if (targetMethodNames.contains(ctMethodName)) {
                    ctMethod.insertBefore("" +
                            "        try {\n" +
                            "            String BLACK_BOLD = \"\033[1;30m\";\n" +
                            "            String RED_BOLD = \"\033[1;31m\";\n" +
                            "            String GREEN_BOLD = \"\033[1;32m\";\n" +
                            "            String YELLOW_BOLD = \"\033[1;33m\";\n" +
                            "            String BLUE_BOLD = \"\033[1;34m\";\n" +
                            "            String PURPLE_BOLD = \"\033[1;35m\";\n" +
                            "            String CYAN_BOLD = \"\033[1;36m\";\n" +
                            "            String WHITE_BOLD = \"\033[1;37m\";\n" +
                            "            String RESET = \"\033[0m\";\n" +
                            "            String id = null;\n" +
                            "            try {\n" +
                            "                java.lang.reflect.Field field = org.apache.ibatis.executor.statement.PreparedStatementHandler.class.getField(\"MS_THREAD_LOCAL\");\n" +
                            "                ThreadLocal threadLocal = (ThreadLocal) field.get(null);\n" +
                            "                id = (String) threadLocal.get();\n" +
                            "                threadLocal.remove();\n" +
                            "            } catch (Throwable e) {\n" +
                            "            }" +
                            "            String sql = getSql();\n" +
                            "            java.util.Map jdbcParameterMap = getParameters();\n" +
                            "            if (jdbcParameterMap.size() > 0) {\n" +
                            "                Object[] paramObjs = jdbcParameterMap.values().toArray();\n" +
                            "                int i = 0;\n" +
                            "                while (sql.contains(\"?\")) {\n" +
                            "                    com.alibaba.druid.proxy.jdbc.JdbcParameter jdbcParameter = (com.alibaba.druid.proxy.jdbc.JdbcParameter) paramObjs[i++];\n" +
                            "                    Object value = jdbcParameter.getValue();\n" +
                            "                    value = \"'\" + value + \"'\";\n" +
                            "                    sql = sql.replaceFirst(\"\\\\?\", String.valueOf(value));\n" +
                            "                }\n" +
                            "            }\n" +
                            "            System.out.println(RESET);\n" +
                            "            System.out.println(RED_BOLD + id + \" ==> \");\n" +
                            "            System.out.println(CYAN_BOLD + sql);\n" +
                            "            System.out.println(RESET);\n" +
                            "        } catch (Throwable e) {\n" +
                            "            e.printStackTrace();\n" +
                            "        }\n" +
                            "");
                    break;
                }
            }
            ctClass.writeFile("C:\\Users\\Administrator\\Desktop");
            System.out.println(RED_BOLD + "agent-ibatis-sqllog [PreparedStatementProxyImpl] has been loaded.");
            System.out.print(RESET);
            return ctClass.toBytecode();
        }
        return null;
    }

}