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

    private CtClass getCtClass(String className) {
        ClassPool classPool = ClassPool.getDefault();
        classPool.appendClassPath(new LoaderClassPath(Thread.currentThread().getContextClassLoader()));
        return classPool.getOrNull(className);
    }

    @Override
    public byte[] transform(ClassLoader loader,
                            String className,
                            Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain,
                            byte[] classfileBuffer) {
        try {
            className = className.replace("/", ".");
            String preparedStatementHandler = "org.apache.ibatis.executor.statement.PreparedStatementHandler";
            List<String> mysqls = Arrays.asList("com.mysql.jdbc.PreparedStatement", "com.mysql.cj.jdbc.ClientPreparedStatement");
            List<String> oracles = Arrays.asList("", "oracle.jdbc.driver.OraclePreparedStatement");
            if (preparedStatementHandler.equals(className)) {
                return preparedStatementHandler(getCtClass(className));
            } else if (mysqls.contains(className)) {
                return clientPreparedStatement(getCtClass(className));
            } else if (oracles.contains(className)) {
                return oraclePreparedStatement(getCtClass(className));
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    private byte[] preparedStatementHandler(CtClass ctClass) throws Exception {
        if (ctClass != null) {
            System.out.println(RED_BOLD + "agent-ibatis-sqllog [PreparedStatementHandler] is being loaded...");
            System.out.print(RESET);
            CtField ctField = CtField.make(" public static final java.lang.ThreadLocal MS_THREAD_LOCAL = new java.lang.ThreadLocal(); ", ctClass);
            ctClass.addField(ctField);
            for (CtConstructor ctConstructor : ctClass.getConstructors()) {
                String name = ctConstructor.getName();
                String targetName = "PreparedStatementHandler";
                if (name.equals(targetName)) {
                    ctConstructor.insertAfter("" +
                            "try {\n" +
                            "    MS_THREAD_LOCAL.set($2.getId());\n" +
                            "} catch (Throwable e) {\n" +
                            "    e.printStackTrace();\n" +
                            "};\n" +
                            "");
                    break;
                }
            }
            /* ctClass.writeFile("C:\\Users\\Administrator\\Desktop"); */
            System.out.println(RED_BOLD + "agent-ibatis-sqllog [PreparedStatementHandler] has been loaded.");
            System.out.print(RESET);
            return ctClass.toBytecode();
        }
        return null;
    }

    private void addFieldParamsMap(CtClass ctClass) throws Exception {
        CtField ctField = CtField.make(" private final java.util.Map paramsMap = new java.util.HashMap(); ", ctClass);
        ctClass.addField(ctField);
    }

    private void interceptMethodSetAny(CtMethod method) throws Exception {
        List<String> setMethods1 = Arrays.asList("setNull", "setBoolean", "setShort", "setInt", "setFloat", "setDouble", "setLong", "setBigDecimal");
        List<String> setMethods2 = Arrays.asList("setString", "setDate", "setTime", "setTimestamp", "setArray", "setObject");
        if (setMethods1.contains(method.getName())) {
            method.insertBefore("" +
                    "try {\n" +
                    "    paramsMap.put(Integer.valueOf($1), String.valueOf($2));\n" +
                    "} catch (Throwable e) {\n" +
                    "    e.printStackTrace();\n" +
                    "}\n" +
                    "");
        } else if (setMethods2.contains(method.getName())) {
            method.insertBefore("" +
                    "try {\n" +
                    "    paramsMap.put(Integer.valueOf($1), \"'\" + $2 + \"'\");\n" +
                    "} catch (Throwable e) {\n" +
                    "    e.printStackTrace();\n" +
                    "}\n" +
                    "");
        }
    }

    private byte[] clientPreparedStatement(CtClass ctClass) throws Exception {
        if (ctClass != null) {
            System.out.println(RED_BOLD + "agent-ibatis-sqllog [ClientPreparedStatement] is being loaded...");
            System.out.print(RESET);
            addFieldParamsMap(ctClass);
            for (CtMethod ctMethod : ctClass.getDeclaredMethods()) {
                interceptMethodSetAny(ctMethod);
                if ("executeInternal".equals(ctMethod.getName())) {
                    ctMethod.insertBefore("" +
                            "try {\n" +
                            "    String BLACK_BOLD = \"\033[1;30m\";\n" +
                            "    String RED_BOLD = \"\033[1;31m\";\n" +
                            "    String GREEN_BOLD = \"\033[1;32m\";\n" +
                            "    String YELLOW_BOLD = \"\033[1;33m\";\n" +
                            "    String BLUE_BOLD = \"\033[1;34m\";\n" +
                            "    String PURPLE_BOLD = \"\033[1;35m\";\n" +
                            "    String CYAN_BOLD = \"\033[1;36m\";\n" +
                            "    String WHITE_BOLD = \"\033[1;37m\";\n" +
                            "    String RESET = \"\033[0m\";\n" +
                            "    String id = null;\n" +
                            "    try {\n" +
                            "        java.lang.reflect.Field field = org.apache.ibatis.executor.statement.PreparedStatementHandler.class.getField(\"MS_THREAD_LOCAL\");\n" +
                            "        ThreadLocal threadLocal = (ThreadLocal) field.get(null);\n" +
                            "        id = (String) threadLocal.get();\n" +
                            "        threadLocal.remove();\n" +
                            "    } catch (Throwable e) {\n" +
                            "    }\n" +
                            "    if (id == null) {\n" +
                            "        id = \"\";\n" +
                            "    }\n" +
                            "    java.lang.String sql = getPreparedSql();\n" +
                            "    sql = sql.replaceAll(\"(?m)^[ \\t]*\\r?\\n\", \"\");\n" +
                            "    sql = sql.replace(\"?\", \"#1#\");\n" +
                            "    int i = 1;\n" +
                            "    while (sql.contains(\"#1#\")) {\n" +
                            "        sql = sql.replaceFirst(\"#1#\", String.valueOf(paramsMap.get(Integer.valueOf(i++))));\n" +
                            "    }\n" +
                            "    System.out.println(RESET);\n" +
                            "    System.out.println(RED_BOLD + id + \" ==> \");\n" +
                            "    System.out.println(CYAN_BOLD + sql);\n" +
                            "    System.out.println(RESET);\n" +
                            "} catch (Throwable e) {\n" +
                            "    e.printStackTrace();\n" +
                            "}\n" +
                            "");
                }
            }
            ctClass.writeFile("C:\\Users\\Administrator\\Desktop");
            System.out.println(RED_BOLD + "agent-ibatis-sqllog [ClientPreparedStatement] has been loaded.");
            System.out.print(RESET);
            return ctClass.toBytecode();
        }
        return null;
    }

    private byte[] oraclePreparedStatement(CtClass ctClass) throws Exception {
        if (ctClass != null) {
            System.out.println(RED_BOLD + "agent-ibatis-sqllog [OraclePreparedStatement] is being loaded...");
            System.out.print(RESET);
            addFieldParamsMap(ctClass);
            for (CtMethod ctMethod : ctClass.getDeclaredMethods()) {
                interceptMethodSetAny(ctMethod);
                if ("executeInternal".equals(ctMethod.getName())) {
                    ctMethod.insertBefore("" +
                            "try {\n" +
                            "    String BLACK_BOLD = \"\033[1;30m\";\n" +
                            "    String RED_BOLD = \"\033[1;31m\";\n" +
                            "    String GREEN_BOLD = \"\033[1;32m\";\n" +
                            "    String YELLOW_BOLD = \"\033[1;33m\";\n" +
                            "    String BLUE_BOLD = \"\033[1;34m\";\n" +
                            "    String PURPLE_BOLD = \"\033[1;35m\";\n" +
                            "    String CYAN_BOLD = \"\033[1;36m\";\n" +
                            "    String WHITE_BOLD = \"\033[1;37m\";\n" +
                            "    String RESET = \"\033[0m\";\n" +
                            "    String id = null;\n" +
                            "    try {\n" +
                            "        java.lang.reflect.Field field = org.apache.ibatis.executor.statement.PreparedStatementHandler.class.getField(\"MS_THREAD_LOCAL\");\n" +
                            "        ThreadLocal threadLocal = (ThreadLocal) field.get(null);\n" +
                            "        id = (String) threadLocal.get();\n" +
                            "        threadLocal.remove();\n" +
                            "    } catch (Throwable e) {\n" +
                            "    }\n" +
                            "    if (id == null) {\n" +
                            "        id = \"\";\n" +
                            "    }\n" +
                            "    java.lang.String sql = getOriginalSql();\n" +
                            "    sql = sql.replaceAll(\"(?m)^[ \\t]*\\r?\\n\", \"\");\n" +
                            "    sql = sql.replace(\"?\", \"#1#\");\n" +
                            "    int i = 1;\n" +
                            "    while (sql.contains(\"#1#\")) {\n" +
                            "        sql = sql.replaceFirst(\"#1#\", String.valueOf(paramsMap.get(Integer.valueOf(i++))));\n" +
                            "    }\n" +
                            "    System.out.println(RESET);\n" +
                            "    System.out.println(RED_BOLD + id + \" ==> \");\n" +
                            "    System.out.println(CYAN_BOLD + sql);\n" +
                            "    System.out.println(RESET);\n" +
                            "} catch (Throwable e) {\n" +
                            "    e.printStackTrace();\n" +
                            "}\n" +
                            "");
                }
            }
            ctClass.writeFile("C:\\Users\\Administrator\\Desktop");
            System.out.println(RED_BOLD + "agent-ibatis-sqllog [OraclePreparedStatement] has been loaded.");
            System.out.print(RESET);
            return ctClass.toBytecode();
        }
        return null;
    }

}