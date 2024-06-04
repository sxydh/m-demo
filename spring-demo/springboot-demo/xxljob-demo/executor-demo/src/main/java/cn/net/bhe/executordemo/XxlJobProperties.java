package cn.net.bhe.executordemo;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@Accessors(chain = true)
@ConfigurationProperties(prefix = "xxl.job")
public class XxlJobProperties {

    private String adminAddresses;
    private String accessToken;
    private String executorAppname;
    private String executorAddress;
    private String executorIp;
    private Integer executorPort;
    private String executorLogpath;
    private Integer executorLogretentiondays;

}
