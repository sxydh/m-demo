package cn.net.bhe.mybatisplugindemo.plugin;

import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;

import java.sql.PreparedStatement;

@Intercepts({
        /* 拦截的方法签名 */
        @Signature(
                /* 拦截的方法所在的类 */
                type = ParameterHandler.class,
                /* 拦截的方法名称 */
                method = "setParameters",
                /* 拦截的方法入参 */
                args = {PreparedStatement.class})
})
public class ParameterHandlerPlugin implements Interceptor {

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        return invocation.proceed();
    }

}
