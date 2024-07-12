package cn.net.bhe.provider.filter;

import cn.net.bhe.api.HelloService;
import org.apache.dubbo.rpc.*;

public class DemoFilter implements Filter {

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        Result result = invoker.invoke(invocation);
        if (invocation.getTargetServiceUniqueName().contains(HelloService.class.getSimpleName())) {
            result.setValue(result.getValue() + " Provider's " + DemoFilter.class.getSimpleName());
        }
        return result;
    }

}
