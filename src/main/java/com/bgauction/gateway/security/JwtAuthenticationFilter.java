package com.bgauction.gateway.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class JwtAuthenticationFilter implements GatewayFilter {

    private final JwtUtil jwtUtil;
    private final String serviceInternalKey;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, @Value("${service.internal-key}") String serviceInternalKey) {
        this.jwtUtil = jwtUtil;
        this.serviceInternalKey = serviceInternalKey;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String token = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (token == null) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        if (token.startsWith("Bearer ")) {
            token = token.substring(7).trim();
        }

        if (jwtUtil.validateToken(token)) {
            String userIdFromToken = jwtUtil.extractUserId(token);

            ServerHttpRequest updatedRequest = exchange.getRequest().mutate()
                    .headers(headers -> headers.remove(HttpHeaders.AUTHORIZATION))
                    .header("X-Service-Key", serviceInternalKey)
                    .header("X-User-Id", userIdFromToken)
                    .build();

            return chain.filter(exchange.mutate().request(updatedRequest).build());
        } else {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }
}
