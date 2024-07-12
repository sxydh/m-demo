package cn.net.bhe.provider.filter;

import cn.net.bhe.api.HelloService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.rpc.*;

@Slf4j
public class DemoFilter implements Filter {

    public DemoFilter() {
        log.info("{}: new", DemoFilter.class.getName());
    }

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        log.info("Consumer token: {}", invocation.getAttachment("token"));
        Result result = invoker.invoke(invocation);
        log.info("{}: invoke={}", DemoFilter.class.getName(), result.getValue());
        return result;
    }

}
