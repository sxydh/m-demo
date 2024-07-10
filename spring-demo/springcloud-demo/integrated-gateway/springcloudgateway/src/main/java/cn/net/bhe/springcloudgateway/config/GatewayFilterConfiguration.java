package cn.net.bhe.springcloudgateway.config;

import org.springframework.cloud.gateway.config.conditional.ConditionalOnEnabledFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayFilterConfiguration {

    @Bean
    @ConditionalOnEnabledFilter
    public RequestRateLimiterGatewayFilterFactory requestRateLimiterGatewayFilterFactory() {
        return new RequestRateLimiterGatewayFilterFactory();
    }

}
