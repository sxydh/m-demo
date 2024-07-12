package cn.net.bhe.provider.filter;

import cn.net.bhe.api.HelloService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.rpc.*;

@Slf4j
public class DemoFilter implements Filter {

    public DemoFilter() {
        log.info("{}: new", DemoFilter.class.getSimpleName());
    }

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        String token = RpcContext.getServerContext().getAttachment("token");
        log.info("Client token: {}", token);
        Result result = invoker.invoke(invocation);
        if (invocation.getTargetServiceUniqueName().contains(HelloService.class.getSimpleName())) {
            result.setValue(result.getValue() + " Provider's " + DemoFilter.class.getSimpleName());
        }
        return result;
    }

}
