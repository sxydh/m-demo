package cn.net.bhe.executordemo;

import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Log4j2
@Component
public class XxlJobHandler {

    @XxlJob("jobHandlerDemo")
    public void jobHandlerDemo() {
        log.info("jobHandlerDemo: {}", UUID.randomUUID().toString());
    }

}
