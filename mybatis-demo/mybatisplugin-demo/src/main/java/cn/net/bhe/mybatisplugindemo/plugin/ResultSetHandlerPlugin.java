package cn.net.bhe.mybatisplugindemo.plugin;

import org.apache.ibatis.executor.resultset.ResultSetHandler;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;

import java.sql.Statement;

@Intercepts({
        /* 拦截的方法签名 */
        @Signature(
                /* 拦截的方法所在的类 */
                type = ResultSetHandler.class,
                /* 拦截的方法名称 */
                method = "handleResultSets",
                /* 拦截的方法入参 */
                args = {Statement.class})
})
public class ResultSetHandlerPlugin implements Interceptor {

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        return invocation.proceed();
    }

}
