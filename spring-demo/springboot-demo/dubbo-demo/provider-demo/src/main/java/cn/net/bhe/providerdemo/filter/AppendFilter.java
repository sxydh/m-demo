package cn.net.bhe.providerdemo.filter;

import dubbodemo.api.HelloService;
import lombok.extern.log4j.Log4j2;
import org.apache.dubbo.rpc.*;

@Log4j2
public class AppendFilter implements Filter {

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        Result result = invoker.invoke(invocation);
        if (invocation.getTargetServiceUniqueName().contains(HelloService.class.getSimpleName())) {
            result.setValue(result.getValue() + "'s customized " + AppendFilter.class.getSimpleName());
        }
        return result;
    }

}
