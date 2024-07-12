package cn.net.bhe.consumer.filter;

import org.apache.dubbo.rpc.*;

public class AppendFilter implements Filter {

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        return invoker.invoke(invocation);
    }

}
