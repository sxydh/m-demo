package cn.net.bhe.consumer.filter;

import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.rpc.*;

import java.util.UUID;

@Slf4j
public class DemoFilter implements Filter {

    public DemoFilter() {
        log.info("{}: new", DemoFilter.class.getName());
    }

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        log.info("{}: invoke", DemoFilter.class.getName());
        invocation.setAttachment("token", UUID.randomUUID().toString());
        return invoker.invoke(invocation);
    }

}
