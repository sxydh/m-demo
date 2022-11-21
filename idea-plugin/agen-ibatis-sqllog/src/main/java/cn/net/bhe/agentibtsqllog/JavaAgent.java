package cn.net.bhe.agentibtsqllog;

import java.lang.instrument.Instrumentation;

/**
 * @author Administrator
 */
public class JavaAgent {

    public static void premain(String agentArgs, Instrumentation inst) {
        inst.addTransformer(new LogTransformer());
    }

}