package cn.net.bhe.consumer.filter;

import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.rpc.*;

@Slf4j
public class DemoFilter implements Filter {

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        log.info("Consumer's {}: invoke", DemoFilter.class.getSimpleName());
        return invoker.invoke(invocation);
    }

}
