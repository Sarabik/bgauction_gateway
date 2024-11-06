package com.bgauction.gateway.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class ServiceKeyAddingFilter implements GatewayFilter {

    private final String serviceInternalKey;

    public ServiceKeyAddingFilter(@Value("${service.internal-key}") String serviceInternalKey) {
        this.serviceInternalKey = serviceInternalKey;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest updatedRequest = exchange.getRequest().mutate()
                .header("X-Service-Key", serviceInternalKey)
                .build();
        return chain.filter(exchange.mutate().request(updatedRequest).build());
    }
}
