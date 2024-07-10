package cn.net.bhe.springcloudgateway.config;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;

import java.util.concurrent.ArrayBlockingQueue;

public class RequestRateLimiterGatewayFilterFactory extends AbstractGatewayFilterFactory<RequestRateLimiterGatewayFilterFactory.Config> {

    private final ArrayBlockingQueue<Object> queue = new ArrayBlockingQueue<>(5);

    public RequestRateLimiterGatewayFilterFactory() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            if (queue.offer(new Object())) {
                try {
                    return chain.filter(exchange);
                } finally {
                    queue.poll();
                }
            }
            ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
            return response.setComplete();
        };
    }

    public static class Config {
    }

}
