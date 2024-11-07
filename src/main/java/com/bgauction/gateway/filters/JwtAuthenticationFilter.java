package com.bgauction.gateway.filters;

import com.bgauction.gateway.security.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class JwtAuthenticationFilter implements GatewayFilter {

    private final RedisTemplate<String, String> redisTemplate;
    private final JwtUtil jwtUtil;
    private final String serviceInternalKey;

    public JwtAuthenticationFilter(RedisTemplate<String, String> redisTemplate, JwtUtil jwtUtil,
                                   @Value("${service.internal-key}") String serviceInternalKey) {
        this.redisTemplate = redisTemplate;
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

        if (isTokenBlacklisted(token)) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
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

    private boolean isTokenBlacklisted(String token) {
        Boolean isBlacklisted = redisTemplate.hasKey(token);
        return isBlacklisted != null && isBlacklisted;
    }
}
