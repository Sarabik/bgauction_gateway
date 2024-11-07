package com.bgauction.gateway.filters;

import com.bgauction.gateway.security.JwtUtil;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.concurrent.TimeUnit;

@Component
public class LogoutFilter  implements GatewayFilter {

    private final RedisTemplate<String, String> redisTemplate;
    private final JwtUtil jwtUtil;

    public LogoutFilter(RedisTemplate<String, String> redisTemplate, JwtUtil jwtUtil) {
        this.redisTemplate = redisTemplate;
        this.jwtUtil = jwtUtil;
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
            long expiration = jwtUtil.getExpiration(token);
            long currentTime = System.currentTimeMillis();
            long timeRemains = expiration - currentTime;
            redisTemplate.opsForValue().set(token, "blacklisted", timeRemains, TimeUnit.MILLISECONDS);
            exchange.getResponse().setStatusCode(HttpStatus.OK);
        } else {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        }
        return exchange.getResponse().setComplete();
    }
}
