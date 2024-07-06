package cn.net.bhe.providerdemo.filter;

import org.apache.dubbo.rpc.*;

public class AppendFilter implements Filter {

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        Result result = invoker.invoke(invocation);
        result.setValue(result.getValue() + "'s customized " + AppendFilter.class);
        return result;
    }

}
