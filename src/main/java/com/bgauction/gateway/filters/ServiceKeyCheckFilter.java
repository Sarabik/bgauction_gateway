package com.bgauction.gateway.filters;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class ServiceKeyCheckFilter implements GatewayFilter {

    private final String serviceInternalKey;

    public ServiceKeyCheckFilter(@Value("${service.internal-key}") String serviceInternalKey) {
        this.serviceInternalKey = serviceInternalKey;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String serviceKey = exchange.getRequest().getHeaders().getFirst("X-Service-Key");

        if (serviceKey == null || !serviceKey.equals(serviceInternalKey)) {
            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
            return exchange.getResponse().setComplete();
        }
        return chain.filter(exchange);
    }
}
