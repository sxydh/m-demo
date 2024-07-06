package cn.net.bhe.mybatisplugindemo.plugin;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

@Intercepts({
        /* 拦截的方法签名 */
        @Signature(
                /* 拦截的方法所在的类 */
                type = Executor.class,
                /* 拦截的方法名称 */
                method = "query",
                /* 拦截的方法入参 */
                args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class})
})
public class ExecutorPlugin implements Interceptor {

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        return invocation.proceed();
    }

}
